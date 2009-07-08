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
package org.pathvisio.cytoscape;

import cytoscape.data.readers.AbstractGraphReader;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.LayoutAdapter;
import cytoscape.task.TaskMonitor;
import cytoscape.view.CyNetworkView;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.pathvisio.model.Pathway;

/**
 * An AbstractGraphReader that uses Pathway.readFromXml to read GPML
 * and then uses @{link GpmlConverter} to turn it into a real
 * network with nodes and edges.
 * <p>
 * Can handle files and URLs.
 */
public class GpmlReader extends AbstractGraphReader {
	GpmlConverter converter;
	GpmlHandler gpmlHandler;
	
	URLConnection urlCon;
	
	public GpmlReader(String fileName, GpmlHandler gpmlHandler) {
		super(fileName);
		this.gpmlHandler = gpmlHandler;
	}

	public GpmlReader(URLConnection con, URL url, GpmlHandler gpmlHandler) {
		super(url.toString());
		urlCon = con;
		this.gpmlHandler = gpmlHandler;
	}
	
	public void read() throws IOException {
		try {
			Pathway pathway = new Pathway();
			if(urlCon != null) {
				pathway.readFromXml(new InputStreamReader(urlCon.getInputStream()), true);
			} else {
				pathway.readFromXml(new File(fileName), true);
			}
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