package org.pathvisio.visualization.gui;

import java.awt.Component;
import java.awt.Frame;

import org.pathvisio.gui.swing.dialogs.OkCancelDialog;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationManager;

public class VisualizationDialog extends OkCancelDialog {
	
	VisualizationPanel visPanel;
	VisualizationManager visMgr;
	
	public VisualizationDialog(VisualizationManager visMgr, Frame frame, Component locationComp) {
		super(frame, "Visualization options", locationComp, true, false);
		
		this.visMgr = visMgr;
		
		//If there is no visualization yet, create one
		if(visMgr.getVisualizations().size() == 0) {
			visMgr.addVisualization(new Visualization("untitled"));
		}
		if(visMgr.getActiveVisualization() == null) {
			visMgr.setActiveVisualization(0);
		}
		visPanel.setVisualizationManager(visMgr);
		pack();
	}

	protected Component createDialogPane() {
		visPanel = new VisualizationPanel();
		return visPanel;
	}
	
	protected void okPressed() {
		visMgr.saveXML(); //Save the settings
		super.okPressed();
	}
}
