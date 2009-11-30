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
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.geom.Point2D;

class JavaEmitter implements Emitter
{
	PrintWriter target;
	String name;

	Point2D pen;
	Point2D start;

	double offX = 0;
	double offY = 0;

	boolean fRelative = false;
    boolean fRound = true;

	String format = "%.2ff";

	public void setRelative(boolean value)
	{
		fRelative = value;
	}

	public void setRound(boolean value)
	{
		fRound = value;
	}

	public void setOffset (double x, double y)
	{
		offX = x;
		offY = y;
	}

	public void setFormat (String value)
	{
		format = value;
	}

	private void printAbsolutePoint (Point2D p)
	{
		double x = p.getX() - offX;
		double y = p.getY() - offY;

		if (fRound)
		{
			target.printf (format, x);
			target.print (", ");
			target.printf (format, y);
		}
		else
		{
			target.print (x + ", " + y);
		}
	}

	private void printRelativePoint (Point2D p)
	{
		double x = p.getX() + pen.getX();
		double y = p.getY() + pen.getY();

		if (fRound)
		{
			target.printf (format, x);
			target.print (", ");
			target.printf (format, y);
		}
		else
		{
			target.print (x + ", " + y);
		}
	}

	public JavaEmitter (PrintWriter w, String _name)
	{
		assert (w != null);
		target = w;
		name = _name;
		target.println ("GeneralPath path = new GeneralPath();");
		Point2D.Double pen = new Point2D.Double(0,0);
	}

	public void move (Point2D p)
	{
		target.print ("path.moveTo (");
		printAbsolutePoint (p);
		target.println (");");

		pen = p;
		start = p;
	}

	public void close ()
	{
		target.println ("path.closePath();");
		pen = start;
	}

	public void line (Point2D p)
	{
		target.print ("path.lineTo (");
		printAbsolutePoint (p);
		target.println (");");

		pen = p;
	}

	public void cubic (Point2D p1, Point2D p2, Point2D p)
	{
		target.print ("path.curveTo (");
		printAbsolutePoint (p1);
		target.print (", ");
		printAbsolutePoint (p2);
		target.print (", ");
		printAbsolutePoint (p);
		target.println (");");

		pen = p;
	}

	public void quad (Point2D p1, Point2D p)
	{
		target.print ("path.quadTo (");
		printAbsolutePoint (p1);
		target.print (", ");
		printAbsolutePoint (p);
		target.println (");");
		pen = p;
	}

	public void arc (Point2D r, double rotation, boolean large, boolean sweep, Point2D p)
	{
		assert (false);
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
		target.print ("path.moveTo (");
		printRelativePoint (p);
		target.println (");");
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
		target.println ("return path;");
		target.flush();
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
