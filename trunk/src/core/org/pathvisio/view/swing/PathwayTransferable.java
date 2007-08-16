package org.pathvisio.view.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.PathwayElement;

public class PathwayTransferable implements Transferable {
	public static final DataFlavor pathwayDataFlavor = new DataFlavor(String.class, "text/xml");
	List<PathwayElement> elements;
	
	public PathwayTransferable(List<PathwayElement> elements) {
		this.elements = elements;
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		XMLOutputter xmlout = new XMLOutputter(Format.getPrettyFormat());
		String xml = "";
		for(PathwayElement e : elements) {
			try {
				Element xmle = GpmlFormat.createJdomElement(e, GpmlFormat.GPML);
				xml += xmlout.outputString(xmle) + "\n";
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		return xml;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { pathwayDataFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return pathwayDataFlavor.equals(flavor);
	}
}
