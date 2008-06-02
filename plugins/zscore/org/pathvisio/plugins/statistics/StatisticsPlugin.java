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
package org.pathvisio.plugins.statistics;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.plugin.Plugin;

public class StatisticsPlugin implements Plugin 
{
	public void init() 
	{
		StatisticsAction statisticsAction = new StatisticsAction();

		Logger.log.info ("Initializing statistics plugin");
		SwingEngine.getCurrent().registerMenuAction ("Data", statisticsAction);
	}
	
	public static class StatisticsAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public StatisticsAction() 
		{
			super();
			putValue(NAME, "Statistics...");
			putValue(SHORT_DESCRIPTION, "Do simple pathway statistics");
		}

		public void actionPerformed(ActionEvent e) 
		{
			JOptionPane.showMessageDialog(SwingEngine.getCurrent().getFrame(), "Action not implemented");
		}
	}


}