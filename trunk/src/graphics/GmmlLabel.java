package graphics;
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


import gmmlVision.GmmlVision;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import util.ColorConverter;
import util.SwtUtils;
import data.GmmlData;


public class GmmlLabel extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;
	private static final int INITIAL_FONTSIZE = 10;
	public static final int INITIAL_WIDTH = 80;
	public static final int INITIAL_HEIGHT = 20;
	
	public final List attributes = Arrays.asList(new String[] {
			"TextLabel", "CenterX", "CenterY", "Width","Height",
			"FontName","FontWeight","FontStyle","FontSize","Color",
			"Notes"
	});
	
	String text				= "";
	String fontName			= "Times New Roman";
	String fontWeight		= "bold";
	String fontStyle		= "normal";
	int fontSize;
	double fontSizeDouble;
	RGB color = new RGB(0,0,0);
	String notes = "";
			
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this label will be part of
	 */
	public GmmlLabel(GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_LABEL;

		this.fontSizeDouble = INITIAL_FONTSIZE * canvas.getZoomFactor();
		this.fontSize = (int)this.fontSizeDouble;
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
		String fontStyle, int fontSize, RGB color, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.centerX  = x;
		this.centerY = y;
		setGmmlWidth(width);
		setGmmlHeight(height);
		this.text = text;
		this.fontName = font;
		this.fontWeight = fontWeight;
		this.fontStyle = fontStyle;
		this.fontSize = fontSize;
		this.fontSizeDouble = fontSize;
		this.color = color;
				
		calcStart();	
		setHandleLocation();
		createJdomElement(doc);
	}
	
	public GmmlLabel (int x, int y, int width, int height, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.centerX = x;
		this.centerY = y;
		setGmmlWidth(width);
		setGmmlHeight(height);
		
		calcStart();	
		setHandleLocation();
		createJdomElement(doc);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlLabel
	 * @param canvas - the GmmlDrawing this GmmlLabel will be part of
	 */
	public GmmlLabel (Element e, GmmlDrawing canvas) {
		this(canvas);
		
		this.jdomElement = e;
		mapAttributes(e);
		calcStart();
		setHandleLocation();
	}
		
	public void setFontSize(double size) {
		fontSizeDouble = size;
		fontSize = (int)size;
	}
	
	public void setText(String text) {
		this.text = text;
		
		//Adjust width to text length
		GC gc = new GC(canvas.getDisplay());
		Font f = new Font(canvas.getDisplay(), fontName, fontSize, getFontStyle());
		gc.setFont (f);
		Point ts = gc.textExtent(text);
		f.dispose();
		gc.dispose();
		
		//Keep center location
		double nWidth = ts.x + 10 * getDrawing().getZoomFactor();
		double nHeight = ts.y + 10 * getDrawing().getZoomFactor();
		startX -= (nWidth - width)/2;
		startY -= (nHeight - height)/2;
		width = nWidth;
		height = nHeight;
		
		updateToPropItems();
		
		setHandleLocation();
	}
	
	/**
	 * Updates the JDom representation of this label
	 */
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("TextLabel", text);
			jdomElement.setAttribute("FontName", fontName);
			jdomElement.setAttribute("FontWeight", fontWeight);
			jdomElement.setAttribute("FontStyle", fontStyle);
			jdomElement.setAttribute("FontSize", Integer.toString(fontSize));
			jdomElement.setAttribute("Notes", notes);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("CenterX", Integer.toString(getCenterX() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString(getCenterY() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString((int)width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Integer.toString((int)height * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
			}
		}
	}
	
	private Text t;
	public void createTextControl()
	{
		Color background = canvas.getShell().getDisplay()
		.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		
		Composite textComposite = new Composite(canvas, SWT.NONE);
		textComposite.setLayout(new GridLayout());
		textComposite.setLocation(getCenterX(), getCenterY() - 10);
		textComposite.setBackground(background);
		
		Label label = new Label(textComposite, SWT.CENTER);
		label.setText("Specify label:");
		label.setBackground(background);
		t = new Text(textComposite, SWT.SINGLE | SWT.BORDER);

		t.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				disposeTextControl();
			}
		});
				
		t.setFocus();
		
		Button b = new Button(textComposite, SWT.PUSH);
		b.setText("OK");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				disposeTextControl();
			}
		});
		
		textComposite.pack();
	}
	
	protected void disposeTextControl()
	{
		markDirty();
		setText(t.getText());
		markDirty();
		Composite c = t.getParent();
		c.setVisible(false);
		c.dispose();
				
		canvas.redrawDirtyRect();
	}
	
	protected void createJdomElement(Document doc) {
		if(jdomElement == null) {
			jdomElement = new Element("Label");
			jdomElement.addContent(new Element("Graphics"));
			
			doc.getRootElement().addContent(jdomElement);
		}
	}
	
	protected void adjustToZoom(double factor)
	{
		startX		*= factor;
		startY		*= factor;
		width		*= factor;
		height		*= factor;
		fontSizeDouble *= factor;
		fontSize = (int)fontSizeDouble;
		setHandleLocation();
	}

	private int getFontStyle() {
		int style = SWT.NONE;
		
		if (fontWeight.equalsIgnoreCase("bold"))
		{
			style |= SWT.BOLD;
		}
		
		if (fontStyle.equalsIgnoreCase("italic"))
		{
			style |= SWT.ITALIC;
		}
		return style;
	}
	
	protected void draw(PaintEvent e, GC buffer)
	{
		int style = getFontStyle();
		
		Font f = new Font(e.display, fontName, fontSize, style);
		
		buffer.setFont (f);
		
		Point textSize = buffer.textExtent (text);
		
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
		
		buffer.drawString (text, 
			(int) getCenterX() - (textSize.x / 2) , 
			(int) getCenterY() - (textSize.y / 2), true);
		
		f.dispose();
		c.dispose();
		
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	protected Rectangle getBounds()
	{
		Rectangle bounds = getOutline().getBounds();
		GC gc = new GC(canvas);
		Font f = new Font(canvas.getDisplay(), fontName, fontSize, getFontStyle());
		gc.setFont (f);
		org.eclipse.swt.graphics.Point p = gc.textExtent(text);
		util.LinAlg.Point c = getCenter();
		bounds.add(new Rectangle2D.Double(c.x - p.x/2, c.y - p.y/2, p.x, p.y));
		
		gc.dispose();
		f.dispose();
		return bounds;
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
		
		Object[] values = new Object[] {text, (double)getCenterX(), 
				(double)getCenterY(), width, height, fontName, fontWeight, 
				fontStyle, fontSize, color, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();
		
		String text = ((String)propItems.get(attributes.get(0)));
		centerX		= (Double)propItems.get(attributes.get(1));
		centerY		= (Double)propItems.get(attributes.get(2));
		width		= (Double)propItems.get(attributes.get(3));
		height 		= (Double)propItems.get(attributes.get(4));
		fontName	= (String)propItems.get(attributes.get(5));
		fontWeight	= (String)propItems.get(attributes.get(6));
		fontStyle	= (String)propItems.get(attributes.get(7));
		fontSize	= (Integer)propItems.get(attributes.get(8));
		color		= (RGB)propItems.get(attributes.get(9));
		notes		= (String)propItems.get(attributes.get(10));
		
		//Check for change in text and resize width if needed
		if(!this.text.equals(text)) setText(text);
		
		calcStart();
		markDirty();
		setHandleLocation();
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
					case 0: // TextLabel
						this.text = value; break;
					case 1: // CenterX
						this.centerX = Integer.parseInt(value) / GmmlData.GMMLZOOM ; break;
					case 2: // CenterY
						this.centerY = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
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
						this.fontSize = Integer.parseInt(value);
						this.fontSizeDouble = this.fontSize; break;
					case 9: // Color
						this.color = ColorConverter.gmmlString2Color(value); break;
					case 10: // Notes
						this.notes = value; break;
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
