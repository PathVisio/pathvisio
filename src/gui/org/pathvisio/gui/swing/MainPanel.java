// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.BackpageTextProvider;
import org.pathvisio.gui.BackpageTextProvider.BackpageAttributes;
import org.pathvisio.gui.BackpageTextProvider.BackpageXrefs;
import org.pathvisio.gui.swing.CommonActions.ZoomAction;
import org.pathvisio.gui.swing.dnd.PathwayImportHandler;
import org.pathvisio.gui.swing.propertypanel.PathwayTableModel;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.Handle;
import org.pathvisio.view.Label;
import org.pathvisio.view.SelectionBox;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;


/**
 * this is the contents of the main window in the WikiPathways applet,
 * and contains the editor window, side panels, toolbar and menu.
 *
 * For the standalone application, the derived class MainPanelStandalone is used.
 */
public class MainPanel extends JPanel implements VPathwayListener, ApplicationEventListener {

	private JSplitPane splitPane;

	protected JToolBar toolBar;

	private JScrollPane pathwayScrollPane;

	private JScrollPane propertiesScrollPane;

	protected JTabbedPane sidebarTabbedPane;

	protected JMenuBar menuBar;
	
	protected GraphicsChoiceButton itemsDropDown;
	
	private ObjectsPane objectsPane;

	private JTable propertyTable;

	protected BackpagePane backpagePane;
	protected BackpageTextProvider bpt;

	protected CommonActions actions;

	private final PathwayTableModel model;

	Set<Action> hideActions;

	protected SwingEngine swingEngine;

	private final PathwayElementMenuListener pathwayElementMenuListener;

	public PathwayTableModel getModel(){
		return model;
	}

	public PathwayElementMenuListener getPathwayElementMenuListener()
	{
		return pathwayElementMenuListener;
	}

	private boolean mayAddAction(Action a) {
		return hideActions == null || !hideActions.contains(a);
	}

	protected void addMenuActions(JMenuBar mb) {
		JMenu fileMenu = new JMenu("File");

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

		JMenu selectionMenu = new JMenu("Selection");

		for(Action a : actions.layoutActions) addToMenu(a, selectionMenu);
		editMenu.add (selectionMenu);

		JMenu viewMenu = new JMenu("View");
		JMenu zoomMenu = new JMenu("Zoom");
		viewMenu.add(zoomMenu);
		for(Action a : actions.zoomActions) addToMenu(a, zoomMenu);

		JMenu helpMenu = new JMenu("Help");

		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(viewMenu);
		mb.add(helpMenu);
	}

	/**
	 * Constructor for this class. Creates the main panel of this application, containing
	 * the main GUI elements (menubar, toolbar, sidepanel, drawing pane). Actions that should
	 * not be added to the menubar and toolbar should be specified in the hideActions parameter
	 * @param hideActions The {@link Action}s that should not be added to the toolbar and menubar
	 */
	public MainPanel(SwingEngine swingEngine, Set<Action> hideActions)
	{
		this.hideActions = hideActions;
		this.swingEngine = swingEngine;
		pathwayElementMenuListener = new PathwayElementMenuListener(swingEngine);
		model = new PathwayTableModel(swingEngine);
	}

	public void createAndShowGUI()
	{
		setLayout(new BorderLayout());
		setTransferHandler(new PathwayImportHandler());
		swingEngine.getEngine().addApplicationEventListener(this);

		actions = swingEngine.getActions();

		toolBar = new JToolBar();
		toolBar.setFloatable(false); // disable floatable toolbar, aka Abomination of interaction design.
		addToolBarActions(swingEngine, toolBar);

		add(toolBar, BorderLayout.PAGE_START);
		// menuBar will be added by container (JFrame or JApplet)

		pathwayScrollPane = new JScrollPane();
		// set background color when no VPathway is loaded, override l&f because it is usually white.
		pathwayScrollPane.getViewport().setBackground(Color.LIGHT_GRAY);
		
		objectsPane = new ObjectsPane(swingEngine);
		int numItemsPerRow = 10;
		objectsPane.addButtons(actions.newDatanodeActions, "Data Nodes", numItemsPerRow);
		objectsPane.addButtons(actions.newShapeActions, "Basic Shapes", numItemsPerRow);
		objectsPane.addButtons(actions.newMIMShapeActions, "MIM shapes", numItemsPerRow);
		objectsPane.addButtons(actions.newInteractionActions, "Basic interactions", numItemsPerRow);
		//objectsPane.addButtons(actions.newRLInteractionActions, "Receptor/ligand", numItemsPerRow);
		objectsPane.addButtons(actions.newMIMInteractionActions, "MIM interactions", numItemsPerRow);
		objectsPane.addButtons(actions.newCellularComponentActions, "Cellular compartments", numItemsPerRow);
		objectsPane.addButtons(actions.newAnnotationActions, "Annotations", numItemsPerRow);
		objectsPane.addButtons(actions.newTemplateActions, "Templates", numItemsPerRow);
				
		propertyTable = new JTable(model) {

			public TableCellRenderer getCellRenderer(int row, int column) {
				TableCellRenderer r = model.getCellRenderer(row, column);
				return r == null ? super.getCellRenderer(row, column) : r;
			}

			public TableCellEditor getCellEditor(int row, int column) {
				TableCellEditor e = model.getCellEditor(row, column);
				return e == null ? super.getCellEditor(row, column) : e;
			}
		};
		//TODO: make this prettier, it's not good for the tablemodel to have
		//a reference to the table. Quick fix for preventing TableCellEditor
		//to remain open upon selecting a new PathwayElement
		model.setTable(propertyTable);

		propertiesScrollPane = new JScrollPane(propertyTable);

		bpt = new BackpageTextProvider ();
		bpt.addBackpageHook(new BackpageAttributes(swingEngine.getGdbManager().getCurrentGdb()));
		bpt.addBackpageHook(new BackpageXrefs(swingEngine.getGdbManager().getCurrentGdb()));

		backpagePane = new BackpagePane(bpt, swingEngine.getEngine());
		backpagePane.addHyperlinkListener(swingEngine);

		sidebarTabbedPane = new JTabbedPane();
		sidebarTabbedPane.addTab("Objects", objectsPane);
		sidebarTabbedPane.addTab( "Properties", propertiesScrollPane );
		sidebarTabbedPane.addTab( "Backpage", new JScrollPane(backpagePane) );

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				pathwayScrollPane, sidebarTabbedPane);
		splitPane.setResizeWeight(1);
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);
		Action[] keyStrokeActions = new Action[] {
				actions.copyAction,
				actions.pasteAction,
		};
		InputMap im = getInputMap();
		ActionMap am = getActionMap();
		for(Action a : keyStrokeActions) {
			im.put((KeyStroke)a.getValue(Action.ACCELERATOR_KEY), a.getValue(Action.NAME));
			am.put(a.getValue(Action.NAME), a);
		}

		menuBar = new JMenuBar();
		addMenuActions(menuBar);
	}

	/**
	 * Constructor for this class. Creates the main panel of this application, containing
	 * the main GUI elements (menubar, toolbar, sidepanel, drawing pane).
	 */
	public MainPanel(SwingEngine swingEngine)
	{
		this(swingEngine, null);
	}

	/**
	 * {@link ActionListener} for the Zoom combobox on the toolbar. The user can select one
	 * of the predefined ZoomActions (50%, 100%, 200%, Zoom to fit, etc.),
	 * or enter a number or percentage manually.
	 */
	protected class ZoomComboListener implements ActionListener {

		public void actionPerformed(ActionEvent e){
			JComboBox combo = (JComboBox) e.getSource();
			Object s = combo.getSelectedItem();
			
			if (s instanceof Action) {
				((Action) s).actionPerformed(e);
				
				// after the selection of "fit to window" the new calculated zoom 
				// percentage is displayed 
				if(s instanceof CommonActions.ZoomToFitAction) {
					double pct = swingEngine.getEngine().getActiveVPathway().getPctZoom();
					double rpct = Math.round(pct);
					combo.setSelectedItem(rpct + "%");
				}
			} else if (s instanceof String) {
				String zs = (String) s;
				zs=zs.replace("%","");
				try {
					double zf = Double.parseDouble(zs);
						if(zf > 0){ // Ignore negative number
							ZoomAction za = new ZoomAction(swingEngine.getEngine(), zf);
							za.setEnabled(true);
							za.actionPerformed(e);
						}
				} catch (Exception ex) {
					// Ignore bad input
				}
			}
		}

	}


	protected void addToolBarActions(final SwingEngine swingEngine, JToolBar tb)
	{
		tb.setLayout(new WrapLayout(1, 1));

		addToToolbar(actions.importAction);
		addToToolbar(actions.exportAction);
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
		combo.addActionListener(new ZoomComboListener());

		addToToolbar(combo, TB_GROUP_SHOW_IF_VPATHWAY);

		tb.addSeparator();

		// define the drop-down menu for data nodes 
		String tooltip = "Select a data node to draw";
		GraphicsChoiceButton datanodeButton = new GraphicsChoiceButton();
		datanodeButton.setToolTipText(tooltip);
		
		int numItemsPerRow = 6;
		datanodeButton.addLabel("Data Nodes");
		datanodeButton.addButtons(actions.newDatanodeActions, numItemsPerRow);		

		datanodeButton.addLabel("Annotations");
		datanodeButton.addButtons(actions.newAnnotationActions, numItemsPerRow);
		
		addToToolbar(datanodeButton, TB_GROUP_SHOW_IF_EDITMODE);
		tb.addSeparator(new Dimension(2,0));
		
		// define the drop-down menu for shapes 
		tooltip = "Select a shape to draw";
		GraphicsChoiceButton shapeButton = new GraphicsChoiceButton();
		shapeButton.setToolTipText(tooltip);		
		itemsDropDown = shapeButton;
		
		numItemsPerRow = 6;
		shapeButton.addLabel("Basic shapes");
		shapeButton.addButtons(actions.newShapeActions, numItemsPerRow);		

		shapeButton.addLabel("MIM shapes");
		shapeButton.addButtons(actions.newMIMShapeActions, numItemsPerRow);

		shapeButton.addLabel("Cellular components");
		shapeButton.addButtons(actions.newCellularComponentActions, numItemsPerRow);
		
		addToToolbar(shapeButton, TB_GROUP_SHOW_IF_EDITMODE);
		tb.addSeparator(new Dimension(2,0));
		
		// define the drop-down menu for interactions
		tooltip = "Select an interaction to draw";
		GraphicsChoiceButton lineButton = new GraphicsChoiceButton();
		lineButton.setToolTipText(tooltip);		
		
		numItemsPerRow = 6;		
		lineButton.addLabel("Basic interactions");
		lineButton.addButtons(actions.newInteractionActions, numItemsPerRow);
		
		lineButton.addLabel("MIM interactions");
		lineButton.addButtons(actions.newMIMInteractionActions, numItemsPerRow);		
		
		addToToolbar(lineButton, TB_GROUP_SHOW_IF_EDITMODE);
		tb.addSeparator(new Dimension(2,0));
		
		// define the drop-down menu for templates
		tooltip = "Select a template to draw";
		GraphicsChoiceButton templateButton = new GraphicsChoiceButton();
		templateButton.setToolTipText(tooltip);		
		
		numItemsPerRow = 6;		
		templateButton.addLabel("Templates");
		templateButton.addButtons(actions.newTemplateActions, numItemsPerRow);	
		
		addToToolbar(templateButton, TB_GROUP_SHOW_IF_EDITMODE);
		tb.addSeparator();
		
		addToToolbar(actions.layoutActions);
	}

	public static final String TB_GROUP_SHOW_IF_EDITMODE = "edit";
	public static final String TB_GROUP_SHOW_IF_VPATHWAY = "vpathway";

	Map<String, List<Component>> toolbarGroups = new HashMap<String, List<Component>>();

	public void addToToolbar(Component c, String group) {
		JToolBar tb = getToolBar();
		if(tb == null) {
			Logger.log.warn("Trying to register toolbar action while no toolbar is available " +
					"(running in headless mode?)");
			return;
		}
		tb.add(c);
		addToToolbarGroup(c, group);
	}

	public void addToToolbar(Component c) {
		addToToolbar(c, null);
	}

	public void addToToolbar(Action[] actions) {
		for(Action a : actions) {
			addToToolbar(a);
		}
	}

	public JButton addToToolbar(Action a, String group) {
		if(mayAddAction(a)) {
			JButton b = getToolBar().add(a);
			b.setFocusable(false);
			addToToolbarGroup(b, group);
			return b;
		}
		return null;
	}

	public JButton addToToolbar(Action a) {
		return addToToolbar(a, null);
	}

	private void addToToolbarGroup(Component c, String group) {
		if(group != null) {
			List<Component> gb = toolbarGroups.get(group);
			if(gb == null) {
				toolbarGroups.put(group, gb = new ArrayList<Component>());
			}
			gb.add(c);
		}
	}

	public void addToMenu(Action a, JMenu parent) {
		if(mayAddAction(a)) {
			parent.add(a);
		}
	}

	public List<Component> getToolbarGroup(String group) {
		List<Component> tbg = toolbarGroups.get(group);
		if(tbg == null) tbg = new ArrayList<Component>();
		return tbg;
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

	public JScrollPane getScrollPane() {
		return pathwayScrollPane;
	}

	public JSplitPane getSplitPane() {
		return splitPane;
	}

	public BackpagePane getBackpagePane() {
		return backpagePane;
	}

	public void vPathwayEvent(VPathwayEvent e) {
		VPathway vp = (VPathway)e.getSource();
		switch(e.getType()) {
		case VPathwayEvent.ELEMENT_DOUBLE_CLICKED:
			VPathwayElement pwe = e.getAffectedElement();
			if(pwe instanceof Handle)
			{
				pwe = ((Handle)pwe).getParent();
			}
			if(pwe instanceof Graphics &&
					!(pwe instanceof SelectionBox)) {
				PathwayElement p = ((Graphics)pwe).getPathwayElement();
				if(p != null) {
					swingEngine.getPopupDialogHandler().getInstance(p, !vp.isEditMode(), null, this).setVisible(true);
				}
			}
			break;
		case VPathwayEvent.EDIT_MODE_ON:
			for(Component b : getToolbarGroup(TB_GROUP_SHOW_IF_EDITMODE)) {
				b.setEnabled(true);
			}
			break;
		case VPathwayEvent.EDIT_MODE_OFF:
			for(Component b : getToolbarGroup(TB_GROUP_SHOW_IF_EDITMODE)) {
				b.setEnabled(false);
			}
			break;
		case VPathwayEvent.HREF_ACTIVATED:
			if(e.getAffectedElement() instanceof Label) {
				try {
					hyperlinkUpdate(new HyperlinkEvent(e.getSource(), HyperlinkEvent.EventType.ACTIVATED, new URL(((Label)e.getAffectedElement()).getPathwayElement().getHref())));
				} catch (MalformedURLException e1) {
					swingEngine.getEngine().getActiveVPathway().selectObject(e.getAffectedElement());
					swingEngine.handleMalformedURLException("The specified link address is not valid.", this, e1);
				}
			}
		}
	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) {
		swingEngine.hyperlinkUpdate(e);
	}

	public void applicationEvent(ApplicationEvent e) {
		switch(e.getType()) {
		case ApplicationEvent.VPATHWAY_CREATED:
			{
				VPathway vp = (VPathway)e.getSource();
				vp.addVPathwayListener(this);
				vp.addVPathwayListener(pathwayElementMenuListener);
				for(Component b : getToolbarGroup(TB_GROUP_SHOW_IF_VPATHWAY)) {
					b.setEnabled(true);
				}
			}
			break;
		case ApplicationEvent.VPATHWAY_DISPOSED:
			{
				VPathway vp = (VPathway)e.getSource();
				vp.removeVPathwayListener(this);
				vp.removeVPathwayListener(pathwayElementMenuListener);
			}
			break;
		}

	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public JTabbedPane getSideBarTabbedPane()
	{
		return sidebarTabbedPane;
	}

	public void dispose()
	{
		backpagePane.dispose();
	}

	/**
	 * hook of the objects tab
	 */
	public ObjectsPane getObjectsPane()
	{
		return objectsPane;
	}
	
	/**
	 * hook of the drop-down menu
	 */
	public GraphicsChoiceButton getItemsDropDown()
	{
		return itemsDropDown;
	}
	
	/**
	 * add items with text to the drop-down menu (e.g. -> arrow)
	 */
	public void addMenuItems(Action [] aa, DropDownButton lineButton)
	{
		for(Action a : aa) {
			lineButton.addComponent(new JMenuItem(a));
		}
	}
}
