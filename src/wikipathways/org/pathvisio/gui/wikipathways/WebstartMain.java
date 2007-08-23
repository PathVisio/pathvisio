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
package org.pathvisio.gui.wikipathways;

import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.GuiMain;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.wikipathways.Parameter;
import org.pathvisio.wikipathways.UserInterfaceHandler;
import org.pathvisio.wikipathways.WikiPathways;

public class WebstartMain extends GuiMain {
	WikiPathways wiki;
	UserInterfaceHandler uiHandler;
	
	protected void createAndShowGUI() {
		Engine engine = new Engine();
		Engine.setCurrent(engine);
		SwingEngine.setCurrent(new SwingEngine(engine));
		
		super.createAndShowGUI();
		
		initWiki();
	}

	private void initWiki() {
		uiHandler = new WebstartUserInterfaceHandler(getFrame());
		wiki = new WikiPathways(uiHandler);
		wiki.prepareMainPanel(getMainPanel());

				
		final RunnableWithProgress r = new RunnableWithProgress() {
			public Object excecuteCode() {
				parseCommandLine(getArgs());
								
				try {
					wiki.init(SwingEngine.getCurrent().createWrapper(), 
							getProgressKeeper(), new URL("http://www.wikipathways.org"));
				} catch(Exception e) {
					Logger.log.error("Error while starting editor", e);
					JOptionPane.showMessageDialog(
							getMainPanel(), e.getClass() + ": " + e.getMessage(), "Error while initializing editor", JOptionPane.ERROR_MESSAGE);
				};
				return null;
			}
		};
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {					
					uiHandler.runWithProgress(r, "", ProgressKeeper.PROGRESS_UNKNOWN, false, true);
				}
			});
		} catch (Exception e) {
			Logger.log.error("Unable to start editor", e);
		}
	}
		
	void parseCommandLine(String[] args) {
		for(int i = 0; i < args.length - 1; i++) {
			//Check for parameters
			String a = args[i];
			if(a.startsWith("-")) {
				if	(a.equalsIgnoreCase("-pwName")) {
					Logger.log.trace("Parsed -pwName argument" + args[i+1]);
					Parameter.PW_NAME.setValue(args[i+1]);
				}
				else if	(a.equalsIgnoreCase("-pwUrl")) {
					Logger.log.trace("Parsed -pwUrl argument" + args[i+1]);
					Parameter.PW_URL.setValue(args[i+1]);
				}
				else if	(a.equalsIgnoreCase("-rpcUrl")) {
					Logger.log.trace("Parsed -rpcUrl argument" + args[i+1]);
					Parameter.RPC_URL.setValue(args[i+1]);
				}
				else if (a.equalsIgnoreCase("-pwSpecies")) {
					Logger.log.trace("Parsed -pwSpecies argument" + args[i+1]);
					Parameter.PW_SPECIES.setValue(args[i+1]);
				}
				else if (a.equalsIgnoreCase("-user")) {
					Logger.log.trace("Parsed -user argument" + args[i+1]);
					Parameter.USER.setValue(args[i+1]);
				}
				else if (a.equalsIgnoreCase("-new")) {
					Logger.log.trace("Parsed -new flag");
					String value = args[i+1];
					if(value.equalsIgnoreCase("true") || value.equals("1")) {
						Parameter.PW_NEW.setValue(Boolean.toString(true));
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		final WebstartMain gui = new WebstartMain();
		gui.setArgs(args);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.createAndShowGUI();
			}
		});
	}
}