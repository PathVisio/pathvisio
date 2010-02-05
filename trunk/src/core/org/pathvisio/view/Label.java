// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.PathwayElement;

/**
 * Represents the view of a PathwayElement with ObjectType.LABEL.
 */
public class Label extends GraphicsShape
{

	public static final int M_INITIAL_FONTSIZE = 10 * 15;
	public static final int M_INITIAL_WIDTH = 80 * 15;
	public static final int M_INITIAL_HEIGHT = 20 * 15;
	public static final double M_ARCSIZE = 225;

	double getFontSize()
	{
		return gdata.getMFontSize() * canvas.getZoomFactor();
	}

	void setFontSize(double v)
	{
		gdata.setMFontSize(v / canvas.getZoomFactor());
	}

	/**
	 * Constructor for this class
	 * @param canvas - the VPathway this label will be part of
	 */
	public Label(VPathway canvas, PathwayElement o)
	{
		super(canvas, o);
	}

	public String getLabelText() {
		return gdata.getTextLabel();
	}

	String prevText = "";

	protected Rectangle2D getTextBounds(Graphics2D g) {
		Rectangle2D tb = null;
		if(g != null) {
			 tb = g.getFontMetrics(getVFont()).getStringBounds(getLabelText(), g);
			 tb.setRect(getVLeft() + tb.getX(), getVTop() + tb.getY(), tb.getWidth(), tb.getHeight());
		} else { //No graphics context, we can only guess...
			tb = getBoxBounds(true);
		}
		return tb;
	}

	protected Rectangle2D getBoxBounds(boolean stroke)
	{
		return getVShape(stroke).getBounds2D();
	}

	protected Dimension computeTextSize(Graphics2D g) {
		Rectangle2D tb = getTextBounds(g);
		return new Dimension((int)tb.getWidth(), (int)tb.getHeight());
	}

	Graphics2D g2d = null; //last Graphics2D for determining text size
	public void doDraw(Graphics2D g)
	{
		if(g2d != null) g2d.dispose();
		g2d = (Graphics2D)g.create();

		g.setColor(getLineColor());

		Font f = getVFont();
		g.setFont(f);

		Rectangle area = getBoxBounds(true).getBounds();

		setLineStyle(g);
		drawShape(g);

		// don't draw label outside box
		g.clip (new Rectangle (area.x - 1, area.y - 1, area.width + 1, area.height + 1));
		drawTextLabel(g);
		
		if(isHighlighted())
		{
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke());
			Rectangle2D r = new Rectangle2D.Double(getVLeft(), getVTop(), getVWidth(), getVHeight());
			g.fill(r);
		}
		super.doDraw(g2d);
	}

	/**
	 * Outline of a label is determined by
	 * - position of the handles
	 * - size of the text
	 * Because the text can sometimes be larger than the handles
	 */
	protected Shape calculateVOutline()
	{
		Shape outline = super.calculateVOutline();
		Rectangle2D bb = getBoxBounds(true);
		Rectangle2D tb = getTextBounds(g2d);
		tb.add(bb);
		Area a = new Area(outline);
		a.add(new Area(tb));
		return a;
	}
}
