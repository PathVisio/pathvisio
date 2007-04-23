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
// $Id: MappToGmml.java,v 1.5 2005/10/21 12:33:27 gontran Exp $
package org.pathvisio.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.gui.Engine;
import org.pathvisio.data.ConvertType;
import org.pathvisio.data.DataSources;

/**
 * The class MappFormat is responsible for all interaction with 
 * .mapp files (GenMapp pathway format). Here is also codified all the
 * assumptions about the .mapp format.
 * 
 * @author Martijn, Thomas
 *
 */
public class MappFormat
{	
	private static final String sqlInfoInsert = 
		"INSERT INTO INFO (Title, MAPP, GeneDB, Version, Author, " +
		"Maint, Email, Copyright, Modify, Remarks, BoardWidth, BoardHeight, " +
		"WindowWidth, WindowHeight, Notes) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	// note: column GeneDBVersion is not in all mapps. 
	// Notably the mapps converted from kegg are different from the rest. 
	private static final String sqlObjectsInsert = 
		"INSERT INTO OBJECTS (ObjKey, ID, SystemCode, Type, CenterX, " + 
		"CenterY, SecondX, SecondY, Width, Height, Rotation, " +
		"Color, Label, Head, Remarks, Image, Links, Notes) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String sqlInfoSelect = 
		"SELECT Title, MAPP, GeneDB, Version, Author, " +
		"Maint, Email, Copyright, Modify, Remarks, BoardWidth, BoardHeight, " +
		"WindowWidth, WindowHeight, Notes " +
		"FROM INFO";
	private static final String sqlObjectsSelect = 
		"SELECT ObjKey, ID, SystemCode, Type, CenterX, " + 
		"CenterY, SecondX, SecondY, Width, Height, Rotation, " +
		"Color, Label, Head, Remarks, Image, Links, Notes " +
		"FROM OBJECTS";

	public static final String[] organism_latin_name = {
		"",
		"Mus musculus",
		"Homo sapiens",
		"Rattus norvegicus",
		"Bos taurus",
		"Caenorhabditis elegans",
		"Gallus gallus",
		"Danio rero",
		"Drosophila melanogaster",
		"Canis familiaris",
		"Xenopus tropicalis",
		"Arabidopsis thaliana"

	};

	static final String[] organism_short_code = {
		"___",
		"Mm_", 
		"Hs_", 
		"Rn_", 
		"Bt_", 
		"Ce_", 
		"Gg_", 
		"Dr_", 
		"Dm_", 
		"Cf_", 
		"Xt_", 
		"At_", 				
	};

    private static String database_after = ";DriverID=22;READONLY=true";
    private static String database_before =
            "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
    
    //  These constants below define columns in the info table.
    //  they are linked to the order of columns in the sqlInfoSelect 
    //  statement above
	static final int icolTitle = 0;
	static final int icolMAPP = 1;
	static final int icolGeneDB = 2;
	static final int icolVersion = 3;
	static final int icolAuthor = 4;
	static final int icolMaint = 5;
	static final int icolEmail = 6;
	static final int icolCopyright = 7;
	static final int icolModify = 8;
	static final int icolRemarks = 9;
	static final int icolBoardWidth = 10;
	static final int icolBoardHeight = 11;
	static final int icolWindowWidth = 12;
	static final int icolWindowHeight = 13;
	static final int icolNotes = 14;

	// these constants define the columns in the Objects table.
	// they are linked to the order of columns in the sqlObjectsSelect 
	// statement above.
	static final int colObjKey = 0;
	static final int colID = 1;
	static final int colSystemCode = 2;
	static final int colType = 3;
	static final int colCenterX = 4;
	static final int colCenterY = 5;
	static final int colSecondX = 6;
	static final int colSecondY = 7;
	static final int colWidth = 8;
	static final int colHeight = 9;
	static final int colRotation = 10;
	static final int colColor = 11;
	static final int colLabel = 12;
	static final int colHead = 13;
	static final int colRemarks = 14;
	static final int colImage = 15;
	static final int colLinks = 16;
	static final int colNotes = 17;

    /**
     * MAPPTmpl.gtp is a template access database for newly generated
     * mapp's. This file should be
     * in the classpath, normally in resources.jar.
     */
	private static String mappTemplateFile = "MAPPTmpl.gtp";
    
    static void readFromMapp (String filename, Pathway data)
    	throws ConverterException
    {
    	String database = database_before + filename + database_after;

    	try
    	{
	    	// Load Sun's jdbc-odbc driver
	        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
    	} catch (ClassNotFoundException cnfe)
    	{
    		// decoupling: wrap classnotfoundexception into converterexception
    		ConverterException ce = new ConverterException("Class not found exception in converter");
    		ce.setStackTrace(cnfe.getStackTrace());
    		throw ce;
    	}
        Engine.log.debug ("Connection string: " + database);
		
		// Create the connection to the database
        
        try 
        {
	        Connection con = DriverManager.getConnection(database, "", "");
	        
	        Statement s = con.createStatement();
	        
	        Engine.log.trace ("READING INFO TABLE");
	        // first do the INFO table, only one row.
		    {
		        ResultSet r = s.executeQuery(sqlInfoSelect);
		        r.next();
		        int cCol = r.getMetaData().getColumnCount();
		        String[] row = new String[cCol];
		        for (int i = 0; i < cCol; ++i) row[i] = r.getString(i + 1);
		        
		        copyMappInfo(row, data, filename);
	    	}    
	
		    Engine.log.trace ("READING OBJECTS TABLE");
	        // now do the OBJECTS table, multiple rows
	        {
		        ResultSet r = s.executeQuery(sqlObjectsSelect);
		        int cCol = r.getMetaData().getColumnCount();
		        String[] row = new String[cCol];
		        while (r.next())
		        {
		        	for (int i = 0; i < cCol; ++i) row[i] = r.getString(i + 1);
		        	copyMappObjects(row, data);
		        }
	        }
        }
        catch (SQLException sqle)
        {
        	// decoupling: wrap sqlexception into converterexception
    		ConverterException ce = new ConverterException("SQLException while converting");
    		ce.setStackTrace(sqle.getStackTrace());
    		throw ce;
        }
    }
    
    private static void copyResource(String resource, java.io.File destination) throws IOException 
    {
		try {
			ClassLoader cl = MappFormat.class.getClassLoader();
			InputStream inStream = cl.getResourceAsStream(resource);

			java.io.FileOutputStream outStream=new java.io.FileOutputStream(destination);

			int len;
			byte[] buf=new byte[2048];
			 
			while ((len=inStream.read(buf))!=-1) {
				outStream.write(buf,0,len);
			}
		} catch (Exception e) {
			throw new IOException("Can't copy resource "+mappTemplateFile+" -> "+destination+".\n" + e.getMessage());
		}
	}

    public static void exportMapp (String filename, 
    		String[] mappInfo, List<String[]> mappObjects)
    {    	
        String database = database_before + filename + ";DriverID=22";
        
        try {
        	copyResource (mappTemplateFile, new File(filename));
        	
            // Load Sun's jdbc-odbc driver
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            
            // Create the connection to the database
            Connection con = DriverManager.getConnection(database, "", "");
            
            // Create a new sql statement

    		PreparedStatement sInfo = con.prepareStatement(sqlInfoInsert);
            PreparedStatement sObjects = con.prepareStatement(sqlObjectsInsert);
            
            
            int k = 1;
            for (String[] row : mappObjects)
            {
    			sObjects.setInt (1, k);
    			for (int j = 1; j < row.length; ++j)
    			{
    				Engine.log.trace ("[" + (j + 1) + "] " + row[j]);
//    				System.err.println ("[" + (j + 1) + "] " + row[j]);
    				if (j >= 14 && j < 17)
    				{
    					if (row[j] != null && row[j].equals("")) row[j] = null;
    					sObjects.setObject(j + 1, row[j], Types.LONGVARCHAR);
    					// bug workaround, see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4401822
    				}
    				else
    				{
    					// the line below is a bugfix for 
    					// Hs_Contributed_20060824/cellular_process-GenMAPP/Hs_Signaling_of_Hepatocyte_Growth_Factor_Receptor_Biocarta.mapp. 
    					// No idea why this is necessary
    					if (row[j] == null) row[j] = "";
    					sObjects.setString(j + 1, row[j]);
    				}
    			}
    			
    			sObjects.executeUpdate();    			
    			k++;
            }

			for (int j = 0; j < mappInfo.length; ++j)
			{
				Engine.log.trace("[" + (j + 1) + "] " + mappInfo[j]);
				
				sInfo.setString (j + 1, mappInfo[j]);
			}    			
			sInfo.executeUpdate();
            con.close();
            
        } catch (ClassNotFoundException cl_ex) {
        	Engine.log.error ("-> Could not find the Sun JbdcObdcDriver\n");
        } catch (SQLException ex) {
        	Engine.log.error ("-> SQLException: "+ex.getMessage());        
            ex.printStackTrace();
        } catch (IOException e)
        {
        	Engine.log.error (e.getMessage());
        }
    }
    
	public static String[] uncopyMappInfo (Pathway data)
	{
		String[] mappInfo = new String[15];
		
		PathwayElement mi = null;
		for (PathwayElement o : data.getDataObjects())
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
				mi = o;
		}
			
		mappInfo[icolTitle] = mi.getMapInfoName();
		mappInfo[icolVersion] = mi.getVersion();
		mappInfo[icolAuthor] = mi.getAuthor();
		mappInfo[icolMaint] = mi.getMaintainer();
		mappInfo[icolEmail] = mi.getEmail();
		mappInfo[icolCopyright] = mi.getCopyright();
		mappInfo[icolModify] = mi.getLastModified();
		
		mappInfo[icolNotes] = mi.findComment("GenMAPP notes");
		mappInfo[icolRemarks] = mi.findComment("GenMAPP remarks");		
		
		mappInfo[icolBoardWidth] = "" + mi.getMBoardWidth();
		mappInfo[icolBoardHeight] = "" + mi.getMBoardHeight();
		mappInfo[icolWindowWidth] = "" + mi.getWindowWidth();
		mappInfo[icolWindowHeight] = "" + mi.getWindowHeight();
		
		return mappInfo;
	}
	
	// This method copies the Info table of the genmapp mapp to a new gpml
	// pathway
	public static void copyMappInfo(String[] row, Pathway data, String filename)
	{

		/* Data is lost when converting from GenMAPP to GPML:
		*
		* GenMAPP: 
		*		"Title", "MAPP", "Version", "Author",
		* 		"Maint", "Email", "Copyright","Modify", 
		*		"Remarks", "BoardWidth", "BoardHeight","WindowWidth",
		*		"WindowHeight", "GeneDB", "Notes"
		* GPML:    
		*		"Name", NONE, Version, "Author",  
		*		"MaintainedBy", "Email", "Availability", "LastModified",
		*		"Comment", "BoardWidth", "BoardHeight", NONE, 
		*		NONE, NONE, "Notes"
		*
		*/
	
		Engine.log.trace ("CONVERTING INFO TABLE TO GPML");
		
		PathwayElement o = data.getMappInfo();
		
		o.setMapInfoName(row[icolTitle]);
		o.setMapInfoDataSource("GenMAPP 2.0");
		o.setVersion(row[icolVersion]);
		o.setAuthor(row[icolAuthor]);
		o.setMaintainer(row[icolMaint]);
		o.setEmail(row[icolEmail]);
		o.setCopyright(row[icolCopyright]);
		o.setLastModified(row[icolModify]);
	
		o.addComment(row[icolNotes], "GenMAPP notes");
		o.addComment(row[icolRemarks], "GenMAPP remarks");

		o.setMBoardWidth(Double.parseDouble(row[icolBoardWidth]));
		o.setMBoardHeight(Double.parseDouble(row[icolBoardHeight]));
		o.setWindowWidth(Double.parseDouble(row[icolWindowWidth]));
		o.setWindowHeight(Double.parseDouble(row[icolWindowHeight]));
		
		// guess organism based on first three characters of filename
		String short_code = new File (filename).getName().substring(0, 3);
		if (code2organism.containsKey(short_code))
		{		
			o.setOrganism(code2organism.get(short_code));
		}
	}
       
	private static String mapBetween (String[] from, String[] to, String value) throws ConverterException
    {
    	for(int i=0; i < from.length; i++) 
		{
		    if(from[i].equals(value)) 
		    {
		    	return to[i];
		    }		    
		    else if (i == from.length-1) 
		    {
		    	throw new ConverterException ("'" + value + "' is invalid\n");
		    }
		}
    	return null;
    }

	public static List<String[]> uncopyMappObjects(Pathway data) throws ConverterException
	{
		List<String[]> result = new ArrayList<String[]>();
		
		for (PathwayElement o : data.getDataObjects())
		{
			int objectType = o.getObjectType();
			String[] row = new String[18];
			
			// init:
			row[colCenterX] = "0.0";
			row[colCenterY] = "0.0";
			row[colSecondX] = "0.0";
			row[colSecondY] = "0.0";
			row[colWidth] = "0.0";
			row[colHeight] = "0.0";
			row[colRotation] = "0.0";
			row[colColor] = "-1";
			
			switch (objectType)
			{
				case ObjectType.LINE:
					unmapNotesAndComments (o, row);
					unmapLineType(o, row);
					result.add(row);
					break;
				case ObjectType.DATANODE:	
					unmapNotesAndComments (o, row);
					unmapGeneProductType(o, row);
					result.add(row);
					break;
				case ObjectType.INFOBOX:
					unmapInfoBoxType(o, row);
					result.add(row);
					break;
				case ObjectType.LABEL:
					unmapNotesAndComments (o, row);
					unmapLabelType(o, row);
					result.add(row);
					break;
				case ObjectType.LEGEND:
					unmapLegendType(o, row);
					result.add(row);
					break;
				case ObjectType.SHAPE:
					
					unmapNotesAndComments (o, row);
					switch (o.getShapeType())
					{
						case BRACE:
							unmapBraceType(o, row);
							break;
						case OVAL:
						case ARC:
						case RECTANGLE:					
							unmapShapeType(o, row);
							break;
						case CELLA:
						case PROTEINB:
						case ORGANA:
						case ORGANB:
						case ORGANC:
							unmapFixedShapeType(o, row);
							break;
						case PENTAGON: //TODO: incorrect separation
						case HEXAGON:
						case RIBOSOME:
						case TRIANGLE:
						case VESICLE:							
							unmapComplexShapeType(o, row);
					}
					result.add(row);
					break;
			}
			
		}
				
		return result;
	}

	private static void unmapNotesAndComments(PathwayElement o, String[] row)
	{		
		row[colNotes] = o.findComment("GenMAPP notes");
		row[colRemarks] = o.findComment("GenMAPP remarks");		
	}
	
	private static void mapNotesAndComments(PathwayElement o, String[] row)
	{
        if (row[colNotes] != null &&
        		!row[colNotes].equals(""))
        {        	
    		o.addComment(row[colNotes], "GenMAPP notes");
        }

        if (row[colRemarks] != null &&
        		!row[colRemarks].equals(""))
        {            
    		o.addComment(row[colRemarks], "GenMAPP remarks");
        }
	}

	// This list adds the elements from the OBJECTS table to the new gpml
	// pathway
    public static void copyMappObjects(String[] row, Pathway data) throws ConverterException
    {

		// Create the GenMAPP --> GPML mappings list for use in the switch
		// statement

		List typeslist = Arrays.asList(new String[] { 
				"Arrow", "DottedArrow", "DottedLine", "Line",
				"Brace", "Gene", "InfoBox", "Label", "Legend", "Oval",
				"Rectangle", "TBar", "Receptor", "LigandSq",  "ReceptorSq",
				"LigandRd", "ReceptorRd", "CellA", "Arc", "Ribosome",
				"OrganA", "OrganB", "OrganC", "ProteinB", "Poly", "Vesicle"
		});
		PathwayElement o = null;		
		int index = typeslist.indexOf(row[colType]);		
		switch(index) {
		
				case 0: /*Arrow*/
				case 1: /*DottedArrow*/
				case 2: /*DottedLine"*/			
				case 3: /*Line*/
				case 11: /*TBar*/
				case 12: /*Receptor*/           
				case 13: /*LigandSq*/           
				case 14: /*ReceptorSq*/         
				case 15: /*LigandRd*/
				case 16: /*ReceptorRd*/
						o = mapLineType(row);
						mapNotesAndComments (o, row);
						break;							
				case 4: /*Brace*/
						o = mapBraceType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueId());
						break;							
				case 5: /*Gene*/
						o = mapGeneProductType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueId());
						break;																					
				case 6: /*InfoBox*/
						o = mapInfoBoxType (row, data);
						break;
				case 7: /*Label*/
						o = mapLabelType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueId());
						break;
				case 8: /*Legend*/
						o = mapLegendType(row);
						break;							
				case 9: /*Oval*/						
				case 10: /*Rectangle*/
				case 18: /*Arc*/
						o = mapShapeType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueId());
						break;							
				case 17: /*CellA*/
				case 19: /*Ribosome*/							
				case 20: /*OrganA*/							
				case 21: /*OrganB*/							
				case 22: /*OrganC*/
						o = mapFixedShapeType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueId());
						break;							
				case 23: /*ProteinB*/
				case 24: /*Poly*/
				case 25: /*Vesicle*/
						o = mapComplexShapeType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueId());
						break;
				default: 
						throw new ConverterException (
							"-> Type '" 
							+ row[colType]
							+ "' is not recognised as a GenMAPP type "
							+ "and is therefore not processed.\n");							
		}
		data.add(o);
    }

    
    private static void unmapLineType (PathwayElement o, String[] mappObject)
    {   	
    	int lineStyle = o.getLineStyle();
		LineType lineType = o.getLineType();
		String style = lineType.getMappName();
		if (lineStyle == LineStyle.DASHED && (lineType == LineType.ARROW || lineType == LineType.LINE))
			style = "Dotted" + style;
		
		mappObject[colType] = style;		
		mappObject[colCenterX] = "" + o.getMStartX();
    	mappObject[colCenterY] = "" + o.getMStartY();
    	mappObject[colSecondX] = "" + o.getMEndX();
    	mappObject[colSecondY] = "" + o.getMEndY();
    	unmapColor (o, mappObject);    	
    }

	private static void mapColor(PathwayElement o, String[] mappObject)
	{
        int i = Integer.parseInt(mappObject[colColor]);
        o.setTransparent(i < 0);
		o.setColor(ConvertType.fromMappColor(mappObject[colColor]));	
	}

	private static void unmapColor(PathwayElement o, String[] mappObject)
	{
		mappObject[colColor] = ConvertType.toMappColor(o.getColor(), o.isTransparent());	
	}

	private static Map<String,LineType> mappLineTypes = initMappLineTypes();
	
	static private Map<String,LineType> initMappLineTypes()
	{
		Map<String,LineType> result = new HashMap<String,LineType>();
		result.put ("DottedLine", LineType.LINE);
		result.put ("DottedArrow", LineType.ARROW);
		for (LineType l : LineType.values())
		{
			result.put (l.getMappName(), l);
		}
		return result;
	}
	
	private static PathwayElement mapLineType(String [] mappObject) throws ConverterException
	{		
    	PathwayElement o = new PathwayElement(ObjectType.LINE);
    	
		String type = mappObject[colType];
    	if(type.startsWith("Dotted"))
    	{
			o.setLineStyle(LineStyle.DASHED);
    	}
    	else
    	{
    		o.setLineStyle(LineStyle.SOLID);
    	}
    	    	
    	o.setLineType(mappLineTypes.get(type));		
        o.setMStartX(Double.parseDouble(mappObject[colCenterX]));       
        o.setMStartY(Double.parseDouble(mappObject[colCenterY]));
        o.setMEndX(Double.parseDouble(mappObject[colSecondX]));
        o.setMEndY(Double.parseDouble(mappObject[colSecondY]));
        mapColor(o, mappObject);        
        return o;
	}
    
	private static void unmapCenter (PathwayElement o, String[] mappObject)
	{
		mappObject[colCenterX] = "" + o.getMCenterX();
    	mappObject[colCenterY] = "" + o.getMCenterY();	
	}
	
	private static void mapCenter (PathwayElement o, String[] mappObject)
	{
		o.setMCenterX(Double.parseDouble(mappObject[colCenterX]));
		o.setMCenterY(Double.parseDouble(mappObject[colCenterY]));
	}

	private static void unmapRotation (PathwayElement o, String[] mappObject)
	{
		mappObject[colRotation] = "" + o.getRotation();
	}
	
	private static void mapRotation (PathwayElement o, String[] mappObject)
	{
		o.setRotation(Double.parseDouble(mappObject[colRotation]));
	}
	
	private static void unmapShape (PathwayElement o, String[] mappObject)
	{
    	unmapCenter(o, mappObject);    	
    	mappObject[colWidth] = "" + o.getMWidth();
    	mappObject[colHeight] = "" + o.getMHeight();	
	}

	private static void mapShape (PathwayElement o, String[] mappObject)
	{
    	mapCenter(o, mappObject);    	
    	o.setMWidth(Double.parseDouble(mappObject[colWidth]));
    	o.setMHeight(Double.parseDouble(mappObject[colHeight]));
	}
	
	private static void unmapShape_half (PathwayElement o, String[] mappObject)
	{
    	unmapCenter(o, mappObject);    	
    	mappObject[colWidth] = "" + o.getMWidth() / 2;
    	mappObject[colHeight] = "" + o.getMHeight() / 2;	
	}

	private static void mapShape_half (PathwayElement o, String[] mappObject)
	{
    	mapCenter(o, mappObject);    	
    	o.setMWidth(Double.parseDouble(mappObject[colWidth]) * 2);
    	o.setMHeight(Double.parseDouble(mappObject[colHeight]) * 2);	
	}

	private static void unmapBraceType (PathwayElement o, String[] mappObject) throws ConverterException
    {    	
    	mappObject[colType] = "Brace";    	
    	mappObject[colRotation] = "" + o.getOrientation();    	
    	unmapShape (o, mappObject);
    	unmapColor (o, mappObject);
    }

    private static PathwayElement mapBraceType(String[] mappObject) throws ConverterException
    {
    	PathwayElement o = new PathwayElement(ObjectType.SHAPE);
    	o.setShapeType (ShapeType.BRACE);
    	mapShape(o, mappObject);
    	mapColor(o, mappObject);
    	o.setOrientation((int)Double.parseDouble(mappObject[colRotation]));
        return o;          
    }
    
    private static void unmapGeneProductType (PathwayElement o, String[] mappObject) throws ConverterException
    {    	
    	mappObject[colType] = "Gene";
    	mappObject[colSystemCode] =
			mapBetween (DataSources.dataSources, DataSources.systemCodes, 
					o.getDataSource());

		mappObject[colHead] = o.getBackpageHead();
		mappObject[colID] = o.getGeneID();
		mappObject[colLabel] = o.getTextLabel();
		mappObject[colLinks] = o.getXref();    	
		unmapShape(o, mappObject);
    }
    
    private static PathwayElement mapGeneProductType(String[] mappObject) throws ConverterException
	{
    	PathwayElement o = new PathwayElement(ObjectType.DATANODE);
    	
    	String syscode = mappObject[colSystemCode];
    	if (syscode == null) syscode = "";
    	syscode = syscode.trim();
    	
        o.setDataSource(mapBetween (
				DataSources.systemCodes, DataSources.dataSources, syscode));  

        o.setBackpageHead(mappObject[colHead]);
        if (mappObject[colID] == null)
        {
        	o.setGeneID("");
        }
        else
        {
        	o.setGeneID(mappObject[colID]);
        }
        o.setTextLabel(mappObject[colLabel]);

        // TODO:  for some IDs the type is known, e.g. SwissProt is always a
		// protein, incorporate this knowledge to assign a type per ID
        o.setDataNodeType("GeneProduct");
        String xrefv = mappObject[colLinks];
        if (xrefv == null) { xrefv = ""; }
        o.setXref(xrefv);
        
        mapShape(o, mappObject);
        return o;			
	}
    
	private static PathwayElement mapInfoBoxType (String[] mappObject, Pathway data)
	{
    	PathwayElement o = data.getInfoBox();
        
    	mapCenter (o, mappObject);                
        return o;
	}
	
	private static void unmapInfoBoxType (PathwayElement o, String[] mappObject)
    {    	
    	mappObject[colType] = "InfoBox";
    	
    	unmapCenter (o, mappObject);
    }

	private static PathwayElement mapLegendType (String[] mappObject)
	{
    	PathwayElement o = new PathwayElement(ObjectType.LEGEND);
 
    	mapCenter (o, mappObject);
    	        
        return o;
	}
	
	private static void unmapLegendType (PathwayElement o, String[] mappObject)
    {    	
    	mappObject[colType] = "Legend";
    	
    	unmapCenter (o, mappObject);    	
    }

	private final static int styleBold = 1; 
	private final static int styleItalic = 2;
	private final static int styleUnderline = 4;
	private final static int styleStrikethru = 8;
    
    private static PathwayElement mapLabelType(String[] mappObject) 
    {
    	PathwayElement o = new PathwayElement(ObjectType.LABEL);

    	mapShape(o, mappObject);
    	mapColor(o, mappObject);
        
    	o.setTextLabel(mappObject[colLabel]);
        
    	if (mappObject[colID] == null)
    	{
    		o.setFontName("");
    	}
    	else
    	{
    		o.setFontName(mappObject[colID]);
    	}
    	
        
        o.setMFontSize(15.0 * Double.parseDouble(mappObject[colSecondX]));
        
        String styleString = mappObject[colSystemCode]; 
        int style = styleString == null ? 0 : (int)(styleString.charAt(0));
            
        o.setBold((style & styleBold) > 0);
        o.setItalic((style & styleItalic) > 0);
        o.setUnderline((style & styleUnderline) > 0);
        o.setStrikethru((style & styleStrikethru) > 0);
        
        
        String xrefv = mappObject[colLinks];
        if (xrefv == null) { xrefv = ""; }
        o.setXref(xrefv);
        return o;
    }

    private static void unmapLabelType (PathwayElement o, String[] mappObject)
    {    	
    	mappObject[colType] = "Label";
    	mappObject[colLabel] = o.getTextLabel();
    	
    	unmapShape(o, mappObject);
    	unmapColor(o, mappObject);
    	
    	mappObject[colID] = o.getFontName();
    	mappObject[colSecondX] = "" + (o.getMFontSize() / 15.0);
    	
    	int style = 16; 
    	// note: from VB source I learned that 16 is added to prevent field from becoming 0, 
    	// as this can't be stored in a text field in the database
    	if (o.isBold()) style |= styleBold;   	
    	if (o.isItalic()) style |= styleItalic;    	
    	if (o.isUnderline()) style |= styleUnderline;    	
    	if (o.isStrikethru()) style |= styleStrikethru;
    	
    	char stylechars[] = new char[1];
    	stylechars[0] = (char)style;
    	
    	mappObject[colSystemCode] = new String (stylechars);    	
		mappObject[colLinks] = o.getXref();    	
    }
    
	private static PathwayElement mapShapeType(String[] mappObject)
    {
    	PathwayElement o = new PathwayElement(ObjectType.SHAPE);
    	ShapeType shapeType = ShapeType.fromMappName(mappObject[colType]);
    	o.setShapeType(shapeType);        
    	if (shapeType == ShapeType.ARC || shapeType == ShapeType.OVAL)
    		mapShape_half (o, mappObject);
    	else
    		mapShape (o, mappObject);
		
        int i = Integer.parseInt(mappObject[colColor]);
        o.setTransparent(i < 0);
        if (shapeType == ShapeType.ARC)
        {
        	o.setColor(ConvertType.fromMappColor(mappObject[colColor]));
        }
        else
        {
        	o.setFillColor(ConvertType.fromMappColor(mappObject[colColor]));
        }        
		
        mapRotation (o, mappObject);        
        return o;
    }
    
    private static void unmapShapeType (PathwayElement o, String[] mappObject)
    {    	
    	ShapeType shapeType = o.getShapeType();
    	mappObject[colType] = ShapeType.toMappName(shapeType);
    	if (shapeType == ShapeType.ARC || shapeType == ShapeType.OVAL)
    		unmapShape_half (o, mappObject);
    	else
    		unmapShape (o, mappObject);
		
		// note: when converting gpml to mapp,
		// line color is discarded for oval and rect
    	if (shapeType == ShapeType.ARC)
    	{
    		mappObject[colColor] = ConvertType.toMappColor(o.getColor(), o.isTransparent());	
    	}
    	else
    	{
    		mappObject[colColor] = ConvertType.toMappColor(o.getFillColor(), o.isTransparent());
    	}
		unmapRotation (o, mappObject);    	
    }
    
    private static PathwayElement mapFixedShapeType(String[] mappObject)
    {
    	PathwayElement o = new PathwayElement(ObjectType.SHAPE);
        o.setShapeType(ShapeType.fromMappName(mappObject[colType]));
        mapShape (o, mappObject);
        return o;        
    }

    private static void unmapFixedShapeType (PathwayElement o, String[] mappObject)
    {    	
    	ShapeType shapeType = o.getShapeType();
    	mappObject[colType] = ShapeType.toMappName(shapeType);
    	
    	if (shapeType == ShapeType.CELLA)
    	{
    		mappObject[colRotation] = "-1.308997";
    		mappObject[colColor] = "0";
    		mappObject[colWidth] = "1500";
    		mappObject[colHeight] = "375";
    	}    	
    	unmapShape (o, mappObject);
    }
        
    private static PathwayElement mapComplexShapeType(String[] mappObject) throws ConverterException 
	{       		
    	PathwayElement o = new PathwayElement(ObjectType.SHAPE);
    	
    	if (mappObject[colType].equals("Poly"))
        {
        	switch ((int)Double.parseDouble(mappObject[colSecondY]))
        	{
        	case 3: o.setShapeType(ShapeType.TRIANGLE); break;
        	case 5: o.setShapeType(ShapeType.PENTAGON); break;
        	case 6: o.setShapeType(ShapeType.HEXAGON); break;
        	default: throw
        		new ConverterException ("Found polygon with unexpectec edge count: " + 
        				mappObject[colSecondY]); 
        	}
        }
    	else
    	{
    		o.setShapeType(ShapeType.fromMappName(mappObject[colType]));            
    	}
        
    	mapShape (o, mappObject);
        mapRotation (o, mappObject);
        return o;
    }
    
    private static void unmapComplexShapeType (PathwayElement o, String[] mappObject)
    {   
    	ShapeType shapeType = o.getShapeType();
    	mappObject[colType] = ShapeType.toMappName(shapeType);
 		
    	if (shapeType == ShapeType.TRIANGLE)
    	{
    		mappObject[colSecondY] = "3";
    	} else if (shapeType == ShapeType.PENTAGON)
    	{
    		mappObject[colSecondY] = "5";    		
		} else if (shapeType == ShapeType.HEXAGON)
		{
			mappObject[colSecondY] = "6";  			
		}
    	
    	unmapShape (o, mappObject);
        unmapRotation (o, mappObject);
    }
    
	/**
	 * {@link HashMap} containing mappings from system name (as used in Gpml) to system code
	 */
	private static final HashMap<String,String> code2organism = initOrganism2code();

	private static HashMap<String, String> initOrganism2code()
	{
		HashMap<String, String> result = new HashMap<String,String>();
		for(int i = 0; i < organism_latin_name.length; i++)
			result.put(organism_short_code[i], organism_latin_name[i]);
		return result;
	}
    
}
