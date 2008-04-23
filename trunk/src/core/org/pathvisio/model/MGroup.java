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
	
	/**
	 * Iterates over all group elements to find
	 * the total rectangular bounds
	 */
	public Rectangle2D getMBounds() {
		Rectangle2D bounds = null;
		for(PathwayElement e : getGroupElements()) {
			if(e == this) continue; //To prevent recursion error
			if(bounds == null) bounds = e.getMBounds();
			else bounds.add(e.getMBounds());
		}
		return bounds == null ? new Rectangle2D.Double() : bounds;
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
