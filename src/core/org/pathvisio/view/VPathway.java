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
package org.pathvisio.view;

import org.pathvisio.gui.Engine;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationManager.VisualizationEvent;
import org.pathvisio.visualization.VisualizationManager.VisualizationListener;
import org.pathvisio.model.*;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * This class implements and handles a drawing.
 * Graphics objects are stored in the drawing and can be 
 * visualized. The class also provides methods for mouse  and key
 * event handling.
 */
public class VPathway extends Canvas implements MouseListener, MouseMoveListener, 
PaintListener, MouseTrackListener, KeyListener, PathwayListener, VisualizationListener
{	
	private static final long serialVersionUID = 1L;
	static final double M_PASTE_OFFSET = 10 * 15;
	
	/**
	 * All objects that are visible on this mapp, including the handles
	 * but excluding the legend, mappInfo and selectionBox objects
	 */
	private ArrayList<VPathwayElement> drawingObjects;
	public ArrayList<VPathwayElement> getDrawingObjects() { return drawingObjects; }
	
	/**
	 * The {@link VPathwayElement} that is pressed last mouseDown event}
	 */
	VPathwayElement pressedObject	= null;	
	
	/**
	 * The {@link Graphics} that is directly selected since last mouseDown event
	 */
	public Graphics selectedGraphics = null;
	
	/**
	 * {@link InfoBox} object that contains information about this pathway,
	 * currently only used for information in {@link gmmlVision.PropertyPanel}
	 * (TODO: has to be implemented to behave the same as any Graphics object
	 * when displayed on the drawing)
	 */
	InfoBox infoBox;
	private Pathway data;
	public Pathway getGmmlData()
	{
		return data;
	}
	
	SelectionBox s; 
		
	private boolean editMode;
	/**
	 * Checks if this drawing is in edit mode
	 * @return false if in edit mode, true if not
	 */
	public boolean isEditMode() { return editMode; }
	
	/**
	 * Map the contents of a single data object to this VPathway
	 */	
	private Graphics fromGmmlDataObject (PathwayElement o)
	{
		Graphics result = null;
		switch (o.getObjectType())
		{
			case ObjectType.DATANODE: result = new GeneProduct(this, o); break;
			case ObjectType.SHAPE: result = new Shape(this, o); break;
			case ObjectType.LINE: result = new Line(this, o); break;
			case ObjectType.MAPPINFO: 
				InfoBox mi = new InfoBox(this, o);
				addObject(mi); 
				setMappInfo(mi);
				result = mi; 
				break;				
			case ObjectType.LABEL: result = new Label(this, o); break;					
		}
		return result;
	}
	
	/**
	 * Maps the contents of a pathway to this VPathway
	 */	
	public void fromGmmlData(Pathway _data)
	{		
		data = _data;
			
		for (PathwayElement o : data.getDataObjects())
		{
			fromGmmlDataObject (o);
		}
		int width = (int)vFromM(infoBox.getGmmlData().getMBoardWidth());
		int height = (int)vFromM(infoBox.getGmmlData().getMBoardHeight());
		setSize(width, height); 
		data.fireObjectModifiedEvent(new PathwayEvent(null, PathwayEvent.MODIFIED_GENERAL));
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
	public VPathway(Composite parent, int style)
	{
		super (parent, style);
		
		drawingObjects	= new ArrayList<VPathwayElement>();
		
		s = new SelectionBox(this);
		
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
	public void setMappInfo(InfoBox mappInfo)
	{
		this.infoBox = mappInfo;
		infoBox.getGmmlData().addListener(this);
	}

	/**
	 * Gets the MappInfo containing information on the pathway
	 */
	public InfoBox getMappInfo() { return infoBox; }
		
	/**
	 * Adds an element to the drawing
	 * @param o the element to add
	 */
	public void addObject(VPathwayElement o)
	{
		if(!drawingObjects.contains(o)) { //Don't add duplicates!
			drawingObjects.add(o);
		}
		
	}

	HashMap<MPoint, VPoint> pointsMtoV = new HashMap<MPoint, VPoint>();
	protected VPoint getPoint(MPoint mPoint) {
		VPoint p = pointsMtoV.get(mPoint);
		if(p == null) {
			p = newPoint(mPoint);
		}
		return p;
	}
	
	
	private VPoint newPoint(MPoint mPoint) {
		VPoint p = null;
		for(MPoint ep : mPoint.getEqualPoints()) {
			p = pointsMtoV.get(ep);
			if(p != null) {
				p.addMPoint(mPoint);
				pointsMtoV.put(mPoint, p);
				break;
			}
		}
		if(p == null) p = new VPoint(this);
		p.addMPoint(mPoint);
		pointsMtoV.put(mPoint, p);
		return p;
	}
	
	/**
	 * Get the gene identifiers of all genes in this pathway
	 * @return	List containing an identifier for every gene on the mapp
	 * @deprecated get this info from Pathway directly
	 */
	public ArrayList<String> getMappIds()
	{
		ArrayList<String> mappIds = new ArrayList<String>();
		for(VPathwayElement o : drawingObjects)
		{
			if(o instanceof GeneProduct)
			{
				mappIds.add(((GeneProduct)o).getID());
			}
		}
		return mappIds;
	}
	
	/**
	 * Get the systemcodes of all genes in this pathway
	 * @return	List containing a systemcode for every gene on the mapp
	 * 
	 * @deprecated get this info from Pathway directly
	 */
	public ArrayList<String> getSystemCodes()
	{
		ArrayList<String> systemCodes = new ArrayList<String>();
		for(VPathwayElement o : drawingObjects)
		{
			if(o instanceof GeneProduct)
			{
				systemCodes.add(((GeneProduct)o).getSystemCode());
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
		Engine.getWindow().showLegend(!editMode);	
		redraw();
	}
	
	private double zoomFactor = 1.0/15.0;
	/**
	 * Get the current zoomfactor used. 
	 * 1/15 means 100%, 15 gpml unit = 1 pixel
	 * 2/15 means 200%, 7.5 gpml unit = 1 pixel
	 * 
	 * The 15/1 ratio is there because of 
	 * the Visual Basic legacy of GenMAPP
	 * 
	 * To distinguish between model coordinates and view coordinates,
	 * we prefix all coordinates with either v or m (or V or M). For example:
	 * 
	 * mTop = gdata.getMTop();
	 * vTop = GeneProduct.getVTop();
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
	 * same as getZoomFactor, but in %
	 * @return
	 */
	public double getPctZoom() { return zoomFactor * 100 * 15.0; }

	/**
	 * Sets the drawings zoom in percent
	 * @param pctZoomFactor zoomfactor in percent
	 */
	public void setPctZoom(double pctZoomFactor)
	{
		zoomFactor = pctZoomFactor / 100.0 / 15.0;
		int width = (int)vFromM(infoBox.getGmmlData().getMBoardWidth());
		int height = (int)vFromM(infoBox.getGmmlData().getMBoardHeight());
		setSize(width, height); 				
		redraw();
	}

	public void setPressedObject(VPathwayElement o) {
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
		boolean altPressed = (ve.stateMask & SWT.ALT) != 0;
		// If draggin, drag the pressed object
		if (pressedObject != null && isDragging)
		{
			double vdx = ve.x - vPreviousX;
			double vdy = ve.y - vPreviousY;
			pressedObject.vMoveBy(vdx, vdy);
				
			vPreviousX = ve.x;
			vPreviousY = ve.y;
			
			if (pressedObject instanceof Handle && altPressed && newGraphics == NEWNONE &&
					((Handle)pressedObject).parent instanceof VPoint)
			{
				resetHighlight();
				Point2D p2d = new Point2D.Double(ve.x, ve.y);
				List<VPathwayElement> objects = getObjectsAt (p2d);
				Collections.sort(objects);
				Handle g = (Handle)pressedObject;
				VPoint p = (VPoint)g.parent;
				VPathwayElement x = null;
				for (VPathwayElement o : objects)
				{
					if (o instanceof VPoint && o != p) {
						x = o;
						p.link((VPoint)o);
						break;
					} else if(o instanceof Graphics && !(o instanceof Line)) {
						x = o;
						p.link((Graphics)o);
						break;
					} 
				}
				if(x != null) x.highlight();
			}
			redrawDirtyRect();
		}
	}
	
	public void selectObject(VPathwayElement o) {
		clearSelection();
		lastAdded.select();
		s.addToSelection(lastAdded);
	}
	
	/**
	 * Handles mouse Pressed input
	 */
	public void mouseDown(MouseEvent e)
	{		
		setFocus();
		if (editMode)
		{
			if (newGraphics != NEWNONE)
			{
				newObject(new Point(e.x, e.y));
				Engine.getWindow().deselectNewItemActions();
			}
			else
			{
				editObject(new Point(e.x, e.y), e);
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
			resetHighlight();
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
			Engine.log.trace("Creating image of size " + getSize().x + ", " + getSize().y);
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
		for(VPathwayElement o : drawingObjects)
		{
			if(o.vIntersects(r))
			{
				if(checkDrawAllowed(o)) {
					o.draw (e, buffer);
				}
				
				if(v != null && o instanceof Graphics) {
						try {
							v.visualizeDrawing((Graphics) o, e, buffer);
						} catch(Exception ex) {
							Engine.log.error(
									"Unable to apply visualization " + v + " on " + o, ex);
							ex.printStackTrace();
						}
				}
				if(o instanceof GeneProduct) ((GeneProduct)o).drawHighlight(e, buffer);
			}
		}
		
		e.gc.drawImage(image, 0, 0);
		buffer.dispose();
	}

	boolean checkDrawAllowed(VPathwayElement o) {
		if(isEditMode()) return true;
		else return !(	o instanceof Handle ||
						(o == s && !isDragging)
					);
	}

	/**
	 * deselect all elements on the drawing
	 */
	private void clearSelection()
	{
		for(VPathwayElement o : drawingObjects) o.deselect(); //Deselect all objects
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
			doClickSelect(p2d, e);
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
		for(VPathwayElement o : drawingObjects) o.unhighlight();
		redraw();
	}
	
	/**
	 * Called by MouseDown, when we're in editting mode and we're not adding new objects
	 * prepares for dragging the object
	 */
	private void editObject(Point p, MouseEvent e)
	{
		Point2D p2d = new Point2D.Double(p.x, p.y);
		
		pressedObject = getObjectAt(p2d);
		
		// if we clicked on an object
		if (pressedObject != null)
		{
			// if our object is an handle, select also it's parent.
			if(pressedObject instanceof Handle)
			{
				((Handle)pressedObject).parent.select();
			} else {
				doClickSelect(p2d, e);
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
	VPathwayElement getObjectAt(Point2D p2d) {
		Collections.sort(drawingObjects);
		VPathwayElement probj = null;
		for (VPathwayElement o : drawingObjects)
		{
			if (o.vContains(p2d))
			{
				// select this object, unless it is an invisible gmmlHandle
				if (o instanceof Handle && !((Handle)o).isVisible()) 
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
	List<VPathwayElement> getObjectsAt(Point2D p2d) 
	{
		List<VPathwayElement> result = new ArrayList<VPathwayElement>();
		for (VPathwayElement o : drawingObjects)
		{
			if (o.vContains(p2d))
			{
				// select this object, unless it is an invisible gmmlHandle
				if (o instanceof Handle && !((Handle)o).isVisible()) 
					;
				else 
					result.add(o);
			}
		}
		return result;
	}
	
	void doClickSelect(Point2D p2d, MouseEvent e) {
		//Ctrl pressed, add/remove from selection
		boolean ctrlPressed =  (e.stateMask & SWT.CTRL) != 0;
		if(ctrlPressed) 
		{
			if(pressedObject instanceof SelectionBox) {
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
			if(!(pressedObject instanceof SelectionBox))
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
	public static final Color stdRGB = new Color(0, 0, 0);

	/**
	 * pathvisio distinguishes between placing objects with a click
	 * or with a drag. If you don't move the cursor in between the mousedown
	 * and mouseup event, the object is placed with a default initial size.
	 * 
	 * newObjectDragStart is used to determine the mousemovement during the click.
	 */
	private Point newObjectDragStart;
	
	/** newly placed object, is set to null again when mouse button is released */
	private PathwayElement newObject = null;
	/** minimum drag length for it to be considered a drag and not a click */
	private static final int MIN_DRAG_LENGTH = 3;

	/**
	 * Add a new object to the drawing
	 * {@see VPathway#setNewGraphics(int)}
	 * @param p	The point where the user clicked on the drawing to add a new graphics
	 */
	private void newObject(Point ve)
	{
		newObjectDragStart = ve;
		int mx = (int)mFromV((double)ve.x);
		int my = (int)mFromV((double)ve.y); 
		
		PathwayElement gdata = null;
		Handle h = null;
		lastAdded = null; // reset lastAdded class member
		switch(newGraphics) {
		case NEWNONE:
			return;
		case NEWLINE:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.LINE);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		case NEWLINEARROW:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.ARROW);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		case NEWLINEDASHED:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.DASHED);
			gdata.setLineType (LineType.LINE);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		case NEWLINEDASHEDARROW:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.DASHED);
			gdata.setLineType (LineType.ARROW);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		case NEWLABEL:
			gdata = new PathwayElement(ObjectType.LABEL);
			gdata.setMCenterX(mx);
			gdata.setMCenterY(my);
			gdata.setMWidth(Label.M_INITIAL_WIDTH);
			gdata.setMHeight(Label.M_INITIAL_HEIGHT);
			gdata.setMFontSize (Label.M_INITIAL_FONTSIZE);
			gdata.setGraphId(data.getUniqueId());
			data.add (gdata); // will cause lastAdded to be set
			((Label)lastAdded).createTextControl();
			h = null;
			break;
		case NEWARC:
			gdata = new PathwayElement(ObjectType.SHAPE);
			gdata.setShapeType(ShapeType.ARC);
			gdata.setMCenterX (mx);
			gdata.setMCenterY (my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setColor(stdRGB);
			gdata.setRotation (0);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Shape)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWBRACE:
			gdata = new PathwayElement(ObjectType.SHAPE);
			gdata.setShapeType(ShapeType.BRACE);
			gdata.setMCenterX (mx);
			gdata.setMCenterY (my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setOrientation(OrientationType.RIGHT);
			gdata.setColor(stdRGB);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Shape)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWGENEPRODUCT:
			gdata = new PathwayElement(ObjectType.DATANODE);
			gdata.setMCenterX(mx);
			gdata.setMCenterY(my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setTextLabel("Gene");
			gdata.setXref("");
			gdata.setColor(stdRGB);
			gdata.setGraphId(data.getUniqueId());
			data.add (gdata); // will cause lastAdded to be set
			h = ((GeneProduct)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWRECTANGLE:
			gdata = new PathwayElement(ObjectType.SHAPE);
			gdata.setShapeType(ShapeType.RECTANGLE);
			gdata.setMCenterX (mx);
			gdata.setMCenterY (my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setColor(stdRGB);
			gdata.setRotation (0);
			gdata.setGraphId(data.getUniqueId());
			data.add (gdata); // will cause lastAdded to be set
			h = ((Shape)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWOVAL:
			gdata = new PathwayElement(ObjectType.SHAPE);
			gdata.setShapeType(ShapeType.OVAL);
			gdata.setMCenterX (mx);
			gdata.setMCenterY (my);
			gdata.setMWidth(1);
			gdata.setMHeight(1);
			gdata.setColor(stdRGB);
			gdata.setRotation (0);
			gdata.setGraphId(data.getUniqueId());
			data.add (gdata); // will cause lastAdded to be set
			h = ((Shape)lastAdded).handleSE;
			isDragging = true;
			break;
		case NEWTBAR:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.TBAR);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		case NEWRECEPTORROUND:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.RECEPTOR_ROUND);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		case NEWRECEPTORSQUARE:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.RECEPTOR_SQUARE);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		case NEWLIGANDROUND:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.LIGAND_ROUND);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		case NEWLIGANDSQUARE:
			gdata = new PathwayElement(ObjectType.LINE);
			gdata.setMStartX(mx);
			gdata.setMStartY(my);
			gdata.setMEndX(mx);
			gdata.setMEndY(my);	
			gdata.setColor (stdRGB);
			gdata.setLineStyle (LineStyle.SOLID);
			gdata.setLineType (LineType.LIGAND_SQUARE);
			data.add (gdata); // will cause lastAdded to be set
			h = ((Line)lastAdded).getEnd().getHandle();
			isDragging = true;
			break;
		}
		
		newObject = gdata;
		selectObject(lastAdded);
		pressedObject = h;
		
		vPreviousX = ve.x;
		vPreviousY = ve.y;
				
	}
	

	public static final int DRAW_ORDER_HANDLE = 0;
	public static final int DRAW_ORDER_SELECTIONBOX = 1;
	public static final int DRAW_ORDER_SELECTED = 2;
	public static final int DRAW_ORDER_GENEPRODUCT = 3;
	public static final int DRAW_ORDER_LABEL = 4;
	public static final int DRAW_ORDER_ARC = 5;
	public static final int DRAW_ORDER_BRACE = 6;
	public static final int DRAW_ORDER_SHAPE = 7;
	public static final int DRAW_ORDER_LINE = 8;
	public static final int DRAW_ORDER_LINESHAPE = 9;
	public static final int DRAW_ORDER_MAPPINFO = 10;
	public static final int DRAW_ORDER_DEFAULT = 11;
	
	public void mouseEnter(MouseEvent e) {}

	public void mouseExit(MouseEvent e) {}
	
	/**
	 * Responsible for drawing a tooltip displaying expression data when 
	 * hovering over a geneproduct
	 */
	public void mouseHover(MouseEvent e) {
		Visualization v = VisualizationManager.getCurrent();
		if(v != null && v.usesToolTip()) {
			Point2D p = new Point2D.Double(e.x, e.y);
			
			VPathwayElement o = getObjectAt(p);
			if(o != null && o instanceof Graphics) {
				Shell tip = v.visualizeToolTip(getShell(), this, (Graphics)o);
				if(tip == null) return;
				Point mp = toDisplay(e.x + 15, e.y + 15);
				tip.setLocation(mp.x, mp.y);
	            tip.setVisible(true);
			}
		}
	}

	private void selectGeneProducts() {
		clearSelection();
		for(VPathwayElement o : getDrawingObjects()) {
			if(o instanceof GeneProduct) s.addToSelection(o);
		}
	}
	
	private void insertPressed() {
		Set<VPathwayElement> objects = new HashSet<VPathwayElement>();
		objects.addAll(s.getSelection());
		for(VPathwayElement o : objects) {
			if(o instanceof Line) {
				PathwayElement g = ((Line)o).getGmmlData();
				PathwayElement[] gNew = g.splitLine();
							
				removeDrawingObject(o); //Remove the old line
				
				//Clear refs on middle point (which is new)
				gNew[0].getMEnd().setGraphRef(null);
				gNew[1].getMStart().setGraphRef(null);
				
				gNew[1].setGraphId(data.getUniqueId());
				data.add(gNew[0]);
				Line l1 = (Line)lastAdded;
				data.add(gNew[1]);
				Line l2 = (Line)lastAdded;				
				
				l1.getEnd().link(l2.getStart());
			}
		}
		s.addToSelection(lastAdded);
	}
	
	public void createGroup() {
		//GroupId is created on first getGroupId call
		PathwayElement group = new PathwayElement(ObjectType.GROUP);
		data.add(group);
		
		group.setTextLabel("new group");
		group.setGroupStyle(GroupStyle.NONE);
		
		String id = group.getGroupId();
		
		//Add the selected pathway elements
		List<Graphics> selection = getSelectedGraphics();
		
		for(Graphics g : selection) {
			PathwayElement pe = g.getGmmlData();
			String ref = pe.getGroupRef();
			if(ref == null) {
				pe.setGroupRef(id);
			} else if(ref != id) {
				PathwayElement refGroup = data.getGroupById(ref);
				refGroup.setGroupRef(id);
			}
		}
	}

	public void keyPressed(KeyEvent e) { 
		//if(e.keyCode == SWT.CTRL) ctrlPressed();
		//if(e.keyCode == SWT.ALT) altPressed();
		if(e.keyCode == SWT.INSERT) insertPressed();
		if(e.keyCode == 100) //CTRL-D to select all gene-products
			if((e.stateMask & SWT.CTRL) != 0) {
				selectGeneProducts();
				redraw();
			}
		if(e.keyCode == 103) //CTRL-G to select all gene-products
			if((e.stateMask & SWT.CTRL) != 0) {
				//do group thing
				createGroup();
			}
	}

	
	
	public void keyReleased(KeyEvent e) {		
		//if(e.keyCode == SWT.CTRL) ctrlReleased();
		//if(e.keyCode == SWT.ALT) altReleased();
		if(e.keyCode == SWT.DEL) {
			ArrayList<VPathwayElement> toRemove = new ArrayList<VPathwayElement>();
			for(VPathwayElement o : drawingObjects)
			{
				if(!o.isSelected() || o == s || o == infoBox) continue; //Object not selected, skip
				toRemove.add(o);
			}
			removeDrawingObjects(toRemove);
		}
	}
	
	/**
	 * Removes the GmmlDrawingObjects in the ArrayList from the drawing
	 * @param toRemove	The List containing the objects to be removed
	 */
	public void removeDrawingObjects(ArrayList<VPathwayElement>toRemove)
	{
		for(VPathwayElement o : toRemove)
		{
			removeDrawingObject(o);
			
		}
		s.fitToSelection();
	}
	
	public void removeDrawingObject(VPathwayElement toRemove) {
		toRemove.destroy(); //Object will remove itself from the drawing
		s.removeFromSelection(toRemove); //Remove from selection
	}

	Graphics lastAdded = null;
	
	public void gmmlObjectModified(PathwayEvent e) {
		switch (e.getType())
		{
			case PathwayEvent.DELETED:
				// TODO: affected object should be removed
				addDirtyRect(null); // mark everything dirty
				break;
			case PathwayEvent.ADDED:
				lastAdded = fromGmmlDataObject(e.getAffectedData());
				addDirtyRect(null); // mark everything dirty
				break;
			case PathwayEvent.WINDOW:
				int width = (int)vFromM(infoBox.getGmmlData().getMBoardWidth());
				int height = (int)vFromM(infoBox.getGmmlData().getMBoardHeight());
				setSize(width, height); 
				break;
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
		
		List<PathwayElement> result = new ArrayList<PathwayElement>();
		for (VPathwayElement g : drawingObjects)
		{
			if (g.isSelected() && g instanceof Graphics
					&& !(g instanceof SelectionBox))
			{
				result.add(((Graphics)g).gdata.copy());
			}
		}
		if (result.size() > 0)
		{
			Engine.clipboard = result;
		}
		else
		{
			Engine.clipboard = null;
		}
		
		//clipboard.dispose();
	}
	
	/**
	 * TODO: document
	 * @return
	 */
	public List<Graphics> getSelectedGraphics() {
		List<Graphics> result = new ArrayList<Graphics>();
		for (VPathwayElement g : drawingObjects)
		{
			if (g.isSelected() && g instanceof Graphics
					&& !(g instanceof SelectionBox))
			{
				result.add((Graphics)g);
			}
		}
		return result;
	}
	
	/**
	 * If global clipboard contains GmmlDataObjects,
	 * makes another copy of these objects, and pastes them in. 
	 * The clipboard contents will be moved 10 pixels souteast,
	 * so they won't exactly overlap with the original.
	 */
	public void pasteFromClipboad()
	{
		if (Engine.clipboard != null)
		{
			clearSelection();
			Map<String, String> idmap = new HashMap<String, String>();
			Set<String> newids = new HashSet<String>();
			
			/*
			 * Step 1: generate new unique ids for copied items
			 */
			for (PathwayElement o : Engine.clipboard)
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
			for (PathwayElement o : Engine.clipboard)
			{
				if (o.getObjectType() == ObjectType.MAPPINFO ||
					o.getObjectType() == ObjectType.INFOBOX)
				{
					// these object types we skip,
					// because they have to be unique in a pathway
					continue;
				}
				
				lastAdded = null;
				o.setMStartX(o.getMStartX() + M_PASTE_OFFSET);
				o.setMStartY(o.getMStartY() + M_PASTE_OFFSET);
				o.setMEndX(o.getMEndX() + M_PASTE_OFFSET);
				o.setMEndY(o.getMEndY() + M_PASTE_OFFSET);
				o.setMLeft(o.getMLeft() + M_PASTE_OFFSET);
				o.setMTop(o.getMTop() + M_PASTE_OFFSET);
				// make another copy to preserve clipboard contents for next paste
				PathwayElement p = o.copy();
				
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
			getDisplay().syncExec(new Runnable() {
				public void run() {
					redraw();
				}
			});
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
