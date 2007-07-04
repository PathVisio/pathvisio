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
package org.pathvisio.view.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.VPathwayWrapper;

public class VPathwaySwing extends JPanel implements VPathwayWrapper,
		MouseMotionListener, MouseListener, KeyListener, VPathwayListener {
	VPathway child;

	JScrollPane container;

	public VPathwaySwing(JScrollPane parent) {
		super();
		if (parent == null)
			throw new IllegalArgumentException("parent is null");
		this.container = parent;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		setFocusable(true);
		setRequestFocusEnabled(true);
	}

	public void setChild(VPathway c) {
		child = c;
		child.addVPathwayListener(this);
	}

	public Rectangle getVBounds() {
		return getBounds();
	}

	public Dimension getVSize() {
		return getPreferredSize();
	}

	public Dimension getViewportSize() {
		if (container instanceof JScrollPane) {
			return ((JScrollPane) container).getViewport().getExtentSize();
		}
		return getSize();
	}

	public void redraw() {
		repaint();
	}

	protected void paintComponent(Graphics g) {
		child.draw((Graphics2D) g);
	}

	public void redraw(Rectangle r) {
		repaint(r);
	}

	public void setVSize(Dimension size) {
		setSize(size);
		setMaximumSize(size);
		setMinimumSize(size);
		setPreferredSize(size);
	}

	public void setVSize(int w, int h) {
		setVSize(new Dimension(w, h));
	}

	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
			child.mouseDoubleClick(new SwingMouseEvent(e));
		}
	}

	public void mouseEntered(MouseEvent e) {
		child.mouseEnter(new SwingMouseEvent(e));
	}

	public void mouseExited(MouseEvent e) {
		child.mouseExit(new SwingMouseEvent(e));

	}

	public void mousePressed(MouseEvent e) {
		child.mouseDown(new SwingMouseEvent(e));
	}

	public void mouseReleased(MouseEvent e) {
		child.mouseUp(new SwingMouseEvent(e));
	}

	public void keyPressed(KeyEvent e) {
		System.out.println("Key pressed........!");
		child.keyPressed(new SwingKeyEvent(e));
	}

	public void keyReleased(KeyEvent e) {
		System.out.println("Key released......!");
		child.keyReleased(new SwingKeyEvent(e));
	}

	public void keyTyped(KeyEvent e) {
		// TODO: find out how to handle this one
		System.out.println("Key types.....!");
	}

	public void mouseDragged(MouseEvent e) {
		child.mouseMove(new SwingMouseEvent(e));
	}

	public void mouseMoved(MouseEvent e) {
		child.mouseMove(new SwingMouseEvent(e));
	}

	public void registerKeyboardAction(KeyStroke k, Action a) {
		super.registerKeyboardAction(a, k, WHEN_IN_FOCUSED_WINDOW);
	}
	
	public VPathway createVPathway() {
		setChild(new VPathway(this));
		return child;
	}

	public void vPathwayEvent(VPathwayEvent e) {
		if(e.getType() == VPathwayEvent.MODEL_LOADED) {
			if(e.getSource() == child) {
				container.setViewportView(this);
				container.getViewport().setBackground(Color.GRAY);
				container.revalidate();
			}
		}
	}

}
