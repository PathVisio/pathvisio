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
package graphics;

import gmmlVision.GmmlVision;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import data.*;

import visualization.*;
import visualization.VisualizationManager.*;

/**
 * This class implements and handles a drawing.
 * GmmlGraphics objects are stored in the drawing and can be 
 * visualized. The class also provides methods for mouse  and key
 * event handling.
 */
public class GmmlDrawing extends Canvas implements MouseListener, MouseMoveListener, 
PaintListener, MouseTrackListener, KeyListener, GmmlListener, VisualizationListener
{	
	private static final long serialVersionUID = 1L;
		
	/**
	 * All objects that are visible on this mapp, including the handles
	 * but excluding the legend, mappInfo and selectionBox objects
	 */
	private ArrayList<GmmlDrawingObject> drawingObjects;
	public ArrayList<GmmlDrawingObject> getDrawingObjects() { return drawingObjects; }
	
	/**
	 * The {@link GmmlDrawingObject} that is pressed last mouseDown event}
	 */
	GmmlDrawingObject pressedObject	= null;	
	
	/**
	 * The {@link GmmlGraphics} that is directly selected since last mouseDown event
	 */
	public GmmlGraphics selectedGraphics = null;
	
	/**
	 * {@link GmmlInfoBox} object that contains information about this pathway,
	 * currently only used for information in {@link gmmlVision.GmmlPropertyTable}
	 * (TODO: has to be implemented to behave the same as any GmmlGraphics object
	 * when displayed on the drawing)
	 */
	GmmlInfoBox infoBox;
	private GmmlData data;
	public GmmlData getGmmlData()
	{
		return data;
	}
	
	GmmlSelectionBox s; 
		
	private boolean editMode;
	/**
	 * Checks if this drawing is in edit mode
	 * @return false if in edit mode, true if not
	 */
	public boolean isEditMode() { return editMode; }
	
	/**
	 * Map the contents of a single data object to this GmmlDrawing
	 */	
	private GmmlGraphics fromGmmlDataObject (GmmlDataObject o)
	{
		GmmlGraphics result = null;
		switch (o.getObjectType())
		{
			case ObjectType.BRACE: result = new GmmlBrace(this, o); break;
			case ObjectType.GENEPRODUCT: result = new GmmlGeneProduct(this, o); break;
			case ObjectType.SHAPE: result = new GmmlShape(this, o); break;
			case ObjectType.LINE: result = new GmmlLine(this, o); break;
			case ObjectType.MAPPINFO: 
				GmmlInfoBox mi = new GmmlInfoBox(this, o);
				addObject(mi); 
				setMappInfo(mi);
				result = mi; 
				break;				
			case ObjectType.LABEL: result = new GmmlLabel(this, o); break;					
		}
		return result;
	}
	
	/**
	 * Maps the contents of a pathway to this GmmlDrawing
	 */	
	public void fromGmmlData(GmmlData _data)
	{		
		data = _data;
			
		for (GmmlDataObject o : data.getDataObjects())
		{
			fromGmmlDataObject (o);
		}
		setSize(getMappInfo().getBoardSize());
		data.fireObjectModifiedEvent(new GmmlEvent(null, GmmlEvent.MODIFIED_GENERAL));
		data.addListener(this);
	}

	private int newGraphics = NEWNONE;
	/**
	 * Method to set the new graphics type that has to be added next time the user clicks on the
	 * drawing. 
	 * @param type One of the NEWXX fields of this class, where XX stands for the type of graphics to draw
	 */
	public void setNewGraphics(int type) { newGraphics = type; }
	
	private Rectangle dirtyRect = null;
	/**
	 * Adds object boundaries to the 'dirty rectangle', which marks the area that needs to be redrawn
	 */
	public void addDirtyRect(Rectangle r)
	{
		if(r == null) { //In case r is null, add whole drawing
			org.eclipse.swt.graphics.Rectangle b = getBounds();
			r = new Rectangle(b.x, b.y, b.width, b.height);
		}
		if(dirtyRect == null)
			dirtyRect = r;
		else
			dirtyRect.add(r);	
	}
	
	/**
	 * Redraw parts marked dirty
	 * reset dirty rect afterwards
	 */
	public void redrawDirtyRect()
	{
		if (dirtyRect != null)
			redraw (dirtyRect.x, dirtyRect.y, dirtyRect.width + 1, dirtyRect.height + 1, false);
		dirtyRect = null;
	}
	
	/**
	 *Constructor for this class
	 */	
	public GmmlDrawing(Composite parent, int style)
	{
		super (parent, style);
		
		drawingObjects	= new ArrayList<GmmlDrawingObject>();
		
		s = new GmmlSelectionBox(this);
		
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener (this);
		addMouseTrackListener(this);
		addKeyListener(this);
		VisualizationManager.addListener(this);
	}
		
	/**
	 * Sets the MappInfo containing information on the pathway
	 * @param mappInfo
	 */
	public void setMappInfo(GmmlInfoBox mappInfo)
	{
		this.infoBox = mappInfo;
		infoBox.getGmmlData().addListener(this);
	}

	/**
	 * Gets the MappInfo containing information on the pathway
	 */
	public GmmlInfoBox getMappInfo() { return infoBox; }
		
	/**
	 * Adds an element to the drawing
	 * @param o the element to add
	 */
	public void addObject(GmmlDrawingObject o)
	{
		if(!drawingObjects.contains(o)) { //Don't add duplicates!
			drawingObjects.add(o);
		}
		
	}

	/**
	 * Get the gene identifiers of all genes in this pathway
	 * @return	List containing an identifier for every gene on the mapp
	 * @deprecated get this info from GmmlData directly
	 */
	public ArrayList<String> getMappIds()
	{
		ArrayList<String> mappIds = new ArrayList<String>();
		for(GmmlDrawingObject o : drawingObjects)
		{
			if(o instanceof GmmlGeneProduct)
			{
				mappIds.add(((GmmlGeneProduct)o).getID());
			}
		}
		return mappIds;
	}
	
	/**
	 * Get the systemcodes of all genes in this pathway
	 * @return	List containing a systemcode for every gene on the mapp
	 * 
	 * @deprecated get this info from GmmlData directly
	 */
	public ArrayList<String> getSystemCodes()
	{
		ArrayList<String> systemCodes = new ArrayList<String>();
		for(GmmlDrawingObject o : drawingObjects)
		{
			if(o instanceof GmmlGeneProduct)
			{
				systemCodes.add(((GmmlGeneProduct)o).getSystemCode());
			}
		}
		return systemCodes;
	}
	
	/**
	 * Set this drawing to editmode
	 * @param editMode	true if editmode has to be enabled, false if disabled (view mode)
	 */
	public void setEditMode(boolean editMode)
	{
		this.editMode = editMode;
		if(!editMode)
		{
			clearSelection();
		}
		GmmlVision.getWindow().showLegend(!editMode);	
		redraw();
	}
	
	private double zoomFactor = 1;
	/**
	 * Get the current zoomfactor used. 
	 * 1.0 means 100%, 15 gpml unit = 1 pixel
	 * 2.0 means 200%, 7.5 gpml unit = 1 pixel
	 * 
	 * The 15/1 ratio is there because of 
	 * the Visual Basic legacy of GenMAPP
	 * 
	 * To distinguish between model coordinates and view coordinates,
	 * we prefix all coordinates with either v or m (or V or M). For example:
	 * 
	 * mTop = gdata.getMTop();
	 * vTop = GmmlGeneProduct.getVTop();
	 * 
	 * Calculations done on M's and V's should always match.
	 * The only way to convert is to use the functions
	 * mFromV and vFromM.
	 * 
	 * Correct: mRight = mLeft + mWidth;
	 * Wrong: mLeft += vDx; 
	 * Fixed: mLeft += mFromV(vDx);
	 * 
	 * @return	the current zoomfactor
	 */
	public double getZoomFactor() { return zoomFactor; }
	
	/**
	 * Sets the drawings zoom in percent
	 * @param pctZoomFactor zoomfactor in percent
	 */
	public void setPctZoom(double pctZoomFactor)
	{
		double factor = 0.01*pctZoomFactor/zoomFactor;
		zoomFactor = pctZoomFactor / 100;
		setSize((int)(getSize().x * factor), (int)(getSize().y * factor));
				
		redraw();
	}

	public void setPressedObject(GmmlDrawingObject o) {
		pressedObject = o;
	}
	
	int vPreviousX;
	int vPreviousY;
	boolean isDragging;
	/**
	 * handles mouse movement
	 */
	public void mouseMove(MouseEvent ve)
	{
//		// Dispose the tooltip if shown
//		if(tip != null)
//		{
//			if(!tip.isDisposed()) tip.dispose();
//		}
		// If draggin, drag the pressed object
		if (pressedObject != null && isDragging)
		{
			pressedObject.vMoveBy(ve.x - vPreviousX, ve.y - vPreviousY);
			
			vPreviousX = ve.x;
			vPreviousY = ve.y;
			
			if (pressedObject instanceof GmmlHandle && altPressed &&
					((GmmlHandle)pressedObject).parent instanceof GmmlLine)
			{
				resetHighlight();
				Point2D p2d = new Point2D.Double(ve.x, ve.y);
				List<GmmlDrawingObject> objects = getObjectsAt (p2d);
				Collections.sort(objects);
				GmmlHandle g = (GmmlHandle)pressedObject;
				GmmlLine l = (GmmlLine)g.parent;
				GmmlDrawingObject x = null;
				for (GmmlDrawingObject o : objects)
				{
					if (o instanceof GmmlGraphicsShape && o != l)
					{
						x = o;
						l.link(g, (GmmlGraphicsShape)o);
					}
				}
				if(x != null) x.highlight();
				
			}
			redrawDirtyRect();
		}
	}
	/**
	 * Handles mouse Pressed input
	 */
	//TODO: Variable ctrlPressed is not set to false when editting labels.
	//		Probably the keyRelease event is not picked up.
	public void mouseDown(MouseEvent e)
	{		
		setFocus();
		if (editMode)
		{
			if (newGraphics != NEWNONE)
			{
				newObject(new Point(e.x, e.y));
				GmmlVision.getWindow().deselectNewItemActions();
			}
			else
			{
				editObject(new Point(e.x, e.y));
			}
		}
		else
		{
			mouseDownViewMode(e);
		}

	}
		
	/**
	 * Handles mouse Released input
	 */
	public void mouseUp(MouseEvent e)
	{
		if(isDragging)
		{
			if(s.isSelecting()) { //If we were selecting, stop it
				s.stopSelecting();
			}
			// check if we placed a new object by clicking or dragging
			// if it was a click, give object the initial size.
			else if (newObject != null && 
					Math.abs(newObjectDragStart.x - e.x) <= MIN_DRAG_LENGTH &&
					Math.abs(newObjectDragStart.y - e.y) <= MIN_DRAG_LENGTH)
			{
				newObject.setInitialSize();
			}
			newObject = null;
			redrawDirtyRect();
		}
		isDragging = false;
	}
	
	/**
	 * Handles mouse entered input
	 */
	public void mouseDoubleClick(MouseEvent e) {	}

	/**
	 * Paints all components in the drawing.
	 * This method is called automatically in the 
	 * painting process
	 */
	public void paintControl (PaintEvent e)
	{		
		Image image = (Image)getData("double-buffer-image");
		// create an image for double-buffering, if it doesn't exist 
		// or the component has been resized
		if(image == null
				|| image.getBounds().width != getSize().x
				|| image.getBounds().height != getSize().y)
		{
			image = new Image(getDisplay(), getSize().x, getSize().y);
			setData("double-buffer-image", image);
		}

		GC buffer = new GC(image);
		buffer.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
		buffer.fillRectangle(e.x, e.y, e.width, e.height);
		
		buffer.setAntialias(SWT.ON);
		
		Rectangle2D.Double r = new Rectangle.Double(e.x, e.y, e.width, e.height);
		    	
		Collections.sort(drawingObjects);
		
		Visualization v = VisualizationManager.getCurrent();
		for(GmmlDrawingObject o : drawingObjects)
		{
			if(o.vIntersects(r))
			{
				if(checkDrawAllowed(o)) {
					o.draw (e, buffer);
				}
				
				if(v != null && o instanceof GmmlGraphics) {
						try {
							v.visualizeDrawing((GmmlGraphics) o, e, buffer);
						} catch(Exception ex) {
							GmmlVision.log.error(
									"Unable to apply visualization " + v + " on " + o, ex);
							ex.printStackTrace();
						}
				}
				if(o instanceof GmmlGeneProduct) ((GmmlGeneProduct)o).drawHighlight(e, buffer);
			}
		}
		
		e.gc.drawImage(image, 0, 0);
		buffer.dispose();
	}

	boolean checkDrawAllowed(GmmlDrawingObject o) {
		if(isEditMode()) return true;
		else return !(	o instanceof GmmlHandle ||
						(o == s && !isDragging)
					);
	}

	/**
	 * deselect all elements on the drawing
	 */
	private void clearSelection()
	{
		for(GmmlDrawingObject o : drawingObjects) o.deselect(); //Deselect all objects
		s.reset();
	}

	/**
	 * Handles event when on mouseDown in case the drawing is in view mode
	 * (does nothing yet)
	 * @param e	the mouse event to handle
	 */
	private void mouseDownViewMode(MouseEvent e) 
	{
		Point2D p2d = new Point2D.Double(e.x, e.y);

		pressedObject = getObjectAt(p2d);
		
		if (pressedObject != null)
			doClickSelect(p2d);
		else
			startSelecting(p2d);
	}
	
	/**
	 * Initializes selection, resetting the selectionbox
	 * and then setting it to the position specified
	 * @param vp - the point to start with the selection
	 */
	private void startSelecting(Point2D vp)
	{
		vPreviousX = (int)vp.getX();
		vPreviousY = (int)vp.getY();
		isDragging = true;
		
		clearSelection();
		s.reset(vp.getX(), vp.getY());
		s.startSelecting();
		pressedObject = s.getCornerHandle();
	}
		
	/**
	 * Resets highlighting, unhighlights all GmmlDrawingObjects
	 */
	public void resetHighlight() 
	{
		for(GmmlDrawingObject o : drawingObjects) o.unhighlight();
		redraw();
	}
	
	/**
	 * Called by MouseDown, when we're in editting mode and we're not adding new objects
	 * prepares for dragging the object
	 */
	private void editObject(Point p)
	{
		Point2D p2d = new Point2D.Double(p.x, p.y);
		
		pressedObject = getObjectAt(p2d);
		
		// if we clicked on an object
		if (pressedObject != null)
		{
			// if our object is an handle, select also it's parent.
			if(pressedObject instanceof GmmlHandle)
			{
				((GmmlHandle)pressedObject).parent.select();
			} else {
				doClickSelect(p2d);
			}
			
			// start dragging
			vPreviousX = p.x;
			vPreviousY = p.y;
			
			isDragging = true;		
		}
		else
		{
			// start dragging selectionbox	
			startSelecting(p2d);
		}		
	}

	/**
	 * Find the object at a particular location on the drawing
	 * 
	 * if you want to get more than one @see #getObjectsAt(Point2D)
	 */
	GmmlDrawingObject getObjectAt(Point2D p2d) {
		Collections.sort(drawingObjects);
		GmmlDrawingObject probj = null;
		for (GmmlDrawingObject o : drawingObjects)
		{
			if (o.vContains(p2d))
			{
				// select this object, unless it is an invisible gmmlHandle
				if (o instanceof GmmlHandle && !((GmmlHandle)o).isVisible()) 
					;
				else 
					probj = o;
			}
		}
		return probj;
	}
	
	/**
	 * Find all objects at a particular location on the drawing
	 * 
	 * if you only need the top object, @see #getObjectAt(Point2D)
	 */
	List<GmmlDrawingObject> getObjectsAt(Point2D p2d) 
	{
		List<GmmlDrawingObject> result = new ArrayList<GmmlDrawingObject>();
		for (GmmlDrawingObject o : drawingObjects)
		{
			if (o.vContains(p2d))
			{
				// select this object, unless it is an invisible gmmlHandle
				if (o instanceof GmmlHandle && !((GmmlHandle)o).isVisible()) 
					;
				else 
					result.add(o);
			}
		}
		return result;
	}
	
	void doClickSelect(Point2D p2d) {
		//Ctrl pressed, add/remove from selection
		if(ctrlPressed) 
		{
			if(pressedObject instanceof GmmlSelectionBox) {
				//Object inside selectionbox clicked, pass to selectionbox
				s.objectClicked(p2d);
			}
			else if(pressedObject.isSelected()) { //Already in selection: remove
				s.removeFromSelection(pressedObject);
			} else {
				s.addToSelection(pressedObject); //Not in selection: add
			}
			pressedObject = null; //Disable dragging
		} 
		else //Ctrl not pressed
		{
			//If pressedobject is not selectionbox:
			//Clear current selection and select pressed object
			if(!(pressedObject instanceof GmmlSelectionBox))
			{
				clearSelection();
				s.addToSelection(pressedObject);
			} else { //Check if clicked object inside selectionbox
				if(s.getChild(p2d) == null) clearSelection();
			}
		}
		redrawDirtyRect();
	}
	
	public static final int NEWNONE = -1;
	public static final int NEWLINE = 0;
	public static final int NEWLABEL = 1;
	public static final int NEWARC = 2;
	public static final int NEWBRACE = 3;
	public static final int NEWGENEPRODUCT = 4;
	public static final int NEWLINEDASHED = 5;
	public static final int NEWLINEARROW = 6;
	public static final int NEWLINEDASHEDARROW = 7;
	public static final int NEWRECTANGLE = 8;
	public static final int NEWOVAL = 9;
	public static final int NEWTBAR = 10;
	public static final int NEWRECEPTORROUND = 11;
	public static final int NEWLIGANDROUND = 12;
	public static final int NEWRECEPTORSQUARE = 13;
	public static final int NEWLIGANDSQUARE = 14;
	public static final int NEWLINEMENU = 15;
	public static final int NEWLINESHAPEMENU = 16;
	public static final RGB stdRGB = new RGB(0, 0, 0);

	/**
	 * pathvisio distinguishes between placing objects with a click
	 * or with a drag. If you don't move the cursor in between the mousedown
	 * and mouseup event, the object is placed with a default initial size.
	 * 
	 * newObjectDragStart is used to determine the mousemovement during the click.
	 */
	private Point newObjectDragStart;
	
	/** newly placed object, is set to null again when mouse button is released */
	private GmmlDataObject newObject = null;
	/** minimum drag length for it to be considered a drag and not a click */
	private static final int MIN_DRAG_LENGTH = 3;

	/**
	 * Add a new object to the drawing
	 * {@see GmmlDrawing#setNewGraphics(int)}
	 * @param p	The point where the user clicked on the drawing to add a new graphics
	 */
	private void newObject(Point ve)
	{
		newObjectDragStart = ve;
		int mx = (int)mFromV((double)ve.x);
		int my = (int)mFromV((double)ve.y); 
		
		GmmlDataObject gdata = null;
		GmmlHandle h = null;
		lastAdded = null; // reset lastAdded class member
		switch(newGraphics) {
		case NEWNONE:
			return;
		case NEWLINE:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.LINE);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		case NEWLINEARROW:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.ARROW);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		case NEWLINEDASHED:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.DASHED);
			gdata.setLineType (LineType.LINE);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		case NEWLINEDASHEDARROW:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.DASHED);
			gdata.setLineType (LineType.ARROW);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		case NEWLABEL:
			gdata = new GmmlDataObject(ObjectType.LABEL);
			gdata.setMCenterX(mx);
			gdata.setMCenterY(my);
			gdata.setMWidth(mFromV(GmmlLabel.M_INITIAL_WIDTH));
			gdata.setMHeight(mFromV(GmmlLabel.M_INITIAL_HEIGHT));
			gdata.setMFontSize (mFromV(GmmlLabel.M_INITIAL_FONTSIZE));
			gdata.setGraphId(data.getUniqueId());
			data.add (gdata); // will cause lastAdded to be set
			((GmmlLabel)lastAdded).createTextControl();
			h = null;
			break;
		case NEWARC:
			gdata = new GmmlDataObject(ObjectType.SHAPE);
			gdata.setShapeType(ShapeType.ARC);
			gdata.setMCenterX (mx);
			gdata.setMCenterY (my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setColor(stdRGB);
			gdata.setRotation (0);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlShape)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWBRACE:
			gdata = new GmmlDataObject(ObjectType.BRACE);
			gdata.setMCenterX (mx);
			gdata.setMCenterY (my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setOrientation(OrientationType.RIGHT);
			gdata.setColor(stdRGB);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlBrace)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWGENEPRODUCT:
			gdata = new GmmlDataObject(ObjectType.GENEPRODUCT);
			gdata.setMCenterX(mx);
			gdata.setMCenterY(my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setGeneID("Gene");
			gdata.setXref("");
			gdata.setColor(stdRGB);
			gdata.setGraphId(data.getUniqueId());
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlGeneProduct)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWRECTANGLE:
			gdata = new GmmlDataObject(ObjectType.SHAPE);
			gdata.setShapeType(ShapeType.RECTANGLE);
			gdata.setMCenterX (mx);
			gdata.setMCenterY (my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setColor(stdRGB);
			gdata.setRotation (0);
			gdata.setGraphId(data.getUniqueId());
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlShape)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWOVAL:
			gdata = new GmmlDataObject(ObjectType.SHAPE);
			gdata.setShapeType(ShapeType.OVAL);
			gdata.setMCenterX (mx);
			gdata.setMCenterY (my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setColor(stdRGB);
			gdata.setRotation (0);
			gdata.setGraphId(data.getUniqueId());
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlShape)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWTBAR:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.TBAR);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		case NEWRECEPTORROUND:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.RECEPTOR_ROUND);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		case NEWRECEPTORSQUARE:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.RECEPTOR_SQUARE);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		case NEWLIGANDROUND:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.LIGAND_ROUND);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		case NEWLIGANDSQUARE:
			gdata = new GmmlDataObject(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.LIGAND_SQUARE);
			data.add (gdata); // will cause lastAdded to be set
			h = ((GmmlLine)lastAdded).getHandleEnd();
			isDragging = true;
			break;
		}
		
		newObject = gdata;
		clearSelection();
		lastAdded.select();
		s.addToSelection(lastAdded);
		pressedObject = h;
		
		vPreviousX = ve.x;
		vPreviousY = ve.y;
				
	}
	

	public static final int DRAW_ORDER_HANDLE = 0;
	public static final int DRAW_ORDER_SELECTIONBOX = 1;
	public static final int DRAW_ORDER_SELECTED = 2;
	public static final int DRAW_ORDER_GENEPRODUCT = 3;
	public static final int DRAW_ORDER_LABEL = 4;
	public static final int DRAW_ORDER_SHAPE = 5;
	public static final int DRAW_ORDER_ARC = 6;
	public static final int DRAW_ORDER_BRACE = 7;
	public static final int DRAW_ORDER_LINESHAPE = 8;
	public static final int DRAW_ORDER_LINE = 9;
	public static final int DRAW_ORDER_MAPPINFO = 10;
	public static final int DRAW_ORDER_DEFAULT = 11;
	
	public void mouseEnter(MouseEvent e) {}

	public void mouseExit(MouseEvent e) {}

//	Shell tip;
	
	/**
	 * Responsible for drawing a tooltip displaying expression data when 
	 * hovering over a geneproduct
	 */
	public void mouseHover(MouseEvent e) {
		Visualization v = VisualizationManager.getCurrent();
		if(v != null && v.usesToolTip()) {
			Point2D p = new Point2D.Double(e.x, e.y);
			
			GmmlDrawingObject o = getObjectAt(p);
			if(o != null && o instanceof GmmlGraphics) {
				Shell tip = v.visualizeToolTip(getShell(), this, (GmmlGraphics)o);
				if(tip == null) return;
				Point mp = toDisplay(e.x + 15, e.y + 15);
				tip.setLocation(mp.x, mp.y);
	            tip.setVisible(true);
			}
		}
	}

	private void selectGeneProducts() {
		clearSelection();
		for(GmmlDrawingObject o : getDrawingObjects()) {
			if(o instanceof GmmlGeneProduct) s.addToSelection(o);
		}
	}
	
	private boolean ctrlPressed;
	private void ctrlPressed() 	{ ctrlPressed = true; 	}
	private void ctrlReleased() 	{ ctrlPressed = false; 	}

	private boolean altPressed;
	private void altPressed() 	{ altPressed = true; 	}
	private void altReleased() 	{ 
		resetHighlight();
		altPressed = false; 	
	}

	public void keyPressed(KeyEvent e) { 
		if(e.keyCode == SWT.CTRL) ctrlPressed();
		if(e.keyCode == SWT.ALT) altPressed();
		if(e.keyCode == 103) 
			if(ctrlPressed) {
				selectGeneProducts();
				redraw();
			}
	}

	public void keyReleased(KeyEvent e) {
		ArrayList<GmmlDrawingObject> toRemove = new ArrayList<GmmlDrawingObject>();
		
		if(e.keyCode == SWT.CTRL) ctrlReleased();
		if(e.keyCode == SWT.ALT) altReleased();
		if(e.keyCode == SWT.DEL) {
			for(GmmlDrawingObject o : drawingObjects)
			{
				if(!o.isSelected() || o == s || o == infoBox) continue; //Object not selected, skip
				toRemove.add(o);
				if(o instanceof GmmlGraphics) //Also add handles
				{
					for(GmmlHandle h : ((GmmlGraphics)o).getHandles()) toRemove.add(h);
				}
			}
			removeDrawingObjects(toRemove);
		}
		if(e.keyCode == SWT.HOME) {
			System.out.println("================");
			Collections.sort(drawingObjects);
			for(GmmlDrawingObject o : drawingObjects) {
				System.out.println(o.toString() + "\t" + o.isSelected() + "\t" + o.drawingOrder + "\t");
				if(o instanceof GmmlGraphics) {
					System.out.println("\t is GmmlGraphics\t" + ((GmmlGraphics)o).getGmmlData().getObjectType());
				}
			}
		}
	}
	
	/**
	 * Removes the GmmlDrawingObjects in the ArrayList from the drawing
	 * @param toRemove	The List containing the objects to be removed
	 */
	public void removeDrawingObjects(ArrayList<GmmlDrawingObject>toRemove)
	{
		for(GmmlDrawingObject o : toRemove)
		{
			drawingObjects.remove(o); //Remove from drawing
			s.removeFromSelection(o); //Remove from selection
			if(o instanceof GmmlGraphics) {
				GmmlGraphics g = (GmmlGraphics)o;
				GmmlVision.getGmmlData().remove(g.getGmmlData());
				g.getGmmlData().removeListener(this);
			}
			
		}
		s.fitToSelection();
	}

	GmmlGraphics lastAdded = null;
	
	public void gmmlObjectModified(GmmlEvent e) {
		switch (e.getType())
		{
			case GmmlEvent.DELETED:
				// TODO: affected object should be removed
				addDirtyRect(null); // mark everything dirty
				break;
			case GmmlEvent.ADDED:
				lastAdded = fromGmmlDataObject(e.getAffectedData());
				addDirtyRect(null); // mark everything dirty
				break;
			case GmmlEvent.WINDOW:
				setSize((int)infoBox.getGmmlData().getMBoardWidth(), 
						(int)infoBox.getGmmlData().getMBoardHeight());
		}
		redrawDirtyRect();
	}
		
	/**
	 * Makes a copy of all GmmlDataObjects in current selection,
	 * and puts them in the global clipboard.
	 *
	 */
	public void copyToClipboard()
	{
		//Clipboard clipboard = new Clipboard (this.getDisplay());
		
		List<GmmlDataObject> result = new ArrayList<GmmlDataObject>();
		for (GmmlDrawingObject g : drawingObjects)
		{
			if (g.isSelected() && g instanceof GmmlGraphics
					&& !(g instanceof GmmlSelectionBox))
			{
				result.add(((GmmlGraphics)g).gdata.copy());
			}
		}
		if (result.size() > 0)
		{
			GmmlVision.clipboard = result;
		}
		else
		{
			GmmlVision.clipboard = null;
		}
		
		//clipboard.dispose();
	}
	
	/**
	 * If global clipboard contains GmmlDataObjects,
	 * makes another copy of these objects, and pastes them in. 
	 * The clipboard contents will be moved 10 pixels souteast,
	 * so they won't exactly overlap with the original.
	 */
	public void pasteFromClipboad()
	{
		if (GmmlVision.clipboard != null)
		{
			clearSelection();
			Map<String, String> idmap = new HashMap<String, String>();
			Set<String> newids = new HashSet<String>();
			
			/*
			 * Step 1: generate new unique ids for copied items
			 */
			for (GmmlDataObject o : GmmlVision.clipboard)
			{
				String id = o.getGraphId();
				if (id != null) 
				{
					String x;
					do
					{
						/* generate a unique id.
						 * at the same time, check that it is not 
						 * equal to one of the unique ids
						 * that we generated since the start of this
						 * method
						 */ 
						x = data.getUniqueId();
					} while (newids.contains(x));
					newids.add(x); // make sure we don't generate this one again
					
					idmap.put(id, x);
				}
			}
			/*
			 * Step 2: do the actual copying 
			 */
			for (GmmlDataObject o : GmmlVision.clipboard)
			{
				lastAdded = null;
				o.setMStartX(o.getMStartX() + 10);
				o.setMStartY(o.getMStartY() + 10);
				o.setMEndX(o.getMEndX() + 10);
				o.setMEndY(o.getMEndY() + 10);
				o.setMLeft(o.getMLeft() + 10);
				o.setMTop(o.getMTop() + 10);
				// make another copy to preserve clipboard contents for next paste
				GmmlDataObject p = o.copy();
				
				// set new unique id
				if (p.getGraphId() != null)
				{					
					p.setGraphId(idmap.get(p.getGraphId()));					
				}
				// update graphref
				String y = p.getStartGraphRef(); 
				if (y != null)
				{
					//TODO: mapping graphrefs to newly created id's 
					// doesn't work properly yet
				/*	if (idmap.containsKey(y))
					{
						p.setStartGraphRef(idmap.get(y));
					}
					else
					{*/
						p.setStartGraphRef(null);
					//}				
				}
				y = p.getEndGraphRef(); 
				if (y != null)
				{
				/*	if (idmap.containsKey(y))
					{
						p.setEndGraphRef(idmap.get(y));
					}
					else
					{*/
						p.setEndGraphRef(null);
				//	}				
				}
				
				data.add (p); // causes lastAdded to be set
				lastAdded.select();
				s.addToSelection(lastAdded);
			}
		}
	}

	public void visualizationEvent(VisualizationEvent e) {
		switch(e.type) {
		case(VisualizationEvent.COLORSET_MODIFIED):
		case(VisualizationEvent.VISUALIZATION_SELECTED):
		case(VisualizationEvent.VISUALIZATION_MODIFIED):
		case(VisualizationEvent.PLUGIN_MODIFIED):
			redraw();
		}
	}	
	
	/** 
	 * helper method to convert view coordinates to model coordinates 
	 * */
	public double mFromV(double v) { return v / zoomFactor; }

	/** 
	 * helper method to convert view coordinates to model coordinates 
	 * */
	public double vFromM(double m) { return m * zoomFactor; }
	
} // end of class
