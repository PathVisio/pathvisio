package org.pathvisio.cytoscape.actions;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.pathvisio.cytoscape.GpmlPlugin;

import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;

public class SettingsAction extends CytoscapeAction {
	GpmlPlugin plugin;

	public SettingsAction(GpmlPlugin plugin) {
		this.plugin = plugin;
	}

	protected void initialize() {
		super.initialize();
		putValue(NAME, "Settings");
	}

	public String getPreferredMenu() {
		return "Plugins";
	}

	public boolean isInMenuBar() {
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		JCheckBox checkbox = new JCheckBox("Show GPML as Cytoscape network.");  
		checkbox.setSelected(plugin.isLoadAsNetwork());
		String message = "Showing a pathway without redundant nodes and no graphical elements?";  
		Object[] params = {message, checkbox};
		int r = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), params, "Settings", JOptionPane.OK_CANCEL_OPTION);
		if(r == 0) {
			boolean loadAsNetwork = checkbox.isSelected();  
			plugin.setLoadAsNetwork(loadAsNetwork);
		}
	}

}
