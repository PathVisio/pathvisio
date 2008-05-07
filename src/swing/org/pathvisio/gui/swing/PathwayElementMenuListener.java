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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.pathvisio.gui.swing.CommonActions.AddLiteratureAction;
import org.pathvisio.gui.swing.CommonActions.EditLiteratureAction;
import org.pathvisio.gui.swing.CommonActions.PropertiesAction;
import org.pathvisio.model.AnchorType;
import org.pathvisio.model.ConnectorType;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.Group;
import org.pathvisio.view.Handle;
import org.pathvisio.view.InfoBox;
import org.pathvisio.view.Line;
import org.pathvisio.view.MouseEvent;
import org.pathvisio.view.VAnchor;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.VPoint;
import org.pathvisio.view.ViewActions;
import org.pathvisio.view.swing.VPathwaySwing;

/**
 * Implementation of {@link VPathwayListener} that handles righ-click events to 
 * show a popup menu when a {@link VPathwayElement} is clicked.
 * @author thomas
 *
 */
public class PathwayElementMenuListener implements VPathwayListener {
	/**
	 * Get an instance of a {@link JPopupMenu} for a given {@link VPathwayElement}
	 * @param e The {@link VPathwayElement} to create the popup menu for. If e is an instance of
	 * {@link Handle}, the menu is based on the parent element.
	 * @return The {@link JPopupMenu} for the given pathway element
	 */
	private static JPopupMenu getMenuInstance(VPathwayElement e) {
		if(e instanceof Handle) e = ((Handle)e).getParent();
		if(e instanceof VPoint) e = ((VPoint)e).getLine();
		
		VPathway vp = e.getDrawing();
		VPathwaySwing component = (VPathwaySwing)vp.getWrapper();
		ViewActions vActions = vp.getViewActions();
		
		JPopupMenu menu = new JPopupMenu();

		//Don't show delete if the element cannot be deleted
		if(!(e instanceof InfoBox)) {
			menu.add(vActions.delete);
		}
				
		JMenu selectMenu = new JMenu("Select");
		selectMenu.add(vActions.selectAll);
		selectMenu.add(vActions.selectDataNodes);
		menu.add(selectMenu);
		menu.addSeparator();
		
		//Only show group/ungroup when multiple objects or a group are selected
		if((e instanceof Group) || vp.getSelectedGraphics().size() > 1) {
			JMenu groupMenu = new JMenu("Group");
			groupMenu.add(vActions.toggleGroup);
			menu.add(groupMenu);
			menu.addSeparator();
		}
		
		if((e instanceof Line)) {
			final Line line = (Line)e;
			
			menu.add(vActions.addAnchor);
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
		
		JMenu litMenu = new JMenu("Literature");
		litMenu.add(new AddLiteratureAction(component, e));
		litMenu.add(new EditLiteratureAction(component, e));
		menu.add(litMenu);
		if(e instanceof Graphics) {
			menu.addSeparator();
			menu.add(new PropertiesAction(component,e));
		}
		return menu;
	}
	
	public void vPathwayEvent(VPathwayEvent e) {
		switch(e.getType()) {
		case VPathwayEvent.ELEMENT_CLICKED_UP:
		case VPathwayEvent.ELEMENT_CLICKED_DOWN:
			assert(e.getVPathway() != null);
			assert(e.getVPathway().getWrapper() instanceof VPathwaySwing);
			
			if(e.getMouseEvent().isPopupTrigger()) {
				Component invoker = (VPathwaySwing)e.getVPathway().getWrapper();
				MouseEvent me = e.getMouseEvent();
				JPopupMenu m = getMenuInstance(e.getAffectedElement());
				m.show(invoker, me.getX(), me.getY());
			}
			break;
		}
	}
}
