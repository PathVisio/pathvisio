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
 */
public abstract class SegmentedConnector extends AbstractConnector 
{
	protected Point2D fromLineCoordinate(double l, Segment[] segments) 
	{
		double totalLength = getTotalLength(segments);
		
		double pixelsRemaining = totalLength * l;
		if (pixelsRemaining < 0) pixelsRemaining = 0;
		if (pixelsRemaining > totalLength) pixelsRemaining = totalLength;

		// count off each segment from pixelsRemaining, until there aren't enough pixels left
		Segment segment = null;
		double slength = 0.0;
		for(Segment s : segments) 
		{
			slength = s.getMLength();
			segment = s;
			if (pixelsRemaining < slength) 
			{
				break; // not enough pixels left, we found our segment.
			}
			pixelsRemaining -= slength;
		}

		//Find the location on the segment
		Point2D s = segment.getMStart();
		Point2D e = segment.getMEnd();

		// protection against division by 0
		if (slength == 0)
		{
			return new Point2D.Double(s.getX(), s.getY());
		}
		else
		{
			// start from s, in the direction of e, for pixelRemaining pixels.
			double deltax = e.getX() - s.getX();
			double deltay = e.getY() - s.getY();

			return new Point2D.Double(
					s.getX() + deltax / slength * pixelsRemaining,
					s.getY() + deltay / slength * pixelsRemaining
			);
		}
	}

	protected Shape calculateShape(Segment[] segments) 
	{
		GeneralPath path = new GeneralPath();
		for(Segment s : segments) 
		{
			if(s == null) { //Ignore null segments
				Logger.log.error("Null segment in connector!");
				continue;
			}
			path.moveTo((float)s.getMStart().getX(), (float)s.getMStart().getY());
			path.lineTo((float)s.getMEnd().getX(), (float)s.getMEnd().getY());
		}
		return path;
	}

	/** @returns sum of the lengths of the segments */
	double getTotalLength (Segment[] segments) 
	{
		double totLength = 0.0;
		for (Segment seg : segments)
			totLength += seg.getMLength();
		return totLength;
	}
	
	public Point2D fromLineCoordinate(double l) {
		return fromLineCoordinate(l, getSegments());
	}
	
	public double toLineCoordinate(Point2D v) 
	{
		Segment[] segments = getSegments();
		double totLength = getTotalLength(segments);

		/* we're looking for the segment that has the shortest (projected) distance to v */
		double bestDistance = Double.MAX_VALUE; // the shortest distance found thus far
		double bestInLineCoordinates = 0.0; // the best result so far, in line coordinates. 
		
		double runningLength = 0.0; // sum of the length of the segments so far
		for (Segment seg : segments) 
		{			
			// projection of v on the segment 
			// TODO: probably could be merged with LinAlg.project.
			// Couldn't do that right away because I need the intermediate u value.
			Point base = new Point(seg.getMStart());
			Point direction = new Point(seg.getMEnd()).subtract(new Point(seg.getMStart()));
			Point vrelative = new Point(v).subtract(new Point(seg.getMStart()));

			double u = (vrelative.x * direction.x + vrelative.y * direction.y) 
			/ (direction.x * direction.x + direction.y * direction.y);

			Point projection = new Point(base.x + u * direction.x, base.y + u * direction.y);

			// calculate distance between v and segment.
			// special case: if u is smaller than 0 or larger than 1
			// then the projection lies outside the segment.
			double distance; 
			if (u < 0)
			{
				u = 0;
				distance = LinAlg.distance(new Point(v), new Point(seg.getMStart()));
			}
			else if (u > 1)
			{
				u = 1;
				distance = LinAlg.distance(new Point(v), new Point(seg.getMEnd()));
			}
			else distance = LinAlg.distance (projection, new Point(v));

			// did we find a better match?
			if (distance < bestDistance)
			{
				// save it, and calculate what the result would be in line coordinates.
				bestDistance = distance;
				bestInLineCoordinates = (runningLength + u * seg.getMLength()) / totLength;
			}
			
			// calculate the sum of the length of the segments so far
			runningLength += seg.getMLength(); 
		}

		return bestInLineCoordinates;
	}
}
