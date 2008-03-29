//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 

//http://www.apache.org/licenses/LICENSE-2.0 

//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.

package org.pathvisio.gui.wikipathways;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.security.AccessControlException;
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

	public final void init() {
		//Add a mouse listener that requests focus on clicking
		//To fix bug 299
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				PathwayPageApplet.this.requestFocus();
				PathwayPageApplet.this.requestFocusInWindow();
			}
		});
		
		//Check if other applets are present that already have an instance
		//of WikiPathways
		try {
			Logger.log.trace(this + ": INIT CALLED....");

			//Check if there are other applets around
			WikiPathways owiki = findExistingWikiPathways();
			if(owiki != null) {
				wiki = owiki;
				uiHandler = owiki.getUserInterfaceHandler();
				isFirstApplet = false;
			} else {
				uiHandler = new AppletUserInterfaceHandler(PathwayPageApplet.this);
				wiki = new WikiPathways(uiHandler);
			}

			//Onlyl set new engine if this is the first applet
			if(isFirstApplet) {
				Engine engine = new Engine();
				Engine.setCurrent(engine);
				SwingEngine.setCurrent(new SwingEngine(engine));
			}

			parseArguments();

			//Init with progress monitor
			final RunnableWithProgress<Void> r = new RunnableWithProgress<Void>() {
				public Void excecuteCode() {
					try {
						doInitWiki(getProgressKeeper(), getDocumentBase());
					} catch(AccessControlException ae) {
						if(isFirstApplet) {
							onError("You didn't accept the certificate needed to run this applet.\n" +
									"After restarting the browser, click the edit button and choose" +
									"\n'Run' in the security dialog that pops up.", 
							"Security exception");
						}
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
			//Perform some actions after the runnable is done
			r.getProgressKeeper().addListener(new ProgressListener() {
				public void progressEvent(ProgressEvent e) {
					if(e.getType() == ProgressEvent.FINISHED) {
						afterInit();
					}
				}
			});

			if(wiki.initPerformed()) {
				Logger.log.trace(this + ": Init already performed");
				while(wiki.isInit()) {
					try {
						Thread.sleep(10);
					} catch(InterruptedException e) {
						//ignore
					}
				}
				afterInit();
			} else {
				Logger.log.trace(this + ": Performing init in background");
				uiHandler.runWithProgress(r, "", ProgressKeeper.PROGRESS_UNKNOWN, false, true);			
			}
		} catch(Exception e) {
			onError("Error: " + e.getClass() + ": " + e.getMessage(), "Error");
		}
	}

	public void stop() {
		Logger.log.trace("Applet.stop called, stopping save reminder");
		if(wiki != null) SaveReminder.stopSaveReminder(wiki);
	}
	
	public void start() {
		Logger.log.trace("Applet.start called, starting save reminder");
		if(wiki != null) wiki.startSaveReminder();
	}
	
	public void destroy() {
		Logger.log.trace("Applet.destroy called, stopping save reminder");
		if(wiki != null) SaveReminder.stopSaveReminder(wiki);
	}
	
	private void onError(String msg, String title) {
		JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
		getAppletContext().showDocument(getDocumentBase(), "_self");
	}

	private void afterInit() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Logger.log.trace(this + ": Creating GUI");
				if(!this.getClass().equals(PathwayPageApplet.class)) {
					createToolbar();
					createGui();
					validate();
				}
			}
		});
	}

	protected final WikiPathways findExistingWikiPathways() {
		Logger.log.trace(this + ": Finding other pathway applets");
		Enumeration<Applet> applets = getAppletContext().getApplets();
		while(applets.hasMoreElements()) {
			Applet a = applets.nextElement();
			if(a instanceof PathwayPageApplet) {
				Logger.log.trace(this + ":Returning " + a);
				return ((PathwayPageApplet)a).wiki;
			}
		}
		Logger.log.trace(this + ": No other pathway applets found, returning null");
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
		Logger.log.trace(this + ":doInitWiki");
		if(isFirstApplet) {
			wiki.init(pk, base);
		} else {
			Logger.log.trace(this + ": calling initVPathway");
			wiki.initVPathway();
		}
	}

	public boolean mayExit() {
		return wiki != null ? wiki.mayExit() : true;
	}
	
	protected void doInit() {
		//May be implemented by subclasses
	}

	protected void createGui() {
		//May be implemented by subclasses
	}

	/**
	 * Get the default description that will be used
	 * when the changes are saved to the server
	 * If the return value is null, the user will be prompted
	 * for a description
	 */
	protected String getDefaultDescription() {
		return null;
	}
	
	protected void createToolbar() {
		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
		toolbar.setFloatable(false);
		toolbar.add(new Actions.ExitAction(wiki.getUserInterfaceHandler(), wiki, true, getDefaultDescription()));
		toolbar.add(new Actions.ExitAction(wiki.getUserInterfaceHandler(), wiki, false, getDefaultDescription()));
		getContentPane().add(toolbar, BorderLayout.WEST);
	}

	void parseArguments() {
		for(Parameter p : Parameter.values()) {
			p.setValue(getParameter(p.getName()));
		}
	}

	public String toString() {
		return this.getClass() + ": " + getName() + hashCode();
	}
}
