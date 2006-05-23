package graphics;

import gmmlVision.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;

import data.GmmlGex.ConvertThread;

/**
 * This class implements and handles a drawing.
 * GmmlGraphics objects are stored in the drawing and can be 
 * visualized. The class also provides methods for mouse 
 * event handling.
 */
public class GmmlDrawing extends Canvas implements MouseListener, MouseMoveListener, PaintListener
{	
	private static final long serialVersionUID = 1L;
	
	public GmmlVision gmmlVision;
	
	public int colorSetIndex;

	/*
	 * All objects that are visible on this mapp, including the handles
	 * but excluding the legend, mappInfo and selectionBox objects
	 */
	ArrayList<GmmlDrawingObject> drawingObjects;
	
	GmmlDrawingObject pressedObject	= null;	
	
	public GmmlGraphics selectedGraphics = null;
	
	public GmmlMappInfo mappInfo;
	
	public GmmlLegend legend;
	
	GmmlSelectionBox s; 
	
	boolean isSelecting;
	boolean isDragging;
	
	public boolean editMode;
	
	public int newGraphics = NEWNONE;
		
	int previousX;
	int previousY;
	
	public Dimension dims = new Dimension(1000, 1000);
	public double zoomFactor = 1;
	
	private Rectangle dirtyRect = null;
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
//		graphics		= new Vector<GmmlGraphics>();
//		handles			= new Vector();
		
		s = new GmmlSelectionBox(this);
		
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener (this);
		
//		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		colorSetIndex = -1;

		legend = new GmmlLegend(this, SWT.NONE);
		legend.setDrawing(this);
	}
	
	public void setGmmlVision(GmmlVision gmmlVision) {
		this.gmmlVision = gmmlVision;
	}
	
	public void setMappInfo(GmmlMappInfo mappInfo)
	{
		this.mappInfo = mappInfo;
		legend.setLocation(mappInfo.mapInfoLeft, mappInfo.mapInfoTop);
	}
	
	private void calculateSize()
	{
		setSize (
			(int)(dims.width*zoomFactor), 
			(int)(dims.height*zoomFactor)
		);
	}

	/**
 	 * Adds an element to the drawing. Checks if 
	 * the object to add is an instance of GmmlHandle
	 * and in case it is, adds the object to the correct
	 * vector of gmmlgraphics objects.
	 * @param o - the object to add
	 */
	public void addElement(GmmlDrawingObject o)
	{
		drawingObjects.add(o);
	
//		if(o instanceof GmmlHandle)
//		{
//			GmmlHandle h = (GmmlHandle)o;
////			handles.addElement(h);
//		}
//		else
//		{
//			GmmlDrawingObject object = (GmmlDrawingObject) o;
//			gm.addElement(object);
			
//		}
	}

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
		
	public void setEditMode(boolean editMode)
	{
		this.editMode = editMode;
		if(!editMode)
		{
			clearSelection();
		}
		showLegend(!editMode);	
		redraw();
	}
	
	public void setColorSetIndex(int colorSetIndex)
	{
		this.colorSetIndex = colorSetIndex;
		if(colorSetIndex < 0)
		{
			showLegend(false);
		} else {
			showLegend(true);
		}
		redraw();	
	}
	
	public void showLegend(boolean show)
	{
		if(show && colorSetIndex > -1 && !editMode && gmmlVision.getShowLegendSelected())
		{
			legend.resetContents();
			legend.setVisible(true);
		} else {
			legend.setVisible(false);
		}
	}
	
	/**
	 * Sets the drawings zoom
	 * @param zoom
	 */
	public void setZoom(double zoom)
	{
		double factor = 0.01*zoom/zoomFactor;
		zoomFactor = zoom / 100;
		calculateSize();
		
		// iterate over all graphics to adjust them
		for(GmmlDrawingObject o : drawingObjects)
		{
			if (o instanceof GmmlGraphics)
			{
				GmmlGraphics g = (GmmlGraphics) o;
				g.adjustToZoom(factor);		
			}
		}
		
		legend.adjustToZoom(factor);
		redraw();
	}

	/**
	 * handles mouse movement
	 */
	public void mouseMove(MouseEvent e)
	{
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
			// adjust selection area
			s.markDirty();
			s.x2 = e.x;
			s.y2 = e.y;
			s.markDirty(); // also mark selection area
			
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
				newObject(e);
			}
			else
			{
				editObject(e);
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
			updatePropertyTable(gmmlVision.propertyTable.g);
		} else if (isSelecting)
		{
			updatePropertyTable(null);
//			redrawRectangleList(s.getSideAreas());
			s.markDirty();
			redrawDirtyRect();
//			redraw();
		}
		isDragging = false;
		isSelecting = false;
//		redrawSelection(e.x,e.y,10,10);
	}
	
	/**
	 * Handles mouse entered input
	 */
	public void mouseDoubleClick(MouseEvent e)
	{
		if(!editMode) {
			Point2D p = new Point2D.Double(e.x, e.y);
			
			Collections.sort(drawingObjects);
			Iterator it = drawingObjects.iterator();
			// drawingObjects is a sortedSet, so Guaranteed ordered checking
			while (it.hasNext())
			{
				GmmlDrawingObject o = (GmmlDrawingObject) it.next();
				if (o.isContain(p))
				{
					pressedObject = o;
					if (o instanceof GmmlGraphics)
					{
						GmmlGraphics g = (GmmlGraphics) o;
						selectedGraphics = g;
					}
					break;
				}
			}
			// Check if selectedGraphics is GeneProduct
			if (selectedGraphics instanceof GmmlGeneProduct) 
			{
				GmmlGeneProduct gp = (GmmlGeneProduct)selectedGraphics;
				// Get the backpage text
				String geneId = gp.getGeneId();
				String bpText = gmmlVision.gmmlGdb.getBpInfo(geneId);
				String gexText = gmmlVision.gmmlGex.getDataString(geneId);
				if (bpText != null) 
				{
					gmmlVision.bpBrowser.setBpText(bpText);
				} 
				else 
				{
					gmmlVision.bpBrowser.setBpText("<I>No gene information found</I>");
				}
				if (gexText != null) 
				{
					gmmlVision.bpBrowser.setGexText(gexText);
				}
				else 
				{
					gmmlVision.bpBrowser.setGexText("<I>No expression data found</I>");
				}
			}
		}
		
	}

	/**
	 * Paints all components in the drawing.
	 * This method is called automatically in the 
	 * painting process
	 */
	public void paintControl (PaintEvent e)
	{
		// paint parrent
		// not necessary in swt
		//~ super.paintComponent(g);
		
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

//		GmmlDrawingObject o = p.poll();
//		while(o != null)
//		{
//			o.draw(e, buffer);
//			o = p.poll();
//		}
		
		e.gc.drawImage(image, 0, 0);
		buffer.dispose();
	}

	public void updateJdomElements() {
		mappInfo.updateJdomElement();
		// Update jdomElement for every graphics object
		Iterator it = drawingObjects.iterator();
		while(it.hasNext()) {
			GmmlDrawingObject o = (GmmlDrawingObject)it.next();
			if(o instanceof GmmlGraphics) {
				((GmmlGraphics)o).updateJdomElement();
			}
		}
	}

	public void updatePropertyTable(GmmlDrawingObject o)
	{
		GmmlGraphics g;
		if (o != null)
		{
			if (o instanceof GmmlHandle)
			{
				g = ((GmmlHandle)o).parent;
			}
			else
			{
				g = (GmmlGraphics)o;
			}
		}
		else
		{
			g = mappInfo;
		}
		g.updateToPropItems();
		gmmlVision.propertyTable.tableViewer.setInput(g);
	}
	
	private void clearSelection()
	{
		for (GmmlDrawingObject g : drawingObjects)
		{			
			g.deselect(); 
		}		
	}

	private void mouseDownViewMode(MouseEvent e)
	{
		
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
	 * Called by MouseDown, when we're in editting mode and we're not adding new objects
	 * prepares for dragging the object
	 */
	private void editObject(MouseEvent e)
	{
		Point2D p = new Point2D.Double(e.x, e.y);
		
		pressedObject = null;
		Collections.sort(drawingObjects);
		for (GmmlDrawingObject o : drawingObjects)
		{
			if (o.isContain(p))
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
			// if we click on an object outside the selection
			if(!pressedObject.isSelected())
			{
				// clear the selection
				//TODO: if ctrl is pressed, don't clear, but just add object				
				initSelection(p);
				pressedObject.select();
			}
			// if our object is an handle, select also it's parent.
			if(pressedObject instanceof GmmlHandle)
			{
				((GmmlHandle)pressedObject).parent.select();
				updatePropertyTable(((GmmlHandle)pressedObject).parent);
			}
			else
			{
				updatePropertyTable(pressedObject);
			}
			
			// start dragging
			previousX = e.x;
			previousY = e.y;
			
			isSelecting = false;
			isDragging = true;			
		}
		else
		{
			// start selecting
			isDragging = false;
			isSelecting = true;
			initSelection(p);
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

	private void newObject(MouseEvent e)
	{
		GmmlGraphics g = null;
		GmmlHandle h = null;
		
		switch(newGraphics) {
		case NEWNONE:
			return;
		case NEWLINE:
			g = new GmmlLine(e.x, e.y, e.x, e.y,stdRGB, this, gmmlVision.gmmlData.doc);
			GmmlLine l = (GmmlLine)g;
			l.style = GmmlLine.STYLE_SOLID;
			l.type = GmmlLine.TYPE_LINE;
			h = l.handleEnd;
			isDragging = true;
			break;
		case NEWLINEARROW:
			g = new GmmlLine(e.x, e.y, e.x, e.y, stdRGB, this, gmmlVision.gmmlData.doc);
			l = (GmmlLine)g;
			l.style = GmmlLine.STYLE_SOLID;
			l.type = GmmlLine.TYPE_ARROW;
			h = l.handleEnd;
			isDragging = true;
			break;
		case NEWLINEDASHED:
			g = new GmmlLine(e.x, e.y, e.x, e.y, stdRGB, this, gmmlVision.gmmlData.doc);
			l = (GmmlLine)g;
			l.style = GmmlLine.STYLE_DASHED;
			l.type = GmmlLine.TYPE_LINE;
			h = l.handleEnd;
			isDragging = true;
			break;
		case NEWLINEDASHEDARROW:
			g = new GmmlLine(e.x, e.y, e.x, e.y, stdRGB, this, gmmlVision.gmmlData.doc);
			l = (GmmlLine)g;
			l.style = GmmlLine.STYLE_DASHED;
			l.type = GmmlLine.TYPE_ARROW;
			h = l.handleEnd;
			isDragging = true;
			break;
		case NEWLABEL:
			g = new GmmlLabel(e.x, e.y, (int)(GmmlLabel.INITIAL_WIDTH * zoomFactor),
					(int)(GmmlLabel.INITIAL_HEIGHT * zoomFactor), this, gmmlVision.gmmlData.doc);
			((GmmlLabel)g).createTextControl();
			h = ((GmmlLabel)g).handlecenter;
			break;
		case NEWARC:
			g = new GmmlArc(e.x, e.y, 0, zoomFactor *80, stdRGB, 0, this, gmmlVision.gmmlData.doc);
			h = ((GmmlArc)g).handlex;
			isDragging = true;
			break;
		case NEWBRACE:
			g = new GmmlBrace(e.x, e.y, 0, GmmlBrace.INITIAL_PPO * zoomFactor, GmmlBrace.ORIENTATION_BOTTOM, 
					stdRGB, this, gmmlVision.gmmlData.doc);
			h = ((GmmlBrace)g).handlewidth;
			isDragging = true;
			break;
		case NEWGENEPRODUCT:
			g = new GmmlGeneProduct(e.x, e.y, GmmlGeneProduct.INITIAL_WIDTH * zoomFactor, 
					GmmlGeneProduct.INITIAL_HEIGHT * zoomFactor, "", "", stdRGB, this, 
					gmmlVision.gmmlData.doc);
			((GmmlGeneProduct)g).createTextControl();
			h = ((GmmlGeneProduct)g).handlecenter;
			break;
		case NEWRECTANGLE:
			g = new GmmlShape(e.x, e.y, 0, zoomFactor *20, GmmlShape.TYPE_RECTANGLE, stdRGB, 0, this, gmmlVision.gmmlData.doc);
			h = ((GmmlShape)g).handlex;
			isDragging = true;
			break;
		case NEWOVAL:
			g = new GmmlShape(e.x, e.y, 0, zoomFactor *20, GmmlShape.TYPE_OVAL, stdRGB, 0, this, gmmlVision.gmmlData.doc);
			h = ((GmmlShape)g).handlex;
			isDragging = true;
			break;
		case NEWTBAR:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_TBAR, stdRGB, this, gmmlVision.gmmlData.doc);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		case NEWRECEPTORROUND:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_RECEPTOR_ROUND, stdRGB, this, gmmlVision.gmmlData.doc);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		case NEWRECEPTORSQUARE:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_RECEPTOR_SQUARE, stdRGB, this, gmmlVision.gmmlData.doc);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		case NEWLIGANDROUND:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_LIGAND_ROUND, stdRGB, this, gmmlVision.gmmlData.doc);
			h = ((GmmlLineShape)g).handleEnd;
			isDragging = true;
			break;
		case NEWLIGANDSQUARE:
			g = new GmmlLineShape(e.x, e.y, e.x, e.y, GmmlLineShape.TYPE_LIGAND_SQUARE, stdRGB, this, gmmlVision.gmmlData.doc);
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
		
		gmmlVision.deselectNewItemActions();
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
	public static final int DRAW_ORDER_DEFAULT = 10;
	
	
	
} // end of class