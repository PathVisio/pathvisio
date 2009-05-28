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

import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.bridgedb.IDMapperException;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.GdbEvent;
import org.pathvisio.data.GdbManager.GdbEventListener;
import org.pathvisio.debug.Logger;
import org.pathvisio.gex.GexManager;
import org.pathvisio.gex.SimpleGex;
import org.pathvisio.gui.swing.PathwayElementMenuListener.PathwayElementMenuHook;
import org.pathvisio.model.Pathway;
import org.pathvisio.plugin.PluginManager;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.swing.StandaloneCompat;
import org.pathvisio.view.VPathway;
import org.pathvisio.visualization.VisualizationEvent;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationMethodRegistry;

/**
 * StandaloneEngine is a singleton that ties together several
 * other important singletons and provides access to them for
 * the entire swing standalone application (not SWT).
 * StandaloneEngine provides functionality for the Standalone application
 * such as data visualization and access to gene expression data. It
 * is also a contact point for Plugins, and makes sure
 * Gex data is cached when a suitable pgdb, pgex and gpml are loaded.
 * 
 * StandaloneEngine is a singleton: There should be always exactly 
 * one instance of it.
 * 
 * //TODO: this class will probably be renamed in the future  
 */
public class PvDesktop implements ApplicationEventListener, GdbEventListener, VisualizationManager.VisualizationListener
{
	private final VisualizationManager visualizationManager;
	private final GexManager gexManager;
	private final SwingEngine swingEngine;
	private final StandaloneCompat compat;
	
	/**
	 * During construction, visualizationManager and gexManager will be initialized.
	 * SwingEngine needs to have been initialized already.
	 */
	public PvDesktop(SwingEngine swingEngine)
	{
		if (swingEngine == null) throw new NullPointerException();
		this.swingEngine = swingEngine;
		swingEngine.getEngine().addApplicationEventListener(this);
		swingEngine.getGdbManager().addGdbEventListener(this);
		gexManager = GexManager.getCurrent();
		visualizationManager = new VisualizationManager(
				VisualizationMethodRegistry.getCurrent(),
				swingEngine.getEngine(), gexManager);
		visualizationManager.addListener(this);
		compat = new StandaloneCompat(this);
	}

	/**
	 * Return the global visualizationManager instance.
	 */
	public VisualizationManager getVisualizationManager() 
	{
		return visualizationManager;
	}
	
	public VisualizationMethodRegistry getVisualizationMethodRegistry()
	{
		return VisualizationMethodRegistry.getCurrent();
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
		final SimpleGex gex = gexManager.getCurrentGex();
		final Pathway p = swingEngine.getEngine().getActivePathway();
		if(p != null && gex != null) {
			final ProgressKeeper pk = new ProgressKeeper(
					(int)1E5
			);
			final ProgressDialog d = new ProgressDialog(
					JOptionPane.getFrameForComponent(swingEngine.getApplicationPanel()), 
					"", pk, false, true
			);
					
			SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
				protected Void doInBackground() {
					pk.setTaskName("Loading expression data");
					try
					{	
						gex.cacheData(p.getDataNodeXrefs(), pk, swingEngine.getGdbManager().getCurrentGdb());
					}
					catch (IDMapperException e)
					{
						Logger.log.error ("Exception while caching expression data ", e);
					}
					pk.finished();
					return null;
				}
				
				@Override
				protected void done()
				{
					swingEngine.getEngine().getActiveVPathway().redraw();
				}
			};
			
			sw.execute();
			d.setVisible(true);
		}
	}
	

	/**
	 * Update Gex cache in response to opening pathways.
	 */
	public void applicationEvent(ApplicationEvent e) 
	{
		if(e.getType() == ApplicationEvent.PATHWAY_OPENED) 
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
		for (int i = 0; i < menuBar.getMenuCount(); ++i)
		{
			JMenu menuAt = menuBar.getMenu(i);
			if (menuAt.getText().equals (submenu))
			{
				menuAt.add(a);
				break;
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
			catch (IDMapperException ex)
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
		disposed = true;
	}

	public void visualizationEvent(VisualizationEvent e) 
	{
		// redraw Pathway
		VPathway vPwy = swingEngine.getEngine().getActiveVPathway();
		if (vPwy != null) vPwy.redraw();
	}

	private PluginManager manager = null;
	
	public void initPlugins(List<String> locations)
	{
		if (manager != null) throw new IllegalStateException ("Can't initialize plugin manager twice!");
		manager = new PluginManager(locations, this);	
	}
	
	public PluginManager getPluginManager()
	{
		return manager;
	}
}
