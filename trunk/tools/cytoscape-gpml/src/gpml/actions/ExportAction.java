package gpml.actions;

import gpml.GpmlPlugin;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.pathvisio.model.ConverterException;

import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;

public class ExportAction extends CytoscapeAction {
	GpmlPlugin plugin;
	
	public ExportAction(GpmlPlugin plugin) {
		super();
		this.plugin = plugin;		
	}
	
	protected void initialize() {
		super.initialize();
		putValue(NAME, "Export as GPML");
	}
	
	public String getPreferredMenu() {
		return "File.Export";
	}

	public boolean isInMenuBar() {
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String fn = f.getName().toLowerCase();
				return 	fn.endsWith(".gpml") ||
						fn.endsWith(".xml");
			}
			public String getDescription() {
				return "GPML file (*.gpml, *.xml)";
			}
		});
		fc.showSaveDialog(Cytoscape.getDesktop());
		File file = fc.getSelectedFile();
		if(file != null) {
			try {
				plugin.writeToFile(Cytoscape.getCurrentNetworkView(), file);
			} catch (ConverterException e1) {
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
						"Unable to save to GPML: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
