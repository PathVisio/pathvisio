package org.pathvisio.cytoscape;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import org.pathvisio.model.PathwayElement;

import ding.view.DGraphView;

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
		
		g2d.drawString(pwElm.getTextLabel(), 0, b.height / 2);
		
		g2d.dispose();
	}
	
	double scaleFactor = 1;
	
	public void viewportChanged(int w, int h, double newXCenter, double newYCenter, double newScaleFactor) {
		scaleFactor = newScaleFactor;
		super.viewportChanged(w, h, newXCenter, newYCenter, newScaleFactor);
		
	}
}
