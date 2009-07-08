// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.cytoscape.actions;

import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.pathvisio.cytoscape.GpmlPlugin;
import org.pathvisio.model.ConverterException;

/**
 * Export current network as GPML
 */
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
