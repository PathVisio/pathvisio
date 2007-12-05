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

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Engine;
import org.pathvisio.R.RController;
import org.pathvisio.R.RDataIn;
import org.pathvisio.R.RCommands.RException;
import org.pathvisio.R.wizard.RWizard;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.DBConnectorSwt;
import org.pathvisio.data.GdbManager;
import org.pathvisio.data.GexImportWizard;
import org.pathvisio.data.GexSwt;
import org.pathvisio.data.GexManager;
import org.pathvisio.data.GexManager.GexManagerEvent;
import org.pathvisio.data.GexManager.GexManagerListener;
import org.pathvisio.data.GexSwt.ProgressWizardDialog;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.swt.SwtPreferences.SwtPreference;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationDialog;
import org.pathvisio.visualization.VisualizationManager;

/**
 * This class is the main class in the GPML project. 
 * It acts as a container for pathwaydrawings and facilitates
 * loading, creating and saving drawings to and from GPML.
 */
public class MainWindow extends MainWindowBase
{
	/**
	 * {@link Action} to select an expression dataset
	 */
	private class SelectGexAction extends Action
	{
		MainWindow window;
		public SelectGexAction(MainWindow w)
		{
			window = w;
			setText("Select &Expression Data");
			setToolTipText("Select Expression Data");
		}
		
		public void run () 
		{
			try 
			{
				DBConnectorSwt dbcon = GexSwt.getDBConnector();
				String dbName = dbcon.openChooseDbDialog(getShell());
				
				if(dbName == null) return;
				
				GexManager.setCurrentGex(dbName, false);
			} 
			catch(Exception e) 
			{
				String msg = "Failed to open Expression Dataset" + e.getMessage();
				MessageDialog.openError (window.getShell(), "Error", 
						"Error: " + msg + "\n\n" + 
						"See the error log for details.");
				Logger.log.error(msg, e);
			}		
		}
	}
	private SelectGexAction selectGexAction = new SelectGexAction(this);
	
	/**
	 * {@link Action} to select a Gene Database
	 */
	private class SelectMetaboliteDbAction extends Action
	{
		MainWindowBase window;
		public SelectMetaboliteDbAction(MainWindowBase w)
		{
			window = w;
			setText("Select &Metabolite Database");
			setToolTipText("Select Metabolite Database");
		}
		
		public void run () {			
			try {
				DBConnectorSwt dbcon = SwtEngine.getCurrent().getSwtDbConnector(DBConnector.TYPE_GDB);
				String dbName = dbcon.openChooseDbDialog(getShell());
				
				if(dbName == null) return;
				
				GdbManager.setMetaboliteDb(dbName);
			} 
			catch(Exception e) 
			{
				String msg = "Failed to open Metabolite Database; " + e.getMessage();
				MessageDialog.openError (window.getShell(), "Error", 
						"Error: " + msg + "\n\n" + 
						"See the error log for details.");
				Logger.log.error(msg, e);
			}
		}
	}
	private SelectMetaboliteDbAction selectMetaboliteDbAction = 
		new SelectMetaboliteDbAction(this);

	/**
	 * {@link Action} that opens an {@link GexImportWizard} that guides the user
	 * through the steps required to create a new
	 * expression dataset
	 */
	private class CreateGexAction extends Action
	{
		MainWindow window;
		public CreateGexAction(MainWindow w)
		{
			window = w;
			setText("&Create new Expression Dataset");
			setToolTipText("Create a new Expression Dataset from a tab delimited text file");
		}
		
		public void run() {
			if(!GdbManager.isConnected())
			{
				MessageDialog.openWarning(getShell(), "Warning", "No gene database selected, " +
						"select gene database before creating a new expression dataset");
				return;
			}
			ProgressWizardDialog dialog = new ProgressWizardDialog(getShell(), new GexImportWizard());
			dialog.setBlockOnOpen(true);
			dialog.open();
		}
	}
	private CreateGexAction createGexAction = new CreateGexAction(this);
	
	/**
	 * {@link Action} to open the {@link ColorSetWindow}
	 */
	private class ColorSetManagerAction extends Action implements GexManagerListener
	{
		MainWindow window;
		public ColorSetManagerAction (MainWindow w)
		{
			window = w;
			setText("&Color Set manager");
			setToolTipText("Create and edit color sets");
			setImageDescriptor(ImageDescriptor.createFromURL(
					Engine.getCurrent().getResourceURL("icons/colorset.gif")));
			GexManager.addListener(this);
			setEnabled(false);
		}
		public void run () {
			VisualizationDialog d = new VisualizationDialog(getShell());
			d.setTabItemOnOpen(VisualizationDialog.TABITEM_COLORSETS);
			d.open();
		}
		public void gexManagerEvent(GexManagerEvent e) 
		{
			switch(e.getType()) 
			{
			case GexManagerEvent.CONNECTION_OPENED:
				setEnabled(true); break;
			case GexManagerEvent.CONNECTION_CLOSED:
				setEnabled(false); break;
			}	
		}
	}
	private ColorSetManagerAction colorSetManagerAction = new ColorSetManagerAction(this);
	
	private class VisualizationDialogAction extends Action
	{
		MainWindow window;
		public VisualizationDialogAction (MainWindow w)
		{
			window = w;
			setText("&Visualizations");
			setToolTipText("Create and edit visualizations");
			setImageDescriptor(ImageDescriptor.createFromURL(
					Engine.getCurrent().getResourceURL("icons/visualizations.gif")));
		}
		public void run () {
			VisualizationDialog d = new VisualizationDialog(getShell());
			d.open();
		}
	}
	private VisualizationDialogAction visualizationDialogAction = new VisualizationDialogAction(this);
	
	/**
	 * {@link Action} to open the pathway statistics wizard
	 */
	private class RStatsAction extends Action
	{
		MainWindow window;
		public RStatsAction (MainWindow w)
		{
			window = w;
			setText("Perform statistical test@Ctrl+R");
		}
		
		public void run() {
			WizardDialog wd = new RWizard.RWizardDialog(getShell(), new RWizard());
			wd.setBlockOnOpen(true);
			if(RController.startR()) wd.open();
		}
	}
	private RStatsAction rStatsAction = new RStatsAction(this);
	
	/**
	 * {@link Action} to load results from pathway statistics
	 */
	private class RLoadStatsAction extends Action
	{
		MainWindow window;
		public RLoadStatsAction (MainWindow w)
		{
			window = w;
			setText("&Load results");
		}
		
		public void run() {
			FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
			fd.setFilterPath(SwtPreference.SWT_DIR_RDATA.getValue());
			fd.setFilterNames(new String[] {"R data file"});
			fd.setFilterExtensions(new String[] {"*.*"});
			File file = new File(fd.open());
			if(file.canRead()) {
				try {
					RDataIn.displayResults(RDataIn.loadResultSets(file), file.getName());
				} catch(RException e) {
					MessageDialog.openError(getShell(), "Unable to load results", e.getMessage());
				}
			}
		}
	}
	private RLoadStatsAction rLoadStatsAction = new RLoadStatsAction(this);
	
	protected void createViewActionsCI() {
		super.createViewActionsCI();
		//Add swich to editmode
		viewActionsCI.getToolBarManager().add(switchEditModeAction);
	}
	
	/**
	 * Creates element of the coolbar containing controls related to visualizations
	 */
	protected void createVisualizationCI() {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(VisualizationManager.getComboItem());
		toolBarManager.add(visualizationDialogAction);
		toolBarManager.add(colorSetManagerAction);
		visualizationCI = new ToolBarContributionItem(toolBarManager, "ColorSetActions");
	}
	

	/**
	 * Shows or hides the visualizationCI
	 * @param show	true/false for either show or hide
	 */
	public void showVisualizationCI(boolean show) {
		if(show) {
			getCoolBarManager().insertAfter(viewActionsCI.getId(), visualizationCI);
		} else {
			getCoolBarManager().remove(visualizationCI);
		}
		getCoolBarManager().update(true);
	}
	
	protected StatusLineManager createStatusLineManager() {
		return super.createStatusLineManager();
	}

	/**
	 *Builds and ads a menu to the Engine frame
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
		dataMenu.add(selectMetaboliteDbAction);
		dataMenu.add(selectGexAction);
		dataMenu.add(createGexAction);
		dataMenu.add(colorSetManagerAction);
		dataMenu.add(visualizationDialogAction);
		if(SwtEngine.getCurrent().USE_R) {
			MenuManager statsMenu = new MenuManager("&Pathway statistics");
			dataMenu.add(statsMenu);
			statsMenu.add(rStatsAction);
			statsMenu.add(rLoadStatsAction);
		}
//		dataMenu.add(new CommonActions.BiopaxAction(this));
		
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

	ToolBarContributionItem visualizationCI;

	/**
	 * overrides craeteCoolBarManager in MainWindowBase.
	 * this one also creates a visualizationCI. 
	 */
	protected CoolBarManager createCoolBarManager(int style)
	{
		createCommonActionsCI();
		createEditActionsCI();
		createAlignActionsCI();
		createViewActionsCI();
		createVisualizationCI();
		
		CoolBarManager coolBarManager = new CoolBarManager(style);
		coolBarManager.setLockLayout(true);
		
		coolBarManager.add(commonActionsCI);
		coolBarManager.add(viewActionsCI);
		
		coolBarManager.add(visualizationCI);
		
		return coolBarManager;
	}
	
	protected void addPanelTabs() {
		Composite visPanel = VisualizationManager.createSidePanel(rightPanel.getTabFolder());
		
		rightPanel.addTab(bpBrowser, "Backpage");
		rightPanel.addTab(propertyTable, "Properties");
		rightPanel.addTab(pwSearchComposite, "Pathway Search");
		rightPanel.addTab(legend, "Legend");
		rightPanel.addTab(visPanel, "Visualization");
	}
	
	public void vPathwayEvent(VPathwayEvent e) {
		super.vPathwayEvent(e);
		if(e.getType() == VPathwayEvent.ELEMENT_DRAWN) {
			Visualization v = VisualizationManager.getCurrent();
			VPathwayElement elm = e.getAffectedElement();
			if(v != null && elm instanceof Graphics) {
				v.visualizeDrawing((Graphics)elm, e.getGraphics2D());
			}
		}
	}
	
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
		GexManager.addListener(this);
	}

	public boolean editOnOpen() {
		return false; //Don't force edit mode on open pathway
	}

} // end of class
