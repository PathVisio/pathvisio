package org.pathvisio.gui.swing;

import java.awt.Component;

import org.pathvisio.gui.swing.menus.PathwayElementMenu;
import org.pathvisio.view.Handle;
import org.pathvisio.view.MouseEvent;
import org.pathvisio.view.SelectionBox;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.swing.VPathwaySwing;

public class PathwayElementMenuListener implements VPathwayListener {    
	public static PathwayElementMenu getMenuInstance(VPathwayElement e) {
		if(e instanceof Handle) e = ((Handle)e).getParent();
		return new PathwayElementMenu(e);
	}
	
	public void vPathwayEvent(VPathwayEvent e) {
		switch(e.getType()) {
		case VPathwayEvent.ELEMENT_CLICKED_UP:
		case VPathwayEvent.ELEMENT_CLICKED_DOWN:
			assert(e.getVPathway() != null);
			assert(e.getVPathway().getWrapper() instanceof VPathwaySwing);
			VPathwayElement element = e.getAffectedElement();
			if(element instanceof Handle) element = ((Handle)element).getParent();
			
			if(element instanceof SelectionBox) {
				return;
			}
			
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
