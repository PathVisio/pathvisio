package org.pathvisio.view;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class VPathwayWrapperBase implements VPathwayWrapper {

	public void copyToClipboard(Pathway source, List<PathwayElement> result) {
		
	}

	public VPathway createVPathway() {
		return new VPathway(this);
	}

	public Rectangle getVBounds() {
		return new Rectangle();
	}

	public Dimension getVSize() {
		return new Dimension();
	}

	public Dimension getViewportSize() {
		return new Dimension();
	}

	public void redraw() {
		
	}

	public void redraw(Rectangle r) {
		
	}

	public void registerKeyboardAction(KeyStroke k, Action a) {
		
	}

	public void setVSize(Dimension size) {
		
	}

	public void setVSize(int w, int h) {
		
	}

	public void pasteFromClipboard() {
	
	}

}
