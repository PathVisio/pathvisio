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

import org.pathvisio.core.model.PathwayElement;

/**
 * Represents the view of a PathwayElement with ObjectType.LABEL.
 */
public class Label extends GraphicsShape
{
	/**
	 * Constructor for this class
	 * @param canvas - the VPathway this label will be part of
	 */
	public Label(VPathway canvas, PathwayElement o)
	{
		super(canvas, o);
	}

}
