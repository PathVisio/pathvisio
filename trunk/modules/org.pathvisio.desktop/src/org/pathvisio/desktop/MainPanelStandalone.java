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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.desktop.gex.BackpageExpression;
import org.pathvisio.gui.BackpageTextProvider.BackpageAttributes;
import org.pathvisio.gui.DataPaneTextProvider;
import org.pathvisio.gui.MainPanel;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.WrapLayout;

/**
 * the mainPanel for the standalone (non-applet) version of PathVisio.
 */
public class MainPanelStandalone extends MainPanel
{
	protected JMenuBar menuBar;

	private StandaloneActions standaloneActions = null;
	private List<File> recent;
	
	public static GlobalPreference[] mostRecentArray = new GlobalPreference[] {
    	GlobalPreference.MOST_RECENT_1, GlobalPreference.MOST_RECENT_2,
    	GlobalPreference.MOST_RECENT_3, GlobalPreference.MOST_RECENT_4,
    	GlobalPreference.MOST_RECENT_5, GlobalPreference.MOST_RECENT_6,
    	GlobalPreference.MOST_RECENT_7, GlobalPreference.MOST_RECENT_8,
    	GlobalPreference.MOST_RECENT_9, GlobalPreference.MOST_RECENT_10,
    };

	@Override
	protected void addMenuActions(JMenuBar mb) {
		JMenu fileMenu = new JMenu("File");

		addToMenu(standaloneActions.newAction, fileMenu);
		addToMenu(standaloneActions.openAction, fileMenu);

		recentPathwaysMenu = new JMenu("Open Recent");
		initRecentPathwayList();
		fileMenu.add (recentPathwaysMenu);
		addToMenu(actions.standaloneSaveAction, fileMenu);
		addToMenu(actions.standaloneSaveAsAction, fileMenu);
		fileMenu.addSeparator();
		addToMenu(actions.importAction, fileMenu);
		addToMenu(actions.exportAction, fileMenu);
		fileMenu.addSeparator();
		addToMenu(standaloneActions.printAction, fileMenu);
		addToMenu(actions.exitAction, fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		addToMenu(actions.undoAction, editMenu);
		addToMenu(actions.copyAction, editMenu);
		addToMenu(actions.pasteAction, editMenu);
		addToMenu(standaloneActions.searchAction, editMenu);
		editMenu.addSeparator();
		addToMenu(standaloneActions.preferencesAction, editMenu);

		JMenu selectionMenu = new JMenu("Selection");
		for(Action a : actions.layoutActions) addToMenu(a, selectionMenu);

		editMenu.add (selectionMenu);

		JMenu dataMenu = new JMenu("Data");
		addToMenu (standaloneActions.selectGeneDbAction, dataMenu);
		addToMenu (standaloneActions.selectMetaboliteDbAction, dataMenu);

		JMenu viewMenu = new JMenu("View");
		JMenu zoomMenu = new JMenu("Zoom");
		viewMenu.add(zoomMenu);
		for(Action a : actions.zoomActions) addToMenu(a, zoomMenu);

		JMenu pluginsMenu = new JMenu("Plugins");//new
//		pluginsMenu.add(standaloneActions.pluginManagerAction);
		pluginsMenu.add(standaloneActions.newPluginManagerAction);
		pluginsMenu.add(standaloneActions.loadLocalBundlesAction);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(actions.aboutAction);
		helpMenu.add(standaloneActions.helpAction);

		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(dataMenu);
		mb.add(viewMenu);
		mb.add(pluginsMenu);//new
		mb.add(helpMenu);
	}

	private final PvDesktop desktop;

	public MainPanelStandalone(PvDesktop desktop)
	{
		super(desktop.getSwingEngine(), null);
		this.desktop = desktop;

		standaloneActions = new StandaloneActions(desktop);
	}

	@Override
	public void createAndShowGUI()
	{
		super.createAndShowGUI();

		dpt = new DataPaneTextProvider();
		
		// data hook for showing basic annotation and expression data on the data panel
		dpt.addDataHook(new BackpageAttributes(swingEngine.getGdbManager().getCurrentGdb()));
		dpt.addDataHook(new BackpageExpression(desktop.getGexManager()));
		DataPane dataPane = new DataPane(dpt, swingEngine.getEngine());
		dataPane.addHyperlinkListener(swingEngine);
		sidebarTabbedPane.addTab( "Data", new JScrollPane(dataPane) );
		
		SearchPane searchPane = new SearchPane(swingEngine);
		sidebarTabbedPane.addTab ("Search", searchPane);

		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("mac os x")) {
			initMacListeners();
		}
	}

	@Override
	public void applicationEvent(ApplicationEvent e) {
		super.applicationEvent(e);
		switch (e.getType()) {
		case PATHWAY_OPENED:
		case PATHWAY_SAVE:
			putInRecentPathwayList(swingEngine.getEngine().getActivePathway().getSourceFile());
			refreshRecentPathwaysMenu();
			break;
		}
	}

	JMenu recentPathwaysMenu;

	private void refreshRecentPathwaysMenu()
	{
		PreferenceManager prefs = PreferenceManager.getCurrent();

		recentPathwaysMenu.removeAll();
		int added = 0;
		recentPathwaysMenu.setMnemonic(KeyEvent.VK_R);
		for (int i = 0; i < 10; i++)
		{
			if (!prefs.get(mostRecentArray[i]).equals ("" + null))
			{
				final File file = prefs.getFile(mostRecentArray[i]);
				JMenuItem menuItem = new JMenuItem(file.getName(), KeyEvent.VK_0 + i);
				menuItem.setAccelerator(KeyStroke.getKeyStroke(menuItem.getMnemonic(), InputEvent.CTRL_DOWN_MASK));
				menuItem.setToolTipText(file.getAbsolutePath());
				menuItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						if (swingEngine.canDiscardPathway())
						{
							swingEngine.importPathway(file);
						}
					}
				});
				recentPathwaysMenu.add(menuItem);
				added++;
			}
		}
		recentPathwaysMenu.setEnabled(added != 0);
	}

	@Override
	protected void addToolBarActions(final SwingEngine swingEngine, JToolBar tb)
	{
		tb.setLayout(new WrapLayout(1, 1));

		addToToolbar(standaloneActions.newAction);
		addToToolbar(standaloneActions.openAction);
		addToToolbar(actions.standaloneSaveAction);

		// do the common part that is shared between applet and standalone version.
		addCommonToolbarActions(swingEngine, tb);
	}

	public void putInRecentPathwayList(File pwf)
	{
		PreferenceManager prefs = PreferenceManager.getCurrent();
        recent.remove(pwf);
		recent.add(0, pwf);
		if(recent.size() > 10) recent.remove(recent.size()-1);

	    for (int i = 0; i < recent.size(); ++i)
	    {

	    	prefs.setFile(mostRecentArray[i], recent.get(i));
	    }
	}

	public void initRecentPathwayList()
	{
		PreferenceManager prefs = PreferenceManager.getCurrent();
		recent = new LinkedList<File>();
		for (int i = 0; i < 10; ++i)
		{
			if (!prefs.get(mostRecentArray[i]).equals ("" + null))
			{
				recent.add(prefs.getFile(mostRecentArray[i]));
			}
		}
		refreshRecentPathwaysMenu();
	}


	/**
	 * Initializes Mac event listeners via reflection so there is no compile-time dependencies on Apple classes.
	 * Effectively performs the folowing:
	 * <pre>
	 *   com.apple.eawt.Application app = Application.getApplication();
	 *   app.addApplicationListener(new ApplicationAdapter() {
	 *     public void handleAbout(ApplicationEvent e) {
	 *       e.setHandled(true);
	 *       new HelpAboutAction().actionPerformed(null);
	 *     }
	 *     public void handleQuit(ApplicationEvent e) {
	 *       e.setHandled(false);
	 *       new ExitGateAction().actionPerformed(null);
	 *     }
	 *     public void handlePreferences(ApplicationEvent e) {
	 *       e.setHandled(true);
	 *       optionsDialog.showDialog();
	 *       optionsDialog.dispose();
	 *     }
	 *   });
	 *
	 *   app.setEnabledPreferencesMenu(true);
	 * </pre>
	 */
	protected void initMacListeners() {
		try {
			// load the Apple classes
			final Class<?> eawtApplicationClass =
					getClass().getClassLoader().loadClass("com.apple.eawt.Application");
			final Class<?> eawtApplicationListenerInterface =
					getClass().getClassLoader().loadClass("com.apple.eawt.ApplicationListener");
			final Class<?> eawtApplicationEventClass =
					getClass().getClassLoader().loadClass("com.apple.eawt.ApplicationEvent");

			// method used in the InvocationHandler
			final Method appEventSetHandledMethod = eawtApplicationEventClass.getMethod("setHandled", boolean.class);

			// Invocation handler used to process Apple application events
			InvocationHandler handler = new InvocationHandler() {

				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					Object appEvent = args[0];
					if ("handleAbout".equals(method.getName())) {
						appEventSetHandledMethod.invoke(appEvent, Boolean.TRUE);
						swingEngine.getActions().aboutAction.actionPerformed(null);
					} else if ("handleQuit".equals(method.getName())) {
						appEventSetHandledMethod.invoke(appEvent, Boolean.FALSE);
						swingEngine.getActions().exitAction.actionPerformed(null);
					} else if ("handlePreferences".equals(method.getName())) {
						appEventSetHandledMethod.invoke(appEvent, Boolean.TRUE);
						standaloneActions.preferencesAction.actionPerformed(null);
					} else if ("handleOpenFile".equals(method.getName())) {
						String filename = null;
						try {
							Method getFilenameMethod = appEvent.getClass().getDeclaredMethod("getFilename", (Class<?>[]) null);
							filename = (String) getFilenameMethod.invoke(appEvent, (Object[]) null);
							Logger.log.info("Opening filename: " + filename);
							if (swingEngine.canDiscardPathway()) {
								swingEngine.openPathway(new File(filename));
							}
						} catch (Exception ex) {
							Logger.log.error("Error opening " + filename, ex);
						}

					} else {
						System.out.println(method.getName());
					}
					return null;
				}
			};

			// Create an ApplicationListener proxy instance
			Object applicationListenerObject = Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[]{ eawtApplicationListenerInterface }, handler);

			// get hold of the Application object
			Method getApplicationMethod = eawtApplicationClass.getMethod("getApplication");
			Object applicationObject = getApplicationMethod.invoke(null);

			// enable the preferences menu item
			Method setEnabledPreferencesMenuMethod = eawtApplicationClass.getMethod("setEnabledPreferencesMenu",
					boolean.class);
			setEnabledPreferencesMenuMethod.invoke(applicationObject, Boolean.TRUE);

			// Register our proxy instance as an ApplicationListener
			Method addApplicationListenerMethod = eawtApplicationClass.getMethod("addApplicationListener",
					eawtApplicationListenerInterface);
			addApplicationListenerMethod.invoke(applicationObject, applicationListenerObject);

		} catch (Throwable error) {
			// oh well, we tried
			Logger.log.error("Problem setting up Mac bindings", error);
		}
	}
}
