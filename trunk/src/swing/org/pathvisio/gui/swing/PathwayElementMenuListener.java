package org.pathvisio.gui.swing;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.pathvisio.gui.swing.actions.PropertiesAction;
import org.pathvisio.view.Handle;
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
		
		JPopupMenu menu = new JPopupMenu();
		ViewActions vActions = vp.getViewActions();
		menu.add(vActions.delete);
		
		JMenu selectMenu = new JMenu("Select");
		selectMenu.add(vActions.selectAll);
		selectMenu.add(vActions.selectDataNodes);
		menu.add(selectMenu);
		menu.addSeparator();
		menu.add(new PropertiesAction(e));

		
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
