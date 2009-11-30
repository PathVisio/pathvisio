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
package org.pathvisio.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * This represents the view of a PathwayElement with ObjectType.GROUP.
 * This can be drawn as a shaded area, or the group can be invisible.
 *
 * Also contains the getGroupGraphics method to quickly access all Graphics' that
 * are in this group.
 */
public class Group extends Graphics implements LinkProvider, VElementMouseListener
{
	public static final int FLAG_SELECTED = 1 << 0;
	public static final int FLAG_MOUSEOVER = 1 << 1;
	public static final int FLAG_ANCHORSVISIBLE = 1 << 2;

	public Group(VPathway canvas, PathwayElement pe)
	{
		super(canvas, pe);
		canvas.addVElementMouseListener(this);
	}

	/**
	 * Generates current id-ref pairs from all current groups
	 *
	 * @return HashMap<String, String>
	 */
	protected Map<String, String> getIdRefPairs()
	{
		// idRefPairs<id, ref>
		Map<String, String> idRefPairs = new HashMap<String, String>();

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
	protected List<String> getRefList()
	{
		Map<String, String> idRefPairs = this.getIdRefPairs();
		List<String> refList = new ArrayList<String>();
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

//	/**
//	 * Determines whether any member of the highest-level group object related
//	 * to the current group object contains the point specified
//	 *
//	 * @param point -
//	 *            the point to check
//	 * @return True if the object contains the point, false otherwise
//	 */
//	protected boolean vContains(Point2D point)
//	{
//		ArrayList<String> refList = this.getRefList();
//
//		// return true if group object is referenced by selection
//		for (VPathwayElement vpe : canvas.getDrawingObjects())
//		{
//			if (vpe instanceof Graphics && !(vpe instanceof Group)
//					&& vpe.vContains(point))
//			{
//				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
//				String ref = pe.getGroupRef();
//				// System.out.println("pe: " + pe + " ref: " + ref + " refList:
//				// "
//				// + refList.toString());
//				if (ref != null && refList.contains(ref))
//				{
//					// System.out.println(ref + " contains point");
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	/**
	 * Determines whether the area defined by the grouped elements
	 * contains the point specified. The elements themselves are
	 * excluded to support individual selection within a group. The
	 * ultimate effect is then selection of group by clicking the area
	 * and not the members of the group.
	 *
	 * @param point -
	 *            the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected boolean vContains(Point2D point)
	{
		// return false if point falls on any individual element
		for (VPathwayElement vpe : canvas.getDrawingObjects())
		{
			if (vpe instanceof Graphics && !(vpe instanceof Group)
					&& vpe.vContains(point))
			{
				return false;

			}
		}
		// return true if point within bounds of grouped objects
		if (this.getVShape(true).contains(point))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

//	@Override
//	protected boolean vIntersects(Rectangle2D r)
//	{
//		ArrayList<String> refList = this.getRefList();
//
//		// return true if group object is referenced by selection
//		for (VPathwayElement vpe : canvas.getDrawingObjects())
//		{
//			if (vpe instanceof Graphics && !(vpe instanceof Group)
//					&& vpe.vIntersects(r))
//			{
//				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
//				String ref = pe.getGroupRef();
//				if (ref != null && refList.contains(ref))
//				{
//					// System.out.println(ref + " intersects point");
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	/**
	 * Returns graphics for members of a group, including nested members
	 *
	 * @return ArrayList<Graphics>
	 */
	public List<Graphics> getGroupGraphics()
	{
		List<Graphics> gg = new ArrayList<Graphics>();
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
			g.select();
		}
		super.select();
	}

	@Override
	public void deselect() {
		for (Graphics g : getGroupGraphics())
		{
			g.deselect();
		}
		super.deselect();
	}

	@Override
	protected void vMoveBy(double dx, double dy)
	{
		for (Graphics g : getGroupGraphics())
		{
			g.vMoveBy(dx, dy);
		}
		//Move graphRefs
		//GraphLink.moveRefsBy(gdata, mFromV(vdx), mFromV(vdy));
		Set<VPoint> toMove = new HashSet<VPoint>();
		for(GraphRefContainer ref : gdata.getReferences()) {
			if(ref instanceof MPoint) {
				toMove.add(canvas.getPoint((MPoint)ref));
			}
		}
		for(VPoint p : toMove) p.vMoveBy(dx, dy);

		// update group outline
		markDirty();
	}


	protected void doDraw(Graphics2D g2d)
	{
		//Build the flags
		int flags = 0;
		if(isSelected()) flags += FLAG_SELECTED;
		if(mouseover) flags += FLAG_MOUSEOVER;
		if(showLinkAnchors) flags += FLAG_ANCHORSVISIBLE;

		//Draw the group style appearance
		GroupPainter p = GroupPainterRegistry.getPainter(gdata.getGroupStyle().toString());
		p.drawGroup(g2d, this, flags);

		//anchors
		if(showLinkAnchors) {
			for(LinkAnchor la : getLinkAnchors()) {
				la.draw((Graphics2D)g2d.create());
			}
		}
	}

	boolean mouseover = false;

	public void vElementMouseEvent(VElementMouseEvent e) {
		if(e.getElement() == this) {
			boolean old = mouseover;
			if(e.getType() == VElementMouseEvent.TYPE_MOUSEENTER) {
				mouseover = true;
			} else if(e.getType() == VElementMouseEvent.TYPE_MOUSEEXIT) {
				mouseover = false;
			}
			if(old != mouseover) {
				markDirty();
				canvas.redrawDirtyRect();
			}
		}
	}

	public void highlight(Color c) {
		super.highlight(c);
		//Highlight the children
		for(Graphics g : getGroupGraphics()) {
			g.highlight();
		}
	}

	protected Shape calculateVOutline() {
		//Include rotation and stroke
		Area a = new Area(getVShape(true));
		//Include link anchors
		if(showLinkAnchors) {
			for(LinkAnchor la : getLinkAnchors()) {
				a.add(new Area(la.getShape()));
			}
		}
		return a;
	}

	protected Shape getVShape(boolean rotate)
	{
		Rectangle2D mb = null;
		if(rotate) {
			mb = gdata.getRBounds();
		} else {
			mb = gdata.getMBounds();
		}
		return canvas.vFromM(mb);
	}

	protected void setVScaleRectangle(Rectangle2D r)
	{
		// TODO Auto-generated method stub

	}

	List<LinkAnchor> linkAnchors = new ArrayList<LinkAnchor>();

	private static final int MIN_SIZE_LA = 15 * 25;
	private int numLinkanchorsH = -1;
	private int numLinkanchorsV = -1;

	public List<LinkAnchor> getLinkAnchors() {
		//Number of link anchors depends on the size of the object
		//If the width/height is large enough, there will be three link anchors per side,
		//Otherwise there will be only one link anchor per side
		int numH = gdata.getMWidth() >= MIN_SIZE_LA ? 3 : 1;
		int numV = gdata.getMHeight() >= MIN_SIZE_LA ? 3 : 1;
		if(numH != numLinkanchorsH || numV != numLinkanchorsV) {
			createLinkAnchors(numH, numV);
		}
		return linkAnchors;
	}

	private void createLinkAnchors(int numH, int numV) {
		linkAnchors.clear();
		double deltaH = 2.0/(numH + 1);
		for(int i = 1; i <= numH; i++) {
			linkAnchors.add(new LinkAnchor(canvas, gdata, -1 + i * deltaH, -1));
			linkAnchors.add(new LinkAnchor(canvas, gdata, -1 + i * deltaH, 1));
		}
		double deltaV = 2.0/(numV + 1);
		for(int i = 1; i <= numV; i++) {
			linkAnchors.add(new LinkAnchor(canvas, gdata, -1, -1 + i * deltaV));
			linkAnchors.add(new LinkAnchor(canvas, gdata, 1, -1 + i * deltaV));
		}
		numLinkanchorsH = numH;
		numLinkanchorsV = numV;
	}

	boolean showLinkAnchors = false;

	public void showLinkAnchors() {
		if(!showLinkAnchors) {
			showLinkAnchors = true;
			markDirty();
		}
	}

	public void hideLinkAnchors() {
		if(showLinkAnchors) {
			showLinkAnchors = false;
			markDirty();
		}
	}

	public LinkAnchor getLinkAnchorAt(Point2D p) {
		for(LinkAnchor la : getLinkAnchors()) {
			if(la.getMatchArea().contains(p)) {
				return la;
			}
		}
		return null;
	}

	@Override protected void destroy()
	{
		super.destroy();
		canvas.removeVElementMouseListener(this);
	}
}
