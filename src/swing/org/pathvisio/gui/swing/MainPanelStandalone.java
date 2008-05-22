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

import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MainPanelStandalone extends MainPanel 
{
	private static final long serialVersionUID = 1L;

	protected JMenuBar menuBar;
	
	@Override
	protected void addMenuActions(JMenuBar mb) {
		JMenu fileMenu = new JMenu("File");
		
		addToMenu(StandaloneActions.newAction, fileMenu);
		addToMenu(StandaloneActions.openAction, fileMenu);
		addToMenu(actions.saveAction, fileMenu);
		addToMenu(actions.saveAsAction, fileMenu);
		fileMenu.addSeparator();
		addToMenu(actions.importAction, fileMenu);
		addToMenu(actions.exportAction, fileMenu);
		fileMenu.addSeparator();
		addToMenu(actions.exitAction, fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		addToMenu(actions.undoAction, editMenu);
		addToMenu(actions.copyAction, editMenu);
		addToMenu(actions.pasteAction, editMenu);
		editMenu.addSeparator();
		addToMenu(StandaloneActions.preferencesAction, editMenu);
		
		JMenu selectionMenu = new JMenu("Selection");
		JMenu alignMenu = new JMenu("Align");
		JMenu stackMenu = new JMenu("Stack");
		
		for(Action a : actions.alignActions) addToMenu(a, alignMenu);
		for(Action a : actions.stackActions) addToMenu(a, stackMenu);
		
		selectionMenu.add(alignMenu);
		selectionMenu.add(stackMenu);
		editMenu.add (selectionMenu);

		JMenu dataMenu = new JMenu("Data");
		addToMenu (StandaloneActions.selectGeneDbAction, dataMenu);
		addToMenu (StandaloneActions.selectMetaboliteDbAction, dataMenu);
		dataMenu.addSeparator();
		addToMenu (StandaloneActions.importGexDataAction, dataMenu);

		JMenu viewMenu = new JMenu("View");
		JMenu zoomMenu = new JMenu("Zoom");
		viewMenu.add(zoomMenu);
		for(Action a : actions.zoomActions) addToMenu(a, zoomMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(StandaloneActions.aboutAction);
		helpMenu.add(StandaloneActions.helpAction);
		
		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(dataMenu);
		mb.add(viewMenu);
		mb.add(helpMenu);
	}
	
	public MainPanelStandalone()
	{
		this (null);
	}

	public MainPanelStandalone(Set<Action> hideActions)
	{
		super(hideActions);
		
		SearchPane searchPane = new SearchPane();
		sidebarTabbedPane.addTab ("Search", searchPane); 
	}

}
