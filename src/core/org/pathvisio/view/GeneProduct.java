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
import java.awt.geom.Point2D;
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
	 * {@inheritDoc}
	 * GeneProduct overrides vContains, because the base implementation only considers a 
	 * hit with the outline, which makes it hard to grab with the mouse.
	 */
	@Override
	protected boolean vContains(Point2D point)
	{
		// first use getVBounds as a rough approximation
		if (getVBounds().contains(point))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	public void doDraw(Graphics2D g)
	{
		java.awt.Shape origClip = g.getClip();

		RectangularShape area = getShape(false, false).getBounds();

		setLineStyle(g);
		g.setColor(getLineColor());
		drawShape(g);

		//Label
		//Don't draw label outside gene box
		g.clip (new Rectangle2D.Double (area.getX() - 1, area.getY() - 1, area.getWidth() + 1, area.getHeight()+ 1));

		Font f = getVFont();
		g.setFont(f);

		drawTextLabel(g);

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
