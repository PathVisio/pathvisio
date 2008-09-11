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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.CommonActions.ZoomAction;
import org.pathvisio.util.Resources;

import com.mammothsoftware.frwk.ddb.DropDownButton;

import edu.stanford.ejalbert.BrowserLauncher;

public class MainPanelStandalone extends MainPanel 
{
	private static final long serialVersionUID = 1L;

	protected JMenuBar menuBar;
	
	final private StandaloneActions standaloneActions;
	
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
		JMenu alignMenu = new JMenu("Align");
		JMenu stackMenu = new JMenu("Stack");
		
		for(Action a : actions.alignActions) addToMenu(a, alignMenu);
		for(Action a : actions.stackActions) addToMenu(a, stackMenu);
		
		selectionMenu.add(alignMenu);
		selectionMenu.add(stackMenu);
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
		super(null);
		standaloneActions = new StandaloneActions(swingEngine);		
		
		SearchPane searchPane = new SearchPane(engine, swingEngine);
		sidebarTabbedPane.addTab ("Search", searchPane); 
		
		backpagePane.addHyperlinkListener(
				new HyperlinkListener() 
				{
					public void hyperlinkUpdate(HyperlinkEvent e) 
					{
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
						{
							URL url = e.getURL();
							try
							{
								BrowserLauncher b = new BrowserLauncher(null);
								b.openURLinBrowser(url.toString());
							}
							catch (Exception ex)
							{
								Logger.log.error ("Couldn't open url '" + url + "'", ex);
								JOptionPane.showMessageDialog(swingEngine.getFrame(), 
										"Error opening the Browser, see error log for details.");
							}
						}
					}
				}
				);

	}

	protected void addToolBarActions(final Engine engine, JToolBar tb) 
	{
		tb.setLayout(new WrapLayout(1, 1));
		
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
						ZoomAction za = new ZoomAction(engine, zf);
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
		
		addToToolbar(actions.alignActions);
		addToToolbar(actions.stackActions);
	}
	
}
