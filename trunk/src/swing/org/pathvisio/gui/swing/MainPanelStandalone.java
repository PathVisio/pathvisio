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

import com.mammothsoftware.frwk.ddb.DropDownButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.CommonActions.ZoomAction;
import org.pathvisio.util.Resources;

/**
 * the mainPanel for the standalone (non-applet) version of PathVisio.
 */
public class MainPanelStandalone extends MainPanel 
{
	protected JMenuBar menuBar;
	
	private StandaloneActions standaloneActions = null;
	
	@Override
	protected void addMenuActions(JMenuBar mb) {
		JMenu fileMenu = new JMenu("File");
		
		addToMenu(standaloneActions.newAction, fileMenu);
		addToMenu(standaloneActions.openAction, fileMenu);
		addToMenu(actions.standaloneSaveAction, fileMenu);
		addToMenu(actions.standaloneSaveAsAction, fileMenu);
		fileMenu.addSeparator();
		addToMenu(actions.importAction, fileMenu);
		addToMenu(actions.exportAction, fileMenu);
		fileMenu.addSeparator();
		addToMenu(actions.exitAction, fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		addToMenu(actions.undoAction, editMenu);
		addToMenu(actions.copyAction, editMenu);
		addToMenu(actions.pasteAction, editMenu);
		addToMenu(standaloneActions.searchAction, editMenu);
		editMenu.addSeparator();
		addToMenu(standaloneActions.preferencesAction, editMenu);
		
		JMenu selectionMenu = new JMenu("Selection");
		for(Action a : actions.layoutActions) addToMenu(a, selectionMenu);
		
		editMenu.add (selectionMenu);

		JMenu dataMenu = new JMenu("Data");
		addToMenu (standaloneActions.selectGeneDbAction, dataMenu);
		addToMenu (standaloneActions.selectMetaboliteDbAction, dataMenu);

		JMenu viewMenu = new JMenu("View");
		JMenu zoomMenu = new JMenu("Zoom");
		viewMenu.add(zoomMenu);
		for(Action a : actions.zoomActions) addToMenu(a, zoomMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(standaloneActions.aboutAction);
		helpMenu.add(standaloneActions.helpAction);
		
		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(dataMenu);
		mb.add(viewMenu);
		mb.add(helpMenu);
	}
	
	public MainPanelStandalone(Engine engine, final SwingEngine swingEngine)
	{
		super(swingEngine, null);
		
		SearchPane searchPane = new SearchPane(engine, swingEngine);
		sidebarTabbedPane.addTab ("Search", searchPane); 
	}

	@Override
	protected void addToolBarActions(final SwingEngine swingEngine, JToolBar tb) 
	{
		tb.setLayout(new WrapLayout(1, 1));

		standaloneActions = new StandaloneActions(swingEngine);		

		addToToolbar(standaloneActions.newAction);
		addToToolbar(standaloneActions.openAction);
		addToToolbar(actions.standaloneSaveAction);
		tb.addSeparator();
		addToToolbar(actions.copyAction);
		addToToolbar(actions.pasteAction);
		
		tb.addSeparator();

		addToToolbar(actions.undoAction);
		
		tb.addSeparator();

		addToToolbar(new JLabel("Zoom:", JLabel.LEFT));
		JComboBox combo = new JComboBox(actions.zoomActions);
		combo.setMaximumSize(combo.getPreferredSize());
		combo.setEditable(true);
		combo.setSelectedIndex(5); // 100%
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				Object s = combo.getSelectedItem();
				if (s instanceof Action) {
					((Action) s).actionPerformed(e);
				} else if (s instanceof String) {
					String zs = (String) s;
					try {
						double zf = Double.parseDouble(zs);
						ZoomAction za = new ZoomAction(swingEngine.getEngine(), zf);
						za.setEnabled(true);
						za.actionPerformed(e);
					} catch (Exception ex) {
						// Ignore bad input
					}
				}
			}
		});
		addToToolbar(combo, TB_GROUP_SHOW_IF_VPATHWAY);

		tb.addSeparator();

		String submenu = "line";
		
		for(Action[] aa : actions.newElementActions) {
			if(aa.length == 1) {
				addToToolbar(aa[0]);
			} else { //This is the line/receptor sub-menu
				String icon = "newlinemenu.gif";
				String tooltip = "Select a line to draw"; 
				
				if(submenu.equals("receptors")) { //Next one is receptors
					icon = "newlineshapemenu.gif";
					tooltip = "Select a receptor/ligand to draw";
				} else {
					submenu = "receptors";
				}
				DropDownButton lineButton = new DropDownButton(new ImageIcon(
						Resources.getResourceURL(icon)));
				lineButton.setToolTipText(tooltip);
				for(Action a : aa) {
					lineButton.addComponent(new JMenuItem(a));
				}
				addToToolbar(lineButton, TB_GROUP_SHOW_IF_EDITMODE);
			}
		}
				
		tb.addSeparator();
		
		addToToolbar(actions.layoutActions);
	}
	
}
