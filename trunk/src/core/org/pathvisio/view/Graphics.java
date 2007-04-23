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

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Region;

import org.pathvisio.preferences.Preferences;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.PathwayListener;


/**
 * This class is a parent class for all graphics
 * that can be added to a VPathway.
 */
public abstract class Graphics extends VPathwayElement implements PathwayListener
{
	public static RGB selectColor = Preferences.getColorProperty(Preferences.PREF_COL_SELECTED);
	public static RGB highlightColor = Preferences.getColorProperty(Preferences.PREF_COL_HIGHLIGHTED);
	
	protected PathwayElement gdata = null;
	
	public Graphics(VPathway canvas, PathwayElement o) {
		super(canvas);
		o.addListener(this);
		gdata = o;
	}
	
	public void select()
	{
		super.select();
		for (Handle h : getHandles())
		{
			h.show();
		}
	}
	
	public void deselect()
	{
		super.deselect();
		for (Handle h : getHandles())
		{
			h.hide();
		}
	}
	
	public PathwayElement getGmmlData() {
		return gdata;
	}
	
//	public List getAttributes() { return gdata.getAttributes() ;}
	boolean listen = true;
	public void gmmlObjectModified(PathwayEvent e) {	
		if(listen) markDirty(); // mark everything dirty
	}
	
	public Region createVisualizationRegion() {
		Region region = new Region();
		java.awt.Rectangle r = getVBounds();
		region.add(r.x, r.y, r.width, r.height);
		return region;
	}
	
	
	/**
	 * Get the x-coordinate of the center point of this object
	 * adjusted to the current zoom factor

	 * @return the center x-coordinate as integer
	 */
	public int getVCenterX() { return (int)(vFromM(gdata.getMCenterX())); }
	
	/**
	 * Get the y-coordinate of the center point of this object
	 * adjusted to the current zoom factor
	 * 
	 * @return the center y-coordinate as integer
	 */
	public int getVCenterY() { return (int)(vFromM(gdata.getMCenterY())); }

	public int getVLeft() { return (int)(vFromM(gdata.getMLeft())); }
	public int getVWidth() { return (int)(vFromM(gdata.getMWidth()));  }
	public int getVTop() { return (int)(vFromM(gdata.getMTop())); }
	public int getVHeight() { return (int)(vFromM(gdata.getMHeight())); }
	
	/**
	 * Get the x-coordinate of the center point of this object
	 * adjusted to the current zoom factor

	 * @return the center x-coordinate as double
	 */
	public double getVCenterXDouble() { return vFromM(gdata.getMCenterX()); }
	
	/**
	 * Get the y-coordinate of the center point of this object
	 * adjusted to the current zoom factor
	 * 
	 * @return the center y-coordinate as double
	 */
	public double getVCenterYDouble() { return vFromM(gdata.getMCenterY()); }

	public double getVLeftDouble() { return vFromM(gdata.getMLeft()); }
	public double getVWidthDouble() { return vFromM(gdata.getMWidth());  }
	public double getVTopDouble() { return vFromM(gdata.getMTop()); }
	public double getVHeightDouble() { return vFromM(gdata.getMHeight()); }
	
	protected void destroy() {
		super.destroy();
		gdata.removeListener(canvas);
		Pathway parent = gdata.getParent();
		if(parent != null) parent.remove(gdata);
	}
	
}