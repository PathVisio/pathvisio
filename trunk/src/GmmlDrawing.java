import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;
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
	
	GmmlBpBrowser backPageBrowser;
	
	Vector drawingObjects;
	Vector graphics;
	Vector handles;
	Vector lineHandles;

	Vector selection;
	
	GmmlDrawingObject clickedObject	= null;
	GmmlDrawingObject draggedObject	= null;
	GmmlDrawingObject pressedObject	= null;	
	
	GmmlGraphics selectedGraphics	= null;
	GmmlGraphics draggedGraphics	= null;
	
	GmmlSelectionBox s; 
	
	boolean isSelecting;
		
	int previousX;
	int previousY;
	
	Dimension dims = new Dimension(1000, 1000);
	double zoomPercentage = 100;
	
	/**
	 *Constructor for this class
	 */	
	public GmmlDrawing(Composite parent, int style)
	{
		super (parent, style);
		
		drawingObjects	= new Vector();
		graphics		= new Vector();
		handles			= new Vector();
		lineHandles		= new Vector();
		selection		= new Vector();
		
		s = new GmmlSelectionBox(this);
		
		
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener (this);
		
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
	}
	
	public void setBrowser(GmmlBpBrowser browser) {
		backPageBrowser = browser;
	}
	
	private void calculateSize()
	{
		setSize (
			(int)(dims.width*zoomPercentage/100), 
			(int)(dims.height*zoomPercentage/100)
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
			if(h.type == 3 || h.type == 4)
			{
				lineHandles.addElement(h);
			}
			else 
			{
				handles.addElement(h);
			}	
		}
		else
		{
			GmmlDrawingObject object = (GmmlDrawingObject) o;
			graphics.addElement(object);
			
		}
	}

	/**
	 * handles mouse movement
	 */
	public void mouseMove(MouseEvent e)
	{
		if (!selection.isEmpty())
		{		
			Iterator it = selection.iterator();
			while (it.hasNext())
			{
				GmmlGraphics g = (GmmlGraphics) it.next();
				g.isSelected = true;
				g.moveBy(e.x - previousX, e.y - previousY);
			}
			previousX = e.x;
			previousY = e.y;			
		}
		
		if (draggedObject != null)
		{
			draggedObject.moveBy(e.x - previousX, e.y - previousY);
			
			previousX = e.x;
			previousY = e.y;
	
			redraw();
		}
						
		if (isSelecting)
		{
			s.x2 = e.x;
			s.y2 = e.y;			
			redraw();
		}
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
		if(selectedGraphics instanceof GmmlGeneProduct) {
			GmmlGeneProduct gp = (GmmlGeneProduct)selectedGraphics;
			// Get the backpage text
			GmmlGdb gmmlGdb = new GmmlGdb();
			String geneId = gp.getGeneId();
			String bpText = gmmlGdb.getBpInfo(geneId);
			String gexText = gmmlGdb.getExprInfo(geneId);
			if(bpText != null) {
				backPageBrowser.setBpText(bpText);
			} else {
				backPageBrowser.setBpText("<I>No gene information found</I>");
			}
			if(gexText != null) {
				backPageBrowser.setGexText(gexText);
			} else {
				backPageBrowser.setGexText("<I>No expression data found</I>");
			}
		}
		
	}

	/**
	 * Handles mouse Pressed input
	 */
	public void mouseDown(MouseEvent e)
	{
		if (draggedObject != null)
		{	
			// dragging in progress...
			return;
		}
		
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
		redraw();
		
		if (pressedObject != null)
		{
			// start dragging
			isSelecting = false;
						
			previousX = e.x;
			previousY = e.y;
			
			draggedObject = pressedObject;
			pressedObject = null;
		}
		
		else if (pressedObject == null)
		{
			// start selecting
			isSelecting = true;
			initSelection(p);
		}
	}
	
	/**
	 * Handles mouse Released input
	 */
	public void mouseUp(MouseEvent e)
	{
		if (isSelecting)
		{
			Iterator it = graphics.iterator();

			while (it.hasNext())
			{
				GmmlDrawingObject o = (GmmlDrawingObject) it.next();
				if (o instanceof GmmlGraphics)
				{
					GmmlGraphics g = (GmmlGraphics) o;
					// TODO
					// see if there is an intersection with the GmmlSelectionBox
					//~ if (g.intersects(r))
					//~ {
						//~ selection.addElement(g);
					//~ }
				}				
			}			
			isSelecting = false;
			redraw();
		}
		
		else if (draggedObject == null)
		{
			return;
		}
		
		else
		{
			draggedObject.moveBy(e.x - previousX, e.y - previousY);
			draggedObject = null;
		}
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
		Iterator it = graphics.iterator();	
		while (it.hasNext())
		{
			GmmlDrawingObject o = (GmmlDrawingObject) it.next();
			o.draw(e);
		}
	
		// iterate through all handles to paint them, after 
		// painting the graphics, to ensure handles are painted
		// on top of graphics
		it = handles.iterator();
		while (it.hasNext())
		{
			GmmlHandle h = (GmmlHandle) it.next();
			h.draw(e);
		}
		
		// iterate through all line handles to paint them
		it = lineHandles.iterator();
		while (it.hasNext())
		{
			GmmlHandle h = (GmmlHandle) it.next();
			h.draw(e);
		}		
	}

	/**
	 * Sets the drawings zoom
	 * @param zoom
	 */
	public void setZoom(double zoom)
	{
		double factor = zoom/zoomPercentage;
		zoomPercentage = zoom;
		calculateSize();
		
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
	 * Initializes selection, resetting the selectionbox
	 * and then setting it to the position specified
	 * @param p - the point to start with the selection
	 */
	private void initSelection(Point2D p)
	{
		selection.clear();
		s.resetRectangle();
		s.x1 = (int)p.getX();
		s.x2 = s.x1;
		s.y1 = (int)p.getY();		
		s.y2 = s.y1;		
	}
	
} // end of class