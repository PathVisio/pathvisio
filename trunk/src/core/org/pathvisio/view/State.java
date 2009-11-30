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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.pathvisio.model.PathwayElement;

/**
 * represents the view of a PathwayElement with ObjectType.STATE.
 */
public class State extends GraphicsShape
{
	public static final Color INITIAL_FILL_COLOR = Color.WHITE;

	//note: not the same as color!
	Color fillColor = INITIAL_FILL_COLOR;

	public State (VPathway canvas, PathwayElement o) {
		super(canvas, o);
		setHandleLocation();
	}

	/**
	 * Calculate the font size adjusted to the canvas zoom factor.
	 */
	private int getVFontSize()
	{
		return (int)(vFromM (gdata.getMFontSize()));
	}

	public void doDraw(Graphics2D g)
	{
		java.awt.Shape origClip = g.getClip();
		RectangularShape area = new Rectangle2D.Double(
			getVLeft(), getVTop(), getVWidth(), getVHeight());

		//White background
		g.setPaint (Color.WHITE);
		g.fill(area);

		//Rectangular Outline
		g.setStroke(new BasicStroke());
		if(isSelected()) {
			g.setColor(selectColor);
		} else {
			g.setColor(gdata.getColor());
		}
		g.draw(area);

		//Label
		//Don't draw label outside gene box
		g.clip (new Rectangle2D.Double (area.getX() - 1, area.getY() - 1, area.getWidth() + 1, area.getHeight()+ 1));

		g.setFont(new Font(gdata.getFontName(), getVFontStyle(), getVFontSize()));

		String label = gdata.getTextLabel();
		if(label.length() > 0) {
			TextLayout tl = new TextLayout(label, g.getFont(), g.getFontRenderContext());
			Rectangle2D tb = tl.getBounds();
			tl.draw(g, 	(int)area.getX() + (int)(area.getWidth() / 2) - (int)(tb.getWidth() / 2),
					(int)area.getY() + (int)(area.getHeight() / 2) + (int)(tb.getHeight() / 2));
		}
		g.setClip(origClip); //Reset clipping
		drawHighlight(g);
		super.doDraw((Graphics2D)g.create());
	}

	public void drawHighlight(Graphics2D g)
	{
		if(isHighlighted())
		{
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke (HIGHLIGHT_STROKE_WIDTH));
			Rectangle2D r = new Rectangle2D.Double(getVLeft(), getVTop(), getVWidth(), getVHeight());
			g.draw(r);
		}
	}
}
