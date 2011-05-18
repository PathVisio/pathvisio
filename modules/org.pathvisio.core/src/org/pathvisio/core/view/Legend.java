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
package org.pathvisio.core.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.core.model.PathwayElement;

/**
 * Legend objects can be used by plugins to draw a Legend for visualization of 
 * expression datasets, for example to indicate which color corresponds to which expression value.
 * <p>
 * In the absence of any visualizations, Legends are completely empty.
 * <p> 
 * Legends are automatically created for pathway elements with ObjectType.LEGEND. 
 */
public class Legend extends Graphics
{
	public Legend(VPathway canvas, PathwayElement o)
	{
		super(canvas, o);
	}

	int sizeX = 1;
	int sizeY = 1; //Real size is calculated on first call to draw()
	private static final int M_INITIAL_LEGEND_WIDTH = 200;
	private static final int M_INITIAL_LEGEND_HEIGHT = 300;

	/**
	 * Draws nothing by default, only when it is selected an outline is drawn.
	 */
	@Override
	protected void doDraw(Graphics2D g)
	{
		// do nothing, actual drawing is handled by visualization plugin
		if (isSelected())
		{
			g.setColor(Color.BLACK);
			g.setStroke	(new BasicStroke (
					1,
					BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_MITER,
					10, new float[] {4, 4}, 0));
			g.draw(getVShape(false));
		}
	}

	/**
	 * Simple drag operation, object is moved by the delta coordinates passed in.
	 */
	@Override
	protected void vMoveBy(double vdx, double vdy)
	{
		gdata.setMTop (gdata.getMTop()  + mFromV(vdy));
		gdata.setMLeft (gdata.getMLeft() + mFromV(vdx));
	}

	/**
	 * vContains looks for overlap with the whole rectangle, not just the outline,
	 * so it is easy to drag.
	 */
	@Override
	protected boolean vContains(Point2D point)
	{
		// first use getVBounds as a rough approximation
		if (getVBounds().contains(point))
		{
			return getVShape(false).contains(point);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Simply a rectangle of constant size.
	 */
	@Override
	protected Shape getVShape(boolean rotate)
	{
		double vLeft = vFromM(gdata.getMLeft());
		double vTop = vFromM(gdata.getMTop());
		double vW = sizeX;
		double vH = sizeY;
		if(vW == 1 && vH == 1) {
			vW = vFromM(M_INITIAL_LEGEND_WIDTH);
			vH = vFromM(M_INITIAL_LEGEND_HEIGHT);
		}
		return new Rectangle2D.Double(vLeft, vTop, vW, vH);
	}

	/**
	 * Get Font object scaled to current zoom factor.
	 */
	public Font getVFont() {
		String name = gdata.getFontName();
		int style = getVFontStyle();
		int size = (int)getVFontSize();
		return new Font(name, style, size);
	}

	/**
	 * Get font size scaled to current zoom factor.
	 */
	double getVFontSize()
	{
		return vFromM(gdata.getMFontSize());
	}

	/**
	 * Doesn't do anything, you can't change the size of a Legend object.
	 */
	@Override
	protected void setVScaleRectangle(Rectangle2D r)
	{
		//Do nothing, can't resize infobox		
	}
}
