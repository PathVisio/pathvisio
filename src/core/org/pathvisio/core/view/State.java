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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.pathvisio.core.model.MState;
import org.pathvisio.core.model.PathwayElement;

/**
 * represents the view of a PathwayElement with ObjectType.STATE.
 */
public class State extends GraphicsShape
{	
	public State (VPathway canvas, PathwayElement o) {
		super(canvas, o);
	}

	public void doDraw(Graphics2D g)
	{
		g.setColor(getLineColor());
		setLineStyle(g);
		drawShape(g);
		
		g.setFont(getVFont());
		drawTextLabel(g);
		
		drawHighlight(g);
	}

	protected void vMoveBy(double vdx, double vdy)
	{
		Point2D newPos = new Point2D.Double (getVCenterX() + vdx, getVCenterY() + vdy); 
		Point2D newRel = ((MState)gdata).getParentDataNode().toRelativeCoordinate(newPos);
		double x = newRel.getX();
		double y = newRel.getY();
		if (x > 1) x = 1;
		if (x < -1) x = -1;
		if (y > 1) y = 1;
		if (y < -1) y = -1;
		gdata.setRelX(x);
		gdata.setRelY(y);
	}

	@Override
	public void destroy()
	{
		super.destroy();
	}
}
