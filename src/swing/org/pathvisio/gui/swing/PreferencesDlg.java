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
package org.pathvisio.gui.swing;

import javax.swing.JPanel;

import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;

/**
 * Global dialog for setting the user preferences.
 */
public class PreferencesDlg extends AbstractPreferenceDlg
{
	PreferencesDlg ()
	{
		super (PreferenceManager.getCurrent());
	}
	
	public void initPanels()
	{		
		PreferencePanelBuilder builder;
		
		builder = createBuilder();

		builder.addIntegerField (
				GlobalPreference.GUI_SIDEPANEL_SIZE,
				"Initial side panel size (percent of window size):", 0, 100);
		
		builder.addBooleanField ( 
			GlobalPreference.DATANODES_ROUNDED,
			"Use rounded rectangles for data nodes");

		builder.addBooleanField(
				GlobalPreference.SNAP_TO_ANGLE, 
				"Snap to angle when moving line and rotation handles");

		builder.addIntegerField (
				GlobalPreference.SNAP_TO_ANGLE_STEP,
				"Distance between snap-steps in degrees:", 1, 90);

		builder.addBooleanField (
				GlobalPreference.MIM_SUPPORT,
				"Load support for molecular interaction maps (MIM) at program start");

		builder.addBooleanField (
				GlobalPreference.SHOW_ADVANCED_ATTRIBUTES,									   
				"Show advanced attributes (e.g. references)");

		builder.addBooleanField (
				GlobalPreference.USE_SYSTEM_LOOK_AND_FEEL,									   
				"Use Java System look-and-feel at program start");

		JPanel displayPanel = builder.getPanel();
		
		builder = createBuilder();
		
		builder.addColorField(
				GlobalPreference.COLOR_NO_CRIT_MET, 
				"Default color for 'no criteria met':");

		builder.addColorField(
				GlobalPreference.COLOR_NO_GENE_FOUND, 
				"Default color for 'gene not found':");

		builder.addColorField(
				GlobalPreference.COLOR_NO_DATA_FOUND, 
				"Default color for 'no data found':");

		builder.addColorField(
				GlobalPreference.COLOR_SELECTED, 
				"Line color for selected objects:");

		builder.addColorField(
				GlobalPreference.COLOR_HIGHLIGHTED,
				"Highlight color");

		JPanel colorPanel = builder.getPanel();
		
		builder = createBuilder();

		builder.addFileField(
				GlobalPreference.FILE_LOG, 
				"Log file:", false);
		
		JPanel filePanel = builder.getPanel();;

		builder = createBuilder();

		builder.addFileField (GlobalPreference.DIR_PWFILES,
				"Gpml pathways:", true);
					
		builder.addFileField (GlobalPreference.DIR_GDB,
				"Gene databases:", true);
		
		builder.addFileField (GlobalPreference.DIR_EXPR,
				"Expression datasets:", true);		
		
		JPanel dirPanel = builder.getPanel();;

		builder = createBuilder();

		builder.addStringField (GlobalPreference.DB_ENGINE_GDB,
				"Database connector class for gene database:");

		builder.addStringField (GlobalPreference.DB_ENGINE_GEX,
				"Database connector class for expression dataset:");
		
		JPanel dbPanel = builder.getPanel();

		addPanel ("Display", displayPanel);
		addPanel ("Display.Colors", colorPanel);
		addPanel ("Directories", dirPanel);
		addPanel ("Files", filePanel);
		addPanel ("Database", dbPanel);
	}
	
}
