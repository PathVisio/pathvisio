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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEvent;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.Gex;
import org.pathvisio.data.Gex.ExpressionDataEvent;
import org.pathvisio.data.Gex.ExpressionDataListener;
import org.pathvisio.gui.swt.awt.VPathwaySwingComposite;
import org.pathvisio.preferences.swt.SwtPreferences.SwtPreference;
import org.pathvisio.search.PathwaySearchComposite;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.visualization.LegendPanel;

/**
 * MainWindowBase is an abstract and incomplete Main Window that contains some
 * core functionality. This way we can create different flavours of the main window
 * without having too much duplicate code. Descendants should at least provide
 * a constructor, and override createCoolBarManager and createMenuManager.
 */
public abstract class MainWindowBase extends ApplicationWindow implements 
	ApplicationEventListener, ExpressionDataListener, VPathwayListener
{
	private static final long serialVersionUID = 1L;
	static int ZOOM_TO_FIT = -1;
		
	protected CommonActions.UndoAction undoAction = new CommonActions.UndoAction(this);	
	protected CommonActions.NewAction newAction = new CommonActions.NewAction (this);
	protected CommonActions.OpenAction openAction = new CommonActions.OpenAction (this);	
	protected CommonActions.ImportAction importAction = new CommonActions.ImportAction (this);	
	protected CommonActions.SaveAction saveAction = new CommonActions.SaveAction(this);	
	protected CommonActions.SaveAsAction saveAsAction = new CommonActions.SaveAsAction (this);
	protected CommonActions.ExportAction exportAction = new CommonActions.ExportAction (this);
	protected CommonActions.ExitAction exitAction = new CommonActions.ExitAction(this);
	protected CommonActions.PreferencesAction preferencesAction = new CommonActions.PreferencesAction(this);
	protected CommonActions.AboutAction aboutAction = new CommonActions.AboutAction(this);
	protected CommonActions.CopyAction copyAction = new CommonActions.CopyAction(this);
	protected CommonActions.HelpAction helpAction = new CommonActions.HelpAction(this);	
	protected CommonActions.PasteAction pasteAction = new CommonActions.PasteAction(this);
	protected CommonActions.SwitchEditModeAction switchEditModeAction = new CommonActions.SwitchEditModeAction(this);
	public ShowRightPanelAction showRightPanelAction = new ShowRightPanelAction(this);
	protected SelectGdbAction selectGdbAction = new SelectGdbAction(this);
	protected AlignActions.AlignCenterXAction alignCenterXAction = new AlignActions.AlignCenterXAction(this);
	protected AlignActions.AlignCenterYAction alignCenterYAction = new AlignActions.AlignCenterYAction(this);
	protected AlignActions.AlignLeftAction alignLeftAction = new AlignActions.AlignLeftAction(this);
	protected AlignActions.AlignRightAction alignRightAction = new AlignActions.AlignRightAction(this);
	protected AlignActions.AlignTopAction alignTopAction = new AlignActions.AlignTopAction(this);
	protected AlignActions.AlignBottomAction alignBottomAction = new AlignActions.AlignBottomAction(this);
	protected AlignActions.SetCommonWidthAction setCommonWidthAction = new AlignActions.SetCommonWidthAction(this);
	protected AlignActions.SetCommonHeightAction setCommonHeightAction = new AlignActions.SetCommonHeightAction(this);

	/**
	 * {@link Action} to select a Gene Database
	 */
	private class SelectGdbAction extends Action
	{
		MainWindowBase window;
		public SelectGdbAction(MainWindowBase w)
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
				setStatus("Using Gene Database: '" + SwtPreference.SWT_CURR_GDB + "'");
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
	 * deselect all buttons in the alignActionsCI coolbar
	 */
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
		Engine.getActiveVPathway().setNewGraphics(VPathway.NEWNONE);
	}
	
	/**
	 * {@link Action} to show or hide the right sidepanel
	 */
	public class ShowRightPanelAction extends Action
	{
		MainWindowBase window;
		public ShowRightPanelAction (MainWindowBase w)
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
	 * Loads expression data for all {@link GeneProduct}s in the loaded pathway
	 */
	private void cacheExpressionData()
	{
		if(Engine.isDrawingOpen())
		{
			VPathway drawing = Engine.getActiveVPathway();
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
		Engine.getActiveVPathway().setNewGraphics(VPathway.NEWNONE);
	}

	// Elements of the coolbar
	ToolBarContributionItem commonActionsCI;
	ToolBarContributionItem editActionsCI;
	ToolBarContributionItem alignActionsCI;
	ToolBarContributionItem viewActionsCI;
	
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
		final MainWindowBase window = this;
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
	
	//	KH 20070514 begin
	/**
	 * set up the alignActions coolbar
	 */
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
	   Invoked when user tries to close window.
	   We'll ask the user if he wants to save the pathway
	*/
	protected boolean canHandleShellCloseEvent()
	{
		return SwtEngine.canDiscardPathway();
	}
	
	protected MenuManager menuManager = null;
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
		
	public boolean close() {
		Engine.fireApplicationEvent(
				new ApplicationEvent(this, ApplicationEvent.APPLICATION_CLOSE));
		return super.close();
	}
	
	public ScrolledComposite sc;
	public BackpagePanel bpBrowser; //Browser for showing backpage information
	public PropertyPanel propertyTable;	//Table showing properties of Graphics objects
	SashForm sashForm; //SashForm containing the drawing area and sidebar
	TabbedSidePanel rightPanel; //side panel containing backbage browser and property editor
	PathwaySearchComposite pwSearchComposite; //Composite that handles pathway searches and displays results
	LegendPanel legend; //Legend to display colorset information
	VPathwaySwingComposite swingPathwayComposite;
	
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
				
	public void applicationEvent(ApplicationEvent e) {
		switch(e.type) {
		case ApplicationEvent.PATHWAY_OPENED:
			if(Gex.isConnected()) cacheExpressionData();
			break;
		}
		switch(e.type) {
		case ApplicationEvent.PATHWAY_NEW:
		case ApplicationEvent.PATHWAY_OPENED:
			Engine.getActiveVPathway().addVPathwayListener(this);
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
	
	public void vPathwayEvent(VPathwayEvent e) {
		switch(e.getType()) {
		case VPathwayEvent.EDIT_MODE_OFF:
			showLegend(true);
			break;
		case VPathwayEvent.EDIT_MODE_ON:
			showLegend(false);
			break;
		case VPathwayEvent.NEW_ELEMENT_ADDED:
			deselectNewItemActions();
			break;
		}
	}
	
	public MainWindowBase(Shell shell)
	{
		super(shell);
	}

}
