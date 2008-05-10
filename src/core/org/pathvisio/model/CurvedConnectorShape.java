// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.model;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Implements a curved connector that draws curved lines between
 * the segments.
 * @author thomas
 *
 */
public class CurvedConnectorShape extends ElbowConnectorShape {

	public Shape calculateShape(Segment[] segments) {
		GeneralPath path = new GeneralPath();
		Segment first = segments[0];
		Segment last = segments[segments.length - 1];
		path.moveTo(
				(float)first.getMStart().getX(), 
				(float)first.getMStart().getY()
		);
		Point2D prev = first.getMStart();
		
		for(int i = 1; i < segments.length - 1; i++) {
			Segment s = segments[i];
			Point2D center = s.getMCenter();
			Point2D start = s.getMStart();
			path.curveTo(
					(float)prev.getX(),
					(float)prev.getY(),
					(float)start.getX(),
					(float)start.getY(),
					(float)center.getX(),
					(float)center.getY()
			);
			prev = s.getMCenter();
		}
		
		path.curveTo(
				(float)last.getMStart().getX(),
				(float)last.getMStart().getY(),
				(float)last.getMEnd().getX(),
				(float)last.getMEnd().getY(),
				(float)last.getMEnd().getX(),
				(float)last.getMEnd().getY()
		);
		return path;
	}
}
