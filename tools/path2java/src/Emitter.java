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

import java.awt.geom.Point2D;

/**
 * Implement this to transfer path operations onto any output. The output could 
 * be for example a file format or graphics device.
 */
interface Emitter
{
	public void move (Point2D p);
	public void close ();
	public void line (Point2D p);
	public void cubic (Point2D p1, Point2D p2, Point2D p);
	public void quad (Point2D p1, Point2D p);
	public void arc (Point2D r, double rotation, boolean large, boolean sweep, Point2D p);
	public void smoothCube (Point2D p2, Point2D p);
	public void smoothQuad (Point2D p);

	public void horizontal (double x);
	public void vertical (double y);

	public void moveRelative (Point2D p);
	public void lineRelative (Point2D p);
	public void cubicRelative (Point2D p1, Point2D p2, Point2D p);
	public void quadRelative (Point2D p1, Point2D p);
	public void arcRelative (Point2D r, double rotation, boolean large, boolean sweep, Point2D p);
	public void smoothCubeRelative (Point2D p2, Point2D p);
	public void smoothQuadRelative (Point2D p);

	public void horizontalRelative (double x);
	public void verticalRelative (double y);

	public void flush();
}
