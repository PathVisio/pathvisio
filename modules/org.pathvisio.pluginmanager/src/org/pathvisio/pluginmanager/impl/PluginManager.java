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
package org.pathvisio.pluginmanager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.IPluginManager;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.desktop.plugin.PluginRepoPreference;
import org.pathvisio.gui.ProgressDialog;
import org.pathvisio.pluginmanager.impl.data.BundleVersion;
import org.pathvisio.pluginmanager.impl.data.Category;
import org.pathvisio.pluginmanager.impl.data.PVBundle;
import org.pathvisio.pluginmanager.impl.data.PVRepository;
import org.pathvisio.pluginmanager.impl.dialogs.PluginManagerDialog;
import org.pathvisio.pluginmanager.impl.io.RepoXmlReader;

/**
 * implementation of the IPluginManager interface
 * functionality to install plugins, connecting to the repository,
 * uninstalling plugins
 * 
 * @author martina
 *
 */
public class PluginManager implements IPluginManager {

	private List<PVRepository> onlineRepos;
	private List<BundleVersion> problems;
	private Map<String, BundleVersion> tmpBundles;
	
	private BundleContext context;
	private RepositoryAdmin repoAdmin;

	private Map<String, Plugin> runningPlugins;
	
	private PvDesktop desktop;
	
	private LocalRepositoryHandler localHandler;
	private PluginManagerDialog dlg;
	
	public static final String ARG_PROPERTY_WPID = "wp.id";
	
	public PluginManager (BundleContext context) {
		this.context = context;
		onlineRepos = new ArrayList<PVRepository>();
		runningPlugins = new HashMap<String, Plugin>();
		problems = new ArrayList<BundleVersion>();
		localHandler = new LocalRepositoryHandler(context, this);
		tmpBundles = new HashMap<String, BundleVersion>();
	}
	
	public void init(PvDesktop desktop) {
		this.desktop = desktop;

		// initialize local repository
		localHandler.init(GlobalPreference.getBundleDir());

		// do not save bundles that could not get started
		localHandler.clean(problems);

		// initializes running plugins
		initPlugins();

		// initialize online repositories
		final ServiceReference ref = context.getServiceReference(RepositoryAdmin.class.getName());
		repoAdmin = (RepositoryAdmin) context.getService(ref);

		if (ref != null) {
			SwingWorker<Void, Map<Repository, URL>> worker = new SwingWorker<Void, Map<Repository, URL>>() {
				boolean atLeastOneSuccess = false;
				Throwable connectionException;

				@Override
				protected Void doInBackground() throws Exception {

					URL url = new URL(PreferenceManager.getCurrent().get(PluginRepoPreference.ONLINE_REPO_URL));
					try {
						Repository repo = repoAdmin.addRepository(url);
						Map<Repository, URL> map = new HashMap<Repository, URL>();
						map.put(repo, url);
						publish(map);
						atLeastOneSuccess = true;
					} catch (Exception e) {
						Logger.log.error("Could not initialize repository " + url + "\t" + e.getMessage());
						connectionException = e;
					}
					return null;
				}

				@Override
				protected void process(List<Map<Repository, URL>> result) {
					for (Map<Repository, URL> repo : result) {
						for (Repository r : repo.keySet()) {
							setUpOnlineRepo(r, repo.get(r));
						}
					}
				}

				@Override
				protected void done() {
					if (atLeastOneSuccess) {
						status = PluginManagerStatus.CONNECTION_COMPLETED_SUCCESSFULLY;
						
						// check if WP plugin needs to be installed
						String str = System.getProperty(ARG_PROPERTY_WPID);
						if (str != null && !runningPlugins.containsKey("org.pathvisio.wpclient")) {
							for (BundleVersion plugin : getAvailablePlugins()) {
								if (plugin.getSymbolicName().equals("org.pathvisio.wpclient")) {
									installPluginFromRepo(plugin);
								}
							}
						}
					} else {
						status = PluginManagerStatus.CONNECTION_COMPLETED_FAILURE;
						savedConnectionException = connectionException;
					}

					if (dlg != null) {
						dlg.updateData();
					}
				}

			};
			worker.execute();
		} else {
			Logger.log.error("Could not initialize online repositories.");
		}
	}
	
	/**
	 * calls init method of all registered plugins
	 */
	private void initPlugins() {
		try {
			ServiceReference[] refs = context.getServiceReferences(Plugin.class.getName(), null);
			if(refs != null) {
				for(int i = 0; i < refs.length; i++) {
					Plugin plugin = (Plugin) context.getService(refs[i]);
					if(!runningPlugins.containsKey(refs[i].getBundle().getSymbolicName())) {
						Logger.log.info("Initialize plugin " + refs[i].getBundle().getSymbolicName());
						
						ServiceReference ref = context.getServiceReference(PvDesktop.class.getName());
						checkTmpBundles(refs[i]);
						plugin.init((PvDesktop) context.getService(ref));
						runningPlugins.put(refs[i].getBundle().getSymbolicName(), plugin);
					} else {
						Logger.log.info("Plugin " + refs[i].getBundle().getSymbolicName() + " is running.");
					}
				}
			} else {
				Logger.log.info("No plugins loaded.");
			}
		} catch (InvalidSyntaxException e) {
			JOptionPane.showMessageDialog(desktop.getFrame(), "Problem occured when starting plugins.");
			Logger.log.error("Could not initialize plugins (" + e.getMessage() + ")");
		}
	}

	
	private void checkTmpBundles(ServiceReference serviceReference) {
		String symName = serviceReference.getBundle().getSymbolicName();
		if(getLocalHandler().containsBundle(symName) == null) {
			if(!symName.equals("org.pathvisio.gex") && !symName.equals("org.pathvisio.statistics") &&
					!symName.equals("org.pathvisio.visualization")) {
				PVBundle b = new PVBundle();
				b.setInstalled(true);
				b.setSymbolicName(symName);
				b.setName(symName);
				b.setType("plugin");
				b.setSource("temp");
				BundleVersion v = new BundleVersion();
				v.setOsgiBundle(serviceReference.getBundle());
				v.setBundle(b);
				v.setTmp(true);
				v.setVersion(Utils.formatVersion(serviceReference.getBundle().getVersion().toString()));
				tmpBundles.put(symName, v);
			}
		}
	}

	/**
	 * initializes the online repository
	 * information comes from the pathvisio.xml file
	 * in the repository
	 * such an XML file can contain more than one 
	 * repository
	 */
	private void setUpOnlineRepo(Repository repo, URL url) {
		if(repo != null) {
			Logger.log.info("Initialize repository " + url);
			
			List<PVRepository> repositories = readPluginInfo(url);
			if(repositories != null) {
				for(PVRepository r : repositories) {
					// set to installed if bundle is present in local repository
					for(BundleVersion version : r.getBundleVersions()) {
						if(localHandler.containsBundle(version.getSymbolicName()) != null) {
							version.getBundle().setInstalled(true);
						}
					}
				}
				// a database can contain more than one repository
				// all of them will be added separately
				onlineRepos.addAll(repositories);
			}
		}
	}
	
	/**
	 * currently the pathvisio.xml file is created through a mysql dump
	 */
	private List<PVRepository> readPluginInfo(URL url) {
		try {
			RepoXmlReader reader = new RepoXmlReader();
			List<PVRepository> repositories = reader.parseFile(Utils.getXMLURL(url));
			return repositories;
		} catch (MalformedURLException e) {
			Logger.log.error("Could not initialize repository " + url + "\t" + e.getMessage());
		}
		return null;
	}
	
	
	/**
	 * installs plugin from online repository
	 */
	public void installPluginFromRepo(final BundleVersion version) {
				
		final ProgressKeeper pk = new ProgressKeeper();
		final ProgressDialog d = new ProgressDialog(dlg, "", pk, false, true);
		
		SwingWorker<Boolean, Boolean> sw = new SwingWorker<Boolean, Boolean>() {
			@Override protected Boolean doInBackground() throws InterruptedException {
				pk.setTaskName("Installing plugin");
				Thread.sleep(200);

				List<Resource> resources = resolveDependencies(version);
				if(resources.size() == 0) {
					version.getBundle().getStatus().setSuccess(false);
					version.getBundle().getStatus().setMessage("Could not load dependencies for bundle " + version.getSymbolicName());
					
					Logger.log.error("Could not load dependencies for bundle " + version.getSymbolicName());
					problems.add(version);
				} else {
					List<Bundle> bundleList = new ArrayList<Bundle>();
						for(Resource res : resources) {
							BundleVersion bundleVersion = getAvailableBundle(res.getSymbolicName(), Utils.formatVersion(res.getVersion().toString()));

							if(bundleVersion != null && !bundleVersion.isInstalled()) {
								File file;
								try {
									file = Utils.downloadFile(res.getURI(), res, localHandler.getLocalRepoLocation());
									try {
										Bundle b = context.installBundle(file.toURI().toString());
										bundleList.add(b);
										bundleVersion.getBundle().getStatus().setBundle(b);
										bundleVersion.setJarFile(file.getAbsolutePath());
										Logger.log.info("Bundle installed " + res.getURI());
									} catch (BundleException e) {
										bundleVersion.getBundle().getStatus().setSuccess(false);
										bundleVersion.getBundle().getStatus().setMessage("Could not install plugin " + res.getURI());
										file.delete();
										problems.add(bundleVersion);
										Logger.log.error("Could not install plugin " + res.getURI() + "\t" + e.getMessage());
									}
								} catch (Exception e1) {
									bundleVersion.getBundle().getStatus().setSuccess(false);
									bundleVersion.getBundle().getStatus().setMessage("Could not download file from " + res.getURI() + " (" + e1.getMessage() + ")");
									problems.add(bundleVersion);
								}
							} else {
								Logger.log.error("Resource not found in database or it is already installed.");
							}
						}
					
						for(Bundle b : bundleList) {
							BundleVersion bundleVersion = getAvailableBundle(b.getSymbolicName(), Utils.formatVersion(b.getVersion().toString()));
							if(bundleVersion != null) {
								startBundle(b, bundleVersion);
							} else {
								Logger.log.error("BundleVersion not found.");
							}
						}
						
						localHandler.updateLocalXml();
						initPlugins();
				}
				pk.finished();
				return true;
			}

			@Override public void done()
			{
				if(version.getBundle().isInstalled()) {
					JOptionPane.showMessageDialog(dlg, "Plugin " + version.getSymbolicName() + "  was installed successfully.");
				} else {
					JOptionPane.showMessageDialog(dlg, "There was a problem installing plugin " + version.getSymbolicName() + ". Please check the error tab.");
				}
				localHandler.clean(problems);
				// show status
				if(dlg != null) dlg.updateData();
			}
		};

		sw.execute();
		d.setVisible(true);
	}
	
	/**
	 * uninstalls plugin
	 * @param bundleVersion
	 */
	public void uninstallBundle(BundleVersion bundleVersion) {
		if(localHandler.getStartedBundles().containsKey(bundleVersion.getSymbolicName())) {
			int reply = JOptionPane.showConfirmDialog(dlg, "Do you really want to uninstall plugin " + bundleVersion.getName() + "?", "Warning", JOptionPane.YES_NO_OPTION);
	        if (reply == JOptionPane.YES_OPTION) {
		        Bundle b = localHandler.getStartedBundles().get(bundleVersion.getSymbolicName());
				try {
					b.stop();
					b.uninstall();
					File file = new File(bundleVersion.getJarFile());
					file.delete();
					BundleVersion onlineBundle = getAvailableBundle(bundleVersion.getSymbolicName(), bundleVersion.getVersion());
					if(onlineBundle != null) {
						onlineBundle.getBundle().setInstalled(false);
					}
					localHandler.removeBundleVersion(bundleVersion);
	
					localHandler.updateLocalXml();
					dlg.updateData();
					Logger.log.info("Bundle " + bundleVersion.getName() + " is uninstalled.");
					JOptionPane.showMessageDialog(dlg, "Plugin " + bundleVersion.getSymbolicName() + " has been uninstalled.");
				} catch (BundleException e) {
					Logger.log.error("Could not stop plugin " + bundleVersion.getName());
					JOptionPane.showMessageDialog(dlg, "Could not uninstall plugin " + bundleVersion.getSymbolicName() + ". Please check error tab.");
				}
	        }
		} else {
			if(bundleVersion.getTmp()) {
				int reply = JOptionPane.showConfirmDialog(dlg, "Do you really want to uninstall plugin " + bundleVersion.getName() + "?", "Warning", JOptionPane.YES_NO_OPTION);
		        if (reply == JOptionPane.YES_OPTION) {
		    		try {
						bundleVersion.getOsgiBundle().stop();
						bundleVersion.getOsgiBundle().uninstall();
						tmpBundles.remove(bundleVersion.getSymbolicName());
						dlg.updateData();
						Logger.log.info("Bundle " + bundleVersion.getName() + " is uninstalled.");
						JOptionPane.showMessageDialog(dlg, "Plugin " + bundleVersion.getSymbolicName() + " has been uninstalled.");
					} catch (BundleException e) {
						Logger.log.error("Could not stop plugin " + bundleVersion.getName());
						JOptionPane.showMessageDialog(dlg, "Could not uninstall plugin " + bundleVersion.getSymbolicName() + ". Please check error tab.");
					}
	        	}
	        }
    	}
	}
	
	/**
	 * resolves dependencies when installing bundle
	 * all dependencies will be downloaded and started
	 */
	private List<Resource> resolveDependencies(BundleVersion bundleVersion) {
		List<Resource> list = new ArrayList<Resource>();
		try {
			Resolver resolver = repoAdmin.resolver();
			
			String filter = "(&(symbolicname=" + bundleVersion.getSymbolicName() + ")(version>="+ bundleVersion.getVersion()+ "))";
			Resource[] resources = repoAdmin.discoverResources(filter);
			if(resources != null && resources.length > 0) {
				Logger.log.info("Installing bundle " + bundleVersion.getSymbolicName());
				
				resolver.add(resources[0]);
				if(!resolver.resolve()) {
					resolver.getUnsatisfiedRequirements();
				}
				
				for(Resource res : resolver.getAddedResources()) {
					if(!list.contains(res)) list.add(res);
				}
				for(Resource res : resolver.getRequiredResources()) {
					if(!list.contains(res))list.add(res);
				}
				
				for(Reason r : resolver.getUnsatisfiedRequirements()) {
					String filterR = "(&(symbolicname=" + r.getResource().getSymbolicName() + ")(version>="+ r.getResource().getVersion()+ "))";
					Resource[] res = repoAdmin.discoverResources(filterR);
					if(res.length == 1) {
						boolean installed = false;
						for(Bundle b : context.getBundles()) {
							if(b.getSymbolicName().equals(res[0].getSymbolicName())) {
								installed = true;
							}
						}
						if(!installed) {
							if(!list.contains(res[0])) list.add(res[0]);
						}
					}
				}
			} else {
				Logger.log.error("Could not resolve bundle " + bundleVersion.getSymbolicName());
			}
		} catch (InvalidSyntaxException e) {
			Logger.log.error("Could not resolve bundle " + bundleVersion.getSymbolicName());
		}
		return list;
	}
	
	/**
	 * starts a bundle
	 */
	private void startBundle(Bundle b, BundleVersion bundleVersion) {
		try {
			b.start();
			localHandler.getStartedBundles().put(bundleVersion.getSymbolicName(), b);
			Logger.log.info("Bundle started " + bundleVersion.getSymbolicName());
			
			bundleVersion.getBundle().getStatus().setSuccess(true);
			bundleVersion.getBundle().getStatus().setMessage("Installed");
			bundleVersion.getBundle().setInstalled(true);
			
			BundleVersion copyVersion = bundleVersion.copyVersion();
			localHandler.getLocalRepository().addPluginVersion(copyVersion);
			
		} catch (BundleException e) {
			bundleVersion.getBundle().getStatus().setSuccess(false);
			bundleVersion.getBundle().getStatus().setMessage("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
			
			Logger.log.error("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
			try {
				// necessary so it can be installed at a later point
				b.uninstall();
				File file = new File(bundleVersion.getJarFile());
				file.delete();
				problems.add(bundleVersion);
			} catch (BundleException e1) {}
		}
	}

	/**
	 * returns bundle version when available in one of the online
	 * repositories
	 */
	public BundleVersion getAvailableBundle(String symbolicName, String version) {
		for(PVRepository repo : onlineRepos) {
			for(BundleVersion v : repo.getBundleVersions()) {
				if(v.getSymbolicName().equals(symbolicName) && v.getVersion().equals(version)) {
					return v;
				}
			}
		}
		return null;
	}
	
	public Set<BundleVersion> getBundlesPerTag(String tag) {
		Set<BundleVersion> set = new HashSet<BundleVersion>();
		for(PVRepository repo : onlineRepos) {
			for(BundleVersion v : repo.getBundleVersions()) {
				if(!v.isInstalled() && v.getBundle().hasCatgeory(tag)) {
					set.add(v);
				}
			}
		}
		return set;
	}
	
	public Set<Category> getAvailableTags() {
		Set<Category> tags = new HashSet<Category>();
		for(PVRepository repo : onlineRepos) {
			for(BundleVersion version : repo.getBundleVersions()) {
				if(version.getType() != null && version.getType().equals("plugin")) {
					tags.addAll(version.getBundle().getCategories());
				}
			}
		}
		return tags;
	}

	/**
	 * returns all available plugins that are not installed
	 * does not return libraries
	 */
	public List<BundleVersion> getAvailablePlugins() {	
		Map<String, BundleVersion> map = new HashMap<String, BundleVersion>();
		for(PVRepository repo : onlineRepos) {
			for(BundleVersion version : repo.getBundleVersions()) {
				if(version.getType() != null && version.getType().equals("plugin")) {
					if(!version.isInstalled()) {
						// only show latest version of the plugin
						if(map.containsKey(version.getSymbolicName())) {
							String currentVer = map.get(version.getSymbolicName()).getVersion();
							String newVer = version.getVersion();
							int comp = Utils.compareVersions(currentVer, newVer);
							if(comp < 0) {
								map.remove(version.getSymbolicName());
								map.put(version.getSymbolicName(), version);
							}
						} else {
							map.put(version.getSymbolicName(), version);
						}
					}
				}
			}
		}
		List<BundleVersion> list = new ArrayList<BundleVersion>();
		list.addAll(map.values());
		return list;
	}

	/**
	 * returns all bundle versions that could not get started
	 */
	public List<BundleVersion> getErrors() {
		List<BundleVersion> list = new ArrayList<BundleVersion>();
		
			for(BundleVersion version : problems) {
				if(!version.getBundle().getStatus().isSuccess()) {
					if(!list.contains(version)) {
						list.add(version);
					}
				}
			}

		
		return list;
	}

	
	/**
	 * implements method of interface that opens the
	 * plugin manager dialog
	 * is called from org.pathvisio.desktop module
	 */
	@Override
	public void showGui(JFrame parent) {
		dlg = new PluginManagerDialog(this);
		dlg.createAndShowGUI(parent);
	}
	
	/**
	 * implements method of interface that installs
	 * a list of plugins and their dependencies
	 */
	@Override
	public void installLocalPlugins(List<File> plugins, List<File> dependencies) {
		List<BundleVersion> bundleVersion = new ArrayList<BundleVersion>();
		for(File file : dependencies) {
			try {
				BundleVersion ver = createBundleVersion(file, "lib");
				if(ver != null) bundleVersion.add(ver);
			} catch (FileNotFoundException e) {
				Logger.log.error("Could not read file " + file.getAbsolutePath() + " (" + e.getMessage() + ")");
			} catch (IOException e) {
				Logger.log.error("Could not read file " + file.getAbsolutePath() + " (" + e.getMessage() + ")");
			}
		}
		
		for(File file : plugins) {
			try {
				BundleVersion ver = createBundleVersion(file, "plugin");
				if(ver != null) bundleVersion.add(ver);
			} catch (FileNotFoundException e) {
				Logger.log.error("Could not read file " + file.getAbsolutePath() + " (" + e.getMessage() + ")");
			} catch (IOException e) {
				Logger.log.error("Could not read file " + file.getAbsolutePath() + " (" + e.getMessage() + ")");
			}
		}
		Map<BundleVersion, Bundle> map = new HashMap<BundleVersion, Bundle>();
		for(BundleVersion v : bundleVersion) {
			try {
				File file = new File(v.getJarFile());
				Bundle b = context.installBundle(file.toURI().toString());
				v.getBundle().getStatus().setBundle(b);
				
				map.put(v, b);
				Logger.log.info("Bundle installed " + b.getSymbolicName());
			} catch (BundleException e) {
				v.getBundle().getStatus().setSuccess(false);
				v.getBundle().getStatus().setMessage("Could not install plugin " + v.getSymbolicName());
				File f = new File(v.getJarFile());
				f.delete();
				problems.add(v);
				Logger.log.error("Could not install plugin " + v.getSymbolicName() + "\t" + e.getMessage());
			}
		}
		
		for(BundleVersion version : map.keySet()) {
			startBundle(map.get(version), version);
		}
		localHandler.updateLocalXml();
		initPlugins();
		JOptionPane.showMessageDialog(desktop.getFrame(), "Plugins installed.\nCheck the plugin manager for more information.");
		
	}
	
	/**
	 * creates a bundle version object for a local jar file
	 * uses symbolic name, name and version in the manifest
	 */
	private BundleVersion createBundleVersion(File file, String type) throws FileNotFoundException, IOException {
		JarInputStream jarStream = new JarInputStream(new FileInputStream(file));
		Manifest mf = jarStream.getManifest();
		
		Attributes attr = mf.getMainAttributes();
		String symName = attr.getValue("Bundle-SymbolicName");
		String version = attr.getValue("Bundle-Version");
		version = Utils.formatVersion(version);
		String name = attr.getValue("Bundle-Name");
		if(name.contains("%")) {
			name = symName;
		}
		jarStream.close();
		if(!localHandler.getLocalRepository().containsBundle(symName) || (localHandler.getLocalRepository().getBundle(symName, version) == null)) {
			PVBundle bundle = new PVBundle();
			bundle.setName(name);
			bundle.setSymbolicName(symName);
			bundle.setType(type);
			bundle.setSource("local installation");
			
			BundleVersion bVer = new BundleVersion();
			bVer.setBundle(bundle);
			bVer.setVersion(version);
			File destFile = new File(localHandler.getLocalRepository().getUrl(), symName + "-" + version + ".jar");
			Utils.copyFile(file, destFile);
			bVer.setJarFile(destFile.getAbsolutePath());
			return bVer;
		}
		return null;
	}
	
	// SETTERS & GETTERS

	public List<BundleVersion> getProblems() {
		return problems;
	}

	public LocalRepositoryHandler getLocalHandler() {
		return localHandler;
	}
	
	public List<PVRepository> getOnlineRepos() {
		return onlineRepos;
	}

	public Map<String, BundleVersion> getTmpBundles() {
		return tmpBundles;
	}

	public enum PluginManagerStatus
	{
		/** This status indicates that the PluginManager attempted to connect,
		 * but failed due to a problem, most likely due to firewall or slow connection (timeout) */
		CONNECTION_COMPLETED_FAILURE,
		
		/** This status indicates that the PluginManager is still attempting to connect to the online repository */
		BUSY,
		
		/** 
		 * This status indicates that AT LEAST ONE online repository was initialized correctly.
		 * It is possible that others did fail.  
		 * If the number of online repositories is still zero, it means there weren't any to begin with. */
		CONNECTION_COMPLETED_SUCCESSFULLY
	}
	
	private PluginManagerStatus status = PluginManagerStatus.BUSY;
	private Throwable savedConnectionException = null;
	
	public PluginManagerStatus getStatus() {
		return status;
	}

	/**
	 * Return the status of connecting to online repositories.
	 * The resulting message may contain html formatting suitable for a JLabel.
	 */
	public String getStatusMessage()
	{
		String msg;
		switch (status)
		{
		case BUSY:
			msg = "Attempting to connect to online repository, please wait...";
			break;
		case CONNECTION_COMPLETED_FAILURE:
			msg = "PathVisio was not able to connect to the online plugin repository.";
			if (savedConnectionException != null)
			{
				// create a user-friendly exception message
				Throwable t = savedConnectionException;
				if (t.getCause() != null) t = t.getCause();
				Logger.log.warn("<html>Exception occurred while connecting to the online repository." +
						"<br>" + t.getClass().getSimpleName() + ": " + savedConnectionException.getCause().getMessage());
			}
			break;
		default:
		case CONNECTION_COMPLETED_SUCCESSFULLY:
			// if connection has completed successfully, but repo is empty, then there simply are no plugins
			msg = "Connected succesfully to " + onlineRepos.size();
			if(onlineRepos.size() == 1) {
				msg = msg + " repository.";
			} else {
				msg = msg + " repositories.";
			}
		}
		return msg;
	}

	public PvDesktop getDesktop() {
		return desktop;
	}

}
