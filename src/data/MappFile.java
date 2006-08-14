package data;

import java.io.*;
import java.sql.*;
import java.util.*;
import debug.Logger;

public class MappFile {
    
	public static Logger log;
	
    private static String database_after = ";DriverID=22;READONLY=true";
    private static String database_before =
            "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
    // This method returns the OBJECTS data of the given .MAPP file as a Nx18 string array
    public static String[][] importMAPPObjects(String filename) {
        log.trace ("IMPORTING OBJECTS TABLE OF MAPP FILE '"+filename+"'");
        String database = database_before + filename + database_after;
		String[] headers = {"ObjKey", "ID", "SystemCode", "Type", "CenterX",
                "CenterY",
                "SecondX", "SecondY", "Width", "Height", "Rotation",
                "Color", "Label", "Head",
                "Remarks", "Image", "Links", "Notes"};
                String[][] result = null;

                try {
                    // Load Sun's jdbc-odbc driver
                    Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                    
                    log.debug ("Connection string: " + database);
					
					// Create the connection to the database
                    Connection con = DriverManager.getConnection(database, "", "");
                    
                    // Create a new sql statement
                    Statement s = con.createStatement();
                    // Count the rows of the column "Type" (has an instance for every object)
                    ResultSet r = s.executeQuery(
                            "SELECT COUNT(Type) AS rowcount FROM Objects");
                    r.next();
                    int nrRows = r.getInt("rowcount")+1;
                    // now create a nrRows*18 string array
                    result = new String[nrRows + 1][headers.length];
                    result[0] = headers;
                    // and fill it
                    for (int j = 0; j < headers.length; j++) {
                        r = s.executeQuery("SELECT " + headers[j] + " FROM Objects");
                        for (int i = 1; i < nrRows; i++) {
                            r.next();
                            result[i][j] = r.getString(1);
                            //GUI.GUIframe.textOut.append("added " + result[i][j] + " to " +
                            //        headers[j] + " at row " + i+"\n");
                        }
                    }
                    r.close();
                    con.close();
                } catch (SQLException ex) {
                    log.error ("-> SQLException: "+ex.getMessage());
                    log.error ("-> Could not import data from file '"+filename+"' due to an SQL exception \n"+ex.getMessage()+"\n");
					ex.printStackTrace();
                } catch (ClassNotFoundException cl_ex) {
                    log.error ("-> Could not find the Sun JbdcObdcDriver\n");
                }
                return result;
    }
    
    private static void copyFile(java.io.File source, java.io.File destination) throws IOException 
    {
		try {
			java.io.FileInputStream inStream=new java.io.FileInputStream(source);
			java.io.FileOutputStream outStream=new java.io.FileOutputStream(destination);

			int len;
			byte[] buf=new byte[2048];
			 
			while ((len=inStream.read(buf))!=-1) {
				outStream.write(buf,0,len);
			}
		} catch (Exception e) {
			throw new IOException("Can't copy file "+source+" -> "+destination+".\n" + e.getMessage());
		}
	}
    
    public static void exportMapp (String filename, 
    		String[][] mappInfo, List mappObjects)
    {
    	File template = new File("E:\\prg\\gmml\\trunk\\gmml2mapp\\MAPPTmpl.gtp");
    	
        String database = database_before + filename + ";DriverID=22";
        
        try {
        	copyFile (template, new File(filename));
        	
            // Load Sun's jdbc-odbc driver
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            
            // Create the connection to the database
            Connection con = DriverManager.getConnection(database, "", "");
            
            // Create a new sql statement
            PreparedStatement sInfo = con.prepareStatement(
            		"INSERT INTO INFO (Title, MAPP, GeneDB, GeneDBVersion, Version, Author, " +
            		"Maint, Email, Copyright, Modify, Remarks, BoardWidth, BoardHeight, " +
            		"WindowWidth, WindowHeight, Notes) " +
            		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            	);
            PreparedStatement sObjects = con.prepareStatement(
            		"INSERT INTO OBJECTS (ObjKey, ID, SystemCode, Type, CenterX, " + 
            		"CenterY, SecondX, SecondY, Width, Height, Rotation, " +
            		"Color, Label, Head, Remarks, Image, Links, Notes) " +
            		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
            Iterator it = mappObjects.iterator();
            int k = 1;
            while (it.hasNext())
            {
    			String[] row = (String[])(it.next());
    			
    			sObjects.setInt (1, k);
    			for (int j = 1; j < row.length; ++j)
    			{
    				
    				log.trace("[" + (j + 1) + "] " + row[j]);
    				if (j >= 14)
    				{
    					sObjects.setObject(j + 1, row[j], Types.LONGVARCHAR);
    					// bug workaround, see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4401822
    				}
    				else
    				{
    					sObjects.setString(j + 1, row[j]);
    				}
    			}
    			
    			sObjects.executeUpdate();    			
    			k++;
            }

    		for (int i = 1; i < mappInfo.length; ++i)
    		{    			
    			for (int j = 0; j < mappInfo[i].length; ++j)
    			{
    				sInfo.setString (j + 1, mappInfo[i][j]);
    			}    			
    			sInfo.executeUpdate();
    		}
            con.close();
            
        } catch (ClassNotFoundException cl_ex) {
            log.error ("-> Could not find the Sun JbdcObdcDriver\n");
        } catch (SQLException ex) {
            log.error ("-> SQLException: "+ex.getMessage());        
            ex.printStackTrace();
        } catch (IOException e)
        {
        	log.error (e.getMessage());
        }
    }
    
    // This method returns the INFO data of the given .MAPP file as a 1x16 string array
    public static String[][] importMAPPInfo(String filename) {
        log.trace ("IMPORTING INFO TABLE OF MAPP FILE '"+filename+"'");
        String database = database_before + filename + database_after;
        String[] headers = {"Title", "MAPP", "GeneDB", "GeneDBVersion", "Version", "Author",
                "Maint", "Email", "Copyright",
                "Modify", "Remarks", "BoardWidth", "BoardHeight",
                "WindowWidth", "WindowHeight", "Notes"};
        String[][] result = null;
        
        try {
                    // Load Sun's jdbc-odbc driver
                    Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                    
                    // Create the connection to the database
                    Connection con = DriverManager.getConnection(database, "", "");
                    
                    // Create a new sql statement
                    Statement s = con.createStatement();
                    
                    // now create a nrRows*18 string array
                    result = new String[2][headers.length];
                    result[0] = headers;
                    ResultSet r = null;
                    // and fill it
                    for (int j = 0; j < headers.length; j++) {
                        r = s.executeQuery("SELECT " + headers[j] + " FROM Info");
                        r.next();
                        result[1][j] = r.getString(1);
                    }
                    r.close();
                    con.close();
                } catch (ClassNotFoundException cl_ex) {
                    log.error ("-> Could not find the Sun JbdcObdcDriver\n");
                } catch (SQLException ex) {
                    log.error ("-> SQLException: "+ex.getMessage());
                    log.error ("-> Could not import data from file '"+filename+"' due to an SQL exception:\n"+ex.getMessage()+"\n");
                }
                return result;
                
    }    
}
