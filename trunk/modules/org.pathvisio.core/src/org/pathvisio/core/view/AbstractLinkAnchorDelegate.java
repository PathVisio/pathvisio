package org.pathvisio.core.view;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLinkAnchorDelegate implements LinkProvider
{
	protected List<LinkAnchor> linkAnchors = new ArrayList<LinkAnchor>();	
	
	public void createLinkAnchors() 
	{
		linkAnchors.clear();
	}
	
	public void hideLinkAnchors() 
	{
		for (LinkAnchor la : linkAnchors)
		{
			la.destroy();
		}
		linkAnchors.clear();
	}

	/* (non-Javadoc)
	 * @see org.pathvisio.core.view.LinkAnchorDelegate#getLinkAnchorAt(java.awt.geom.Point2D)
	 */
	public LinkAnchor getLinkAnchorAt(Point2D p) {
		for(LinkAnchor la : linkAnchors) {
			if(la.getMatchArea().contains(p)) {
				return la;
			}
		}
		return null;
	}

}
