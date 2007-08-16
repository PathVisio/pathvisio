package org.pathvisio.gui.swing.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.SwingEngine;

public class FileImportHandler extends TransferHandler {
	DataFlavor urlFlavor;
	
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for(DataFlavor df : transferFlavors) {
			if(df.getRepresentationClass().equals(String.class)) {
				if(df.getMimeType().contains("text/uri")) {
					urlFlavor = df;
					return true;					
				}
			}
		}
		return false;
	}
	
	public boolean importData(JComponent comp, Transferable t) {
		try {
			URL url = new URL((String)t.getTransferData(urlFlavor));
			System.out.println(url);
			SwingEngine.getCurrent().openPathway(url);
			return true;
		} catch (Exception e) {
			Logger.log.error("Unable to process drop", e);
		}
		return false;
	}
}
