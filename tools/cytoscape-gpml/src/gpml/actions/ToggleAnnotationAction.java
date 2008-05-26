package gpml.actions;

import giny.view.GraphView;
import gpml.GpmlHandler;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;

import org.pathvisio.debug.Logger;

import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;

public class ToggleAnnotationAction extends CytoscapeAction {
	GpmlHandler gpmlHandler;
	
	boolean checked = true;
	
	public ToggleAnnotationAction(GpmlHandler gpmlHandler) {
		this.gpmlHandler = gpmlHandler;
	}
	
	protected void initialize() {
		putValue(NAME, "Toggle GPML annotations");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl H"));
		super.initialize();
	}
	
	public String getPreferredMenu() {
		return "View";
	}
	
	public boolean isInMenuBar() {
		return true;
	}
	
	public boolean isInToolBar() {
		return false;
	}
	
	public void menuSelected(MenuEvent e) {
		Logger.log.trace(getClass() + ": menuSelected()");
	}
	
	public void actionPerformed(ActionEvent e) {
		checked = !checked;
		GraphView view = Cytoscape.getCurrentNetworkView();
		if(view != null) {
			gpmlHandler.showAnnotations(view, checked);
		}
	}
}
