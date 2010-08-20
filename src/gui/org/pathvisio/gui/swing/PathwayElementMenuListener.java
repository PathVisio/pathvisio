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

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import org.pathvisio.gui.swing.CommonActions.AddLiteratureAction;
import org.pathvisio.gui.swing.CommonActions.EditLiteratureAction;
import org.pathvisio.gui.swing.CommonActions.PropertiesAction;
import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
import org.pathvisio.model.AnchorType;
import org.pathvisio.model.ConnectorType;
import org.pathvisio.model.GroupStyle;
import org.pathvisio.view.Citation;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.Group;
import org.pathvisio.view.Handle;
import org.pathvisio.view.InfoBox;
import org.pathvisio.view.Line;
import org.pathvisio.view.MouseEvent;
import org.pathvisio.view.State;
import org.pathvisio.view.VAnchor;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.ViewActions;
import org.pathvisio.view.ViewActions.PositionPasteAction;
import org.pathvisio.view.swing.VPathwaySwing;

/**
 * Implementation of {@link VPathwayListener} that handles righ-click events to
 * show a popup menu when a {@link VPathwayElement} is clicked.
 *
 * This class is responsible for maintaining a list of {@link PathwayElementMenuHook}'s,
 * There should be a single Listener per MainPanel, possibly listening to multiple {@link VPathway}'s.
 */
public class PathwayElementMenuListener implements VPathwayListener {

	private List<PathwayElementMenuHook> hooks = new ArrayList<PathwayElementMenuHook>();
	

	public void addPathwayElementMenuHook(PathwayElementMenuHook hook)
	{
		hooks.add (hook);
	}

	public void removePathwayElementMenuHook(PathwayElementMenuHook hook)
	{
		hooks.remove(hook);
	}

	/**
	 * This should be implemented by plug-ins
	 * that wish to hook into the Pathway Element Menu
	 */
	public interface PathwayElementMenuHook
	{
		public void pathwayElementMenuHook (VPathwayElement e, JPopupMenu menu);
	}

	/**
	 * Get an instance of a {@link JPopupMenu} for a given {@link VPathwayElement}
	 * @param e The {@link VPathwayElement} to create the popup menu for. If e is an instance of
	 * {@link Handle}, the menu is based on the parent element.
	 * @return The {@link JPopupMenu} for the given pathway element
	 */
	private JPopupMenu getMenuInstance(SwingEngine swingEngine, VPathwayElement e) {
		if(e instanceof Citation) return null;

		if(e instanceof Handle) e = ((Handle)e).getParent();

		VPathway vp = e.getDrawing();
		VPathwaySwing component = (VPathwaySwing)vp.getWrapper();
		ViewActions vActions = vp.getViewActions();

		JPopupMenu menu = new JPopupMenu();

		//Don't show delete if the element cannot be deleted
		if(!(e instanceof InfoBox)) {
			menu.add(vActions.delete1);
		}

		JMenu selectMenu = new JMenu("Select");
		selectMenu.add(vActions.selectAll);
		selectMenu.add(vActions.selectDataNodes);
		menu.add(selectMenu);
		menu.addSeparator();
		
		// new feature to copy and paste with the right-click menu
		menu.add(vActions.copy);
		
		PositionPasteAction a = vActions.positionPaste;
		Point loc = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(loc, component);
		a.setPosition(loc);
		
		menu.add(a);
		menu.addSeparator();
		

		//Only show group/ungroup when multiple objects or a group are selected
		if((e instanceof Group)) {
			GroupStyle s = ((Group)e).getPathwayElement().getGroupStyle();
			if(s == GroupStyle.GROUP) {
				menu.add(vActions.toggleGroup);
			} else {
				menu.add(vActions.toggleComplex);
			}
			menu.addSeparator();
		} else if(vp.getSelectedGraphics().size() > 1) {
			menu.add(vActions.toggleGroup);
			menu.add(vActions.toggleComplex);
		}

		if (e instanceof GeneProduct)
		{
			menu.add(vActions.addState);
		}
		if (e instanceof State)
		{
			menu.add(vActions.removeState);
		}
		
		if((e instanceof Line)) {
			final Line line = (Line)e;

			menu.add(vActions.addAnchor);
			
			if (line.getPathwayElement().getConnectorType() == ConnectorType.SEGMENTED) {
				menu.add(vActions.addWaypoint);
				menu.add(vActions.removeWaypoint);
			}
			
			JMenu typeMenu = new JMenu("Line type");

			ButtonGroup buttons = new ButtonGroup();

			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					line.getPathwayElement().setConnectorType(
							ConnectorType.fromName(e.getActionCommand())
					);
				}
			};
			for(ConnectorType t : ConnectorType.getValues()) {
				JRadioButtonMenuItem mi = new JRadioButtonMenuItem(t.getName());
				mi.setActionCommand(t.getName());
				mi.setSelected(t.equals(line.getPathwayElement().getConnectorType()));
				mi.addActionListener(listener);
				typeMenu.add(mi);
				buttons.add(mi);
			}
			menu.add(typeMenu);
		}

		if((e instanceof VAnchor)) {
			final VAnchor anchor = ((VAnchor)e);

			JMenu anchorMenu = new JMenu("Anchor type");
			ButtonGroup buttons = new ButtonGroup();

			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					anchor.getMAnchor().setShape(
							AnchorType.fromName(e.getActionCommand()));
				}
			};

			for(AnchorType at : AnchorType.getValues()) {
				JRadioButtonMenuItem mi = new JRadioButtonMenuItem(at.getName());
				mi.setActionCommand(at.getName());
				mi.setSelected(at.equals(anchor.getMAnchor().getShape()));
				mi.addActionListener(listener);
				anchorMenu.add(mi);
				buttons.add(mi);
			}

			menu.add(anchorMenu);
		}

		JMenu orderMenu = new JMenu("Order");
		orderMenu.add(vActions.orderBringToFront);
		orderMenu.add(vActions.orderSendToBack);
		orderMenu.add(vActions.orderUp);
		orderMenu.add(vActions.orderDown);
		menu.add(orderMenu);

		if(e instanceof Graphics) {
			JMenu litMenu = new JMenu("Literature");
			litMenu.add(new AddLiteratureAction(swingEngine, component, e));
			litMenu.add(new EditLiteratureAction(swingEngine, component, e));
			menu.add(litMenu);

			menu.addSeparator();
			menu.add(new PropertiesAction(swingEngine, component,e));
		}
		
		

		// give plug-ins a chance to add menu items.
		for (PathwayElementMenuHook hook : hooks)
		{
			hook.pathwayElementMenuHook (e, menu);
		}

		return menu;
	}

	private SwingEngine swingEngine;

	PathwayElementMenuListener(SwingEngine swingEngine)
	{
		this.swingEngine = swingEngine;
	}

	public void vPathwayEvent(VPathwayEvent e) {
		switch(e.getType()) {
		case VPathwayEvent.ELEMENT_CLICKED_DOWN:
			if(e.getAffectedElement() instanceof Citation) {
				Citation c = (Citation)e.getAffectedElement();
				PathwayElementDialog d = PathwayElementDialog.getInstance(swingEngine,
						c.getParent().getPathwayElement(), false, null, null
				);
				d.selectPathwayElementPanel(PathwayElementDialog.TAB_LITERATURE);
				d.setVisible(true);
				break;
			}
		case VPathwayEvent.ELEMENT_CLICKED_UP:
			assert(e.getVPathway() != null);
			assert(e.getVPathway().getWrapper() instanceof VPathwaySwing);
			
			if(e.getMouseEvent().isPopupTrigger()) {
				Component invoker = (VPathwaySwing)e.getVPathway().getWrapper();
				MouseEvent me = e.getMouseEvent();
				JPopupMenu m = getMenuInstance(swingEngine, e.getAffectedElement());
				if(m != null) {
					m.show(invoker, me.getX(), me.getY());
				}
			}
			break;
		}
	}
}
