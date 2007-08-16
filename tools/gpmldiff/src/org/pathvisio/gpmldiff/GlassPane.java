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
package org.pathvisio.gpmldiff;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.font.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.GeneralPath;
import java.text.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

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
 * it is also friedly for other users,
 * if someone adds a mouseListener to this GlassPane
 * or set a new cursor it will respect them
 *
 * @author Alexander Potochkin
 */
class GlassPane extends JPanel implements AWTEventListener
{
    private final JFrame frame;
    private Point mousePos = new Point();

	// baloon margin is both the horizontal and vertical margin.
	private static final int BALOON_SPACING = 50;
	private static final int BALOON_MARGIN = 20;
	private static final Color BALOON_PAINT = Color.YELLOW;
	private static final int HINT_FONT_SIZE = 11;
	private static final float WAYPOINT_OFFSET = 200;

	private double zoomFactor = 1.0;

	
	private boolean alignTop = true;

	void setPctZoom (double value)
	{
		zoomFactor = value / 100;
		if (showHint)
		{
			repaint();
		}
	}
	
    public GlassPane(JFrame frame)
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
	   setHint implies showHint (true)
	 */
	void setHint(Map <String, String> _hint, double _x1, double _y1, double _x2, double _y2)
	{
		hint = _hint;
		x1 = _x1;
		y1 = _y1;
		x2 = _x2;
		y2 = _y2;
		showHint = true;
		repaint();
	}

	private int baloonWidth = 0;
	private int baloonHeight = 0;

	JViewport oldView;
	JViewport newView;

	void setViewPorts (JViewport o, JViewport n)
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
	void clearHint()
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
		int textHeight = 0;
		int maxTextWidth = 0;

		Font f = new Font("SansSerif", Font.PLAIN, HINT_FONT_SIZE);
		Font fb = new Font("SansSerif", Font.BOLD, HINT_FONT_SIZE);

		Map <TextLayout, Point> layouts = new HashMap <TextLayout, Point>();

		int ypos = 0;
		for (Map.Entry<String, String> entry : hint.entrySet())
		{
			TextLayout tl0 = new TextLayout (entry.getKey() + ": ", fb, frc);
			TextLayout tl1 = new TextLayout (entry.getValue(), f, frc);
			Rectangle2D b0 = tl0.getBounds();
			Rectangle2D b1 = tl1.getBounds();

			ypos += tl0.getAscent();

			layouts.put (tl0, new Point (0, ypos));
			layouts.put (tl1, new Point (10 + (int)b0.getWidth(), ypos));

			ypos += tl0.getDescent() + 10 + tl0.getLeading();

			int width = (int)(b0.getWidth() + b1.getWidth());
			if (width > maxTextWidth) { maxTextWidth = width; }
		}
		baloonWidth = maxTextWidth + 2 * BALOON_MARGIN;
		baloonHeight = ypos + 2 * BALOON_MARGIN;

		// figure out coordinates that are not in the way of the mouse.
		Shape bg = getHintShape();
		if (mousePos != null && bg.contains(mousePos))
		{
			// toggle alignTop and calculate new shape
			alignTop = !alignTop;
			bg = getHintShape();
		}

		g2.setPaint (BALOON_PAINT);
		g2.fill (bg);
		g2.setColor (Color.BLACK);
		g2.draw (bg);

		Point hintPos = getHintPos();
		// then do actual drawing
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

		
		Point p = relativeToView (x1, y1, oldView);
		GeneralPath path = new GeneralPath ();
		Point s = new Point (
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
		p = relativeToView (x2, y2, newView);
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
			
		g2.setStroke (new BasicStroke (5));
		g2.setColor (Color.YELLOW);
		g2.draw (path);
		
		g2.dispose();
    }

	Point relativeToView (double x, double y, JViewport view)
	{
		Point p = view.getLocationOnScreen();
		Point p2 = getLocationOnScreen();
		Point p3 = view.getViewPosition();
		int rx = (int)(p.getX() - p2.getX() - p3.getX() + (x * zoomFactor / 15.0));
		int ry = (int)(p.getY() - p2.getY() - p3.getY() + (y * zoomFactor / 15.0));
		return new Point (rx, ry);
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
                MouseEvent converted = SwingUtilities.convertMouseEvent(me.getComponent(), me, frame.getGlassPane());
                mousePos = converted.getPoint();
			}
            repaint();
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

