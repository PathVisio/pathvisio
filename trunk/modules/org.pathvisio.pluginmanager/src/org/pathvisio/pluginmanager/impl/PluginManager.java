// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2013 BiGCaT Bioinformatics
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
package org.pathvisio.pluginmanager.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.jdesktop.swingworker.SwingWorker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.IPluginManager;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.ProgressDialog;
import org.pathvisio.pluginmanager.impl.data.BundleVersion;
import org.pathvisio.pluginmanager.impl.data.PVRepository;
import org.pathvisio.pluginmanager.impl.dialogs.PluginManagerDialog;
import org.pathvisio.pluginmanager.impl.io.RepoXmlFactory;
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

	private Logger logger; 
	private PVRepository localRepository;
	private List<PVRepository> onlineRepos;
	private List<BundleVersion> problems;
	
	private BundleContext context;
	private RepositoryAdmin repoAdmin;

	private List<Plugin> runningPlugins;
	private Map<String, Bundle> startedBundles;
	
	private PvDesktop desktop;
	
	public PluginManager (BundleContext context) {
		logger = new Logger();
		logger.setStream(System.out);
		this.context = context;
		onlineRepos = new ArrayList<PVRepository>();
		runningPlugins = new ArrayList<Plugin>();
		problems = new ArrayList<BundleVersion>();
		startedBundles = new HashMap<String, Bundle>();
	}
	
	public void init(File localRepo, Set<URL> onlineRepo, PvDesktop desktop) {
		this.desktop = desktop;
		
		// initialize local repository and start bundles
		if(new File(localRepo, "pathvisio.xml").exists()) {
			logger.info("Read local repository file and start bundles.");
			localRepository = new RepoXmlFactory().readXml(new File(localRepo, "pathvisio.xml"));
			setUpLocalRepo();
		} else {
			logger.info("Initialize local repository file.");
			localRepository = new PVRepository();
			localRepository.setUrl(localRepo.getAbsolutePath());
		}

		// do not save bundles that could not get started
		localRepository.getBundleVersions().removeAll(problems);
		
		// initializes running plugins
		initPlugins();
		
		// initialize online repositories
		ServiceReference ref = context.getServiceReference(RepositoryAdmin.class.getName());
		if(ref != null) {
			repoAdmin = (RepositoryAdmin) context.getService(ref);
			for(URL url : onlineRepo) {
				try {
					Repository repo = repoAdmin.addRepository(url);
					setUpOnlineRepo(repo, url);
				} catch (Exception e) {
					logger.error("Could not initialize repository " + url + "\t" + e.getMessage());
				} 
			}
		} else {
			logger.error("Could not initialize online repositories.");
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
			logger.info("Initialize repository " + url);
			
			List<PVRepository> repositories = readPluginInfo(url);
			if(repositories != null) {
				for(PVRepository r : repositories) {
					// set to installed if bundle is present in local repository
					for(BundleVersion version : r.getBundleVersions()) {
						if(localRepository.containsBundle(version.getSymbolicName())) {
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
	 * currently the pathvisio.xml file is created through a 
	 * mysql dump
	 * TODO: replace with JAXB xml file
	 */
	private List<PVRepository> readPluginInfo(URL url) {
		try {
			RepoXmlReader reader = new RepoXmlReader();
			List<PVRepository> repositories = reader.parseFile(Utils.getXMLURL(url));
			return repositories;
		} catch (MalformedURLException e) {
			logger.error("Could not initialize repository " + url + "\t" + e.getMessage());
		}
		return null;
	}
	
	private void setUpLocalRepo() {
		List<Bundle> bundleList = new ArrayList<Bundle>();
		for(BundleVersion version : localRepository.getBundleVersions()) {
			logger.info("Install bundle from local repository (" + version.getSymbolicName() + ", " + version.getVersion() + ")");
			try {
				Bundle b = context.installBundle(version.getJarFile());
				bundleList.add(b);
				version.getBundle().getStatus().setBundle(b);
			} catch (BundleException e) {
				version.getBundle().getStatus().setSuccess(false);
				version.getBundle().getStatus().setMessage("Could not install plugin " + version.getJarFile());
				File file = new File(version.getJarFile());
				file.delete();
				problems.add(version);
				logger.error("Could not install plugin " + version.getJarFile());
			}
		}
		
		for(Bundle b : bundleList) {
			BundleVersion bundleVersion = localRepository.getBundle(b.getSymbolicName(), Utils.formatVersion(b.getVersion().toString()));
			try {
				b.start();
				
				startedBundles.put(bundleVersion.getSymbolicName(), b);
				logger.info("Bundle started " + bundleVersion.getSymbolicName());
				
				bundleVersion.getBundle().getStatus().setSuccess(true);
				bundleVersion.getBundle().getStatus().setMessage("Installed");
				bundleVersion.getBundle().setInstalled(true);
				
			} catch (BundleException e) {
				bundleVersion.getBundle().getStatus().setSuccess(false);
				bundleVersion.getBundle().getStatus().setMessage("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
				
				logger.error("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
				try {
					// necessary to uninstall and delete file
					// so it can be installed at a later point
					b.uninstall();
					File file = new File(b.getLocation());
					file.delete();
					problems.add(bundleVersion);
				} catch (BundleException e1) {}
			}
		}
	}

	private void initPlugins() {
		try {
			ServiceReference[] refs = context.getServiceReferences(Plugin.class.getName(), null);
			if(refs != null) {
				for(int i = 0; i < refs.length; i++) {
					Plugin plugin = (Plugin) context.getService(refs[i]);
					if(!runningPlugins.contains(plugin)) {
						logger.info("Initialize plugin " + refs[i].getBundle().getSymbolicName());
						
						ServiceReference ref = context.getServiceReference(PvDesktop.class.getName());
						plugin.init((PvDesktop) context.getService(ref));
						runningPlugins.add(plugin);
					} else {
						logger.info("Plugin " + refs[i].getBundle().getSymbolicName() + " is running.");
					}
				}
			} else {
				logger.info("No plugins loaded.");
			}
		} catch (InvalidSyntaxException e) {
			logger.error("Could not initialize plugins");
		}
	}
	
	public void installPluginFromRepo(final BundleVersion version) {
		
		final ProgressKeeper pk = new ProgressKeeper();
		
		final ProgressDialog d = new ProgressDialog(desktop.getFrame(), "", pk, false, true);

		SwingWorker<Boolean, Boolean> sw = new SwingWorker<Boolean, Boolean>() {
			@Override protected Boolean doInBackground() {
				pk.setTaskName("Installing plugin");
				pk.setProgress(10);
				List<Resource> resources = resolveDependencies(version);
				if(resources.size() == 0) {
					version.getBundle().getStatus().setSuccess(false);
					version.getBundle().getStatus().setMessage("Could not load dependencies for bundle " + version.getSymbolicName());
					
					logger.error("Could not load dependencies for bundle " + version.getSymbolicName());
					problems.add(version);
				} else {
					List<Bundle> bundleList = new ArrayList<Bundle>();
					
					for(Resource res : resources) {
						BundleVersion bundleVersion = getAvailableBundle(res.getSymbolicName(), Utils.formatVersion(res.getVersion().toString()));
						if(bundleVersion != null && !bundleVersion.isInstalled()) {
							File file = Utils.downloadFile(res.getURI(), res, localRepository.getRepoLocation());
							if(file != null) {
								try {
									Bundle b = context.installBundle(file.toURI().toString());
									bundleList.add(b);
									bundleVersion.getBundle().getStatus().setBundle(b);
									bundleVersion.setJarFile(file.getAbsolutePath());
									logger.info("Bundle installed " + res.getURI());
								} catch (BundleException e) {
									bundleVersion.getBundle().getStatus().setSuccess(false);
									bundleVersion.getBundle().getStatus().setMessage("Could not install plugin " + res.getURI());
									file.delete();
									problems.add(bundleVersion);
									logger.error("Could not install plugin " + res.getURI() + "\t" + e.getMessage());
								}
							} else {
								bundleVersion.getBundle().getStatus().setSuccess(false);
								bundleVersion.getBundle().getStatus().setMessage("Could not download file from " + res.getURI());
								problems.add(bundleVersion);
							}
						} else {
							logger.error("Resource not found in database or it is already installed.");
						}
					}
				
					for(Bundle b : bundleList) {
						BundleVersion bundleVersion = getAvailableBundle(b.getSymbolicName(), Utils.formatVersion(b.getVersion().toString()));
						if(bundleVersion != null) {
							startBundle(b, bundleVersion);
						} else {
							logger.error("BundleVersion not found.");
						}
					}
					
					updateLocalXml();
					initPlugins();
					
				}
				pk.finished();
				return true;
			}

			@Override public void done()
			{
				if(version.getBundle().isInstalled()) {
					JOptionPane.showMessageDialog(desktop.getFrame(), "The plugins was installed successfully.");
				} else {
					JOptionPane.showMessageDialog(desktop.getFrame(), "There was a problem installing the plugin. Please check the error tab.");
				}
				localRepository.getBundleVersions().removeAll(problems);
				// show status
				dlg.updateData();
			}
		};

		sw.execute();
		d.setVisible(true);
	}
	
	public void uninstallBundle(BundleVersion bundleVersion) {
		if(startedBundles.containsKey(bundleVersion.getSymbolicName())) {
			Bundle b = startedBundles.get(bundleVersion.getSymbolicName());
			try {
				b.stop();
				b.uninstall();
				File file = new File(bundleVersion.getJarFile());
				file.delete();
				getAvailableBundle(bundleVersion.getSymbolicName(), bundleVersion.getVersion()).getBundle().setInstalled(false);
				localRepository.getBundleVersions().remove(bundleVersion);
				startedBundles.remove(bundleVersion.getSymbolicName());
				updateLocalXml();
				dlg.updateData();
				logger.info("Bundle " + bundleVersion.getName() + " is uninstalled.");
			} catch (BundleException e) {
				logger.error("Could not stop plugin " + bundleVersion.getName());
			}
		}
	}
	
	private List<Resource> resolveDependencies(BundleVersion bundleVersion) {
		List<Resource> list = new ArrayList<Resource>();
		try {
			Resolver resolver = repoAdmin.resolver();
			Resource[] resources = repoAdmin.discoverResources("(symbolicname=" + bundleVersion.getSymbolicName() + ")");

			if(resources != null && resources.length > 0) {
				logger.info("Installing bundle " + bundleVersion.getSymbolicName() + " requires " + resources.length + " resources.");
				
				resolver.add(resources[0]);
				loadDependencies(resolver, resources[0]);
				
				for(Resource res : resolver.getAddedResources()) {
					list.add(res);
				}
				for(Resource res : resolver.getRequiredResources()) {
					list.add(res);
				}
			} else {
				logger.error("Could not resolve bundle " + bundleVersion.getSymbolicName());
			}
		} catch (InvalidSyntaxException e) {
			logger.error("Could not resolve bundle " + bundleVersion.getSymbolicName());
		}
		return list;
	}
	
	private void startBundle(Bundle b, BundleVersion bundleVersion) {
		try {
			b.start();
			startedBundles.put(bundleVersion.getSymbolicName(), b);
			logger.info("Bundle started " + bundleVersion.getSymbolicName());
			
			bundleVersion.getBundle().getStatus().setSuccess(true);
			bundleVersion.getBundle().getStatus().setMessage("Installed");
			bundleVersion.getBundle().setInstalled(true);
			
			BundleVersion copyVersion = bundleVersion.copyVersion();
			localRepository.addPluginVersion(copyVersion);
			
		} catch (BundleException e) {
			bundleVersion.getBundle().getStatus().setSuccess(false);
			bundleVersion.getBundle().getStatus().setMessage("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
			
			logger.error("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
			try {
				// necessary so it can be installed at a later point
				b.uninstall();
				File file = new File(bundleVersion.getJarFile());
				file.delete();
				problems.add(bundleVersion);
			} catch (BundleException e1) {}
		}
	}
	
	private void updateLocalXml() {
		File f = new File(localRepository.getRepoLocation(), "pathvisio.xml");
		if(f.exists()) {
			f.delete();
		}
		new RepoXmlFactory().writeXml(f, localRepository);
	}
	
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

	private void loadDependencies(Resolver resolver, Resource resource) {
		if(!resolver.resolve()) {
			resolver.getUnsatisfiedRequirements();
		}
	}

	public List<BundleVersion> getAvailablePlugins() {
		List<BundleVersion> list = new ArrayList<BundleVersion>();
	
		for(PVRepository repo : onlineRepos) {
			for(BundleVersion version : repo.getBundleVersions()) {
				if(version.getType() != null && version.getType().equals("plugin")) {
					if(!version.isInstalled() && !list.contains(version)) {
						list.add(version);
					}
				}
			}
		}
		return list;
	}
	
	public List<BundleVersion> getInstalledPlugins() {
		List<BundleVersion> list = new ArrayList<BundleVersion>();
		
		for(BundleVersion version : localRepository.getBundleVersions()) {
			if(version.getType() != null && version.getType().equals("plugin") && !problems.contains(version)) {
				if(version.isInstalled() && !list.contains(version)) {
					list.add(version);
				}
			}
			
		}
		return list;
	}
	
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

	private PluginManagerDialog dlg;
	
	@Override
	public void showGui(JFrame parent) {
		dlg = new PluginManagerDialog(this);
		dlg.createAndShowGUI(parent);
	}

	public PVRepository getLocalRepository() {
		return localRepository;
	}

	public void setLocalRepository(PVRepository localRepository) {
		this.localRepository = localRepository;
	}

	public List<PVRepository> getOnlineRepos() {
		return onlineRepos;
	}

	public void setOnlineRepos(List<PVRepository> onlineRepos) {
		this.onlineRepos = onlineRepos;
	}

	@Override
	public void installLocalPlugins(File bundleDir) {
		// TODO Auto-generated method stub
		
	}
}