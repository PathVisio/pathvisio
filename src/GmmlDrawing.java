import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.*;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;

/**
 * This class implements and handles a drawing.
 * GmmlGraphics objects are stored in the drawing and can be 
 * visualized. The class also provides methods for mouse 
 * event handling.
 */
class GmmlDrawing extends Canvas implements MouseListener, MouseMoveListener, PaintListener
{	
	private static final long serialVersionUID = 1L;
	
	GmmlVision gmmlVision;
	
	Vector drawingObjects;
	Vector graphics;
	Vector handles;

	Vector selection;
	
	GmmlDrawingObject pressedObject	= null;	
	GmmlHandle pressedHandle = null;
	
	GmmlGraphics selectedGraphics = null;
	
	GmmlMappInfo mappInfo;
	
	GmmlSelectionBox s; 
	
	boolean isSelecting;
	boolean isDragging;
	
	boolean editMode;
	
	int newGraphics = NEWNONE;
		
	int previousX;
	int previousY;
	
	Dimension dims = new Dimension(1000, 1000);
	double zoomFactor = 1;
	
	/**
	 *Constructor for this class
	 */	
	public GmmlDrawing(Composite parent, int style)
	{
		super (parent, style);
		
		drawingObjects	= new Vector();
		graphics		= new Vector();
		handles			= new Vector();
		selection		= new Vector();
		
		s = new GmmlSelectionBox(this);
		
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener (this);
		
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
	}
	
	public void setGmmlVision(GmmlVision gmmlVision) {
		this.gmmlVision = gmmlVision;
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
	public void addElement(Object o)
	{
		drawingObjects.addElement(o);
	
		if(o instanceof GmmlHandle)
		{
			GmmlHandle h = (GmmlHandle)o;
			handles.addElement(h);
		}
		else
		{
			GmmlDrawingObject object = (GmmlDrawingObject) o;
			graphics.addElement(object);
			
		}
	}

	public void setEditMode(boolean editMode)
	{
		this.editMode = editMode;
		if(!editMode)
		{
			clearSelection();
			redraw();
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
		
		System.out.println(zoomFactor);
		
		// iterate over all graphics to adjust them
		Iterator it = graphics.iterator();	
		while (it.hasNext())
		{
			GmmlDrawingObject o = (GmmlDrawingObject) it.next();
			if (o instanceof GmmlGraphics)
			{
				GmmlGraphics g = (GmmlGraphics) o;
				g.adjustToZoom(factor);		
			}
		}
		
		redraw();
	}

	/**
	 * handles mouse movement
	 */
	public void mouseMove(MouseEvent e)
	{
		if (isDragging)
		{		
			Rectangle rp = getRedrawRectangle(selection);
			Iterator it = selection.iterator();
			while(it.hasNext())
			{
				((GmmlDrawingObject)it.next()).moveBy(e.x - previousX, e.y - previousY);
			}
			Rectangle r = getRedrawRectangle(selection);
			r.add(rp);
			redraw(r.x, r.y, r.width, r.height, false);
			previousX = e.x;
			previousY = e.y;
		}
						
		if (isSelecting)
		{
			Vector toRedraw = new Vector();
			ArrayList rl = s.getSideAreas();
			
			s.x2 = e.x;
			s.y2 = e.y;
			
			Iterator it = graphics.iterator();
			while (it.hasNext())
			{
				GmmlDrawingObject o = (GmmlDrawingObject) it.next();
				if (o instanceof GmmlGraphics)
				{
					GmmlGraphics g = (GmmlGraphics) o;
					if (g.intersects(s.getRectangle()))
					{
						g.isSelected = true;
						if (!selection.contains(g))
						{
							selection.addElement(g);
							toRedraw.addElement(g);
						}
					}
					else
					{
						g.isSelected = false;
						if (selection.contains(g))
						{
							selection.remove(g);
							toRedraw.addElement(g);
						}
					}
				}				
			}
			rl.addAll(s.getSideAreas());
			redrawRectangleList(rl);
			
			Rectangle r = getRedrawRectangle(toRedraw);
			redraw(r.x, r.y, r.width, r.height, false);
		}
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
			redrawRectangleList(s.getSideAreas());
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
		Point2D p = new Point2D.Double(e.x, e.y);
		
		Iterator it = drawingObjects.iterator();
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
			String bpText = gmmlVision.gmmlDb.getBpInfo(geneId);
			String gexText = gmmlVision.gmmlDb.getGexData(geneId);
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

	private Rectangle getRedrawRectangle(Vector toRedraw)
	{
		Rectangle r = new Rectangle();
		Iterator it = toRedraw.iterator();
		while (it.hasNext())
		{
			GmmlDrawingObject g = (GmmlDrawingObject) it.next();
			if(r.x == 0 && r.y ==0 && r.width == 0 && r.height == 0) {
				r = new Rectangle(g.getBounds());
			} else {
				r.add(g.getBounds());
			}
			r.add(g.getBounds());
		}
		r.grow(5,5);
		return r;
	}
	
	private void redrawRectangleList(ArrayList rl)
	{
		for(int i = 0; i < rl.size(); i++)
		{
			Rectangle r = (Rectangle)rl.get(i);
			redraw(r.x, r.y, r.width, r.height, false);
		}
	}
	
	public void redrawObject(GmmlDrawingObject o)
	{
		Rectangle r = o.getBounds();
		r.grow(5,5);
		redraw(r.x, r.y, r.width, r.height, false);
	}
	/**
	 * Paints all components in the drawing.
	 * This method is called automatically in the 
	 * painting process
	 */
	public void paintControl (PaintEvent e)
	{
		GC gc = e.gc;	
		// paint parrent
		// not necessary in swt
		//~ super.paintComponent(g);
		
		// iterate through all graphics to paint them
		Rectangle2D.Double r = new Rectangle.Double(e.x, e.y, e.width, e.height);
		Iterator it = graphics.iterator();
		while (it.hasNext())
		{
			GmmlDrawingObject o = (GmmlDrawingObject) it.next();
			if(o.intersects(r)) 
			{
				o.draw(e);
			}
		}
	
		// iterate through all handles to paint them, after 
		// painting the graphics, to ensure handles are painted
		// on top of graphics
		it = handles.iterator();
		while (it.hasNext())
		{
			GmmlHandle h = (GmmlHandle) it.next();
			if(h.intersects(r)) 
			{
				h.draw(e);
			}
		}		
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
		Iterator it = selection.iterator();
		while (it.hasNext())
		{
			GmmlDrawingObject g = (GmmlDrawingObject) it.next();
			g.isSelected = false;
			if (g instanceof GmmlHandle)
			{
				((GmmlHandle)g).parent.isSelected = false;
			}
		}
		selection.clear();
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
	
	private void editObject(MouseEvent e)
	{
		Point2D p = new Point2D.Double(e.x, e.y);
		Vector toDraw = new Vector();
		pressedHandle = null;
		Iterator it = drawingObjects.iterator();
		while (it.hasNext())
		{
			GmmlDrawingObject o = (GmmlDrawingObject) it.next();
			if(selection.size() == 1)
			{
				o.isSelected = false;
			}
			if (o.isContain(p))
			{
				if (o instanceof GmmlHandle && selection.size() == 1)
				{
					pressedHandle = (GmmlHandle)o;
				}
				else if (o instanceof GmmlGraphics)
				{
					pressedObject = o;
				}
			}
		}
		
		if (pressedHandle != null)
		{
			pressedObject = (GmmlDrawingObject)pressedHandle;
		}
		
		if (pressedObject != null)
		{
			if(!selection.contains(pressedObject))
			{
				//TODO: if ctrl is pressed, don't clear, but just add object
				toDraw = (Vector)selection.clone();
				initSelection(p);
				selection.add(pressedObject);
			}
			if(pressedObject instanceof GmmlHandle)
			{
				((GmmlHandle)pressedObject).parent.isSelected = true;
			}
			pressedObject.isSelected = true;
			toDraw.addAll(selection);
			Rectangle r = getRedrawRectangle(toDraw);
			redraw(r.x, r.y, r.width, r.height, false);
			
			updatePropertyTable(pressedObject);
			
			// start dragging
			previousX = e.x;
			previousY = e.y;
			
			isSelecting = false;
			isDragging = true;
			pressedObject = null;	
		}
		else if (pressedObject == null)
		{
			// start selecting
			isDragging = false;
			isSelecting = true;
			Rectangle r = getRedrawRectangle(selection);
			initSelection(p);
			redraw(r.x, r.y, r.width, r.height, false);
//			updatePropertyTable(null);
		}
	}

	static final int NEWNONE = -1;

	static final int NEWLINE = 0;

	static final int NEWLABEL = 1;

	static final int NEWARC = 2;

	static final int NEWBRACE = 3;

	static final int NEWGENEPRODUCT = 4;

	static final int NEWLINEDASHED = 5;

	static final int NEWLINEARROW = 6;

	static final int NEWLINEDASHEDARROW = 7;

	static final int NEWRECTANGLE = 8;

	static final int NEWOVAL = 9;

	static final int NEWTBAR = 10;

	static final int NEWRECEPTORROUND = 11;

	static final int NEWLIGANDROUND = 12;

	static final int NEWRECEPTORSQUARE = 13;

	static final int NEWLIGANDSQUARE = 14;

	static final int NEWLINEMENU = 15;

	static final int NEWLINESHAPEMENU = 16;

	static final RGB stdRGB = new RGB(0, 0, 0);

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
		g.isSelected = true;
		selection.add(h);
		updatePropertyTable(g);
		
		previousX = e.x;
		previousY = e.y;
		
		gmmlVision.deselectNewItemActions();
	}
	
} // end of class