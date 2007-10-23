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
package org.pathvisio.visualization;

import java.util.EventObject;

/**
   Event fired in response to changes to the visualization model, such
   as the addition / modification of a plugin, changes to
   visualization settings or colorsets.

   This enables ui elements to keep up-to-date
   with visualizations while the user adjusts them.
 */
public class VisualizationEvent extends EventObject
{
	private static final long serialVersionUID = 1L;
	public static final int COLORSET_ADDED = 0;
	public static final int COLORSET_REMOVED = 1;
	public static final int COLORSET_MODIFIED = 2;
	public static final int VISUALIZATION_ADDED = 3;
	public static final int VISUALIZATION_REMOVED = 4;
	public static final int VISUALIZATION_MODIFIED = 5;
	public static final int VISUALIZATION_SELECTED = 6;
	public static final int PLUGIN_MODIFIED = 7;
	public static final int PLUGIN_ADDED = 8;
	public static final int PLUGIN_SIDEPANEL_ACTIVATED = 9;

	public Object source;
	public int type;

	public VisualizationEvent(Object source, int type)
	{
		super(source == null ? VisualizationManager.class : source);
		this.source = source;
		this.type = type;
	}
}
