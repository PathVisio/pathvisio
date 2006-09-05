package graphics;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import data.*;

public class GmmlMappInfo extends GmmlGraphics {
	
	//Elements not stored in gmml
	String fontName			= "Times New Roman";
	String fontWeight		= "regular";
	int fontSize			= 10;
	double fontSizeDouble = fontSize;
	
	int sizeX = 1;
	int sizeY = 1; //Real size is calculated on first call to draw()
	
	public GmmlMappInfo (GmmlDrawing canvas, GmmlDataObject o) {
		super(canvas, o);
		canvas.setMappInfo(this);
		drawingOrder = GmmlDrawing.DRAW_ORDER_MAPPINFO;		
	}

	public void setName(String name) { 
		markDirty();
		gdata.setMapInfoName(name);  
		markDirty();
		canvas.redrawDirtyRect();
	}
	
	public void setBoardSize(Point size) {
		gdata.setBoardWidth (size.x);
		gdata.setBoardHeight (size.y);
		canvas.setSize((int)gdata.getBoardWidth(), (int)gdata.getBoardHeight());
	}
	
	public void setWindowSize(Point size) {
		gdata.setWindowWidth (size.x);
		gdata.setWindowHeight (size.y);
//		canvas.gmmlVision.getShell().setSize(windowWidth, windowHeight);
	}
	
	public Point getBoardSize() { return new Point((int)gdata.getBoardWidth(), (int)gdata.getBoardHeight()); }
	
	public void adjustToZoom(double factor) 
	{
		gdata.setMapInfoLeft((int)(gdata.getMapInfoLeft() * factor));
		gdata.setMapInfoTop((int)(gdata.getMapInfoTop() * factor));
		fontSizeDouble *= factor;
		fontSize = (int)this.fontSizeDouble;
	}
	
	public boolean intersects(Rectangle2D.Double r) 
	{
		Rectangle2D rect = new Rectangle2D.Double(gdata.getMapInfoLeft(), gdata.getMapInfoTop(), sizeX, sizeY);
		return rect.intersects(r);
	}
	
	public boolean isContain(Point2D p) 
	{
		Rectangle2D rect = new Rectangle2D.Double(gdata.getMapInfoLeft(), gdata.getMapInfoTop(), sizeX, sizeY);
		return rect.contains(p);
	}
	
	public Rectangle getBounds()
	{
		return new Rectangle(gdata.getMapInfoLeft(), gdata.getMapInfoTop(), sizeX, sizeY);
	}
	
	protected void moveBy(double dx, double dy)
	{
		markDirty();
		gdata.setMapInfoTop (gdata.getMapInfoTop()  + (int)dy);
		gdata.setMapInfoLeft (gdata.getMapInfoLeft() + (int)dx);
		markDirty();
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
				{"Name: ", gdata.getMapInfoName()},
				{"Maintained by: ", gdata.getMaintainedBy()},
				{"Email: ", gdata.getEmail()},
				{"Availability: ", gdata.getAvailability()},
				{"Last modified: ", gdata.getLastModified()},
				{"Organism: ", gdata.getOrganism()},
				{"Data Source: ", gdata.getDataSource()}};
		int shift = 0;
		int mapInfoLeft = gdata.getMapInfoLeft();
		int mapInfoTop = gdata.getMapInfoTop();
		for(String[] s : text)
		{
			if(s[1] == null || s[1].equals("")) continue; //Skip empty labels
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
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
 