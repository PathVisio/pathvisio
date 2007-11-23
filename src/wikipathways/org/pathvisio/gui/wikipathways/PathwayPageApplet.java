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


import java.applet.Applet;
import java.awt.BorderLayout;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.GuiInit;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.util.ProgressKeeper.ProgressEvent;
import org.pathvisio.util.ProgressKeeper.ProgressListener;
import org.pathvisio.wikipathways.Parameter;
import org.pathvisio.wikipathways.UserInterfaceHandler;
import org.pathvisio.wikipathways.WikiPathways;

public class PathwayPageApplet extends JApplet {
	UserInterfaceHandler uiHandler;
	WikiPathways wiki;
	boolean isFirstApplet = true;
	boolean performedInit = false;
	
	public final void init() {
		//Check if other applets are present that already have an instance
		//of WikiPathways
		
		Logger.log.trace("INIT CALLED....");

		if(performedInit) return; //Don't process init twice!
		
		WikiPathways owiki = findExistingWikiPathways();
		if(owiki != null) {
			wiki = owiki;
			uiHandler = owiki.getUserInterfaceHandler();
			isFirstApplet = false;
		} else {
			uiHandler = new AppletUserInterfaceHandler(PathwayPageApplet.this);
			wiki = new WikiPathways(uiHandler);
		}
		
		if(isFirstApplet) {
			Engine engine = new Engine();
			Engine.setCurrent(engine);
			SwingEngine.setCurrent(new SwingEngine(engine));
			GuiInit.init();
		}
		
		parseArguments();

		//Init with progress monitor
		final RunnableWithProgress<Void> r = new RunnableWithProgress<Void>() {
			public Void excecuteCode() {
				try {
					doInitWiki(getProgressKeeper(), getDocumentBase());
				} catch(Exception e) {
					Logger.log.error("Error while starting applet", e);
					JOptionPane.showMessageDialog(
							PathwayPageApplet.this, e.getClass() + ": See error logg for details", "Error while initializing editor", JOptionPane.ERROR_MESSAGE);
				};
				doInit();
				getProgressKeeper().finished();
				return null;
			}
		};
		r.getProgressKeeper().addListener(new ProgressListener() {
			public void progressEvent(ProgressEvent e) {
				if(e.getType() == ProgressEvent.FINISHED) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							Logger.log.trace("Creating GUI");
							createToolbar();
							createGui();
							validate();
						}
					});
				}
			}
		});
		
		uiHandler.runWithProgress(r, "", ProgressKeeper.PROGRESS_UNKNOWN, false, true);
		performedInit = true;
	}
	
	protected final WikiPathways findExistingWikiPathways() {
		Logger.log.trace("Finding other pathway applets");
		Enumeration<Applet> applets = getAppletContext().getApplets();
		Logger.log.trace("Start iteration...");
		while(applets.hasMoreElements()) {
			Applet a = applets.nextElement();
			Logger.log.trace("Processing " + a);
			if(a instanceof PathwayPageApplet) {
				Logger.log.trace("Returning " + a);
				return ((PathwayPageApplet)a).wiki;
			}
		}
		Logger.log.trace("No other pathway applets found, returning null");
		return null; //Nothing found
	}
	
	/**
	 * In this method the WikiPathways class is initiated, 
	 * by calling {@link WikiPathways#init(ProgressKeeper, URL)}
	 * @see {@link WikiPathways#init(ProgressKeeper, URL)}
	 * @param pk
	 * @param base
	 * @throws Exception
	 */
	protected void doInitWiki(ProgressKeeper pk, URL base) throws Exception {
		Logger.log.trace("PathwayPageApplet:doInitWiki");
		if(isFirstApplet) {
			wiki.init(pk, base);
		} else {
			Logger.log.trace("Adding initVPathway");
			wiki.initVPathway();
		}
	}
	
	protected void doInit() {
		//May be implemented by subclasses
	}
	
	protected void createGui() {
		//May be implemented by subclasses
	}
		
	protected void createToolbar() {
		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
		toolbar.setFloatable(false);
		toolbar.add(new Actions.ExitAction(wiki.getUserInterfaceHandler(), wiki, true));
		toolbar.add(new Actions.ExitAction(wiki.getUserInterfaceHandler(), wiki, false));
		getContentPane().add(toolbar, BorderLayout.WEST);
	}
	
	void parseArguments() {
		for(Parameter p : Parameter.values()) {
			p.setValue(getParameter(p.getName()));
		}
	}
}
