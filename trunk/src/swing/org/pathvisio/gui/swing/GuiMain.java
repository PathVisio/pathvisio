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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.data.DataException;
import org.pathvisio.data.GdbManager;
import org.pathvisio.data.GexManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.DataNodeListExporter;
import org.pathvisio.model.EUGeneExporter;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.MappFormat;
import org.pathvisio.plugin.PluginManager;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.view.MIMShapes;

/**
 * Main class for the Swing GUI. This class creates and shows the GUI.
 * Subclasses may override {@link #createAndShowGUI(MainPanel)} to perform custom
 * actions before showing the GUI.
 * @author thomas
 *
 */
public class GuiMain
{
	protected MainPanelStandalone mainPanel;
	
	private void initLog()
	{
		String logDest = Engine.getCurrent().getPreferences().get(GlobalPreference.FILE_LOG);
		Logger.log.setDest (logDest);		
		Logger.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level
	}

	// plugin files specified at command line
	private List<File> pluginFiles = new ArrayList<File>();
	
	// pathway specified at command line
	private URL pathwayUrl = null;
	private String pgexFile = null;

	public void parseArgs(String [] args)
	{
		
		for(int i = 0; i < args.length - 1; i++) 
		{
			if("-p".equals(args[i])) 
			{
				pluginFiles.add(new File(args[i + 1]));
				i++;
			}
			else if ("-d".equals(args[i]))
			{
				pgexFile = args[i + 1];
				if (!new File(pgexFile).exists())
				{
					printHelp();
					System.exit(-1);
				}
				i++;
			}
			else if ("-o".equals(args[i])) 
			{
				String pws = args[i + 1];
				try {
					File f = new File(pws);
					//Assume the argument is a file
					if(f.exists()) {
						pathwayUrl = f.toURI().toURL();
					//If it doesn't exist, assume it's an url
					} else {
						pathwayUrl = new URL(pws);
					}
				} catch(MalformedURLException e) {
					printHelp();
					System.exit(-1);
				}
				i++;
			}
		}
	}
	
	/**
	 * Act upon the command line arguments
	 */
	public void processOptions()
	{
		//Create a plugin manager that loads the plugins
		if(pluginFiles.size() > 0) {
			PluginManager pluginManager = new PluginManager(
					pluginFiles.toArray(new File[0])
			);
		}
		
		if(pathwayUrl != null) {
			SwingEngine.getCurrent().openPathway(pathwayUrl);
		}
	
		if (pgexFile != null)
		{
			try
			{
				GexManager.setCurrentGex(pgexFile, false);
				SwingEngine.getCurrent().loadGexCache();
				Logger.log.info ("Loaded pgex " + pgexFile);
			}
			catch (DataException e)
			{
				Logger.log.error ("Couldn't open pgex " + pgexFile, e);
			}
		}
	}
	
	/**
	 * Creates and shows the GUI. Creates and shows the Frame, sets the size, title and menubar.
	 * @param mainPanel The main panel to show in the frame
	 */
	protected JFrame createAndShowGUI(MainPanelStandalone mainPanel) 
	{
		initLog();
		initImporters();
		initExporters();
		MIMShapes.registerShapes();
		
		//Create and set up the window.
		JFrame frame = new JFrame(Globals.APPLICATION_NAME);
		// dispose on close, otherwise windowClosed event is not called.
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		frame.add(mainPanel);
		frame.setJMenuBar(mainPanel.getMenuBar());
		try {
		    UIManager.setLookAndFeel(
		        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Logger.log.error("Unable to load native look and feel", ex);
		}
		frame.pack();
		frame.setSize(800, 600);
		frame.addWindowListener(new WindowAdapter() 
		{
			public void windowClosed(WindowEvent arg0) 
			{
				GuiMain.this.shutdown();
			}
		});
		
		//Display the window.
		frame.setVisible(true);

		int spPercent = Engine.getCurrent().getPreferences().getInt (GlobalPreference.GUI_SIDEPANEL_SIZE);
		double spSize = (100 - spPercent) / 100.0;
		mainPanel.getSplitPane().setDividerLocation(spSize);
		
		return frame;
	}

	private void shutdown() 
	{
		PreferenceManager prefs = Engine.getCurrent().getPreferences();
		prefs.store();
	}
	
	public MainPanel getMainPanel() { return mainPanel; }
	
	
	static void printHelp() {
		System.out.println(
				"Command line parameters:\n" +
				"-o: A GPML file to open\n" +
				"-p: A plugin file/directory to load\n" +
				"-d: A pgex data file to load\n"
		);
	}
	
	
	public static void main(String[] args) {
		final GuiMain gui = new GuiMain();
		gui.parseArgs (args);
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
			
			public void run() {
				Engine.init();
				Engine.getCurrent().setApplicationName("PathVisio (experimental)");
				SwingEngine.init();
				MainPanelStandalone mps = new MainPanelStandalone();
				JFrame frame = gui.createAndShowGUI(mps);
				SwingEngine.getCurrent().setFrame(frame);
				SwingEngine.getCurrent().setApplicationPanel(mps);
				gui.processOptions();

			}
		});
	}
	
	private static void initImporters() 
	{
		Engine.getCurrent().addPathwayImporter(new MappFormat());
		Engine.getCurrent().addPathwayImporter(new GpmlFormat());
	}
	
	private static void initExporters() 
	{
		Engine.getCurrent().addPathwayExporter(new MappFormat());
		Engine.getCurrent().addPathwayExporter(new GpmlFormat());
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_SVG));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PNG));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_TIFF));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PDF));	
		Engine.getCurrent().addPathwayExporter(new DataNodeListExporter(SwingEngine.getCurrent().getGdbManager()));
		Engine.getCurrent().addPathwayExporter(new EUGeneExporter());
	}
	
}
