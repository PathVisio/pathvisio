

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.swt.widgets.Composite;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.plugins.VisualizationPlugin;

/**
 * Colors drawing-objects according to their graphId / graphRef values
 * @author thomas
 *
 */
public class ColorByLinkPlugin extends VisualizationPlugin {	
	static final String NAME = "Graphical link color";
	static final String DESCRIPTION = 
		"This plugin colors objects depending on their graphRef and graphId attributes";
	
	static final int refMarkRadius = 12;
	static final int refMarkAlpha = 128;
	
	HashMap<String, Color> id2col;
	Random rnd;
	
	public ColorByLinkPlugin(Visualization v) {
		super(v);
		setDisplayOptions(DRAWING);
		setIsGeneric(true);
		setIsConfigurable(false);
		
		id2col = new HashMap<String, Color>();
		rnd = new Random();
	}

	public String getName() { return NAME; }
	public String getDescription() { return DESCRIPTION; }
	
	public void initSidePanel(Composite parent) { }

	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		PathwayElement gd = g.getPathwayElement();
		String[] ids = parseIds(gd);
		if(ids[0] != null) { //This is a shape
			drawShape(ids[0], g, g2d);
			return;
		}
		if(ids[1] != null) {
			drawLineStart(ids[1], g, g2d);
		}
		if(ids[2] != null) {
			drawLineEnd(ids[2], g, g2d);
		}
	}
	
	void drawLineStart(String id, Graphics g, Graphics2D g2d) {
		PathwayElement gd = g.getPathwayElement();
		drawRefMark( 
				id,			
				// TODO: this should be in visual coords
				(int)gd.getMStartX() - refMarkRadius/2, 
				(int)gd.getMStartY() - refMarkRadius/2,
				g2d);
	}
	
	void drawLineEnd(String id, Graphics g, Graphics2D g2d) {
		PathwayElement gd = g.getPathwayElement();
		drawRefMark( 
				id,			
				// TODO: this should be in visual coords
				(int)gd.getMEndX() - refMarkRadius/2, 
				(int)gd.getMEndY() - refMarkRadius/2,
				g2d);
	}
	
	void drawRefMark(String id, int x, int y, Graphics2D g2d) {
		Color c = getRGB(id);
		g2d.setColor(c);
		g2d.fillOval(x, y, refMarkRadius, refMarkRadius);
	}
	
	void drawShape(String id, Graphics g, Graphics2D g2d) {
		PathwayElement gd = g.getPathwayElement();
		Color oldRGB = gd.getColor();
		gd.dontFireEvents(2);
		gd.setColor(getRGB(id));
		g.draw(g2d);
		gd.setColor(oldRGB);
	}
	
	Color getRGB(String id) {
		Color rgb = id2col.get(id);
		if(rgb == null) {
			rgb = randomRGB();
			id2col.put(id, rgb);
		}
		return rgb;
	}
	
	Color randomRGB() {
		int rgb = java.awt.Color.HSBtoRGB(rnd.nextFloat(), 1, 1);
		Color c = new Color(rgb);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), refMarkAlpha);
	}
	
	String[] parseIds(PathwayElement gd) {
		String[] ids = new String[3];
		String gid = gd.getGraphId();
		String sr = gd.getStartGraphRef();
		String er =  gd.getEndGraphRef();
		if(gid != null) ids[0] = gid.equals("") ? null : gid;
		if(sr != null) 	ids[1] = sr.equals("") ? null : sr;
		if(er != null) 	ids[2] = er.equals("") ? null : er;
		return ids;
	}
		
	public Component visualizeOnToolTip(Graphics g) { return null; }
	
	public void visualizeOnSidePanel(Collection<Graphics> objects) {	}
}
