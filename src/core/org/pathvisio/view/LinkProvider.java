package org.pathvisio.view;

import java.awt.geom.Point2D;

/**
 * Classes that implement this interface can provide anchor points to
 * which a point can link
 * @author thomas
 */
public interface LinkProvider {
	public void showLinkAnchors();
	
	public void hideLinkAnchors();
	
	public LinkAnchor getLinkAnchorAt(Point2D p);
}
