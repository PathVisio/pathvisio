package visualization.plugins;

import graphics.GmmlGraphics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import visualization.Visualization;
import data.GmmlDataObject;

/**
 * Provides label for Gene Product
 * @author thomas
 *
 */
public class ColorByLinkPlugin extends VisualizationPlugin {	
	static final String NAME = "Graphical link color";
	static final String DESCRIPTION = 
		"This plugin colors objects depending on their graphRef and graphId attributes";
	
	static final int refMarkRadius = 12;
	static final int refMarkAlpha = 128;
	
	HashMap<Integer, RGB> id2col;
	Random rnd;
	
	public ColorByLinkPlugin(Visualization v) {
		super(v);
		setDisplayOptions(DRAWING);
		setIsGeneric(true);
		setIsConfigurable(false);
		
		id2col = new HashMap<Integer, RGB>();
		rnd = new Random();
	}

	public String getName() { return NAME; }
	public String getDescription() { return DESCRIPTION; }
	
	public void createSidePanelComposite(Composite parent) { }

	public void draw(GmmlGraphics g, PaintEvent e, GC buffer) {
		GmmlDataObject gd = g.getGmmlData();
		int[] ids = parseIds(gd);
		if(ids[0] != 0) { //This is a shape
			drawShape(ids[0], g, e, buffer);
			return;
		}
		if(ids[1] != 0) {
			drawLineStart(ids[1], g, e, buffer);
		}
		if(ids[2] != 0) {
			drawLineEnd(ids[2], g, e, buffer);
		}
	}
	
	void drawLineStart(int id, GmmlGraphics g, PaintEvent e, GC buffer) {
		GmmlDataObject gd = g.getGmmlData();
		drawRefMark( 
				id,			
				(int)gd.getStartX() - refMarkRadius/2, 
				(int)gd.getStartY() - refMarkRadius/2,
				e, buffer);
	}
	
	void drawLineEnd(int id, GmmlGraphics g, PaintEvent e, GC buffer) {
		GmmlDataObject gd = g.getGmmlData();
		drawRefMark( 
				id,			
				(int)gd.getEndX() - refMarkRadius/2, 
				(int)gd.getEndY() - refMarkRadius/2,
				e, buffer);
	}
	
	void drawRefMark(int id, int x, int y, PaintEvent e, GC buffer) {
		int origAlpha = buffer.getAlpha();
		Color c = new Color(e.display, getRGB(id));
		buffer.setBackground(c);
		buffer.setAlpha(refMarkAlpha);
		buffer.fillOval(x, y, refMarkRadius, refMarkRadius);
		buffer.setAlpha(origAlpha);
	}
	
	void drawShape(int id, GmmlGraphics g, PaintEvent e, GC buffer) {
		GmmlDataObject gd = g.getGmmlData();
		RGB oldRGB = gd.getColor();
		gd.dontFireEventsOnce();
		gd.setColor(getRGB(id));
		g.draw(e, buffer);
		gd.dontFireEventsOnce();
		gd.setColor(oldRGB);
	}
	
	RGB getRGB(int id) {
		RGB rgb = id2col.get(id);
		if(rgb == null) {
			rgb = randomRGB();
			id2col.put(id, rgb);
		}
		return rgb;
	}
	
	RGB randomRGB() {
		int rgb = java.awt.Color.HSBtoRGB(rnd.nextFloat(), 1, 1);
		java.awt.Color c = new java.awt.Color(rgb);
		return new RGB(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	int[] parseIds(GmmlDataObject gd) {
		int[] ids = new int[3];
		try { ids[0] = Integer.parseInt(gd.getGraphId()); } catch(NumberFormatException e) {}
		try { ids[1] = Integer.parseInt(gd.getStartGraphRef()); } catch(NumberFormatException e) {}
		try { ids[2] = Integer.parseInt(gd.getEndGraphRef()); } catch(NumberFormatException e) {}
		return ids;
	}
		
	public Composite getToolTipComposite(Composite parent, GmmlGraphics g) { return null; }
	
	public void updateSidePanel(Collection<GmmlGraphics> objects) {	}
}
