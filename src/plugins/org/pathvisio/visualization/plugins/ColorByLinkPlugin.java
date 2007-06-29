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
package org.pathvisio.visualization.plugins;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.swt.widgets.Composite;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;

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
		PathwayElement gd = g.getGmmlData();
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
		PathwayElement gd = g.getGmmlData();
		drawRefMark( 
				id,			
				// TODO: this should be in visual coords
				(int)gd.getMStartX() - refMarkRadius/2, 
				(int)gd.getMStartY() - refMarkRadius/2,
				g2d);
	}
	
	void drawLineEnd(String id, Graphics g, Graphics2D g2d) {
		PathwayElement gd = g.getGmmlData();
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
		PathwayElement gd = g.getGmmlData();
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
		
	public Composite visualizeOnToolTip(Composite parent, Graphics g) { return null; }
	
	public void visualizeOnSidePanel(Collection<Graphics> objects) {	}
}
