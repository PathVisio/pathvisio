import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Dimension;
import java.awt.Graphics;
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
	
	GmmlBpBrowser backPageBrowser;
	GmmlPropertyTable propertyTable;
	
	Vector drawingObjects;
	Vector graphics;
	Vector handles;

	Vector selection;
	
	GmmlDrawingObject pressedObject	= null;	
	
	GmmlGraphics selectedGraphics = null;
	
	GmmlSelectionBox s; 
	
	boolean isSelecting;
	boolean isDragging;
		
	int previousX;
	int previousY;
	
	Dimension dims = new Dimension(1000, 1000);
	double zoomFactor = 100;
	
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
	
	public void setBrowser(GmmlBpBrowser browser) {
		backPageBrowser = browser;
	}
	
	public void setPropertyTable(GmmlPropertyTable propertyTable) {
		this.propertyTable = propertyTable;
	}
	
	private void calculateSize()
	{
		setSize (
			(int)(dims.width*zoomFactor/100), 
			(int)(dims.height*zoomFactor/100)
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

	public void updateJdomElements() {
		// Update jdomElement for every graphics object
		Iterator it = drawingObjects.iterator();
		while(it.hasNext()) {
			GmmlDrawingObject o = (GmmlDrawingObject)it.next();
			if(o instanceof GmmlGraphics) {
				((GmmlGraphics)o).updateJdomGraphics();
			}
		}
	}
	
	/**
	 * handles mouse movement
	 */
	public void mouseMove(MouseEvent e)
	{
		if (isDragging)
		{		
			Iterator it = selection.iterator();
			while (it.hasNext())
			{
				GmmlDrawingObject g = (GmmlDrawingObject) it.next();
				g.moveBy(e.x - previousX, e.y - previousY);
			}
			previousX = e.x;
			previousY = e.y;
			redraw();
		}
						
		if (isSelecting)
		{
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
						}
					}
					else
					{
						g.isSelected = false;
						if (selection.contains(g))
						{
							selection.remove(g);
						}
					}
				}				
			}
			redraw();
		}
	}

	/**
	 * Handles mouse Pressed input
	 */
	public void mouseDown(MouseEvent e)
	{		
		setFocus();
		
		Point2D p = new Point2D.Double(e.x, e.y);
		
		Iterator it = drawingObjects.iterator();
		GmmlHandle pressedHandle = null;
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
			pressedObject.isSelected = true;
			if(!selection.contains(pressedObject))
			{
				//TODO: if ctrl is pressed, don't clear, but just add object
				initSelection(p);
				selection.add(pressedObject);
				if(pressedObject instanceof GmmlHandle)
				{
					((GmmlHandle)pressedObject).parent.isSelected = true;
				}
			}
			// show property table
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
			initSelection(p);
			updatePropertyTable(null);
		}
	}
	
	/**
	 * Handles mouse Released input
	 */
	public void mouseUp(MouseEvent e)
	{
		updatePropertyTable(propertyTable.g);
		isDragging = false;
		isSelecting = false;
		
		redraw();
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
			GmmlGdb gmmlGdb = new GmmlGdb();
			String geneId = gp.getGeneId();
			String bpText = gmmlGdb.getBpInfo(geneId);
			String gexText = gmmlGdb.getExprInfo(geneId);
			if (bpText != null) 
			{
				backPageBrowser.setBpText(bpText);
			} 
			else 
			{
				backPageBrowser.setBpText("<I>No gene information found</I>");
			}
			if (gexText != null) 
			{
				backPageBrowser.setGexText(gexText);
			}
			else 
			{
				backPageBrowser.setGexText("<I>No expression data found</I>");
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
	}

	/**
	 * Sets the drawings zoom
	 * @param zoom
	 */
	public void setZoom(double zoom)
	{
		double factor = zoom/zoomFactor;
		zoomFactor = zoom;
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
	
	private void updatePropertyTable(GmmlDrawingObject o)
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
			g.updateToPropItems();
			propertyTable.setGraphics(g);
			propertyTable.tableViewer.setInput(g.propItems);
		}
		else
		{
			propertyTable.setGraphics(null);
			propertyTable.tableViewer.setInput(null);
		}

	}
	/**
	 * Initializes selection, resetting the selectionbox
	 * and then setting it to the position specified
	 * @param p - the point to start with the selection
	 */
	private void initSelection(Point2D p)
	{
		Iterator it = selection.iterator();
		while (it.hasNext())
		{
			GmmlDrawingObject g = (GmmlDrawingObject) it.next();
			g.isSelected = false;
		}
		selection.clear();
		s.resetRectangle();
		s.x1 = (int)p.getX();
		s.x2 = s.x1;
		s.y1 = (int)p.getY();		
		s.y2 = s.y1;		
	}
	
	//TODO: resize when moving object out of drawing boundaries
//	private void checkBoundaries() {
//		Iterator it = drawingObjects.iterator();
//		while(it.hasNext())
//		{
//			GmmlDrawingObject o = (GmmlDrawingObject)it.next();
//			if(o instanceof GmmlHandle) {
//				GmmlHandle h = (GmmlHandle)o;
//				if(h.centerx > getSize().x)
//				{
//					this.setSize((int)h.centerx, getSize().y);
//				} 
//				else if (h.centery > getSize().y)
//				{
//					this.setSize(getSize().x, (int)h.centery);
//				}
//			}
//		}
//	}
	
} // end of class