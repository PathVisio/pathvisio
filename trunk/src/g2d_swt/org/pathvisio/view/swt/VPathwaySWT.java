package org.pathvisio.view.swt;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.view.InputEvent;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayWrapper;

public class VPathwaySWT extends Canvas implements VPathwayWrapper, PaintListener, 
				MouseListener, KeyListener, MouseMoveListener, MouseTrackListener {
	final SWTGraphics2DRenderer renderer = new SWTGraphics2DRenderer();
	
	private VPathway child;
	
	public VPathwaySWT(Composite parent, int style) {
		super(parent, style);
	}

	public VPathway createVPathway() {
		setChild(new VPathway(this));
		return child;
	}
	
	protected void setChild(VPathway c) {
		child = c;
		addPaintListener(this);
		addMouseListener(this);
		addMouseMoveListener(this);
		addMouseTrackListener(this);
		addKeyListener(this);
	}
	
	public void redraw(Rectangle r) {
		redraw(r.x, r.y, r.width, r.height, false);
	}

	public void setVSize(Dimension size) {
		setVSize(size.width, size.height);
	}

	public void setVSize(int w, int h) {
		setSize(w, h);
	}
	
	public Dimension getVSize() {
		org.eclipse.swt.graphics.Point p = getSize();
		return new Dimension(p.x, p.y);
	}
	
	public Rectangle getVBounds() {
		org.eclipse.swt.graphics.Rectangle b = getBounds();
		return new Rectangle(b.x, b.y, b.width, b.height);
	}
	
	public Dimension getViewportSize() {
		org.eclipse.swt.graphics.Point scs = SwtEngine.getWindow().sc.getSize();
		return new Dimension(scs.x, scs.y);
	}
	
	//Method 1: transfer from BufferedImage
	public void paintControl(PaintEvent e) {
		GC gc = e.gc; // gets the SWT graphics context from the event

		gc.setClipping(e.x, e.y, e.width, e.height);
		
		renderer.prepareRendering(gc); // prepares the Graphics2D renderer

		Graphics2D g2d = renderer.getGraphics2D();
			
		child.draw(g2d, new Rectangle(e.x, e.y, e.width, e.height));
		
		renderer.render(gc);
	}

	//Method 2: use Graphics2D extension
//	public void paintControl(PaintEvent e) {
//		GC gc = e.gc; // gets the SWT graphics context from the event
//		
//		Graphics2D g2d = new SWTGraphics2D(gc, gc.getDevice());
//		child.draw(g2d, new Rectangle(e.x, e.y, e.width, e.height));
//		
//	}
	
	public static int convertStateMask(int swtMask) {
		int newMask = 0;
		newMask = addModifier(swtMask, SWT.CTRL, newMask, InputEvent.M_CTRL);
		newMask = addModifier(swtMask, SWT.ALT, newMask, InputEvent.M_ALT);
		//newMask = addModifier(stateMask, SWT.)//TODO: find SWT mapping for M_META
		newMask = addModifier(swtMask, SWT.SHIFT, newMask, InputEvent.M_SHIFT);
		return newMask;
	}

	private static int addModifier(int swtMask, int swtModifier, int pvMask, int pvModifier) {
		if((swtMask & swtModifier) != 0) {
			pvMask |= pvModifier;
		}
		return pvMask;
	}
	
	public void mouseDoubleClick(MouseEvent e) {
		SwtMouseEvent pve = new SwtMouseEvent(
				e, org.pathvisio.view.MouseEvent.MOUSE_CLICK, 2);
		child.mouseDoubleClick(pve);
	}

	public void mouseDown(MouseEvent e) {
		SwtMouseEvent pve = new SwtMouseEvent(
				e, org.pathvisio.view.MouseEvent.MOUSE_DOWN, 0);
		child.mouseDown(pve);
	}

	public void mouseUp(MouseEvent e) {
		SwtMouseEvent pve = new SwtMouseEvent(
				e, org.pathvisio.view.MouseEvent.MOUSE_UP, 0);
		child.mouseUp(pve);
	}

	public void keyPressed(KeyEvent e) {
		SwtKeyEvent pve = new SwtKeyEvent(e, org.pathvisio.view.KeyEvent.KEY_PRESSED);
		child.keyPressed(pve);
	}

	public void keyReleased(KeyEvent e) {
		SwtKeyEvent pve = new SwtKeyEvent(e, org.pathvisio.view.KeyEvent.KEY_RELEASED);
		child.keyReleased(pve);
	}

	public void mouseMove(MouseEvent e) {
		SwtMouseEvent pve = new SwtMouseEvent(
				e, org.pathvisio.view.MouseEvent.MOUSE_MOVE, 0);
		child.mouseMove(pve);
	}

	public void mouseEnter(MouseEvent e) {
		SwtMouseEvent pve = new SwtMouseEvent(
				e, org.pathvisio.view.MouseEvent.MOUSE_ENTER, 0);
		child.mouseEnter(pve);
	}

	public void mouseExit(MouseEvent e) {
		SwtMouseEvent pve = new SwtMouseEvent(
				e, org.pathvisio.view.MouseEvent.MOUSE_ENTER, 0);
		child.mouseExit(pve);
	}

	public void mouseHover(MouseEvent e) {
		SwtMouseEvent pve = new SwtMouseEvent(
				e, org.pathvisio.view.MouseEvent.MOUSE_HOVER, 0);
		child.mouseHover(pve);	
	}
}