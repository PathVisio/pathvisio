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
package org.pathvisio.view;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class Group extends Graphics
{

	public Group(VPathway canvas, PathwayElement pe)
	{
		super(canvas, pe);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Generates current id-ref pairs from all current groups
	 * 
	 * @return HashMap<String, String>
	 */
	protected HashMap<String, String> getIdRefPairs()
	{
		// idRefPairs<id, ref>
		HashMap<String, String> idRefPairs = new HashMap<String, String>();

		// Populate hash map of id-ref pairs for all groups
		for (VPathwayElement vpe : canvas.getDrawingObjects())
		{
			if (vpe instanceof Graphics && vpe instanceof Group)
			{
				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
				if (pe.getGroupRef() != null)
				{
					idRefPairs.put(pe.getGroupId(), pe.getGroupRef());
				}
			}
		}

		return idRefPairs;
	}

	/**
	 * Generates list of group references nested under this group
	 * 
	 * @return ArrayList<String>
	 */
	protected ArrayList<String> getRefList()
	{
		HashMap<String, String> idRefPairs = this.getIdRefPairs();
		ArrayList<String> refList = new ArrayList<String>();
		String thisId = this.getPathwayElement().getGroupId();
		refList.add(thisId);
		boolean hit = true;

		while (hit)
		{
			hit = false;
			// search for hits in hash map; add to refList
			for (String id : idRefPairs.keySet())
			{
				if (refList.contains(idRefPairs.get(id)))
				{
					refList.add(id);
					hit = true;
				}
			}
			// remove hits from hash map
			for (int i = 0; i < refList.size(); i++)
			{
				idRefPairs.remove(refList.get(i));
			}
		}
		return refList;
	}

	/**
	 * Determines whether any member of the highest-level group object related
	 * to the current group object contains the point specified
	 * 
	 * @param point -
	 *            the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected boolean vContains(Point2D point)
	{
		ArrayList<String> refList = this.getRefList();

		// return true if group object is referenced by selection
		for (VPathwayElement vpe : canvas.getDrawingObjects())
		{
			if (vpe instanceof Graphics && !(vpe instanceof Group)
					&& vpe.vContains(point))
			{
				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
				String ref = pe.getGroupRef();
				// System.out.println("pe: " + pe + " ref: " + ref + " refList:
				// "
				// + refList.toString());
				if (ref != null && refList.contains(ref))
				{
					// System.out.println(ref + " contains point");
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean vIntersects(Rectangle2D r)
	{
		ArrayList<String> refList = this.getRefList();

		// return true if group object is referenced by selection
		for (VPathwayElement vpe : canvas.getDrawingObjects())
		{
			if (vpe instanceof Graphics && !(vpe instanceof Group)
					&& vpe.vIntersects(r))
			{
				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
				String ref = pe.getGroupRef();
				if (ref != null && refList.contains(ref))
				{
					// System.out.println(ref + " intersects point");
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns graphics for members of a group, including nested members
	 * 
	 * @return ArrayList<Graphics>
	 */
	public ArrayList<Graphics> getGroupGraphics()
	{
		ArrayList<Graphics> gg = new ArrayList<Graphics>();
		// return true if group object is referenced by selection
		for (VPathwayElement vpe : canvas.getDrawingObjects())
		{
			if (vpe instanceof Graphics && vpe != this)
			{
				Graphics vpeg = (Graphics) vpe;
				PathwayElement pe = vpeg.getPathwayElement();
				String ref = pe.getGroupRef();
				if (ref != null && ref.equals(getPathwayElement().getGroupId()))
				{
					gg.add(vpeg);
				}
			}
		}
		return gg;
	}

	@Override
	public void select()
	{
		for (Graphics g : getGroupGraphics())
		{
			System.out.println("g: " + g);
			g.select();
		}
		super.select();
	}

	@Override
	protected void vMoveBy(double dx, double dy)
	{
		for (Graphics g : getGroupGraphics())
		{
			g.vMoveBy(dx, dy);
		}
		//super.vMoveBy(dx, dy);
	}

	@Override
	public int getNaturalOrder()
	{

		return VPathway.DRAW_ORDER_GROUP;
	}

	@Override
	protected void doDraw(Graphics2D g2d)
	{
		// TODO make unique selection box for groups

	}

	@Override
	protected Shape getVOutline()
	{
		// TODO Return outline of the Group members, distinct from global
		// selection box

		Rectangle rect = new Rectangle();

		return rect;
	}

	protected Shape getVShape(boolean rotate)
	{
		// TODO Auto-generated method stub
		return new Rectangle();
	}

	protected void setVScaleRectangle(Rectangle2D r)
	{
		// TODO Auto-generated method stub

	}
}
