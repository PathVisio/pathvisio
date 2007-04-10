// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
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
import visualization.plugins.VisualizationPlugin;
import data.gpml.GmmlDataObject;
import util.ColorConverter;

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
	
	HashMap<String, RGB> id2col;
	Random rnd;
	
	public ColorByLinkPlugin(Visualization v) {
		super(v);
		setDisplayOptions(DRAWING);
		setIsGeneric(true);
		setIsConfigurable(false);
		
		id2col = new HashMap<String, RGB>();
		rnd = new Random();
	}

	public String getName() { return NAME; }
	public String getDescription() { return DESCRIPTION; }
	
	public void initSidePanel(Composite parent) { }

	public void visualizeOnDrawing(GmmlGraphics g, PaintEvent e, GC buffer) {
		GmmlDataObject gd = g.getGmmlData();
		String[] ids = parseIds(gd);
		if(ids[0] != null) { //This is a shape
			drawShape(ids[0], g, e, buffer);
			return;
		}
		if(ids[1] != null) {
			drawLineStart(ids[1], g, e, buffer);
		}
		if(ids[2] != null) {
			drawLineEnd(ids[2], g, e, buffer);
		}
	}
	
	void drawLineStart(String id, GmmlGraphics g, PaintEvent e, GC buffer) {
		GmmlDataObject gd = g.getGmmlData();
		drawRefMark( 
				id,			
				// TODO: this should be in visual coords
				(int)gd.getMStartX() - refMarkRadius/2, 
				(int)gd.getMStartY() - refMarkRadius/2,
				e, buffer);
	}
	
	void drawLineEnd(String id, GmmlGraphics g, PaintEvent e, GC buffer) {
		GmmlDataObject gd = g.getGmmlData();
		drawRefMark( 
				id,			
				// TODO: this should be in visual coords
				(int)gd.getMEndX() - refMarkRadius/2, 
				(int)gd.getMEndY() - refMarkRadius/2,
				e, buffer);
	}
	
	void drawRefMark(String id, int x, int y, PaintEvent e, GC buffer) {
		int origAlpha = buffer.getAlpha();
		Color c = new Color(e.display, getRGB(id));
		buffer.setBackground(c);
		buffer.setAlpha(refMarkAlpha);
		buffer.fillOval(x, y, refMarkRadius, refMarkRadius);
		buffer.setAlpha(origAlpha);
	}
	
	void drawShape(String id, GmmlGraphics g, PaintEvent e, GC buffer) {
		GmmlDataObject gd = g.getGmmlData();
		RGB oldRGB = ColorConverter.toRGB(gd.getColor());
		gd.dontFireEvents(2);
		gd.setColor(ColorConverter.fromRGB(getRGB(id)));
		g.draw(e, buffer);
		gd.setColor(ColorConverter.fromRGB(oldRGB));
	}
	
	RGB getRGB(String id) {
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
	
	String[] parseIds(GmmlDataObject gd) {
		String[] ids = new String[3];
		String gid = gd.getGraphId();
		String sr = gd.getStartGraphRef();
		String er =  gd.getEndGraphRef();
		if(gid != null) ids[0] = gid.equals("") ? null : gid;
		if(sr != null) 	ids[1] = sr.equals("") ? null : sr;
		if(er != null) 	ids[2] = er.equals("") ? null : er;
		return ids;
	}
		
	public Composite visualizeOnToolTip(Composite parent, GmmlGraphics g) { return null; }
	
	public void visualizeOnSidePanel(Collection<GmmlGraphics> objects) {	}
}
