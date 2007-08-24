package org.pathvisio.view;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

public class ViewActions implements VPathwayListener, SelectionListener {
	VPathway vPathway;

	public static final String GROUP_ENABLE_EDITMODE = "editmode";
	public static final String GROUP_ENABLE_VPATHWAY_LOADED = "vpathway";
	public static final String GROUP_ENABLE_WHEN_SELECTION = "selection";
	
	public final SelectClassAction selectDataNodes;
	public final SelectAllAction selectAll;
	public final GroupAction toggleGroup;
	public final DeleteAction delete;
	
	Engine engine;
	
	public ViewActions(VPathway vp) {
		vPathway = vp;
		
		engine = Engine.getCurrent();
//		engine.addApplicationEventListener(this);
		vp.addSelectionListener(this);
		vp.addVPathwayListener(this);
		
		selectDataNodes = new SelectClassAction("DataNode", GeneProduct.class);
		selectAll = new SelectAllAction();
		toggleGroup = new GroupAction();
		delete = new DeleteAction();
		
		registerToGroup(selectDataNodes, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(selectAll, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(toggleGroup, GROUP_ENABLE_EDITMODE);
		registerToGroup(toggleGroup, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(delete, GROUP_ENABLE_EDITMODE);
		registerToGroup(delete, GROUP_ENABLE_WHEN_SELECTION);
		
		setGroupEnabled(true, GROUP_ENABLE_VPATHWAY_LOADED);
		setGroupEnabled(vp.getSelectedGraphics().size() > 0, GROUP_ENABLE_WHEN_SELECTION);
		setGroupEnabled(vp.isEditMode(), GROUP_ENABLE_EDITMODE);
	}
	
	HashMap<String, List<Action>> actionGroups = new HashMap<String, List<Action>>();
	
	public void registerToGroup(Action a, String group) {
		List<Action> actions = actionGroups.get(group);
		if(actions == null) {
			actionGroups.put(group, actions = new ArrayList<Action>());
		}
		if(!actions.contains(a)) actions.add(a);
	}
	
	public void registerToGroup(Action[] actions, String group) {
		for(Action a : actions) registerToGroup(a, group);
	}
	
	public void registerToGroup(Action[][] actions, String group) {
		for(Action[] aa : actions) {
			for(Action a : aa) registerToGroup(a, group);
		}
	}
	
	public void setGroupEnabled(boolean enabled, String group) {
		List<Action> actions = actionGroups.get(group);
		if(actions != null) {
			
			for(Action a : actions) {
				System.out.println("Setting action " + a + " to " + enabled);
				a.setEnabled(enabled);
			}
		}
	}
	
//	public void applicationEvent(ApplicationEvent e) {
//		if(e.type == ApplicationEvent.VPATHWAY_CREATED) {
//			VPathway vp = (VPathway)e.getSource();
//			vp.addSelectionListener(this);
//			vp.addVPathwayListener(this);
//			setGroupEnabled(true, GROUP_ENABLE_VPATHWAY_LOADED);
//			setGroupEnabled(vp.getSelectedGraphics().size() > 0, GROUP_ENABLE_WHEN_SELECTION);
//			setGroupEnabled(vp.isEditMode(), GROUP_ENABLE_EDITMODE);
//		}
//	}

	public void vPathwayEvent(VPathwayEvent e) {
		VPathway vp = (VPathway)e.getSource();
		if			(e.getType() == VPathwayEvent.EDIT_MODE_OFF) {
			setGroupEnabled(false, GROUP_ENABLE_EDITMODE);
		} else if 	(e.getType() == VPathwayEvent.EDIT_MODE_ON) {
			setGroupEnabled(true, GROUP_ENABLE_EDITMODE);
			setGroupEnabled(vp.getSelectedGraphics().size() > 0, GROUP_ENABLE_WHEN_SELECTION);
		}
	}

	public void selectionEvent(SelectionEvent e) {
		VPathway vp = ((SelectionBox)e.getSource()).getDrawing();
		boolean enabled = vp.getSelectedGraphics().size() > 0;
		setGroupEnabled(enabled, GROUP_ENABLE_WHEN_SELECTION);
		setGroupEnabled(vp.isEditMode(), GROUP_ENABLE_EDITMODE);
	}
	
//	private abstract class EnableOnSelectAction extends AbstractAction implements SelectionListener {
//		public EnableOnSelectAction() {
//			vPathway.addSelectionListener(this);
//		}
//		
//		public void selectionEvent(SelectionEvent e) {
//			setEnabled(vPathway.getSelectedGraphics().size() > 0);
//		}
//	}
	
	private class SelectClassAction extends AbstractAction {
		Class c;
		public SelectClassAction(String name, Class c) {
			super("Select all " + name + " objects");
			this.c = c;
		}
		public void actionPerformed(ActionEvent e) {
			vPathway.selectObjects(c);
		}
	}
	
	private class SelectAllAction extends AbstractAction {
		public SelectAllAction() {
			super("Select all");
		}
		public void actionPerformed(ActionEvent e) {
			vPathway.selectAll();
		}
	}
		
	private class GroupAction extends AbstractAction implements SelectionListener {
		public GroupAction() {
			super();
			putValue(NAME, "Group");
			setLabel();
		}

		public void actionPerformed(ActionEvent e) {
			if(!isEnabled()) return; //Don't perform action if not enabled
			vPathway.toggleGroup(vPathway.getSelectedGraphics());
		}

		public void selectionEvent(SelectionEvent e) {
			switch(e.type) {
			case SelectionEvent.OBJECT_ADDED:
			case SelectionEvent.OBJECT_REMOVED:
			case SelectionEvent.SELECTION_CLEARED:
				setLabel();
			}
		}
		
		private void setLabel() {
			int groups = 0;
			int unGrouped = 0;
			for(Graphics g : vPathway.getSelectedGraphics()) {
				if(g instanceof Group) groups++;
				if(g.getGmmlData().getGroupRef() == null) {
					unGrouped++;
				}
			}
			setEnabled(true);
			if(unGrouped >= 2) {
				putValue(Action.NAME, "Group");
			} else if(groups == 1) {
				putValue(Action.NAME, "Ungroup");
			} else {
				putValue(Action.NAME, "Group");
				setEnabled(false);
			}
		}		
	}
	
	private class DeleteAction extends AbstractAction {
		public DeleteAction() {
			super();
			putValue(NAME, "Delete");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_DELETE, 0));
		}

		public void actionPerformed(ActionEvent e) {
			if(!isEnabled()) return; //Don't perform action if not enabled
			
			ArrayList<VPathwayElement> toRemove = new ArrayList<VPathwayElement>();
			for(VPathwayElement o : vPathway.getDrawingObjects())
			{
				if (!o.isSelected() || o == vPathway.selection || o == vPathway.infoBox)
					continue; // Object not selected, skip
				toRemove.add(o);
			}
			vPathway.removeDrawingObjects(toRemove, true);
		}
	}
}
