package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

/**
 * GmmlDataObject is responsible for maintaining the data
 * for all the individual objects that can appear on a pwy
 * (Lines, GeneProducts, Shapes, etc.) 
 * 
 * GmmlDataObjects
 * contain a union of all possible fields (e.g it has
 * both start and endpoints for lines, and label text for labels)
 * Each field can be accessed through a specific accessor, or
 * through getProperty() and setProperty()
 * 
 * most fields cannot be set to null. Notable exceptions are
 * graphId, startGraphRef and endGraphRef.
 * 
 * @author Martijn
 *
 */
public class GmmlDataObject
{		
	/**
	 * Default constructor will be removed, to force setting
	 * of object type to a valid value.
	 * 
	 * @deprecated
	 */
	public GmmlDataObject ()
	{
	
	}
	
	/**
	 * The required parameter objectType ensures only
	 * objects with a valid type can be created.
	 * 
	 * @param ot Type of object, one of the ObjectType.* fields
	 */
	public GmmlDataObject (int ot)
	{
		if (ot < ObjectType.MIN_VALID || ot > ObjectType.MAX_VALID)
		{
			throw new IllegalArgumentException("Trying to set objectType to " + ot);
		}
		objectType = ot;
	}
	
	/**
	 * Parent of this object: may be null (for example,
	 * when object is in clipboard)
	 */
	private GmmlData parent = null;
	public GmmlData getParent() { return parent; }
	
	/**
	 * Set parent. Do not use this method directly!
	 * parent is set automatically when using GmmlData.add/remove
	 * 
	 * This method takes care of graphref reference accounting.
	 * @param v the parent
	 */
	public void setParent(GmmlData v)
	{
		if (v != parent)
		{
			if (parent != null)
			{
				if (startGraphRef != null)
				{
					parent.removeRef(startGraphRef, this);
				}
				if (endGraphRef != null)
				{
					parent.removeRef(startGraphRef, this);
				}
			}			
			parent = v;
			if (v != null)
			{
				if (startGraphRef != null)
				{
					v.addRef(startGraphRef, this);
				}
				if (endGraphRef != null)
				{
					v.addRef(startGraphRef, this);
				}
			}
		}
	}
		
	public List<String> getAttributes()
	{
		List<String> result = Arrays.asList(new String[] { 
				"Notes", "Comment"
		});
		switch (getObjectType())
		{
			case ObjectType.MAPPINFO:
				result = ( Arrays.asList (new String[] {
						"Notes", "Comment",
						"MapInfoName", "Organism", "Data-Source",
						"Version", "Author", "Maintained-By", 
						"Email", "Last-modified", "Availability",
						"BoardWidth", "BoardHeight", "WindowWidth", "WindowHeight"						
				}));
				break;
			case ObjectType.GENEPRODUCT:
				result = ( Arrays.asList (new String[] {
						"Notes", "Comment",
						"CenterX", "CenterY", "Width", "Height",
						
						// line, shape, brace, geneproduct, label
						"Color",				
						// gene product
						"Name", "GeneProduct-Data-Source", "GeneID", 
						"Xref", "BackpageHead", "Type", "GraphId"
				}));
				break;
			case ObjectType.SHAPE:
				result = ( Arrays.asList(new String[] {
						"Notes", "Comment",
						"CenterX", "CenterY", "Width", "Height",
						// line, shape, brace, geneproduct, label
						"Color", 
						
						// shape
						"ShapeType", "Rotation", "GraphId"
				}));
				break;
			case ObjectType.BRACE:
				result = (Arrays.asList(new String[] {
						
						"Notes", "Comment",
						"CenterX", "CenterY", "Width", "Height",
						// line, shape, brace, geneproduct, label
						"Color", 
						
						// brace
						"Orientation", "GraphId"
				}));
				break;
			case ObjectType.LINE:
				result = ( Arrays.asList(new String[] {
						"Notes", "Comment",
						
						// line, shape, brace, geneproduct, label
						"Color", 
						
						// line
						"StartX", "StartY", "EndX", "EndY",			
						"LineType", "LineStyle", "StartGraphRef", "EndGraphRef"						
				}));
				break;
			case ObjectType.LABEL:
				result = ( Arrays.asList(new String[] {
						"Notes", "Comment", "Xref", 
						"CenterX", "CenterY", "Width", "Height",
						// line, shape, brace, geneproduct, label
						"Color", 
						// label
						"TextLabel", 
						"FontName","FontWeight","FontStyle","FontSize",
						"GraphId"
				}));
				break;
				
		}
		return result;
	}
	

	public static final List<String> attributes = Arrays.asList(new String[] {
			
			// all
			"Notes", "Comment",

			// line, shape, brace, geneproduct, label
			"Color", 
			
			// shape, brace, geneproduct, label
			"CenterX", "CenterY", "Width", "Height", 
			
			// shape
			"ShapeType", "Rotation", 
			
			// line
			"StartX", "StartY", "EndX", "EndY",			
			"LineType", "LineStyle",
			
			// brace
			"Orientation",
			
			// gene product
			"Name", "GeneProduct-Data-Source", "GeneID", 
			"Xref", "BackpageHead", "Type", 
			
			// label
			"TextLabel", 
			"FontName","FontWeight","FontStyle","FontSize",

			// mappinfo
			"MapInfoName", "Organism", "Data-Source",
			"Version", "Author", "Maintained-By", 
			"Email", "Last-modified", "Availability",
			"BoardWidth", "BoardHeight", "WindowWidth", "WindowHeight",
			
			// other
			"GraphId", "StartGraphRef", "EndGraphRef"
	});
	
	/**
	 * This works so that
	 * o.setNotes(x) is the equivalent of
	 * o.setProperty("Notes", x);
	 * 
	 * Value may be null in some cases, e.g. graphRef
	 * 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, Object value)
	{
		assert (key != null);
		
		int i = attributes.indexOf(key);
		// TODO: use enum instead of integer index.
		switch (i)
		{		
			case 0: setNotes		((String) value); break;
			case 1: setComment 		((String) value); break;
	
			case 2: setColor 		((RGB)    value); break;
				
			case 3: setCenterX 		((Double) value); break;
			case 4: setCenterY 		((Double) value); break;
			case 5: setWidth		((Double) value); break;
			case 6: setHeight		((Double) value); break;
			
			case 7: setShapeType	((Integer)value); break;
			case 8: setRotation		((Double) value); break;
				
			case 9: setStartX 		((Double) value); break;
			case 10: setStartY 		((Double) value); break;
			case 11: setEndX 		((Double) value); break;
			case 12: setEndY 		((Double) value); break;
			case 13: setLineType		((Integer)value); break;
			case 14: setLineStyle	((Integer)value); break;
				
			case 15: setOrientation	((Integer)value); break;
	
			case 16: setGeneProductName ((String) value); break;
			case 17: setDataSource		((String) value); break;
			case 18: setGeneID			((String)value); break;
			case 19: setXref			((String)  value); break;
			case 20: setBackpageHead	((String)value); break;
			case 21: setGeneProductType ((String)  value); break;
			
			case 22: setLabelText 	((String) value); break;
			case 23: setFontName		((String)  value); break;
			case 24: setBold 		((Boolean) value); break;
			case 25: setItalic 		((Boolean) value); break;
			case 26: setFontSize		((Double)  value); break;

			case 27: setMapInfoName((String) value); break;
			case 28: setOrganism ((String) value); break;
			case 29: setDataSource ((String) value); break;
			case 30: setVersion ((String) value); break;
			case 31: setAuthor ((String) value); break;
			case 32: setMaintainedBy((String) value); break;
			case 33: setEmail ((String) value); break;
			case 34: setLastModified ((String)value); break;
			case 35: setAvailability ((String)value); break;
			case 36: setBoardWidth ((Double)value); break;
			case 37: setBoardHeight ((Double)value); break;
			case 38: setWindowWidth ((Double)value); break;
			case 39: setWindowHeight ((Double)value); break;
			
			case 40: setGraphId ((String)value); break;
			case 41: setStartGraphRef ((String)value); break;
			case 42: setEndGraphRef ((String)value); break;
			default: throw new IllegalArgumentException("Invalid key");
		}
	}
	
	public Object getProperty(String key)
	{		
		//TODO: use hashtable or other way better than switch statement
		int i = attributes.indexOf(key);
		Object result = null;
		switch (i)
		{
			case 0: result = getNotes(); break;
			case 1: result = getComment(); break;
			case 2: result = getColor(); break;
			
			case 3: result = getCenterX(); break;
			case 4: result = getCenterY(); break;
			case 5: result = getWidth(); break;
			case 6: result = getHeight(); break;
			
			case 7: result = getShapeType(); break;
			case 8: result = getRotation(); break;
			
			case 9: result = getStartX(); break;
			case 10: result = getStartY(); break;
			case 11: result = getEndX(); break;
			case 12: result = getEndY(); break;
			case 13: result = getLineType(); break;
			case 14: result = getLineStyle(); break;
			
			case 15: result = getOrientation(); break;
						
			case 16: result = getGeneProductName(); break;
			case 17: result = getDataSource(); break;
			case 18: result = getGeneID(); break;
			case 19: result = getXref(); break;
			case 20: result = getBackpageHead(); break;
			case 21: result = getGeneProductType(); break;
			
			case 22: result = getLabelText(); break;	
			case 23: result = getFontName(); break;
			case 24: result = isBold(); break;
			case 25: result = isItalic(); break;
			case 26: result = getFontSize(); break;

			case 27: result = getMapInfoName(); break;
			case 28: result = getOrganism (); break;
			case 29: result = getDataSource (); break;
			case 30: result = getVersion (); break;
			case 31: result = getAuthor (); break;
			case 32: result = getMaintainedBy(); break;
			case 33: result = getEmail (); break;
			case 34: result = getLastModified (); break;
			case 35: result = getAvailability (); break;
			case 36: result = getBoardWidth (); break;
			case 37: result = getBoardHeight (); break;
			case 38: result = getWindowWidth (); break;
			case 39: result = getWindowHeight (); break;

			case 40: result = getGraphId (); break;
			case 41: result = getStartGraphRef (); break;
			case 42: result = getEndGraphRef (); break;

		}

		return result;
	}
	
	/**
	 * Copy Object. The object will not
	 * be part of the same GmmlData object, it's parent
	 * will be set to null.
	 * 
	 * No events will be sent to the parent of the original.
	 */
	public GmmlDataObject copy()
	{
		GmmlDataObject result = new GmmlDataObject(objectType);
		result.parent = null;
		result.author = author;
		result.availability = availability;
		result.backpageHead = backpageHead;
		result.boardHeight = boardHeight;
		result.boardWidth = boardWidth;
		result.centerx = centerx;
		result.centery = centery;
		result.color = color;
		result.comment = comment;
		result.dataSource = dataSource;
		result.email = email;
		result.endx = endx;
		result.endy = endy;
		result.fBold = fBold;
		result.fItalic = fItalic;
		result.fontName = fontName;
		result.fontSize = fontSize;
		result.fStrikethru = fStrikethru;
		result.fTransparent = fTransparent;
		result.fUnderline = fUnderline;
		result.geneID = geneID;
		result.geneProductName = geneProductName;
		result.geneProductType = geneProductType;
		result.height = height;
		result.labelText = labelText;
		result.lastModified = lastModified;
		result.lineStyle = lineStyle;
		result.lineType = lineType;
		result.maintainedBy = maintainedBy;
		result.mapInfoDataSource = mapInfoDataSource;
		result.mapInfoLeft = mapInfoLeft;
		result.mapInfoName = mapInfoName;
		result.mapInfoTop = mapInfoTop;
		result.notes = notes;
		result.organism = organism;
		result.rotation = rotation;
		result.shapeType = shapeType;
		result.startx = startx;
		result.starty = starty;
		result.version = version;
		result.width = width;
		result.windowHeight = windowHeight;
		result.windowWidth = windowWidth;
		result.xref = xref;
		result.startGraphRef = startGraphRef;
		result.endGraphRef = endGraphRef;
		result.graphId = graphId;
		
		return result;
	}

	protected int objectType = ObjectType.GENEPRODUCT;
	public int getObjectType() { return objectType; }
	
	/**
	 * in the future, change of objecttype won't be possible at all.
	 * Objecttype should be set through constructor
	 * 
	 * @deprecated
	 * @param v
	 */
	public void setObjectType(int v) 
	{ 
		if (objectType != v)
		{
			objectType = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	// only for lines:	
	protected double startx = 0;
	public double getStartX() { return startx; }
	public void setStartX(double v) 
	{ 
		if (startx != v)
		{
			startx = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double starty = 0;
	public double getStartY() { return starty; }
	public void setStartY(double v) 
	{ 
		if (starty != v)
		{
			starty = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double endx = 0;
	public double getEndX() { return endx; }
	public void setEndX(double v) 
	{
		if (endx != v)
		{
			endx = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double endy = 0;
	public double getEndY() { return endy; }
	public void setEndY(double v) 
	{
		if (endy != v)
		{
			endy = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL)); 
		}
	}
	
	protected int lineStyle = LineStyle.SOLID;
	public int getLineStyle() { return lineStyle; }
	public void setLineStyle(int value) 
	{ 
		if (lineStyle != value)
		{
			lineStyle = value; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected int lineType = LineType.LINE;
	public int getLineType() { return lineType; }
	public void setLineType(int value) 
	{
		if (lineType != value)
		{
			lineType = value; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL)); 
		}
	}
			
	protected RGB color = new RGB(0, 0, 0);	
	public RGB getColor() { return color; }
	public void setColor(RGB v) 
	{
		if (v == null) throw new IllegalArgumentException();
		if (color != v)
		{
			color = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL)); 
		}
	}
	
	protected boolean fTransparent;
	public boolean isTransparent() { return fTransparent; }
	public void setTransparent(boolean v) 
	{
		if (fTransparent != v)
		{
			fTransparent = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	// general
	protected String comment = "";
	public String getComment() { return comment; }
	public void setComment (String v) 
	{
		if (v == null) throw new IllegalArgumentException();
		if (comment != v)
		{
			comment = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected String notes = "";
	public String getNotes() { return notes; }
	public void setNotes (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (notes != v)
		{
			notes = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	// for geneproduct only
	protected String geneID = "";
	public String getGeneID() { return geneID; }
	public void setGeneID(String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (geneID != v)
		{
			geneID = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected String xref = "";
	public String getXref() { return xref; }
	public void setXref(String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (xref != v)
		{
			xref = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected String geneProductName = "";
	public String getGeneProductName() { return geneProductName; }
	public void setGeneProductName(String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (geneProductName != v)
		{
			geneProductName = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	} 
	
	protected String backpageHead = "";
	public String getBackpageHead() { return backpageHead; }
	public void setBackpageHead(String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (backpageHead != v)
		{
			backpageHead = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected String geneProductType = "unknown";
	public String getGeneProductType() { return geneProductType; }
	public void setGeneProductType(String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (geneProductType != v)
		{
			geneProductType = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL)); 
		}
	}
	
	protected String dataSource = "";
	public String getDataSource() { return dataSource; }
	public void setDataSource(String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (dataSource != v)
		{
			dataSource = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	/**
	 * SystemCode is a one- or two-letter abbreviation of datasource,
	 * used in the MappFormat but also in databases.
	 */
	public String getSystemCode()
	{
		String systemCode = "";
		if(MappFormat.sysName2Code.containsKey(dataSource)) 
			systemCode = MappFormat.sysName2Code.get(dataSource);
		return systemCode;
	}
	 
	protected double centerx = 0;
	public double getCenterX() { return centerx; }
	public void setCenterX(double v) 
	{
		if (centerx != v)
		{
			centerx = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL)); 
		}
	}
	
	protected double centery = 0;
	public double getCenterY() { return centery; }
	public void setCenterY(double v) 
	{ 
		if (centery != v)
		{
			centery = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double width = 0;
	public double getWidth() { return width; }
	public void setWidth(double v) 
	{ 
		if (width != v)
		{
			width = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double height = 0;
	public double getHeight() { return height; }
	public void setHeight(double v) 
	{ 
		if (height != v)
		{
			height = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
		
	// starty for shapes
	public double getTop() { return centery - height / 2; }
	public void setTop(double v) 
	{ 
		centery = v + height / 2;
		fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
	}
	
	// startx for shapes
	public double getLeft() { return centerx - width / 2; }
	public void setLeft(double v) 
	{ 
		centerx = v + width / 2;
		fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
	}
	
	protected int shapeType = ShapeType.RECTANGLE;
	public int getShapeType() { return shapeType; }
	public void setShapeType(int v) 
	{ 
		if (shapeType != v)
		{
			shapeType = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	public void setOrientation(int orientation) {
		switch (orientation)
		{
			case OrientationType.TOP: setRotation(0); break;
			case OrientationType.LEFT: setRotation(Math.PI/2); break;
			case OrientationType.BOTTOM: setRotation(Math.PI); break;
			case OrientationType.RIGHT: setRotation(Math.PI*(3.0/2)); break;
		}
	}
		
	public int getOrientation() {
		double r = rotation / Math.PI;
		if(r < 1.0/4 || r >= 7.0/4) return OrientationType.TOP;
		if(r > 1.0/4 && r <= 3.0/4) return OrientationType.LEFT;
		if(r > 3.0/4 && r <= 5.0/4) return OrientationType.BOTTOM;
		if(r > 5.0/4 && r <= 7.0/4) return OrientationType.RIGHT;
		return 0;
	}

	protected double rotation = 0; // in radians
	public double getRotation() { return rotation; }
	public void setRotation(double v) 
	{ 
		if (rotation != v)
		{
			rotation = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	// for labels
	protected boolean fBold = false;
	public boolean isBold() { return fBold; }
	public void setBold(boolean v) 
	{ 
		if (fBold != v)
		{
			fBold = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected boolean fStrikethru = false;
	public boolean isStrikethru() { return fStrikethru; }
	public void setStrikethru(boolean v) 
	{ 
		if (fStrikethru != v)
		{
			fStrikethru = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected boolean fUnderline = false;
	public boolean isUnderline() { return fUnderline; }
	public void setUnderline(boolean v) 
	{ 
		if (fUnderline != v)
		{
			fUnderline = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected boolean fItalic = false;
	public boolean isItalic() { return fItalic; }
	public void setItalic(boolean v) 
	{ 
		if (fItalic != v)
		{
			fItalic = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected String fontName= "Arial";
	public String getFontName() { return fontName; }
	public void setFontName(String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (fontName != v)
		{
			fontName = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected String labelText = "";
	public String getLabelText() { return labelText; }
	public void setLabelText (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (labelText != v)
		{
			labelText = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double fontSize = 1;	
	public double getFontSize() { return fontSize; }
	public void setFontSize(double v) 
	{ 
		if (fontSize != v)
		{
			fontSize = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}	
	
	protected String mapInfoName = "";
	public String getMapInfoName() { return mapInfoName; }
	public void setMapInfoName (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (mapInfoName != v)
		{
			mapInfoName = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected String organism = "";
	public String getOrganism() { return organism; }
	public void setOrganism (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (organism != v)
		{
			organism = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String mapInfoDataSource = "";
	public String getMapInfoDataSource() { return mapInfoDataSource; }
	public void setMapInfoDataSource (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (mapInfoDataSource != v)
		{
			mapInfoDataSource = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String version = "";
	public String getVersion() { return version; }
	public void setVersion (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (version != v)
		{
			version = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String author = "";
	public String getAuthor() { return author; }
	public void setAuthor (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (author != v)
		{
			author = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String maintainedBy = ""; 
	public String getMaintainedBy() { return maintainedBy; }
	public void setMaintainedBy (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (maintainedBy != v)
		{
			maintainedBy = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String email = "";
	public String getEmail() { return email; }
	public void setEmail (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (email != v)
		{
			email = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String availability = "";
	public String getAvailability() { return availability; }
	public void setAvailability (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (availability != v)
		{
			availability = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String lastModified = "";
	public String getLastModified() { return lastModified; }
	public void setLastModified (String v) 
	{ 
		if (v == null) throw new IllegalArgumentException();
		if (lastModified != v)
		{
			lastModified = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double boardWidth;
	public double getBoardWidth() { return boardWidth; }
	public void setBoardWidth(double v) 
	{ 
		if (boardWidth != v)
		{
			boardWidth = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.WINDOW));
		}
	}

	protected double boardHeight;
	public double getBoardHeight() { return boardHeight; }
	public void setBoardHeight(double v) 
	{ 
		if (boardHeight != v)
		{
			boardHeight = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.WINDOW));
		}
	}

	protected double windowWidth;
	public double getWindowWidth() { return windowWidth; }
	public void setWindowWidth(double v) 
	{ 
		if (windowWidth != v)
		{
			windowWidth = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.WINDOW));
		}
	}

	protected double windowHeight;
	public double getWindowHeight() { return windowHeight; }
	public void setWindowHeight(double v) 
	{ 
		if (windowHeight != v)
		{
			windowHeight = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.WINDOW));
		}
	}
	
	protected int mapInfoLeft;
	public int getMapInfoLeft() { return mapInfoLeft; }
	public void setMapInfoLeft(int v) 
	{ 
		if (mapInfoLeft != v)
		{
			mapInfoLeft = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	protected int mapInfoTop;
	public int getMapInfoTop() { return mapInfoTop; }
	public void setMapInfoTop(int v) 
	{ 
		if (mapInfoTop != v)
		{
			mapInfoTop = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String graphId = null;
	public String getGraphId() { return graphId; }
	public void setGraphId (String v) 
	{ 
		if (graphId != v)
		{
			graphId = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String startGraphRef = null;
	public String getStartGraphRef() { return startGraphRef; }
	/**
	 * Set a reference to another object with a graphId.
	 * If a parent is set, this will automatically deregister
	 * the previously held reference and register the new reference
	 * as necessary
	 * @param v: reference to set.
	 */
	public void setStartGraphRef (String v) 
	{ 
		if (startGraphRef != v)
		{
			if (parent != null)
			{
				if (startGraphRef != null)
				{
					parent.removeRef(startGraphRef, this);
				}
				if (v != null)
				{
					parent.addRef(v, this);
				}
			}
			startGraphRef = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	protected String endGraphRef = null;
	public String getEndGraphRef() { return endGraphRef; }
	/**
	 * @see setStartGraphRef();
	 * @param v
	 */
	public void setEndGraphRef (String v) 
	{ 
		if (endGraphRef != v)
		{
			if (parent != null)
			{
				if (endGraphRef != null)
				{
					parent.removeRef(endGraphRef, this);
				}
				if (v != null)
				{
					parent.addRef(v, this);
				}
			}
			endGraphRef = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}

	int noFire = 0;
	public void dontFireEvents(int times) {
		noFire = times;
	}
	
	private List<GmmlListener> listeners = new ArrayList<GmmlListener>();
	public void addListener(GmmlListener v) { listeners.add(v); }
	public void removeListener(GmmlListener v) { listeners.remove(v); }
	public void fireObjectModifiedEvent(GmmlEvent e) 
	{
		if(noFire > 0) {
			noFire -= 1;
			return;
		}
		for (GmmlListener g : listeners)
		{
			g.gmmlObjectModified(e);
		}
	}

}
