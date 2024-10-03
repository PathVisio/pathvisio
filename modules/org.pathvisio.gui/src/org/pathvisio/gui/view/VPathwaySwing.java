/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.gui.view;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import org.pathvisio.core.gui.PathwayTransferable;
import org.pathvisio.core.gui.SwingKeyEvent;
import org.pathvisio.core.gui.SwingMouseEvent;
import org.pathvisio.core.gui.ToolTipProvider;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.GraphicsShape;
import org.pathvisio.core.view.Handle;
import org.pathvisio.core.view.Label;
import org.pathvisio.core.view.VElementMouseEvent;
import org.pathvisio.core.view.VElementMouseListener;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.core.view.VPathwayEvent;
import org.pathvisio.core.view.VPathwayListener;
import org.pathvisio.core.view.VPathwayWrapper;
import org.pathvisio.gui.MainPanel;
import org.pathvisio.gui.dnd.PathwayImportHandler;


/**
 * swing-dependent implementation of VPathway.
 */
public class VPathwaySwing extends JPanel implements VPathwayWrapper,
MouseMotionListener, MouseListener, KeyListener, VPathwayListener, VElementMouseListener, MouseWheelListener {

	protected VPathway child;

	protected JScrollPane container;

	public VPathwaySwing(JScrollPane parent) {
		super();
		if (parent == null)
			throw new IllegalArgumentException("parent is null");
		this.container = parent;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addMouseWheelListener(this);

		setFocusable(true);
		setRequestFocusEnabled(true);
		setTransferHandler(new PathwayImportHandler());

		setDoubleBuffered(
				PreferenceManager.getCurrent().getBoolean(
						GlobalPreference.ENABLE_DOUBLE_BUFFERING));
	}

	public void setChild(VPathway c) {
		child = c;
		child.addVPathwayListener(this);
		child.addVElementMouseListener(this);
	}

	public VPathway getChild() {
		return child;
	}

	public Dimension getViewportSize() {
		if (container instanceof JScrollPane) {
			return ((JScrollPane) container).getViewport().getExtentSize();
		}
		return getSize();
	}

	/**
	 * Schedule redraw of the entire visible area
	 */
	public void redraw() 
	{
		repaint();
	}

	/**
	 * Draw immediately
	 */
	protected void paintComponent(Graphics g)
	{
		if(child != null) child.draw((Graphics2D) g);
	}

	/**
	 * Schedule redraw of a certain part of the pathway
	 */
	public void redraw(Rectangle r) 
	{
		repaint(r);
	}

	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
			child.mouseDoubleClick(new SwingMouseEvent(e));
		}
	}

	public void mouseEntered(MouseEvent e) {
		//requestFocus(); //TODO: test if this is needed in applet.
		child.mouseEnter(new SwingMouseEvent(e));
	}

	public void mouseExited(MouseEvent e) {
		child.mouseExit(new SwingMouseEvent(e));

	}

	public void mousePressed(MouseEvent e) {
		requestFocus();
		child.mouseDown(new SwingMouseEvent(e));
	}

	public void mouseReleased(MouseEvent e) {
		child.mouseUp(new SwingMouseEvent(e));
	}

	public void keyPressed(KeyEvent e) {
		child.keyPressed(new SwingKeyEvent(e));
	}

	public void keyReleased(KeyEvent e) {
		child.keyReleased(new SwingKeyEvent(e));
	}

	public void keyTyped(KeyEvent e) {
		// TODO: find out how to handle this one
	}

	public void mouseDragged(MouseEvent e) {
		Rectangle r = container.getViewport().getViewRect();
		final int stepSize = 10;
		int newx = (int)r.getMinX();
		int newy = (int)r.getMinY();
		// scroll when dragging out of view
		if (e.getX() > r.getMaxX())
		{
			newx += stepSize;
		}
		if (e.getX() < r.getMinX())
		{
			newx = Math.max (newx - stepSize, 0);
		}
		if (e.getY() > r.getMaxY())
		{
			newy += stepSize;
		}
		if (e.getY() < r.getMinY())
		{
			newy = Math.max (newy - stepSize, 0);
		}
		container.getViewport().setViewPosition(
				new Point (newx, newy)
		);
		child.mouseMove(new SwingMouseEvent(e));
	}

	public void mouseMoved(MouseEvent e) {
		child.mouseMove(new SwingMouseEvent(e));
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
	    int notches = e.getWheelRotation();
	    if(notches < 0) {
	    	child.zoomToCursor(child.getPctZoom() * 21 / 20, e.getPoint());
	    } else { 
	    	child.zoomToCursor(child.getPctZoom() * 20 / 21, e.getPoint());
	    }
	    
	    Component comp = container.getParent().getParent();
	    if (comp instanceof MainPanel) ((MainPanel)comp).updateZoomCombo();
	}
	
	public void registerKeyboardAction(KeyStroke k, Action a) {
		super.registerKeyboardAction(a, k, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		//super.registerKeyboardAction(a, k, WHEN_IN_FOCUSED_WINDOW);
	}

	public VPathway createVPathway() {
		setChild(new VPathway(this));
		return child;
	}

	public void vPathwayEvent(VPathwayEvent e) {
		switch(e.getType()) {
		case MODEL_LOADED:
			if(e.getSource() == child) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						container.setViewportView(VPathwaySwing.this);
						resized();
						VPathwaySwing.this.requestFocus();
					}
				});
			}
			break;
		case ELEMENT_HOVER:
			showToolTip(e);
			break;
		}
	}

	/**
	 * paste from clip board with the common shift
	 */
	public void pasteFromClipboard() {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		TransferHandler handler = getTransferHandler();
		handler.importData(this, clip.getContents(this));
	}
	
	/**
	 * paste from clip board at the cursor position
	 */
	public void positionPasteFromClipboard(Point cursorPosition) {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		PathwayImportHandler handler = (PathwayImportHandler) getTransferHandler();
		handler.importDataAtCursorPosition(this, clip.getContents(this), cursorPosition);
	}

	List<PathwayElement> lastCopied;

	public void copyToClipboard(Pathway source, List<PathwayElement> copyElements) {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		clip.setContents(new PathwayTransferable(source, copyElements),
				(PathwayImportHandler)getTransferHandler());
		((PathwayImportHandler)getTransferHandler()).obtainedOwnership();
	}

	Set<ToolTipProvider> toolTipProviders = new HashSet<ToolTipProvider>();

	public void addToolTipProvider(ToolTipProvider p) {
		toolTipProviders.add(p);
	}

	public void showToolTip(final VPathwayEvent e) {
		if(toolTipProviders.size() == 0) return;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				List<VPathwayElement> elements = e.getAffectedElements();
				if(elements.size() > 0) {
					PathwayToolTip tip = new PathwayToolTip(elements);

					if(!tip.hasContent()) return;

					final JWindow w = new JWindow();
					w.setLayout(new BorderLayout());
					w.add(tip, BorderLayout.CENTER);

					Point p = e.getMouseEvent().getLocation();
					SwingUtilities.convertPointToScreen(p, VPathwaySwing.this);
					w.setLocation(p);

					//For some reason the mouse listener only works when
					//adding it directly to the scrollpane (the first child component).
					tip.getComponent(0).addMouseListener(new MouseAdapter() {
						public void mouseExited(MouseEvent e) {
							//Don't dispose if on scrollbars
							if(e.getComponent().contains(e.getPoint()))
								return;
							//Don't dispose if mouse is down (usually when scrolling)
							if((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
								return;
							w.dispose();
						}
					});

					w.setVisible(true);
					w.pack();
				}
			}
		});
	}

	class PathwayToolTip extends JPanel {
		private boolean hasContent;

		public PathwayToolTip(List<VPathwayElement> elements) {
			applyToolTipStyle(this);
			setLayout(new BorderLayout());
			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref"));
			for(ToolTipProvider p : toolTipProviders) {
				Component c = p.createToolTipComponent(this, elements);
				if(c != null) {
					hasContent = true;
					builder.append(c);
					builder.nextLine();
				}
			}

			JPanel contents = builder.getPanel();
			applyToolTipStyle(contents);
			JScrollPane scroll = new JScrollPane(contents, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			int w = contents.getPreferredSize().width +
				scroll.getVerticalScrollBar().getPreferredSize().width + 5;
			int h = contents.getPreferredSize().height +
				scroll.getHorizontalScrollBar().getPreferredSize().height + 5;
			w = Math.min(400, w);
			h = Math.min(500, h);
			setPreferredSize(new Dimension(w, h));
			add(scroll, BorderLayout.CENTER);
		}

		public boolean hasContent() {
			return hasContent;
		}

	}

	/**
	 * Apply the tooltip style (e.g. colors) to the given component.
	 */
	public static void applyToolTipStyle(JComponent c) {
		c.setForeground((Color)UIManager.get("ToolTip.foreground"));
		c.setBackground((Color)UIManager.get("ToolTip.background"));
		c.setFont((Font)UIManager.get("ToolTip.font"));
	}

	public void scrollTo(Rectangle r)
	{
		container.getViewport().scrollRectToVisible(r);
	}

	public void vElementMouseEvent(VElementMouseEvent e) {
		//Change mouse cursor based on underlying object
		if(	e.getElement() instanceof Handle) {
			if(e.getType() == VElementMouseEvent.TYPE_MOUSEENTER) {
				
				Handle h = (Handle) e.getElement();
				if(h.getAngle() == 1)  setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else setCursor(Cursor.getPredefinedCursor(calculateCursorStyle(e)));
				
			} else if(e.getType() == VElementMouseEvent.TYPE_MOUSEEXIT) {
				setCursor(Cursor.getDefaultCursor());
			}
		} else if (e.getElement() instanceof Label) {
			if(e.getType() == VElementMouseEvent.TYPE_MOUSE_SHOWHAND) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if(e.getType() == VElementMouseEvent.TYPE_MOUSE_NOTSHOWHAND) {
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	/**
	 * calculates the corresponding cursor type
	 * depending on the angle of the current handle object
	 * and the rotation of the object
	 * @param e
	 * @return
	 */
	private int calculateCursorStyle(VElementMouseEvent e) {
		Handle h = (Handle) e.getElement();
		if(h.getParent() instanceof GraphicsShape) {
			GraphicsShape gs = (GraphicsShape) h.getParent();
			double rotation = gs.getPathwayElement().getRotation();
			double degrees = h.getAngle() + (rotation * (180 / Math.PI));
			
			if(degrees > 360)  degrees = degrees - 360;
			
			if(h.getAngle() == 1) {
				return Cursor.MOVE_CURSOR;
			} else {
				switch (h.getFreedom()) {
					case X:
						return getXYCursorPosition(degrees);
					case Y:
						return getXYCursorPosition(degrees);
					case FREE:
						return getFREECursorPosition(degrees);
					case NEGFREE:
						return getFREECursorPosition(degrees);
				}
			}
		}

		return Cursor.DEFAULT_CURSOR;
	}

	private int getXYCursorPosition(double degrees) {
		if(degrees < 45 || degrees > 315) {
			return Cursor.E_RESIZE_CURSOR;
		} else if (degrees < 135) {
			return Cursor.S_RESIZE_CURSOR;
		} else if (degrees < 225) {
			return Cursor.W_RESIZE_CURSOR;
		} else if (degrees < 315) {
			return Cursor.N_RESIZE_CURSOR;
		}
		return Cursor.DEFAULT_CURSOR;
	}
	
	private int getFREECursorPosition(double degrees) {
		if(degrees < 90) {
			return Cursor.SE_RESIZE_CURSOR;
		} else if(degrees < 180) {
			return Cursor.SW_RESIZE_CURSOR;
		} else if (degrees < 270) {
			return Cursor.NW_RESIZE_CURSOR;
		} else if (degrees <= 360) {
			return Cursor.NE_RESIZE_CURSOR;
		}
		return Cursor.DEFAULT_CURSOR;
	}
	
	private boolean disposed = false;
	public void dispose()
	{
		assert (!disposed);
		if (container != null && container instanceof JScrollPane)
			((JScrollPane)container).remove(this);
		child.removeVPathwayListener(this);
		child.removeVElementMouseListener(this);

		removeMouseListener(this);
		removeMouseMotionListener(this);
		removeKeyListener(this);
		setTransferHandler(null);

		// clean up actions, inputs registered earlier for this component
		// (otherwise VPathway won't get GC'ed, because actions contain reference to VPathay)
		getActionMap().clear();
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).clear();

		child = null; // free VPathway for GC
		disposed = true;
	}

	private int oldVWidth = 0;
	private int oldVHeight = 0;

	public void resized()
	{		
		int vw = (int)child.getVWidth();
		int vh = (int)child.getVHeight();

		if (vw != oldVWidth || vh != oldVHeight)
		{
			oldVWidth = vw;
			oldVHeight = vh;
			setPreferredSize(new Dimension(vw, vh));
			revalidate();
			repaint();
		}
		
	}

	public Rectangle getViewRect()
	{
		return container.getViewport().getViewRect();
	}
	
	public void scrollCenterTo(int x, int y)
	{
		int w = container.getViewport().getWidth();
		int h = container.getViewport().getHeight();
		Rectangle r = new Rectangle(x - (w / 2), y - (h / 2), w - 1, h - 1);
		scrollRectToVisible(r);
	}

}
