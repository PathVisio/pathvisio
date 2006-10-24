// $Id: MappToGmml.java,v 1.5 2005/10/21 12:33:27 gontran Exp $
package data;

import gmmlVision.GmmlVision;

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

    public final static String[] systemCodes = 
	{ 
	"D", "F", "G", "I", "L", "M",
	"Q", "R", "S", "T", "U",
	"W", "Z", "X", "En", "Em", 
	"H", "Om", "Pd", "Pf", "O", ""
	};

    public final static String[] dataSources = 
	{
	"SGD", "FlyBase", "GenBank", "InterPro" ,"Entrez Gene", "MGI",
	"RefSeq", "RGD", "SwissProt", "GeneOntology", "UniGene",
	"WormBase", "ZFIN", "Affy", "Ensembl", "EMBL", 
	"HUGO", "OMIM", "PDB", "Pfam", "Other", ""
	};

    /**
     * MAPPTmpl.gtp is a template access database for newly generated
     * mapp's. This file should be
     * in the classpath, normally in resources.jar.
     */
	private static String mappTemplateFile = "MAPPTmpl.gtp";
    
    static void readFromMapp (String filename, GmmlData data)
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
        GmmlVision.log.debug ("Connection string: " + database);
		
		// Create the connection to the database
        
        try 
        {
	        Connection con = DriverManager.getConnection(database, "", "");
	        
	        Statement s = con.createStatement();
	        
	        GmmlVision.log.trace ("READING INFO TABLE");
	        // first do the INFO table, only one row.
		    {
		        ResultSet r = s.executeQuery(sqlInfoSelect);
		        r.next();
		        int cCol = r.getMetaData().getColumnCount();
		        String[] row = new String[cCol];
		        for (int i = 0; i < cCol; ++i) row[i] = r.getString(i + 1);
		        
		        copyMappInfo(row, data);
	    	}    
	
		    GmmlVision.log.trace ("READING OBJECTS TABLE");
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
    				
    				GmmlVision.log.trace("[" + (j + 1) + "] " + row[j]);
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
				GmmlVision.log.trace("[" + (j + 1) + "] " + mappInfo[j]);
				
				sInfo.setString (j + 1, mappInfo[j]);
			}    			
			sInfo.executeUpdate();
            con.close();
            
        } catch (ClassNotFoundException cl_ex) {
        	GmmlVision.log.error ("-> Could not find the Sun JbdcObdcDriver\n");
        } catch (SQLException ex) {
        	GmmlVision.log.error ("-> SQLException: "+ex.getMessage());        
            ex.printStackTrace();
        } catch (IOException e)
        {
        	GmmlVision.log.error (e.getMessage());
        }
    }
    
	public static String[] uncopyMappInfo (GmmlData data)
	{
		String[] mappInfo = new String[15];
		
		GmmlDataObject mi = null;
		for (GmmlDataObject o : data.dataObjects)
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
				mi = o;
		}
			
		mappInfo[icolTitle] = mi.getMapInfoName();
		mappInfo[icolVersion] = mi.getVersion();
		mappInfo[icolAuthor] = mi.getAuthor();
		mappInfo[icolMaint] = mi.getMaintainedBy();
		mappInfo[icolEmail] = mi.getEmail();
		mappInfo[icolCopyright] = mi.getAvailability();
		mappInfo[icolModify] = mi.getLastModified();
		
		mappInfo[icolNotes] = mi.getNotes();
		mappInfo[icolRemarks] = mi.getComment();		
		
		mappInfo[icolBoardWidth] = "" + mi.getBoardWidth() *  GmmlData.GMMLZOOM;
		mappInfo[icolBoardHeight] = "" + mi.getBoardHeight() *  GmmlData.GMMLZOOM;
		mappInfo[icolWindowWidth] = "" + mi.getWindowWidth() *  GmmlData.GMMLZOOM;
		mappInfo[icolWindowHeight] = "" + mi.getWindowHeight() *  GmmlData.GMMLZOOM;
		
		return mappInfo;
	}
	
	// This method copies the Info table of the genmapp mapp to a new gmml
	// pathway
	public static void copyMappInfo(String[] row, GmmlData data)
	{

		/* Data is lost when converting from GenMAPP to GMML:
		*
		* GenMAPP: 
		*		"Title", "MAPP", "Version", "Author",
		* 		"Maint", "Email", "Copyright","Modify", 
		*		"Remarks", "BoardWidth", "BoardHeight","WindowWidth",
		*		"WindowHeight", "GeneDB", "Notes"
		* GMML:    
		*		"Name", NONE, Version, "Author",  
		*		"MaintainedBy", "Email", "Availability", "LastModified",
		*		"Comment", "BoardWidth", "BoardHeight", NONE, 
		*		NONE, NONE, "Notes"
		*
		*/
	
		GmmlVision.log.trace ("CONVERTING INFO TABLE TO GMML");
		
		GmmlDataObject o = new GmmlDataObject(ObjectType.MAPPINFO);
		o.setParent(data);
		
		o.setMapInfoName(row[icolTitle]);
		o.setMapInfoDataSource("GenMAPP 2.0");
		o.setVersion(row[icolVersion]);
		o.setAuthor(row[icolAuthor]);
		o.setMaintainedBy(row[icolMaint]);
		o.setEmail(row[icolEmail]);
		o.setAvailability(row[icolCopyright]);
		o.setLastModified(row[icolModify]);
		
		o.setNotes(row[icolNotes]);
		o.setComment(row[icolRemarks]);		

		o.setBoardWidth(Double.parseDouble(row[icolBoardWidth]) / GmmlData.GMMLZOOM);
		o.setBoardHeight(Double.parseDouble(row[icolBoardHeight]) / GmmlData.GMMLZOOM);
		o.setWindowWidth(Double.parseDouble(row[icolWindowWidth]) / GmmlData.GMMLZOOM);
		o.setWindowHeight(Double.parseDouble(row[icolWindowHeight]) / GmmlData.GMMLZOOM);		
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

	public static List<String[]> uncopyMappObjects(GmmlData data) throws ConverterException
	{
		List<String[]> result = new ArrayList<String[]>();
		
		for (GmmlDataObject o : data.dataObjects)
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
				case ObjectType.BRACE:
					unmapNotesAndComments (o, row);
					unmapBraceType(o, row);
					result.add(row);
					break;
				case ObjectType.GENEPRODUCT:	
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
					unmapShapeType(o, row);
					result.add(row);
					break;
				case ObjectType.FIXEDSHAPE:					
					unmapNotesAndComments (o, row);
					unmapFixedShapeType(o, row);
					result.add(row);
					break;
				case ObjectType.COMPLEXSHAPE:			
					unmapNotesAndComments (o, row);
					unmapComplexShapeType(o, row);
					result.add(row);
					break;
			}
			
		}
				
		return result;
	}

	private static void unmapNotesAndComments(GmmlDataObject o, String[] row)
	{
		row[colNotes] = o.getNotes();
		row[colRemarks] = o.getComment();
	}
	
	private static void mapNotesAndComments(GmmlDataObject o, String[] row)
	{
        if (row[colNotes] != null &&
        		!row[colNotes].equals(""))
        {        	
        	o.setNotes(row[colNotes]);
        }

        if (row[colRemarks] != null &&
        		!row[colRemarks].equals(""))
        {            
            o.setComment(row[colRemarks]);
        }
	}

	// This list adds the elements from the OBJECTS table to the new gmml
	// pathway
    public static void copyMappObjects(String[] row, GmmlData data) throws ConverterException
    {

		// Create the GenMAPP --> GMML mappings list for use in the switch
		// statement

		List typeslist = Arrays.asList(new String[] { 
				"Arrow", "DottedArrow", "DottedLine", "Line",
				"Brace", "Gene", "InfoBox", "Label", "Legend", "Oval",
				"Rectangle", "TBar", "Receptor", "LigandSq",  "ReceptorSq",
				"LigandRd", "ReceptorRd", "CellA", "Arc", "Ribosome",
				"OrganA", "OrganB", "OrganC", "ProteinB", "Poly", "Vesicle"
		});
		GmmlDataObject o = null;		
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
						break;							
				case 5: /*Gene*/
						o = mapGeneProductType(row);
						mapNotesAndComments (o, row);
						break;																					
				case 6: /*InfoBox*/
						o = mapInfoBoxType (row);
						break;
				case 7: /*Label*/
						o = mapLabelType(row);
						mapNotesAndComments (o, row);
						break;
				case 8: /*Legend*/
						o = mapLegendType(row);
						break;							
				case 9: /*Oval*/						
				case 10: /*Rectangle*/
				case 18: /*Arc*/
						o = mapShapeType(row);
						mapNotesAndComments (o, row);
						break;							
				case 17: /*CellA*/
				case 19: /*Ribosome*/							
				case 20: /*OrganA*/							
				case 21: /*OrganB*/							
				case 22: /*OrganC*/
						o = mapFixedShapeType(row);
						mapNotesAndComments (o, row);
						break;							
				case 23: /*ProteinB*/
				case 24: /*Poly*/
				case 25: /*Vesicle*/
						o = mapComplexShapeType(row);
						mapNotesAndComments (o, row);
						break;
				default: 
						throw new ConverterException (
							"-> Type '" 
							+ row[colType]
							+ "' is not recognised as a GenMAPP type "
							+ "and is therefore not processed.\n");							
		}
		o.setParent(data);
    }

    
    private static void unmapLineType (GmmlDataObject o, String[] mappObject)
    {    	
    	final String[] genmappLineTypes = {
    		"Line", "Arrow", "TBar", "Receptor", "LigandSq", 
    		"ReceptorSq", "LigandRd", "ReceptorRd"};
    	
    	int lineStyle = o.getLineStyle();
		int lineType = o.getLineType();
		String style = genmappLineTypes[lineType];
		if (lineStyle == LineStyle.DASHED && (lineType == LineType.ARROW || lineType == LineType.LINE))
			style = "Dotted" + style;
		
		mappObject[colType] = style;		
		mappObject[colCenterX] = "" + o.getStartX() * GmmlData.GMMLZOOM;
    	mappObject[colCenterY] = "" + o.getStartY() * GmmlData.GMMLZOOM;
    	mappObject[colSecondX] = "" + o.getEndX() * GmmlData.GMMLZOOM;
    	mappObject[colSecondY] = "" + o.getEndY() * GmmlData.GMMLZOOM;
    	unmapColor (o, mappObject);    	
    }

	private static void mapColor(GmmlDataObject o, String[] mappObject)
	{
        int i = Integer.parseInt(mappObject[colColor]);
        o.setTransparent(i < 0);
		o.setColor(ConvertType.fromMappColor(mappObject[colColor]));	
	}

	private static void unmapColor(GmmlDataObject o, String[] mappObject)
	{
		mappObject[colColor] = ConvertType.toMappColor(o.getColor(), o.isTransparent());	
	}

	private static GmmlDataObject mapLineType(String [] mappObject) throws ConverterException
	{
		final List mappLineTypes = Arrays.asList(new String[] {
				"DottedLine", "DottedArrow", "Line", "Arrow", "TBar", "Receptor", "LigandSq", 
				"ReceptorSq", "LigandRd", "ReceptorRd"});
		
    	GmmlDataObject o = new GmmlDataObject(ObjectType.LINE);
    	
		String type = mappObject[colType];
    	int lineStyle = LineStyle.SOLID;		
    	int lineType = mappLineTypes.indexOf(type);
    	if(type.equals("DottedLine") || type.equals("DottedArrow"))
    	{
			lineStyle = LineStyle.DASHED;
    	}
    	else
    	{
    		lineType -= 2;
    	}
    	if (lineType < 0) throw new ConverterException ("Invalid Line Type '" + type + "'");
    	
    	o.setLineStyle(lineStyle);
    	o.setLineType(lineType);
		
        o.setStartX(Double.parseDouble(mappObject[colCenterX]) / GmmlData.GMMLZOOM);       
        o.setStartY(Double.parseDouble(mappObject[colCenterY]) / GmmlData.GMMLZOOM);
        o.setEndX(Double.parseDouble(mappObject[colSecondX]) / GmmlData.GMMLZOOM);
        o.setEndY(Double.parseDouble(mappObject[colSecondY]) / GmmlData.GMMLZOOM);
        mapColor(o, mappObject);        
        return o;
	}
    
	private static void unmapCenter (GmmlDataObject o, String[] mappObject)
	{
		mappObject[colCenterX] = "" + o.getCenterX() * GmmlData.GMMLZOOM;
    	mappObject[colCenterY] = "" + o.getCenterY() * GmmlData.GMMLZOOM;	
	}
	
	private static void mapCenter (GmmlDataObject o, String[] mappObject)
	{
		o.setCenterX(Double.parseDouble(mappObject[colCenterX]) / GmmlData.GMMLZOOM);
		o.setCenterY(Double.parseDouble(mappObject[colCenterY]) / GmmlData.GMMLZOOM);
	}

	private static void unmapRotation (GmmlDataObject o, String[] mappObject)
	{
		mappObject[colRotation] = "" + o.getRotation();
	}
	
	private static void mapRotation (GmmlDataObject o, String[] mappObject)
	{
		o.setRotation(Double.parseDouble(mappObject[colRotation]));
	}
	
	private static void unmapShape (GmmlDataObject o, String[] mappObject)
	{
    	unmapCenter(o, mappObject);    	
    	mappObject[colWidth] = "" + o.getWidth() * GmmlData.GMMLZOOM;
    	mappObject[colHeight] = "" + o.getHeight() * GmmlData.GMMLZOOM;	
	}

	private static void mapShape (GmmlDataObject o, String[] mappObject)
	{
    	mapCenter(o, mappObject);    	
    	o.setWidth(Double.parseDouble(mappObject[colWidth]) / GmmlData.GMMLZOOM);
    	o.setHeight(Double.parseDouble(mappObject[colHeight]) / GmmlData.GMMLZOOM);	
	}
	
	private static void unmapShape_half (GmmlDataObject o, String[] mappObject)
	{
    	unmapCenter(o, mappObject);    	
    	mappObject[colWidth] = "" + o.getWidth() * GmmlData.GMMLZOOM / 2;
    	mappObject[colHeight] = "" + o.getHeight() * GmmlData.GMMLZOOM / 2;	
	}

	private static void mapShape_half (GmmlDataObject o, String[] mappObject)
	{
    	mapCenter(o, mappObject);    	
    	o.setWidth(Double.parseDouble(mappObject[colWidth]) * 2 / GmmlData.GMMLZOOM);
    	o.setHeight(Double.parseDouble(mappObject[colHeight]) * 2 / GmmlData.GMMLZOOM);	
	}

	private static void unmapBraceType (GmmlDataObject o, String[] mappObject) throws ConverterException
    {    	
    	mappObject[colType] = "Brace";    	
    	mappObject[colRotation] = "" + o.getOrientation();    	
    	unmapShape (o, mappObject);
    	unmapColor (o, mappObject);
    }

    private static GmmlDataObject mapBraceType(String[] mappObject) throws ConverterException
    {
    	GmmlDataObject o = new GmmlDataObject(ObjectType.BRACE);
    	
    	mapShape(o, mappObject);
    	mapColor(o, mappObject);
    	o.setOrientation((int)Double.parseDouble(mappObject[colRotation]));
        return o;          
    }
    
    private static void unmapGeneProductType (GmmlDataObject o, String[] mappObject) throws ConverterException
    {    	
    	mappObject[colType] = "Gene";
    	mappObject[colSystemCode] =
			mapBetween (dataSources, systemCodes, 
					o.getDataSource());

		mappObject[colHead] = o.getBackpageHead();
		mappObject[colID] = o.getGeneProductName();
		mappObject[colLabel] = o.getGeneID();
		mappObject[colLinks] = o.getXref();    	
		unmapShape(o, mappObject);
    }
    
    private static GmmlDataObject mapGeneProductType(String[] mappObject) throws ConverterException
	{
    	GmmlDataObject o = new GmmlDataObject(ObjectType.GENEPRODUCT);
    	
    	String syscode = mappObject[colSystemCode];
    	if (syscode == null) syscode = "";
    	syscode = syscode.trim();
    	
        o.setDataSource(mapBetween (
				systemCodes, dataSources, syscode));  

        o.setBackpageHead(mappObject[colHead]);
        o.setGeneProductName(mappObject[colID]);
        o.setGeneID(mappObject[colLabel]);

        // TODO:  for some IDs the type is known, e.g. SwissProt is always a
		// protein, incorporate this knowledge to assign a type per ID
        o.setGeneProductType("unknown");
        String xrefv = mappObject[colLinks];
        if (xrefv == null) { xrefv = ""; }
        o.setXref(xrefv);
        
        mapShape(o, mappObject);
        return o;			
	}
    
	private static GmmlDataObject mapInfoBoxType (String[] mappObject)
	{
    	GmmlDataObject o = new GmmlDataObject(ObjectType.INFOBOX);
        
    	mapCenter (o, mappObject);                
        return o;
	}
	
	private static void unmapInfoBoxType (GmmlDataObject o, String[] mappObject)
    {    	
    	mappObject[colType] = "InfoBox";
    	
    	unmapCenter (o, mappObject);
    }

	private static GmmlDataObject mapLegendType (String[] mappObject)
	{
    	GmmlDataObject o = new GmmlDataObject(ObjectType.LEGEND);
 
    	mapCenter (o, mappObject);
    	        
        return o;
	}
	
	private static void unmapLegendType (GmmlDataObject o, String[] mappObject)
    {    	
    	mappObject[colType] = "Legend";
    	
    	unmapCenter (o, mappObject);    	
    }

	private final static int styleBold = 1; 
	private final static int styleItalic = 2;
	private final static int styleUnderline = 4;
	private final static int styleStrikethru = 8;
    
    private static GmmlDataObject mapLabelType(String[] mappObject) 
    {
    	GmmlDataObject o = new GmmlDataObject(ObjectType.LABEL);

    	mapShape(o, mappObject);
    	mapColor(o, mappObject);
        
    	o.setLabelText(mappObject[colLabel]);
        
        o.setFontName(mappObject[colID]);
        
        o.setFontSize(Double.parseDouble(mappObject[colSecondX]));
        
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

    private static void unmapLabelType (GmmlDataObject o, String[] mappObject)
    {    	
    	mappObject[colType] = "Label";
    	mappObject[colLabel] = o.getLabelText();
    	
    	unmapShape(o, mappObject);
    	unmapColor(o, mappObject);
    	
    	mappObject[colID] = o.getFontName();
    	mappObject[colSecondX] = "" + o.getFontSize();
    	
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
    
	private static GmmlDataObject mapShapeType(String[] mappObject)
    {
    	GmmlDataObject o = new GmmlDataObject(ObjectType.SHAPE);
    	int shapeType = ShapeType.fromMappName(mappObject[colType]);
    	o.setShapeType(shapeType);        
    	if (shapeType == ShapeType.ARC || shapeType == ShapeType.OVAL)
    		mapShape_half (o, mappObject);
    	else
    		mapShape (o, mappObject);
        mapColor (o, mappObject);
        mapRotation (o, mappObject);        
        return o;
    }
    
    private static void unmapShapeType (GmmlDataObject o, String[] mappObject)
    {    	
    	int shapeType = o.getShapeType();
    	mappObject[colType] = ShapeType.toMappName(shapeType);
    	if (shapeType == ShapeType.ARC || shapeType == ShapeType.OVAL)
    		unmapShape_half (o, mappObject);
    	else
    		unmapShape (o, mappObject);
    	unmapColor (o, mappObject);
    	unmapRotation (o, mappObject);    	
    }
    
    private static GmmlDataObject mapFixedShapeType(String[] mappObject)
    {
    	GmmlDataObject o = new GmmlDataObject(ObjectType.FIXEDSHAPE);
        o.setShapeType(ShapeType.fromMappName(mappObject[colType]));
        mapCenter (o, mappObject);
        return o;        
    }

    private static void unmapFixedShapeType (GmmlDataObject o, String[] mappObject)
    {    	
    	int shapeType = o.getShapeType();
    	mappObject[colType] = ShapeType.toMappName(shapeType);
    	
    	if (shapeType == ShapeType.CELLA)
    	{
    		mappObject[colRotation] = "-1.308997";
    		mappObject[colColor] = "0";
    		mappObject[colWidth] = "1500";
    		mappObject[colHeight] = "375";
    	}    	
    	unmapCenter (o, mappObject);
    }
        
    private static GmmlDataObject mapComplexShapeType(String[] mappObject) throws ConverterException 
	{       		
    	GmmlDataObject o = new GmmlDataObject(ObjectType.COMPLEXSHAPE);
    	
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
        
        o.setWidth(Double.parseDouble(mappObject[colWidth]));
        mapCenter (o, mappObject);
        mapRotation (o, mappObject);
        return o;
    }
    
    private static void unmapComplexShapeType (GmmlDataObject o, String[] mappObject)
    {   
    	int shapeType = o.getShapeType();
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
    	
    	unmapCenter (o, mappObject);
        unmapRotation (o, mappObject);
    	mappObject[colWidth] = "" + o.getWidth();
    }
    
	/**
	 * {@link HashMap} containing mappings from system name (as used in Gmml) to system code
	 */
	public static final HashMap<String,String> sysName2Code = initSysName2Code();

	/**
	 * Initializes the {@link HashMap} containing the mappings between system name (as used in gmml)
	 * and system code
	 */
	private static HashMap<String, String> initSysName2Code()
	{
		HashMap<String, String> sn2c = new HashMap<String,String>();
		for(int i = 0; i < dataSources.length; i++)
			sn2c.put(dataSources[i], systemCodes[i]);
		return sn2c;
	}

	//System names converted to arraylist for easy index lookup
	public final static List<String> lDataSources = Arrays.asList(dataSources);
    
}
