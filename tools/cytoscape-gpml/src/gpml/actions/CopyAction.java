package gpml.actions;

import gpml.GpmlPlugin;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import cytoscape.util.CytoscapeAction;

public class CopyAction extends CytoscapeAction {
	GpmlPlugin plugin;
	
	public CopyAction(GpmlPlugin plugin) {
		this.plugin = plugin;
	}
	
	protected void initialize() {
		super.initialize();
		putValue(NAME, "Copy GPML");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
	}
	
	public String getPreferredMenu() {
		return "Edit";
	}
	
	public boolean isInMenuBar() {
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		plugin.drag(Toolkit.getDefaultToolkit().getSystemClipboard());
	}
}
