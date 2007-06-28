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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.R.RController;
import org.pathvisio.R.RDataIn;
import org.pathvisio.R.RCommands.RException;
import org.pathvisio.R.wizard.RWizard;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.Gex;
import org.pathvisio.data.GexImportWizard;
import org.pathvisio.data.Gex.ExpressionDataEvent;
import org.pathvisio.data.Gex.ExpressionDataListener;
import org.pathvisio.gui.swt.BackpagePanel;
import org.pathvisio.gui.swt.Engine;
import org.pathvisio.gui.swt.GuiMain;
import org.pathvisio.gui.swt.PropertyPanel;
import org.pathvisio.gui.swt.TabbedSidePanel;
import org.pathvisio.preferences.Preferences;
import org.pathvisio.search.PathwaySearchComposite;
import org.pathvisio.visualization.LegendPanel;
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
		
		public void run () {
			try {
				DBConnector dbcon = Gex.getDBConnector();
				String dbName = dbcon.openChooseDbDialog(getShell());
				
				if(dbName == null) return;
				
				Gex.connect(dbName);
			} catch(Exception e) {
				String msg = "Failed to open Expression Dataset" + e.getMessage();
				MessageDialog.openError (window.getShell(), "Error", 
						"Error: " + msg + "\n\n" + 
						"See the error log for details.");
				Engine.log.error(msg, e);
			}		
		}
	}
	private SelectGexAction selectGexAction = new SelectGexAction(this);
	

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
			if(!Gdb.isConnected())
			{
				MessageDialog.openWarning(getShell(), "Warning", "No gene database selected, " +
						"select gene database before creating a new expression dataset");
				return;
			}
			WizardDialog dialog = new WizardDialog(getShell(), new GexImportWizard());
			dialog.setBlockOnOpen(true);
			dialog.open();
		}
	}
	private CreateGexAction createGexAction = new CreateGexAction(this);
	
	/**
	 *{@link Action} to start conversion of a GenMAPP gex to an expression database in
	 * pgex format
	 */
	private class ConvertGexAction extends Action
	{
		MainWindow window;
		public ConvertGexAction(MainWindow w)
		{
			window = w;
			setText("&Gex to PathVisio");
			setToolTipText("Convert from GenMAPP 2 Gex to PathVisio Expression Data");
		}
		
		public void run () {
			File gexFile = null;
			File gmGexFile = null;
			
			// Initialize filedialog to open GenMAPP gex
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Expression Dataset to convert");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Expression Datasets");
			fileDialog.setFilterExtensions(new String[] {"*.gex","*.*"});
			fileDialog.setFilterNames(new String[] {"Expression Dataset (*.gex)","All files (*.*)"});
			String file = fileDialog.open();
			// Only proceed if user selected a file
			if(file == null) return;
			gmGexFile = new File(file);
			
			String dbName = null;
			try {
				DBConnector dbcon = Gex.getDBConnector();
				dbName = dbcon.openNewDbDialog(getShell(), 
						gmGexFile.getName().replace(".gex", ".properties"));
			} catch(Exception e) {
				String msg = "Failed to get database connector" + e.getMessage();
				MessageDialog.openError (window.getShell(), "Error", 
						"Error: " + msg + "\n\n" + 
						"See the error log for details.");
				Engine.log.error(msg, e);
			}
			
			// Only proceed if user selected a file
			if(dbName != null) {
				Gex.setDbName(dbName);
				Gex.setGmGexFile(gmGexFile);
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, Gex.convertRunnable);
				} catch(Exception e) {
					String msg = "While converting GenMAPP GEX: " + e.getMessage();
					MessageDialog.openError (window.getShell(), "Error", 
							"Error: " + msg + "\n\n" + 
							"See the error log for details.");
					Engine.log.error(msg, e);
				}
				
			}
		}
	}
	private ConvertGexAction convertGexAction = new ConvertGexAction(this);
	
	/**
	 * {@link Action} to start conversion of a GenMAPP Gene database to a gene database 
	 * in hsqldb format
	 */
	private class ConvertGdbAction extends Action
	{
		MainWindow window;
		public ConvertGdbAction(MainWindow w)
		{
			window = w;
			setText("&Gdb to PathVisio");
			setToolTipText("Convert from GenMAPP 2 Gene database to PathVisio Gene database");
		}
		
		public void run () {
			String dbName = null;
			File gmGdbFile = null;
			// Initialize filedialog to open GenMAPP gdb
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Gene database to convert");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Gene Databases");
			fileDialog.setFilterExtensions(new String[] {"*.gdb","*.*"});
			fileDialog.setFilterNames(new String[] {"Gene database (*.gdb)","All files (*.*)"});
			String file = fileDialog.open();
			// Only proceed if user selected a file
			if(file == null) return;
			gmGdbFile = new File(file);

			try {
				DBConnector dbcon = Gex.getDBConnector();
				dbName = dbcon.openNewDbDialog(getShell(), 
						gmGdbFile.getName().replace(".gdb", ".properties"));
			} catch(Exception e) {
				MessageDialog.openError(getShell(), 
						"Error", "Unable to create database connector, " +
						"see error log for details");
				Engine.log.error("Unable to create database connector", e);	
			}
			
			// Only proceed if user selected a database name
			if(dbName != null) {
				Gdb.setConvertGdbName(dbName);
				Gdb.setConvertGmGdbFile(gmGdbFile);
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, Gdb.getConvertRunnable());
				} catch(Exception e) {
					String msg = "While converting GenMAPP gene database: "+ e.getMessage();
					MessageDialog.openError (window.getShell(), "Error", 
							"Error: " + msg + "\n\n" + 
					"See the error log for details.");
					Engine.log.error(msg, e);
				}
			}
		}
	}
	private ConvertGdbAction convertGdbAction = new ConvertGdbAction(this);
	
	/**
	 * {@link Action} to open the {@link ColorSetWindow}
	 */
	private class ColorSetManagerAction extends Action implements ExpressionDataListener
	{
		MainWindow window;
		public ColorSetManagerAction (MainWindow w)
		{
			window = w;
			setText("&Color Set manager");
			setToolTipText("Create and edit color sets");
			setImageDescriptor(ImageDescriptor.createFromURL(
					Engine.getResourceURL("icons/colorset.gif")));
			Gex.addListener(this);
			setEnabled(false);
		}
		public void run () {
			VisualizationDialog d = new VisualizationDialog(getShell());
			d.setTabItemOnOpen(VisualizationDialog.TABITEM_COLORSETS);
			d.open();
		}
		public void expressionDataEvent(ExpressionDataEvent e) {
			switch(e.type) {
			case ExpressionDataEvent.CONNECTION_OPENED:
				setEnabled(true); break;
			case ExpressionDataEvent.CONNECTION_CLOSED:
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
					Engine.getResourceURL("icons/visualizations.gif")));
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
			fd.setFilterPath(Engine.getPreferences().getString(Preferences.PREF_DIR_RDATA));
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
		editMenu.add(undoAction); // only in v2 while testing!
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
		dataMenu.add(selectGexAction);
		dataMenu.add(createGexAction);
		dataMenu.add(colorSetManagerAction);
		dataMenu.add(visualizationDialogAction);
		if(Engine.USE_R) {
			MenuManager statsMenu = new MenuManager("&Pathway statistics");
			dataMenu.add(statsMenu);
			statsMenu.add(rStatsAction);
			statsMenu.add(rLoadStatsAction);
		}
		dataMenu.add(new CommonActions.BiopaxAction(this));
		MenuManager convertMenu = new MenuManager("&Convert from GenMAPP 2");
		convertMenu.add(convertGexAction);
		convertMenu.add(convertGdbAction);
		dataMenu.add(convertMenu);
		
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
	
	/**
	 * Create and initialize widgets on the main window.
	 */
	protected Control createContents(Composite parent)
	{		
		Shell shell = parent.getShell();
		shell.setSize(800, 600);
		shell.setLocation(100, 100);
		
		GuiMain.loadImages(shell.getDisplay());
		
		shell.setImage(Engine.getImageRegistry().get("shell.icon"));
		
		Composite viewComposite = new Composite(parent, SWT.NULL);
		viewComposite.setLayout(new FillLayout());
		
		sashForm = new SashForm(viewComposite, SWT.HORIZONTAL);
		
		sc = new ScrolledComposite (sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setFocus();
		
		rightPanel = new TabbedSidePanel(sashForm, SWT.NULL);
		
		//rightPanel controls
		bpBrowser = new BackpagePanel(rightPanel.getTabFolder(), SWT.NONE);
		propertyTable = new PropertyPanel(
				rightPanel.getTabFolder(), SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		pwSearchComposite = new PathwaySearchComposite(rightPanel.getTabFolder(), SWT.NONE, this);
		legend = new LegendPanel(rightPanel.getTabFolder(), SWT.V_SCROLL | SWT.H_SCROLL);
		Composite visPanel = VisualizationManager.createSidePanel(rightPanel.getTabFolder());
		
		rightPanel.addTab(bpBrowser, "Backpage");
		rightPanel.addTab(propertyTable, "Properties");
		rightPanel.addTab(pwSearchComposite, "Pathway Search");
		rightPanel.addTab(legend, "Legend");
		rightPanel.addTab(visPanel, "Visualization");
		
		int sidePanelSize = Engine.getPreferences().getInt(Preferences.PREF_SIDEPANEL_SIZE);
		sashForm.setWeights(new int[] {100 - sidePanelSize, sidePanelSize});
		showRightPanelAction.setChecked(sidePanelSize > 0);
		
		rightPanel.getTabFolder().setSelection(0); //select backpage browser tab
		rightPanel.hideTab("Legend"); //hide legend on startup
		
		setStatus("Using Gene Database: '" + Engine.getPreferences().getString(Preferences.PREF_CURR_GDB) + "'");

		Engine.updateTitle();
		
		return parent;		
	};
	
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
		
		Engine.addApplicationEventListener(this);
		Gex.addListener(this);
	}

} // end of class
