package org.pathvisio.cytoscape;

import java.io.File;
import java.io.IOException;

import org.pathvisio.model.Pathway;

import cytoscape.data.readers.AbstractGraphReader;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.LayoutAdapter;
import cytoscape.task.TaskMonitor;
import cytoscape.view.CyNetworkView;

public class GpmlReader extends AbstractGraphReader {
	GpmlConverter converter;
	GpmlHandler gpmlHandler;
	
	public GpmlReader(String fileName, GpmlHandler gpmlHandler) {
		super(fileName);
		this.gpmlHandler = gpmlHandler;
	}

	public void read() throws IOException {
		try {
			Pathway pathway = new Pathway();
			pathway.readFromXml(new File(fileName), true);
			converter = new GpmlConverter(gpmlHandler, pathway);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}
	}
	
	public CyLayoutAlgorithm getLayoutAlgorithm() {
		return new LayoutAdapter() {
			public void doLayout(CyNetworkView networkView, TaskMonitor monitor) {
				converter.layout(networkView);
			}
		};
	}
	
	public int[] getEdgeIndicesArray() {
		return converter.getEdgeIndicesArray();
	}
	
	public int[] getNodeIndicesArray() {
		return converter.getNodeIndicesArray();
	}
		
	public String getNetworkName() {
		String pwName = converter.getPathway().getMappInfo().getMapInfoName();
		return pwName == null ? super.getNetworkName() : pwName;
	}
}