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

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Implemenents an graphical operations by putting 
 * them together into a java.awt.Shape.
 */
class ShapeEmitter implements Emitter
{
	GeneralPath path;
	Point2D pen;
	Point2D start;

	/**
	   get the resulting shape. Call afterwards
	 */
	public Shape getShape()
	{
		return path;
	}

	ShapeEmitter ()
	{
		path = new GeneralPath();
		pen = new Point2D.Double (0,0);
	}

	public void move (Point2D p)
	{
		path.moveTo ((float)p.getX(), (float)p.getY());
		pen = p;
		start = p;
	}

	public void close ()
	{
		path.closePath();
		pen = start;
	}

	public void line (Point2D p)
	{
		path.lineTo ((float)p.getX(), (float)p.getY());
		pen = p;
	}

	public void cubic (Point2D p1, Point2D p2, Point2D p)
	{
		path.curveTo (
			(float)p1.getX(), (float)p1.getY(),
			(float)p2.getX(), (float)p2.getY(),
			(float)p.getX(), (float)p.getY()
			);
		pen = p;
	}

	public void quad (Point2D p1, Point2D p)
	{
		path.quadTo ((float)p1.getX(), (float)p1.getY(), (float)p.getX(), (float)p.getY());
		pen = p;
	}

	public void arc (Point2D r, double rotation, boolean large, boolean sweep, Point2D p)
	{
		assert (false);
		/*
		  Arc2D a = new Arc2D.Double ();
		a.setStartPoint (pen);
		a.setWidth (r.getX() * 2);
		a.setHeight (r.getY() * 2);

		// ration is ignored, Arc2D can't handle it.
		path.append(a.)
			pen = p;
		*/
	}

	public void smoothCube (Point2D p2, Point2D p)
	{
		assert (false);
	}

	public void smoothQuad (Point2D p)
	{
		assert (false);
	}

	public void moveRelative (Point2D p)
	{
		path.moveTo ((float)(pen.getX() + p.getX()), (float)(pen.getY() + p.getY()));
		pen.setLocation (pen.getX() + p.getX(), pen.getY() + p.getY());
		start = pen;
	}

	public void lineRelative (Point2D p)
	{
		assert (false);
	}

	public void cubicRelative (Point2D p1, Point2D p2, Point2D p)
	{
		assert (false);
	}

	public void quadRelative (Point2D p1, Point2D p)
	{
		assert (false);
	}

	public void arcRelative (Point2D r, double rotation, boolean large, boolean sweep, Point2D p)
	{
		assert (false);
	}

	public void smoothCubeRelative (Point2D p2, Point2D p)
	{
		assert (false);
	}

	public void smoothQuadRelative (Point2D p)
	{
		assert (false);
	}

	public void flush()
	{
	}

	public void horizontal (double x)
	{
		assert (false);
	}

	public void vertical (double y)
	{
		assert (false);
	}

	public void horizontalRelative (double x)
	{
		assert (false);
	}

	public void verticalRelative (double y)
	{
		assert (false);
	}

}
