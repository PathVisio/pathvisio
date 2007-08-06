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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.gui.swing.actions.CommonActions.AlignAction;
import org.pathvisio.gui.swing.actions.CommonActions.CopyAction;
import org.pathvisio.gui.swing.actions.CommonActions.ImportAction;
import org.pathvisio.gui.swing.actions.CommonActions.NewElementAction;
import org.pathvisio.gui.swing.actions.CommonActions.PasteAction;
import org.pathvisio.gui.swing.actions.CommonActions.SaveAction;
import org.pathvisio.gui.swing.actions.CommonActions.SaveAsAction;
import org.pathvisio.gui.swing.actions.CommonActions.StackAction;
import org.pathvisio.gui.swing.actions.CommonActions.ZoomAction;
import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
import org.pathvisio.gui.swing.propertypanel.PathwayTableModel;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.AlignType;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.StackType;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;

import com.mammothsoftware.frwk.ddb.DropDownButton;

public class MainPanel extends JPanel implements VPathwayListener, ApplicationEventListener {
	private JSplitPane splitPane;

	private JMenuBar menuBar;

	private JToolBar toolBar;

	private JScrollPane pathwayScrollPane;

	private JScrollPane propertiesScrollPane;
	
	private JTabbedPane sidebarTabbedPane;

	private JTable propertyTable;

	private BackpagePane backpagePane;
	
	public MainPanel() {
		setLayout(new BorderLayout());

		Engine.getCurrent().addApplicationEventListener(this);
		
		menuBar = new JMenuBar();
		addMenuActions(menuBar);
		toolBar = new JToolBar();
		addToolBarActions(toolBar);

		add(toolBar, BorderLayout.PAGE_START);
		// menuBar will be added by container (JFrame or JApplet)

		pathwayScrollPane = new JScrollPane();

		final PathwayTableModel model = new PathwayTableModel();
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
		
		propertiesScrollPane = new JScrollPane(propertyTable);
		
		backpagePane = new BackpagePane();
		
		sidebarTabbedPane = new JTabbedPane();
		sidebarTabbedPane.addTab( "Properties", propertiesScrollPane );
		sidebarTabbedPane.addTab( "Backpage", new JScrollPane(backpagePane) );
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				pathwayScrollPane, sidebarTabbedPane);
		splitPane.setResizeWeight(1);
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);
	}
	
	protected void addMenuActions(JMenuBar mb) {
		JMenu pathwayMenu = new JMenu("Pathway");
		pathwayMenu.add(new SaveAction());
		pathwayMenu.add(new SaveAsAction());
		pathwayMenu.add(new ImportAction(this));
		//pathwayMenu.add(new ExportAction()); //TODO: implement

		JMenu editMenu = new JMenu("Edit");
		editMenu.add(new CopyAction());
		editMenu.add(new PasteAction());

		JMenu selectionMenu = new JMenu("Selection");
		JMenu alignMenu = new JMenu("Align");
		JMenu stackMenu = new JMenu("Stack");
		
		alignMenu.add(new AlignAction(AlignType.CENTERX));
		alignMenu.add(new AlignAction(AlignType.CENTERY));
		alignMenu.add(new AlignAction(AlignType.LEFT));
		alignMenu.add(new AlignAction(AlignType.RIGHT));
		alignMenu.add(new AlignAction(AlignType.TOP));
		alignMenu.add(new AlignAction(AlignType.WIDTH));
		alignMenu.add(new AlignAction(AlignType.HEIGHT));
		stackMenu.add(new StackAction(StackType.CENTERX));
		stackMenu.add(new StackAction(StackType.CENTERY));
		stackMenu.add(new StackAction(StackType.LEFT));
		stackMenu.add(new StackAction(StackType.RIGHT));
		stackMenu.add(new StackAction(StackType.TOP));
		stackMenu.add(new StackAction(StackType.BOTTOM));
		
		selectionMenu.add(alignMenu);
		selectionMenu.add(stackMenu);
		
		JMenu viewMenu = new JMenu("View");
		JMenu zoomMenu = new JMenu("Zoom");
		viewMenu.add(zoomMenu);
		zoomMenu.add(new ZoomAction(VPathway.ZOOM_TO_FIT));
		zoomMenu.add(new ZoomAction(10));
		zoomMenu.add(new ZoomAction(25));
		zoomMenu.add(new ZoomAction(50));
		zoomMenu.add(new ZoomAction(75));
		zoomMenu.add(new ZoomAction(100));
		zoomMenu.add(new ZoomAction(150));
		zoomMenu.add(new ZoomAction(200));

		mb.add(pathwayMenu);
		mb.add(editMenu);
		mb.add(selectionMenu);
		mb.add(viewMenu);
	}

	protected void addToolBarActions(JToolBar tb) {
		tb.setLayout(new WrapLayout(1, 1));
		
		addToToolbar(new SaveAction());
		addToToolbar(new SaveAsAction());
		addToToolbar(new ImportAction(this));
		//addToToolbar(new ExportAction()); //TODO: implement
		tb.addSeparator();
		addToToolbar(new CopyAction(), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new PasteAction(), TB_GROUP_HIDE_ON_EDIT);
		tb.addSeparator();

		tb.addSeparator();

		addToToolbar(new JLabel("Zoom:", JLabel.LEFT));
		JComboBox combo = new JComboBox(new Object[] {
				new ZoomAction(VPathway.ZOOM_TO_FIT), new ZoomAction(10),
				new ZoomAction(25), new ZoomAction(50), new ZoomAction(75),
				new ZoomAction(100), new ZoomAction(150), new ZoomAction(200) });
		combo.setMaximumSize(combo.getPreferredSize());
		combo.setEditable(true);
		combo.setSelectedIndex(5); // 100%
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				Object s = combo.getSelectedItem();
				if (s instanceof ZoomAction) {
					((ZoomAction) s).actionPerformed(e);
				} else if (s instanceof String) {
					String zs = (String) s;
					try {
						double zf = Double.parseDouble(zs);
						new ZoomAction(zf).actionPerformed(e);
					} catch (Exception ex) {
						// Ignore bad input
					}
				}
			}
		});
		addToToolbar(combo);

		tb.addSeparator();

		addToToolbar(new NewElementAction(VPathway.NEWGENEPRODUCT), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new NewElementAction(VPathway.NEWLABEL), TB_GROUP_HIDE_ON_EDIT);
		// New line menu
		DropDownButton lineButton = new DropDownButton(new ImageIcon(Engine.getCurrent()
				.getResourceURL("icons/newlinemenu.gif")));
		lineButton.addComponent(new JMenuItem(new NewElementAction(
				VPathway.NEWLINE)));
		lineButton.addComponent(new JMenuItem(new NewElementAction(
				VPathway.NEWLINEARROW)));
		lineButton.addComponent(new JMenuItem(new NewElementAction(
				VPathway.NEWLINEDASHED)));
		lineButton.addComponent(new JMenuItem(new NewElementAction(
				VPathway.NEWLINEDASHEDARROW)));
		lineButton.setRunFirstItem(true);
		addToToolbar(lineButton, TB_GROUP_HIDE_ON_EDIT);

		addToToolbar(new NewElementAction(VPathway.NEWRECTANGLE), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new NewElementAction(VPathway.NEWOVAL), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new NewElementAction(VPathway.NEWARC), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new NewElementAction(VPathway.NEWBRACE), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new NewElementAction(VPathway.NEWTBAR), TB_GROUP_HIDE_ON_EDIT);

		// New lineshape menu
		DropDownButton lineShapeButton = new DropDownButton(new ImageIcon(
				Engine.getCurrent().getResourceURL("icons/newlineshapemenu.gif")));
		lineShapeButton.addComponent(new JMenuItem(new NewElementAction(
				VPathway.NEWLIGANDROUND)));
		lineShapeButton.addComponent(new JMenuItem(new NewElementAction(
				VPathway.NEWRECEPTORROUND)));
		lineShapeButton.addComponent(new JMenuItem(new NewElementAction(
				VPathway.NEWLIGANDSQUARE)));
		lineShapeButton.addComponent(new JMenuItem(new NewElementAction(
				VPathway.NEWRECEPTORSQUARE)));
		lineShapeButton.setRunFirstItem(true);
		addToToolbar(lineShapeButton, TB_GROUP_HIDE_ON_EDIT);
		
		tb.addSeparator();
		
		addToToolbar(new AlignAction(AlignType.CENTERX), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new AlignAction(AlignType.CENTERY), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new AlignAction(AlignType.LEFT), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new AlignAction(AlignType.RIGHT), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new AlignAction(AlignType.TOP), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new AlignAction(AlignType.WIDTH), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new AlignAction(AlignType.HEIGHT), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new StackAction(StackType.CENTERX), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new StackAction(StackType.CENTERY), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new StackAction(StackType.LEFT), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new StackAction(StackType.RIGHT), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new StackAction(StackType.TOP), TB_GROUP_HIDE_ON_EDIT);
		addToToolbar(new StackAction(StackType.BOTTOM), TB_GROUP_HIDE_ON_EDIT);
	}

	public static final String TB_GROUP_HIDE_ON_EDIT = "edit";
	
	HashMap<String, List<Component>> toolbarGroups = new HashMap<String, List<Component>>();
	
	public void addToToolbar(Component c, String group) {
		JToolBar tb = getToolBar();
		tb.add(c);
		addToToolbarGroup(c, group);
	}
		
	public void addToToolbar(Component c) {
		addToToolbar(c, null);
	}
	
	public JButton addToToolbar(Action a, String group) {
		JButton b = getToolBar().add(a);
		addToToolbarGroup(b, group);
		return b;
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
	
	public List<Component> getToolbarGroup(String group) {
		List<Component> tbg = toolbarGroups.get(group);
		if(tbg == null) tbg = new ArrayList<Component>();
		return tbg;
	}
	
	public JMenuBar getMenuBar() {
		return menuBar;
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
		switch(e.getType()) {
		case VPathwayEvent.ELEMENT_DOUBLE_CLICKED:
			if(e.getAffectedElement() instanceof Graphics) {
				PathwayElement p = ((Graphics)e.getAffectedElement()).getGmmlData();
				if(p != null) {
					PathwayElementDialog.getInstance(p, 
							JOptionPane.getFrameForComponent(this), this).setVisible(true);
				}
			}
			break;
		case VPathwayEvent.EDIT_MODE_ON:
			for(Component b : getToolbarGroup(TB_GROUP_HIDE_ON_EDIT)) {
				b.setEnabled(true);
			}
			break;
		case VPathwayEvent.EDIT_MODE_OFF:
			for(Component b : getToolbarGroup(TB_GROUP_HIDE_ON_EDIT)) {
				b.setEnabled(false);
			}
			break;
		}
	}
	
	public void applicationEvent(ApplicationEvent e) {
		switch(e.type) {
		case ApplicationEvent.VPATHWAY_CREATED:
			VPathway vp = (VPathway)e.getSource();
			vp.addVPathwayListener(this);
			vp.addVPathwayListener(new PathwayElementMenuListener());
			break;
		}
	}
}
