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
package org.pathvisio.view;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

/**
 * A collection of {@link Action}s related to the pathway view. An instance of this class contains
 * actions bound to one instance of a {@link VPathway} (non-static fields). The static inner classes are not bound to a particular {@link VPathway},
 * but act on the currently active pathway by calling {@link Engine#getActiveVPathway()}.
 * 
 * Instances of actions may be registered to one or more groups, which changes the action's property on
 * certain events (see GROUP* constants). The {@link Action} instances that are fields of this class are
 * already registered to the proper groups.
 * 
 * An instance of this class belonging to a {@link VPathway} can be obtained using {@link VPathway#getViewActions()}. 
 * 
 * @author thomas
 */
public class ViewActions implements VPathwayListener, SelectionListener {
	private static URL IMG_COPY= Engine.getCurrent().getResourceURL("icons/copy.gif");
	private static URL IMG_PASTE = Engine.getCurrent().getResourceURL("icons/paste.gif");
	
	/**
	 * The group of actions that will be enabled when the VPathway is in edit mode and 
	 * disabled when not
	 */
	public static final String GROUP_ENABLE_EDITMODE = "editmode";
	
	/**
	 * The group of actions that will be enabled when a VPathway is loaded and 
	 * disabled when not
	 */
	public static final String GROUP_ENABLE_VPATHWAY_LOADED = "vpathway";
	
	/**
	 * The group of actions that will be enabled when the selection isn't empty 
	 */
	public static final String GROUP_ENABLE_WHEN_SELECTION = "selection";

	static final int SMALL_INCREMENT = 2;
	static final int LARGE_INCREMENT = 20;
	
	VPathway vPathway;
	
	public final SelectClassAction selectDataNodes;
	public final SelectAllAction selectAll;
	public final GroupAction toggleGroup;
	public final DeleteAction delete;
	public final CopyAction copy;
	public final PasteAction paste;
	public final KeyMoveAction keyMove;
	public final UndoAction undo;

	Engine engine;
	
	ViewActions(VPathway vp) {
		vPathway = vp;
		
		engine = Engine.getCurrent();
//		engine.addApplicationEventListener(this);
		vp.addSelectionListener(this);
		vp.addVPathwayListener(this);
		
		selectDataNodes = new SelectClassAction("DataNode", GeneProduct.class);
		selectAll = new SelectAllAction();
		toggleGroup = new GroupAction();
		delete = new DeleteAction();
		copy = new CopyAction();
		paste = new PasteAction();
		keyMove = new KeyMoveAction(null);
		undo = new UndoAction();
		
		registerToGroup(selectDataNodes, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(selectAll, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(toggleGroup, GROUP_ENABLE_EDITMODE);
		registerToGroup(toggleGroup, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(delete, GROUP_ENABLE_EDITMODE);
		registerToGroup(delete, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(copy, 	ViewActions.GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(paste, 	ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(paste, 	ViewActions.GROUP_ENABLE_EDITMODE);
		registerToGroup(keyMove, ViewActions.GROUP_ENABLE_EDITMODE);
		
		setGroupEnabled(true, GROUP_ENABLE_VPATHWAY_LOADED);
		setGroupEnabled(vp.getSelectedGraphics().size() > 0, GROUP_ENABLE_WHEN_SELECTION);
		setGroupEnabled(vp.isEditMode(), GROUP_ENABLE_EDITMODE);
	}
	
	HashMap<String, List<Action>> actionGroups = new HashMap<String, List<Action>>();
	
	/**
	 * Register the given action to a group (one of the GROUP* contants)
	 * @param a	The action to register
	 * @param group The group to register the action to
	 */
	public void registerToGroup(Action a, String group) {
		List<Action> actions = actionGroups.get(group);
		if(actions == null) {
			actionGroups.put(group, actions = new ArrayList<Action>());
		}
		if(!actions.contains(a)) actions.add(a);
	}
	
	/**
	 * Register the given actions to a group (one of the GROUP* constants)
	 * @param actions The actions to register
	 * @param group The group to register the actions to
	 */
	public void registerToGroup(Action[] actions, String group) {
		for(Action a : actions) registerToGroup(a, group);
	}
	
	/**
	 * Register the given actions to a group (one of the GROUP* constants)
	 * @param actions The actions to register
	 * @param group The group to register the actions to
	 */
	public void registerToGroup(Action[][] actions, String group) {
		for(Action[] aa : actions) {
			for(Action a : aa) registerToGroup(a, group);
		}
	}
	
	/**
	 * Set the enabled state of a action group
	 * @param enabled Whether the actions in the group should be enabled or not
	 * @param group The group to enable/disable (one of the GROUP* constants)
	 */
	public void setGroupEnabled(boolean enabled, String group) {
		List<Action> actions = actionGroups.get(group);
		if(actions != null) {
			
			for(Action a : actions) {
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
		setGroupEnabled(vp.isEditMode(), GROUP_ENABLE_EDITMODE);
		setGroupEnabled(enabled, GROUP_ENABLE_WHEN_SELECTION);
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
	
	public static class CopyAction extends AbstractAction {			
		public CopyAction() {
			super();
			putValue(NAME, "Copy");
			putValue(SMALL_ICON, new ImageIcon(IMG_COPY));
			String descr = "Copy selected pathway objects to clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) vp.copyToClipboard();
		}		
	}
	
	public static class PasteAction extends AbstractAction {
		public PasteAction() {
			super();
			putValue(NAME, "Paste");
			putValue(SMALL_ICON, new ImageIcon(IMG_PASTE));
			String descr = "Paste pathway elements from clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(isEnabled() && vp != null) vp.pasteFromClipboard();
		}
	}
	
	public static class KeyMoveAction extends AbstractAction {
		KeyStroke key;

		public KeyMoveAction(KeyStroke key) {
		this.key = key; 
		}
		
		public void actionPerformed(ActionEvent e) {

		int moveIncrement = 0;
		
		if ((e.getModifiers() &
				ActionEvent.SHIFT_MASK) != 0)
		{ moveIncrement = LARGE_INCREMENT;}
		else {moveIncrement = SMALL_INCREMENT;}
		
		VPathway vp = Engine.getCurrent().getActiveVPathway();
		vp.moveByKey(key, moveIncrement);
		}
	}

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
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl A"));
		}
		public void actionPerformed(ActionEvent e) {
			vPathway.selectAll();
		}
	}
		
	private class GroupAction extends AbstractAction implements SelectionListener {
		public GroupAction() {
			super();
			putValue(NAME, "Group");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl G"));
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
				if(g.getPathwayElement().getGroupRef() == null) {
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
	
	public static class UndoAction extends AbstractAction implements UndoManagerListener, ApplicationEventListener {
		public UndoAction() {
			super();
			putValue(NAME, "Undo");
			putValue(SHORT_DESCRIPTION, "Undo last action");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					java.awt.event.KeyEvent.VK_Z, java.awt.event.KeyEvent.CTRL_DOWN_MASK));
			Engine.getCurrent().addApplicationEventListener(this);
			setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if (vp != null)
			{
				vp.undo();
			}		
		}

		public void undoManagerEvent(UndoManagerEvent e) {
			String msg = e.getMessage();
			putValue(SHORT_DESCRIPTION, "Undo: " + msg);
			setEnabled(!msg.equals(UndoManager.CANT_UNDO));
		}

		public void applicationEvent(ApplicationEvent e) {
			if(e.type == ApplicationEvent.VPATHWAY_CREATED) {
				((VPathway)e.source).getUndoManager().addListener(this);
			}
		}
	}
}
