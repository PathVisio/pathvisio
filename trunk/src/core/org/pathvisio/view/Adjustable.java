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
package org.pathvisio.view;

import java.awt.geom.Rectangle2D;

/**
 * Any object that has handles and can be adjusted by them
 */
public interface Adjustable
{
	/**
	 * Transforms this object to fit to the coordinates
	 * passed on by the given handle
	 * @param h	The Handle to adjust to
	 */
	public void adjustToHandle(Handle h, double vx, double vy);

	/** only needs to be implemented for Handles that are not type Handle.DIRECTION_FREE */
	public Rectangle2D getVBounds();
	
	/** only needs to be implemented for Handles that are not type Handle.DIRECTION_FREE */
	public double getVWidth();
	
	public double getVHeight();
	//public Rectangle2D calculateVOutline();
}
