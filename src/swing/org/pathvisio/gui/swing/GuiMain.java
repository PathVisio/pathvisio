// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import edu.stanford.ejalbert.BrowserLauncher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.bridgedb.IDMapperException;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.Revision;
import org.pathvisio.data.GdbEvent;
import org.pathvisio.data.GdbManager;
import org.pathvisio.data.GdbManager.GdbEventListener;
import org.pathvisio.debug.Logger;
import org.pathvisio.gex.GexManager.GexManagerEvent;
import org.pathvisio.gex.GexManager.GexManagerListener;
import org.pathvisio.gex.CachedData;
import org.pathvisio.gex.GexManager;
import org.pathvisio.gex.SimpleGex;
import org.pathvisio.gui.swing.SwingEngine.Browser;
import org.pathvisio.model.BatikImageWithDataExporter;
import org.pathvisio.model.DataNodeListExporter;
import org.pathvisio.model.EUGeneExporter;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.MappFormat;
import org.pathvisio.model.RasterImageWithDataExporter;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.view.MIMShapes;
import org.pathvisio.visualization.VisualizationManager;

/**
 * Main class for the Swing GUI. This class creates and shows the GUI.
 * Subclasses may override {@link #createAndShowGUI(MainPanel)} to perform custom
 * actions before showing the GUI.
 * @author thomas
 *
 */
public class GuiMain implements GdbEventListener, GexManagerListener
{
	private GuiMain()
	{
	}
	
	private MainPanelStandalone mainPanel;
	
	private PvDesktop pvDesktop;
	private SwingEngine swingEngine;
	
	private static void initLog(Engine engine)
	{
		String logDest = PreferenceManager.getCurrent().get(GlobalPreference.FILE_LOG);
		Logger.log.setDest (logDest);		
		Logger.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level
		Logger.log.info("Application name: " + engine.getApplicationName() + 
				" revision: " + Revision.REVISION);
		Logger.log.info("os.name: " + System.getProperty("os.name") +
					" os.version: " + System.getProperty("os.version") +
					" java.version: " + System.getProperty ("java.version"));
		Logger.log.info ("Locale: " + Locale.getDefault().getDisplayName());
	}

	// plugin files specified at command line
	private List<String> pluginLocations = new ArrayList<String>();
	
	// pathway specified at command line
	private URL pathwayUrl = null;
	private File pathwayFile = null;
	private String pgexFile = null;

	public void parseArgs(String [] args)
	{	
		for(int i = 0; i < args.length; i++) 
		{
			if("-p".equals(args[i])) 
			{
				i++;
				if (i < args.length) 
					pluginLocations.add(args[i]);
				else
				{
					System.out.println ("Missing plugin location after -p option"); 
					printHelp();
					System.exit(-1);
				}
			}
			else if ("-d".equals(args[i]))
			{
				i++;
				if (i < args.length) 
				{
					pgexFile = args[i];
					if (!new File(pgexFile).exists())
					{
						System.out.println ("Data file '" + pgexFile + "' not found"); 
						printHelp();
						System.exit(-1);
					}
				}
				else
				{
					System.out.println ("Missing data file location after -d option"); 
					printHelp();
					System.exit(-1);
				}
			}
			else if ("-o".equals(args[i])) 
			{
				// ignore, -o option is deprecated
			}
			else
			{
				String pws = args[i];
				File f = new File(pws);
				//Assume the argument is a file
				if(f.exists()) 
				{
					pathwayFile = f;
				} 
				else //If it doesn't exist, assume it's an url
				{
					try {
						pathwayUrl = new URL(pws);
					} catch(MalformedURLException e) 
					{
						System.out.println ("Pathway '" + args[i] + "' not a valid file or URL"); 
						printHelp();
						System.exit(-1);
					}							
				}
			}
		}
	}
	
	/**
	 * Act upon the command line arguments
	 */
	public void processOptions()
	{
		//Create a plugin manager that loads the plugins
		if(pluginLocations.size() > 0) {
			pvDesktop.initPlugins(pluginLocations);
		}
		
		
		if (pathwayFile != null)
		{
			swingEngine.openPathway (pathwayFile);
		}
		else if(pathwayUrl != null) {
			swingEngine.openPathway(pathwayUrl);
		}
	
		if (pgexFile != null)
		{
			try
			{
				
				pvDesktop.getGexManager().setCurrentGex(pgexFile, false);
				pvDesktop.loadGexCache();
				Logger.log.info ("Loaded pgex " + pgexFile);
			}
			catch (IDMapperException e)
			{
				Logger.log.error ("Couldn't open pgex " + pgexFile, e);
			}
		}
	}
	
	private String shortenString(String s) {
		return shortenString(s, 20);
	}
	
	private String shortenString(String s, int maxLength) {
		if(s.length() > maxLength) {
			String prefix = "...";
			s = s.substring(s.length() - maxLength - prefix.length());
			s = prefix + s;
		}
		return s;
	}
	
	private void setGdbStatus(JLabel gdbLabel, JLabel mdbLabel) {
		PreferenceManager prf = PreferenceManager.getCurrent();
		String gdb = prf.get(GlobalPreference.DB_CONNECTSTRING_GDB);
		String mdb = prf.get(GlobalPreference.DB_CONNECTSTRING_METADB);
		gdbLabel.setText(gdb != null ? (" | Gene database: " + shortenString(gdb)) : "");
		mdbLabel.setText(mdb != null ? (" | Metabolite database: " + shortenString(mdb)) : "");
		gdbLabel.setToolTipText(gdb != null ? gdb : "");
		mdbLabel.setToolTipText(mdb != null ? mdb : "");
	}
	
	public void gdbEvent(GdbEvent e) {
		if(e.getType() == GdbEvent.GDB_CONNECTED) {
			setGdbStatus(gdbLabel, mdbLabel);
		}
	}
	
	public void gexManagerEvent(GexManagerEvent e) 
	{
		if(e.getType() == GexManagerEvent.CONNECTION_OPENED ||
				e.getType() == GexManagerEvent.CONNECTION_CLOSED) 
		{
			SimpleGex gex = pvDesktop.getGexManager().getCurrentGex();
			if(gex != null && gex.isConnected()) {
				gexLabel.setText(" | Dataset: " + shortenString(gex.getDbName()));
				gexLabel.setToolTipText(gex.getDbName());
			} else {
				gexLabel.setText("");
				gexLabel.setToolTipText("");
			}
		}
	}

	private JLabel gdbLabel;
	private JLabel mdbLabel;
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
		
		frame.add(mainPanel, BorderLayout.CENTER);
		
		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		frame.add(statusBar, BorderLayout.SOUTH);
		
		gdbLabel = new JLabel();
		mdbLabel = new JLabel();
		gexLabel = new JLabel();

		statusBar.add(gdbLabel);
		statusBar.add(mdbLabel);
		statusBar.add(gexLabel);
		setGdbStatus(gdbLabel, mdbLabel);
		
		swingEngine.getGdbManager().addGdbEventListener(this);
		
		pvDesktop.getGexManager().addListener(this);
		
		frame.setJMenuBar(mainPanel.getMenuBar());
		frame.pack();
		PreferenceManager preferences = PreferenceManager.getCurrent();
		frame.setSize(preferences.getInt(GlobalPreference.WIN_W), preferences.getInt(GlobalPreference.WIN_H));
		frame.setLocation(preferences.getInt(GlobalPreference.WIN_X), preferences.getInt(GlobalPreference.WIN_Y));
		
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
	}
	
	public MainPanel getMainPanel() { return mainPanel; }
	
	
	static void printHelp() {
		System.out.println(
				"pathvisio [options] [pathway file]\n" +
				"Valid options are:\n" +
				"-p: A plugin file/directory to load\n" +
				"-d: A pgex data file to load\n"
		);
	}
	
	private void init()
	{
		PreferenceManager.init();
		Engine engine = new Engine();
		initLog(engine);
		engine.setApplicationName("PathVisio 1.1");
		if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.USE_SYSTEM_LOOK_AND_FEEL))
		{
			try {
			    UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				Logger.log.error("Unable to load native look and feel", ex);
			}
		}

		swingEngine = new SwingEngine(engine);
		swingEngine.setUrlBrowser(new Browser() {
			public void openUrl(URL url) {
				try {
					BrowserLauncher b = new BrowserLauncher(null);
					b.openURLinBrowser(url.toString());
				} catch (Exception ex) {
					Logger.log.error ("Couldn't open url '" + url + "'", ex);
				}
			}
		});
		pvDesktop = new PvDesktop (swingEngine);
		swingEngine.getGdbManager().initPreferred();
		
		mainPanel = new MainPanelStandalone(pvDesktop);
		mainPanel.createAndShowGUI();

		JFrame frame = createAndShowGUI(mainPanel, swingEngine);
		initImporters(engine);
		initExporters(engine, swingEngine.getGdbManager());
		MIMShapes.registerShapes();
		swingEngine.setFrame(frame);
		swingEngine.setApplicationPanel(mainPanel);
		
		// load plugins from the default plugin dir
		pluginLocations.add("" + new File (GlobalPreference.getApplicationDir(), "plugins"));
		processOptions();		
	}
	
	public static void main(String[] args) {
		final GuiMain gui = new GuiMain();
		gui.parseArgs (args);
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{		
			public void run() 
			{
				gui.init();
			}
		});
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
