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

import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.util.Utils;

/**
 * State-specific implementation of methods that calculate derived
 * coordinates that are not stored in GPML directly
 */
public class MState extends PathwayElement implements GraphRefContainer
{
	protected MState()
	{
		super(ObjectType.STATE);
	}

	@Override
	public int getZOrder()
	{
		PathwayElement dn = getParentDataNode();
		if (dn == null) return 0; //TODO: must be cached like centerX etc.
		return dn.getZOrder() + 1;
	}
	
	public PathwayElement getParentDataNode()
	{
		Pathway parent = getParent();
		if (parent == null) 
			return null;

		return parent.getElementById(getGraphRef());
	}
	
	private void updateCoordinates()
	{
		PathwayElement dn = getParentDataNode();
		if (dn != null)
		{
			double centerx = dn.getMCenterX() + (getRelX() * dn.getMWidth() / 2);
			double centery = dn.getMCenterY() + (getRelY() * dn.getMHeight() / 2);
			setMCenterY(centery);
			setMCenterX(centerx);
		}
	}

	@Override
	public void linkTo(GraphIdContainer idc, double relX, double relY)
	{
		String id = idc.getGraphId();
		if(id == null) id = idc.setGeneratedGraphId();
		setGraphRef(idc.getGraphId());
		setRelX(relX);
		setRelY(relY);
	}

	@Override
	public void unlink()
	{
		// called when referred object is being destroyed.
		// destroy self.
		parent.remove(this);
	}

	@Override
	public void setRelX(double value)
	{
		super.setRelX(value);
		updateCoordinates();
	}
	
	@Override
	public void setRelY(double value)
	{
		super.setRelY(value);
		updateCoordinates();
	}
	
	@Override
	public void refeeChanged()
	{
		updateCoordinates();
	}

	public void setGraphRef(String v)
	{
		if (!Utils.stringEquals(graphRef, v))
		{
			if (parent != null)
			{
				if (graphRef != null)
				{
					parent.removeGraphRef(graphRef, this);
				}
				if (v != null)
				{
					parent.addGraphRef(v, this);
					updateCoordinates();
				}
			}
			graphRef = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.GRAPHREF));
		}
	}

	@Override
	public void setParent(Pathway v)
	{
		if (parent != v)
		{
			super.setParent(v);
			if (parent != null && graphRef != null)
			{
				updateCoordinates();
			}
		}
	}

}
