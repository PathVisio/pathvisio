package ensembl2visio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class HsqldbGDBMaker extends GDBMaker {
	
	public HsqldbGDBMaker(String txtFile, String dbName) {
		super(txtFile, dbName);
	}

    public void connect() throws ClassNotFoundException, SQLException {
    	removeOldFiles();
    	Properties prop = new Properties();
    	prop.setProperty("user","sa");
    	prop.setProperty("password","");
    	prop.setProperty("hsqldb.default_table_type","cached");
   
    	Class.forName("org.hsqldb.jdbcDriver");
    	con = DriverManager.getConnection("jdbc:hsqldb:file:" + getDbName(), prop);
    	con.setAutoCommit(true);
    }

    void removeOldFiles() {
    	new File(getDbName() + ".properties").delete();
    }
    
	public void close() {
		try {
			if(con != null) {
				con.commit();
				Statement sh = con.createStatement();
				sh.execute("SHUTDOWN COMPACT");
			}
		} catch(Exception e) {
			error("Unable to shutdown and compact", e);
		}
		super.close();
	}
	
	public void createGDBFromTxt(String file, String dbname) {
		super.createGDBFromTxt(file, dbname);
		setPropertyReadOnly(dbname, true);
	}
	
	String getDbName() {
		return "hsqldb/" + super.getDbName();
	}
	
	public void setPropertyReadOnly(String dbname, boolean readonly) {
		Properties prop = new Properties();
		File propFile = new File(dbname + ".properties");
		try {
			prop.load(new FileInputStream(propFile));
			prop.setProperty("hsqldb.files_readonly", Boolean.toString(readonly));
			prop.store(new FileOutputStream(propFile), "HSQL Database Engine");
		} catch (Exception e) {
			error("Unable to set readonly to " + readonly, e);
		}
    }
}
