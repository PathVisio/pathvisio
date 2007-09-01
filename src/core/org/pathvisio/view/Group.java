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

import org.pathvisio.model.PathwayElement;

public class Group extends Graphics {

	public Group(VPathway canvas, PathwayElement pe) {
		super(canvas, pe);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Determines wheter a Graphics object contains the point specified
	 * 
	 * @param point -
	 *            the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected boolean vContains(Point2D point) {
		for (VPathwayElement vpe : canvas.getDrawingObjects()) {
			if (!(vpe instanceof Group)  && vpe.vContains(point)) {
				if (vpe instanceof Graphics) {
					PathwayElement pe = ((Graphics) vpe).getPathwayElement();
					String ref = pe.getGroupRef();
//					String ref2 = getGmmlData().getGroupId();
//					System.out.println("pe: "+pe+" ref: "+ref+" ref2: "+ref2);
					if (ref != null && ref.equals(getPathwayElement().getGroupId())) {
//						System.out.println("Contains: "+ref);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected boolean vIntersects(Rectangle2D r) {
		for (VPathwayElement vpe : canvas.getDrawingObjects()) {
			if (!(vpe instanceof Group) && vpe.vIntersects(r)) {
				if (vpe instanceof Graphics) {
					PathwayElement pe = ((Graphics) vpe).getPathwayElement();
					String ref = pe.getGroupRef();
					if (ref != null && ref.equals(getPathwayElement().getGroupId())) {
						//System.out.println("Intersects: "+ref);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public ArrayList<Graphics> getGroupGraphics(){
		ArrayList<Graphics> gg = new ArrayList<Graphics>();
		for (VPathwayElement vpe : canvas.getDrawingObjects()) {
			if (vpe != this) {
				if (vpe instanceof Graphics) {
					Graphics vpeg = (Graphics) vpe;
					PathwayElement pe =  vpeg.getPathwayElement();
					String ref = pe.getGroupRef();
					if (ref != null && ref.equals(getPathwayElement().getGroupId())) {
						gg.add(vpeg);					
					}
				}
			}
		}
		return gg;
	}
	
	@Override
	public void select() {
		for(Graphics g: getGroupGraphics() ){
			g.select();
		}
		super.select();
	}

	@Override
	protected void vMoveBy(double dx, double dy) {
		for(Graphics g: getGroupGraphics() ){
			g.vMoveBy(dx, dy);
		}
		//super.vMoveBy(dx, dy);
	}
	
	@Override
	public int getNaturalOrder() {

		return VPathway.DRAW_ORDER_GROUP;
	}

	@Override
	protected void doDraw(Graphics2D g2d) {
		// TODO make unique selection box for groups
		
	}
	
	@Override
	protected Shape getVOutline() {
		// TODO Return outline of the Group members, distinct from global selection box
		
		Rectangle rect = new Rectangle();

		return rect;
	}

	protected Shape getVShape(boolean rotate) {
		// TODO Auto-generated method stub
		return new Rectangle();
	}
	
	protected void setVScaleRectangle(Rectangle2D r) {
		// TODO Auto-generated method stub
		
	}
}
