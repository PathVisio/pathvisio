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

/**
 * State-specific implementation of methods that calculate derived
 * coordinates that are not stored in GPML directly
 */
public class MState extends PathwayElement
{
	protected MState()
	{
		super(ObjectType.STATE);
	}

	public double getMCenterX()
	{
		PathwayElement dn = getParentDataNode();
		return dn.getMCenterX() + (getRelX() * dn.getMWidth() / 2);
	}

	public double getMCenterY()
	{
		PathwayElement dn = getParentDataNode();
		return dn.getMCenterY() + (getRelY() * dn.getMHeight() / 2);
	}

	public double getMLeft()
	{
		return getMCenterX() - getMWidth() / 2;
	}

	public double getMTop()
	{
		return getMCenterY() - getMHeight() / 2;
	}

	public void setMCenterX(double v)
	{
		//TODO (not sure if you should be able to set this directly)
	}

	public void setMCenterY(double v)
	{
		//TODO (not sure if you should be able to set this directly)
	}

	public void setMLeft(double v)
	{
		//TODO (not sure if you should be able to set this directly)
	}

	public void setMTop(double v)
	{
		//TODO (not sure if you should be able to set this directly)
	}

	private PathwayElement getParentDataNode()
	{
		Pathway parent = getParent();
		if (parent == null) return null;

		return parent.getElementById(getGraphRef());
	}
}
