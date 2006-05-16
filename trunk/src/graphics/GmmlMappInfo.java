package graphics;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.jdom.Attribute;
import org.jdom.Element;

import data.GmmlData;

public class GmmlMappInfo extends GmmlGraphics {
	
	final static List attributes =  Arrays.asList(new String[] {
		"Name", "Organism", "Data-Source", "Version", "Author",
		"Maintained-By", "Email", "Availability", "Last-Modified",
		"BoardWidth", "BoardHeight", "WindowWidth", "WindowHeight"
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
	
	Element jdomElement;
	
	GmmlDrawing drawing;
	
	public GmmlMappInfo(Element e) 
	{
		jdomElement = e;
		mapAttributes(e);
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
				boardWidth, boardHeight, windowWidth, windowHeight
				};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}

	public void updateFromPropItems()
	{
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
			}
		}
	}
	
	public void adjustToZoom(double factor) {}
	
	public boolean intersects(Rectangle2D.Double r) 
	{
		return false;
	}
	
	public boolean isContain(Point2D p) 
	{
		return false;
	}
	
	public Rectangle getBounds()
	{
		return new Rectangle();
	}
	
	public void draw(PaintEvent e) {}
	public void draw(PaintEvent e, GC buffer) {}
	public Vector<GmmlHandle> getHandles()
	{
		return null;
	}
}
