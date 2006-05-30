package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class GmmlGdb {
	final static File propsFile = new File("gdb.properties");
	public Connection con;
	public Properties props;
	
	File gdbFile;
	
	public GmmlGdb() {
		props = new Properties();
		try {
			// Check if properties file exists
			if(propsFile.canRead()) {
				props.load(new FileInputStream(propsFile));
			} else {
				// Create properties file
				propsFile.createNewFile();
				props.setProperty("currentGdb", "none");
			}
			if(!props.get("currentGdb").equals("none"))
			{
				gdbFile = new File((String)props.get("currentGdb"));
				if(connect(null) != null)
					setCurrentGdb("none");
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void setCurrentGdb(String gdb) {
		changeProps("currentGdb", gdb);
	}
	
	public void changeProps(String name, String value) {
		props.setProperty(name, value);
		try {
			props.store(new FileOutputStream(propsFile),"Gene Database Properties");
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getBpInfo(String id) {
		try {
			Statement s = con.createStatement();
			ResultSet r = s.executeQuery("SELECT backpageText FROM gene " +
					"WHERE id = '" + id + "'");
			r.next();
			String result = r.getString(1);
			return result;
		} catch(Exception e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList ensId2Refs(String ensId) {
		ArrayList crossIds = new ArrayList();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight FROM link " +
					"WHERE idLeft = '" + ensId + "'"
					);
			while(r1.next()) {
				crossIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return crossIds;
	}
	
//	Don't use this, multiple simple select queries is faster
//	Subsequentially use ref2EnsIds ensIds2Refs
	public ArrayList getCrossRefs(String id) {
		ArrayList crossIds = new ArrayList();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight FROM link " +
					"WHERE idLeft IN ( 		  " +
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + id + "')"
					);
			while(r1.next()) {
				crossIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return crossIds;
	}
	
	public ArrayList ref2EnsIds(String ref)
	{
		ArrayList ensIds = new ArrayList();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + ref + "'"
			);
			while(r1.next()) {
				ensIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ensIds;
	}
	
//	Don't use this, multiple simple select queries is faster
	public HashMap getEns2RefHash(ResultSet r, int ensIndex)
	{
		HashMap ens2RefHash = new HashMap();
		try {
			int pr = 0;
			StringBuilder idString = new StringBuilder();
			while(r.next()) {
				idString.append("'" + r.getString(ensIndex) + "', ");
				pr++;
				if(pr % 500 == 0) {
					System.out.println(pr);
				}
			}
			String ids = idString.substring(0,idString.lastIndexOf(", "));
		long t = System.currentTimeMillis();
		ResultSet r1 = con.createStatement().executeQuery(
				"SELECT idLeft, idRight FROM link WHERE idLeft IN ( " +
				ids + ")"
		);
		System.out.println("Query to find reference ids for genes took: " +
				(System.currentTimeMillis()-t) + " ms");
		while(r1.next()) {
			String id = r1.getString(1);
			if(ens2RefHash.containsKey(id)) {
				((ArrayList)ens2RefHash.get(id)).add(r1.getString(2));
			} else {
				ArrayList refIds = new ArrayList();
				refIds.add(r1.getString(2));
				ens2RefHash.put(id, refIds);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ens2RefHash;
	}
	
//	Don't use this, multiple simple select queries is faster
	public HashMap getRef2EnsHash(ResultSet r, int refIndex)
	{
		HashMap ref2EnsHash = null;
		try {
			int pr = 0;
			StringBuilder idString = new StringBuilder();
			while(r.next()) {
				idString.append("'" + r.getString(refIndex) + "', ");
				pr++;
				if(pr % 500 == 0) {
					System.out.println(pr);
				}
			}
			String ids = idString.substring(0,idString.lastIndexOf(", "));
			
			ref2EnsHash = new HashMap();
			long t = System.currentTimeMillis();
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idLeft, idRight FROM link WHERE idRight IN ( " +
					ids + ")"
			);
			System.out.println("Query to find ensembl ids for genes took: " +
					(System.currentTimeMillis()-t) + " ms");
			while(r1.next()) {
				String id = r1.getString(2);
				if(ref2EnsHash.containsKey(id)) {
					((ArrayList)ref2EnsHash.get(id)).add(r.getString(1));
				} else {
					ArrayList ensIds = new ArrayList();
					ensIds.add(r1.getString(1));
					ref2EnsHash.put(id, ensIds);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ref2EnsHash;
	}
	
	public String connect(File gdbFile)
	{
		if(gdbFile == null)
			gdbFile = this.gdbFile;
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Properties prop = new Properties();
			prop.setProperty("user","sa");
			prop.setProperty("password","");
			//prop.setProperty("hsqldb.default_table_type","cached");
			String file = gdbFile.getAbsolutePath().toString();
			con = DriverManager.getConnection("jdbc:hsqldb:file:" + 
					file.substring(0, file.lastIndexOf(".")), prop);
			con.setReadOnly(true);
			setCurrentGdb(gdbFile.getAbsoluteFile().toString());
			return null;
		} catch(Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public void close()
	{
		if(con != null)
		{
			try
			{
				Statement sh = con.createStatement();
				sh.executeQuery("SHUTDOWN"); // required, to write last changes
				sh.close();
				con = null;
			} catch (Exception e) {
				System.out.println ("Error: " +e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
}
