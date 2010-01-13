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
package org.pathvisio.model;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

/**
 * Group specific implementation of methods that calculate derived
 * coordinates that are not stored in GPML directly
 * @author thomas
 */
public class MGroup extends PathwayElement {
	protected MGroup() {
		super(ObjectType.GROUP);
	}

	/**
	 * Center x of the group bounds
	 */
	public double getMCenterX() {
		return getMBounds().getCenterX();
	}

	/**
	 * Center y of the group bounds
	 */
	public double getMCenterY() {
		return getMBounds().getCenterY();
	}

	/**
	 * Height of the group bounds
	 */
	public double getMHeight() {
		return getMBounds().getHeight();
	}

	/**
	 * Left of the group bounds
	 */
	public double getMLeft() {
		return getMBounds().getX();
	}

	/**
	 * Top of the group bounds
	 */
	public double getMTop() {
		return getMBounds().getY();
	}

	/**
	 * Width of the group bounds
	 */
	public double getMWidth() {
		return getMBounds().getWidth();
	}

	public void setMCenterX(double v) {
		double d = v - getMBounds().getCenterX();
		for(PathwayElement e : getGroupElements()) {
			e.setMCenterX(e.getMCenterX() + d);
		}
	}

	public void setMCenterY(double v) {
		double d = v - getMBounds().getCenterY();
		for(PathwayElement e : getGroupElements()) {
			e.setMCenterY(e.getMCenterY() + d);
		}
	}

	public void setMHeight(double v) {
		double d = v - getMBounds().getHeight();
		for(PathwayElement e : getGroupElements()) {
			e.setMHeight(e.getMHeight() + d);
		}
	}

	public void setMWidth(double v) {
		double d = v - getMBounds().getWidth();
		for(PathwayElement e : getGroupElements()) {
			e.setMWidth(e.getMWidth() + d);
		}
	}

	public void setMLeft(double v) {
		double d = v - getMBounds().getX();
		for(PathwayElement e : getGroupElements()) {
			e.setMLeft(e.getMLeft() + d);
		}
	}

	public void setMTop(double v) {
		double d = v - getMBounds().getY();
		for(PathwayElement e : getGroupElements()) {
			e.setMTop(e.getMTop() + d);
		}
	}

	static final double BOUNDS_SPACING = 8 * 15; //Make the bounds slightly
											  //larger than the summed bounds
											  //of the containing elements
	/**
	 * Iterates over all group elements to find
	 * the total rectangular bounds.
	 * Note: doesn't include rotation of the nested elements.
	 * If you want to include rotation, use {@link #getRBounds()} instead.
	 */
	public Rectangle2D getMBounds() {
		Rectangle2D bounds = null;
		for(PathwayElement e : getGroupElements()) {
			if(e == this) continue; //To prevent recursion error
			if(bounds == null) bounds = e.getMBounds();
			else bounds.add(e.getMBounds());
		}
		if(bounds != null) {
			return new Rectangle2D.Double(
				bounds.getX() - BOUNDS_SPACING,
				bounds.getY() - BOUNDS_SPACING,
				bounds.getWidth() + 2*BOUNDS_SPACING,
				bounds.getHeight() + 2*BOUNDS_SPACING
			);
		} else {
			return new Rectangle2D.Double();
		}
	}

	/**
	 * Iterates over all group elements to find
	 * the total rectangular bounds, taking into
	 * account rotation of the nested elements
	 */
	public Rectangle2D getRBounds() {
		Rectangle2D bounds = null;
		for(PathwayElement e : getGroupElements()) {
			if(e == this) continue; //To prevent recursion error
			if(bounds == null) bounds = e.getRBounds();
			else bounds.add(e.getRBounds());
		}
		if(bounds != null) {
			return new Rectangle2D.Double(
				bounds.getX() - BOUNDS_SPACING,
				bounds.getY() - BOUNDS_SPACING,
				bounds.getWidth() + 2 * BOUNDS_SPACING,
				bounds.getHeight() + 2 * BOUNDS_SPACING
			);
		} else {
			return new Rectangle2D.Double();
		}
	}

	/**
	 * Get the group elements. Convenience method that
	 * checks for a valid parent and never returns
	 * null
	 */
	private Set<PathwayElement> getGroupElements() {
		Set<PathwayElement> result = new HashSet<PathwayElement>();
		Pathway parent = getParent();
		if(parent != null) {
			result = parent.getGroupElements(getGroupId());
		}
		return result;
	}
}
