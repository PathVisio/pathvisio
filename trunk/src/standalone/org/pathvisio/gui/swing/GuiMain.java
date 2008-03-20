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
import javax.swing.UIManager;

import org.pathvisio.Globals;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.GlobalPreference;

/**
 * Main class for the Swing GUI. This class creates and shows the GUI.
 * Subclasses may override {@link #createAndShowGUI(MainPanel)} to perform custom
 * actions before showing the GUI.
 * @author thomas
 *
 */
public class GuiMain {
	private String[] args;
	private JFrame frame;
	protected MainPanelStandalone mainPanel;
	
	/**
	 * Creates and shows the GUI. Creates and shows the Frame, sets the size, title and menubar.
	 * @param mainPanel The main panel to show in the frame
	 */
	protected void createAndShowGUI(MainPanelStandalone mainPanel) {
		GuiInit.init();
		
		//Create and set up the window.
		frame = new JFrame(Globals.APPLICATION_NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.add(mainPanel);
		frame.setJMenuBar(mainPanel.getMenuBar());
		frame.setSize(800, 600);
		try {
		    UIManager.setLookAndFeel(
		        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Logger.log.error("Unable to load native look and feel", ex);
		}
		frame.pack();
		//Display the window.
		frame.setVisible(true);

		int spPercent = GlobalPreference.getValueInt(GlobalPreference.GUI_SIDEPANEL_SIZE);
		double spSize = (100 - spPercent) / 100.0;
		mainPanel.getSplitPane().setDividerLocation(spSize);
	}

	public JFrame getFrame() { return frame; }
	
	public MainPanel getMainPanel() { return mainPanel; }
	
	public String[] getArgs() { return args; }
	
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public static void main(String[] args) {
		final GuiMain gui = new GuiMain();
		gui.args = args;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainPanelStandalone mps = new MainPanelStandalone();
				SwingEngine.getCurrent().setApplicationPanel(mps);
				gui.createAndShowGUI(mps);
			}
		});
	}
}
