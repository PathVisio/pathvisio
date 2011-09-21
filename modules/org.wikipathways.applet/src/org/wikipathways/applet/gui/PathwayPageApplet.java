// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.wikipathways.applet.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.pathvisio.core.Engine;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElementEvent;
import org.pathvisio.core.model.PathwayElementListener;
import org.pathvisio.core.model.StaticProperty;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.core.util.ProgressKeeper.ProgressEvent;
import org.pathvisio.core.util.ProgressKeeper.ProgressListener;
import org.pathvisio.gui.MainPanel;
import org.pathvisio.gui.SwingEngine;
import org.wikipathways.applet.Parameter;
import org.wikipathways.applet.RunnableWithProgress;
import org.wikipathways.applet.UserInterfaceHandler;
import org.wikipathways.applet.WikiPathways;

public class PathwayPageApplet extends JApplet {
	UserInterfaceHandler uiHandler;
	WikiPathways wiki;

	public final void init() {
		/* Do not force LAF, seems to give problems
  		try {
		    UIManager.setLookAndFeel(
		        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Logger.log.error("Unable to load native look and feel", ex);
		}
		*/

		//Add a mouse listener that requests focus on clicking
		//To fix bug 299
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				PathwayPageApplet.this.requestFocus();
				PathwayPageApplet.this.requestFocusInWindow();
			}
		});

		try {
			Logger.log.trace(this + ": INIT CALLED....");

			PreferenceManager.init();
			final Engine engine = new Engine();
			SwingEngine swingEngine = new SwingEngine(engine);

			uiHandler = new UserInterfaceHandler(PathwayPageApplet.this);
			wiki = new WikiPathways(uiHandler, swingEngine);

			parseArguments();

			//Init with progress monitor
			final RunnableWithProgress<Void> r = new RunnableWithProgress<Void>() {
				public Void excecuteCode() {
					try {
						doInitWiki(getProgressKeeper(), getDocumentBase());
					} catch(AccessControlException ae) {
							onError("You didn't accept the certificate needed to run this applet.\n" +
									"After restarting the browser, click the edit button and choose" +
									"\n'Run' in the security dialog that pops up.",
							"Security exception");
					} catch(Exception e) {
						Logger.log.error("Error while starting applet", e);
						String msg =  e.getClass() +
						"\n See error log (" + PreferenceManager.getCurrent().get(GlobalPreference.WP_FILE_LOG) + ") for details";
						JOptionPane.showMessageDialog(
								PathwayPageApplet.this, msg, "Error while initializing editor", JOptionPane.ERROR_MESSAGE);
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

			Logger.log.trace(this + ": Performing init in background");
			uiHandler.runWithProgress(r, "", false, true);
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
		if(wiki != null) {
			SaveReminder.stopSaveReminder(wiki);
			wiki.dispose();
			wiki = null;
		}
		uiHandler = null;
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
		wiki.init(pk, base);
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

	private JFrame fullScreenFrame;

	public boolean isFullScreen() {
		return fullScreenFrame != null;
	}

	/**
	 * Makes the applet go to fullscreen mode.
	 * Creates a new frame and transfers the mainPanel from
	 * the applet to the frame.
	 * @see #toEmbedded(boolean)
	 */
	protected void toFullScreen() {
		final MainPanel mainPanel = wiki.getMainPanel();
		fullScreenFrame = new JFrame();

		PathwayElement mappInfo = wiki.getPathway().getMappInfo();

		fullScreenFrame.setTitle("WikiPathways editor - " + mappInfo.getMapInfoName());

		wiki.getPathway().getMappInfo().addListener(new PathwayElementListener() {
			public void gmmlObjectModified(PathwayElementEvent e) {
				if (e.affectsProperty(StaticProperty.MAPINFONAME)) {
					fullScreenFrame.setTitle("WikiPathways editor - " +
							e.getModifiedPathwayElement().getMapInfoName());
				}
			}
		});
		getContentPane().repaint();

		fullScreenFrame.getContentPane().add(mainPanel);

		fullScreenFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(wiki.exit(false, null)) {
					fullScreenFrame.dispose();
				}
			}
		});

		fullScreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		fullScreenFrame.setVisible(true);
		fullScreenFrame.setSize(800, 600);
		fullScreenFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		fullScreenFrame.validate();
	}


	/**
	 * Closes the fullscreen mode and returns the applet
	 * to embedded mode
	 * @see PathwayPageApplet#toFullScreen()
	 */
	public void toEmbedded() {
		MainPanel mainPanel = wiki.getMainPanel();

		fullScreenFrame.getContentPane().remove(mainPanel);
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		fullScreenFrame.setVisible(false);
		fullScreenFrame.dispose();
		fullScreenFrame = null;

		validate();
		repaint();
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
		Parameter parameters = wiki.getParameters();
		for(String name : parameters.getNames()) {
			parameters.setValue(name, getParameter(name));
		}
	}

	public String toString() {
		return this.getClass() + ": " + getName() + hashCode();
	}
}
