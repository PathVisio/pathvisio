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

class StringEmitter implements Emitter
{
	String result = "";

	boolean fRelative = true;
    boolean fRound = true;
	double roundFactor = 1.0;

	double offX = 0;
	double offY = 0;

	void setRelative(boolean value)
	{
		fRelative = value;
	}

	void setRound(boolean value)
	{
		fRound = value;
	}

	void setOffset (double x, double y)
	{
		offX = x;
		offY = y;
	}

	public StringEmitter ()
	{
	}

	/**
	   Get the resulting string.
	   Call after the parsing is done.
	*/
	public String getResult()
	{
		return result;
	}

	public void cleanPoint(Point2D p)
	{
		double x = p.getX() + offX;
		double y = p.getY() + offY;

		if (fRound)
		{
			x = Math.round (x);
			y = Math.round (y);
		}

		p.setLocation (x, y);
	}

	public void move (Point2D p)
	{
		cleanPoint (p);
		result += "M " + p.getX() + ", " + p.getY() + " ";
	}

	public void close ()
	{
		result += "z ";
	}

	public void line (Point2D p)
	{
		assert (false);
	}

	public void cubic (Point2D p1, Point2D p2, Point2D p)
	{
		cleanPoint (p);
		cleanPoint (p1);
		cleanPoint (p2);
		result += "C " +
			p1.getX() + ", " + p1.getY() + " " +
			p2.getX() + ", " + p2.getY() + " " +
			p.getX() + ", " + p.getY() + " ";
	}

	public void quad (Point2D p1, Point2D p)
	{
		assert (false);
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
		assert (false);
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
		System.out.println (result);
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
