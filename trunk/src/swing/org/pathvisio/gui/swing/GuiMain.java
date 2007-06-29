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

import javax.swing.JFrame;

import org.pathvisio.Engine;
import org.pathvisio.view.swing.VPathwaySwing;

public class GuiMain {

	private static void createAndShowGUI() {
		GuiInit.init();
		
		//Create and set up the window.
		JFrame frame = new JFrame("PathVisio...swing it baby!");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		MainPanel mainPanel = SwingEngine.getApplicationPanel();
		frame.add(mainPanel);
		frame.setJMenuBar(mainPanel.getMenuBar());
		frame.setSize(800, 600);
		
//		try {
//		    UIManager.setLookAndFeel(
//		        UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception ex) {
//			Engine.log.error("Unable to load native look and feel", ex);
//		}
		
		//Display the window.
		frame.setVisible(true);
		Engine.newPathway(new VPathwaySwing(mainPanel.getScrollPane()));
		Engine.getActiveVPathway().setEditMode(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
