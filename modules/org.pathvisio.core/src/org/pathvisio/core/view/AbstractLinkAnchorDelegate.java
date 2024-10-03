/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
