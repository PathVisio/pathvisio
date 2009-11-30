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
import java.awt.geom.RoundRectangle2D;

import org.pathvisio.model.PathwayElement;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;

/**
 * This class implements a geneproduct and
 * provides methods to resize and draw it.
 * //TODO: rename this class to DataNode
 */
public class GeneProduct extends GraphicsShape
{
	public static final Color INITIAL_FILL_COLOR = Color.WHITE;

	//note: not the same as color!
	Color fillColor = INITIAL_FILL_COLOR;

	public GeneProduct (VPathway canvas, PathwayElement o) {
		super(canvas, o);
	}

	/**
	 * @deprecated get this info from PathwayElement directly
	 */
	public String getID()
	{
		//Looks like the wrong way around, but in gpml the ID is attribute 'Name'
		//NOTE: maybe change this in gpml?
		return gdata.getGeneID();
	}

	/**
	 * Calculate the font size adjusted to the canvas zoom factor.
	 */
	private int getVFontSize()
	{
		return (int)(vFromM (gdata.getMFontSize()));
	}

	/**
	 * Get the outline shape for this DataNode
	 * This is used for drawing the DataNode, but
	 * it can also be used by Visualization plugins as a clipping area
	 */
	public RectangularShape getShape()
	{
		RectangularShape area = new Rectangle2D.Double(
				getVLeft(), getVTop(), getVWidth(), getVHeight());
			boolean rounded = PreferenceManager.getCurrent().getBoolean(GlobalPreference.DATANODES_ROUNDED);
			if(rounded) {
				double r = Math.max(area.getWidth(), area.getHeight()) * 0.2;
				area = new RoundRectangle2D.Double(area.getX(), area.getY(),
						area.getWidth(), area.getHeight(),
						r, r);
			}
		return area;
	}

	public void doDraw(Graphics2D g)
	{
		java.awt.Shape origClip = g.getClip();

		RectangularShape area = getShape();
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
