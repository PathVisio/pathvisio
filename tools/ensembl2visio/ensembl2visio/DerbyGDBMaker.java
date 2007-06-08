// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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
package ensembl2visio;

import java.io.*;
import java.sql.*;
import java.util.zip.*;

import org.pathvisio.debug.StopWatch;


public class DerbyGDBMaker extends GDBMaker {
	public static String DB_NAME_IN_ZIP = "database";
	
	public DerbyGDBMaker(String dbName) {
		super(dbName);
	}   	
	
	public void connect(boolean create) throws SQLException, ClassNotFoundException {
		if(create) {
			//Remove old db files
			removeFiles(new File(getDbName()));
		}
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		con = DriverManager.getConnection("jdbc:derby:" + getDbName() + (create ? ";create=true" : ""));
	}

	void AddMetabolitesFromTxt(String file, String dbname) 
    {
		StopWatch timer = new StopWatch();    	
    	
    	info("Timer started");
    	timer.start();
    	try {
    		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    		con = DriverManager.getConnection("jdbc:derby:" + dbname);
        	
	    	con.setAutoCommit(false);
			pstGene = con.prepareStatement(
					"INSERT INTO gene " +
					"	(id, code," +
					"	 backpageText)" +
					"VALUES (?, ?, ?)"
			);
			pstLink = con.prepareStatement(
					"INSERT INTO link " +
					"	(idLeft, codeLeft," +
					"	 idRight, codeRight)" +
					"VALUES (?, ?, ?, ?)"
			);
			
			
			BufferedReader in = new BufferedReader(new FileReader(file));
			String l;
			l = in.readLine(); // skip header row 
			int codeIndex = -1;
			int error = 0;
			int progress = 0;
			int nCols = 8;
			String[] cols = new String[nCols];
	    	// Columns in input:
			// <
			while((l = in.readLine()) != null)
			{
				progress++;
				cols = l.split("\t", nCols);
				
				String idCas = cols[0]; // CAS no
				String id;
				String code;
				String bpText = "<TABLE border='1'>" +
    			"<TR><TH>Metabolite:<TH>" + cols[1] +
    			"<TR><TH>Bruto Formula:<TH>" + cols[3] + 
    			"</TABLE>";

				code = "Ca"; // CAS
				id = cols[0];		    	
				error += addGene(idCas, id, code, bpText);
				error += addLink(idCas, id, code);
					
				if (cols.length > 2 && cols[2].length() > 0)
				{
					code = "Ck"; // KEGG compound
					id = cols[2];		    	
					error += addGene(idCas, id, code, bpText);
					error += addLink(idCas, id, code);
				}
				
				if (cols.length > 7 && cols[7].length() > 0)
				{
					code = "Nw"; // NuGO wiki
					id = cols[7];		    	
					error += addGene(idCas, id, code, bpText);
					error += addLink(idCas, id, code);
				}
				
				if (cols.length > 5 && cols[5].length() > 0)
				{
					code = "Ce"; // ChEBI
					id = cols[5];		    	
					error += addGene(idCas, id, code, bpText);
					error += addLink(idCas, id, code);
				}
				
				if (cols.length > 6 && cols[6].length() > 0)
				{	
					code = "Cp"; // PuBCHEM
					id = cols[6];		    	
					error += addGene(idCas, id, code, bpText);
					error += addLink(idCas, id, code);
				}
				
				if (cols.length > 4 && cols[4].length() > 0)
				{
					code = "Ch"; // HMDB
					id = cols[4];		    	
					error += addGene(idCas, id, code, bpText);
					error += addLink(idCas, id, code);
				}
				
				if(progress % PROGRESS_INTERVAL == 0) {
					info("Processed " + progress + " lines");
					con.commit();
				}
			}
			con.commit();
					
			ResultSet r = con.createStatement().executeQuery("SELECT COUNT(*) FROM gene");
			r.next();		
			info("total ids in gene table: " + r.getString(1));
			info("total errors (duplicates): " + error);
			r.close();
			
			info("END processing text file");
			
			info("Compacting database");
			compact();
	    }
		catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getNextException().getMessage());
		}	
		catch (Exception e)
		{
			e.printStackTrace();
		}
		info("Closing connections");
				
    	close();
    	info("Timer stopped: " + timer.stop());    	
    }
	
	public void connect() throws ClassNotFoundException, SQLException {
		connect(true);
	}
	
	void removeFiles(File file) {
		if(!file.exists()) return;
		
		info("Removing old file: " + file.toString());
		if(file.isDirectory()) {
			for(File f : file.listFiles()) removeFiles(f);
		}
		file.delete();
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
				DriverManager.getConnection("jdbc:derby:" + getDbName() + ";shutdown=true");
				con.close();
			}
		} catch(Exception e) {
			error("Database shutdown", e);
		}
		super.close();
//		toZip();
	}
	
	public void compact() throws SQLException {
		con.commit();
		con.setAutoCommit(true);

		CallableStatement cs = con.prepareCall
		("CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, ?)");
		//Gene table
		cs.setString(1, "APP");
		cs.setString(2, "GENE");
		cs.setShort(3, (short) 1);
		cs.execute();
		
		//Link table
		cs.setString(1, "APP");
		cs.setString(2, "LINK");
		cs.setShort(3, (short) 1);
		cs.execute();
		
		con.commit(); //Just to be sure...
	}
	
	public void createIndices() throws SQLException {
		//LINK table
		Statement sh = con.createStatement();
		sh.execute(
				"CREATE INDEX i_right ON link(idright, coderight)"
		);
		sh.execute(
				"CREATE INDEX i_codeleft ON link(codeleft)"
		);
		sh.execute(
				"CREATE INDEX i_gene ON gene(id)"
		);
		sh.execute(
				"CREATE INDEX i_code ON gene(code)"
		);
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
