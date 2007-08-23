package org.pathvisio.view;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

public class ViewActions {
	VPathway vPathway;
	
	public final SelectClassAction selectDataNodes;
	public final SelectAllAction selectAll;
	public final GroupAction toggleGroup;
	public final DeleteAction delete;
	
	
	public ViewActions(VPathway vp) {
		vPathway = vp;
		
		selectDataNodes = new SelectClassAction("DataNode", GeneProduct.class);
		selectAll = new SelectAllAction();
		toggleGroup = new GroupAction();
		delete = new DeleteAction();
	}
	
	private abstract class EnableOnSelectAction extends AbstractAction implements SelectionListener {
		public EnableOnSelectAction() {
			vPathway.addSelectionListener(this);
		}
		
		public void selectionEvent(SelectionEvent e) {
			setEnabled(vPathway.getSelectedGraphics().size() > 0);
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
		}
		public void actionPerformed(ActionEvent e) {
			vPathway.selectAll();
		}
	}
		
	private class GroupAction extends EnableOnSelectAction implements SelectionListener {
		public GroupAction() {
			super();
			putValue(NAME, "Group");
			setLabel();
		}

		public void actionPerformed(ActionEvent e) {
				vPathway.toggleGroup(vPathway.getSelectedGraphics());
		}

		public void selectionEvent(SelectionEvent e) {
			switch(e.type) {
			case SelectionEvent.OBJECT_ADDED:
			case SelectionEvent.OBJECT_REMOVED:
			case SelectionEvent.SELECTION_CLEARED:
				setLabel();
			}
			super.selectionEvent(e);
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
	
	private class DeleteAction extends EnableOnSelectAction implements SelectionListener {
		public DeleteAction() {
			super();
			putValue(NAME, "Delete");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_DELETE, 0));
		}

		public void actionPerformed(ActionEvent e) {
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
