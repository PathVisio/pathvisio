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
package org.pathvisio.gui;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Globals;
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
import org.pathvisio.gui.Engine.ApplicationEvent;
import org.pathvisio.gui.Engine.ApplicationEventListener;
import org.pathvisio.preferences.Preferences;
import org.pathvisio.search.PathwaySearchComposite;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathway;
import org.pathvisio.visualization.LegendPanel;
import org.pathvisio.visualization.VisualizationDialog;
import org.pathvisio.visualization.VisualizationManager;

/**
 * This class is the main class in the GPML project. 
 * It acts as a container for pathwaydrawings and facilitates
 * loading, creating and saving drawings to and from GPML.
 */
public class MainWindow extends ApplicationWindow implements 
						ApplicationEventListener, ExpressionDataListener
{
	private static final long serialVersionUID = 1L;
	static int ZOOM_TO_FIT = -1;
		
	private CommonActions.UndoAction undoAction = new CommonActions.UndoAction(this);	
	private CommonActions.NewAction newAction = new CommonActions.NewAction (this);
	private CommonActions.OpenAction openAction = new CommonActions.OpenAction (this);	
	private CommonActions.ImportAction importAction = new CommonActions.ImportAction (this);	
	private CommonActions.SaveAction saveAction = new CommonActions.SaveAction(this);	
	private CommonActions.SaveAsAction saveAsAction = new CommonActions.SaveAsAction (this);
	private CommonActions.ExportAction exportAction = new CommonActions.ExportAction (this);
	private CommonActions.ExitAction exitAction = new CommonActions.ExitAction(this);
	private CommonActions.PreferencesAction preferencesAction = new CommonActions.PreferencesAction(this);
	private CommonActions.AboutAction aboutAction = new CommonActions.AboutAction(this);
	private CommonActions.CopyAction copyAction = new CommonActions.CopyAction(this);
	private CommonActions.HelpAction helpAction = new CommonActions.HelpAction(this);	
	private CommonActions.PasteAction pasteAction = new CommonActions.PasteAction(this);
	private CommonActions.SwitchEditModeAction switchEditModeAction = new CommonActions.SwitchEditModeAction(this);
	public ShowRightPanelAction showRightPanelAction = new ShowRightPanelAction(this);
	private SelectGdbAction selectGdbAction = new SelectGdbAction(this);
	private AlignActions.AlignCenterXAction alignCenterXAction = new AlignActions.AlignCenterXAction(this);
	private AlignActions.AlignCenterYAction alignCenterYAction = new AlignActions.AlignCenterYAction(this);
	private AlignActions.AlignLeftAction alignLeftAction = new AlignActions.AlignLeftAction(this);
	private AlignActions.AlignRightAction alignRightAction = new AlignActions.AlignRightAction(this);
	private AlignActions.AlignTopAction alignTopAction = new AlignActions.AlignTopAction(this);
	private AlignActions.AlignBottomAction alignBottomAction = new AlignActions.AlignBottomAction(this);
	private AlignActions.SetCommonWidthAction setCommonWidthAction = new AlignActions.SetCommonWidthAction(this);
	private AlignActions.SetCommonHeightAction setCommonHeightAction = new AlignActions.SetCommonHeightAction(this);

	/**
	 * {@link Action} to select a Gene Database
	 */
	private class SelectGdbAction extends Action
	{
		MainWindow window;
		public SelectGdbAction(MainWindow w)
		{
			window = w;
			setText("Select &Gene Database");
			setToolTipText("Select Gene Database");
		}
		
		public void run () {			
			try {
				DBConnector dbcon = Gdb.getDBConnector();
				String dbName = dbcon.openChooseDbDialog(getShell());
				
				if(dbName == null) return;
				
				Gdb.connect(dbName);
				setStatus("Using Gene Database: '" + Engine.getPreferences().getString(Preferences.PREF_CURR_GDB) + "'");
				cacheExpressionData();
			} catch(Exception e) {
				String msg = "Failed to open Gene Database; " + e.getMessage();
				MessageDialog.openError (window.getShell(), "Error", 
						"Error: " + msg + "\n\n" + 
						"See the error log for details.");
				Engine.log.error(msg, e);
			}
		}
	}

	/**
	 * {@link Action} to show or hide the right sidepanel
	 */
	public class ShowRightPanelAction extends Action
	{
		MainWindow window;
		public ShowRightPanelAction (MainWindow w)
		{
			super("Show &information panel", IAction.AS_CHECK_BOX);
			window = w;
			setChecked(true);
		}
		
		public void run() {
			if(isChecked()) rightPanel.show();
			else rightPanel.hide();
		}
	}

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
	 * Loads expression data for all {@link GeneProduct}s in the loaded pathway
	 */
	private void cacheExpressionData()
	{
		if(Engine.isDrawingOpen())
		{
			VPathway drawing = Engine.getVPathway();
			//Check for neccesary connections
			if(Gex.isConnected() && Gdb.isConnected())
			{
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, Gex.createCacheRunnable(drawing.getMappIds(), drawing.getSystemCodes()));
					drawing.redraw();
				} catch(Exception e) {
					String msg = "while caching expression data: " + e.getMessage();					
					MessageDialog.openError (getShell(), "Error", 
							"Error: " + msg + "\n\n" + 
							"See the error log for details.");
					Engine.log.error(msg, e);
				}
			}
		}
	}

	/**
	 * Deselects all {@link NewElementAction}s on the toolbar and sets 
	 * {@link VPathway}.newGraphics to {@link VPathway}.NEWNONE
	 */
	public void deselectNewItemActions()
	{
		IContributionItem[] items = editActionsCI.getToolBarManager().getItems();
		for(int i = 0; i < items.length; i++)
		{
			if(items[i] instanceof ActionContributionItem)
			{
				((ActionContributionItem)items[i]).getAction().setChecked(false);
			}
		}
		Engine.getVPathway().setNewGraphics(VPathway.NEWNONE);
	}


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

	public void deselectAlignItemActions()
	{
		IContributionItem[] items = alignActionsCI.getToolBarManager().getItems();
		for(int i = 0; i < items.length; i++)
		{
			if(items[i] instanceof ActionContributionItem)
			{
				((ActionContributionItem)items[i]).getAction().setChecked(false);
			}
		}
		Engine.getVPathway().setNewGraphics(VPathway.NEWNONE);
	}
	
	// Elements of the coolbar
	ToolBarContributionItem commonActionsCI;
	ToolBarContributionItem editActionsCI;
	ToolBarContributionItem alignActionsCI;
	ToolBarContributionItem visualizationCI;
	ToolBarContributionItem viewActionsCI;
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
	 * Creates element of the coolbar containing common actions as new, save etc.
	 */
	protected void createCommonActionsCI()
	{
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(newAction);
		toolBarManager.add(openAction);
		toolBarManager.add(saveAction);
		commonActionsCI = new ToolBarContributionItem(toolBarManager, "CommonActions");
	}

	/**
	 * Creates element of the coolbar only shown in edit mode (new element actions)
	 */
	protected void createEditActionsCI()
	{
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);		
		toolBarManager.add(new NewElementAction(VPathway.NEWGENEPRODUCT));
		toolBarManager.add(new NewElementAction(VPathway.NEWLABEL));
		toolBarManager.add(new NewElementAction(VPathway.NEWLINEMENU));
		toolBarManager.add(new NewElementAction(VPathway.NEWRECTANGLE));
		toolBarManager.add(new NewElementAction(VPathway.NEWOVAL));
		toolBarManager.add(new NewElementAction(VPathway.NEWARC));
		toolBarManager.add(new NewElementAction(VPathway.NEWBRACE));
		toolBarManager.add(new NewElementAction(VPathway.NEWTBAR));
		toolBarManager.add(new NewElementAction(VPathway.NEWLINESHAPEMENU));

		editActionsCI = new ToolBarContributionItem(toolBarManager, "EditModeActions");
	}
	
	/**
	 * Creates element of the coolbar containing controls related to viewing a pathway
	 */
	protected void createViewActionsCI()
	{
		final MainWindow window = this;
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		//Add zoomCombo
		toolBarManager.add(new ControlContribution("ZoomCombo") {
			protected Control createControl(Composite parent) {
				final Combo zoomCombo = new Combo(parent, SWT.DROP_DOWN);
				zoomCombo.setItems(new String[] { "200%", "100%", "75%", "50%", "Zoom to fit" });
				zoomCombo.setText("100%");
				zoomCombo.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						int pctZoom = 100;
						String zoomText = zoomCombo.getText().replace("%", "");
						try {
							pctZoom = Integer.parseInt(zoomText);
						} catch (Exception ex) { 
							if(zoomText.equals("Zoom to fit"))
									{ pctZoom = ZOOM_TO_FIT; } else { return; }
						}
						new CommonActions.ZoomAction(window, pctZoom).run();
					}
					public void widgetDefaultSelected(SelectionEvent e) { widgetSelected(e); }
				});
				return zoomCombo;
			}
		});
		//Add swich to editmode
		toolBarManager.add(switchEditModeAction);
		
		viewActionsCI =  new ToolBarContributionItem(toolBarManager, "SwitchActions");
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
	 * Shows or hides the editActionsCI
	 * @param show	true/false for either show or hide
	 */
	public void showEditActionsCI(boolean show)
	{
		if(show) {
			getCoolBarManager().insertAfter(viewActionsCI.getId(), editActionsCI);
		}
		else {
			getCoolBarManager().remove(editActionsCI);
		}
//		showVisualizationCI(!show); //Visualizations can show up in edit mode...
		getCoolBarManager().update(true);
	}

	/**
	   Shows or hides the alignActionsCI.
	   @param show	true/false for either show or hide
	*/
	public void showAlignActionsCI(boolean show)
	{
		if(show) {
			getCoolBarManager().insertAfter(editActionsCI.getId(), alignActionsCI);
		}
		else {
			getCoolBarManager().remove(alignActionsCI);
		}
//		showVisualizationCI(!show); //Visualizations can show up in edit mode...
		getCoolBarManager().update(true);
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
	
	public MainWindow()
	{
		this(null);
	}
	
	private MenuManager menuManager = null;
	/**
	 * can be accessed by plugins etc. 
	 * to add menu items and even complete menus.
	 * 
	 * (plugin API)
	 */
	public MenuManager getMenuManager()
	{
		return menuManager;
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
	
	public boolean close() {
		Engine.fireApplicationEvent(
				new ApplicationEvent(this, ApplicationEvent.CLOSE_APPLICATION));
		return super.close();
	}
	
	public ScrolledComposite sc;
	public BackpagePanel bpBrowser; //Browser for showing backpage information
	public PropertyPanel propertyTable;	//Table showing properties of Graphics objects
	SashForm sashForm; //SashForm containing the drawing area and sidebar
	TabbedSidePanel rightPanel; //side panel containing backbage browser and property editor
	PathwaySearchComposite pwSearchComposite; //Composite that handles pathway searches and displays results
	LegendPanel legend; //Legend to display colorset information

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

	public TabbedSidePanel getSidePanel() { return rightPanel; }
	
	public LegendPanel getLegend() { return legend; }
	
	public void showLegend(boolean show) {	
		if(show && Gex.isConnected()) {
			if(rightPanel.isVisible("Legend")) return; //Legend already visible, only refresh
			rightPanel.unhideTab("Legend", 0);
			rightPanel.selectTab("Legend");
		}
		
		else rightPanel.hideTab("Legend");
	}
			
	/**
	 * Creates a new empty drawing canvas
	 * @return the empty {@link VPathway}
	 */
	public VPathway createNewDrawing()
	{		
		return new VPathway(sc, SWT.NO_BACKGROUND);
	}
	
	public void applicationEvent(ApplicationEvent e) {
		VPathway drawing = null;
		switch(e.type) {
		case ApplicationEvent.NEW_PATHWAY:
			drawing = Engine.getVPathway();
			sc.setContent(drawing);
			break;
		case ApplicationEvent.OPEN_PATHWAY:
			drawing = Engine.getVPathway();
			sc.setContent(drawing);
			if(Gex.isConnected()) cacheExpressionData();
			break;	
		}
	}

	public void expressionDataEvent(ExpressionDataEvent e) {
		switch(e.type) {
		case ExpressionDataEvent.CONNECTION_CLOSED:
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					showLegend(false);
				}
			});
			break;
		case ExpressionDataEvent.CONNECTION_OPENED:
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					cacheExpressionData();
					showLegend(true);
				}
			});
			break;
		}
	}

	//	KH 20070514 begin
	protected void createAlignActionsCI()
	{
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(alignCenterXAction);
		toolBarManager.add(alignCenterYAction);
		toolBarManager.add(alignLeftAction);
		toolBarManager.add(alignRightAction);
		toolBarManager.add(alignTopAction);
		toolBarManager.add(alignBottomAction);
		toolBarManager.add(setCommonWidthAction);
		toolBarManager.add(setCommonHeightAction);
	
		alignActionsCI = new ToolBarContributionItem(toolBarManager, "AlignActions");
	}
	
//	KH end
	/**
	   Invoked when user tries to close window
	*/
	protected boolean canHandleShellCloseEvent()
	{
		return Engine.canDiscardPathway();
	}
	
} // end of class
