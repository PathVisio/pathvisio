// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.cytoscape;

import ding.view.DGraphView;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import org.pathvisio.core.model.PathwayElement;

/**
 * Cytoscape rendering of a GPML Label.
 * This is pure graphical annotation, not part of the graph.
 */
public class Label extends Annotation {

	public Label(PathwayElement pwElm, DGraphView view) {
		super(pwElm, view);
	}

	public Shape getVOutline() {
//		FontMetrics fm = getFontMetrics(getVFont());
//		Rectangle2D r = fm.getStringBounds(pwElm.getTextLabel(), 
//				image != null ? image.createGraphics() : getGraphics());
//		return new Rectangle2D.Double(getVLeft(), getVTop(), r.getWidth(), r.getHeight());
		return new Rectangle(getVLeft(), getVTop(), getVWidth(), getVHeight());
	}
	
	private Font getVFont() {
		int style = pwElm.isBold() ? Font.BOLD : Font.PLAIN;
		style |= pwElm.isItalic() ? Font.ITALIC : Font.PLAIN;
		return new Font(pwElm.getFontName(), style, (int)GpmlPlugin.mToV(pwElm.getMFontSize() * scaleFactor));
	}

	public void doPaint(Graphics2D g2d) {
		Rectangle b = getBounds();
		g2d.setFont(getVFont());
		g2d.setColor(pwElm.getColor());
		
		g2d.drawString(pwElm.getTextLabel(), b.x, b.y + b.height / 2);
		
		g2d.dispose();
	}
	
	double scaleFactor = 1;
	
	public void viewportChanged(int w, int h, double newXCenter, double newYCenter, double newScaleFactor) {
		scaleFactor = newScaleFactor;
		super.viewportChanged(w, h, newXCenter, newYCenter, newScaleFactor);
		
	}
}
