// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package data;

import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import util.FileUtils;
import debug.StopWatch;

public class DBConnDerby extends DBConnector {
	static final String DB_FILE_EXT_GDB = "pgdb";
	static final String DB_FILE_EXT_GEX = "pgex";
	static final String[] DB_EXTS_GEX = new String[] { "*." + DB_FILE_EXT_GEX, "*.*"};
	static final String[] DB_EXTS_GDB = new String[] { "*." + DB_FILE_EXT_GDB, "*.*"};
	static final String[] DB_EXT_NAMES_GEX = new String[] { "Expression dataset", "All files" };
	static final String[] DB_EXT_NAMES_GDB = new String[] { "Gene database", "All files" };
	
	public static final String DB_NAME_IN_ZIP = "database";
	String lastDbName;
	
	public Connection createConnection(String dbName) throws Exception {
		return createConnection(dbName, PROP_NONE);
	}
	
	public Connection createConnection(String dbName, int props) throws Exception {
		boolean recreate = (props & PROP_RECREATE) != 0;
		if(recreate) {
			File dbFile = new File(dbName);
			FileUtils.deleteRecursive(dbFile);
		}
		
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties prop = new Properties();
		prop.setProperty("create", Boolean.toString(recreate));
		
		StopWatch timer = new StopWatch();
		timer.start();
		
		String url = "jdbc:derby:";
		File dbFile = new File(dbName);
		if(dbFile.isDirectory() || recreate) {
			url += dbName;
		} else {
			url += "jar:(" + dbFile.toString() + ")" + DB_NAME_IN_ZIP;
		}
		Connection con = DriverManager.getConnection(url, prop);
		
		GmmlVision.log.info("Connecting with derby to " + dbName + ":\t" + timer.stop());
		
		lastDbName = dbName;
		return con;
	}
	
	public Connection createNewDatabaseConnection(String dbName) throws Exception {
		return createConnection(FileUtils.removeExtension(dbName), PROP_RECREATE);
	}
	
	public String finalizeNewDatabase(String dbName) throws Exception {
		//Transfer db to zip and clear old dbfiles
		File dbDir = new File(FileUtils.removeExtension(dbName));
		try {
			DriverManager.getConnection("jdbc:derby:" + FileUtils.removeExtension(dbName) + ";shutdown=true");
		} catch(Exception e) {
			GmmlVision.log.error("Database closed", e);
		}
		File zipFile = new File(dbName.endsWith(getDbExt()) ? dbName : dbName + "." + getDbExt());
		toZip(zipFile, dbDir);
		
		FileUtils.deleteRecursive(dbDir);
		
		//Return new database file
		return zipFile.toString();
	}
	
	public void closeConnection(Connection con) throws SQLException {
		closeConnection(con, PROP_NONE);
	}
	
	public void closeConnection(Connection con, int props) throws SQLException {
		if(con != null) {
			if(lastDbName != null) 
				DriverManager.getConnection("jdbc:derby:" + lastDbName + ";shutdown=true");
			con.close();
		}
	}
	
	public void compact(Connection con) throws SQLException {
		con.setAutoCommit(true);

		CallableStatement cs = con.prepareCall
		("CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, ?)");
		//Expression table
		cs.setString(1, "APP");
		cs.setString(2, "EXPRESSION");
		cs.setShort(3, (short) 1);
		cs.execute();
		
		con.commit(); //Just to be sure...
	}
		
	void toZip(File zipFile, File dbDir) {
		try {			
			if(zipFile.exists()) zipFile.delete();
			
			zipFiles(zipFile, dbDir);
			
			String zipPath = zipFile.getAbsolutePath().replace(File.separatorChar, '/');
			String url = "jdbc:derby:jar:(" + zipPath + ")" + DB_NAME_IN_ZIP;

			DriverManager.getConnection(url);
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	void zipFiles(File zipFile, File dir) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
		out.setMethod(ZipOutputStream.STORED);
		for(File f : dir.listFiles()) addFiles(f, DB_NAME_IN_ZIP + '/', out);
		out.closeEntry();
		out.close();
	}
	
	byte[] buf = new byte[1024];
	void addFiles(File file, String dir, ZipOutputStream out) throws Exception {
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
	
	String getDbExt() {
		switch(getDbType()) {
		case TYPE_GDB: return DB_FILE_EXT_GDB;
		case TYPE_GEX: return DB_FILE_EXT_GEX;
		default: return "";
		}
	}
	String[] getDbExts() {
		switch(getDbType()) {
		case TYPE_GDB: return DB_EXTS_GDB;
		case TYPE_GEX: return DB_EXTS_GEX;
		default: return null;
		}
	}
	
	String[] getDbExtNames() {
		switch(getDbType()) {
		case TYPE_GDB: return DB_EXT_NAMES_GDB;
		case TYPE_GEX: return DB_EXT_NAMES_GEX;
		default: return null;
		}
	}
	
	public String openChooseDbDialog(Shell shell) {
		FileDialog fd = createFileDialog(shell, SWT.OPEN, getDbExts(), getDbExtNames());
		return fd.open();
	}

	public String openNewDbDialog(Shell shell, String defaultName) {
		FileDialog fd = createFileDialog(shell, SWT.SAVE, getDbExts(), getDbExtNames());
		if(defaultName != null) fd.setFileName(defaultName);
		return fd.open();
	}
}
