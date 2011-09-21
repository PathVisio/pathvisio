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
package org.pathvisio.core.view;

import java.net.URL;

import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;


/**
 * Represents a template that consists of graphics and
 * can be added to a pathway.
 * @author thomas
 */
public interface Template {
	/**
	 * Create and add the pathway elements for this template to the given pathway.
	 * Coordinates mx and my represent the point where the template
	 * has to be inserted (in model coordinates)
	 * @param mx	The x coordinate of the base point
	 * @param my	The y coordinate of the base point
	 * @return A list with the elements that where added by the template
	 */
	PathwayElement[] addElements(Pathway p, double mx, double my);

	/**
	 * Get the element that will be used to drag the template after
	 * adding (e.g. the South-East handle in case of a DataNode)
	 * @return The drag element of the last added template graphics
	 */
	VPathwayElement getDragElement(VPathway vp);

	/**
	 * Get the name for this template
	 * @return
	 */
	String getName();

	/**
	 * Get the description for this template
	 * @return
	 */
	String getDescription();

	/**
	 * Get the location of the icon for this template
	 * @return The icon location, or null if the template doesn't have an icon
	 */
	URL getIconLocation();


	void postInsert(PathwayElement[] newElements);
}
