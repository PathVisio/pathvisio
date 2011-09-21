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
package org.pathvisio.core.view;

import org.pathvisio.core.model.PathwayElement.MPoint;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.LinAlg.Point;

/**
 * One of the two endpoints of a line. Carries a single handle.
 */
public class VPoint implements Adjustable
{
	// the handle that goes with this VPoint.
	// This Handle is created, destroyed and generally managed by Line, not by VPoint
	Handle handle;
	
	private Line line;
	private MPoint mPoint;
	private final VPathway canvas;

	private boolean isHighlighted = false;

	public boolean isHighlighted()
	{
		return isHighlighted;
	}

	public void highlight()
	{
		if (!isHighlighted)
		{
			isHighlighted = true;
			line.markDirty();
		}
	}

	public void unhighlight()
	{
		if (isHighlighted)
		{
			isHighlighted = false;
			line.markDirty();
		}
	}

	VPoint(VPathway canvas, MPoint mPoint, Line line) {
		this.canvas = canvas;
		this.mPoint = mPoint;
		this.line = line;
	}

	protected void unlink() {
		mPoint.setGraphRef(null);
	}

	protected double getVX() { return canvas.vFromM(getMPoint().getX()); }
	protected double getVY() { return canvas.vFromM(getMPoint().getY()); }

	protected void setVLocation(double vx, double vy) {
		mPoint.setX(canvas.mFromV(vx));
		mPoint.setY(canvas.mFromV(vy));
	}

	protected void vMoveBy(double dx, double dy) {
		mPoint.moveBy(canvas.mFromV(dx), canvas.mFromV(dy));
	}

	public MPoint getMPoint() {
		return mPoint;
	}

	public Line getLine() {
		return line;
	}

	public void adjustToHandle(Handle h, double vnewx, double vnewy)
	{
		double mcx = canvas.mFromV (vnewx);
		double mcy = canvas.mFromV (vnewy);

		if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.SNAP_TO_ANGLE) ||
			canvas.isSnapModifierPressed())
		{
			// get global preference and convert to radians.
			double lineSnapStep = PreferenceManager.getCurrent().getInt(
				GlobalPreference.SNAP_TO_ANGLE_STEP) * Math.PI / 180;
			VPoint p1 = line.getStart();
			VPoint p2 = line.getEnd();
			double basex, basey;
			// base is the static point the line rotates about.
			// it is equal to the OTHER point, the one we're not moving.
			if (p1 == this)
			{
				basex = p2.getMPoint().getX();
				basey = p2.getMPoint().getY();
			}
			else
			{
				basex = p1.getMPoint().getX();
				basey = p1.getMPoint().getY();
			}
			// calculate rotation and round it off
			double rotation = Math.atan2(basey - mcy, basex - mcx);
			rotation = Math.round (rotation / lineSnapStep) * lineSnapStep;
			// project point mcx, mcy on a line with the desired angle.
			Point yr = new Point (Math.cos (rotation), Math.sin (rotation));
			Point prj = LinAlg.project(new Point (basex, basey), new Point(mcx, mcy), yr);
			mcx = prj.x;
			mcy = prj.y;
		}

		mPoint.setX(mcx);
		mPoint.setY(mcy);
	}

	protected Handle getHandle()
	{
		return handle;
	}
	
	public double getVWidth() { return 0;  }

	public double getVHeight() { return 0;  }
}
