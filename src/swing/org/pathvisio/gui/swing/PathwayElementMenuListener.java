package org.pathvisio.gui.swing;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.pathvisio.gui.swing.actions.PropertiesAction;
import org.pathvisio.gui.swing.menus.PathwayElementMenu;
import org.pathvisio.view.Group;
import org.pathvisio.view.Handle;
import org.pathvisio.view.MouseEvent;
import org.pathvisio.view.SelectionBox;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.ViewActions;
import org.pathvisio.view.swing.VPathwaySwing;

public class PathwayElementMenuListener implements VPathwayListener {    
	private static PathwayElementMenu getMenuInstance(VPathwayElement e) {
		if(e instanceof Handle) e = ((Handle)e).getParent();
		VPathway vp = e.getDrawing();
		List<Action> actions = new ArrayList<Action>();
		ViewActions vActions = vp.getViewActions();
		actions.add(new PropertiesAction(e));
		actions.add(vActions.delete);
		actions.add(vActions.selectAll);
		actions.add(vActions.selectDataNodes);
		
		if(e instanceof SelectionBox) {
		} else if (e instanceof Group) {
		}
		return new PathwayElementMenu(e, actions);
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
				PathwayElementMenu m = getMenuInstance(e.getAffectedElement());
				m.show(invoker, me.getX(), me.getY());
			}
			break;
		}
	}
}
