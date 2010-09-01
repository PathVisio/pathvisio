// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import java.awt.Color;
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

import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.debug.Logger;

/**
 * The class MappFormat is responsible for all interaction with
 * .mapp files (GenMapp pathway format). Here is also codified all the
 * assumptions about the .mapp format.
 *
 *
 * Certain aspects of the Mapp <-> GPML conversion are getting quite hairy,
 * as there are certain inconsistencies within GenMAPP. We intend to isolate
 * all these inconsistencies just within this class, so they don't propagate
 * to the rest of PathVisio.
 *
 * For example:
 *
 * Color: The "Color" column in the Objects table sometimes corresponds to
 * fillColor (for Rect and Oval) and sometimes to LineColor (for Label, Line,
 * Arc, Brace, etc.). This gets confusing especially for Arc and Brace, because
 * they all get mapped to ObjectType.Shape. For those objects that can have
 * a fill color, the value of the color column can be -1, which indicates
 * transparency.
 *
 * Size: certain objects (i.e. Polygons) always maintain equal width and height.
 * other objects always have a fixed size and even a fixed rotation in the case
 * of CELLB.
 */
public class MappFormat implements PathwayImporter, PathwayExporter
{
	private static final String SQL_INFO_INSERT =
		"INSERT INTO INFO (Title, MAPP, GeneDB, Version, Author, " +
		"Maint, Email, Copyright, Modify, Remarks, BoardWidth, BoardHeight, " +
		"WindowWidth, WindowHeight, Notes) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	// note: column GeneDBVersion is not in all mapps.
	// Notably the mapps converted from kegg are different from the rest.
	private static final String SQL_OBJECTS_INSERT =
		"INSERT INTO OBJECTS (ObjKey, ID, SystemCode, Type, CenterX, " +
		"CenterY, SecondX, SecondY, Width, Height, Rotation, " +
		"Color, Label, Head, Remarks, Image, Links, Notes) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String SQL_INFO_SELECT =
		"SELECT Title, MAPP, GeneDB, Version, Author, " +
		"Maint, Email, Copyright, Modify, Remarks, BoardWidth, BoardHeight, " +
		"WindowWidth, WindowHeight, Notes " +
		"FROM INFO";
	private static final String SQL_OBJECTS_SELECT =
		"SELECT ObjKey, ID, SystemCode, Type, CenterX, " +
		"CenterY, SecondX, SecondY, Width, Height, Rotation, " +
		"Color, Label, Head, Remarks, Image, Links, Notes " +
		"FROM OBJECTS";

    private static final String DATABASE_AFTER = ";DriverID=22;READONLY=true";
    private static final String DATABASE_BEFORE =
            "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";

    private static final int PIXELS_TO_MAPP = 15;

    //  These constants below define columns in the info table.
    //  they are linked to the order of columns in the sqlInfoSelect
    //  statement above
	private static final int ICOL_TITLE = 0;
	private static final int ICOL_MAPP = 1;
	private static final int ICOL_GENEDB = 2;
	private static final int ICOL_VERSION = 3;
	private static final int ICOL_AUTHOR = 4;
	private static final int ICOL_MAINT = 5;
	private static final int ICOL_EMAIL = 6;
	private static final int ICOL_COPYRIGHT = 7;
	private static final int ICOL_MODIFY = 8;
	private static final int ICOL_REMARKS = 9;
	private static final int ICOL_BOARDWIDTH = 10;
	private static final int ICOL_BOARDHEIGHT = 11;
	private static final int ICOL_WINDOWWIDTH = 12;
	private static final int ICOL_WINDOWHEIGHT = 13;
	private static final int ICOL_NOTES = 14;

	// these constants define the columns in the Objects table.
	// they are linked to the order of columns in the SQL_OBJECTS_SELECT
	// statement above.
	private static final int COL_OBJKEY = 0;
	private static final int COL_ID = 1;
	private static final int COL_SYSTEMCODE = 2;
	private static final int COL_TYPE = 3;
	private static final int COL_CENTERX = 4;
	private static final int COL_CENTERY = 5;
	private static final int COL_SECONDX = 6;
	private static final int COL_SECONDY = 7;
	private static final int COL_WIDTH = 8;
	private static final int COL_HEIGHT = 9;
	private static final int COL_ROTATION = 10;
	private static final int COL_COLOR = 11;
	private static final int COL_LABEL = 12;
	private static final int COL_HEAD = 13;
	private static final int COL_REMARKS = 14;
	private static final int COL_IMAGE = 15;
	private static final int COL_LINKS = 16;
	private static final int COL_NOTES = 17;

    /**
     * MAPPTmpl.gtp is a template access database for newly generated
     * mapp's. This file should be
     * in the classpath, normally in resources.jar.
     */
	private static String mappTemplateFile = "MAPPTmpl.gtp";

    static void readFromMapp (String filename, Pathway data)
    	throws ConverterException
    {
    	String database = DATABASE_BEFORE + filename + DATABASE_AFTER;

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
        Logger.log.debug ("Connection string: " + database);

		// Create the connection to the database

        try
        {
	        Connection con = DriverManager.getConnection(database, "", "");

	        Statement s = con.createStatement();

	        Logger.log.trace ("READING INFO TABLE");
	        // first do the INFO table, only one row.
		    {
		        ResultSet r = s.executeQuery(SQL_INFO_SELECT);
		        r.next();
		        int cCol = r.getMetaData().getColumnCount();
		        String[] row = new String[cCol];
		        for (int i = 0; i < cCol; ++i) row[i] = r.getString(i + 1);

		        copyMappInfo(row, data, filename);
	    	}

		    Logger.log.trace ("READING OBJECTS TABLE");
	        // now do the OBJECTS table, multiple rows
	        {
		        ResultSet r = s.executeQuery(SQL_OBJECTS_SELECT);
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
    		String[] mappInfo, List<String[]> mappObjects) throws ConverterException
    {
        String database = DATABASE_BEFORE + filename + ";DriverID=22";

        try {
        	copyResource (mappTemplateFile, new File(filename));

            // Load Sun's jdbc-odbc driver
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

            // Create the connection to the database
            Connection con = DriverManager.getConnection(database, "", "");

            // Create a new sql statement

    		PreparedStatement sInfo = con.prepareStatement(SQL_INFO_INSERT);
            PreparedStatement sObjects = con.prepareStatement(SQL_OBJECTS_INSERT);


            int k = 1;
            for (String[] row : mappObjects)
            {
    			sObjects.setInt (1, k);
    			for (int j = 1; j < row.length; ++j)
    			{
    				Logger.log.trace ("[" + (j + 1) + "] " + row[j]);
//    				System.err.println ("[" + (j + 1) + "] " + row[j]);
    				if (j >= COL_REMARKS && j <= COL_LINKS)
    				{
    					if (row[j] != null && row[j].equals("")) row[j] = null;
    					sObjects.setObject(j + 1, row[j], Types.LONGVARCHAR);
    					// bug workaround, see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4401822
    				}
    				else if (j >= COL_CENTERX && j <= COL_ROTATION)
    				{
    					//NOTE: by using setDouble, we prevent period <-> comma digit symbol issues.
    					//NOTE: could be optimized, now we convert
    					// double to string and back to double again. but at least this works.
    					sObjects.setDouble (j + 1, Double.parseDouble(row[j]));
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
				Logger.log.trace("[" + (j + 1) + "] " + mappInfo[j]);

				if (j >= ICOL_BOARDWIDTH && j <= ICOL_WINDOWHEIGHT)
				{
					sInfo.setDouble (j + 1, Double.parseDouble (mappInfo[j]));
				}
				else
				{
					sInfo.setString (j + 1, mappInfo[j]);
				}
			}
			sInfo.executeUpdate();
            con.close();

        } catch (Exception e) {
        	throw new ConverterException(e);
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
		
		String mapInfoName = mi.getMapInfoName();
		// MAPP format imposes a limit of 50 characters.
		if (mapInfoName.length() > 50) mapInfoName = mapInfoName.substring(0, 50);
		Logger.log.warn("Truncated MAPP INFO name to 50 characters");

		mappInfo[ICOL_TITLE] = mapInfoName;
		mappInfo[ICOL_VERSION] = mi.getVersion();
		mappInfo[ICOL_AUTHOR] = mi.getAuthor();
		mappInfo[ICOL_MAINT] = mi.getMaintainer();
		mappInfo[ICOL_EMAIL] = mi.getEmail();
		mappInfo[ICOL_COPYRIGHT] = mi.getCopyright();
		mappInfo[ICOL_MODIFY] = mi.getLastModified();

		mappInfo[ICOL_NOTES] = mi.findComment("GenMAPP notes");
		mappInfo[ICOL_REMARKS] = mi.findComment("GenMAPP remarks");

		double[] size = mi.getMBoardSize();
		mappInfo[ICOL_BOARDWIDTH] = "" + size[0] * PIXELS_TO_MAPP;
		mappInfo[ICOL_BOARDHEIGHT] = "" + size[1] * PIXELS_TO_MAPP;

		String val;
		val = mi.getDynamicProperty("org.pathvisio.model.WindowWidth");
		mappInfo[ICOL_WINDOWWIDTH] = (val == null) ? "0" : val;
		
		val = mi.getDynamicProperty("org.pathvisio.model.WindowWidth");
    	if (val == null) val = "0";
		mappInfo[ICOL_WINDOWHEIGHT] = (val == null) ? "0" : val;

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

		Logger.log.trace ("CONVERTING INFO TABLE TO GPML");

		PathwayElement o = data.getMappInfo();

		o.setMapInfoName(row[ICOL_TITLE]);
		o.setMapInfoDataSource("GenMAPP 2.0");
		o.setVersion(row[ICOL_VERSION]);
		o.setAuthor(row[ICOL_AUTHOR]);
		o.setMaintainer(row[ICOL_MAINT]);
		o.setEmail(row[ICOL_EMAIL]);
		o.setCopyright(row[ICOL_COPYRIGHT]);
		o.setLastModified(row[ICOL_MODIFY]);

		o.addComment(row[ICOL_NOTES], "GenMAPP notes");
		o.addComment(row[ICOL_REMARKS], "GenMAPP remarks");

		//Board size will be calculated
//		o.setMBoardWidth(Double.parseDouble(row[icolBoardWidth]));
//		o.setMBoardHeight(Double.parseDouble(row[icolBoardHeight]));

		o.setDynamicProperty("org.pathvisio.model.WindowWidth", row[ICOL_WINDOWWIDTH]);
		o.setDynamicProperty("org.pathvisio.model.WindowHeight", row[ICOL_WINDOWHEIGHT]);

		// guess organism based on first two characters of filename
		String shortCode = new File (filename).getName().substring(0, 2);
		Organism org = Organism.fromCode(shortCode);
		if (org != null)
		{
			o.setOrganism(org.latinName());
		}
	}

	public static List<String[]> uncopyMappObjects(Pathway data) throws ConverterException
	{
		List<String[]> result = new ArrayList<String[]>();

		for (PathwayElement o : data.getDataObjects())
		{
			ObjectType objectType = o.getObjectType();
			String[] row = new String[18];

			// init:
			row[COL_CENTERX] = "0.0";
			row[COL_CENTERY] = "0.0";
			row[COL_SECONDX] = "0.0";
			row[COL_SECONDY] = "0.0";
			row[COL_WIDTH] = "0.0";
			row[COL_HEIGHT] = "0.0";
			row[COL_ROTATION] = "0.0";
			row[COL_COLOR] = "-1";

			switch (objectType)
			{
				case LINE:
					unmapNotesAndComments (o, row);
					unmapLineType(o, row);
					result.add(row);
					break;
				case DATANODE:
					unmapNotesAndComments (o, row);
					unmapGeneProductType(o, row);
					result.add(row);
					break;
				case INFOBOX:
					unmapInfoBoxType(o, row);
					result.add(row);
					break;
				case LABEL:
					unmapNotesAndComments (o, row);
					unmapLabelType(o, row);
					result.add(row);
					break;
				case LEGEND:
					unmapLegendType(o, row);
					result.add(row);
					break;
				case SHAPE:
					unmapNotesAndComments (o, row);
					ShapeType s = o.getShapeType();
					if (s == ShapeType.BRACE)
					{
						unmapBraceType(o, row);
					}
					else if (s == ShapeType.OVAL ||
							 s == ShapeType.ARC ||
							 s == ShapeType.RECTANGLE)
					{
						unmapShapeType(o, row);
					}
					else if (s == ShapeType.CELLA ||
							 s == ShapeType.RIBOSOME ||
							 s == ShapeType.ORGANA ||
							 s == ShapeType.ORGANB ||
							 s == ShapeType.ORGANC)
					{
						unmapFixedShapeType(o, row);
					}
					else if (s == ShapeType.PENTAGON ||
							 s == ShapeType.HEXAGON ||
							 s == ShapeType.TRIANGLE ||
							 s == ShapeType.PROTEINB ||
							 s == ShapeType.VESICLE)
					{
						unmapComplexShapeType(o, row);
					}
					else
					{
						Logger.log.warn("This Pathway uses Shapes not supported by GenMAPP");
						unmapUnknownShapeType (o, row);
					}
					result.add(row);
					break;
			}

		}

		return result;
	}

	/**
	 * Unmapp a gpml shape type that is not defined in GenMAPP.
	 * This will create an oval shape with the same properties
	 * (rotation, location, size and color)
	 */
	private static void unmapUnknownShapeType(PathwayElement o, String[] mappObject)
	{
		mappObject[COL_TYPE] = ShapeType.OVAL.getMappName();
		unmapShapeHalf (o, mappObject);
		mappObject[COL_COLOR] = toMappColor(o.getFillColor(), o.isTransparent());
		unmapRotation (o, mappObject);
	}

	private static void unmapNotesAndComments(PathwayElement o, String[] row)
	{
		row[COL_NOTES] = o.findComment("GenMAPP notes");
		row[COL_REMARKS] = o.findComment("GenMAPP remarks");
	}

	private static void mapNotesAndComments(PathwayElement o, String[] row)
	{
        if (row[COL_NOTES] != null &&
        		!row[COL_NOTES].equals(""))
        {
    		o.addComment(row[COL_NOTES], "GenMAPP notes");
        }

        if (row[COL_REMARKS] != null &&
        		!row[COL_REMARKS].equals(""))
        {
    		o.addComment(row[COL_REMARKS], "GenMAPP remarks");
        }
	}

	// This list adds the elements from the OBJECTS table to the new gpml
	// pathway
    public static void copyMappObjects(String[] row, Pathway data) throws ConverterException
    {

		// Create the GenMAPP --> GPML mappings list for use in the switch
		// statement

		List<String> typeslist = Arrays.asList(new String[] {
				"Arrow", "DottedArrow", "DottedLine", "Line",
				"Brace", "Gene", "InfoBox", "Label", "Legend", "Oval",
				"Rectangle", "TBar", "Receptor", "LigandSq",  "ReceptorSq",
				"LigandRd", "ReceptorRd", "CellA", "Arc", "Ribosome",
				"OrganA", "OrganB", "OrganC", "ProteinB", "Poly", "Vesicle"
		});
		PathwayElement o = null;
		int index = typeslist.indexOf(row[COL_TYPE]);
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
						o.setGraphId(data.getUniqueGraphId());
						break;
				case 5: /*Gene*/
						o = mapGeneProductType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueGraphId());
						break;
				case 6: /*InfoBox*/
						o = mapInfoBoxType (row, data);
						break;
				case 7: /*Label*/
						o = mapLabelType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueGraphId());
						break;
				case 8: /*Legend*/
						o = mapLegendType(row);
						break;
				case 9: /*Oval*/
				case 10: /*Rectangle*/
				case 18: /*Arc*/
						o = mapShapeType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueGraphId());
						break;
				case 17: /*CellA*/
				case 19: /*Ribosome*/
				case 20: /*OrganA*/
				case 21: /*OrganB*/
				case 22: /*OrganC*/
						o = mapFixedShapeType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueGraphId());
						break;
				case 23: /*ProteinB*/
				case 24: /*Poly*/
				case 25: /*Vesicle*/
						o = mapComplexShapeType(row);
						mapNotesAndComments (o, row);
						o.setGraphId(data.getUniqueGraphId());
						break;
				default:
						throw new ConverterException (
							"-> Type '"
							+ row[COL_TYPE]
							+ "' is not recognised as a GenMAPP type "
							+ "and is therefore not processed.\n");
		}
		data.add(o);
    }


    private static void unmapLineType (PathwayElement o, String[] mappObject)
    {
    	int lineStyle = o.getLineStyle();
		LineType lineType = o.getEndLineType();
		String style = lineType.getMappName();
		if(style == null) {
			style = LineType.LINE.getMappName();
		}
		if (lineStyle == LineStyle.DASHED && (lineType == LineType.ARROW || lineType == LineType.LINE))
			style = "Dotted" + style;

		mappObject[COL_TYPE] = style;
		mappObject[COL_CENTERX] = "" + o.getMStartX() * PIXELS_TO_MAPP;
    	mappObject[COL_CENTERY] = "" + o.getMStartY() * PIXELS_TO_MAPP;
    	mappObject[COL_SECONDX] = "" + o.getMEndX() * PIXELS_TO_MAPP;
    	mappObject[COL_SECONDY] = "" + o.getMEndY() * PIXELS_TO_MAPP;
		mappObject[COL_COLOR] = toMappColor(o.getColor(), false);
    }

	private static Map<String,LineType> mappLineTypes = initMappLineTypes();

	static private Map<String,LineType> initMappLineTypes()
	{
		Map<String,LineType> result = new HashMap<String,LineType>();
		result.put ("DottedLine", LineType.LINE);
		result.put ("DottedArrow", LineType.ARROW);
		for (LineType l : LineType.getValues())
		{
			result.put (l.getMappName(), l);
		}
		return result;
	}

	private static PathwayElement mapLineType(String [] mappObject) throws ConverterException
	{
    	PathwayElement o = PathwayElement.createPathwayElement(ObjectType.LINE);

		String type = mappObject[COL_TYPE];
    	if(type.startsWith("Dotted"))
    	{
			o.setLineStyle(LineStyle.DASHED);
    	}
    	else
    	{
    		o.setLineStyle(LineStyle.SOLID);
    	}

    	o.setEndLineType(mappLineTypes.get(type));
        o.setMStartX(Double.parseDouble(mappObject[COL_CENTERX]) / PIXELS_TO_MAPP);
        o.setMStartY(Double.parseDouble(mappObject[COL_CENTERY]) / PIXELS_TO_MAPP);
        o.setMEndX(Double.parseDouble(mappObject[COL_SECONDX]) / PIXELS_TO_MAPP);
        o.setMEndY(Double.parseDouble(mappObject[COL_SECONDY]) / PIXELS_TO_MAPP);
		o.setColor(fromMappColor(mappObject[COL_COLOR]));
        return o;
	}

	private static void unmapCenter (PathwayElement o, String[] mappObject)
	{
		mappObject[COL_CENTERX] = "" + o.getMCenterX() * PIXELS_TO_MAPP;
    	mappObject[COL_CENTERY] = "" + o.getMCenterY() * PIXELS_TO_MAPP;
	}

	private static void mapCenter (PathwayElement o, String[] mappObject)
	{
		o.setMCenterX(Double.parseDouble(mappObject[COL_CENTERX]) / PIXELS_TO_MAPP);
		o.setMCenterY(Double.parseDouble(mappObject[COL_CENTERY]) / PIXELS_TO_MAPP);
	}

	private static void unmapRotation (PathwayElement o, String[] mappObject)
	{
		mappObject[COL_ROTATION] = "" + o.getRotation();
	}

	private static void mapRotation (PathwayElement o, String[] mappObject)
	{
		o.setRotation(Double.parseDouble(mappObject[COL_ROTATION]));
	}

	private static void unmapShape (PathwayElement o, String[] mappObject)
	{
    	unmapCenter(o, mappObject);
    	mappObject[COL_WIDTH] = "" + o.getMWidth() * PIXELS_TO_MAPP;
    	mappObject[COL_HEIGHT] = "" + o.getMHeight() * PIXELS_TO_MAPP;
	}

	private static void mapShape (PathwayElement o, String[] mappObject)
	{
    	mapCenter(o, mappObject);
    	o.setMWidth(Double.parseDouble(mappObject[COL_WIDTH]) / PIXELS_TO_MAPP);
    	o.setMHeight(Double.parseDouble(mappObject[COL_HEIGHT]) / PIXELS_TO_MAPP);
	}

	private static void unmapShapeHalf (PathwayElement o, String[] mappObject)
	{
    	unmapCenter(o, mappObject);
    	mappObject[COL_WIDTH] = "" + o.getMWidth() / 2 * PIXELS_TO_MAPP;
    	mappObject[COL_HEIGHT] = "" + o.getMHeight() / 2 * PIXELS_TO_MAPP;
	}

	private static void mapShapeHalf (PathwayElement o, String[] mappObject)
	{
    	mapCenter(o, mappObject);
    	o.setMWidth(Double.parseDouble(mappObject[COL_WIDTH]) * 2 / PIXELS_TO_MAPP);
    	o.setMHeight(Double.parseDouble(mappObject[COL_HEIGHT]) * 2 / PIXELS_TO_MAPP);
	}

	private static void unmapBraceType (PathwayElement o, String[] mappObject) throws ConverterException
    {
    	mappObject[COL_TYPE] = "Brace";
    	mappObject[COL_ROTATION] = "" + o.getOrientation();
    	unmapShape (o, mappObject);
		mappObject[COL_COLOR] = toMappColor(o.getColor(), false);
    }

    private static PathwayElement mapBraceType(String[] mappObject) throws ConverterException
    {
    	PathwayElement o = PathwayElement.createPathwayElement(ObjectType.SHAPE);
    	o.setShapeType (ShapeType.BRACE);
    	mapShape(o, mappObject);
    	o.setColor(fromMappColor(mappObject[COL_COLOR]));
    	o.setTransparent (true);
    	o.setOrientation((int)Double.parseDouble(mappObject[COL_ROTATION]));
        return o;
    }

    private static String getSafeDynamicProperty(PathwayElement o, String key)
    {
    	String val = o.getDynamicProperty(key);
    	if (val == null) return ""; else return val;
    }
    
    private static void unmapGeneProductType (PathwayElement o, String[] mappObject) throws ConverterException
    {
    	mappObject[COL_TYPE] = "Gene";
    	mappObject[COL_SYSTEMCODE] = o.getDataSource() != null ? o.getDataSource().getSystemCode() : "";
		mappObject[COL_HEAD] = getSafeDynamicProperty (o, "org.pathvisio.model.BackpageHead");
		mappObject[COL_ID] = o.getGeneID();
		mappObject[COL_LABEL] = o.getTextLabel();
		mappObject[COL_LINKS] = getSafeDynamicProperty (o, "org.pathvisio.model.GenMAPP-Xref");
		unmapShape(o, mappObject);
    }

    private static PathwayElement mapGeneProductType(String[] mappObject) throws ConverterException
	{
    	PathwayElement o = PathwayElement.createPathwayElement(ObjectType.DATANODE);

    	String syscode = mappObject[COL_SYSTEMCODE];
    	if (syscode == null) syscode = "";
    	syscode = syscode.trim();

        o.setDataSource(DataSource.getBySystemCode(syscode));

        o.setDynamicProperty ("org.pathvisio.model.BackpageHead", mappObject[COL_HEAD]);
        if (mappObject[COL_ID] == null)
        {
        	o.setGeneID("");
        }
        else
        {
        	o.setGeneID(mappObject[COL_ID]);
        }
        o.setTextLabel(mappObject[COL_LABEL]);

        o.setDataNodeType("GeneProduct");
        String xrefv = mappObject[COL_LINKS];
        if (xrefv == null) { xrefv = ""; }
        o.setDynamicProperty("org.pathvisio.model.GenMAPP-Xref", xrefv);

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
    	mappObject[COL_TYPE] = "InfoBox";

    	unmapCenter (o, mappObject);
    }

	private static PathwayElement mapLegendType (String[] mappObject)
	{
    	PathwayElement o = PathwayElement.createPathwayElement(ObjectType.LEGEND);

    	mapCenter (o, mappObject);

        return o;
	}

	private static void unmapLegendType (PathwayElement o, String[] mappObject)
    {
    	mappObject[COL_TYPE] = "Legend";

    	unmapCenter (o, mappObject);
    }

	private final static int STYLE_BOLD = 1;
	private final static int STYLE_ITALIC = 2;
	private final static int STYLE_UNDERLINE = 4;
	private final static int STYLE_STRIKETHRU = 8;

    private static PathwayElement mapLabelType(String[] mappObject)
    {
    	PathwayElement o = PathwayElement.createPathwayElement(ObjectType.LABEL);

    	mapShape(o, mappObject);
		o.setColor(fromMappColor(mappObject[COL_COLOR]));

    	o.setTextLabel(mappObject[COL_LABEL]);

    	if (mappObject[COL_ID] == null)
    	{
    		o.setFontName("");
    	}
    	else
    	{
    		o.setFontName(mappObject[COL_ID]);
    	}


        o.setMFontSize(Double.parseDouble(mappObject[COL_SECONDX]));

        String styleString = mappObject[COL_SYSTEMCODE];
        int style = styleString == null ? 0 : (int)(styleString.charAt(0));

        o.setBold((style & STYLE_BOLD) > 0);
        o.setItalic((style & STYLE_ITALIC) > 0);
        o.setUnderline((style & STYLE_UNDERLINE) > 0);
        o.setStrikethru((style & STYLE_STRIKETHRU) > 0);


        String xrefv = mappObject[COL_LINKS];
        if (xrefv == null) { xrefv = ""; }
        o.setDynamicProperty("org.pathvisio.model.GenMAPP-Xref", xrefv);
        return o;
    }

    private static void unmapLabelType (PathwayElement o, String[] mappObject)
    {
    	mappObject[COL_TYPE] = "Label";
    	String text = o.getTextLabel();
    	if(text != null) {
    		text = text.replace("\n", " ");
    	}
    	mappObject[COL_LABEL] = text;

    	unmapShape(o, mappObject);
		mappObject[COL_COLOR] = toMappColor(o.getColor(), false);

    	mappObject[COL_ID] = o.getFontName();
    	mappObject[COL_SECONDX] = "" + (o.getMFontSize());

    	int style = 16;
    	// note: from VB source I learned that 16 is added to prevent field from becoming 0,
    	// as this can't be stored in a text field in the database
    	if (o.isBold()) style |= STYLE_BOLD;
    	if (o.isItalic()) style |= STYLE_ITALIC;
    	if (o.isUnderline()) style |= STYLE_UNDERLINE;
    	if (o.isStrikethru()) style |= STYLE_STRIKETHRU;

    	char stylechars[] = new char[1];
    	stylechars[0] = (char)style;

    	mappObject[COL_SYSTEMCODE] = new String (stylechars);
		mappObject[COL_LINKS] = getSafeDynamicProperty(o, "org.pathvisio.model.GenMAPP-Xref");
    }

	private static PathwayElement mapShapeType(String[] mappObject)
    {
    	PathwayElement o = PathwayElement.createPathwayElement(ObjectType.SHAPE);
    	ShapeType shapeType = ShapeType.fromMappName(mappObject[COL_TYPE]);
    	o.setShapeType(shapeType);
    	if (shapeType == ShapeType.ARC || shapeType == ShapeType.OVAL)
    		mapShapeHalf (o, mappObject);
    	else
    		mapShape (o, mappObject);

        if (shapeType == ShapeType.ARC)
        {
        	o.setColor(fromMappColor(mappObject[COL_COLOR]));
        	o.setTransparent (true);
        }
        else
        {
            int i = Integer.parseInt(mappObject[COL_COLOR]);
            if (i < 0)
            {
            	o.setTransparent(true); // automatically sets fillColor to null
            }
            else
            {
            	o.setFillColor(fromMappColor(mappObject[COL_COLOR]));
            }
        }

        mapRotation (o, mappObject);
        return o;
    }

    private static void unmapShapeType (PathwayElement o, String[] mappObject)
    {
    	ShapeType shapeType = o.getShapeType();
    	mappObject[COL_TYPE] = shapeType.getMappName();
    	if (shapeType == ShapeType.ARC || shapeType == ShapeType.OVAL)
    		unmapShapeHalf (o, mappObject);
    	else
    		unmapShape (o, mappObject);

		// note: when converting gpml to mapp,
		// line color is discarded for oval and rect
    	if (shapeType == ShapeType.ARC)
    	{
    		mappObject[COL_COLOR] = toMappColor(o.getColor(), false);
    	}
    	else
    	{
    		mappObject[COL_COLOR] = toMappColor(o.getFillColor(), o.isTransparent());
    	}
		unmapRotation (o, mappObject);
    }

    private static PathwayElement mapFixedShapeType(String[] mappObject)
    {
    	PathwayElement o = PathwayElement.createPathwayElement(ObjectType.SHAPE);
        o.setShapeType(ShapeType.fromMappName(mappObject[COL_TYPE]));
        mapCenter (o, mappObject);

        if (o.shapeType == ShapeType.CELLA)
		{
        	o.setRotation (-1.308997);
        	o.setMWidth(1500 / PIXELS_TO_MAPP);
        	o.setMHeight(375 / PIXELS_TO_MAPP);
        }
		else if (o.shapeType == ShapeType.RIBOSOME)
		{
        	o.setMWidth (600 / PIXELS_TO_MAPP);
        	o.setMHeight (600 / PIXELS_TO_MAPP);
		}
		else if (o.shapeType == ShapeType.ORGANA)
		{
        	o.setMWidth (500 / PIXELS_TO_MAPP);
        	o.setMHeight (2000 / PIXELS_TO_MAPP);
		}
		else if (o.shapeType == ShapeType.ORGANB)
		{
        	o.setMWidth (500 / PIXELS_TO_MAPP);
        	o.setMHeight (2000 / PIXELS_TO_MAPP);
		}
		else if (o.shapeType == ShapeType.ORGANC)
		{
        	o.setMWidth (600 / PIXELS_TO_MAPP);
        	o.setMHeight (600 / PIXELS_TO_MAPP);
        }
        return o;
    }

    private static void unmapFixedShapeType (PathwayElement o, String[] mappObject)
    {
    	ShapeType shapeType = o.getShapeType();
    	mappObject[COL_TYPE] = shapeType.getMappName();

    	if (shapeType == ShapeType.CELLA)
    	{
    		mappObject[COL_ROTATION] = "-1.308997";
    		mappObject[COL_COLOR] = "0";
    		mappObject[COL_WIDTH] = "1500";
    		mappObject[COL_HEIGHT] = "375";
    	}
    	unmapShape (o, mappObject);
    }

    private static PathwayElement mapComplexShapeType(String[] mappObject) throws ConverterException
	{
    	PathwayElement o = PathwayElement.createPathwayElement(ObjectType.SHAPE);

    	if (mappObject[COL_TYPE].equals("Poly"))
        {
        	switch ((int)Double.parseDouble(mappObject[COL_SECONDY]))
        	{
        	case 3: o.setShapeType(ShapeType.TRIANGLE); break;
        	case 5: o.setShapeType(ShapeType.PENTAGON); break;
        	case 6: o.setShapeType(ShapeType.HEXAGON); break;
        	default: throw
        		new ConverterException ("Found polygon with unexpectec edge count: " +
        				mappObject[COL_SECONDY]);
        	}
        }
    	else
    	{
    		o.setShapeType(ShapeType.fromMappName(mappObject[COL_TYPE]));
    	}

    	mapCenter(o, mappObject);
    	double size = Double.parseDouble(mappObject[COL_WIDTH]);
    	o.setMWidth(size / PIXELS_TO_MAPP);
    	o.setMHeight(size / PIXELS_TO_MAPP);
        mapRotation (o, mappObject);
        return o;
    }

    private static void unmapComplexShapeType (PathwayElement o, String[] mappObject)
    {
    	ShapeType shapeType = o.getShapeType();
    	mappObject[COL_TYPE] = shapeType.getMappName();

    	if (shapeType == ShapeType.TRIANGLE)
    	{
    		mappObject[COL_SECONDY] = "3";
    	} else if (shapeType == ShapeType.PENTAGON)
    	{
    		mappObject[COL_SECONDY] = "5";
		} else if (shapeType == ShapeType.HEXAGON)
		{
			mappObject[COL_SECONDY] = "6";
		}

    	unmapCenter (o, mappObject);
    	double size = o.getMWidth();
    	mappObject[COL_WIDTH] = "" + size;
    	if (shapeType == ShapeType.PROTEINB)
    	{
    		mappObject[COL_HEIGHT] = "0";
    	}
    	else
    	{
    		mappObject[COL_HEIGHT] = "400";
    	}
        unmapRotation (o, mappObject);
    }

	private static String[] extensions = new String[] { "mapp" };

	public String getName() {
		return "GenMAPP";
	}

	public String[] getExtensions() {
		return extensions;
	}

	public void doExport(File file, Pathway pathway) throws ConverterException {
		String[] mappInfo = MappFormat.uncopyMappInfo(pathway);
		List<String[]> mappObjects = MappFormat.uncopyMappObjects(pathway);
		MappFormat.exportMapp (file.getAbsolutePath(), mappInfo, mappObjects);
	}

	public Pathway doImport(File file) throws ConverterException {
        Pathway pathway = new Pathway();
		String inputString = file.getAbsolutePath();

        MappFormat.readFromMapp (inputString, pathway);
        pathway.setSourceFile(null); //Don't save back to mapp file
        return pathway;
	}

    private static Color fromMappColor(String s)
    {

    	int i = Integer.parseInt(s);

    	Color result = new Color(
    			i & 0xFF,
    			(i & 0xFF00) >> 8,
    			(i & 0xFF0000) >> 16
    	);

    	return result;
    }

    private static String toMappColor(Color rgb, boolean fTransparent)
    {
    	if (fTransparent)
    		return "-1";
    	else
    	{
	    	int c = (rgb.getRed()) + (rgb.getGreen() << 8) + (rgb.getBlue() << 16);
	    	return "" + c;
    	}
    }
}
