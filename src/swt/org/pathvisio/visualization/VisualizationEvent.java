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
   visualization settings or addintion / removal of a visualization.

   This enables ui elements to keep up-to-date
   with visualizations while the user adjusts them.
 */
public class VisualizationEvent extends EventObject
{
	private static final long serialVersionUID = 1L;

	/** event type for when a new visualization is added to the visualizationmanager */
	public static final int VISUALIZATION_ADDED = 3;
	/** event type for when a visualization is removed from the visualizationmanager */
	public static final int VISUALIZATION_REMOVED = 4;
	/** event for when the visualization model was modified */
	public static final int VISUALIZATION_MODIFIED = 5;
	/** event for when a different visualization is selected */
	public static final int VISUALIZATION_SELECTED = 6;

	// TODO: make a separate event type for plugins
	public static final int PLUGIN_MODIFIED = 7;
	public static final int PLUGIN_ADDED = 8;
	public static final int PLUGIN_SIDEPANEL_ACTIVATED = 9;

	private int type;
	
	/** get the type of this event, must be one of the VisualizationEvent.XXX constants */
	public int getType () { return type; }

	/**
	   Create a new VisualizationEvent with the specified event type and source.
	   EventType must be one of the VisualizatonEvent.XXX constants.
	   source can not be null.
	 */
	public VisualizationEvent(Object source, int type)
	{
		super(source);
		this.type = type;
	}
}
