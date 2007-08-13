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
package org.pathvisio.gui.swt;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Engine;
import org.pathvisio.data.Gex;

/**
 * This class is the main class in the GPML project. 
 * It acts as a container for pathwaydrawings and facilitates
 * loading, creating and saving drawings to and from GPML.
 */
public class MainWindow extends MainWindowBase
{
	public MainWindow()
	{
		this(null);
	}
	
	/**
	 *Constructor for the MainWindow class
	 *Initializes new MainWindow and sets properties for frame
	 */
	public MainWindow(Shell shell)
	{
		super(shell);
		
		addMenuBar();
		addStatusLine();
		addCoolBar(SWT.FLAT | SWT.LEFT);
		
		Engine.getCurrent().addApplicationEventListener(this);
		Gex.addListener(this);
	}
	
	/**
	 *Builds and ads a menu to the frame
	 */
	protected MenuManager createMenuManager()
	{
		menuManager = new MenuManager();
		MenuManager fileMenu = new MenuManager ("&File");
		fileMenu.add(newAction);
		fileMenu.add(openAction);
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(new Separator());
		fileMenu.add(importAction);
		fileMenu.add(exportAction);
		fileMenu.add(new Separator());
		fileMenu.add(exitAction);
		MenuManager editMenu = new MenuManager ("&Edit");
		editMenu.add(undoAction);
		editMenu.add(new Separator());
		editMenu.add(copyAction);
		editMenu.add(pasteAction);
		editMenu.add(new Separator());
		editMenu.add(switchEditModeAction);
		editMenu.add(preferencesAction);
		MenuManager viewMenu = new MenuManager ("&View");
		viewMenu.add(showRightPanelAction);
		MenuManager zoomMenu = new MenuManager("&Zoom");
		zoomMenu.add(new CommonActions.ZoomAction(this, 50));
		zoomMenu.add(new CommonActions.ZoomAction(this, 75));
		zoomMenu.add(new CommonActions.ZoomAction(this, 100));
		zoomMenu.add(new CommonActions.ZoomAction(this, 125));
		zoomMenu.add(new CommonActions.ZoomAction(this, 150));
		zoomMenu.add(new CommonActions.ZoomAction(this, 200));
		zoomMenu.add(new CommonActions.ZoomAction(this, ZOOM_TO_FIT));
		viewMenu.add(zoomMenu);
		MenuManager dataMenu = new MenuManager ("&Data");
		dataMenu.add(selectGdbAction);
		
		MenuManager helpMenu = new MenuManager ("&Help");
		helpMenu.add(aboutAction);
		helpMenu.add(helpAction);
		menuManager.add(fileMenu);
		menuManager.add(editMenu);
		menuManager.add(viewMenu);
		menuManager.add(dataMenu);
		menuManager.add(helpMenu);
		return menuManager;
	}
	
	protected CoolBarManager createCoolBarManager(int style)
	{
		createCommonActionsCI();
		createEditActionsCI();
		createAlignActionsCI();
		createViewActionsCI();
		
		CoolBarManager coolBarManager = new CoolBarManager(style);
		coolBarManager.setLockLayout(true);
		
		coolBarManager.add(commonActionsCI);
		coolBarManager.add(viewActionsCI);
		return coolBarManager;
	}
		
	protected void addPanelTabs() {
		rightPanel.addTab(bpBrowser, "Backpage");
		rightPanel.addTab(propertyTable, "Properties");
		rightPanel.addTab(pwSearchComposite, "Pathway Search");
		rightPanel.addTab(legend, "Legend");
	}
	
} // end of class
