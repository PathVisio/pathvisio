// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.view;

/**
 * Utility class for creating and destroying LinkAnchors around a rectangular element.
 */
public class DefaultLinkAnchorDelegate extends AbstractLinkAnchorDelegate 
{
	private final Graphics parent;
	private final VPathway canvas;
	
	DefaultLinkAnchorDelegate(Graphics parent)
	{
		this.parent = parent;
		this.canvas = parent.getDrawing();
	}

	private int numLinkanchorsH = -1;
	private int numLinkanchorsV = -1;

	private static final int MIN_SIZE_LA = 25;

	public void showLinkAnchors() 
	{
        if (parent instanceof Group && 
        	parent.gdata.getGroupStyle().isDisallowLinks()) 
        {
            return;
        }
		//Number of link anchors depends on the size of the object
		//If the width/height is large enough, there will be three link anchors per side,
		//Otherwise there will be only one link anchor per side
		String anchorsCnt = parent.gdata.getDynamicProperty("NumberOfAnchors");
        int numAnchors = 3;
        if (anchorsCnt != null) {
            numAnchors = Integer.parseInt(anchorsCnt);
        }
        int numH = parent.gdata.getMWidth() < MIN_SIZE_LA ? 1 : numAnchors;
		int numV = parent.gdata.getMHeight() < MIN_SIZE_LA ? 1 : numAnchors;

		if(numH != numLinkanchorsH || numV != numLinkanchorsV) 
		{
			linkAnchors.clear();
			double deltaH = 2.0/(numH + 1);
			for(int i = 1; i <= numH; i++) {
				linkAnchors.add(new LinkAnchor(canvas, parent, parent.gdata, -1 + i * deltaH, -1));
				linkAnchors.add(new LinkAnchor(canvas, parent, parent.gdata, -1 + i * deltaH, 1));
			}
			double deltaV = 2.0/(numV + 1);
			for(int i = 1; i <= numV; i++) {
				linkAnchors.add(new LinkAnchor(canvas, parent, parent.gdata, -1, -1 + i * deltaV));
				linkAnchors.add(new LinkAnchor(canvas, parent, parent.gdata, 1, -1 + i * deltaV));
			}
			numLinkanchorsH = numH;
			numLinkanchorsV = numV;
		}
	}
	

	public void hideLinkAnchors() 
	{
		super.hideLinkAnchors();
		numLinkanchorsV = -1;
		numLinkanchorsH = -1;
	}

}