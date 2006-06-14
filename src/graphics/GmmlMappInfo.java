package graphics;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.jdom.Attribute;
import org.jdom.Element;

import util.SwtUtils;

import data.GmmlData;

public class GmmlMappInfo extends GmmlGraphics {
	
	final static List attributes =  Arrays.asList(new String[] {
		"Name", "Organism", "Data-Source", "Version", "Author",
		"Maintained-By", "Email", "Availability", "Last-Modified",
		"BoardWidth", "BoardHeight", "WindowWidth", "WindowHeight",
		"MapInfoLeft", "MapInfoTop"
	});
	
	String name = "";
	String organism = "";
	String dataSource = "";
	String version = "";
	String author = "";
	String maintainedBy = ""; 
	String email = "";
	String availability = "";
	String lastModified = "";
	
	public int boardWidth;
	public int boardHeight;
	public int windowWidth;
	public int windowHeight;
	
	public int mapInfoLeft;
	public int mapInfoTop;
	
	Element jdomElement;
	
	GmmlHandle handlecenter;
	
	//Elements not stored in gmml
	String fontName			= "Times New Roman";
	String fontWeight		= "regular";
	int fontSize			= 10;
	double fontSizeDouble = fontSize;
	
	int sizeX = 1;
	int sizeY = 1; //Real size is calculated on first call to draw()
	
	public GmmlMappInfo(GmmlDrawing canvas, Element e) 
	{
		drawingOrder = GmmlDrawing.DRAW_ORDER_MAPPINFO;
		
		this.canvas = canvas;
		jdomElement = e;
		mapAttributes(e);
		
		handlecenter = new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this, canvas);
		setHandleLocation();
		canvas.addElement(handlecenter);
	}
	
	public void mapAttributes(Element e)
	{
		Iterator it = e.getAttributes().iterator();
		while(it.hasNext()) {
			Attribute at = (Attribute)it.next();
			int index = attributes.indexOf(at.getName());
			String value = at.getValue();
			switch(index) {
					case 0: //Name
						name = value; break;
					case 1: //Organism
						organism = value; break;
					case 2: //Data-Source
						dataSource = value; break;
					case 3: //Version
						version = value; break;
					case 4: //Author
						author = value; break;
					case 5: //Maintained-By
						maintainedBy = value; break;
					case 6: //Email
						email = value; break;
					case 7: //Availability
						availability = value; break;
					case 8: //Last-Modified
						lastModified = value; break;
					case 9: //BoardWidth 
						boardWidth = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 10: //BoardHeight
						boardHeight = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 11: //WindowWidth
						windowWidth = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 12: //WindowHeight
						windowHeight = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 13: //MapInfoLeft
						mapInfoLeft = Integer.parseInt(value) / GmmlData.GMMLZOOM; 
						break;
					case 14: //mapInfoTop
						mapInfoTop = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
			}
		}
		// Map the graphics attributes
		Element graphics = e.getChild("Graphics");
		if(graphics != null) {
			mapAttributes(graphics);
		}
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
		
		Object[] values = new Object[] {name, organism, dataSource, version,
				author, maintainedBy, email, availability, lastModified,
				boardWidth, boardHeight, windowWidth, windowHeight, mapInfoLeft, mapInfoTop
				};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}

	public void updateFromPropItems()
	{
		markDirty();
		name			= (String)propItems.get(attributes.get(0));
		organism		= (String)propItems.get(attributes.get(1));
		dataSource		= (String)propItems.get(attributes.get(2));
		version			= (String)propItems.get(attributes.get(3));
		author			= (String)propItems.get(attributes.get(4));
		maintainedBy	= (String)propItems.get(attributes.get(5));
		email			= (String)propItems.get(attributes.get(6));
		availability	= (String)propItems.get(attributes.get(7));
		lastModified	= (String)propItems.get(attributes.get(8));
		boardWidth		= (Integer)propItems.get(attributes.get(9));
		boardHeight		= (Integer)propItems.get(attributes.get(10));
		windowWidth		= (Integer)propItems.get(attributes.get(11));
		windowHeight	= (Integer)propItems.get(attributes.get(12));
		mapInfoLeft	= (Integer)propItems.get(attributes.get(13));
		mapInfoTop		= (Integer)propItems.get(attributes.get(14));
		markDirty();
		setHandleLocation();
		canvas.redrawDirtyRect();
	}
	
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("Name", name);
			jdomElement.setAttribute("Organism", organism);
			jdomElement.setAttribute("Data-Source", dataSource);
			jdomElement.setAttribute("Version", version);
			jdomElement.setAttribute("Author", author);
			jdomElement.setAttribute("Maintained-By", maintainedBy);
			jdomElement.setAttribute("Email", email);
			jdomElement.setAttribute("Availability", availability);
			jdomElement.setAttribute("Last-Modified", lastModified);
			
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("BoardWidth", Integer.toString((int)boardWidth * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("BoardHeight", Integer.toString((int)boardHeight * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("WindowWidth", Integer.toString((int)windowWidth * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("WindowHeight", Integer.toString((int)windowHeight * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("MapInfoLeft", Integer.toString(mapInfoLeft * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("MapInfoTop", Integer.toString(mapInfoTop * GmmlData.GMMLZOOM));
			}
		}
	}
	
	public void adjustToZoom(double factor) 
	{
		mapInfoLeft		*= factor;
		mapInfoTop		*= factor;
		fontSizeDouble *= factor;
		fontSize = (int)this.fontSizeDouble;
	}
	
	public boolean intersects(Rectangle2D.Double r) 
	{
		Rectangle2D rect = new Rectangle2D.Double(mapInfoLeft, mapInfoTop, sizeX, sizeY);
		return rect.intersects(r);
	}
	
	public boolean isContain(Point2D p) 
	{
		Rectangle2D rect = new Rectangle2D.Double(mapInfoLeft, mapInfoTop, sizeX, sizeY);
		return rect.contains(p);
	}
	
	public Rectangle getBounds()
	{
		return new Rectangle(mapInfoLeft, mapInfoTop, sizeX, sizeY);
	}
	
	protected void moveBy(double dx, double dy)
	{
		markDirty();
		mapInfoTop  += dy;
		mapInfoLeft += dx;
		setHandleLocation();
		markDirty();
	}
	
	private void setHandleLocation()
	{
		handlecenter.setLocation(mapInfoLeft, mapInfoTop);
	}
	
	public void draw(PaintEvent e) 
	{
		draw(e, e.gc);
	}
	
	public void draw(PaintEvent e, GC buffer) 
	{		
		sizeX = 1; //Reset sizeX
		
		Font fBold = new Font(e.display, fontName, fontSize, SWT.BOLD);
		Font fNormal = new Font(e.display, fontName, fontSize, SWT.NONE);
		
		if (isSelected())
		{
			buffer.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
		}
		else 
		{
			buffer.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
		}
				
		//Draw Name, Organism, Data-Source, Version, Author, Maintained-by, Email, Availability and last modified
		String[][] text = new String[][] {
				{"Name: ", name},
				{"Maintained by: ", maintainedBy},
				{"Email: ", email},
				{"Availability: ", availability},
				{"Last modified: ", lastModified},
				{"Organism: ", organism},
				{"Data Source: ", dataSource}};
		int shift = 0;
		for(String[] s : text)
		{
			if(s == null || s[1].equals("")) continue; //Skip empty labels
			buffer.setFont(fBold);
			Point labelSize = buffer.textExtent(s[0], SWT.DRAW_TRANSPARENT);
			buffer.drawString(s[0], mapInfoLeft, mapInfoTop + shift, true);
			buffer.setFont(fNormal);
			Point infoSize = buffer.textExtent(s[1], SWT.DRAW_TRANSPARENT);
			buffer.drawString(s[1], mapInfoLeft + labelSize.x, mapInfoTop + shift, true);
			shift += Math.max(infoSize.y, labelSize.y);
			sizeX = Math.max(sizeX, infoSize.x + labelSize.x);
		}
		sizeY = shift;
		
		fBold.dispose();
		fNormal.dispose();
		System.out.println(sizeX + ", " + sizeY);
	}
	
	public Vector<GmmlHandle> getHandles()
	{
		Vector handles = new Vector<GmmlHandle>();
		handles.add(handlecenter);
		return handles;
	}
}
