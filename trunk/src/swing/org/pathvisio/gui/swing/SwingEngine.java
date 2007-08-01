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

import javax.swing.JOptionPane;

import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
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
					"\nSee error log for details");
			Logger.log.error("Unable to open Gpml file", e);
		}
		else
		{
			JOptionPane.showMessageDialog(c,
					message + "\n" + e.getClass() + e.getMessage());
			Logger.log.error("Unable to open Gpml file", e);
		}
	}
		
	public VPathwayWrapper createWrapper() {
		 return new VPathwaySwing(getApplicationPanel().getScrollPane());
	}
	
	public File openPathway(URL url) throws ConverterException {
		return Engine.getCurrent().openPathway(url, createWrapper());
	}
	
	public void importPathway(File f) throws ConverterException {
		Engine.getCurrent().importPathway(f, createWrapper());
	}
	
	public void newPathway() {
		Engine.getCurrent().newPathway(createWrapper());
	}
}
