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
package org.pathvisio.gui.swing;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayWrapper;
import org.pathvisio.view.swing.VPathwaySwing;

public class SwingEngine {	
	private MainPanel mainPanel;
	
	private static SwingEngine current;
	public static SwingEngine getCurrent() {
		if(current == null) current = new SwingEngine();
		return current;
	}
	
	public static void setCurrent(SwingEngine engine) {
		current = engine;
	}
	
	public MainPanel getApplicationPanel() {
		return getApplicationPanel(false);
	}
	
	public MainPanel getApplicationPanel(boolean forceNew) {
		if(forceNew || !hasApplicationPanel()) {
			mainPanel = new MainPanel();
		}
		return mainPanel;
	}
	
	public boolean hasApplicationPanel() {
		return mainPanel != null;
	}
	
	public static String MSG_UNABLE_IMPORT = "Unable to import GPML file.";
	public static String MSG_UNABLE_EXPORT = "Unable to export GPML file.";
	public static String MSG_UNABLE_SAVE = "Unable to save GPML file.";
	public static String MSG_UNABLE_OPEN = "Unable to open GPML file.";
	
	public void handleConverterException(String message, Component c, ConverterException e) {
		if (e.getMessage().contains("Cannot find the declaration of element 'Pathway'"))
		{
			JOptionPane.showMessageDialog(c,
					message + "\n\n" +
					"The most likely cause for this error is that you are trying to open an old Gpml file. " +
					"Please note that the Gpml format has changed as of March 2007. " +
					"The standard pathway set can be re-downloaded from http://pathvisio.org " +
					"Non-standard pathways need to be recreated or upgraded. " +
					"Please contact the authors at " + Globals.DEVELOPER_EMAIL + " if you need help with this.\n" +
					"\nSee error log for details", "Error", JOptionPane.ERROR_MESSAGE);
			Logger.log.error("Unable to open Gpml file", e);
		}
		else
		{
			JOptionPane.showMessageDialog(c,
					message + "\n" + e.getClass() + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			Logger.log.error("Converter exception", e);
		}
	}
		
	public VPathwayWrapper createWrapper() {
		 return new VPathwaySwing(getApplicationPanel().getScrollPane());
	}
	
	public void openPathway(URL url) {
		try {
			Engine.getCurrent().openPathway(url, createWrapper());
		} catch(ConverterException e) {
			handleConverterException(e.getMessage(), null, e);
		}
	}
	
	public void importPathway(File f) {
		try {
			Engine.getCurrent().importPathway(f, createWrapper());
		} catch(ConverterException e) {
			handleConverterException(e.getMessage(), null, e);
		}
	}
	
	public void newPathway() {
		Engine.getCurrent().newPathway(createWrapper());
	}

	public boolean mayOverwrite(File f) {
		boolean allow = true;
		if(f.exists()) {
			int status = JOptionPane.showConfirmDialog(null, "File " + f.getName() + " already exists, overwrite?", 
					"File already exists", JOptionPane.YES_NO_OPTION);
			allow = status == JOptionPane.YES_OPTION;
		}
		return allow;
	}
	
	public boolean savePathwayAs() {
		//Open file dialog
		JFileChooser jfc = new JFileChooser();
		jfc.setAcceptAllFileFilterUsed(true);
		jfc.setDialogTitle("Save pathway");
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		jfc.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String ext = f.toString().substring(f.toString().length() - 4);
				if(ext.equalsIgnoreCase("xml") || ext.equalsIgnoreCase("gpml")) {
					return true;
				}
				return false;
			}
			public String getDescription() {
				return "GPML files (*.gpml, *.xml)";
			}
			
		});
		int status = jfc.showDialog(null, "Save");
		if(status == JFileChooser.APPROVE_OPTION) {
			File toFile = jfc.getSelectedFile();
			try {
				if(mayOverwrite(toFile)) {
					Engine.getCurrent().savePathway(toFile);
					return true;
				}
			} catch(ConverterException e) {
				handleConverterException(e.getMessage(), null, e);
			}
		}
		return false;
	}
	
	public boolean savePathway()
	{
		Pathway pathway = Engine.getCurrent().getActivePathway();
		
		boolean result = true;	
		
        // Overwrite the existing xml file.
		// If the target file is read-only, let the user select a new pathway
		if (pathway.getSourceFile() != null && pathway.getSourceFile().canWrite())
		{
			try {
				Engine.getCurrent().savePathway(pathway.getSourceFile());
			} catch (ConverterException e) {
				handleConverterException(e.getMessage(), null, e);
			}
		}
		else {
			result = savePathwayAs();
		}

		return result;
	}
}
