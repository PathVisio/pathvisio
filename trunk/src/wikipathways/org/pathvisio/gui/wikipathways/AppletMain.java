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

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.GuiInit;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.wikipathways.Parameter;
import org.pathvisio.wikipathways.UserInterfaceHandler;
import org.pathvisio.wikipathways.WikiPathways;

public class AppletMain extends JApplet {	
	private static final long serialVersionUID = 1L;

	private WikiPathways wiki;
	private MainPanel mainPanel;
	private UserInterfaceHandler uiHandler;
	
	public static final String PAR_PATHWAY_URL = "pathway.url";
	public void init() {
		SwingEngine.setCurrent(new SwingEngine());
		Engine.setCurrent(new Engine());
		
		System.out.println("INIT CALLED....");
		Logger.log.trace("INIT CALLED....");
				
		uiHandler = new AppletUserInterfaceHandler(this);
		wiki = new WikiPathways(uiHandler);
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					mainPanel = SwingEngine.getCurrent().getApplicationPanel();
					wiki.prepareMainPanel(mainPanel);
					
					getContentPane().add(mainPanel);
					mainPanel.setVisible(true);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final RunnableWithProgress r = new RunnableWithProgress() {
			public Object excecuteCode() {				
				GuiInit.init();
												
				parseArguments();
								
				try {
					wiki.init(SwingEngine.getCurrent().createWrapper(), 
							getProgressKeeper(), getDocumentBase());
				} catch(Exception e) {
					Logger.log.error("Error while starting editor", e);
					JOptionPane.showMessageDialog(
							AppletMain.this, e.getClass() + ": " + e.getMessage(), "Error while initializing editor", JOptionPane.ERROR_MESSAGE);
				};
				return null;
			}
		};
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					int spPercent = GlobalPreference.getValueInt(GlobalPreference.GUI_SIDEPANEL_SIZE);
					double spSize = (100 - spPercent) / 100.0;
					mainPanel.getSplitPane().setDividerLocation(spSize);
					
					uiHandler.runWithProgress(r, "Starting editor", ProgressKeeper.PROGRESS_UNKNOWN, false, true);
				}
			});
		} catch (Exception e) {
			Logger.log.error("Unable to start applet", e);
		}
		
		System.out.println("INIT ENDED....");
		Logger.log.trace("INIT ENDED....");
	}
		
	public void start() {
		System.out.println("START CALLED....");
		Logger.log.trace("START CALLED....");
						
		System.out.println("START ENDED....");
		Logger.log.trace("START ENDED....");
	}
	
	public void stop() {
		System.out.println("STOP CALLED....");
		Logger.log.trace("STOP CALLED....");
						
		System.out.println("STOP ENDED....");
		Logger.log.trace("STOP ENDED....");
	}

	public void destroy() {
		System.out.println("DESTROY CALLED....");
		Logger.log.trace("DESTROY CALLED....");
		ApplicationEvent e = new ApplicationEvent(this, ApplicationEvent.APPLICATION_CLOSE);
		Engine.getCurrent().fireApplicationEvent(e);
		if(e.doit) {
			super.destroy();
		}
		System.out.println("DESTROY ENDED....");
		Logger.log.trace("DESTROY ENDED....");
	}
	
	void parseArguments() {
		for(Parameter p : Parameter.values()) {
			p.setValue(getParameter(p.getName()));
		}
	}
}
