//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 

//http://www.apache.org/licenses/LICENSE-2.0 

//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.

package org.pathvisio.view.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class PathwayTransferable implements Transferable {
	public static final String INFO_DATASOURCE = "COPIED";

	/**
	 * DataFlavor used for transfering raw GPML code. Mimetype is 'text/xml'.
	 * Note that the equals method of DataFlavor only checks for the main mimetype ('text'),
	 * so returns true for any DataFlavor that stores text, not only xml.
	 */
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

		try {
			Document doc = GpmlFormat.createJdom(pnew);
			out = xmlout.outputString(doc);
		} catch(Exception e) {
			Logger.log.error("Unable to copy to clipboard", e);
		}

		return out;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.stringFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return gpmlDataFlavor.equals(flavor);
	}
	
	public static URL getFileURL(Transferable t) throws UnsupportedFlavorException, IOException {
		for(DataFlavor df : t.getTransferDataFlavors()) {
			if(DataFlavor.javaFileListFlavor.equals(df)) {
				//Return the first element of the file list
				return ((List<File>)t.getTransferData(df)).get(0).toURL();
			}
			//Gnome fix:
			//Check for text/uri-list mime type, an uri list separated by \n
			if(String.class.equals(df.getRepresentationClass())) {
				if("uri-list".equalsIgnoreCase(df.getSubType())) {
					String uriList = (String)t.getTransferData(df);
					return new URL(uriList.substring(0, uriList.indexOf("\n") -1));
				}
			}
		}
		return null;
	}
	
	public static String getXml(Transferable t) throws UnsupportedFlavorException, IOException {
		for(DataFlavor df : t.getTransferDataFlavors()) {
			if(DataFlavor.stringFlavor.equals(df)) {
				 //Make sure this is not the gnome's uri-list
				if(!"uri-list".equalsIgnoreCase(df.getSubType())) {
					return (String)t.getTransferData(df);
				}
			}
		}
		return null;
	}
	
	/**
	 * Creates a pathway from the data in the provided {@link Transferable}.
	 * @param t
	 * @return
	 * @throws ConverterException
	 * @throws MalformedURLException
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	public static Pathway pathwayFromTransferable(Transferable t) throws ConverterException, MalformedURLException, UnsupportedFlavorException, IOException {
		Pathway pnew = new Pathway();
		
		URL url = getFileURL(t);
		if(url != null) {
			File f = new File(url.getFile());
			File file = new File(url.getFile());
			pnew.readFromXml(file, true);
			return pnew;
		}
		
		String xml = getXml(t);
		if(xml != null) {
			GpmlFormat.readFromXml(pnew, new StringReader(xml), true);

			List<PathwayElement> elements = new ArrayList<PathwayElement>();
			for(PathwayElement elm : pnew.getDataObjects()) {
				if(elm.getObjectType() != ObjectType.MAPPINFO) {
					elements.add(elm);
				} else {
					//Only add mappinfo if it's not generated by the transferable
					String source = elm.getMapInfoDataSource();
					if(!PathwayTransferable.INFO_DATASOURCE.equals(source)) {
						elements.add(elm);
					}
				}
			}
		}
		return null;
	}


	/**
	 * Opens a new pathway from the data in the {@link Transferable}, using the provided {@link Engine}.
	 * If the {@link Transferable} contains a link to a file, the pathway in this file will be opened. 
	 * If the {@link Transferable} contains gpml code, a new pathway will be created, and the gpml will be
	 * loaded into this pathway.
	 * @param t
	 * @param engine
	 * @throws IOException 
	 * @throws UnsupportedFlavorException 
	 * @throws ConverterException 
	 */
	public static void openPathwayFromTransferable(Transferable t, Engine engine) throws UnsupportedFlavorException, IOException, ConverterException {
		URL url = getFileURL(t);
		if(url != null) {
			engine.openPathway(url);
		}
		
		String xml = getXml(t);
		if(xml != null) {
			engine.newPathway();
			engine.getActivePathway().readFromXml(new StringReader(xml), true);
		}
	}
}
