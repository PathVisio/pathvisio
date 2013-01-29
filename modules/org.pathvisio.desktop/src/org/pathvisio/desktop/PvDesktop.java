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

package org.pathvisio.desktop;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.data.GdbEvent;
import org.pathvisio.core.data.GdbManager.GdbEventListener;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.data.DataException;
import org.pathvisio.desktop.data.DBConnDerby;
import org.pathvisio.desktop.data.DBConnectorSwing;
import org.pathvisio.desktop.gex.CachedData;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.plugin.PluginDialogSwitch;
import org.pathvisio.desktop.plugin.PluginManager;
import org.pathvisio.desktop.util.StandaloneCompat;
import org.pathvisio.desktop.visualization.VisualizationEvent;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.gui.PathwayElementMenuListener.PathwayElementMenuHook;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.pluginmanager.IPluginManager;

/**
 * PvDesktop ties together several
 * other important singletons and provides access to them for
 * the entire PathVisio standalone application.
 * PvDesktop provides access to components
 * such as data visualization and gene expression data. It
 * is also a contact point for Plugins, and makes sure
 * Gex data is cached when a suitable pgdb, pgex and gpml are loaded.
 * <p>
 * PvDesktop is a singleton: There should be always exactly
 * one instance of it.
 */
public class PvDesktop implements ApplicationEventListener, GdbEventListener, VisualizationManager.VisualizationListener
{
	private final VisualizationManager visualizationManager;
	private final GexManager gexManager;
	private final SwingEngine swingEngine;
	private final StandaloneCompat compat;
	private final PreferencesDlg preferencesDlg;
	private IPluginManager pluginManagerExternal;

	private BundleContext context;
	
	public BundleContext getContext() {
		return context;
	}
	
	/**
	 * During construction, visualizationManager and gexManager will be initialized.
	 * SwingEngine needs to have been initialized already.
	 * 
	 * BundleContext is an OSGi class with contains the ServiceRegistry, this is 
	 * needed in the PluginManager to get the registered Plugins
	 */
	public PvDesktop(SwingEngine swingEngine, BundleContext context)
	{
		this.context = context;
		if (swingEngine == null) throw new NullPointerException();
		this.swingEngine = swingEngine;
		swingEngine.getEngine().addApplicationEventListener(this);
		swingEngine.getGdbManager().addGdbEventListener(this);
		gexManager = new GexManager();
		visualizationManager = new VisualizationManager(
				swingEngine.getEngine(), gexManager);
		visualizationManager.addListener(this);
		compat = new StandaloneCompat(this);
		preferencesDlg = new PreferencesDlg(PreferenceManager.getCurrent());
		loadPluginManager();
		initPanels();
	}

	public void initPanels()
	{
		preferencesDlg.addPanel ("Display",
			preferencesDlg.builder()
			.integerField (
				GlobalPreference.GUI_SIDEPANEL_SIZE,
				"Initial side panel size (percent of window size):", 0, 100)
			.booleanField (
				GlobalPreference.DATANODES_ROUNDED,
				"Use rounded rectangles for data nodes")
			.integerField(
				GlobalPreference.MAX_NR_CITATIONS,
				"Maximum citations to show (use -1 to show all)",
				-1, 1000)
			.booleanField(
				GlobalPreference.SNAP_TO_ANGLE,
				"Snap to angle when moving line and rotation handles")
			.integerField (
				GlobalPreference.SNAP_TO_ANGLE_STEP,
				"Distance between snap-steps in degrees:", 1, 90)
			.booleanField (
				GlobalPreference.MIM_SUPPORT,
				"Load support for molecular interaction maps (MIM) at program start")
			.booleanField (
				GlobalPreference.SHOW_ADVANCED_PROPERTIES,
				"Show advanced properties (e.g. references)")
			.booleanField (
				GlobalPreference.USE_SYSTEM_LOOK_AND_FEEL,
				"Use Java System look-and-feel at program start")
			.booleanField(
				GlobalPreference.ENABLE_DOUBLE_BUFFERING,
				"Enable double-buffering (pathway is drawn slower, but flickerless)")
			.build());


		preferencesDlg.addPanel ("Display.Colors",
				preferencesDlg.builder()
			.colorField(
				GlobalPreference.COLOR_NO_CRIT_MET,
				"Default color for 'no criteria met':")
			.colorField(
				GlobalPreference.COLOR_NO_GENE_FOUND,
				"Default color for 'gene not found':")
			.colorField(
				GlobalPreference.COLOR_NO_DATA_FOUND,
				"Default color for 'no data found':")
			.colorField(
				GlobalPreference.COLOR_SELECTED,
				"Line color for selected objects:")
			.colorField(
				GlobalPreference.COLOR_HIGHLIGHTED,
				"Highlight color")
			.build());

		preferencesDlg.addPanel ("Files", preferencesDlg.builder()
			.fileField(
				GlobalPreference.FILE_LOG,
				"Log file:", false)
			.build());

		preferencesDlg.addPanel ("Directories", preferencesDlg.builder()
			.fileField (GlobalPreference.DIR_PWFILES,
				"Gpml pathways:", true)
			.fileField (GlobalPreference.DIR_GDB,
				"Gene databases:", true)
			.fileField (GlobalPreference.DIR_EXPR,
				"Expression datasets:", true)
			.build());

		preferencesDlg.addPanel ("Database", preferencesDlg.builder()
			.stringField (GlobalPreference.DB_ENGINE_GEX,
				"Database connector class for expression dataset:")
			.build());
		
		preferencesDlg.addPanel("Plugin Manager", 
			preferencesDlg.builder().booleanField(PluginDialogSwitch.PLUGIN_DIALOG_SWITCH, 
					"Select if you want to use the new plug-in manager (work in progress)")
			.build());

	}

	/** return the preferences dialog, can be used to add panels */
	public PreferencesDlg getPreferencesDlg()
	{
		return preferencesDlg;
	}

	/**
	 * Return the global visualizationManager instance.
	 */
	public VisualizationManager getVisualizationManager()
	{
		return visualizationManager;
	}

	/**
	 * returns the global gexManager instance.
	 */
	public GexManager getGexManager()
	{
		return gexManager;
	}

	/**
	 * returns the global swingEngine instance.
	 */
	public SwingEngine getSwingEngine()
	{
		return swingEngine;
	}

	/**
	 * Load the Gex cache for the current pathway. Only starts loading
	 * when an expression dataset is available and a pathway is open.
	 */
	public void loadGexCache() {
		final CachedData gex = gexManager.getCachedData();
		final Pathway p = swingEngine.getEngine().getActivePathway();
		if(p != null && gex != null) {
			try
			{
				gex.clearCache();
				gex.setMapper (swingEngine.getGdbManager().getCurrentGdb());
				gex.preSeed(p.getDataNodeXrefs());
				gex.preSeed(p.getLineXrefs());
				swingEngine.getEngine().getActiveVPathway().redraw();
			}
			catch (DataException e)
			{
				Logger.log.error ("Exception while caching expression data ", e);
			}
		}
	}


	/**
	 * Update Gex cache in response to opening pathways.
	 */
	public void applicationEvent(ApplicationEvent e)
	{
		if(e.getType() == ApplicationEvent.Type.PATHWAY_OPENED)
		{
			loadGexCache();
		}
	}

	/**
	 * Update gex cache in response to opening / closing gene databases
	 */
	public void gdbEvent(GdbEvent e)
	{
		loadGexCache();
	}

	/**
	 * Shortcut for getSwingEngine().getFrame()
	 * Returns frame of main application window.
	 * Useful for positioning / parenting dialogs
	 */
	public JFrame getFrame()
	{
		return swingEngine.getFrame();
	}

	/**
	 * register an action as a menu item
	 * @param submenu one of "File", "Edit", "Data" or "Help"
	 */
	public void registerMenuAction (String submenu, Action a)
	{
		JMenuBar menuBar = swingEngine.getApplicationPanel().getMenuBar();
		if(menuBar == null) {
			Logger.log.warn("Trying to register menu action while no menubar is available " +
					"(running in headless mode?)");
			return;
		}
		for (int i = 0; i < menuBar.getMenuCount(); ++i)
		{
			JMenu menuAt = menuBar.getMenu(i);
			if (menuAt.getText().equals (submenu))
			{
				JMenuItem item = menuAt.add(a);
				registeredActions.put(a, item);
				break;
			}
		}
	}
	
	private Map<Action, JMenuItem> registeredActions = new HashMap<Action, JMenuItem>();
	
	public void unregisterMenuAction (String submenu, Action a)
	{
		JMenuBar menuBar = swingEngine.getApplicationPanel().getMenuBar();
		if(menuBar != null) {
			for (int i = 0; i < menuBar.getMenuCount(); ++i)
			{
				JMenu menuAt = menuBar.getMenu(i);
				if (menuAt.getText().equals (submenu))
				{
					Logger.log.debug("unregister menu action: " + registeredActions.get(a).getText());
					menuAt.remove(registeredActions.get(a));
					registeredActions.remove(a);
					break;
				}
			}
		}
	}
	
	/**
	 * register a submenu in one of the mainMenus ("File", "Help", "Plugins")
	 */
	public void registerSubMenu (String mainMenu, JMenu submenu) {
		JMenuBar menuBar = swingEngine.getApplicationPanel().getMenuBar();
		if(menuBar == null) {
			Logger.log.warn("Trying to register menu while no menubar is available (running in headless mode?)");
			return;
		}
		for (int i = 0; i < menuBar.getMenuCount(); ++i) {
			JMenu menuAt = menuBar.getMenu(i);
			if (menuAt.getText().equals (mainMenu)) {
				JMenuItem item = menuAt.add(submenu);
				registeredMenus.put(submenu, item);
				break;
			} 
		}
	}
	
	private Map<JMenu, JMenuItem> registeredMenus = new HashMap<JMenu, JMenuItem>();//new
	
	/**
	 * should be called from done method in the plugin class so all 
	 * created submenus are removed
	 */
	public void unregisterSubMenu(String mainMenu, JMenu submenu) {
		JMenuBar menuBar = swingEngine.getApplicationPanel().getMenuBar();
		if(menuBar != null) {
			for (int i = 0; i < menuBar.getMenuCount(); ++i) {
				JMenu menuAt = menuBar.getMenu(i);
				if (menuAt.getText().equals (mainMenu)) {
					Logger.log.debug("unregister submenu: " + registeredMenus.get(submenu).getText());
					menuAt.remove(registeredMenus.get(submenu));
					registeredMenus.remove(submenu);
					break;
				}
			}
		}
	}
	
	/**
	 * Returns the JTabbedPane that corresponds to the side-bar
	 * shortcut for
	 * swingEngine.getApplicationPanel.getSideBarTabbedPane.
	 */
	public JTabbedPane getSideBarTabbedPane()
	{
		return swingEngine.getApplicationPanel().getSideBarTabbedPane();
	}

	public void addPathwayElementMenuHook(PathwayElementMenuHook hook)
	{
		swingEngine.getApplicationPanel().getPathwayElementMenuListener().addPathwayElementMenuHook(hook);
	}

	private boolean disposed = false;
	/**
	 * free all resources (such as listeners) held by this class.
	 * Owners of this class must explicitly dispose of it to clean up.
	 */
	public void dispose()
	{
		assert (!disposed);

		//explicit clean shutdown of gex prevents file from being left open
		if (gexManager.isConnected())
		{
			try
			{
				gexManager.getCurrentGex().close();
			}
			catch (DataException ex)
			{
				Logger.log.error ("Couldn't cleanly close pgex database", ex);
			}
		}

		swingEngine.getGdbManager().removeGdbEventListener(this);
		swingEngine.getGdbManager().removeGdbEventListener(compat);
		swingEngine.getEngine().removeApplicationEventListener(this);
		visualizationManager.removeListener(this);
		visualizationManager.dispose();
		gexManager.removeListener(compat);
		gexManager.close();
		disposed = true;
	}

	public void visualizationEvent(VisualizationEvent e)
	{
		loadGexCache();
		
		// redraw Pathway
		VPathway vPwy = swingEngine.getEngine().getActiveVPathway();
		if (vPwy != null) vPwy.redraw();
	}

	private PluginManager manager = null;

	public void initPlugins()
	{
		if (manager != null) throw new IllegalStateException ("Can't initialize plugin manager twice!");
		manager = new PluginManager(this);
	}

	public PluginManager getPluginManager() {
		return manager;
	}
	
	/**
	 * Ask the user to select a gdb. Uses the appropriate swingDbConnector for the
	 * current database type.
	 * dbType is "Metabolite" or "Gene" and is only used in messages to the user.
	 */
	public void selectGdb (String dbType)
	{
		try
		{
			// Get the database connector to connect to Gdb databases.
			// Currently there is only one option: DBConnDerby();
			DBConnectorSwing dbcon = new DBConnDerby();
			String result = dbcon.openChooseDbDialog(null);

			if (result == null) return;
			String dbName = "idmapper-pgdb:" + result;

			if (dbType.equals("Gene"))
			{
				swingEngine.getGdbManager().setGeneDb(dbName);
				PreferenceManager.getCurrent().set (GlobalPreference.DB_CONNECTSTRING_GDB, dbName);
			}
			else
			{
				swingEngine.getGdbManager().setMetaboliteDb(dbName);
				PreferenceManager.getCurrent().set (GlobalPreference.DB_CONNECTSTRING_METADB, dbName);
			}
		}
		catch(Exception ex)
		{
			String msg = "Failed to open " + dbType + " Database; " + ex.getMessage();
			JOptionPane.showMessageDialog(null,
					"Error: " + msg + "\n\n" + "See the error log for details.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			Logger.log.error(msg, ex);
		}
	}
	
	/**
	 * plugin manager registers a service in the activator
	 * this methods gets the plugin manager class from the OSGi registry
	 * @return
	 */
	private void loadPluginManager() {
		System.out.println(" load plugin manager");
		ServiceReference ref = getContext().getServiceReference(IPluginManager.class.getName());
		if(ref != null) {
			pluginManagerExternal = (IPluginManager) getContext().getService(ref);
			System.out.println(pluginManagerExternal);
			// TODO: warning if plugin manager service can not be resolved
		}
	}


	public IPluginManager getPluginManagerExternal() {
		return pluginManagerExternal;
	}
}
