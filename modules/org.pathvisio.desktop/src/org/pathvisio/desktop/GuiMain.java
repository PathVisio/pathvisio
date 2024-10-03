/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.desktop;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.bridgedb.IDMapperException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Globals;
import org.pathvisio.core.data.GdbEvent;
import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.data.GdbManager.GdbEventListener;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.DataNodeListExporter;
import org.pathvisio.core.model.EUGeneExporter;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.ImageExporter;
import org.pathvisio.core.model.MappFormat;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.Resources;
import org.pathvisio.data.DataException;
import org.pathvisio.data.DataInterface;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.GexManager.GexManagerEvent;
import org.pathvisio.desktop.gex.GexManager.GexManagerListener;
import org.pathvisio.desktop.model.BatikImageWithDataExporter;
import org.pathvisio.desktop.model.RasterImageWithDataExporter;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.gui.MainPanel;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.SwingEngine.Browser;

/**
 * Main class for the Swing GUI. This class creates and shows the GUI.
 * Subclasses may override {@link #createAndShowGUI(MainPanelStandalone, SwingEngine)} to perform custom
 * actions before showing the GUI.
 * 
 * @author thomas
 * @author anwesha
 *
 */
public class GuiMain implements GdbEventListener, GexManagerListener
{
	GuiMain() { 
		
	}

	private MainPanelStandalone mainPanel;

	private PvDesktop pvDesktop;
	private SwingEngine swingEngine;
	public AutoSave auto;  // needs to be here for the same timer to be available always

	private static void initLog(Engine engine)
	{
		String logDest = PreferenceManager.getCurrent().get(GlobalPreference.FILE_LOG);
		Logger.log.setDest (logDest);
		Logger.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level
		Logger.log.info("Application name: " + engine.getApplicationName());
		Logger.log.info("os.name: " + System.getProperty("os.name") +
					" os.version: " + System.getProperty("os.version") +
					" java.version: " + System.getProperty ("java.version"));
		Logger.log.info ("Locale: " + Locale.getDefault().getDisplayName());
	}
	
	private void openPathwayFile(String pathwayFile) {
		File f = new File(pathwayFile);
		URL url;
		//Assume the argument is a file
		if(f.exists()) {
			swingEngine.openPathway(f);
		} else {
			//If it doesn't exist, assume it's an url
			try {
				url = new URL(pathwayFile);
				swingEngine.openPathway(url);
			} catch(MalformedURLException e) {
				Logger.log.error("Couldn't open pathway url " + pathwayFile);
			}
		}
	}

	// this is only a workaround to hand over the pathway and pgex file
	// from the command line when using the launcher
	// TODO: find better solution
	public static final String ARG_PROPERTY_PGEX = "pathvisio.pgex";
	public static final String ARG_PROPERTY_PATHWAYFILE = "pathvisio.pathwayfile";
	/**
	 * Act upon the command line arguments
	 */
	public void processOptions() {
		//Create a plugin manager that loads the plugins
		pvDesktop.loadPluginManager();
//		pvDesktop.initPlugins();

		String str = System.getProperty(ARG_PROPERTY_PATHWAYFILE);
		if (str != null) {
			openPathwayFile(str);
		}
		
		str = System.getProperty(ARG_PROPERTY_PGEX);
		if(str != null) {
			try {
				pvDesktop.getGexManager().setCurrentGex(str, false);
				pvDesktop.loadGexCache();
				Logger.log.info ("Loaded pgex " + str);
			} 
			catch (DataException e) {
				Logger.log.error ("Couldn't open pgex " + str, e);
			}
		}
	}

	private String shortenString(String s) {
		return shortenString(s, 20);
	}

	private String shortenString(String s, int maxLength) {
		String prefix = "...";
		if(s.length() > maxLength + prefix.length()) {
			s = s.substring(s.length() - maxLength - prefix.length());
			s = prefix + s;
		}
		return s;
	}
	
	/** The statements below check for local identifier mapping databases,
	*	and change the output line in the bottom of PV to the correct name(s).
	*   In the future, this could be extended with different mapping database.
	*   Please check which combination can occur for correct output (when adding more mapping databases).
	*/	

	private void setGdbStatus(JLabel allLabel) {
		String gdb = "" + swingEngine.getGdbManager().getGeneDb();
		String mdb = "" + swingEngine.getGdbManager().getMetaboliteDb();
		String idb = "" + swingEngine.getGdbManager().getInteractionDb();
		
		if (swingEngine.getGdbManager().getGeneDb() == null && swingEngine.getGdbManager().getMetaboliteDb() == null && swingEngine.getGdbManager().getInteractionDb() == null) {
			allLabel.setText(" | Local mapping databases loaded: " + "None.");
		}
		else if (swingEngine.getGdbManager().getGeneDb() != null && swingEngine.getGdbManager().getMetaboliteDb() == null && swingEngine.getGdbManager().getInteractionDb() == null) {
			allLabel.setText(" | Local mapping databases loaded: " + "GeneProtein: " + shortenString(gdb));
		}
		else if (swingEngine.getGdbManager().getGeneDb() == null && swingEngine.getGdbManager().getMetaboliteDb() != null && swingEngine.getGdbManager().getInteractionDb() == null) {
			allLabel.setText(" | Local mapping databases loaded: " + "Metabolite: " + shortenString(mdb));
		}
		else if (swingEngine.getGdbManager().getGeneDb() == null && swingEngine.getGdbManager().getMetaboliteDb() == null && swingEngine.getGdbManager().getInteractionDb() != null) {
			allLabel.setText(" | Local mapping databases loaded: " + "Interactions: " + shortenString(idb));
		}
		else if (swingEngine.getGdbManager().getGeneDb() != null && swingEngine.getGdbManager().getMetaboliteDb() != null && swingEngine.getGdbManager().getInteractionDb() == null) {
			allLabel.setText(" | Local mapping databases loaded: " + "GeneProtein: " + shortenString(gdb) + " | " + "Metabolite: " + shortenString(mdb));
		}
		else if (swingEngine.getGdbManager().getGeneDb() == null && swingEngine.getGdbManager().getMetaboliteDb() != null && swingEngine.getGdbManager().getInteractionDb() != null) {
			allLabel.setText(" | Local mapping databases loaded: " + "Metabolite: " + shortenString(mdb) + " | " + "Interactions: " + shortenString(idb));
		}
		else if (swingEngine.getGdbManager().getGeneDb() != null && swingEngine.getGdbManager().getMetaboliteDb() == null && swingEngine.getGdbManager().getInteractionDb() != null) {
			allLabel.setText(" | Local mapping databases loaded: " + "GeneProtein: " + shortenString(gdb) + " | " + "Interactions: " + shortenString(idb));
		}
		else {
		     allLabel.setText(" | Local mapping databases loaded: " + "GeneProtein: " + shortenString(gdb) + " | " + "Metabolite: " + shortenString(mdb) + " | " + "Interactions: " + shortenString(idb));
		}
		
		allLabel.setToolTipText("Local BridgeDb mapping databases to support identifier mapping");
	}		
		
	public void gdbEvent(GdbEvent e) {
		setGdbStatus(allLabel);
	}

	public void gexManagerEvent(GexManagerEvent e)
	{
		if(e.getType() == GexManagerEvent.CONNECTION_OPENED ||
				e.getType() == GexManagerEvent.CONNECTION_CLOSED)
		{
			DataInterface gex = pvDesktop.getGexManager().getCurrentGex();
			if(gex != null && gex.isConnected()) {
				gexLabel.setText(" | Dataset: " + shortenString(gex.getDbName()));
				gexLabel.setToolTipText(gex.getDbName());
			} else {
				gexLabel.setText("");
				gexLabel.setToolTipText("");
			}
		}
	}

	private JLabel allLabel;
/*	private JLabel gdbLabel;
	private JLabel mdbLabel;
	private JLabel idbLabel;*/
	private JLabel gexLabel;

	/**
	 * Creates and shows the GUI. Creates and shows the Frame, sets the size, title and menubar.
	 * @param mainPanel The main panel to show in the frame
	 */
	protected JFrame createAndShowGUI(final MainPanelStandalone mainPanel, final SwingEngine swingEngine)
	{
		//Create and set up the window.
		final JFrame frame = new JFrame(Globals.APPLICATION_NAME);
		// dispose on close, otherwise windowClosed event is not called.
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		URL url = Resources.getResourceURL("bigcateye.gif");
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(url));

		frame.add(mainPanel, BorderLayout.CENTER);

		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		frame.add(statusBar, BorderLayout.SOUTH);

		allLabel = new JLabel();
/*		gdbLabel = new JLabel();
		mdbLabel = new JLabel();
		idbLabel = new JLabel();*/
		gexLabel = new JLabel();

		statusBar.add(allLabel);
/*		statusBar.add(gdbLabel);
		statusBar.add(mdbLabel);
		statusBar.add(idbLabel);*/
		statusBar.add(gexLabel);
		//setGdbStatus(gdbLabel, mdbLabel, idbLabel);
		setGdbStatus(allLabel);

		swingEngine.getGdbManager().addGdbEventListener(this);

		pvDesktop.getGexManager().addListener(this);

		frame.setJMenuBar(mainPanel.getMenuBar());
		frame.pack();
		PreferenceManager preferences = PreferenceManager.getCurrent();
		frame.setSize(preferences.getInt(GlobalPreference.WIN_W), preferences.getInt(GlobalPreference.WIN_H));
		int x = preferences.getInt(GlobalPreference.WIN_X);
		int y = preferences.getInt(GlobalPreference.WIN_Y);
		if(x >= 0 && y >= 0) frame.setLocation(x, y);

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent we)
			{

				PreferenceManager prefs = PreferenceManager.getCurrent();
				JFrame frame = swingEngine.getFrame();
				Dimension size = frame.getSize();
				Point p = frame.getLocationOnScreen();
				prefs.setInt(GlobalPreference.WIN_W, size.width);
				prefs.setInt(GlobalPreference.WIN_H, size.height);
				prefs.setInt(GlobalPreference.WIN_X, p.x);
				prefs.setInt(GlobalPreference.WIN_Y, p.y);

				if(swingEngine.canDiscardPathway()) {
					frame.dispose();
				}
			}

			@Override
			public void windowClosed(WindowEvent we)
			{
				GuiMain.this.shutdown(swingEngine);
			
				// stops all bundles, exceptions are no real problem
				// because System.exit(0) should stop everything anyway
				for(Bundle bundle : pvDesktop.getContext().getBundles()) {
					if(bundle.getState() == Bundle.ACTIVE) {
						try {
							bundle.stop();
						} catch (BundleException e) {}
					}
				}
				// added system exit, so the application closes after the window is closed
				System.exit(0);
			}
		});

		//Display the window.
		frame.setVisible(true);

		int spPercent = PreferenceManager.getCurrent().getInt (GlobalPreference.GUI_SIDEPANEL_SIZE);
		double spSize = (100 - spPercent) / 100.0;
		mainPanel.getSplitPane().setDividerLocation(spSize);

		return frame;
	}

	private void shutdown(SwingEngine swingEngine)
	{
		PreferenceManager prefs = PreferenceManager.getCurrent();
		prefs.store();

		//explicit clean shutdown of gdb prevents file from being left open
		if (swingEngine.getGdbManager().isConnected())
		{
			try
			{
				swingEngine.getGdbManager().getCurrentGdb().close();
			}
			catch (IDMapperException ex)
			{
				Logger.log.error ("Couldn't cleanly close pgdb database", ex);
			}
		}
		swingEngine.getGdbManager().removeGdbEventListener(this);
		mainPanel.dispose();
		pvDesktop.getGexManager().removeListener(this);
		pvDesktop.dispose();
		swingEngine.getEngine().dispose();
		swingEngine.dispose();
		Logger.log.info ("PathVisio was shut down cleanly");
		
		// stop the timer and clean out the files on a successful shutdown
		auto.stopTimer();
	}

	public MainPanel getMainPanel() { return mainPanel; }

	public void init(PvDesktop pvDesktop) {
		this.pvDesktop = pvDesktop;
		
		Engine engine = pvDesktop.getSwingEngine().getEngine();
		initLog(engine);
		engine.setApplicationName("PathVisio " + Engine.getVersion());
		if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.USE_SYSTEM_LOOK_AND_FEEL))
		{
			try {
			    UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				Logger.log.error("Unable to load native look and feel", ex);
			}
		}

		swingEngine = pvDesktop.getSwingEngine();
		swingEngine.setUrlBrowser(new Browser() {
			public void openUrl(URL url) {
				try {
					if(Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(url.toURI());
					} else {
						new JOptionPane("Could not open default browser.\n Please go to\n" + url + "\nin your browser.", JOptionPane.WARNING_MESSAGE);
					}
				} catch (Exception ex) {
					Logger.log.error ("Couldn't open url '" + url + "'", ex);
				}
			}
		});

		swingEngine.getGdbManager().initPreferred();

		mainPanel = new MainPanelStandalone(pvDesktop);
		mainPanel.createAndShowGUI();

		JFrame frame = createAndShowGUI(mainPanel, swingEngine);
		initImporters(engine);
		initExporters(engine, swingEngine.getGdbManager());
		swingEngine.setFrame(frame);
		swingEngine.setApplicationPanel(mainPanel);
		
		// start the autosave timer
		auto = new AutoSave(swingEngine);
		auto.startTimer(300);

		processOptions();
	}

	
	private void initImporters(Engine engine)
	{
		engine.addPathwayImporter(new MappFormat());
		engine.addPathwayImporter(new GpmlFormat());
	}

	private void initExporters(Engine engine, GdbManager gdbManager)
	{
		engine.addPathwayExporter(new MappFormat());
		engine.addPathwayExporter(new GpmlFormat());

		GexManager gex = pvDesktop.getGexManager();
		VisualizationManager vis = pvDesktop.getVisualizationManager();
		engine.addPathwayExporter(new RasterImageWithDataExporter(ImageExporter.TYPE_PNG, gex, vis));
		engine.addPathwayExporter(new BatikImageWithDataExporter(ImageExporter.TYPE_SVG, gex, vis));
		engine.addPathwayExporter(new BatikImageWithDataExporter(ImageExporter.TYPE_TIFF, gex, vis));
		engine.addPathwayExporter(new BatikImageWithDataExporter(ImageExporter.TYPE_PDF, gex, vis));
		engine.addPathwayExporter(new DataNodeListExporter(gdbManager));
		engine.addPathwayExporter(new EUGeneExporter());
	}

}
