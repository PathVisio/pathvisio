// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.view;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.model.ConnectorShape;
import org.pathvisio.core.model.FreeConnectorShape;
import org.pathvisio.core.model.GroupStyle;
import org.pathvisio.core.model.MLine;
import org.pathvisio.core.model.MState;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElement.MPoint;
import org.pathvisio.core.model.ShapeType;
import org.pathvisio.core.util.Resources;
import org.pathvisio.core.util.Utils;
import org.pathvisio.core.view.SelectionBox.SelectionEvent;
import org.pathvisio.core.view.SelectionBox.SelectionListener;

import static org.pathvisio.core.model.ObjectType.STATE;

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
	private static final URL IMG_COPY= Resources.getResourceURL("copy.gif");
	private static final URL IMG_PASTE = Resources.getResourceURL("paste.gif");
	private static final URL IMG_UNDO = Resources.getResourceURL("undo.gif");

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
	public final SelectObjectAction selectInteractions;
	public final SelectObjectAction selectLines;
	public final SelectObjectAction selectShapes;
	public final SelectObjectAction selectLabels;
	public final SelectAllAction selectAll;
	public final GroupAction toggleGroup;
	public final ComplexAction toggleComplex;
	public final DeleteAction delete1;
	public final DeleteAction delete2;
	public final CopyAction copy;
	public final PasteAction paste;
	public final PositionPasteAction positionPaste;
	public final KeyMoveAction keyMove;
	public final UndoAction undo;
	public final AddAnchorAction addAnchor;
	public final WaypointAction addWaypoint;
	public final WaypointAction removeWaypoint;
	public final OrderBottomAction orderSendToBack;
	public final OrderTopAction orderBringToFront;
	public final OrderUpAction orderUp;
	public final OrderDownAction orderDown;
	public final ShowUnlinkedConnectors showUnlinked;
	public final ShowInteractionsWithLabels showInteractingLabels;
	public final AddState addState;
	public final RemoveState removeState;
	public final TextFormattingAction formatText;

	private final Engine engine;

	ViewActions(Engine engine, VPathway vp) {
		this.engine = engine;
		vPathway = vp;

		vp.addSelectionListener(this);
		vp.addVPathwayListener(this);

		selectDataNodes = new SelectClassAction("DataNode", GeneProduct.class);
		selectInteractions = new SelectObjectAction("Interactions", ObjectType.LINE);
		selectLines = new SelectObjectAction("Graphical Lines", ObjectType.GRAPHLINE);
		selectShapes = new SelectObjectAction("Shapes", ObjectType.SHAPE);
		selectLabels = new SelectObjectAction("Labels", ObjectType.LABEL);
		selectAll = new SelectAllAction();
		toggleGroup = new GroupAction();
		toggleComplex = new ComplexAction();
		delete1 = new DeleteAction(java.awt.event.KeyEvent.VK_DELETE);
		delete2 = new DeleteAction(java.awt.event.KeyEvent.VK_BACK_SPACE);
		copy = new CopyAction(engine);
		paste = new PasteAction(engine);
		positionPaste = new PositionPasteAction(engine);
		keyMove = new KeyMoveAction(engine, null);
		undo = new UndoAction(engine);
		addAnchor = new AddAnchorAction();
		addWaypoint = new WaypointAction(true);
		removeWaypoint = new WaypointAction(false);
		orderSendToBack = new OrderBottomAction(engine);
		orderBringToFront = new OrderTopAction(engine);
		orderUp = new OrderUpAction(engine);
		orderDown = new OrderDownAction(engine);
		showUnlinked = new ShowUnlinkedConnectors();
		showInteractingLabels = new ShowInteractionsWithLabels();
		addState = new AddState();
		removeState = new RemoveState();
		formatText = new TextFormattingAction(engine, null);

		registerToGroup(selectDataNodes, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(selectAll, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(toggleGroup, GROUP_ENABLE_EDITMODE);
		registerToGroup(toggleGroup, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(toggleComplex, GROUP_ENABLE_EDITMODE);
		registerToGroup(toggleComplex, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(delete1, GROUP_ENABLE_EDITMODE);
		registerToGroup(delete1, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(copy, 	ViewActions.GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(paste, 	ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(paste, 	ViewActions.GROUP_ENABLE_EDITMODE);
		registerToGroup(positionPaste, 	ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(positionPaste, 	ViewActions.GROUP_ENABLE_EDITMODE);
		registerToGroup(keyMove, ViewActions.GROUP_ENABLE_EDITMODE);
		registerToGroup(addAnchor, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(addWaypoint, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(removeWaypoint, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(showUnlinked, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(showInteractingLabels, GROUP_ENABLE_VPATHWAY_LOADED);

		resetGroupStates();
	}

	private Map<String, Set<Action>> actionGroups = new HashMap<String, Set<Action>>();
	private Map<Action, Set<String>> groupActions = new HashMap<Action, Set<String>>();

	/**
	 * Register the given action to a group (one of the GROUP* contants)
	 * @param a	The action to register
	 * @param group The group to register the action to
	 */
	public void registerToGroup(Action a, String group) 
	{
		Utils.multimapPut(actionGroups, group, a);
		Utils.multimapPut(groupActions, a, group);
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

	Map<String, Boolean> groupState = new HashMap<String, Boolean>();

	/**
	 * Resets the group state for the registered actions to the given VPathway's state
	 * e.g. all actions in GROUP_ENABLE_EDITMODE will be enabled when the pathway is in
	 * edit mode, and disabled when not.
	 * @param v The VPathway of which the state will be determined
	 */
	private void resetGroupStates(VPathway v) {
		groupState.put(GROUP_ENABLE_VPATHWAY_LOADED, true);
		groupState.put(GROUP_ENABLE_EDITMODE, vPathway.isEditMode());
		groupState.put(GROUP_ENABLE_WHEN_SELECTION, vPathway.getSelectedPathwayElements().size() > 0);

		for(Action a : groupActions.keySet()) {
			Set<String> groups = groupActions.get(a);
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
		//Don't refresh at object redraw / move
		switch(e.getType()) {
		case EDIT_MODE_OFF:
		case EDIT_MODE_ON:
		case ELEMENT_ADDED:
		case MODEL_LOADED:
		case ELEMENT_CLICKED_UP:
			resetGroupStates(vp);
		default:
			break;
		}
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

	/** "Copy" command in the menu / toolbar, copies selected pathway elements to the clipboard */
	public static class CopyAction extends AbstractAction {
		Engine engine;

		public CopyAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Copy");
			putValue(SMALL_ICON, new ImageIcon(IMG_COPY));
			String descr = "Copy selected pathway objects to clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C,
							Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
			if(vp != null) vp.copyToClipboard();
		}
	}

	/** "Paste" command in the menu / toolbar, pastes from clipboard */
	public static class PasteAction extends AbstractAction {
		Engine engine;

		public PasteAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Paste");
			putValue(SMALL_ICON, new ImageIcon(IMG_PASTE));
			String descr = "Paste pathway objects from clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
			if(isEnabled() && vp != null) vp.pasteFromClipboard();
		}
	}
	
	/** "Paste" command from the right click menu, pastes from clipboard */
	public static class PositionPasteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		Engine engine;
		private Point position;
		
		public PositionPasteAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Paste");
			putValue(SMALL_ICON, new ImageIcon(IMG_PASTE));
			String descr = "Paste pathway objects from clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
//			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V,
//					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
			if(isEnabled() && vp != null) {
				vp.positionPasteFromClipboard(position);
			}
			resetPosition();
		}
		
		public void setPosition(Point position) {
			this.position = position;
		}
		
		private void resetPosition() {
			position = null;
		}
	}

	/** "Nudge" action, move selected element(s) a bit in the direction of the cursor key pressed. */
	public static class KeyMoveAction extends AbstractAction {
		Engine engine;
		KeyStroke key;

		public KeyMoveAction(Engine engine, KeyStroke key) {
			this.engine = engine;
			this.key = key;
		}

		public void actionPerformed(ActionEvent e) {

			int moveIncrement = 0;

			if ((e.getModifiers() &
					ActionEvent.SHIFT_MASK) != 0)
			{ moveIncrement = LARGE_INCREMENT;}
			else {moveIncrement = SMALL_INCREMENT;}

			VPathway vp = engine.getActiveVPathway();
			vp.moveByKey(key, moveIncrement);
		}
	}

	private class SelectClassAction extends AbstractAction {

		Class<?> c;
		public SelectClassAction(String name, Class<?> c) {
			super("Select all " + name + "s");
			this.c = c;
		}
		public void actionPerformed(ActionEvent e) {
			vPathway.selectObjects(c);
		}
	}
	
	/**
	 * Selects all objects of a given objectType
	 * @author anwesha
	 *
	 */
	private class SelectObjectAction extends AbstractAction {
		private ObjectType objtype;
		public SelectObjectAction(String name, ObjectType objtype) {
			super("Select all " + name);
			this.objtype = objtype;
		}
		
		public void actionPerformed(ActionEvent e) {
			vPathway.selectObjectsByObjectType(objtype);
		}
	}
		
	
	private class SelectAllAction extends AbstractAction {

		public SelectAllAction() {
			super("Select all");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		public void actionPerformed(ActionEvent e) {
			vPathway.selectAll();
		}
	}

	/**
	 * Add/remove waypoints on a segmented line.
	 * @author thomas
	 */
	private class WaypointAction extends AbstractAction implements SelectionListener {
		boolean add;
		
		public WaypointAction(boolean add) {
			this.add = add;
			
			if(add) {
				putValue(NAME, "Add waypoint");
				putValue(SHORT_DESCRIPTION, "Add a waypoint to the selected line");
			} else {
				putValue(NAME, "Remove last waypoint");
				putValue(SHORT_DESCRIPTION, "Removes the last waypoint from the selected line");
			}
			vPathway.addSelectionListener(this);
			setEnabled(false);
		}

		public void selectionEvent(SelectionEvent e) {
			boolean enable = false;
			if(e.selection.size() == 1) {
				VPathwayElement ve = e.selection.iterator().next();
				if(ve instanceof Line) {
					ConnectorShape s = ((MLine)((Line)ve).getPathwayElement()).getConnectorShape();
					enable = s instanceof FreeConnectorShape;
				} else {
					enable = false;
				}
			}
			setEnabled(enable);
		}

		public void actionPerformed(ActionEvent e) {
			List<Graphics> selection = vPathway.getSelectedGraphics();
			if(selection.size() == 1) {
				Graphics g = selection.get(0);
				if(g instanceof Line) {
					Line l = (Line)g;
					ConnectorShape s = ((MLine)l.getPathwayElement()).getConnectorShape();
					if(s instanceof FreeConnectorShape) {
						vPathway.getUndoManager().newAction("" + getValue(NAME));
						if(add) {
							addWaypoint((FreeConnectorShape)s, (MLine)l.getPathwayElement());
						} else {
							removeWaypoint((MLine)l.getPathwayElement());
						}
					}
				}
			}
		}
		
		private void removeWaypoint(MLine l) {
			//TODO: Instead of removing the last point, it would be better to adjust the context
			//menu to remove a specific point (like with anchors). This could be done by making 
			//VPoint extend VPathwayElement so we can directly get the selected waypoint here.
			List<MPoint> newPoints = new ArrayList<MPoint>(l.getMPoints());
			newPoints.remove(newPoints.size() - 2);
			l.setMPoints(newPoints);
		}
		
		private void addWaypoint(FreeConnectorShape s, MLine l) {
			//TODO: It would be nice to have access to the mouse position here, so
			//we can add the waypoint to where the user clicked
			//Point2D mp = new Point2D.Double(vPathway.mFromV(p.getX()), vPathway.mFromV(p.getY()));
			//WayPoint nwp = new WayPoint(mp);
			//double c = s.toLineCoordinate(p);
			
			//We don't have the mouse position, just add the waypoint in the center
			//with an offset if needed
			List<MPoint> oldPoints = l.getMPoints();
			List<MPoint> newPoints = new ArrayList<MPoint>(oldPoints);
			
			int i = oldPoints.size() - 1; //MPoints size always >= 2
			MPoint mp = oldPoints.get(i);
			MPoint mp2 = oldPoints.get(i - 1);
			double mc = s.toLineCoordinate(mp.toPoint2D());
			double mc2 = s.toLineCoordinate(mp2.toPoint2D());
			double c = mc2 + (mc - mc2) / 2.0; //Add new waypoint on center of last segment
			Point2D p = s.fromLineCoordinate(c);
			newPoints.add(i, l.new MPoint(p.getX(), p.getY()));
			l.setMPoints(newPoints);
		}
	}
	
	private class AddAnchorAction extends AbstractAction implements SelectionListener {

		public AddAnchorAction() {
			vPathway.addSelectionListener(this);
			putValue(NAME, "Add anchor");
			putValue(SHORT_DESCRIPTION, "Add an anchor point to the selected line");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
						l.gdata.addMAnchor(0.4);
					}
				}
			}
		}
	}

	private class AddState extends AbstractAction
	{
		AddState()
		{
			super ("Add State...");
		}
		
		public void actionPerformed(ActionEvent arg0)
		{
			List<Graphics> selection = vPathway.getSelectedGraphics();
			if(selection.size() > 0) {
				vPathway.getUndoManager().newAction("Add State");
				for(Graphics g : selection) {
					if(g instanceof GeneProduct) {
						GeneProduct gp = (GeneProduct)g;
						PathwayElement elt = PathwayElement.createPathwayElement(STATE);
						elt.setInitialSize();
						((MState)elt).linkTo (gp.getPathwayElement(), 1.0, 1.0); 
						elt.setShapeType(ShapeType.OVAL);
						engine.getActivePathway().add(elt);
						elt.setGeneratedGraphId();
					}
				}
			}			
		}
	}

	private class RemoveState extends AbstractAction
	{
		RemoveState()
		{
			super ("Remove State...");
		}
		
		public void actionPerformed(ActionEvent arg0)
		{
			vPathway.getUndoManager().newAction("Remove State");
			List<VPathwayElement> toRemove = new ArrayList<VPathwayElement>();
			List<Graphics> selection = vPathway.getSelectedGraphics();
			if(selection.size() > 0) {
				for(Graphics g : selection) {
					if(g instanceof State) {
						toRemove.add(g);
					}
				}
			}			
			if (toRemove.size() > 0)
			{
				vPathway.getUndoManager().newAction("Remove state(s)");
				vPathway.removeDrawingObjects(toRemove, true);
			}
			
		}
	}

	private class ComplexAction extends GroupActionBase {
		public ComplexAction() {
			super(
				"Create complex", "Break complex",
				"Create a complex from selected elements",
				"Break selected complex",
				GroupStyle.COMPLEX,
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
				);
		}
	}

	private class GroupAction extends GroupActionBase {
		public GroupAction() {
			super(
				"Group", "Ungroup",
				"Group selected elements",
				"Ungroup selected group",
				GroupStyle.GROUP,
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
			);
		}
	}
	private class GroupActionBase extends AbstractAction implements SelectionListener {
		private String groupLbl, ungroupLbl, groupTt, ungroupTt;
		private GroupStyle groupStyle;

		public GroupActionBase(String groupLbl, String ungroupLbl,
				String groupTt, String ungroupTt,
				GroupStyle style, KeyStroke keyStroke) {
			super();
			this.groupStyle = style;
			this.groupLbl = groupLbl;
			this.ungroupLbl = ungroupLbl;
			this.groupTt = groupTt;
			this.ungroupTt = ungroupTt;
			vPathway.addSelectionListener(this);
			putValue(NAME, groupLbl);
			putValue(SHORT_DESCRIPTION, groupTt);
			putValue(ACCELERATOR_KEY, keyStroke);
			setLabel();
		}

		public void actionPerformed(ActionEvent e) {
			if(!isEnabled()) return; //Don't perform action if not enabled
			Group g = vPathway.toggleGroup(vPathway.getSelectedGraphics());
			if(g != null) {
				g.getPathwayElement().setGroupStyle(groupStyle);
			}
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
				putValue(Action.NAME, groupLbl);
				putValue(SHORT_DESCRIPTION, groupTt);
			} else {
				putValue(Action.NAME, ungroupLbl);
				putValue(SHORT_DESCRIPTION, ungroupTt);
			}
		}
	}

	private class DeleteAction extends AbstractAction {

		public DeleteAction(int ke) {
			super();
			putValue(NAME, "Delete");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ke , 0));
		}

		public void actionPerformed(ActionEvent e) {
			if(!isEnabled()) return; //Don't perform action if not enabled

			List<VPathwayElement> toRemove = new ArrayList<VPathwayElement>();
			for(VPathwayElement o : vPathway.getDrawingObjects())
			{
				if (!o.isSelected() || o == vPathway.selection || o == vPathway.getMappInfo())
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

	/** "Undo" command in the menu / toolbar */
	public static class UndoAction extends AbstractAction implements UndoManagerListener, ApplicationEventListener {
		Engine engine;

		public UndoAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Undo");
			putValue(SHORT_DESCRIPTION, "Undo last action");
			putValue(SMALL_ICON, new ImageIcon(IMG_UNDO));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
							Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
					);
			engine.addApplicationEventListener(this);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
			if (vp != null)
			{
				vp.undo();
			}
		}

		public void undoManagerEvent(UndoManagerEvent e) {
			String msg = e.getMessage();
			putValue(NAME, "Undo: " + msg);
			setEnabled(!msg.equals(UndoManager.CANT_UNDO));
		}

		public void applicationEvent(ApplicationEvent e) 
		{
			switch (e.getType()) {
			case VPATHWAY_CREATED:
				((VPathway)e.getSource()).getUndoManager().addListener(this);
				break;
			case VPATHWAY_DISPOSED:
				((VPathway)e.getSource()).getUndoManager().removeListener(this);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 */
	public static class OrderTopAction extends AbstractAction
	{
		Engine engine;

		public OrderTopAction(Engine engine)
		{
			this.engine = engine;
			putValue(NAME, "Bring to front");
			putValue(SHORT_DESCRIPTION, "Bring the element in front of all other elements");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_CLOSE_BRACKET,
					InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e)
		{
			VPathway vp = engine.getActiveVPathway();
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
		Engine engine;

		public OrderBottomAction(Engine engine)
		{
			this.engine = engine;
			putValue(NAME, "Send to Back");
			putValue(SHORT_DESCRIPTION, "Send the element behind all other elements");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_OPEN_BRACKET,
					InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e)
		{
			VPathway vp = engine.getActiveVPathway();
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
		Engine engine;
		public OrderUpAction(Engine engine)
		{
			this.engine = engine;
			putValue(NAME, "Bring Forward");
			putValue(SHORT_DESCRIPTION, "Bring Forward");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_CLOSE_BRACKET,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
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
		Engine engine;

		public OrderDownAction(Engine engine)
		{
			this.engine = engine;
			putValue(NAME, "Send Backward");
			putValue(SHORT_DESCRIPTION, "Send Backward");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_OPEN_BRACKET,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
			if(vp != null) {
				vp.moveGraphicsDown(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

	/**
	 * Action that toggles highlight of points that are not linked
	 * to an object
	 */
	public class ShowUnlinkedConnectors extends AbstractAction {
		public ShowUnlinkedConnectors() {
			putValue(NAME, "Highlight unlinked interactions");
			putValue(SHORT_DESCRIPTION, "Highlight all interactions that are not linked");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			vPathway.resetHighlight();
				for(PathwayElement pe : vPathway.getPathwayModel().getDataObjects()) {
					if(pe.getObjectType() == ObjectType.LINE) {
						Line vl = (Line)vPathway.getPathwayElementView(pe);
						String grs = pe.getStartGraphRef();
						String gre = pe.getEndGraphRef();
						if(grs == null || "".equals(grs)) {
							vl.getStart().highlight();
						}
						if(gre == null || "".equals(gre)) {
							vl.getEnd().highlight();
						}
					}
				}
		}
	}
	
	
	/**
	 * Action that toggles highlight of points that connect an Interaction to a Label.
	 */
	public class ShowInteractionsWithLabels extends AbstractAction {
		public ShowInteractionsWithLabels() {
			putValue(NAME, "Highlight interactions to Labels");
			putValue(SHORT_DESCRIPTION, "Highlight all interactions that link to a Label instead of a DataNode");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			vPathway.resetHighlight();
				for(PathwayElement pe : vPathway.getPathwayModel().getDataObjects()) {
					if(pe.getObjectType() == ObjectType.LINE) {
						Line vl = (Line)vPathway.getPathwayElementView(pe);
						String grs = pe.getStartGraphRef();
						String gre = pe.getEndGraphRef();
						if(grs == null || "".equals(grs)) {
							PathwayElement start = vPathway.getPathwayModel().getElementById(grs);
							if (start != null && start.getObjectType() == ObjectType.LABEL) vl.getStart().highlight();
						}
						if(gre != null || "".equals(gre)) {
							PathwayElement end = vPathway.getPathwayModel().getElementById(gre);
							if (end != null && end.getObjectType() == ObjectType.LABEL) vl.getEnd().highlight();
						}
					}
				}
		}
	}


	/**
	 * Action for toggling bold or italic flags on selected elements.
	 */
	public static class TextFormattingAction extends AbstractAction 
	{	
		Engine engine;
		KeyStroke key;

		public TextFormattingAction(Engine engine, KeyStroke key) {
			this.engine = engine;
			this.key = key;
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
			Set<VPathwayElement> changeTextFormat = new HashSet<VPathwayElement>();
			changeTextFormat = vp.getSelectedPathwayElements();
			for(VPathwayElement velt : changeTextFormat) {
				if (velt instanceof Graphics){
					PathwayElement o = ((Graphics)velt).getPathwayElement();
					if(key.equals (VPathway.KEY_BOLD)) {
                    				if(o.isBold()) o.setBold(false);
                    				else o.setBold(true);
                    }
					else if(key.equals (VPathway.KEY_ITALIC)) {
						if(o.isItalic()) o.setItalic(false);
						else o.setItalic(true);						
					}
				}
			}
			
			vp.redraw();
		}
	}
}
