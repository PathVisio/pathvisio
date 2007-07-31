package org.pathvisio.gui.swing;

import javax.swing.JEditorPane;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.Gdb;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

public class BackpagePane extends JEditorPane implements SelectionListener, ApplicationEventListener {
	PathwayElement input;
	
	public BackpagePane() {
		super();
		setEditable(false);
		setContentType("text/html");
		
		Engine.getCurrent().addApplicationEventListener(this);
		VPathway vp = Engine.getCurrent().getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);
		
	}

	public void setInput(final PathwayElement e) {
		if(e == null) {
			setText(Gdb.getBackpageHTML(null, null, null));
		} else if(input != e) {
			input = e;
			if(e.getObjectType() == ObjectType.DATANODE) {
				new Thread() {
					public void run() {
						setText(Gdb.getBackpageHTML(
								e.getGeneID(), 
								e.getSystemCode(), 
								e.getBackpageHead()));
					}
				}.start();
			}
		}
	}

	public void selectionEvent(SelectionEvent e) {
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			//Just take the first DataNode in the selection
			for(VPathwayElement o : e.selection) {
				if(o instanceof GeneProduct) {
					setInput(((GeneProduct)o).getGmmlData());
					break; //Selects the first, TODO: use setGmmlDataObjects
				}
			}
			break;
		case SelectionEvent.OBJECT_REMOVED:
			if(e.selection.size() != 0) break;
		case SelectionEvent.SELECTION_CLEARED:
			setInput(null);
			break;
		}
	}

	public void applicationEvent(ApplicationEvent e) {
		if(e.type == ApplicationEvent.VPATHWAY_CREATED) {
			((VPathway)e.source).addSelectionListener(this);
		}
	}
}
