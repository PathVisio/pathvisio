import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
//~ import java.awt.event.*;
import java.util.*;
//~ import javax.swing.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;

/**
 * This class implements and handles a drawing.
 * GmmlGraphics objects are stored in the drawing and can be 
 * visualized. The class also provides methods for mouse 
 * event handling.
 */
//~ class GmmlDrawing extends JPanel implements MouseListener, MouseMotionListener, EventListener
class GmmlDrawing extends Canvas
{	
	private static final long serialVersionUID = 1L;
	
	Vector drawingObjects;
	Vector graphics;
	Vector handles;
	Vector lineHandles;

	Vector selection;
	
	GmmlDrawingObject clickedObject	= null;
	GmmlDrawingObject draggedObject	= null;
	GmmlDrawingObject pressedObject	= null;	
	
	GmmlGraphics selectedGraphics	= null;
	
	GmmlSelectionBox s; 
	
	boolean isSelecting;
		
	double previousX;
	double previousY;
	
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
		//~ selection		= new Vector();
		
		s = new GmmlSelectionBox(this);
		
		//~ addMouseListener(this);
		//~ addMouseMotionListener(this);
		
		//~ this.setBackground(Color.white);
		
		System.out.println ("Adding paint listener");
		
		addPaintListener (new PaintListener() 
		{
			public void paintControl (PaintEvent e) {
				System.out.println ("Paint event called");
				GmmlDrawing.this.paintControl (e);
			}
		} );
	}
	
	private void calculateSize()
	{
		setSize (
			(int)(dims.width*zoomPercentage/100), 
			(int)(dims.height*zoomPercentage/100)
		);
		//~ redraw();
	}

	//~ public void actionPerformed(ActionEvent e)
	//~ {
	//~ }

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

	//~ /**
	 //~ * Handles mouse click input
	 //~ */
	//~ public void mouseClicked(MouseEvent e)
		//~ {
	//~ /*		if (draggedGraphics != null)
			//~ {	
				//~ // dragging in progress...
				//~ return;
			//~ }
	
			//~ double x = e.getX();
			//~ double y = e.getY();
			
			//~ Point2D p = new Point2D.Double(x, y);
			
			//~ Iterator it = graphics.iterator();
			
			//~ boolean graphicsFound = false;
			//~ while (it.hasNext() && !graphicsFound)
			//~ {
				//~ GmmlGraphics g = (GmmlGraphics) it.next();
				//~ g.isContain(p);
			//~ }
			//~ repaint();*/
		//~ }

	//~ /**
	 //~ * handles mouse dragg input
	 //~ */
	//~ public void mouseDragged(MouseEvent e)
	//~ {
		//~ if (!selection.isEmpty())
		//~ {
			//~ double x = e.getX();
			//~ double y = e.getY();
			
			//~ Iterator it = selection.iterator();
			//~ while (it.hasNext())
			//~ {
				//~ GmmlGraphics g = (GmmlGraphics) it.next();
				//~ g.isSelected = true;
				//~ g.moveBy(x - previousX, y - previousY);
			//~ }
			//~ previousX = x;
			//~ previousY = y;			
		//~ }
		
		//~ if (draggedObject != null)
		//~ {
			//~ double x = e.getX();
			//~ double y = e.getY();
			
			//~ draggedObject.moveBy(x - previousX, y - previousY);
			
			//~ previousX = x;
			//~ previousY = y;
	
			//~ repaint();
		//~ }
						
		//~ if (isSelecting)
		//~ {
			//~ s.resize(e.getX() - s.x, e.getY() - s.y);
			//~ repaint();
		//~ }
	//~ }

	//~ /**
	 //~ * Handles mouse entered input
	 //~ */
	//~ public void mouseEntered(MouseEvent e)
	//~ {
	//~ }

	//~ /**
	 //~ * Handles mouse Exited input
	 //~ */
	//~ public void mouseExited(MouseEvent e)
	//~ {
	//~ }

	//~ /**
	 //~ * Handles mouse Moved input
	 //~ */
	//~ public void mouseMoved(MouseEvent e)
	//~ {
	//~ }

	//~ /**
	 //~ * Handles mouse Pressed input
	 //~ */
	//~ public void mousePressed(MouseEvent e)
	//~ {
		//~ if (draggedObject != null)
		//~ {	
			//~ // dragging in progress...
			//~ return;
		//~ }

		//~ double x = e.getX();
		//~ double y = e.getY();
		
		//~ Point2D p = new Point2D.Double(x, y);
		
		//~ Iterator it = drawingObjects.iterator();
		//~ while (it.hasNext())
		//~ {
			//~ GmmlDrawingObject o = (GmmlDrawingObject) it.next();
			//~ if (o.isContain(p))
			//~ {
				//~ pressedObject = o;
				//~ if (o instanceof GmmlGraphics)
				//~ {
					//~ GmmlGraphics g = (GmmlGraphics) o;
					//~ selectedGraphics = g;
				//~ }
				//~ break;
			//~ }
		//~ }
		//~ repaint();
		
		//~ if (pressedObject != null)
		//~ {
			//~ // start dragging
			//~ isSelecting = false;
						
			//~ previousX = x;
			//~ previousY = y;
			
			//~ draggedObject = pressedObject;
			//~ pressedObject = null;
		//~ }
		
		//~ else if (pressedObject == null)
		//~ {
			//~ // start selecting
			//~ isSelecting = true;
			//~ initSelection(p);
		//~ }
	//~ }
	
	//~ /**
	 //~ * Handles mouse Released input
	 //~ */
	//~ public void mouseReleased(MouseEvent e)
	//~ {
		//~ if (isSelecting)
		//~ {
			//~ Rectangle2D.Double r = s.r;
			//~ s.r = null;
			//~ Iterator it = graphics.iterator();

			//~ while (it.hasNext())
			//~ {
				//~ GmmlDrawingObject o = (GmmlDrawingObject) it.next();
				//~ if (o instanceof GmmlGraphics)
				//~ {
					//~ GmmlGraphics g = (GmmlGraphics) o;
					//~ if (g.intersects(r))
					//~ {
						//~ selection.addElement(g);
					//~ }
				//~ }				
			//~ }			
			//~ isSelecting = false;
			//~ repaint();
		//~ }
		
		//~ else if (draggedObject == null)
		//~ {
			//~ return;
		//~ }
		
		//~ else
		//~ {
			//~ double x = e.getX();
			//~ double y = e.getY();
			
			//~ draggedObject.moveBy(x - previousX, y - previousY);
			//~ draggedObject = null;
		//~ }
	//~ }
	
	/**
	 * Paints all components in the drawing.
	 * This method is called automatically in the 
	 * painting process
	 */
	public void paintControl (PaintEvent e)
	//~ public void paintComponent(Graphics g)
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
	//~ private void initSelection(Point2D p)
	//~ {
		//~ selection.clear();
		//~ s.resetRectangle();
		//~ s.x = p.getX();
		//~ s.y = p.getY();		
	//~ }
	
} // end of class