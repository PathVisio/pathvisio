package org.pathvisio.view;

import java.awt.Dimension;
import java.awt.Rectangle;

public abstract interface VPathwayWrapper {		
	public void redraw();
	public void redraw(Rectangle r);
	public void setVSize(Dimension size);
	public void setVSize(int w, int h);
	public Dimension getVSize();
	public Rectangle getVBounds();
	public Dimension getViewportSize();
	
	public VPathway createVPathway();
}
