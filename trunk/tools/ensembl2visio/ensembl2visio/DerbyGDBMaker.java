package ensembl2visio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class DerbyGDBMaker extends GDBMaker {
	public static String DB_NAME_IN_ZIP = "database";
	
	public DerbyGDBMaker(String txtFile, String dbName) {
		super(txtFile, dbName);
	}   	
	
	public void connect() throws ClassNotFoundException, SQLException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		con = DriverManager.getConnection("jdbc:derby:" + getDbName() + ";create=true");
	}
	
	String dbName;
	String getDbName() {
		File dbf = new File(super.getDbName());
		File newPath = new File(dbf.getParentFile(), "derby");
		newPath.mkdir();
		dbf = new File(newPath, dbf.getName());
		dbName = dbf.toString();
		return dbName;
	}

	public void close() {
		try {
			if(con != null) {
				con.close();
				DriverManager.getConnection("jdbc:derby:" + getDbName() + ";shutdown=true");
			}
		} catch(Exception e) {
			error("Database shutdown", e);
		}
		super.close();
		toZip();
	}
	
	public void compact() throws Exception {
		info("Compressing tables");
		//TODO
		info("END Compressing tables");
	}
	
	public void createIndices() throws SQLException {
		//Derby automatically creates indices for all primary keys (also combined)
	}
	
	void toZip() {
		try {
			File dbDir = new File(getDbName());
			//extention becomes .pgdb for gdb and .pgex for gex
			File zipFile = new File(getDbName() + ".pgdb");
			
			if(zipFile.exists()) zipFile.delete();
			
			zipFiles(zipFile, dbDir);
			
			String zipPath = zipFile.getAbsolutePath().replace(File.separatorChar, '/');
			String url = "jdbc:derby:jar:(" + zipPath + ")" + DB_NAME_IN_ZIP;

			DriverManager.getConnection(url);
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	void zipFiles(File zipFile, File dir) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
		out.setMethod(ZipOutputStream.STORED);
		for(File f : dir.listFiles()) addFiles(f, DB_NAME_IN_ZIP + '/', out);
		out.closeEntry();
		out.close();
	}
	
	byte[] buf = new byte[1024];
	void addFiles(File file, String dir, ZipOutputStream out) throws IOException {
		if(file.isDirectory()) {
			if(file.getName().equals("tmp")) return; //Skip 'tmp' directory
			
			String newDir = dir + file.getName() + '/';
			ZipEntry add = new ZipEntry(newDir);
			setZipEntryAttributes(file, add);
			out.putNextEntry(add);
			
			for(File f : file.listFiles()) addFiles(f, newDir,out);
		} else {
			if(file.getName().endsWith(".lck")) return; //Skip '*.lck' files
			ZipEntry add = new ZipEntry(dir + file.getName());
			setZipEntryAttributes(file, add);
			out.putNextEntry(add);
				        
			FileInputStream in = new FileInputStream(file);
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
		}
	}
	
	void setZipEntryAttributes(File f, ZipEntry z) throws IOException {
		z.setTime(f.lastModified());
		z.setMethod(ZipEntry.STORED);
				
		if(f.isDirectory()) {
			z.setCrc(0);
			z.setSize(0);
			z.setCompressedSize(0);
		} else {			
			z.setSize(f.length());
			z.setCompressedSize(f.length());
			z.setCrc(computeCheckSum(f));
		}
	}
	
	long computeCheckSum(File f) throws IOException {
		CheckedInputStream cis = new CheckedInputStream(
				new FileInputStream(f), new CRC32());
		byte[] tempBuf = new byte[128];
		while (cis.read(tempBuf) >= 0) { }
		return cis.getChecksum().getValue();
	}
}
