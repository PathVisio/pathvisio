//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 

//http://www.apache.org/licenses/LICENSE-2.0 

//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.

package org.pathvisio.view;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
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
	private static URL IMG_UNDO = Engine.getCurrent().getResourceURL("icons/undo.gif");

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
	public final AddAnchorAction addAnchor;
	public final OrderBottomAction orderSendToBack;
	public final OrderTopAction orderBringToFront;
	public final OrderUpAction orderUp;
	public final OrderDownAction orderDown;
	
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
		addAnchor = new AddAnchorAction();
		orderSendToBack = new OrderBottomAction();
		orderBringToFront = new OrderTopAction();
		orderUp = new OrderUpAction();
		orderDown = new OrderDownAction();

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
		registerToGroup(addAnchor, GROUP_ENABLE_WHEN_SELECTION);

		resetGroupStates();
	}

	HashMap<String, List<Action>> actionGroups = new HashMap<String, List<Action>>();
	HashMap<Action, List<String>> groupActions = new HashMap<Action, List<String>>();

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

		List<String> groups = groupActions.get(a);
		if(groups == null) {
			groupActions.put(a, groups = new ArrayList<String>());
		}
		if(!groups.contains(group)) groups.add(group);
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
	 * Resets the group state for the registered actions to the VPathway's state
	 * e.g. all actions in GROUP_ENABLE_EDITMODE will be enabled when the pathway is in 
	 * edit mode, and disabled when not.
	 */
	public void resetGroupStates() {
		resetGroupStates(vPathway);
	}

	/**
	 * Resets the group state for the registered actions to the given VPathway's state
	 * e.g. all actions in GROUP_ENABLE_EDITMODE will be enabled when the pathway is in 
	 * edit mode, and disabled when not.
	 * @param v The VPathway of which the state will be determined
	 */
	private void resetGroupStates(VPathway v) {
		HashMap<String, Boolean> groupState = new HashMap<String, Boolean>();
		groupState.put(GROUP_ENABLE_VPATHWAY_LOADED, true);
		groupState.put(GROUP_ENABLE_EDITMODE, vPathway.isEditMode());
		groupState.put(GROUP_ENABLE_WHEN_SELECTION, vPathway.getSelectedPathwayElements().size() > 0);

		for(Action a : groupActions.keySet()) {
			List<String> groups = groupActions.get(a);
			boolean enable = true;
			for(String g : groups) {
				enable &= groupState.get(g);
			}
			a.setEnabled(enable);
		}
	}

//	public void applicationEvent(ApplicationEvent e) {
//	if(e.type == ApplicationEvent.VPATHWAY_CREATED) {
//	VPathway vp = (VPathway)e.getSource();
//	vp.addSelectionListener(this);
//	vp.addVPathwayListener(this);
//	setGroupEnabled(true, GROUP_ENABLE_VPATHWAY_LOADED);
//	setGroupEnabled(vp.getSelectedGraphics().size() > 0, GROUP_ENABLE_WHEN_SELECTION);
//	setGroupEnabled(vp.isEditMode(), GROUP_ENABLE_EDITMODE);
//	}
//	}

	public void vPathwayEvent(VPathwayEvent e) {
		VPathway vp = (VPathway)e.getSource();
		resetGroupStates(vp);
	}

	public void selectionEvent(SelectionEvent e) {
		VPathway vp = ((SelectionBox)e.getSource()).getDrawing();
		resetGroupStates(vp);
	}

//	private abstract class EnableOnSelectAction extends AbstractAction implements SelectionListener {
//	public EnableOnSelectAction() {
//	vPathway.addSelectionListener(this);
//	}

//	public void selectionEvent(SelectionEvent e) {
//	setEnabled(vPathway.getSelectedGraphics().size() > 0);
//	}
//	}

	public static class CopyAction extends AbstractAction {	
		private static final long serialVersionUID = 1L;

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
		private static final long serialVersionUID = 1L;

		public PasteAction() {
			super();
			putValue(NAME, "Paste");
			putValue(SMALL_ICON, new ImageIcon(IMG_PASTE));
			String descr = "Paste pathway objects from clipboard";
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
		private static final long serialVersionUID = 1L;

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
		private static final long serialVersionUID = 1L;

		Class<?> c;
		public SelectClassAction(String name, Class<?> c) {
			super("Select all " + name + " objects");
			this.c = c;
		}
		public void actionPerformed(ActionEvent e) {
			vPathway.selectObjects(c);
		}
	}

	private class SelectAllAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SelectAllAction() {
			super("Select all");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl A"));
		}
		public void actionPerformed(ActionEvent e) {
			vPathway.selectAll();
		}
	}

	private class AddAnchorAction extends AbstractAction implements SelectionListener {
		private static final long serialVersionUID = 1L;

		public AddAnchorAction() {
			vPathway.addSelectionListener(this);
			putValue(NAME, "Add anchor");
			putValue(SHORT_DESCRIPTION, "Add an anchor point to the selected line");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl R"));
			setEnabled(false);
		}

		public void selectionEvent(SelectionEvent e) {
			boolean enable= false;
			for(VPathwayElement ve : e.selection) {
				if(ve instanceof Line) {
					enable = true;
				} else {
					enable = false;
					break;
				}
			}
			setEnabled(enable);
		}

		public void actionPerformed(ActionEvent e) {
			List<Graphics> selection = vPathway.getSelectedGraphics();
			if(selection.size() > 0) {
				vPathway.getUndoManager().newAction("Add anchor");
				for(Graphics g : selection) {
					if(g instanceof Line) {
						Line l = (Line)g;
						l.gdata.addMAnchor(0.5);
					}
				}
				vPathway.redrawDirtyRect();
			}
		}
	}

	private class GroupAction extends AbstractAction implements SelectionListener {
		private static final long serialVersionUID = 1L;

		public GroupAction() {
			super();
			vPathway.addSelectionListener(this);
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
			int unGrouped = 0;
			List<Graphics> selection = vPathway.getSelectedGraphics();
			for(Graphics g : selection) {
				if(g.getPathwayElement().getGroupRef() == null) {
					unGrouped++;
				}
			}
			setEnabled(true);
			if(unGrouped >= 2) {
				putValue(Action.NAME, "Group");
			} else {
				putValue(Action.NAME, "Ungroup");
			}
		}		
	}

	private class DeleteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

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
			if (toRemove.size() > 0)
			{
				vPathway.getUndoManager().newAction("Delete element(s)");
				vPathway.removeDrawingObjects(toRemove, true);
			}
		}
	}

	public static class UndoAction extends AbstractAction implements UndoManagerListener, ApplicationEventListener {
		private static final long serialVersionUID = 1L;

		public UndoAction() {
			super();
			putValue(NAME, "Undo");
			putValue(SHORT_DESCRIPTION, "Undo last action");
			putValue(SMALL_ICON, new ImageIcon(IMG_UNDO));

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
			if(e.getType() == ApplicationEvent.VPATHWAY_CREATED) {
				((VPathway)e.getSource()).getUndoManager().addListener(this);
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 */
	public static class OrderTopAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
		public OrderTopAction() 
		{
			putValue(NAME, "Bring to front");
			putValue(SHORT_DESCRIPTION, "Bring the element in front of all other elements of the same type");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(']', InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) {
				vp.moveGraphicsTop(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 */
	public static class OrderBottomAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
		public OrderBottomAction() 
		{
			putValue(NAME, "Send to back");
			putValue(SHORT_DESCRIPTION, "Send the element behind all other elements of the same type");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('[', InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) 
			{
				vp.moveGraphicsBottom(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 */
	public static class OrderUpAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
		public OrderUpAction() 
		{
			putValue(NAME, "Move up");
			putValue(SHORT_DESCRIPTION, "Move up");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('[', InputEvent.CTRL_DOWN_MASK));
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) {
				vp.moveGraphicsUp(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 */
	public static class OrderDownAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
		public OrderDownAction() 
		{
			putValue(NAME, "Move down");
			putValue(SHORT_DESCRIPTION, "Move down");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(']', InputEvent.CTRL_DOWN_MASK));
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) {
				vp.moveGraphicsDown(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

}
