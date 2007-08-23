package org.pathvisio.gui.swing;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.actions.CommonActions.AddLiteratureAction;
import org.pathvisio.gui.swing.actions.CommonActions.EditLiteratureAction;
import org.pathvisio.gui.swing.actions.CommonActions.PropertiesAction;
import org.pathvisio.view.Group;
import org.pathvisio.view.Handle;
import org.pathvisio.view.InfoBox;
import org.pathvisio.view.MouseEvent;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.ViewActions;
import org.pathvisio.view.swing.VPathwaySwing;

public class PathwayElementMenuListener implements VPathwayListener {    
	private static JPopupMenu getMenuInstance(VPathwayElement e) {
		if(e instanceof Handle) e = ((Handle)e).getParent();
		
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
		
		JMenu litMenu = new JMenu("Literature");
		litMenu.add(new AddLiteratureAction(component, e));
		litMenu.add(new EditLiteratureAction(component, e));
		menu.add(litMenu);
		menu.addSeparator();
		menu.add(new PropertiesAction(component,e));

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
