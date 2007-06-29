package org.pathvisio.gui.swing;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.pathvisio.Engine;
import org.pathvisio.model.ConverterException;

public class SwingEngine {
	private static MainPanel mainPanel;
	
	public static MainPanel getApplicationPanel() {
		if(mainPanel == null) mainPanel = new MainPanel();
		return mainPanel;
	}
	
	public static String MSG_UNABLE_IMPORT = "Unable to import GPML file.";
	public static String MSG_UNABLE_EXPORT = "Unable to export GPML file.";
	public static String MSG_UNABLE_SAVE = "Unable to save GPML file.";
	public static String MSG_UNABLE_OPEN = "Unable to open GPML file.";
	
	public static void handleConverterException(String message, Component c, ConverterException e) {
		if (e.getMessage().contains("Cannot find the declaration of element 'Pathway'"))
		{
			JOptionPane.showMessageDialog(c,
					message + "\n\n" +
					"The most likely cause for this error is that you are trying to open an old Gpml file. " +
					"Please note that the Gpml format has changed as of March 2007. " +
					"The standard pathway set can be re-downloaded from http://pathvisio.org " +
					"Non-standard pathways need to be recreated or upgraded. " +
					"Please contact the authors at martijn.vaniersel@bigcat.unimaas.nl if you need help with this.\n" +
					"\nSee error log for details");
			Engine.log.error("Unable to open Gpml file", e);
		}
		else
		{
			JOptionPane.showMessageDialog(c,
					message + "\n" + e.getClass() + e.getMessage());
			Engine.log.error("Unable to open Gpml file", e);
		}
	}
}
