package org.pathvisio.gui.swing.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.swing.PathwayTransferable;
import org.pathvisio.view.swing.VPathwaySwing;

public class PathwayImportHandler extends TransferHandler {
	public static final DataFlavor urlFlavor = new DataFlavor(String.class, "text/uri");
	
	Set<DataFlavor> supportedFlavors;
	Pathway pathway;
	
	public PathwayImportHandler(Pathway p) {
		pathway = p;
		supportedFlavors = new HashSet<DataFlavor>();
		supportedFlavors.add(PathwayTransferable.gpmlDataFlavor);
		supportedFlavors.add(urlFlavor);
	}
	
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for(DataFlavor d : transferFlavors) {
			if(supportedFlavors.contains(d)) return true;
		}
		return false;
	}
	
	public boolean importData(JComponent comp, Transferable t) {
		DataFlavor preferred = null;
		try {
			for(DataFlavor df : t.getTransferDataFlavors()) {
				if(PathwayTransferable.gpmlDataFlavor.equals(df)) {
					preferred = df;
					break;
				}
				//Only choose this if there is no better option
				if(urlFlavor.equals(df)) {
					preferred = df; //Continue to check for better option
				}
			}
			if(preferred.equals(PathwayTransferable.gpmlDataFlavor)) {
				return importGpml(comp, t);
			} else if(preferred.equals(urlFlavor)) {
				return importUrl(comp, t);
			}
		} catch(Exception e) {
			Logger.log.error("Unable to paste pathway data", e);
		}
		return false;
	}

	private boolean importGpml(JComponent comp, Transferable t) throws UnsupportedFlavorException, IOException, ConverterException {
		Pathway pnew = new Pathway();
		String xml = (String)t.getTransferData(PathwayTransferable.gpmlDataFlavor);
		GpmlFormat.readFromXml(pnew, new StringReader(xml), true);
		
		List<PathwayElement> elements = new ArrayList<PathwayElement>();
		for(PathwayElement elm : pnew.getDataObjects()) {
			if(elm.getObjectType() != ObjectType.MAPPINFO) {
				elements.add(elm);
			}
		}
		((VPathwaySwing)comp).getChild().paste(elements);
		return false;
	}

	private boolean importUrl(JComponent comp, Transferable t) throws MalformedURLException, UnsupportedFlavorException, IOException {
			URL url = new URL((String)t.getTransferData(urlFlavor));
			SwingEngine.getCurrent().openPathway(url);
			return true;
	}
}
