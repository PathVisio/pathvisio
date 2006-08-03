package graphics;

import gmmlVision.GmmlBpBrowser;
import gmmlVision.GmmlVision;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Document;

import data.GmmlGex;
import data.GmmlGex.Sample;
import data.GmmlGex.CachedData.Data;

/**
 * This class implements and handles a drawing.
 * GmmlGraphics objects are stored in the drawing and can be 
 * visualized. The class also provides methods for mouse  and key
 * event handling.
 */
public class GmmlDrawing extends Canvas implements MouseListener, MouseMoveListener, 
PaintListener, MouseTrackListener, KeyListener
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
	 * {@link GmmlMappInfo} object that contains information about this pathway,
	 * currently only used for information in {@link gmmlVision.GmmlPropertyTable}
	 * (TODO: has to be implemented to behave the same as any GmmlGraphics object
	 * when displayed on the drawing)
	 */
	GmmlMappInfo mappInfo;
		
	GmmlSelectionBox s; 
		
	private boolean editMode;
	/**
	 * Checks if this drawing is in edit mode
	 * @return false if in edit mode, true if not
	 */
	public boolean isEditMode() { return editMode; }
	
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
	 * @param g	drawing object of which the boundaries have to be added
	 */
	public void addDirtyRect(GmmlDrawingObject g)
	{
		if(dirtyRect == null)
			dirtyRect = g.getBounds();
		else
			dirtyRect.add(g.getBounds());	
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
		
//		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
	}
		
	/**
	 * Sets the {@link MappInfo} containing information on the pathway
	 * @param mappInfo
	 */
	public void setMappInfo(GmmlMappInfo mappInfo)
	{
		this.mappInfo = mappInfo;
		drawingObjects.add(mappInfo);
	}

	/**
	 * Gets the {@link MappInfo} containing information on the pathway
	 * @return
	 */
	public GmmlMappInfo getMappInfo() { return mappInfo; }
	
	/**
	 * Adds an element to the drawing
	 * @param o the element to add
	 */
	public void addElement(GmmlDrawingObject o)
	{
		drawingObjects.add(o);
	}

	/**
	 * Get the gene identifiers of all genes in this pathway
	 * @return	{@link ArrayList<String>} containing an identifier for every gene on the mapp
	 */
	public ArrayList<String> getMappIds()
	{
		ArrayList<String> mappIds = new ArrayList<String>();
		for(GmmlDrawingObject o : drawingObjects)
		{
			if(o instanceof GmmlGeneProduct)
			{
				mappIds.add(((GmmlGeneProduct)o).name);
			}
		}
		return mappIds;
	}
	
	/**
	 * Get the systemcodes of all genes in this pathway
	 * @return	{@link ArrayList<String>} containing a systemcode for every gene on the mapp
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
	 * Get the current zoomfactor used
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
		
		// iterate over all graphics to adjust them
		for(GmmlDrawingObject o : drawingObjects)
		{
			if (o instanceof GmmlGraphics)
			{
				GmmlGraphics g = (GmmlGraphics) o;
				g.adjustToZoom(factor);		
			}
		}
		
//		legend.adjustToZoom(factor);
		redraw();
	}

	int previousX;
	int previousY;
	boolean isSelecting;
	boolean isDragging;
	/**
	 * handles mouse movement
	 */
	public void mouseMove(MouseEvent e)
	{
		// Dispose the tooltip if shown
		if(tip != null)
		{
			if(!tip.isDisposed()) tip.dispose();
		}
		
		if (isDragging)
		{		
			// if the main selection is a handle
			if (pressedObject != null && pressedObject instanceof GmmlHandle)
			{
				// move only the handle
				pressedObject.moveBy(e.x - previousX, e.y - previousY);
			}
			else
			{
				// move anything but handles
				for(GmmlDrawingObject o : drawingObjects)
				{
					if (o.isSelected() && !(o instanceof GmmlHandle)) 
						o.moveBy(e.x - previousX, e.y - previousY);
				}
			}
			previousX = e.x;
			previousY = e.y;
		}
						
		if (isSelecting)
		{
			s.setCorner(e.x, e.y);
			
			for(GmmlDrawingObject o : drawingObjects)
			{
				if (o instanceof GmmlGraphics)
				{
					GmmlGraphics g = (GmmlGraphics) o;
					if (g.intersects(s.getRectangle()))
					{
						g.select();
					}
					else
					{
						g.deselect();						
					}
				}				
			}
		}
		redrawDirtyRect();
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
		if(!editMode) return;
		if(isDragging)
		{
			updatePropertyTable(GmmlVision.getWindow().propertyTable.g);
			if(s.isSelected()) s.fitToSelection(); redrawDirtyRect();
		} else if (isSelecting)
		{
			updatePropertyTable(null);
			if(s.hasMultipleSelection()) s.select(); else s.markDirty();
			redrawDirtyRect();
		}
		isDragging = false;
		isSelecting = false;
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
//		buffer.setForeground(e.display.getSystemColor(SWT.COLOR_CYAN));
//		buffer.drawRectangle(e.x, e.y, e.width, e.height);
		
		Rectangle2D.Double r = new Rectangle.Double(e.x, e.y, e.width, e.height);
		    	
		Collections.sort(drawingObjects);
		    	    	 
		for(GmmlDrawingObject o : drawingObjects)
		{
			if(o.intersects(r))
			{
				o.draw (e, buffer);
			}
		}
		
		e.gc.drawImage(image, 0, 0);
		buffer.dispose();
	}

	/**
	 * Updates the JDOM nodes for all elements on this drawing
	 */
	public void updateJdomElements() {
		// Update jdomElement for every graphics object
		Iterator it = drawingObjects.iterator();
		while(it.hasNext()) {
			GmmlDrawingObject o = (GmmlDrawingObject)it.next();
			if(o instanceof GmmlGraphics) {
				((GmmlGraphics)o).updateJdomElement();
			}
		}
	}

	/**
	 * Updates the propertytable to display information about the given GmmlDrawingObject
	 * @param o object to update the property table for, if instanceof {@link GmmlHandle}, 
	 * then the parent object is used, if null, then the property table is cleared
	 */
	public void updatePropertyTable(GmmlDrawingObject o)
	{
		GmmlGraphics g = null;
		if (o != null)
		{
			if (o instanceof GmmlHandle && ((GmmlHandle)o).isVisible())
			{
				o = ((GmmlHandle)o).parent;
			}
			if (o instanceof GmmlGraphics)
			{
				g = (GmmlGraphics)o;
				g.updateToPropItems();
			}
		}
		GmmlVision.getWindow().propertyTable.setGraphics(g);
	}
	
	/**
	 * Updates the {@link GmmlBpBrowser} to display information about the given GmmlDrawingObject
	 * (currently only if it's a {@link GmmlGeneProduct})
	 * @param o object to update the backpage browser for, if instance of {@link GmmlHandle}, then the
	 * parent object is used, if null, then the backpage is cleared
	 */
	public void updateBackpageInfo(GmmlDrawingObject o)
	{
		GmmlGeneProduct gp = null;
		if (o instanceof GmmlHandle  && ((GmmlHandle)o).isVisible()) 
			o = ((GmmlHandle)o).parent;
		if (o instanceof GmmlGeneProduct)
			gp = (GmmlGeneProduct)o;
		GmmlVision.getWindow().bpBrowser.setGene(gp);
	}

	/**
	 * deselect all elements on the drawing
	 */
	private void clearSelection()
	{
		for (GmmlDrawingObject g : drawingObjects)
		{			
			g.deselect(); 
		}		
	}

	/**
	 * Handles event when on mouseDown in case the drawing is in view mode
	 * (does nothing yet)
	 * @param e	the mouse event to handle
	 */
	private void mouseDownViewMode(MouseEvent e) 
	{ 
		Point2D p = new Point2D.Double(e.x, e.y);
		GmmlDrawingObject obj = null;
		Collections.sort(drawingObjects);
		for (GmmlDrawingObject o : drawingObjects)
		{
			if (o.isContain(p))
			{
				obj = o;
				break;
			}
		}
		updatePropertyTable(obj);
		updateBackpageInfo(obj);
	}
	
	/**
	 * Initializes selection, resetting the selectionbox
	 * and then setting it to the position specified
	 * @param p - the point to start with the selection
	 */
	private void initSelection(Point2D p)
	{
		clearSelection();
		s.resetRectangle();
		s.x1 = (int)p.getX();
		s.x2 = s.x1;
		s.y1 = (int)p.getY();		
		s.y2 = s.y1;		
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
		
		pressedObject = null;
		Collections.sort(drawingObjects);
		for (GmmlDrawingObject o : drawingObjects)
		{
			if (o.isContain(p2d))
			{
				// select this object, unless it is an invisible gmmlHandle
				if (o instanceof GmmlHandle && !((GmmlHandle)o).isVisible()) 
					;
				else 
					pressedObject = o;
			}
		}
		
		// if we clicked on an object
		if (pressedObject != null)
		{
			// if our object is an handle, select also it's parent.
			if(pressedObject instanceof GmmlHandle)
			{
				((GmmlHandle)pressedObject).parent.select();
			}
			// if we click on an object outside the selection
			else if(!pressedObject.isSelected())
			{
				// clear the selection if CTRL isn't pressed
				if(!ctrlPressed) initSelection(p2d);
				pressedObject.select();
			} else {
				// object is already selected, deselect if CTRL is pressed
				if(ctrlPressed) pressedObject.deselect();
			}
			
			// start dragging
			previousX = p.x;
			previousY = p.y;
			
			isSelecting = false;
			isDragging = true;			
		}
		else
		{
			// start selecting
			isDragging = false;
			isSelecting = true;
			initSelection(p2d);
		}		
		updatePropertyTable(pressedObject);
		updateBackpageInfo(pressedObject);
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
	 * Add a new object to the drawing
	 * {@see GmmlDrawing#setNewGraphics(int)}
	 * @param p	The point where the user clicked on the drawing to add a new graphics
	 */
	private void newObject(Point e)
	{
		GmmlGraphics g = null;
		GmmlHandle h = null;
		Document d = GmmlVision.getGmmlData().getDocument();
		
		switch(newGraphics) {
		case NEWNONE:
			return;
		case NEWLINE:
			g = new GmmlLine(e.x, e.y, e.x, e.y,stdRGB, this, d);
			GmmlLine l = (GmmlLine)g;
			l.style = GmmlLine.STYLE_SOLID;
			l.type = GmmlLine.TYPE_LINE;
			h = l.handleEnd;
			isDragging = true;
			break;
		case NEWLINEARROW:
			g = new GmmlLine(e.x, e.y, e.x, e.y, stdRGB, this, d);
			l = (GmmlLine)g;
			l.style = GmmlLine.STYLE_SOLID;
			l.type = GmmlLine.TYPE_ARROW;
			h = l.handleEnd;
			isDragging = true;
			break;
		case NEWLINEDASHED:
			g = new GmmlLine(e.x, e.y, e.x, e.y, stdRGB, this, d);
			l = (GmmlLine)g;
			l.style = GmmlLine.STYLE_DASHED;
			l.type = GmmlLine.TYPE_LINE;
			h = l.handleEnd;
			isDragging = true;
			break;
		case NEWLINEDASHEDARROW:
			g = new GmmlLine(e.x, e.y, e.x, e.y, stdRGB, this, d);
			l = (GmmlLine)g;
			l.style = GmmlLine.STYLE_DASHED;
			l.type = GmmlLine.TYPE_ARROW;
			h = l.handleEnd;
			isDragging = true;
			break;
		case NEWLABEL:
			g = new GmmlLabel(e.x, e.y, (int)(GmmlLabel.INITIAL_WIDTH * zoomFactor),
					(int)(GmmlLabel.INITIAL_HEIGHT * zoomFactor), this, GmmlVision.getGmmlData().getDocument());
			((GmmlLabel)g).createTextControl();
			h = ((GmmlLabel)g).handlecenter;
			break;
		case NEWARC:
			g = new GmmlArc(e.x, e.y, 0, zoomFactor *80, stdRGB, 0, this, d);
			h = ((GmmlArc)g).handlex;
			isDragging = true;
			break;
		case NEWBRACE:
			g = new GmmlBrace(e.x, e.y, 0, GmmlBrace.INITIAL_PPO * zoomFactor, GmmlBrace.ORIENTATION_BOTTOM, 
					stdRGB, this, d);
			h = ((GmmlBrace)g).handlewidth;
			isDragging = true;
			break;
		case NEWGENEPRODUCT:
			g = new GmmlGeneProduct(e.x, e.y, GmmlGeneProduct.INITIAL_WIDTH * zoomFactor, 
					GmmlGeneProduct.INITIAL_HEIGHT * zoomFactor, "", "", stdRGB, this, 
					d);
			((GmmlGeneProduct)g).createTextControl();
			h = ((GmmlGeneProduct)g).handlecenter;
			break;
		case NEWRECTANGLE:
			g = new GmmlShape(e.x, e.y, 0, zoomFactor *20, GmmlShape.TYPE_RECTANGLE, stdRGB, 0, this, d);
			h = ((GmmlShape)g).handlex;
			isDragging = true;
			break;
		case NEWOVAL:
			g = new GmmlShape(e.x, e.y, 0, zoomFactor *20, GmmlShape.TYPE_OVAL, stdRGB, 0, this, d);
			h = ((GmmlShape)g).handlex;
			isDragging = true;
			break;
		case NEWTBAR:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_TBAR, stdRGB, this, d);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		case NEWRECEPTORROUND:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_RECEPTOR_ROUND, stdRGB, this, d);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		case NEWRECEPTORSQUARE:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_RECEPTOR_SQUARE, stdRGB, this, d);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		case NEWLIGANDROUND:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_LIGAND_ROUND, stdRGB, this, d);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		case NEWLIGANDSQUARE:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_LIGAND_SQUARE, stdRGB, this, d);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		}
		
		addElement(g);
		clearSelection();
		g.select();
		h.select();
		pressedObject = h;
		updatePropertyTable(g);
		
		previousX = e.x;
		previousY = e.y;
		
		GmmlVision.getWindow().deselectNewItemActions();
	}
	

	public static final int DRAW_ORDER_HANDLE = 0;
	public static final int DRAW_ORDER_SELECTIONBOX = 1;
	public static final int DRAW_ORDER_SELECTED = 2;
	public static final int DRAW_ORDER_GENEPRODUCT = 3;
	public static final int DRAW_ORDER_LABEL = 4;
	public static final int DRAW_ORDER_LINE = 5;
	public static final int DRAW_ORDER_ARC = 6;
	public static final int DRAW_ORDER_BRACE = 7;
	public static final int DRAW_ORDER_LINESHAPE = 8;
	public static final int DRAW_ORDER_SHAPE = 9;
	public static final int DRAW_ORDER_MAPPINFO = 10;
	public static final int DRAW_ORDER_DEFAULT = 11;
	
	public void mouseEnter(MouseEvent e) {}

	public void mouseExit(MouseEvent e) {}

	Shell tip;
	
	/**
	 * Responsible for drawing a tooltip displaying expression data when 
	 * hovering over a geneproduct
	 */
	public void mouseHover(MouseEvent e) {
		if(!editMode && GmmlGex.getColorSetIndex() > -1 && GmmlGex.isConnected()) {
			Point2D p = new Point2D.Double(e.x, e.y);
			
			Collections.sort(drawingObjects);
			Iterator it = drawingObjects.iterator();
			while (it.hasNext())
			{
				GmmlDrawingObject o = (GmmlDrawingObject) it.next();
				if (o.isContain(p))
				{
					if (o instanceof GmmlGeneProduct)
					{
						GmmlGeneProduct gp = (GmmlGeneProduct)o;
						
						if(tip != null && !tip.isDisposed()) tip.dispose();
						
						tip = new Shell(getShell().getDisplay(), SWT.ON_TOP | SWT.TOOL);  
						tip.setBackground(getShell().getDisplay()
			                   .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
						tip.setLayout(new RowLayout());
						Label labelL = new Label(tip, SWT.NONE);
			            Label labelR = new Label(tip, SWT.NONE);
			            labelL.setForeground(getShell().getDisplay()
			                    .getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			            labelL.setBackground(getShell().getDisplay()
			                    .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			            labelR.setForeground(getShell().getDisplay()
			                    .getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			            labelR.setBackground(getShell().getDisplay()
			                    .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			            
			            Data mappIdData = GmmlGex.getCachedData(gp.name, gp.getSystemCode());
			            if(mappIdData == null) return; //No data in cache for this geneproduct
			            HashMap<Integer, Object> data = mappIdData.getAverageSampleData();
			            String textL = "";
			            String textR = "";
			            for(Sample s : GmmlGex.getColorSets().get(GmmlGex.getColorSetIndex()).useSamples)
			            {
			            	textL += s.getName() + ":  \n";
			            	textR += data.get(new Integer(s.idSample)) + "\n";
			            }
			            if(textL.equals("") && textR.equals("")) return;
			            labelL.setText(textL);
			            labelR.setText(textR);
			            tip.pack();
			            Point mp = toDisplay(e.x, e.y);
			            tip.setLocation(mp.x + 15, mp.y + 15);
			            tip.setVisible(true);
						break;
					}
				}
			}
	}
	}

	private boolean ctrlPressed;
	private void ctrlPressed() 	{ ctrlPressed = true; 	}
	private void ctrlReleased() 	{ ctrlPressed = false; 	}
	
	public void keyPressed(KeyEvent e) { 
		if(e.keyCode == SWT.CTRL) ctrlPressed();
	}

	public void keyReleased(KeyEvent e) {
		ArrayList<GmmlDrawingObject> toRemove = new ArrayList<GmmlDrawingObject>();
		
		if(e.keyCode == SWT.CTRL) ctrlReleased();
		if(e.keyCode == SWT.DEL) {
			for(GmmlDrawingObject o : drawingObjects)
			{
				if(!o.isSelected()) continue; //Object not selected, skip
				toRemove.add(o);
				if(o instanceof GmmlGraphics) //Also add handles
				{
					for(GmmlHandle h : ((GmmlGraphics)o).getHandles()) toRemove.add(h);
				}
			}
			removeDrawingObjects(toRemove);
			redraw();
		}
		if(e.keyCode == SWT.HOME) {
			System.out.println("================");
			Collections.sort(drawingObjects);
			for(GmmlDrawingObject o : drawingObjects) 
				System.out.println(o.toString() + "\t" + o.isSelected() + "\t" + o.drawingOrder);
		}
	}
	
	/**
	 * Removes the {@link GmmlDrawingObject}s in the {@link ArrayList} from the drawing
	 * @param toRemove	The {@link ArrayList<GmmlDrawingObject>} containing the objects to be removed
	 */
	public void removeDrawingObjects(ArrayList<GmmlDrawingObject>toRemove)
	{
		for(GmmlDrawingObject o : toRemove)
		{
			drawingObjects.remove(o);
		}	
	}
	
	
} // end of class