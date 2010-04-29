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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.PathwayElement;

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

	@Override
	protected void vMoveBy(double vdx, double vdy)
	{
		gdata.setMTop (gdata.getMTop()  + mFromV(vdy));
		gdata.setMLeft (gdata.getMLeft() + mFromV(vdx));
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
			return getVShape(false).contains(point);
		}
		else
		{
			return false;
		}
	}

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

	public Font getVFont() {
		String name = gdata.getFontName();
		int style = getVFontStyle();
		int size = (int)getVFontSize();
		return new Font(name, style, size);
	}

	double getVFontSize()
	{
		return vFromM(gdata.getMFontSize());
	}

	@Override
	protected void setVScaleRectangle(Rectangle2D r)
	{
		//Do nothing, can't resize infobox		
	}
}
