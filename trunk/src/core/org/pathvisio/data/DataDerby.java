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
package org.pathvisio.data;

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

import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.util.FileUtils;

/**
   DBConnector implementation using the Derby driver, with the database in a
   single, uncompressed zip archive
*/
public class DataDerby extends DBConnector
{
	static final String DB_FILE_EXT_GDB = "pgdb";
	static final String DB_FILE_EXT_GEX = "pgex";

	String getDbExt() {
		switch(getDbType()) {
		case TYPE_GDB: return DB_FILE_EXT_GDB;
		case TYPE_GEX: return DB_FILE_EXT_GEX;
		default: return "";
		}
	}

	
	public static final String DB_NAME_IN_ZIP = "database";
	String lastDbName;
	
	public Connection createConnection(String dbName, int props) throws DataException 
	{
		boolean recreate = (props & PROP_RECREATE) != 0;
		if(recreate) {
			File dbFile = new File(dbName);
			FileUtils.deleteRecursive(dbFile);
		}
		
		Properties sysprop = System.getProperties();
		
		try
		{
			sysprop.setProperty("derby.storage.tempDirectory", System.getProperty("java.io.tmpdir"));
			sysprop.setProperty("derby.stream.error.file", File.createTempFile("derby",".log").toString());
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		}
		catch (ClassNotFoundException e)
		{
			throw new DataException (e);
		}
		catch (IOException f)
		{
			throw new DataException (f);
		}
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
		Connection con;
		try
		{
			con = DriverManager.getConnection(url, prop);
		}
		catch (SQLException e)
		{
			throw new DataException (e);
		}
		
		Logger.log.info("Connecting with derby to " + dbName + ":\t" + timer.stop());
		
		lastDbName = dbName;
		return con;
	}
	
	public Connection createNewDatabaseConnection(String dbName) throws DataException {
		return createConnection(FileUtils.removeExtension(dbName), PROP_RECREATE);
	}
	
	public String finalizeNewDatabase(String dbName) throws DataException {
		//Transfer db to zip and clear old dbfiles
		File dbDir = new File(FileUtils.removeExtension(dbName));
		try {
			DriverManager.getConnection("jdbc:derby:" + FileUtils.removeExtension(dbName) + ";shutdown=true");
		} catch(Exception e) {
			Logger.log.error("Database closed", e);
		}
		File zipFile = new File(dbName.endsWith(getDbExt()) ? dbName : dbName + "." + getDbExt());
		toZip(zipFile, dbDir);
		
		FileUtils.deleteRecursive(dbDir);
		
		//Return new database file
		return zipFile.toString();
	}
	
	public void closeConnection(Connection con) throws DataException {
		closeConnection(con, PROP_NONE);
	}
	
	public void closeConnection(Connection con, int props) throws DataException {
		if(con != null) 
		{
			try
			{
				if(lastDbName != null) 
					DriverManager.getConnection("jdbc:derby:" + lastDbName + ";shutdown=true");
				con.close();
			}
			catch (SQLException e)
			{
				throw new DataException (e);
			}
		}
	}
	
	public void compact(Connection con) throws DataException 
	{
		try
		{
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
		catch (SQLException e)
		{
			throw new DataException (e); 
		}
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
	
}
