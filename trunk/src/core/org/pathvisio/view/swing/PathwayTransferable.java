package org.pathvisio.view.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class PathwayTransferable implements Transferable {
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
		PathwayElement info = pathway.getMappInfo();
		if(info != null) pnew.add(info);
		pnew.add(pathway.getInfoBox().copy());
		PathwayElement biopax = pathway.getBiopax();
		if(biopax != null) pnew.add(biopax);

		for(PathwayElement e : elements) {
			pnew.add(e.copy());
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
