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
package org.pathvisio.core.gpmldiff;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/**
 * Derived from:
 *
 * GlassPane tutorial
 * "A well-behaved GlassPane"
 * http://weblogs.java.net/blog/alexfromsun/
 * <p/>
 * This is the final version of the GlassPane
 * it is transparent for MouseEvents,
 * and respects underneath component's cursors by default,
 * it is also friendly for other users,
 * if someone adds a mouseListener to this GlassPane
 * or set a new cursor it will respect them
 *
 * @author Alexander Potochkin
 */
public class GlassPane extends JPanel implements AWTEventListener
{
	/**
	 * Type of element modification
	 */
	private static enum Type
	{
		MODIFY,
		ADD,
		REMOVE
	}


    private final JPanel frame;
    private Point mousePos = new Point();
    private Type type;
    private static Color baloonPaint;

	// baloon margin is both the horizontal and vertical margin.
	private static final int BALOON_SPACING = 50;
	private static final int BALOON_MARGIN = 20;
	private static final int HINT_FONT_SIZE = 11;
	private static final float WAYPOINT_OFFSET = 200;
	private static final int WRAP_WIDTH = 400;

	private double zoomFactor = 1.0;


	private boolean alignTop = true;

	public void setPctZoom (double value)
	{
		zoomFactor = value / 100;
		if (showHint)
		{
			repaint();
		}
	}

    public GlassPane(JPanel frame)
	{
        super(null);
        this.frame = frame;
        setOpaque(false);
    }

	boolean showHint = false;
	Map <String, String> hint = null;

    // view coordinates
	double x1, y1, x2, y2;

	/**
	   setModifyHint implies showHint (true)
	 */
	public void setModifyHint(Map <String, String> aHint, double ax1, double ay1, double ax2, double ay2)
	{
		hint = aHint;
		x1 = ax1;
		y1 = ay1;
		x2 = ax2;
		y2 = ay2;
		baloonPaint = Color.YELLOW;
		showHint = true;
		type = Type.MODIFY;
		repaint();
	}

	/**
	 * implies showHint (true)
	 */
	public void setRemoveHint(Map <String, String> aHint, double x, double y)
	{
		x1 = x;
		y1 = y;
		baloonPaint = Color.RED;
		type = Type.REMOVE;
		showHint = true;
		hint = aHint;
		repaint();
	}

	/**
	 * implies showHint (true)
	 */
	public void setAddHint(Map <String, String> aHint, double x, double y)
	{
		x2 = x;
		y2 = y;
		baloonPaint = Color.GREEN;
		type = Type.ADD;
		showHint = true;
		hint = aHint;
		repaint();
	}

	private int baloonWidth = 0;
	private int baloonHeight = 0;

	JViewport oldView;
	JViewport newView;

	public void setViewPorts (JViewport o, JViewport n)
	{
		oldView = o;
		newView = n;
	}

	/**
	   Note: don't call getHintShape() before setting baloonwidth and
	   baloonheight to meaningful values
	*/
	private Shape getHintShape()
	{
		Point pos = getHintPos();
		Shape bg = new RoundRectangle2D.Double(
			pos.getX(), pos.getY(),
			baloonWidth, baloonHeight,
			BALOON_MARGIN, BALOON_MARGIN
			);
		return bg;
	}


	/**
	   Note: don't call getHintPos() before setting baloonwidth and
	   baloonheight to meaningful values
	*/
	private Point getHintPos()
	{
		int xpos = (int)((getSize().getWidth() - baloonWidth) / 2);
		int ypos = alignTop ? BALOON_SPACING : (int)(getSize().getHeight() - baloonHeight - BALOON_SPACING);
		return new Point (xpos, ypos);
	}

	/**
	   enable showing of hint.
	 */
	public void clearHint()
	{
		hint = null;
		baloonWidth = 0;
		baloonHeight = 0;
		showHint = false;
		repaint();
	}

    protected void paintComponent(Graphics g)
	{
		if (!showHint) return;

        Graphics2D g2 = (Graphics2D) g;

        //g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

		FontRenderContext frc = g2.getFontRenderContext();

		// first determine size.
		int currentTextWidth = 0;

		Font f = new Font("SansSerif", Font.PLAIN, HINT_FONT_SIZE);
		Font fb = new Font("SansSerif", Font.BOLD, HINT_FONT_SIZE);

		// hash to store text layouts for later drawing.
		Map <TextLayout, Point> layouts = new HashMap <TextLayout, Point>();

		int ypos = 0;
		if (hint != null) for (Map.Entry<String, String> entry : hint.entrySet())
		{
			// show the key as a bold, non-wrapped text.
			// Only a single TextLayout needed.
			TextLayout tl0 = new TextLayout (entry.getKey() + ": ", fb, frc);
			int leftwidth = (int)tl0.getBounds().getWidth();
			layouts.put (tl0, new Point (0, (int)(ypos + tl0.getAscent())));

			// show the value as a plain, wrapped text.
			// multiple TextLayouts are needed.
			String text = entry.getValue();
			AttributedString as = new AttributedString (text);
			as.addAttribute(TextAttribute.FONT, f, 0, text.length());

			// use LineBreakMeasurer to wrap the text across multiple lines.
			LineBreakMeasurer lbm = new LineBreakMeasurer (as.getIterator(), frc);

			while (lbm.getPosition() < text.length())
			{
				TextLayout tl = lbm.nextLayout (WRAP_WIDTH);
				ypos += tl.getAscent();
				layouts.put (tl, new Point (10 + leftwidth, ypos));
				ypos += tl.getDescent() + tl.getLeading();
				int width = leftwidth + (int)tl.getBounds().getWidth();
				// store maximum width for calculating baloon width later.
				if (width > currentTextWidth) { currentTextWidth = width; }
			}
		}

		baloonWidth = currentTextWidth + 2 * BALOON_MARGIN;
		baloonHeight = ypos + 2 * BALOON_MARGIN;

		// figure out coordinates that are not in the way of the mouse.
		Shape bg = getHintShape();
		if (mousePos != null && bg.contains(mousePos))
		{
			// toggle alignTop and calculate new shape
			alignTop = !alignTop;
			bg = getHintShape();
		}

		g2.setPaint (baloonPaint);
		g2.fill (bg);
		g2.setColor (Color.BLACK);
		g2.draw (bg);

		Point hintPos = getHintPos();
		// now start actual drawing of text
		for (Map.Entry<TextLayout, Point> entry : layouts.entrySet())
		{
			Point textPos = entry.getValue();
			TextLayout l = entry.getKey();
			l.draw(
				g2,
				(float)(hintPos.getX() + textPos.getX() + BALOON_MARGIN),
				(float)(hintPos.getY() + textPos.getY() + BALOON_MARGIN));
		}

		// draw lines
		Shape oldClip = g2.getClip();

		g2.setStroke (new BasicStroke (5));
		g2.setColor (baloonPaint);

		clipView (g2, oldView);

		Point p;
		Point s;
		GeneralPath path;

		if (type != Type.ADD)
		{
			p = relativeToView (x1, y1, oldView);
			path = new GeneralPath ();
			s = new Point (
				(int)(hintPos.getX()),
				(int)(hintPos.getY() + baloonHeight / 2)
				);
			path.moveTo ((float)s.getX(), (float)s.getY());
			path.curveTo (
				(float)(s.getX() - WAYPOINT_OFFSET),
				(float)(s.getY()),
				(float)(p.getX() + WAYPOINT_OFFSET),
				(float)p.getY(),
				(float)p.getX(),
				(float)p.getY()
				);
			g2.draw (path);
		}

		g2.setClip(oldClip);
		clipView (g2, newView);

		if (type != Type.REMOVE)
		{
			p = relativeToView (x2, y2, newView);
			path = new GeneralPath ();
			s = new Point (
				(int)(hintPos.getX() + baloonWidth),
				(int)(hintPos.getY() + baloonHeight / 2)
				);
			path.moveTo ((float)s.getX(), (float)s.getY());
			path.curveTo (
				(float)s.getX() + WAYPOINT_OFFSET,
				(float)s.getY(),
				(float)p.getX() - WAYPOINT_OFFSET,
				(float)p.getY(),
				(float)p.getX(),
				(float)p.getY());

			g2.draw (path);
		}

		g2.dispose();
    }

	Point relativeToView (double x, double y, JViewport view)
	{
		// TODO: same can be achieved simple with SwingUtilities.convertRectangle
		Point p = view.getLocationOnScreen();
		Point p2 = getLocationOnScreen();
		Point p3 = view.getViewPosition();
		int rx = (int)(p.getX() - p2.getX() - p3.getX() + (x * zoomFactor));
		int ry = (int)(p.getY() - p2.getY() - p3.getY() + (y * zoomFactor));
		return new Point (rx, ry);
	}

	void clipView (Graphics2D g2d, JViewport view)
	{
		Point p = view.getLocationOnScreen();
		Dimension d = view.getSize();
		Point p2 = getLocationOnScreen();
		// TODO: same can be achieved simple with SwingUtilities.convertPoint
		g2d.setClip (
			(int)(p.getX() - p2.getX()),
			(int)(p.getY() - p2.getY()),
			(int)(d.getWidth()),
			(int)(d.getHeight())
			);
	}

	/**
	   Test if the mouse touches last known shape location, and
	   repaints if so.
	 */
	private void testMouseCursor()
	{
		Shape bg = getHintShape();
		if (mousePos != null && bg.contains(mousePos))
		{
			// toggle alignTop
			alignTop = !alignTop;
			repaint();
		}
	}

    public void eventDispatched(AWTEvent event)
	{
        if (event instanceof MouseEvent)
		{
            MouseEvent me = (MouseEvent) event;
            if (!SwingUtilities.isDescendingFrom(me.getComponent(), frame))
			{
                return;
            }
            if (me.getID() == MouseEvent.MOUSE_EXITED && me.getComponent() == frame)
			{
                mousePos = null;
            }
			else
			{
                MouseEvent converted = SwingUtilities.convertMouseEvent(me.getComponent(), me, this);
                mousePos = converted.getPoint();
				testMouseCursor();
			}
        }
    }

    /**
     * If someone adds a mouseListener to the GlassPane or set a new cursor
     * we expect that he knows what he is doing
     * and return the super.contains(x, y)
     * otherwise we return false to respect the cursors
     * for the underneath components
     */
    public boolean contains(int x, int y)
	{
        if (getMouseListeners().length == 0 && getMouseMotionListeners().length == 0
                && getMouseWheelListeners().length == 0
                && getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
		{
            return false;
        }
        return super.contains(x, y);
    }
}

