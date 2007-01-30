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
package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private static final int M_INITIAL_SHAPE_SIZE = 30; // initial Radius for rect and oval
	private static final int M_INITIAL_BRACE_HEIGHT = 15;
	private static final int M_INITIAL_BRACE_WIDTH = 60; 
	private static final int M_INITIAL_GENEPRODUCT_WIDTH = 80;
	private static final int M_INITIAL_GENEPRODUCT_HEIGHT = 20;
	
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
				if (graphId != null)
				{
					parent.removeGraphId(graphId);
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
				if (graphId != null)
				{
					parent.addGraphId(graphId);
				}
			}
		}
	}
		
	public List<PropertyType> getAttributes()
	{
		List<PropertyType> result = Arrays.asList(new PropertyType[] { 
				PropertyType.NOTES, 
				PropertyType.COMMENT
		});
		switch (getObjectType())
		{
			case ObjectType.MAPPINFO:
				result = ( Arrays.asList (new PropertyType[] {
						PropertyType.NOTES, 
						PropertyType.COMMENT,
						PropertyType.MAPINFONAME,
						PropertyType.ORGANISM,
						PropertyType.DATA_SOURCE,
						PropertyType.VERSION,
						PropertyType.AUTHOR,
						PropertyType.MAINTAINED_BY,
						PropertyType.EMAIL,
						PropertyType.LAST_MODIFIED,
						PropertyType.AVAILABILITY,
						PropertyType.BOARDWIDTH,
						PropertyType.BOARDHEIGHT,
						PropertyType.WINDOWWIDTH,
						PropertyType.WINDOWHEIGHT
				}));
				break;
			case ObjectType.GENEPRODUCT:
				result = ( Arrays.asList (new PropertyType[] {
						PropertyType.NOTES,
						PropertyType.COMMENT,
						PropertyType.CENTERX,
						PropertyType.CENTERY,
						PropertyType.WIDTH,
						PropertyType.HEIGHT,
						PropertyType.COLOR,
						PropertyType.NAME,
						PropertyType.GENEPRODUCT_DATA_SOURCE,
						PropertyType.GENEID,
						//PropertyType.XREF,
						PropertyType.BACKPAGEHEAD,
						PropertyType.TYPE,
						PropertyType.GRAPHID
				}));
				break;
			case ObjectType.SHAPE:
				result = ( Arrays.asList(new PropertyType[] {
						PropertyType.NOTES,
						PropertyType.COMMENT,
						PropertyType.CENTERX,
						PropertyType.CENTERY,
						PropertyType.WIDTH,
						PropertyType.HEIGHT,
						PropertyType.COLOR,
						PropertyType.FILLCOLOR,
						PropertyType.SHAPETYPE,
						PropertyType.ROTATION,
						PropertyType.TRANSPARENT,
						PropertyType.GRAPHID
				}));
				break;
			case ObjectType.BRACE:
				result = (Arrays.asList(new PropertyType[] {
						PropertyType.NOTES,
						PropertyType.COMMENT,
						PropertyType.CENTERX,
						PropertyType.CENTERY,
						PropertyType.WIDTH,
						PropertyType.HEIGHT,
						PropertyType.COLOR,
						PropertyType.ORIENTATION,
						PropertyType.GRAPHID
				}));
				break;
			case ObjectType.LINE:
				result = ( Arrays.asList(new PropertyType[] {
						PropertyType.NOTES,
						PropertyType.COMMENT,
						PropertyType.COLOR,
						PropertyType.STARTX,
						PropertyType.STARTY,
						PropertyType.ENDX,
						PropertyType.ENDY,
						PropertyType.LINETYPE,
						PropertyType.LINESTYLE,
						PropertyType.STARTGRAPHREF,
						PropertyType.ENDGRAPHREF,
						PropertyType.GRAPHID
				}));
				break;
			case ObjectType.LABEL:
				result = ( Arrays.asList(new PropertyType[] {
						PropertyType.NOTES,
						PropertyType.COMMENT,
						PropertyType.XREF,
						PropertyType.CENTERX,
						PropertyType.CENTERY,
						PropertyType.WIDTH,
						PropertyType.HEIGHT,
						PropertyType.COLOR,
						PropertyType.TEXTLABEL,
						PropertyType.FONTNAME,
						PropertyType.FONTWEIGHT,
						PropertyType.FONTSTYLE,
						PropertyType.FONTSIZE,
						PropertyType.GRAPHID
				}));
				break;
				
		}
		return result;
	}
	
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
	public void setProperty(PropertyType key, Object value)
	{
		switch (key)
		{		
			case NOTES: setNotes		((String) value); break;
			case COMMENT: setComment 		((String) value); break;
			case COLOR: setColor 		((RGB)    value); break;
				
			case CENTERX: setMCenterX 		((Double) value); break;
			case CENTERY: setMCenterY 		((Double) value); break;
			case WIDTH: setMWidth		((Double) value); break;
			case HEIGHT: setMHeight		((Double) value); break;
			
			case FILLCOLOR: setFillColor	((RGB)	  value); break;
			case SHAPETYPE: setShapeType	((Integer)value); break;
			case ROTATION: setRotation		((Double) value); break;
				
			case STARTX: setMStartX 		((Double) value); break;
			case STARTY: setMStartY 		((Double) value); break;
			case ENDX: setMEndX 		((Double) value); break;
			case ENDY: setMEndY 		((Double) value); break;
			case LINETYPE: setLineType		((Integer)value); break;
			case LINESTYLE: setLineStyle	((Integer)value); break;
				
			case ORIENTATION: setOrientation	((Integer)value); break;
	
			case NAME: setGeneProductName ((String) value); break;
			case GENEPRODUCT_DATA_SOURCE: setDataSource		((String) value); break;
			case GENEID: setGeneID			((String)value); break;
			case XREF: setXref			((String)  value); break;
			case BACKPAGEHEAD: setBackpageHead	((String)value); break;
			case TYPE: setGeneProductType ((String)  value); break;
			
			case TEXTLABEL: setLabelText 	((String) value); break;
			case FONTNAME: setFontName		((String)  value); break;
			case FONTWEIGHT: setBold 		((Boolean) value); break;
			case FONTSTYLE: setItalic 		((Boolean) value); break;
			case FONTSIZE: setMFontSize		((Double)  value); break;

			case MAPINFONAME: setMapInfoName((String) value); break;
			case ORGANISM: setOrganism ((String) value); break;
			case DATA_SOURCE: setDataSource ((String) value); break;
			case VERSION: setVersion ((String) value); break;
			case AUTHOR: setAuthor ((String) value); break;
			case MAINTAINED_BY: setMaintainedBy((String) value); break;
			case EMAIL: setEmail ((String) value); break;
			case LAST_MODIFIED: setLastModified ((String)value); break;
			case AVAILABILITY: setAvailability ((String)value); break;
			case BOARDWIDTH: setMBoardWidth ((Double)value); break;
			case BOARDHEIGHT: setMBoardHeight ((Double)value); break;
			case WINDOWWIDTH: setWindowWidth ((Double)value); break;
			case WINDOWHEIGHT: setWindowHeight ((Double)value); break;
			
			case GRAPHID: setGraphId ((String)value); break;
			case STARTGRAPHREF: setStartGraphRef ((String)value); break;
			case ENDGRAPHREF: setEndGraphRef ((String)value); break;
			case TRANSPARENT: setTransparent ((Boolean)value); break;
		}
	}
	
	public Object getProperty(PropertyType x)
	{		
		//TODO: use hashtable or other way better than switch statement
		Object result = null;
		switch (x)
		{
			case NOTES: result = getNotes(); break;
			case COMMENT: result = getComment(); break;
			case COLOR: result = getColor(); break;
			
			case CENTERX: result = getMCenterX(); break;
			case CENTERY: result = getMCenterY(); break;
			case WIDTH: result = getMWidth(); break;
			case HEIGHT: result = getMHeight(); break;
			
			case FILLCOLOR: result = getFillColor(); break;
			case SHAPETYPE: result = getShapeType(); break;
			case ROTATION: result = getRotation(); break;
			
			case STARTX: result = getMStartX(); break;
			case STARTY: result = getMStartY(); break;
			case ENDX: result = getMEndX(); break;
			case ENDY: result = getMEndY(); break;
			case LINETYPE: result = getLineType(); break;
			case LINESTYLE: result = getLineStyle(); break;
			
			case ORIENTATION: result = getOrientation(); break;
						
			case NAME: result = getGeneProductName(); break;
			case GENEPRODUCT_DATA_SOURCE: result = getDataSource(); break;
			case GENEID: result = getGeneID(); break;
			case XREF: result = getXref(); break;
			case BACKPAGEHEAD: result = getBackpageHead(); break;
			case TYPE: result = getGeneProductType(); break;
			
			case TEXTLABEL: result = getLabelText(); break;	
			case FONTNAME: result = getFontName(); break;
			case FONTWEIGHT: result = isBold(); break;
			case FONTSTYLE: result = isItalic(); break;
			case FONTSIZE: result = getMFontSize(); break;

			case MAPINFONAME: result = getMapInfoName(); break;
			case ORGANISM: result = getOrganism (); break;
			case DATA_SOURCE: result = getDataSource (); break;
			case VERSION: result = getVersion (); break;
			case AUTHOR: result = getAuthor (); break;
			case MAINTAINED_BY: result = getMaintainedBy(); break;
			case EMAIL: result = getEmail (); break;
			case LAST_MODIFIED: result = getLastModified (); break;
			case AVAILABILITY: result = getAvailability (); break;
			case BOARDWIDTH: result = getMBoardWidth (); break;
			case BOARDHEIGHT: result = getMBoardHeight (); break;
			case WINDOWWIDTH: result = getWindowWidth (); break;
			case WINDOWHEIGHT: result = getWindowHeight (); break;

			case GRAPHID: result = getGraphId (); break;
			case STARTGRAPHREF: result = getStartGraphRef (); break;
			case ENDGRAPHREF: result = getEndGraphRef (); break;
			case TRANSPARENT: result = isTransparent (); break;
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
		result.mBoardHeight = mBoardHeight;
		result.mBoardWidth = mBoardWidth;
		result.mCenterx = mCenterx;
		result.mCentery = mCentery;
		result.color = color;
		result.fillColor = fillColor;
		result.comment = comment;
		result.dataSource = dataSource;
		result.email = email;
		result.mEndx = mEndx;
		result.mEndy = mEndy;
		result.fBold = fBold;
		result.fItalic = fItalic;
		result.fontName = fontName;
		result.mFontSize = mFontSize;
		result.fStrikethru = fStrikethru;
		result.fTransparent = fTransparent;
		result.fUnderline = fUnderline;
		result.geneID = geneID;
		result.geneProductName = geneProductName;
		result.geneProductType = geneProductType;
		result.mHeight = mHeight;
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
		result.mStartx = mStartx;
		result.mStarty = mStarty;
		result.version = version;
		result.mWidth = mWidth;
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
	// TODO: move to point subclass
	protected double mStartx = 0;
	public double getMStartX() { return mStartx; }
	public void setMStartX(double v) 
	{ 
		if (mStartx != v)
		{
			mStartx = v;		
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	// TODO: move to point subclass
	protected double mStarty = 0;
	public double getMStartY() { return mStarty; }
	public void setMStartY(double v) 
	{ 
		if (mStarty != v)
		{
			mStarty = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	// TODO: move to point subclass
	protected double mEndx = 0;
	public double getMEndX() { return mEndx; }
	public void setMEndX(double v) 
	{
		if (mEndx != v)
		{
			mEndx = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	// TODO: move to point subclass
	protected double mEndy = 0;
	public double getMEndY() { return mEndy; }
	public void setMEndY(double v) 
	{
		if (mEndy != v)
		{
			mEndy = v; 
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

	/** 
	 * fillcolor can't be null!
	 */
	protected RGB fillColor = new RGB (0, 0, 0);	
	public RGB getFillColor() { return fillColor; }
	public void setFillColor(RGB v) 
	{
		if (v == null) throw new IllegalArgumentException();
		if (fillColor != v)
		{
			fillColor = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL)); 
		}
	}

	protected boolean fTransparent = true;
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
	 
	// TODO: move to point subclass
	protected double mCenterx = 0;
	public double getMCenterX() { return mCenterx; }
	public void setMCenterX(double v) 
	{
		if (mCenterx != v)
		{
			mCenterx = v; 
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL)); 
		}
	}
	
	// TODO: move to point subclass
	protected double mCentery = 0;
	public double getMCenterY() { return mCentery; }
	public void setMCenterY(double v) 
	{ 
		if (mCentery != v)
		{
			mCentery = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double mWidth = 0;
	public double getMWidth() { return mWidth; }
	public void setMWidth(double v) 
	{ 
		if (mWidth != v)
		{
			mWidth = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	
	protected double mHeight = 0;
	public double getMHeight() { return mHeight; }
	public void setMHeight(double v) 
	{ 
		if (mHeight != v)
		{
			mHeight = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
		
	// starty for shapes
	public double getMTop() { return mCentery - mHeight / 2; }
	public void setMTop(double v) 
	{ 
		mCentery = v + mHeight / 2;
		fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
	}
	
	// startx for shapes
	public double getMLeft() { return mCenterx - mWidth / 2; }
	public void setMLeft(double v) 
	{ 
		mCenterx = v + mWidth / 2;
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
	
	protected double mFontSize = 1;	
	public double getMFontSize() { return mFontSize; }
	public void setMFontSize(double v) 
	{ 
		if (mFontSize != v)
		{
			mFontSize = v;
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
	
	// TODO: rename to DrawingWidth/height
	protected double mBoardWidth;
	public double getMBoardWidth() { return mBoardWidth; }
	public void setMBoardWidth(double v) 
	{ 
		if (mBoardWidth != v)
		{
			mBoardWidth = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.WINDOW));
		}
	}

	protected double mBoardHeight;
	public double getMBoardHeight() { return mBoardHeight; }
	public void setMBoardHeight(double v) 
	{ 
		if (mBoardHeight != v)
		{
			mBoardHeight = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.WINDOW));
		}
	}

	protected double windowWidth;

	/**
	 * GenMAPP Legacy attribute
	 * maintained only for reverse compatibility reasons, 
	 * no longer used by PathVisio	
	 */
	public double getWindowWidth() { return windowWidth; }

	/**
	 * GenMAPP Legacy attribute
	 * maintained only for reverse compatibility reasons, 
	 * no longer used by PathVisio	
	 */
	public void setWindowWidth(double v) 
	{ 
		if (windowWidth != v)
		{
			windowWidth = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.WINDOW));
		}
	}

	protected double windowHeight;

	/**
	 * GenMAPP Legacy attribute
	 * maintained only for reverse compatibility reasons, 
	 * no longer used by PathVisio	
	 */
	public double getWindowHeight() { return windowHeight; }
	
	/**
	 * GenMAPP Legacy attribute
	 * maintained only for reverse compatibility reasons, 
	 * no longer used by PathVisio	
	 */
	public void setWindowHeight(double v) 
	{ 
		if (windowHeight != v)
		{
			windowHeight = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.WINDOW));
		}
	}
	
	protected int mapInfoLeft;
	
	/**
	 * @deprecated use regular mLeft and mTop
	 */
	public int getMapInfoLeft() { return mapInfoLeft; }
	
	/**
	 * @deprecated use regular mLeft and mTop
	 */
	public void setMapInfoLeft(int v) 
	{ 
		if (mapInfoLeft != v)
		{
			mapInfoLeft = v;
			fireObjectModifiedEvent(new GmmlEvent (this, GmmlEvent.MODIFIED_GENERAL));
		}
	}
	protected int mapInfoTop;
	
	/**
	 * @deprecated 
	 */
	public int getMapInfoTop() { return mapInfoTop; }
	
	/**
	 * @deprecated 
	 */
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
	/**
	 * Set graphId. This id must be any string unique within the GmmlData object 
	 * 
	 * @see GmmlData#getUniqueId()
	 */
	public void setGraphId (String v) 
	{ 
		if (graphId == null || !graphId.equals(v))
		{
			if (parent != null)
			{
				if (v != null)
				{
					parent.removeGraphId(v);
				}
				parent.addGraphId(v);
			}
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
	 * @param v reference to set.
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
	 * @see #setStartGraphRef(String)
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
	
	/**
	 * This sets the object to a suitable default size.
	 * 
	 * This method is intended to be called right after the object is
	 * placed on the drawing with a click. 
	 */
	public void setInitialSize()
	{

		switch (objectType)
		{
			case ObjectType.SHAPE:
			case ObjectType.FIXEDSHAPE:
			case ObjectType.COMPLEXSHAPE:
				setMWidth(M_INITIAL_SHAPE_SIZE);
				setMHeight(M_INITIAL_SHAPE_SIZE);
				break;
			case ObjectType.BRACE:
				setMWidth(M_INITIAL_BRACE_WIDTH);
				setMHeight(M_INITIAL_BRACE_HEIGHT);
				break;
			case ObjectType.GENEPRODUCT:
				setMWidth(M_INITIAL_GENEPRODUCT_WIDTH);
				setMHeight(M_INITIAL_GENEPRODUCT_HEIGHT);
				break;
			case ObjectType.LINE:
				setMEndX(getMStartX() + M_INITIAL_SHAPE_SIZE);
				setMEndY(getMStartY() + M_INITIAL_SHAPE_SIZE);
				break;
		}
	}

	/**
	 * Gets a list of all lines that refer to this object with their startPoints
	 */
	public Set<GmmlDataObject> getStickyStarts()
	{
		Set<GmmlDataObject> result = 
			new HashSet<GmmlDataObject>();

		if (parent == null) return result;
		
		List<GmmlDataObject> reflist = parent.getReferringObjects(graphId);
		
		if (reflist != null && !graphId.equals("")) 
		{
			// get all referring lines as a hashset, so
			// that a line that refers to the same object twice
			// is only treated once.
			for (GmmlDataObject o : reflist)
			{
				if (o.getObjectType() == ObjectType.LINE)
				{
					String startRef = o.getStartGraphRef();
					if (startRef != null && startRef.equals (graphId))
					{
						result.add(o);
					}
				}
			}
		}
		return result;	
	}
	
	/**
	 * Gets a list of all lines that refer to this object with their endPoints
	 */
	public Set<GmmlDataObject> getStickyEnds()
	{		
		Set<GmmlDataObject> result = 
			new HashSet<GmmlDataObject>();

		if (parent == null) return result;
		
		List<GmmlDataObject> reflist = parent.getReferringObjects(graphId);
		
		if (reflist != null && !graphId.equals("")) 
		{
			// get all referring lines as a hashset, so
			// that a line that refers to the same object twice
			// is only treated once.
			for (GmmlDataObject o : reflist)
			{
				if (o.getObjectType() == ObjectType.LINE)
				{
					String endRef = o.getEndGraphRef();
					if (endRef != null && o.getEndGraphRef().equals (graphId))
					{
						result.add(o);
					}
				}
			}
		}
		return result;
	}
}
