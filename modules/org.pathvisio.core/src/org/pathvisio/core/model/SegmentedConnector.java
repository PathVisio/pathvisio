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
package org.pathvisio.core.model;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.view.LinAlg;
import org.pathvisio.core.view.LinAlg.Point;

/**
 * Base class for segmented connectors.
 * @author thomas
 *
 */
public abstract class SegmentedConnector extends AbstractConnector {
	protected double locateOnSegment(Segment seg, Point2D v) {
		return LinAlg.toLineCoordinates(
				new Point(seg.getMStart()),
				new Point(seg.getMEnd()),
				new Point(v));
	}

	protected Point2D fromLineCoordinate(double l, Segment[] segments) {
		double length = 0;
		for(Segment s : segments) {
			length += Math.abs(s.getMLength());
		}

		//Find the right segment
		double end = 0;
		Segment segment = null;
		int i = 0;
		for(Segment s : segments) {
			double slength = Math.abs(s.getMLength());
			end += slength;
			double ls = (end - slength) / length;
			double le = end / length;
			if(l >= ls && l <= le) {
				segment = s;
				break;
			}
			i++;
		}
		if(segment == null) segment = segments[segments.length - 1];

		//Find the location on the segment
		double slength = Math.abs(segment.getMLength());
		double leftover = (l - (end - slength) / length) * length;
		double relative = slength == 0 ? 0 : leftover / slength;
		
		Point2D s = segment.getMStart();
		Point2D e = segment.getMEnd();

		double vsx = s.getX();
		double vsy = s.getY();
		double vex = e.getX();
		double vey = e.getY();

		int dirx = vsx > vex ? -1 : 1;
		int diry = vsy > vey ? -1 : 1;

		return new Point2D.Double(
				vsx + dirx * Math.abs(vsx - vex) * relative,
				vsy + diry * Math.abs(vsy - vey) * relative
		);
	}

	protected Shape calculateShape(Segment[] segments) {
		GeneralPath path = new GeneralPath();
		int i = 0;
		for(Segment s : segments) {
			i++;
			if(s == null) { //Ignore null segments
				Logger.log.error("Null segment in connector!");
				continue;
			}
			path.moveTo((float)s.getMStart().getX(), (float)s.getMStart().getY());
			path.lineTo((float)s.getMEnd().getX(), (float)s.getMEnd().getY());
		}
		return path;
	}

	protected Segment findSegment(Segment[] segments, Point2D v) {
		Segment foundSeg = null;
		double closestFit = Double.MAX_VALUE;
		double currFit;
		for (Segment seg:segments) 
		{
			// projection of v on the segment 
			// TODO: probably could be merged with LinAlg.project.
			// Couldn't do that right away because I need the intermediate u value.
			Point base = new Point(seg.getMStart());
			Point direction = new Point(seg.getMEnd()).subtract(new Point(seg.getMStart()));
			Point vrelative = new Point(v).subtract(new Point(seg.getMStart()));

			double u = ((vrelative.x)*(direction.x) + (vrelative.y) * (direction.y)) 
			/ ((direction.x) * (direction.x) + (direction.y) * (direction.y));

			Point projection = new Point(base.x + u * direction.x, base.y + u * (direction.y));

			// special case: if u is smaller than 0 or larger than 1
			// then closest lies outside the segment.
			if (u < 0) currFit = LinAlg.distance(new Point(v), new Point(seg.getMStart()));
			else if (u > 1) currFit = LinAlg.distance(new Point(v), new Point(seg.getMEnd()));
			else currFit = LinAlg.distance (projection, new Point(v));

			if (currFit < closestFit) {
				closestFit = currFit;
			}
		}
		return foundSeg;
	}

	protected double getTotalLength() {
		double totLength = 0.0;
		for (Segment seg:getSegments()) {
			totLength = seg.getMLength() + totLength;
		}
		return totLength;
	}
	
	public Point2D fromLineCoordinate(double l) {
		return fromLineCoordinate(l, getSegments());
	}

	/* *
	 *   Find how this segment fits in with the whole elbow and return
	 *      the cursor position may not always be exactly on the segment so need to make best guess.
	 */
	protected double locateOnConnector(Segment segment, Point2D v) {
		double segPercentOfTot = 0.0;
		double currSegPercentOfTot;
		double totLength = getTotalLength();
		for (Segment seg:getSegments()) {
			currSegPercentOfTot = seg.getMLength() / totLength;
			if (seg.equals(segment)) {
				currSegPercentOfTot = currSegPercentOfTot * locateOnSegment(seg, v);
				segPercentOfTot = currSegPercentOfTot + segPercentOfTot;
				break;
			}
			segPercentOfTot = currSegPercentOfTot + segPercentOfTot;
		}
		return segPercentOfTot;
	}
	
	public double toLineCoordinate(Point2D v) {
		Segment[] segments = getSegments();
		Segment seg = findSegment(segments, v);
		if (seg == null) {
			//there is no segment, which means it is the same as a StraightConnectorShape
			return LinAlg.toLineCoordinates(
					new Point(segments[0].getMStart()),
					new Point(segments[segments.length-1].getMEnd()),
					new Point(v)
			);
		}
		return locateOnConnector(seg, v);
	}
}
