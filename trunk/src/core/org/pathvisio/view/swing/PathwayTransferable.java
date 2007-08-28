// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.view.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class PathwayTransferable implements Transferable {
	public static final String INFO_DATASOURCE = "COPIED";
	
	public static final DataFlavor gpmlDataFlavor = new DataFlavor(String.class, "text/xml");
	
	List<PathwayElement> elements;
	Pathway pathway;
	
	public PathwayTransferable(Pathway source, List<PathwayElement> elements) {
		this.elements = elements;
		this.pathway = source;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		Object out = null;

		XMLOutputter xmlout = new XMLOutputter(Format.getPrettyFormat());

		Pathway pnew = new Pathway();
		
		//Always add biopax information
		//TODO: Only when referred to
		PathwayElement biopax = pathway.getBiopax();
		if(biopax != null) {
			pnew.add(biopax.copy());
		}

		Set<String> ids = new HashSet<String>();
		Set<String> groupIds = new HashSet<String>();
		
		boolean infoFound = false;
		for(PathwayElement e : elements) {
			if(e.getGraphId() != null) {
				ids.add(e.getGraphId());
			}
			if(e.getGroupRef() != null) {
				groupIds.add(e.getGroupRef());
			}
			if(e.getObjectType() == ObjectType.MAPPINFO) {
				infoFound = true;
			}
		}
		
		for(PathwayElement e : elements) {
			//Check for valid graphRef (with respect to other copied elements)
			PathwayElement enew = e.copy();
			if(!ids.contains(enew.getStartGraphRef())) {
				enew.setStartGraphRef(null);
			}
			if(!ids.contains(enew.getEndGraphRef())) {
				enew.setEndGraphRef(null);
			}
			pnew.add(enew);
		}
		
		//If no mappinfo, create a dummy one that we can recognize lateron
		if(!infoFound) {
			PathwayElement info = new PathwayElement(ObjectType.MAPPINFO);
			info.setMapInfoDataSource(INFO_DATASOURCE);
			pnew.add(info);
		}
		
		if(gpmlDataFlavor.equals(flavor)) {
			try {
				Document doc = GpmlFormat.createJdom(pnew);
				out = xmlout.outputString(doc);
			} catch(Exception e) {
				Logger.log.error("Unable to copy to clipboard", e);
			}
		}
		
		return out;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { gpmlDataFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return gpmlDataFlavor.equals(flavor);
	}
}
