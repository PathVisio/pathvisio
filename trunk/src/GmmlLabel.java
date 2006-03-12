/*
Copyright 2005 H.C. Achterberg, R.M.H. Besseling, I.Kaashoek, 
M.M.Palm, E.D Pelgrim, BiGCaT (http://www.BiGCaT.unimaas.nl/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and 
limitations under the License.
*/

import java.awt.geom.Rectangle2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*;

import javax.swing.JTable;

import org.jdom.Attribute;
import org.jdom.Element;


public class GmmlLabel extends GmmlGraphics
{
	private static final long serialVersionUID = 1L;

	public final List attributes = Arrays.asList(new String[] {
			"TextLabel", "CenterX", "CenterY", "Width","Height",
			"FontName","FontWeight","FontStyle","FontSize","Color" 
	});
	
	String text				= "";
	String fontName			= "Times New Roman";
	String fontWeight		= "bold";
	String fontStyle		= "normal";
	
	double centerx;
	double centery;
	double width;
	double height;
	int fontSize	= 10;
	
	RGB color;
	
	GmmlDrawing canvas;
	
	Element jdomElement;
	
	GmmlHandle handlecenter	= new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this);

	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this label will be part of
	 */
	public GmmlLabel(GmmlDrawing canvas)
	{
		this.canvas = canvas;
		canvas.addElement(handlecenter);
	}
	
	/**
	 * Constructor for this class
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param width - widht
	 * @param height - height
	 * @param text - the labels text
	 * @param font - the labels font
	 * @param fontWeight - fontweigth
	 * @param fontStyle - fontstyle
	 * @param fontSize - fontsize
	 * @param color - the color the label is painted
	 * @param canvas - the GmmlDrawing the label will be part of
	 */
	public GmmlLabel (int x, int y, int width, int height, String text, String font, String fontWeight, 
		String fontStyle, int fontSize, RGB color, GmmlDrawing canvas)
	{
		this.centerx  = x;
		this.centery = y;
		this.width = width;
		this.height = height;
		this.text = text;
		this.fontName = font;
		this.fontWeight = fontWeight;
		this.fontStyle = fontStyle;
		this.fontSize = fontSize;
		this.color = color;
		this.canvas = canvas;
		
		setHandleLocation();
		canvas.addElement(handlecenter);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlLabel
	 * @param canvas - the GmmlDrawing this GmmlLabel will be part of
	 */
	public GmmlLabel (Element e, GmmlDrawing canvas) {
		this.jdomElement = e;
		// List the attributes

		mapAttributes(e);
		
		this.canvas = canvas;
		
		setHandleLocation();
		canvas.addElement(handlecenter);
	}

	/**
	  * Sets label upper corner to the specified coordinate
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>Double x	- the new x coordinate
	  * <DD>Double y	- the new y coordinates
	  * <DL> 
	  */	
	public void setLocation(double x, double y)
	{
		this.centerx = x;
		this.centery = y;
		
		// Update JDOM Graphics element
		updateJdomGraphics();
	}
	
	/**
	 * Updates the JDom representation of this label
	 */
	public void updateJdomGraphics() {
		if(jdomElement != null) {
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("CenterX", Integer.toString((int)centerx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString((int)centery * GmmlData.GMMLZOOM));
			}
		}
	}
		
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#adjustToZoom()
	 */
	protected void adjustToZoom(double factor)
	{
		centerx		*= factor;
		centery		*= factor;
		width		*= factor;
		height		*= factor;
		fontSize	*= factor;
	}

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e)
	{
		int style = SWT.NONE;
		
		if (fontWeight.equalsIgnoreCase("bold"))
		{
			style |= SWT.BOLD;
		}
		
		if (fontStyle.equalsIgnoreCase("italic"))
		{
			style |= SWT.ITALIC;
		}
		
		Font f = new Font(e.display, fontName, fontSize, style);
		
		e.gc.setFont (f);
		
		Point textSize = e.gc.textExtent (text);
		
		Color c;
		if (isSelected)
		{
			c = new Color (e.display, 255, 0, 0);
		}
		else 
		{
			c = new Color (e.display, this.color);
		}
		e.gc.setForeground (c);
		
		e.gc.drawString (text, 
			(int) centerx - (textSize.x / 2) , 
			(int) centery - (textSize.y / 2), true);
		
		f.dispose();
		
		setHandleLocation();
	}

	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#getPropertyTable()
	 */
	protected JTable getPropertyTable()
	{
		Object[][] data = new Object[][] {{text, new Double(centerx), 
			new Double(centery), new Double(width), new Double(height), 
			fontName, fontWeight, fontStyle, new Integer(fontSize), color}};
		
		Object[] cols = new Object[] {"TextLabel", "CenterX", "CenterY", 
				"Width", "Height", "FontName", "FontWeight", "FontStyle", 
				"FontSize", "Color" };
		
		return new JTable(data, cols);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#moveBy(double, double)
	 */
	protected void moveBy(double dx, double dy)
	{
		setLocation(centerx  + dx, centery + dy);
	}

	/**
	  *Method isContain uses the coordinates of a specific point (pointx, pointy) 
	  *to determine whether a label contains this point. 
	  *To do this, a 'real' rectangle object is formed, on which the normal contains method is used.
	  */	
	protected boolean isContain(Point2D p)
	{
		Rectangle2D rect = new Rectangle2D.Double(centerx - (width/2), centery - (height/2), width, height);
		isSelected = rect.contains(p);
		return isSelected;
	}

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#intersects(java.awt.geom.Rectangle2D.Double)
	 */
	protected boolean intersects(Rectangle2D.Double r)
	{
		isSelected = r.intersects(centerx - width/2, centery - height/2, width, height);
		return isSelected;
	}

	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#updateFromPropertyTable(javax.swing.JTable)
	 */
	protected void updateFromPropertyTable(JTable t)
	{
		text		= t.getValueAt(0, 0).toString();
		centerx		= Double.parseDouble(t.getValueAt(0, 1).toString());
		centery		= Double.parseDouble(t.getValueAt(0, 2).toString());
		width		= Double.parseDouble(t.getValueAt(0, 3).toString());
		height		= Double.parseDouble(t.getValueAt(0, 4).toString());
		fontName	= t.getValueAt(0, 5).toString();
		fontWeight	= t.getValueAt(0, 6).toString();
		fontStyle	= t.getValueAt(0, 7).toString();
		fontSize	= (int)Double.parseDouble(t.getValueAt(0, 8).toString());
		color 		= GmmlColorConvertor.string2Color(t.getValueAt(0, 9).toString());;
	}
	
	/**
	 * Maps attributes to internal variables.
	 * @param e - the element to map to a GmmlArc
	 */
	private void mapAttributes (Element e) {
		// Map attributes
		System.out.println("> Mapping element '" + e.getName()+ "'");
		Iterator it = e.getAttributes().iterator();
		while(it.hasNext()) {
			Attribute at = (Attribute)it.next();
			int index = attributes.indexOf(at.getName());
			String value = at.getValue();
			switch(index) {
					case 0: // TextLabel
						this.text = value; break;
					case 1: // CenterX
						this.centerx = Integer.parseInt(value) / GmmlData.GMMLZOOM ; break;
					case 2: // CenterY
						this.centery = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 3: // Width
						this.width = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 4:	// Height
						this.height = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 5: // FontName
						this.fontName = value; break;
					case 6: // FontWeight
						this.fontWeight = value; break;
					case 7: // FontStyle
						this.fontStyle = value; break;
					case 8: // FontSize
						this.fontSize = Integer.parseInt(value); break;
					case 9: // Color
						this.color = GmmlColorConvertor.string2Color(value); break;
					case -1:
						System.out.println("\t> Attribute '" + at.getName() + "' is not recognized");
			}
		}
		// Map child's attributes
		it = e.getChildren().iterator();
		while(it.hasNext()) {
			mapAttributes((Element)it.next());
		}
	}

	/**
	 * Sets the handles in this class at the correct location.
	 */
	private void setHandleLocation()
	{
		handlecenter.setLocation(centerx, centery - height/2 - handlecenter.height/2);
	}

} // end of class
