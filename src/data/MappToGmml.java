// $Id: MappToGmml.java,v 1.5 2005/10/21 12:33:27 gontran Exp $
package data;

import java.util.*;
import org.jdom.*;
import debug.Logger;

// don't need this for the commandline version!
//import javax.swing.*;


public class MappToGmml
{	
	/**
	 * The GMML xsd implies a certain ordering for children of the pathway element.
	 * (e.g. GeneProduct always comes before LineShape, etc.)
	 * 
	 * This Comparator can sort jdom Elements so that they are in the correct order
	 * for the xsd.
	 *  
	 * @author Martijn.vanIersel
	 */
	private static class ByElementName implements Comparator
	{
		// hashmap for quick lookups during sorting
		private HashMap elementOrdering;
				
		// correctly ordered list of tag names, which are loaded into the hashmap in
		// the constructor.
		private final String[] elements = new String[] {
			"Notes", "Comment", "Graphics", "GeneProduct", "Line", "Label",
			"Shape", "Brace", "FixedShape", "ComplexShape", "InfoBox", "Legend"
		};
		
		/*
		 * Constructor
		 */
		public ByElementName()
		{
			elementOrdering = new HashMap();
			for (int i = 0; i < elements.length; ++i)
			{
				elementOrdering.put (elements[i], new Integer(i));
			}			
		}
		
		/*
		 * As a comparison measure, returns difference of index of element names of a and b 
		 * in elements array. E.g:
		 * Comment -> index 1 in elements array
		 * Graphics -> index 2 in elements array.
		 * If a.getName() is Comment and b.getName() is Graphics, returns 1-2 -> -1
		 */
		public int compare(Object a, Object b) {
			if (!(a instanceof Element && b instanceof Element))
			{
				throw new ClassCastException();
			}			
			return ((Integer)elementOrdering.get(((Element)a).getName())).intValue() - 
				((Integer)elementOrdering.get(((Element)b).getName())).intValue();
		}
		
	}
	
	public static Logger log;

	// These constants define columns in the info table.
	static final int icolTitle = 0;
	static final int icolMAPP = 1;
	static final int icolGeneDB = 2;
	static final int icolGeneDBVersion = 3;
	static final int icolVersion = 4;
	static final int icolAuthor = 5;
	static final int icolMaint = 6;
	static final int icolEmail = 7;
	static final int icolCopyright = 8;
	static final int icolModify = 9;
	static final int icolRemarks = 10;
	static final int icolBoardWidth = 11;
	static final int icolBoardHeight = 12;
	static final int icolWindowWidth = 13;
	static final int icolWindowHeight = 14;
	static final int icolNotes = 15;

    String[] headers = {"Title", "MAPP", "GeneDB", "GeneDBVersion", "Version", "Author",
            "Maint", "Email", "Copyright",
            "Modify", "Remarks", "BoardWidth", "BoardHeight",
            "WindowWidth", "WindowHeight", "Notes"};

	public static String[][] uncopyMappInfo (Document doc)
	{
		String[][] mappInfo = new String[2][16];
		
		Element root = doc.getRootElement();
		
		mappInfo[1][icolTitle] = root.getAttributeValue("Name");
		mappInfo[1][icolVersion] = root.getAttributeValue("Version");
		mappInfo[1][icolAuthor] = root.getAttributeValue("Author");
		mappInfo[1][icolMaint] = root.getAttributeValue("Maintained-By");
		mappInfo[1][icolEmail] = root.getAttributeValue("Email");
		mappInfo[1][icolCopyright] = root.getAttributeValue("Availability");
		mappInfo[1][icolModify] = root.getAttributeValue("Last-Modified");
		
		mappInfo[1][icolNotes] = root.getChildText("Notes");
		mappInfo[1][icolRemarks] = root.getChildText("Comment");
		
		Element graphics = root.getChild("Graphics");
		mappInfo[1][icolBoardWidth] = graphics.getAttributeValue("BoardWidth");
		mappInfo[1][icolBoardHeight] = graphics.getAttributeValue("BoardHeight");
		mappInfo[1][icolWindowWidth] = graphics.getAttributeValue("WindowWidth");
		mappInfo[1][icolWindowHeight] = graphics.getAttributeValue("WindowHeight");
		
		return mappInfo;
	}
	
	// This method copies the Info table of the genmapp mapp to a new gmml
	// pathway
	public static void copyMappInfo( String[][] mappInfo, Document doc)
	{

		/* Data is lost when converting from GenMAPP to GMML:
		*
		* GenMAPP: 
		*		"Title", "MAPP", "Version", "Author","GeneDBVersion",
		* 		"Maint", "Email", "Copyright","Modify", 
		*		"Remarks", "BoardWidth", "BoardHeight","WindowWidth",
		*		"WindowHeight", "GeneDB", "Notes"
		* GMML:    
		*		"Name", NONE, Version, "Author", NONE, 
		*		"MaintainedBy", "Email", "Availability", "LastModified",
		*		"Comment", "BoardWidth", "BoardHeight", NONE, 
		*		NONE, NONE, "Notes"
		*
		*/
	
		log.trace ("CONVERTING INFO TABLE TO GMML");
	
		/*
		*
    	* from: fileInOut.MappFile.importMAPPInfo(String filename)
		* below mappInfo array is indexed in the following order -- NOT
		* like in the SQL order from the Info table.  *sigh*
		*
		* 0 "Title", 1 "MAPP", 2 "Version", 3 "Author", 4 "GeneDBVersion",
		* 5 "Maint", 6 "Email", 7 "Copyright", 8 "Modify", 9 "Remarks",
		* 10 "BoardWidth", 11 "BoardHeight", 12 "WindowWidth",
		* 13 "WindowHeight", 14 "GeneDB", 15 "Notes"
		*
		*/

		Element root = new Element("Pathway");
		doc.setRootElement(root);

		root.setAttribute("Name", mappInfo[1][icolTitle]);
		root.setAttribute("Data-Source", "GenMAPP 2.0");
		root.setAttribute("Version", mappInfo[1][icolVersion]);
		root.setAttribute("Author", mappInfo[1][icolAuthor]);
		root.setAttribute("Maintained-By", mappInfo[1][icolMaint]);
		root.setAttribute("Email", mappInfo[1][icolEmail]);
		root.setAttribute("Availability", mappInfo[1][icolCopyright]);
		root.setAttribute("Last-Modified", mappInfo[1][icolModify]);

		// Info.Notes
		Element notes = new Element("Notes");
		notes.addContent(mappInfo[1][icolNotes]);
		root.addContent(notes);
		
		// Info.Remarks
		Element comments = new Element("Comment");
		comments.addContent(mappInfo[1][icolRemarks]);
		root.addContent(comments);

		Element graphics = new Element("Graphics");
		root.addContent(graphics);

		graphics.setAttribute("BoardWidth", mappInfo[1][icolBoardWidth]);
		graphics.setAttribute("BoardHeight", mappInfo[1][icolBoardHeight]);
		graphics.setAttribute("WindowWidth", mappInfo[1][icolWindowWidth]);
		graphics.setAttribute("WindowHeight", mappInfo[1][icolWindowHeight]);
	}
    
	// these constants define the columns in the Objects table.
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

	public static List uncopyMappObjects(Document doc) throws ConverterException
	{
		List result = new ArrayList();
		
		Element root = doc.getRootElement();
		Iterator i = root.getChildren().iterator();
		while (i.hasNext())
		{
			Element e = (Element)(i.next());
			String type = e.getName();
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
			
			if (type.equals("Line"))
				unmapLineType(e, row);
			else if (type.equals("Brace"))
				unmapBraceType(e, row);
			else if (type.equals("GeneProduct"))
				unmapGeneProductType(e, row);
			else if (type.equals("InfoBox"))
				unmapInfoBoxType(e, row);
			else if (type.equals("Label"))
				unmapLabelType(e, row);
			else if (type.equals("Legend"))
				unmapLegendType(e, row);
			else if (type.equals("Shape"))
				unmapShapeType(e, row);
			else if (type.equals("FixedShape"))
				unmapFixedShapeType(e, row);
			else if (type.equals("ComplexShape"))
				unmapComplexShapeType(e, row);
			else if (type.equals("Comment")) // ignore
				continue;
			else if (type.equals("Notes")) // ignore
				continue;
			else if (type.equals("Graphics")) // ignore
				continue;
			else throw new ConverterException ("Invalid element encountered: " + type);
			
	    	String notes = e.getChildText("Notes");
	    	if (notes != null && notes.equals("")) notes = null;
	    	row[colNotes] = notes;

	    	String comment = e.getChildText("Comment");
	    	if (comment != null && comment.equals("")) comment = null;
	    	row[colRemarks] = comment;    	

			result.add(row);
		}
				
		return result;
	}
	
	// This list adds the elements from the OBJECTS table to the new gmml
	// pathway
    public static void copyMappObjects(String[][] mappObjects, Document doc) throws ConverterException
    {
    	Element root = doc.getRootElement();
    	List elementList = new ArrayList();
    	
        log.trace ("CONVERTING OBJECTS TABLE TO GMML");

		// Create the GenMAPP --> GMML mappings list for use in the switch
		// statement

		String[] types = { 
				"Arrow", "DottedArrow", "DottedLine", "Line",
				"Brace", "Gene", "InfoBox", "Label", "Legend", "Oval",
				"Rectangle", "TBar", "Receptor", "LigandSq",  "ReceptorSq",
				"LigandRd", "ReceptorRd", "CellA", "Arc", "Ribosome",
				"OrganA", "OrganB", "OrganC", "ProteinB", "Poly", "Vesicle"
		};

		List typeslist = new ArrayList();
		int index = 0;

		for(int i=0; i<types.length; i++)
		{
				typeslist.add(types[i]);
		}

		/*index 0 are heades*//*last row is always null*/

		for(int i=1; i<mappObjects.length-1; i++)
		{
			Element e = null;
			
			index = typeslist.indexOf(mappObjects[i][colType]);
			
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
							e = mapLineType(mappObjects[i]);							
							break;							
					case 4: /*Brace*/
							e = mapBraceType(mappObjects[i]);							
							break;							
					case 5: /*Gene*/
							e = mapGeneProductType(mappObjects[i]);
							break;																					
					case 6: /*InfoBox*/
							e = mapInfoBoxType (mappObjects[i]);
							break;
					case 7: /*Label*/
							e = mapLabelType(mappObjects[i]);
							break;
					case 8: /*Legend*/
							e = mapLegendType(mappObjects[i]);
							break;							
					case 9: /*Oval*/						
					case 10: /*Rectangle*/
					case 18: /*Arc*/
							e = mapShapeType( mappObjects[i]);
							break;							
					case 17: /*CellA*/
					case 19: /*Ribosome*/							
					case 20: /*OrganA*/							
					case 21: /*OrganB*/							
					case 22: /*OrganC*/
							e = mapFixedShapeType(mappObjects[i]);							
							break;							
					case 23: /*ProteinB*/
					case 24: /*Poly*/
					case 25: /*Vesicle*/
							e = mapComplexShapeType(mappObjects[i]);							
							break;
					default: 
							throw new ConverterException (
								"-> Type '" 
								+ mappObjects[i][colType]
								+ "' is not recognised as a GenMAPP type "
								+ "and is therefore not processed.\n");							
			}
			
			if (e != null)
			{
				if (index != 6 && index != 8) // not for infobox / legend
				{
			        if (mappObjects[i][colNotes] != null &&
			        		!mappObjects[i][colNotes].equals(""))
			        {
			        	Element notes = new Element("Notes");
			        	notes.addContent(mappObjects[i][colNotes]);
			        	e.addContent(notes);
			        }
	
			        if (mappObjects[i][colRemarks] != null &&
			        		!mappObjects[i][colRemarks].equals(""))
			        {
			            Element comment = new Element("Comment");
			            comment.addContent(mappObjects[i][colRemarks]);
			            e.addContent(comment);
			        }
				}		
				elementList.add(e);
			}
		}
		
		// now sort the generated elements in the order defined by the xsd
		Collections.sort(elementList, new ByElementName());
		Iterator i = elementList.iterator();
		while (i.hasNext())
		{
			Element e = ((Element)i.next());
			if (e != null)
				root.addContent(e);
		}
    }

    
    public static void unmapLineType (Element e, String[] mappObject) throws ConverterException
    {    	
    	String gmmlType = e.getAttributeValue("Type");
		String style = e.getAttributeValue("Style");
		String type = mapBetween (
    			gmmlLineShapeTypes, 
    			genmappLineShapeTypes, 
    			gmmlType
    		);
		mappObject[colType] = type;
		
		if (style.equals("Broken"))
		{
			if (type.equals("Line") || type.equals("Arrow"))
				type = "Dotted" + type;
			else
				throw new ConverterException (
						"Invalid line combination: style 'Broken' and type '" + gmmlType + "'");
		}
		else if (!style.equals("Solid"))
			throw new ConverterException ("Invalid Line Type detected: " + gmmlType);
		
		mappObject[colType] = type;
		
    	Element graphics = e.getChild("Graphics");
    	mappObject[colCenterX] = graphics.getAttributeValue("StartX");
    	mappObject[colCenterY] = graphics.getAttributeValue("StartY");
    	mappObject[colSecondX] = graphics.getAttributeValue("EndX");
    	mappObject[colSecondY] = graphics.getAttributeValue("EndY");
    	mappObject[colColor] = ConvertType.toMappColor(graphics.getAttributeValue("Color"));
    }

	static final String[] genmappLineShapeTypes = {
		"Line", "Arrow", "DottedLine", "DottedArrow", "TBar", "Receptor", "LigandSq", 
		"ReceptorSq", "LigandRd", "ReceptorRd"};

	//!! "Receptor" doesn't exist in GMML, so take receptorsquare
	static final String[] gmmlLineShapeTypes = {
			"Line", "Arrow", "Line", "Arrow", "Tbar", "Receptor", "LigandSquare", 
			"ReceptorSquare", "LigandRound", "ReceptorRound"};

	
	public static Element mapLineType(String [] mappObject) throws ConverterException
	{
    	Element e = new Element("Line");
    	
		String type = mappObject[colType];
    	String style = "Solid";		
		if(type.equals("DottedLine") || type.equals("DottedArrow"))
			style = "Broken";    	
		e.setAttribute("Type", mapBetween (
    			genmappLineShapeTypes, 
    			gmmlLineShapeTypes, type));
		e.setAttribute("Style", style);
        
        Element graphics = new Element ("Graphics");
        
        graphics.setAttribute("StartX", mappObject[colCenterX]);        
        graphics.setAttribute("StartY", mappObject[colCenterY]);
        graphics.setAttribute("EndX", mappObject[colSecondX]);
        graphics.setAttribute("EndY", mappObject[colSecondY]);		
        graphics.setAttribute("Color", ConvertType.toGmmlColor(mappObject[colColor]));
        
        e.addContent(graphics);

        return e;
	}
    
    public static void unmapBraceType (Element e, String[] mappObject) throws ConverterException
    {    	
    	mappObject[colType] = "Brace";    	
    	
    	Element graphics = e.getChild("Graphics");
    	
        String orientation = graphics.getAttributeValue("Orientation");        
		if(orientation.equals("top")) {
            orientation = "0.0";
        } else if(orientation.equals("right")) {
            orientation = "1.0";
        } else if(orientation.equals("bottom")) {
            orientation = "2.0";
        } else if(orientation.equals("left")) {
            orientation = "3.0";
        } else {
        	throw new ConverterException (
        		"-> orientation '"+orientation+"' of element 'Brace' is not valid\n");
        }
		mappObject[colRotation] = orientation;

		mappObject[colCenterX] = graphics.getAttributeValue("CenterX");
    	mappObject[colCenterY] = graphics.getAttributeValue("CenterY");
    	mappObject[colWidth] = graphics.getAttributeValue("Width");
    	mappObject[colHeight] = graphics.getAttributeValue("PicPointOffset");
    	mappObject[colColor] = ConvertType.toMappColor(graphics.getAttributeValue("Color"));
    }

    public static Element mapBraceType(String[] mappObject) throws ConverterException
    {
        Element e = new Element("Brace");

        Element graphics = new Element ("Graphics");
        
        graphics.setAttribute("CenterX", mappObject[colCenterX]);
        graphics.setAttribute("CenterY", mappObject[colCenterY]);
        graphics.setAttribute("PicPointOffset", mappObject[colHeight]);
        graphics.setAttribute("Width", mappObject[colWidth]);		
		graphics.setAttribute("Color", ConvertType.toGmmlColor(mappObject[colColor]));
		
        String orientation = mappObject[colRotation];
		if(orientation.equals("0.0")) {
            orientation = "top";
        } else if(orientation.equals("1.0")) {
            orientation = "right";
        } else if(orientation.equals("2.0")) {
            orientation = "bottom";
        } else if(orientation.equals("3.0")) {
            orientation = "left";
        } else {
        	throw new ConverterException (
        		"-> orientation '"+orientation+"' of element 'Brace' is not valid\n");
        }
		graphics.setAttribute("Orientation", orientation);
		
        e.addContent(graphics);
            
        return e;          
    }
    
    public static void unmapGeneProductType (Element e, String[] mappObject) throws ConverterException
    {    	
    	mappObject[colType] = "Gene";
    	mappObject[colSystemCode] =
			mapBetween (dataSources, systemCodes, 
					e.getAttributeValue("GeneProduct-Data-Source"));

		mappObject[colHead] = e.getAttributeValue("BackpageHead");
		mappObject[colID] = e.getAttributeValue("Name");
		mappObject[colLabel] = e.getAttributeValue("GeneID");
		mappObject[colLinks] = e.getAttributeValue("Xref");
    	
    	Element graphics = e.getChild("Graphics");
    	mappObject[colWidth] = graphics.getAttributeValue("Width");
    	mappObject[colHeight] = graphics.getAttributeValue("Height");
    	mappObject[colCenterX] = graphics.getAttributeValue("CenterX");
    	mappObject[colCenterY] = graphics.getAttributeValue("CenterY");    	
    }
    
    final static String[] systemCodes = 
		{ 
		"D", "F", "G", "I", "L", "M",
		"Q", "R", "S", "T", "U",
		"W", "Z", "X", "O", ""
		};
	
	final static String[] dataSources = 
		{
		"SGD", "FlyBase", "GenBank", "InterPro" ,"LocusLink", "MGI",
		"RefSeq", "RGD", "SwissProt", "GeneOntology", "UniGene",
		"WormBase", "ZFIN", "Affy", "Other", ""
		};

    public static Element mapGeneProductType(String[] mappObject) throws ConverterException
	{
        Element e = new Element("GeneProduct");
    
        if (mappObject[colSystemCode] == null) mappObject[colSystemCode] = "";
        if (mappObject[colLinks] == null) mappObject[colLinks] = "";
        
        mappObject[colSystemCode] = mappObject[colSystemCode].trim();
		e.setAttribute ("GeneProduct-Data-Source", mapBetween (
				systemCodes, dataSources, mappObject[colSystemCode]));

        e.setAttribute ("BackpageHead", mappObject[colHead]);
        e.setAttribute ("Name", mappObject[colID]);
        e.setAttribute ("GeneID", mappObject[colLabel]);

        // TODO:  for some IDs the type is known, e.g. SwissProt is always a
		// protein, incorporate this knowledge to assign a type per ID
        e.setAttribute ("Type", "unknown");
        e.setAttribute ("Xref", mappObject[colLinks]);
        
        Element graphics = new Element ("Graphics");
        
        graphics.setAttribute("CenterX", mappObject[colCenterX]);
        graphics.setAttribute("CenterY", mappObject[colCenterY]);
        graphics.setAttribute("Height", mappObject[colHeight]);
        graphics.setAttribute("Width", mappObject[colWidth]);        
        
        e.addContent(graphics);

        return e;			
	}
    
	public static Element mapInfoBoxType (String[] mappObject)
	{
        Element e = new Element("InfoBox");
        
        e.setAttribute("CenterX", mappObject[colCenterX]);
        e.setAttribute("CenterY", mappObject[colCenterY]);
                
        return e;
	}
	
	public static void unmapInfoBoxType (Element e, String[] mappObject)
    {    	
    	mappObject[colType] = "InfoBox";
    	
    	mappObject[colCenterX] = e.getAttributeValue("CenterX");
    	mappObject[colCenterY] = e.getAttributeValue("CenterY");    	
    }

	public static Element mapLegendType (String[] mappObject)
	{
        Element e = new Element("Legend");
        
        e.setAttribute("CenterX", mappObject[colCenterX]);
        e.setAttribute("CenterY", mappObject[colCenterY]);
                
        return e;
	}
	
	public static void unmapLegendType (Element e, String[] mappObject)
    {    	
    	mappObject[colType] = "Legend";
    	
    	mappObject[colCenterX] = e.getAttributeValue("CenterX");
    	mappObject[colCenterY] = e.getAttributeValue("CenterY");    	
    }

	final static int styleBold = 1; 
    final static int styleItalic = 2;
    final static int styleUnderline = 4;
    final static int styleStrikethru = 8;
    
    public static Element mapLabelType(String[] mappObject) 
    {
        Element e = new Element("Label");

        String label = mappObject[colLabel];
        e.setAttribute("TextLabel", label == null ? "" : label);
        
        Element graphics = new Element ("Graphics");
        
        graphics.setAttribute("CenterX", mappObject[colCenterX]);
        graphics.setAttribute("CenterY", mappObject[colCenterY]);
        graphics.setAttribute("Height", mappObject[colHeight]);
        graphics.setAttribute("Width", mappObject[colWidth]);		
        graphics.setAttribute("Color", ConvertType.toGmmlColor(mappObject[colColor]));
        String fontname = mappObject[colID];
        if (fontname != null) graphics.setAttribute("FontName", fontname);
        graphics.setAttribute("FontSize", ConvertType.makeInteger(mappObject[colSecondX]));
        
        String styleString = mappObject[colSystemCode]; 
        int style = styleString == null ? 0 : (int)(styleString.charAt(0));
                
        if ((style & styleBold) > 0)
        	graphics.setAttribute("FontWeight", "Bold");
        
        if ((style & styleItalic) > 0)
        	graphics.setAttribute("FontStyle", "Italic");

        if ((style & styleUnderline) > 0)
        	graphics.setAttribute("FontDecoration", "Underline");

        if ((style & styleStrikethru) > 0)
        	graphics.setAttribute("FontStrikethru", "Strikethru");            

        e.addContent(graphics);
        
        return e;
    }

    public static void unmapLabelType (Element e, String[] mappObject)
    {    	
    	mappObject[colType] = "Label";
    	mappObject[colLabel] = e.getAttributeValue("TextLabel");
    	
    	Element graphics = e.getChild("Graphics");
    	mappObject[colCenterX] = graphics.getAttributeValue("CenterX");
    	mappObject[colCenterY] = graphics.getAttributeValue("CenterY");
    	mappObject[colWidth] = graphics.getAttributeValue("Width");
    	mappObject[colHeight] = graphics.getAttributeValue("Height");
    	mappObject[colColor] = ConvertType.toMappColor(graphics.getAttributeValue("Color"));
    	mappObject[colID] = graphics.getAttributeValue("FontName");
    	mappObject[colSecondX] = graphics.getAttributeValue("FontSize");
    	
    	int style = 16; 
    	// note: from VB source I learned that 16 is added to prevent field from becoming 0, 
    	// as this can't be stored in a text field in the database
    	String fontWeight = graphics.getAttributeValue("FontWeight");
    	String fontStyle = graphics.getAttributeValue("FontStyle");
    	String fontDecoration = graphics.getAttributeValue ("FontDecoration");
    	String fontStrikethru = graphics.getAttributeValue ("FontStrikethru");
    	if (fontWeight != null && fontWeight.equals("Bold")) style |= styleBold;   	
    	if (fontStyle != null && fontStyle.equals("Italic")) style |= styleItalic;    	
    	if (fontDecoration != null && fontDecoration.equals("Underline")) style |= styleUnderline;    	
    	if (fontStrikethru != null && fontStrikethru.equals("Strikethru")) style |= styleStrikethru;
    	
    	char stylechars[] = new char[1];
    	stylechars[0] = (char)style;
    	mappObject[colSystemCode] = new String (stylechars);    	
    }
    
    public static Element mapShapeType(String[] mappObject)
    {
        Element e = new Element("Shape");

        e.setAttribute("Type", mappObject[colType]);
        
        Element graphics = new Element ("Graphics");
        
        graphics.setAttribute("CenterX", mappObject[colCenterX]);
        graphics.setAttribute("CenterY", mappObject[colCenterY]);
        graphics.setAttribute("Height", mappObject[colHeight]);
        graphics.setAttribute("Width", mappObject[colWidth]);		
        graphics.setAttribute("Rotation", mappObject[colRotation]);		
		graphics.setAttribute("Color", ConvertType.toGmmlColor(mappObject[colColor]));
		
        e.addContent(graphics);
        
        return e;
    }
    
    public static void unmapShapeType (Element e, String[] mappObject)
    {    	
    	mappObject[colType] = e.getAttributeValue("Type");
    	
    	Element graphics = e.getChild("Graphics");
    	mappObject[colCenterX] = graphics.getAttributeValue("CenterX");
    	mappObject[colCenterY] = graphics.getAttributeValue("CenterY");
    	mappObject[colWidth] = graphics.getAttributeValue("Width");
    	mappObject[colHeight] = graphics.getAttributeValue("Height");
    	mappObject[colRotation] = graphics.getAttributeValue("Rotation");
    	mappObject[colColor] = ConvertType.toMappColor(graphics.getAttributeValue("Color"));
    	
    }
    
    public static Element mapFixedShapeType( String[] mappObject)
    {
        Element e = new Element ("FixedShape");
    	e.setAttribute("Type", mappObject[colType]);
        Element graphics = new Element ("Graphics");
        
        graphics.setAttribute("CenterX", mappObject[colCenterX]);
        graphics.setAttribute("CenterY", mappObject[colCenterY]);

        e.addContent(graphics);
        
        return e;        
    }

    public static void unmapFixedShapeType (Element e, String[] mappObject)
    {    	
    	String type = e.getAttributeValue("Type");
    	mappObject[colType] = type;
    	
    	if (type.equals("CellA"))
    	{
    		mappObject[colRotation] = "-1.308997";
    		mappObject[colColor] = "0";
    		mappObject[colWidth] = "1500";
    		mappObject[colHeight] = "375";
    	}    	
    	
    	Element graphics = e.getChild("Graphics");
    	mappObject[colCenterX] = graphics.getAttributeValue("CenterX");
    	mappObject[colCenterY] = graphics.getAttributeValue("CenterY");    	
    }
        
    
    public static Element mapComplexShapeType(String[] mappObject) throws ConverterException 
	{       		
    	Element e = new Element("ComplexShape");
        Element graphics = new Element ("Graphics");
        
        String type;
        if (mappObject[colType].equals("Poly"))
        {
        	switch ((int)Double.parseDouble(mappObject[colSecondY]))
        	{
        	case 3: type = "Triangle"; break;
        	case 5: type = "Pentagon"; break;
        	case 6: type = "Hexagon"; break;
        	default: throw
        		new ConverterException ("Found polygon with unexpectec edge count: " + 
        				mappObject[colSecondY]); 
        	}
        }
        else if (mappObject[colType].equals("Vesicle"))
        {
        	type = "Vesicle";        
        }
        else if (mappObject[colType].equals("ProteinB"))
        {
        	type = "ProteinComplex";
        }
        else
        {
        	throw new ConverterException (
        			"Unexpected ComplexShape type: " + mappObject[colType]);
        }
        
        e.setAttribute("Type", type);
        
        graphics.setAttribute("CenterX", mappObject[colCenterX]);
        graphics.setAttribute("CenterY", mappObject[colCenterY]);
        graphics.setAttribute("Width", mappObject[colWidth]);
        graphics.setAttribute("Rotation", mappObject[colRotation]);
                
        e.addContent(graphics);
        
        return e;
    }
    
    public static void unmapComplexShapeType (Element e, String[] mappObject)
    {   
    	String type = e.getAttributeValue("Type");
    	if (type.equals("Triangle"))
    	{
    		mappObject[colType] = "Poly";
    		mappObject[colSecondY] = "3";
    	} else if (type.equals("Pentagon"))
    	{
    		mappObject[colType] = "Poly";
    		mappObject[colSecondY] = "5";    		
		} else if (type.equals("Hexagon"))
		{
			mappObject[colType] = "Poly";
			mappObject[colSecondY] = "6";  			
		} else if (type.equals("ProteinComplex"))
		{
			mappObject[colType] = "ProteinB";
		} else if (type.equals("Vesicle"))
		{
			mappObject[colType] = "Vesicle";
		}
    	
    	Element graphics = e.getChild("Graphics");
    	mappObject[colCenterX] = graphics.getAttributeValue("CenterX");
    	mappObject[colCenterY] = graphics.getAttributeValue("CenterY");
    	mappObject[colWidth] = graphics.getAttributeValue("Width");
    	mappObject[colRotation] = graphics.getAttributeValue("Rotation");    	
    }
    
}
