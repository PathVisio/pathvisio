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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.pathvisio.Engine;
import org.pathvisio.model.GroupStyle;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.OrientationType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.PathwayListener;
import org.pathvisio.model.ShapeType;
import org.pathvisio.model.PathwayElement.MPoint;
import org.pathvisio.view.SelectionBox.SelectionListener;

import com.hp.hpl.jena.iri.impl.GroupAction;

/**
 * This class implements and handles a drawing. Graphics objects are stored in
 * the drawing and can be visualized. The class also provides methods for mouse
 * and key event handling.
 */
public class VPathway implements PathwayListener
{	
	private static final long serialVersionUID = 1L;
	static final double M_PASTE_OFFSET = 10 * 15;
	public static final double ZOOM_TO_FIT = -1;
	
	private VPathwayWrapper parent; // may be null
	
	/**
	 * All objects that are visible on this mapp, including the handles but
	 * excluding the legend, mappInfo and selectionBox objects
	 */
	private ArrayList<VPathwayElement> drawingObjects;

	public ArrayList<VPathwayElement> getDrawingObjects()
	{
		return drawingObjects; 
	}
	
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
	 * 
	 * @return false if in edit mode, true if not
	 */
	public boolean isEditMode()
	{
		return editMode;
	}
	
	/**
	 * Constructor for this class.
	 * 
	 * @param parent
	 *            Optional gui-specific wrapper for this VPathway
	 */	
	public VPathway(VPathwayWrapper parent)
	{
		this.parent = parent;
		
		drawingObjects	= new ArrayList<VPathwayElement>();
		
		s = new SelectionBox(this);
		
		registerKeyboardActions();
	}
	
	public void redraw()
	{
		if (parent != null)
			parent.redraw();
	}
	
	public VPathwayWrapper getWrapper()
	{
		return parent;
	}
	
	/**
	 * Map the contents of a single data object to this VPathway
	 */	
	private Graphics fromGmmlDataObject (PathwayElement o)
	{
		Graphics result = null;
		switch (o.getObjectType())
		{
		case ObjectType.DATANODE:
			result = new GeneProduct(this, o);
			break;
		case ObjectType.SHAPE:
			result = new Shape(this, o);
			break;
		case ObjectType.LINE:
			result = new Line(this, o);
			break;
			case ObjectType.MAPPINFO: 
				InfoBox mi = new InfoBox(this, o);
				addObject(mi); 
				setMappInfo(mi);
				result = mi; 
				break;				
		case ObjectType.LABEL:
			result = new Label(this, o);
			break;
		case ObjectType.GROUP:
			result = new Group(this, o);
			break;
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
		int width = getVWidth();
		int height = getVHeight();
		if (parent != null)
		{
			parent.setVSize(width, height);
		}
//		data.fireObjectModifiedEvent(new PathwayEvent(null,
//				PathwayEvent.MODIFIED_GENERAL));
		fireVPathwayEvent(new VPathwayEvent(this, VPathwayEvent.MODEL_LOADED));
		data.addListener(this);
	}

	private int newGraphics = NEWNONE;
	/**
	 * Method to set the new graphics type that has to be added next time the
	 * user clicks on the drawing.
	 * 
	 * @param type
	 *            One of the NEWXX fields of this class, where XX stands for the
	 *            type of graphics to draw
	 */
	public void setNewGraphics(int type)
	{
		newGraphics = type;
	}
	
	private Rectangle dirtyRect = null;
	/**
	 * Adds object boundaries to the 'dirty rectangle', which marks the area
	 * that needs to be redrawn
	 */
	public void addDirtyRect(Rectangle r)
	{
		if (r == null)
		{ // In case r is null, add whole drawing
			if (parent != null)
			{
				r = parent.getVBounds();
		}
		}
		if(dirtyRect == null)
			dirtyRect = r;
		else
			dirtyRect.add(r);	
	}
	
	/**
	 * Redraw parts marked dirty reset dirty rect afterwards
	 */
	public void redrawDirtyRect()
	{
		if (dirtyRect != null && parent != null)
			parent.redraw (dirtyRect);
		dirtyRect = null;
	}
			
	/**
	 * Sets the MappInfo containing information on the pathway
	 * 
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
	public InfoBox getMappInfo()
	{
		return infoBox;
	}
		
	/**
	 * Adds an element to the drawing
	 * 
	 * @param o
	 *            the element to add
	 */
	public void addObject(VPathwayElement o)
	{
		if (!drawingObjects.contains(o))
		{ // Don't add duplicates!
			drawingObjects.add(o);
		}
		
	}

	HashMap<MPoint, VPoint> pointsMtoV = new HashMap<MPoint, VPoint>();

	protected VPoint getPoint(MPoint mPoint)
	{
		VPoint p = pointsMtoV.get(mPoint);
		if (p == null)
		{
			p = newPoint(mPoint);
		}
		return p;
	}
	
	private VPoint newPoint(MPoint mPoint)
	{
		VPoint p = null;
		for (MPoint ep : mPoint.getEqualPoints())
		{
			p = pointsMtoV.get(ep);
			if (p != null)
			{
				p.addMPoint(mPoint);
				pointsMtoV.put(mPoint, p);
				break;
			}
		}
		if (p == null)
			p = new VPoint(this);
		p.addMPoint(mPoint);
		pointsMtoV.put(mPoint, p);
		return p;
	}
	
	/**
	 * Get the gene identifiers of all genes in this pathway
	 * 
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
	 * 
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
	 * 
	 * @param editMode
	 *            true if editmode has to be enabled, false if disabled (view
	 *            mode)
	 */
	public void setEditMode(boolean editMode)
	{
		this.editMode = editMode;
		if(!editMode)
		{
			clearSelection();
		}

		redraw();
		int type = editMode ? VPathwayEvent.EDIT_MODE_ON
				: VPathwayEvent.EDIT_MODE_OFF;
		fireVPathwayEvent(new VPathwayEvent(this, type));
	}
	
	private double zoomFactor = 1.0/15.0;
	/**
	 * Get the current zoomfactor used. 1/15 means 100%, 15 gpml unit = 1 pixel
	 * 2/15 means 200%, 7.5 gpml unit = 1 pixel
	 * 
	 * The 15/1 ratio is there because of the Visual Basic legacy of GenMAPP
	 * 
	 * To distinguish between model coordinates and view coordinates, we prefix
	 * all coordinates with either v or m (or V or M). For example:
	 * 
	 * mTop = gdata.getMTop(); vTop = GeneProduct.getVTop();
	 * 
	 * Calculations done on M's and V's should always match. The only way to
	 * convert is to use the functions mFromV and vFromM.
	 * 
	 * Correct: mRight = mLeft + mWidth; Wrong: mLeft += vDx; Fixed: mLeft +=
	 * mFromV(vDx);
	 * 
	 * @return	the current zoomfactor
	 */
	public double getZoomFactor()
	{
		return zoomFactor;
	}

	/**
	 * same as getZoomFactor, but in %
	 * 
	 * @return
	 */
	public double getPctZoom()
	{
		return zoomFactor * 100 * 15.0;
	}

	/**
	 * Sets the drawings zoom in percent
	 * 
	 * @param pctZoomFactor
	 *            zoomfactor in percent, or ZOOM_TO_FIT to fit the zoomfactor to
	 *            the drawing's viewport
	 */
	public void setPctZoom(double pctZoomFactor)
	{
		if(pctZoomFactor == ZOOM_TO_FIT) 
		{
			Dimension drawingSize = getWrapper().getVSize();
			Dimension viewportSize = getWrapper().getViewportSize();
			pctZoomFactor = (int) Math.min(getPctZoom()
					* (double) viewportSize.width / drawingSize.width,
					getPctZoom() * (double) viewportSize.height
							/ drawingSize.height);
		}
		zoomFactor = pctZoomFactor / 100.0 / 15.0;
		int width = getVWidth();
		int height = getVHeight();
		if (parent != null)
		{
			parent.setVSize(width, height); 				
			redraw();
		}
	}

	public void setPressedObject(VPathwayElement o)
	{
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
		boolean altPressed = ve.isKeyDown(MouseEvent.M_ALT);
		// If draggin, drag the pressed object
		if (pressedObject != null && isDragging)
		{
			double vdx = ve.getX() - vPreviousX;
			double vdy = ve.getY() - vPreviousY;
			pressedObject.vMoveBy(vdx, vdy);
				
			vPreviousX = ve.getX();
			vPreviousY = ve.getY();
			
			if (pressedObject instanceof Handle && altPressed
					&& newGraphics == NEWNONE
					&& ((Handle) pressedObject).parent instanceof VPoint)
			{
				resetHighlight();
				Point2D p2d = new Point2D.Double(ve.getX(), ve.getY());
				List<VPathwayElement> objects = getObjectsAt (p2d);
				Collections.sort(objects);
				Handle g = (Handle)pressedObject;
				VPoint p = (VPoint)g.parent;
				VPathwayElement x = null;
				for (VPathwayElement o : objects)
				{
					if (o instanceof VPoint && o != p)
					{
						x = o;
						p.link((VPoint)o);
						break;
					} else if (o instanceof Graphics && !(o instanceof Line))
					{
						x = o;
						p.link((Graphics)o);
						break;
					} 
				}
				if (x != null)
					x.highlight();
			}
			redrawDirtyRect();
		}
	}
	
	/**
	 * Handles movement of objects with the arrow keys
	 * @param ks
	 */
	public void keyMove(KeyStroke ks)
	{
		int smallIncrement = 2;
		
		List<Graphics> selectedGraphics = getSelectedGraphics();
		if (selectedGraphics.size() >0){
			if (ks.equals(KEY_MOVELEFT)){
				for (Graphics g : selectedGraphics){
				g.vMoveBy(-smallIncrement, 0);
				}
			}
			else if (ks.equals(KEY_MOVERIGHT)){
				for (Graphics g : selectedGraphics){
				g.vMoveBy(smallIncrement, 0);
				}
			}
			else if (ks.equals(KEY_MOVEUP)){
				for (Graphics g : selectedGraphics){
			    g.vMoveBy(0, -smallIncrement);
				}
			}
			else if (ks.equals(KEY_MOVEDOWN)){
				for (Graphics g : selectedGraphics){
				g.vMoveBy(0, smallIncrement);
				}
			}
			redrawDirtyRect();
		}
	}
	public void selectObject(VPathwayElement o)
	{
		clearSelection();
		lastAdded.select();
		s.addToSelection(lastAdded);
	}
	
	/**
	 * Handles mouse Pressed input
	 */
	public void mouseDown(MouseEvent e)
	{		
		//setFocus();
		if (editMode)
		{
			if (newGraphics != NEWNONE)
			{
				newObject(new Point(e.getX(), e.getY()));
				//SwtEngine.getCurrent().getWindow().deselectNewItemActions();
			} else
			{
				editObject(new Point(e.getX(), e.getY()), e);
			}
		} else
		{
			mouseDownViewMode(e);
		}
		if(pressedObject != null) {
			fireVPathwayEvent(new VPathwayEvent(this, pressedObject, e, VPathwayEvent.ELEMENT_CLICKED_DOWN));
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
			if (s.isSelecting())
			{ // If we were selecting, stop it
				s.stopSelecting();
			}
			// check if we placed a new object by clicking or dragging
			// if it was a click, give object the initial size.
			else if (newObject != null
					&& Math.abs(newObjectDragStart.x - e.getX()) <= MIN_DRAG_LENGTH
					&& Math.abs(newObjectDragStart.y - e.getY()) <= MIN_DRAG_LENGTH)
			{
				newObject.setInitialSize();
			}
			newObject = null;
			redrawDirtyRect();
		}
		isDragging = false;
		if(pressedObject != null) {
			fireVPathwayEvent(new VPathwayEvent(this, pressedObject, e, VPathwayEvent.ELEMENT_CLICKED_UP));
		}
	}
	
	/**
	 * Handles mouse entered input
	 */
	public void mouseDoubleClick(MouseEvent e)
	{
		VPathwayElement o = getObjectAt(e.getLocation());
		if (o != null)
		{
			fireVPathwayEvent(new VPathwayEvent(this, o,
					VPathwayEvent.ELEMENT_DOUBLE_CLICKED));
		}
	}

	public void draw (Graphics2D g2d)
	{
		draw (g2d, null, true);
	}

	public void draw (Graphics2D g2d, Rectangle area)
	{
		draw (g2d, area, true);
	}
	
	/**
	 * Paints all components in the drawing. This method is called automatically
	 * in the painting process
	 * 
	 * @param g2d
	 *            Graphics2D object the pathway should be drawn onto
	 * @param area
	 *            area that should be updated, null if you want to update the
	 *            entire pathway
	 * @param erase
	 *            true if the background should be erased
	 */
	public void draw (Graphics2D g2d, Rectangle area, boolean erase)
	{		
		if(area == null)
		{
			area = g2d.getClipBounds();
			if (area == null)
			{
				area = new Rectangle (0, 0, getVWidth(), getVHeight());
			}
		}
		
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (erase)
		{
			g2d.setColor(java.awt.Color.WHITE);
			g2d.fillRect(area.x, area.y, area.width, area.height);
		}
		
		g2d.clip(area);
		g2d.setColor(java.awt.Color.BLACK);
		Collections.sort(drawingObjects);
		
		for(VPathwayElement o : drawingObjects)
		{
			if(o.vIntersects(area))
			{
				if (checkDrawAllowed(o))
				{
					o.draw ((Graphics2D)g2d.create());
					fireVPathwayEvent(new VPathwayEvent(this, o, (Graphics2D)g2d.create(), 
							VPathwayEvent.ELEMENT_DRAWN));
				}
			}
		}
	}

	boolean checkDrawAllowed(VPathwayElement o)
	{
		if (isEditMode())
			return true;
		else
			return !(o instanceof Handle || (o == s && !isDragging));
	}

	/**
	 * deselect all elements on the drawing
	 */
	private void clearSelection()
	{
		for (VPathwayElement o : drawingObjects)
			o.deselect(); // Deselect all objects
		s.reset();
	}

	/**
	 * Handles event when on mouseDown in case the drawing is in view mode (does
	 * nothing yet)
	 * 
	 * @param e
	 *            the mouse event to handle
	 */
	private void mouseDownViewMode(MouseEvent e) 
	{
		Point2D p2d = new Point2D.Double(e.getX(), e.getY());

		pressedObject = getObjectAt(p2d);
		
		if (pressedObject != null)
			doClickSelect(p2d, e);
		else
			startSelecting(p2d);
	}
	
	/**
	 * Initializes selection, resetting the selectionbox and then setting it to
	 * the position specified
	 * 
	 * @param vp -
	 *            the point to start with the selection
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
		for (VPathwayElement o : drawingObjects)
			o.unhighlight();
		redraw();
	}
	
	/**
	 * Called by MouseDown, when we're in editting mode and we're not adding new
	 * objects prepares for dragging the object
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
			} else
			{
				doClickSelect(p2d, e);
			}
			
			// start dragging
			vPreviousX = p.x;
			vPreviousY = p.y;
			
			isDragging = true;		
		} else
		{
			// start dragging selectionbox	
			startSelecting(p2d);
		}		
	}

	/**
	 * Find the object at a particular location on the drawing
	 * 
	 * if you want to get more than one
	 * 
	 * @see #getObjectsAt(Point2D)
	 */
	VPathwayElement getObjectAt(Point2D p2d)
	{
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
	 * if you only need the top object,
	 * 
	 * @see #getObjectAt(Point2D)
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
	
	void doClickSelect(Point2D p2d, MouseEvent e)
	{
		//Ctrl pressed, add/remove from selection
		boolean ctrlPressed =  e.isKeyDown(MouseEvent.M_CTRL);
		if(ctrlPressed) 
		{
			if (pressedObject instanceof SelectionBox)
			{
				//Object inside selectionbox clicked, pass to selectionbox
				s.objectClicked(p2d);
			} else if (pressedObject.isSelected())
			{ // Already in selection:
				// remove
				s.removeFromSelection(pressedObject);
			} else
			{
				s.addToSelection(pressedObject); //Not in selection: add
			}
			pressedObject = null; //Disable dragging
		} else
		// Ctrl not pressed
		{
			//If pressedobject is not selectionbox:
			//Clear current selection and select pressed object
			if(!(pressedObject instanceof SelectionBox))
			{
				clearSelection();
				s.addToSelection(pressedObject);
			} else
			{ // Check if clicked object inside selectionbox
				if (s.getChild(p2d) == null)
					clearSelection();
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
	 * pathvisio distinguishes between placing objects with a click or with a
	 * drag. If you don't move the cursor in between the mousedown and mouseup
	 * event, the object is placed with a default initial size.
	 * 
	 * newObjectDragStart is used to determine the mousemovement during the
	 * click.
	 */
	private Point newObjectDragStart;
	
	/** newly placed object, is set to null again when mouse button is released */
	private PathwayElement newObject = null;
	/** minimum drag length for it to be considered a drag and not a click */
	private static final int MIN_DRAG_LENGTH = 3;

	/**
	 * Add a new object to the drawing {@see VPathway#setNewGraphics(int)}
	 * 
	 * @param p
	 *            The point where the user clicked on the drawing to add a new
	 *            graphics
	 */
	private void newObject(Point ve)
	{
		newObjectDragStart = ve;
		int mx = (int)mFromV((double)ve.x);
		int my = (int)mFromV((double)ve.y); 
		
		PathwayElement gdata = null;
		Handle h = null;
		lastAdded = null; // reset lastAdded class member
		switch (newGraphics)
		{
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
			gdata.setTextLabel("Label");
			data.add (gdata); // will cause lastAdded to be set
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
		
		fireVPathwayEvent(new VPathwayEvent(this, lastAdded,
				VPathwayEvent.ELEMENT_ADDED));
	}
	
	public static final int DRAW_ORDER_HANDLE = -1;

	public static final int DRAW_ORDER_GROUP = 0;
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
	
	public void mouseEnter(MouseEvent e)
	{
	}

	public void mouseExit(MouseEvent e)
	{
	}

	//TODO: fix tooltips
//	/**
//	 * Responsible for drawing a tooltip displaying expression data when 
//	 * hovering over a geneproduct
//	 */
//	public void mouseHover(MouseEvent e)
//	{
//		Visualization v = VisualizationManager.getCurrent().;
//		if (v != null && v.usesToolTip())
//		{
//			Point2D p = new Point2D.Double(e.getX(), e.getY());
//			
//			VPathwayElement o = getObjectAt(p);
//			if (o != null && o instanceof Graphics)
//			{
//				// Shell tip = v.visualizeToolTip(getShell(), this,
//				// (Graphics)o);
////				if(tip == null) return;
////				Point mp = toDisplay(e.x + 15, e.y + 15);
////				tip.setLocation(mp.x, mp.y);
////	            tip.setVisible(true);
//			}
//		}
//	}

	/**
	 * Select all objects of the given class
	 * @param c The class of the objects to be selected
	 */
	void selectObjects(Class c) {
		clearSelection();
		for(VPathwayElement vpe : getDrawingObjects()) {
			if(c.isInstance(vpe)) {
				s.addToSelection(vpe);
			}
		}
		redrawDirtyRect();
	}
	
	/**
	 * Select all gene products (datanodes) on the pathway
	 *@deprecated use {@link #selectObjects(Class)} instead
	 */
	private void selectGeneProducts() {
		selectObjects(GeneProduct.class);
	}
	
	void selectAll()
	{
		clearSelection();
		for (VPathwayElement o : getDrawingObjects())
		{
			s.addToSelection(o);
		}
		redrawDirtyRect();
	}
	
	private void insertPressed()
	{
		Set<VPathwayElement> objects = new HashSet<VPathwayElement>();
		objects.addAll(s.getSelection());
		for (VPathwayElement o : objects)
		{
			if (o instanceof Line)
			{
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
	
	public void toggleGroup(List<Graphics> selection)
	{
		boolean grouped = true;

		// Check Group status of current selection
		String topRef = null;
		for (Graphics g : selection)
		{
			PathwayElement pe = g.getGmmlData();
			String ref = pe.getGroupRef();
			String id = pe.getGroupId(); // use to exclude Group Elements
			// from status check
			if (ref == null && id == null)
			{
				// selection includes an ungrouped element; therefore, currently
				// Ungrouped
				grouped = false;
				break;
			} else if (id == null)
			{
				String checkRef = ref;
				while (checkRef != null)
				{
					// set ref to highest-level, non-null group reference
					ref = checkRef;
					PathwayElement refGroup = data.getGroupById(checkRef);
					checkRef = refGroup.getGroupRef();
				}

				if (topRef == null) // first loop through selection
				{
					topRef = ref;
				}

				if (ref != topRef)
				{
					// selection includes elements in distinct, non-nested
					// groups; therefore, currently Ungrouped
					grouped = false;
					break;
				}

				topRef = ref; // set previous selection reference for next
				// loop
			}
		}

		// Group or Ungroup based on current Group status
		if (grouped && topRef != null)
		{
			// Ungroup all elements asociated with topRef
			for (VPathwayElement vpe : this.getDrawingObjects())
			{
				if (vpe instanceof Graphics)
				{
					PathwayElement pe = ((Graphics) vpe).getGmmlData();
					String ref = pe.getGroupRef();

					// remove all references to highest-level group
					if (ref == topRef) // includes Group Elements
					{
						pe.setGroupRef(null);
					}
				}
			}
			// remove highest-level groupId
			PathwayElement topIdGroup = data.getGroupById(topRef);
			topIdGroup.setGroupId(null);

		} else
		{
		//GroupId is created on first getGroupId call
		PathwayElement group = new PathwayElement(ObjectType.GROUP);
		data.add(group);
		
		group.setTextLabel("new group");
		group.setGroupStyle(GroupStyle.NONE);
		
			String id = group.createGroupId();
		
			for (Graphics g : selection)
			{
			PathwayElement pe = g.getGmmlData(); 
			String ref = pe.getGroupRef();
				if (ref == null)
				{
				pe.setGroupRef(id);
				} else
				{
				PathwayElement refGroup = data.getGroupById(ref);
				refGroup.setGroupRef(id);
			}
		}
	}
	}

	public void keyPressed(KeyEvent e)
	{
		//Use registerKeyboardActions
	}
	
	public static final KeyStroke KEY_SELECT_DATA_NODES = KeyStroke
			.getKeyStroke(java.awt.event.KeyEvent.VK_D,
			java.awt.Event.CTRL_MASK);
		
	public static final KeyStroke KEY_GROUP = KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_G, java.awt.Event.CTRL_MASK);
	
	public static final KeyStroke KEY_SELECT_ALL = KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_A, java.awt.Event.CTRL_MASK);

	public static final KeyStroke KEY_DELETE = KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_DELETE, 0);
	
	public static final KeyStroke KEY_MOVERIGHT = KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_RIGHT, java.awt.Event.CTRL_MASK);
	
	public static final KeyStroke KEY_MOVELEFT = KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_LEFT, java.awt.Event.CTRL_MASK);
	
	public static final KeyStroke KEY_MOVEUP = KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_UP, java.awt.Event.CTRL_MASK);
	
	public static final KeyStroke KEY_MOVEDOWN = KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_DOWN, java.awt.Event.CTRL_MASK);
		
	public ViewActions getViewActions() {
		return viewActions;
	}
	
	/**
	 * Several {@link Action}s related to the view
	 */
	private ViewActions viewActions;
	
	private void registerKeyboardActions()
	{
		viewActions = new ViewActions(this);
		if (parent != null)
		{
			parent.registerKeyboardAction(KEY_SELECT_DATA_NODES, viewActions.selectDataNodes);
			parent.registerKeyboardAction(KEY_GROUP, viewActions.toggleGroup);
			parent.registerKeyboardAction(KEY_SELECT_ALL, viewActions.selectAll);
			parent.registerKeyboardAction(KEY_DELETE, viewActions.delete);
			parent.registerKeyboardAction(KEY_MOVELEFT, new AbstractAction()
					{
						public void actionPerformed(ActionEvent e)
						{
						keyMove(KEY_MOVELEFT);
						}
					});
			parent.registerKeyboardAction(KEY_MOVERIGHT, new AbstractAction()
					{
						public void actionPerformed(ActionEvent e)
						{
						keyMove(KEY_MOVERIGHT);
						}
					});
			parent.registerKeyboardAction(KEY_MOVEUP, new AbstractAction()
					{
						public void actionPerformed(ActionEvent e)
						{
						keyMove(KEY_MOVEUP);
						}
					});
			parent.registerKeyboardAction(KEY_MOVEDOWN, new AbstractAction()
					{
						public void actionPerformed(ActionEvent e)
						{
						keyMove(KEY_MOVEDOWN);
						}
					});
		}
	}

	public void keyReleased(KeyEvent e)
	{
		//use registerKeyboardActions
	}
	
	/**
	 * Removes the GmmlDrawingObjects in the ArrayList from the drawing
	 * 
	 * @param toRemove
	 *            The List containing the objects to be removed
	 */
	public void removeDrawingObjects(ArrayList<VPathwayElement>toRemove)
	{
		for(VPathwayElement o : toRemove)
		{
			removeDrawingObject(o);
			
		}
		s.fitToSelection();
	}
	
	public void removeDrawingObject(VPathwayElement toRemove)
	{
		toRemove.destroy(); //Object will remove itself from the drawing
		s.removeFromSelection(toRemove); //Remove from selection
	}

	Graphics lastAdded = null;
	
	public void gmmlObjectModified(PathwayEvent e)
	{
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
				if (parent != null)
				{
					parent.setVSize(width, height);
				}
				break;
		}
		redrawDirtyRect();
	}
		
	/**
	 * Makes a copy of all GmmlDataObjects in current selection, and puts them
	 * in the global clipboard.
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
			Engine.getCurrent().clipboard = result;
		} else
		{
			Engine.getCurrent().clipboard = null;
		}
		
		//clipboard.dispose();
	}
	
	/**
	 * Aligns selected objects based on user-selected align type
	 * 
	 * @param alignType
	 */
	public void alignSelected(AlignType alignType)
	{
		List<Graphics> selectedGraphics = getSelectedGraphics();
		
		if (selectedGraphics.size() > 0)
		{
			switch (alignType)
			{
			case CENTERX : 
				Collections.sort(selectedGraphics, new YComparator());		   
				for (int i=1; i<selectedGraphics.size(); i++)
				{
					selectedGraphics.get(i).getGmmlData().setMCenterX(
							selectedGraphics.get(i-1).getGmmlData().getMCenterX()						
							);
				}
				break;
			case CENTERY : 
				Collections.sort(selectedGraphics, new XComparator());			
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMCenterY(
							selectedGraphics.get(i-1).getGmmlData().getMCenterY()						
							);
				}
				break;
			case LEFT :
				Collections.sort(selectedGraphics, new YComparator());								
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMLeft(
							selectedGraphics.get(i-1).getGmmlData().getMLeft()						
							);
						}
				break;
			case RIGHT : 
				Collections.sort(selectedGraphics, new YComparator());								
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMLeft(
							selectedGraphics.get(i-1).getGmmlData().getMLeft() +
							selectedGraphics.get(i-1).getGmmlData().getMWidth()	-
							selectedGraphics.get(i).getGmmlData().getMWidth()							
							);
					}	
				break;
			case TOP:
				Collections.sort(selectedGraphics, new XComparator());			
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMTop(
							selectedGraphics.get(i-1).getGmmlData().getMTop()						
							);
				}
				break;
			case BOTTOM:
				Collections.sort(selectedGraphics, new XComparator());			
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMTop(
							selectedGraphics.get(i-1).getGmmlData().getMTop() +
							selectedGraphics.get(i-1).getGmmlData().getMHeight() -
							selectedGraphics.get(i).getGmmlData().getMHeight()						
							);
				}
				break;
			case WIDTH:
			case HEIGHT:
				scaleSelected(alignType);
				break;
					}
			redrawDirtyRect();
				}
				}

	/**
	 * Stacks selected objects based on user-selected stack type
	 * 
	 * @param stackType
	 */
	public void stackSelected(StackType stackType)
	{
		List<Graphics> selectedGraphics = getSelectedGraphics();

		if (selectedGraphics.size() > 0)
		{
			switch (stackType)
			{
			case CENTERX:
				Collections.sort(selectedGraphics, new YComparator());		   
				for (int i=1; i<selectedGraphics.size(); i++)
				{
					selectedGraphics.get(i).getGmmlData().setMCenterX(
							selectedGraphics.get(i-1).getGmmlData().getMCenterX()						
							);
					selectedGraphics.get(i).getGmmlData().setMTop(
							selectedGraphics.get(i-1).getGmmlData().getMTop() +
							selectedGraphics.get(i-1).getGmmlData().getMHeight()						
							);
				}		
				break;
			case CENTERY:
				Collections.sort(selectedGraphics, new XComparator());			
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMCenterY(
							selectedGraphics.get(i-1).getGmmlData().getMCenterY()						
							);
					selectedGraphics.get(i).getGmmlData().setMLeft(
							selectedGraphics.get(i-1).getGmmlData().getMLeft() +
							selectedGraphics.get(i-1).getGmmlData().getMWidth()						
							);
					}
				break;
			case LEFT:
				Collections.sort(selectedGraphics, new YComparator());								
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMLeft(
							selectedGraphics.get(i-1).getGmmlData().getMLeft()						
							);
					selectedGraphics.get(i).getGmmlData().setMTop(
							selectedGraphics.get(i-1).getGmmlData().getMTop() +
							selectedGraphics.get(i-1).getGmmlData().getMHeight()						
							);
				}
				break;
			case RIGHT:
				Collections.sort(selectedGraphics, new YComparator());								
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMLeft(
							selectedGraphics.get(i-1).getGmmlData().getMLeft() +
							selectedGraphics.get(i-1).getGmmlData().getMWidth()	-
							selectedGraphics.get(i).getGmmlData().getMWidth()							
							);
					selectedGraphics.get(i).getGmmlData().setMTop(
							selectedGraphics.get(i-1).getGmmlData().getMTop() +
							selectedGraphics.get(i-1).getGmmlData().getMHeight()						
							);

				}
				break;
			case TOP:
				Collections.sort(selectedGraphics, new XComparator());			
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMTop(
							selectedGraphics.get(i-1).getGmmlData().getMTop()						
							);
					selectedGraphics.get(i).getGmmlData().setMLeft(
							selectedGraphics.get(i-1).getGmmlData().getMLeft() +
							selectedGraphics.get(i-1).getGmmlData().getMWidth()						
							);
			}
				break;
			case BOTTOM:
				Collections.sort(selectedGraphics, new XComparator());			
				for (int i=1; i<selectedGraphics.size(); i++)
				{				
					selectedGraphics.get(i).getGmmlData().setMTop(
							selectedGraphics.get(i-1).getGmmlData().getMTop() +
							selectedGraphics.get(i-1).getGmmlData().getMHeight() -
							selectedGraphics.get(i).getGmmlData().getMHeight()						
							);
					selectedGraphics.get(i).getGmmlData().setMLeft(
							selectedGraphics.get(i-1).getGmmlData().getMLeft() +
							selectedGraphics.get(i-1).getGmmlData().getMWidth()						
							);
				}
				break;
			}
			redrawDirtyRect();
	}
	}
	/**
	 * Scales selected objects either by max width or max height
	 * 
	 * @param alignType
	 */
	public void scaleSelected(AlignType alignType)
	{
		
		List<Graphics> selectedGraphics = getSelectedGraphics();
		double maxW = 0;
		double maxH = 0;
		
		if (selectedGraphics.size() > 0)
		{
			switch (alignType)
			{
			case WIDTH:
				for (Graphics g : selectedGraphics)
				{
					Rectangle2D r = g.getVScaleRectangle();
					double w = Math.abs(r.getWidth());
					if (w > maxW)
					{
						maxW = w;
					}
				}
				for (Graphics g : selectedGraphics)
				{
					Rectangle2D r = g.getVScaleRectangle();
					double oldWidth = r.getWidth();
					if (oldWidth < 0)
					{
						r.setRect(r.getX(), r.getY(), -(maxW), r.getHeight());
						g.setVScaleRectangle(r);
						g.vMoveBy((oldWidth+maxW)/2,0);
					} else
					{
						r.setRect(r.getX(), r.getY(), maxW, r.getHeight());
						g.setVScaleRectangle(r);
						g.vMoveBy((oldWidth - maxW)/2,0);
					}
				}
				break;
			case HEIGHT:
				for (Graphics g : selectedGraphics)
				{
					Rectangle2D r = g.getVScaleRectangle();
					double h = Math.abs(r.getHeight());
					if (h > maxH)
					{
						maxH = h;
					}
				}
				for (Graphics g : selectedGraphics)
				{
					Rectangle2D r = g.getVScaleRectangle();
					double oldHeight = r.getHeight();
					if (oldHeight < 0)
					{
						r.setRect(r.getX(), r.getY(), r.getWidth(), -(maxH));
						g.setVScaleRectangle(r);
						g.vMoveBy(0,(maxH+oldHeight)/2);
					} else
					{
					r.setRect(r.getX(), r.getY(), r.getWidth(), maxH);
					g.setVScaleRectangle(r);
					g.vMoveBy(0,(oldHeight - maxH)/2);
					}
				}
				break;
			}
			redrawDirtyRect();
		}
	}
	
	
	/**
	 * Get all elements of the class Graphics that are currently selected
	 * 
	 * @return
	 */
	public List<Graphics> getSelectedGraphics()
	{
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
	 * If global clipboard contains GmmlDataObjects, makes another copy of these
	 * objects, and pastes them in. The clipboard contents will be moved 10
	 * pixels souteast, so they won't exactly overlap with the original.
	 */
	public void pasteFromClipboad()
	{
		if (Engine.getCurrent().clipboard != null)
		{
			clearSelection();
			Map<String, String> idmap = new HashMap<String, String>();
			Set<String> newids = new HashSet<String>();
			
			/*
			 * Step 1: generate new unique ids for copied items
			 */
			for (PathwayElement o : Engine.getCurrent().clipboard)
			{
				String id = o.getGraphId();
				if (id != null) 
				{
					String x;
					do
					{
						/*
						 * generate a unique id. at the same time, check that it
						 * is not equal to one of the unique ids that we
						 * generated since the start of this method
						 */ 
						x = data.getUniqueId();
					} while (newids.contains(x));
					newids.add(x); // make sure we don't generate this one
					// again
					
					idmap.put(id, x);
				}
			}
			/*
			 * Step 2: do the actual copying 
			 */
			for (PathwayElement o : Engine.getCurrent().clipboard)
			{
				if (o.getObjectType() == ObjectType.MAPPINFO
						|| o.getObjectType() == ObjectType.INFOBOX)
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
				// make another copy to preserve clipboard contents for next
				// paste
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
					/*
					 * if (idmap.containsKey(y)) {
					 * p.setStartGraphRef(idmap.get(y)); } else {
					 */
						p.setStartGraphRef(null);
					//}				
				}
				y = p.getEndGraphRef(); 
				if (y != null)
				{
					/*
					 * if (idmap.containsKey(y)) {
					 * p.setEndGraphRef(idmap.get(y)); } else {
					 */
						p.setEndGraphRef(null);
				//	}				
				}
				
				data.add (p); // causes lastAdded to be set
				lastAdded.select();
				s.addToSelection(lastAdded);
			}
		}
	}

	private List<VPathwayListener> listeners = new ArrayList<VPathwayListener>();
	private List<VPathwayListener> removeListeners = new ArrayList<VPathwayListener>();
	
	public void addVPathwayListener(VPathwayListener l)
	{
		if (!listeners.contains(l))
			listeners.add(l);
	}
	
	public void removeVPathwayListener(VPathwayListener l)
	{
		removeListeners.add(l);
	}
	
	/**
	 * Adds a {@link SelectionListener} to the SelectionBox of this VPathway
	 * @param l The SelectionListener to add
	 */
	public void addSelectionListener(SelectionListener l) {
		s.addListener(l);
	}
	
	/**
	 * Removes a {@link SelectionListener} from the SelectionBox of this VPathway
	 * @param l The SelectionListener to remove
	 */
	public void removeSelectionListener(SelectionListener l) {
		s.removeListener(l);
	}
	
	private void cleanupListeners()
	{
		//Do not remove immediately, to prevent ConcurrentModificationException
		//when the listener removes itself
		listeners.removeAll(removeListeners);
		removeListeners.clear();
	}
	
	protected void fireVPathwayEvent(VPathwayEvent e)
	{
		cleanupListeners();
		for (VPathwayListener l : listeners)
		{
			l.vPathwayEvent(e);
		}
	}
		
	/** 
	 * helper method to convert view coordinates to model coordinates 
	 */
	public double mFromV(double v)
	{
		return v / zoomFactor;
	}

	/** 
	 * helper method to convert view coordinates to model coordinates 
	 */
	public double vFromM(double m)
	{
		return m * zoomFactor;
	}

	/**
	 * Get width of entire Pathway view (taking into account zoom)
	 */
	public int getVWidth()
	{
		return (int)vFromM(infoBox.getGmmlData().getMBoardWidth());
	}
	
	/**
	 * Get height of entire Pathway view (taking into account zoom)
	 */
	public int getVHeight()
	{
		return (int)vFromM(infoBox.getGmmlData().getMBoardHeight());
	}
	
	//AP20070716
	public class YComparator implements Comparator<Graphics> {
		public int compare(Graphics g1, Graphics g2) {
			if (g1.getVCenterY() == g2.getVCenterY())
				return 0;
			else if (g1.getVCenterY() < g2.getVCenterY())
				return -1;
			else

				return 1;
		}
	}
	public class XComparator implements Comparator<Graphics> {
		public int compare(Graphics g1, Graphics g2) {
			if (g1.getVCenterX() == g2.getVCenterX())
				return 0;
			else if (g1.getVCenterX() < g2.getVCenterX())
				return -1;
			else

				return 1;
		}
	}
	

} // end of class
