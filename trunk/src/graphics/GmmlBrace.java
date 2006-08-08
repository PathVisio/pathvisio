package graphics;

import gmmlVision.GmmlVision;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import util.ColorConverter;
import util.SwtUtils;
import data.GmmlData;

/**
 * This class implements a brace and provides 
 * methods to resize and draw it
 */
public class GmmlBrace extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;
	
	public static final int INITIAL_PPO = 10;
	public static final int ORIENTATION_TOP		= 0;
	public static final int ORIENTATION_RIGHT	= 1;
	public static final int ORIENTATION_BOTTOM	= 2;
	public static final int ORIENTATION_LEFT	= 3;
	
	public final List attributes = Arrays.asList(new String[] {
			"CenterX", "CenterY", "Width", "PicPointOffset", "Orientation", "Color",
			"Notes"
	});
	
	RGB color;
	
	String notes = "";
	
	// Some mappings to Gmml
	private final List orientationMappings = Arrays.asList(new String[] {
			"top", "right", "bottom", "left"
	});
		
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this brace will be part of
	 */
	public GmmlBrace(GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_BRACE;
	}
	
	/**
	 * Constructor for this class
	 * @param centerX - center x coordinate
	 * @param centerY - center y coordinate
	 * @param width - width
	 * @param ppo - picpoint ofset
	 * @param orientation - orientation (0 for top, 1 for right, 2 for bottom, 3 for left)
	 * @param color - the color this brace will be painted
	 * @param canvas - the GmmlDrawing this brace will be part of
	 */
	public GmmlBrace(double centerX, double centerY, double width, double ppo, int orientation, RGB color, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.centerX = centerX;
		this.centerY = centerY;
		setGmmlWidth(width);
		setGmmlHeight(ppo);
		setOrientation(orientation);
		this.color = color;

		calcStart();
		setHandleLocation();
		createJdomElement(doc);
	}

	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlBrace
	 * @param canvas - the GmmlDrawing this GmmlBrace will be part of
	 */
	public GmmlBrace(Element e, GmmlDrawing canvas) {
		this(canvas);
		
		this.jdomElement = e;
		
		mapAttributes(e);
		
		calcStart();
		setHandleLocation();
	}

	public void setOrientation(int orientation) {
		if(orientation == ORIENTATION_TOP) rotation = 0;
		if(orientation == ORIENTATION_LEFT) rotation = Math.PI/2;
		if(orientation == ORIENTATION_BOTTOM) rotation = Math.PI;
		if(orientation == ORIENTATION_RIGHT) rotation = Math.PI*(3.0/2);
	}
		
	public int getOrientation() {
		double r = rotation / Math.PI;
		if(r < 1.0/4 || r >= 7.0/4) return ORIENTATION_RIGHT;
		if(r > 1.0/4 && r <= 3.0/4) return ORIENTATION_TOP;
		if(r > 3.0/4 && r <= 5.0/4) return ORIENTATION_LEFT;
		if(r > 5.0/4 && r <= 7.0/4) return ORIENTATION_BOTTOM;
		return 0;
	}
	
	public void setRotation(double angle) {
		super.setRotation(angle);
		setOrientation(getOrientation());
	}
	
	/**
	 * Updates the JDom representation of this arc
	 */
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("Notes", notes);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("CenterX", Integer.toString(getCenterX() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString(getCenterY() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString(getGmmlWidth()));
				jdomGraphics.setAttribute("PicPointOffset", Integer.toString(getGmmlHeight()));
				jdomGraphics.setAttribute("Orientation", (String)orientationMappings.get(getOrientation()));
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
			}
		}
	}
	
	protected void createJdomElement(Document doc) {
		if(jdomElement == null) {
			jdomElement = new Element("Brace");
			jdomElement.addContent(new Element("Graphics"));
			
			doc.getRootElement().addContent(jdomElement);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#adjustToZoom()
	 */
	protected void adjustToZoom(double factor)
	{
		startX	*= factor;
		startY	*= factor;
		width	*= factor;
		height	*= factor;
	}

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e, GC buffer)
	{		
		Color c = null;
		if (isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
		}
		else 
		{
			c = SwtUtils.changeColor(c, this.color, e.display);
		}
		buffer.setForeground (c);
		buffer.setLineStyle (SWT.LINE_SOLID);
		buffer.setLineWidth (2);
		
		Transform tr = new Transform(e.display);
		rotateGC(buffer, tr);
		
		int cx = getCenterX();
		int cy = getCenterY();
		int w = (int)width;
		int d = (int)height;
		
		buffer.drawLine (cx + d/2, cy, cx + w/2 - d/2, cy); //line on the right
		buffer.drawLine (cx - d/2, cy, cx - w/2 + d/2, cy); //line on the left
		buffer.drawArc (cx - w/2, cy, d, d, -180, -90); //arc on the left
		buffer.drawArc (cx - d, cy - d,	d, d, -90, 90); //left arc in the middle
		buffer.drawArc (cx, cy - d, d, d, -90, -90); //right arc in the middle
		buffer.drawArc (cx + w/2 - d, cy, d, d, 0, 90); //arc on the right
		
		buffer.setTransform(null);
		
		c.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
			
	public List getAttributes() {
		return attributes;
	}
	
	public void updateToPropItems()
	{
		if (propItems == null)
		{
			propItems = new Hashtable();
		}
		
		Object[] values = new Object[] {getCenterX(), getCenterY(),
				width, height, getOrientation(), color, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();	
		
		centerX = ((Integer)propItems.get(attributes.get(0)));
		centerY = ((Integer)propItems.get(attributes.get(1)));
		width		= (Double)propItems.get(attributes.get(2));
		height		= (Double)propItems.get(attributes.get(3));
		setOrientation((Integer)propItems.get(attributes.get(4)));
		color 		= (RGB)propItems.get(attributes.get(5));
		notes		= (String)propItems.get(attributes.get(6));
		calcStart();
		markDirty();
		setHandleLocation();
		canvas.redrawDirtyRect();
	}
	
	/**
	 * Maps attributes to internal variables.
	 * @param e - the element to map to a GmmlArc
	 */
	private void mapAttributes (Element e) {
		// Map attributes
		GmmlVision.log.trace("> Mapping element '" + e.getName()+ "'");
		Iterator it = e.getAttributes().iterator();
		while(it.hasNext()) {
			Attribute at = (Attribute)it.next();
			int index = attributes.indexOf(at.getName());
			String value = at.getValue();
			switch(index) {
					case 0: // CenterX
						centerX = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 1: // CenterY
						centerY = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 2: // Width
						setGmmlWidth(Integer.parseInt(value) / GmmlData.GMMLZOOM); break;
					case 3: // PicPointOffset
						setGmmlHeight(Double.parseDouble(value) / GmmlData.GMMLZOOM); break;
					case 4: // Orientation
						if(orientationMappings.indexOf(value) > -1)
							setOrientation(orientationMappings.indexOf(value));
						break;
					case 5: // Color
						this.color = ColorConverter.gmmlString2Color(value); break;
					case 6: // Notes
						this.notes = value;
					case -1:
						GmmlVision.log.trace("\t> Attribute '" + at.getName() + "' is not recognized");
			}
		}
		// Map child's attributes
		it = e.getChildren().iterator();
		while(it.hasNext()) {
			mapAttributes((Element)it.next());
		}
	}
}