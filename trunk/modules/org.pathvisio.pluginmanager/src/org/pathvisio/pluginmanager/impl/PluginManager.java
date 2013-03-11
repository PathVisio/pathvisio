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
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

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
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.IPluginManager;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.pluginmanager.impl.data.BundleVersion;
import org.pathvisio.pluginmanager.impl.data.PVBundle;
import org.pathvisio.pluginmanager.impl.data.PVRepository;
import org.pathvisio.pluginmanager.impl.dialogs.PluginManagerDialog;
import org.pathvisio.pluginmanager.impl.io.RepoXmlFactory;
import org.pathvisio.pluginmanager.impl.io.RepoXmlReader;

/**
 * implementation of the IPluginManager interface
 * functionality to install plugins, connecting to the repository,...
 * 
 * @author martina
 *
 */
public class PluginManager implements IPluginManager {

	private PVRepository localRepository;
	private List<PVRepository> onlineRepos;
	
	private BundleContext context;
	private RepositoryAdmin repoAdmin;

	private List<Plugin> runningPlugins;
	
	public PluginManager (BundleContext context) {
		this.context = context;
		onlineRepos = new ArrayList<PVRepository>();
		runningPlugins = new ArrayList<Plugin>();
	}
	
	public void init(File localRepo, Set<URL> onlineRepo) {
		
		// initialize local repository and start bundles
		if(new File(localRepo, "pathvisio.xml").exists()) {
			Logger.log.info("Read local repository file and start bundles.");
			System.out.println("[INFO]\tRead local repository file and start bundles.");
			localRepository = new RepoXmlFactory().readXml(new File(localRepo, "pathvisio.xml"));
			setUpLocalRepo();
		} else {
			Logger.log.info("Initialize local repository file.");
			System.out.println("[INFO]\tInitialize local repository file.");
			localRepository = new PVRepository();
			localRepository.setUrl(localRepo.getAbsolutePath());
		}

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
					Logger.log.error("Could not initialize repository " + url + "\t" + e.getMessage());
					System.out.println("[ERROR]\tCould not initialize repository " + url + "\t" + e.getMessage());
				} 
			}
		} else {
			Logger.log.error("Could not initialize online repositories.");
			System.out.println("[ERROR]\tCould not initialize online repositories.");
		}
	}
	
	private void setUpOnlineRepo(Repository repo, URL url) {
		if(repo != null) {
			Logger.log.info("Initialize repository " + url);
			System.out.println("[INFO]\tInitialize repository " + url);
			
			List<PVRepository> repositories = readPluginInfo(url);
			if(repositories != null) {
				for(PVRepository r : repositories) {
					// set to installed if bundle is present in local repository
					for(BundleVersion version : r.getBundleVersions()) {
						if(localRepository.containsBundle(version.getSymbolicName())) {
							version.getBundle().setInstalled(true);
						}
						System.out.println(repo.getName() + "\t" + version.getSymbolicName() + "\t" + version.getVersion() + "\t" + version.getBundle().isInstalled());
					}
				}
				
				onlineRepos.addAll(repositories);
			}
		}
	}
	
	private List<PVRepository> readPluginInfo(URL url) {
		try {
			RepoXmlReader reader = new RepoXmlReader();
			List<PVRepository> repositories = reader.parseFile(Utils.getXMLURL(url));
			return repositories;
		} catch (MalformedURLException e) {
			Logger.log.error("Could not initialize repository " + url + "\t" + e.getMessage());
			System.out.println("[ERROR]\tCould not initialize repository " + url + "\t" + e.getMessage());
		}
		return null;
	}
	
	private void setUpLocalRepo() {
		List<Bundle> bundleList = new ArrayList<Bundle>();
		for(BundleVersion version : localRepository.getBundleVersions()) {
			Logger.log.info("Install bundle from local repository (" + version.getSymbolicName() + ", " + version.getVersion() + ")");
			System.out.println("[INFO]\tInstall bundle from local repository (" + version.getSymbolicName() + ", " + version.getVersion() + ")");
			try {
				Bundle b = context.installBundle(version.getJarFile());
				bundleList.add(b);
				version.getBundle().getStatus().setBundle(b);
			} catch (BundleException e) {
				version.getBundle().getStatus().setSuccess(false);
				version.getBundle().getStatus().setMessage("Could not install plugin " + version.getJarFile());
				Logger.log.error("Could not install plugin " + version.getJarFile());
				System.out.println("[ERROR]\tCould not install plugin " + version.getJarFile());
				
			}
		}
		for(Bundle b : bundleList) {
			BundleVersion bundleVersion = localRepository.getBundle(b.getSymbolicName(), Utils.formatVersion(b.getVersion().toString()));
			try {
				b.start();
				
				Logger.log.info("Bundle started " + bundleVersion.getSymbolicName());
				System.out.println("[INFO]\tBundle started " + bundleVersion.getSymbolicName());
				
				bundleVersion.getBundle().getStatus().setSuccess(true);
				bundleVersion.getBundle().getStatus().setMessage("Installed");
				bundleVersion.getBundle().setInstalled(true);
				
			} catch (BundleException e) {
				bundleVersion.getBundle().getStatus().setSuccess(false);
				bundleVersion.getBundle().getStatus().setMessage("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
				
				Logger.log.error("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
				System.err.println("[ERROR]\tCould not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
				try {
					// necessary so it can be installed at a later point
					b.uninstall();
					File file = new File(b.getLocation());
					file.delete();
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
						Logger.log.info("Initialize plugin " + refs[i].getBundle().getSymbolicName());
						System.out.println("[INFO]\tInitialize plugin " + refs[i].getBundle().getSymbolicName());
						
						ServiceReference ref = context.getServiceReference(PvDesktop.class.getName());
						plugin.init((PvDesktop) context.getService(ref));
						runningPlugins.add(plugin);
					} else {
						Logger.log.info("Plugin " + refs[i].getBundle().getSymbolicName() + " is running.");
						System.out.println("[INFO]\tPlugin " + refs[i].getBundle().getSymbolicName() + " is running.");
					}
				}
			} else {
				Logger.log.info("No plugins loaded.");
				System.out.println("[INFO]\tNo plugins loaded.");
			}
		} catch (InvalidSyntaxException e) {
			Logger.log.error("Could not initialize plugins");
			System.out.println("[ERROR]\tCould not initialize plugins");
		}
	}
	
	public void installPluginFromRepo(BundleVersion version) {
		List<Resource> resources = resolveDependencies(version);
		if(resources.size() == 0) {
			version.getBundle().getStatus().setSuccess(false);
			version.getBundle().getStatus().setMessage("Could not load dependencies for bundle " + version.getSymbolicName());
			
			Logger.log.error("Could not load dependencies for bundle " + version.getSymbolicName());
			System.out.println("[ERROR]\tCould not load dependencies for bundle " + version.getSymbolicName());
		} else {
			List<Bundle> bundleList = new ArrayList<Bundle>();
			
			for(Resource res : resources) {
				BundleVersion bundleVersion = getBundle(res.getSymbolicName(), Utils.formatVersion(res.getVersion().toString()));
				if(bundleVersion != null && !bundleVersion.isInstalled()) {
					File file = Utils.downloadFile(res.getURI(), res, localRepository.getRepoLocation());
					if(file != null) {
						try {
							Bundle b = context.installBundle(file.toURI().toString());
							bundleList.add(b);
							bundleVersion.getBundle().getStatus().setBundle(b);
							Logger.log.info("Bundle installed " + res.getURI());
							System.out.println("[INFO]\tBundle installed " + res.getURI());
						} catch (BundleException e) {
							bundleVersion.getBundle().getStatus().setSuccess(false);
							bundleVersion.getBundle().getStatus().setMessage("Could not install plugin " + res.getURI());
							file.delete();
							Logger.log.error("Could not install plugin " + res.getURI() + "\t" + e.getMessage());
							System.out.println("[ERROR]\tCould not install plugin " + res.getURI() + "\t" + e.getMessage());
						}
					} else {
						bundleVersion.getBundle().getStatus().setSuccess(false);
						bundleVersion.getBundle().getStatus().setMessage("Could not download file from " + res.getURI());
					}
				} else {
					Logger.log.error("Resource not found in database or it is already installed.");
					System.out.println("[ERROR]\tResource not found in database or it is already installed.");
				}
			}
		
			for(Bundle b : bundleList) {
				BundleVersion bundleVersion = getBundle(b.getSymbolicName(), Utils.formatVersion(b.getVersion().toString()));
				if(bundleVersion != null) {
					startBundle(b, bundleVersion);
				} else {
					Logger.log.error("BundleVersion not found.");
					System.out.println("[ERROR]\tBundleVersion not found.");
				}
			}
			
			updateLocalXml();
			initPlugins();
			dlg.updateData();
		}
	}
	
	private List<Resource> resolveDependencies(BundleVersion bundleVersion) {
		List<Resource> list = new ArrayList<Resource>();
		try {
			Resolver resolver = repoAdmin.resolver();
			Resource[] resources = repoAdmin.discoverResources("(symbolicname=" + bundleVersion.getSymbolicName() + ")");

			if(resources != null && resources.length > 0) {
				Logger.log.info("Installing bundle " + bundleVersion.getSymbolicName() + " requires " + resources.length + " resources.");
				System.out.println("[INFO]\tInstalling bundle " + bundleVersion.getSymbolicName() + " requires " + resources.length + " resources.");
				
				resolver.add(resources[0]);
				loadDependencies(resolver, resources[0]);
				
				for(Resource res : resolver.getAddedResources()) {
					list.add(res);
				}
				for(Resource res : resolver.getRequiredResources()) {
					list.add(res);
				}
			} else {
				Logger.log.error("Could not resolve bundle " + bundleVersion.getSymbolicName());
				System.out.println("[ERROR]\tCould not resolve bundle " + bundleVersion.getSymbolicName());
			}
		} catch (InvalidSyntaxException e) {
			Logger.log.error("Could not resolve bundle " + bundleVersion.getSymbolicName());
			System.out.println("[ERROR]\tCould not resolve bundle " + bundleVersion.getSymbolicName());
		}
		return list;
	}
	
	private void startBundle(Bundle b, BundleVersion bundleVersion) {
		try {
			b.start();
			
			Logger.log.info("Bundle started " + bundleVersion.getSymbolicName());
			System.out.println("[INFO]\tBundle started " + bundleVersion.getSymbolicName());
			
			bundleVersion.getBundle().getStatus().setSuccess(true);
			bundleVersion.getBundle().getStatus().setMessage("Installed");
			bundleVersion.getBundle().setInstalled(true);
			
			BundleVersion copyVersion = bundleVersion.copyVersion();
			copyVersion.setJarFile(b.getLocation());
			localRepository.addPluginVersion(copyVersion);
			
		} catch (BundleException e) {
			bundleVersion.getBundle().getStatus().setSuccess(false);
			bundleVersion.getBundle().getStatus().setMessage("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
			
			Logger.log.error("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
			System.err.println("[ERROR]\tCould not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
			try {
				// necessary so it can be installed at a later point
				b.uninstall();
				File file = new File(b.getLocation());
				file.delete();
			} catch (BundleException e1) {}
		}
	}
	
	private void updateLocalXml() {
		File f = new File(localRepository.getRepoLocation(), "pathvisio.xml");
		System.out.println(localRepository.getBundleVersions().size());
		if(f.exists()) {
			f.delete();
		}
		new RepoXmlFactory().writeXml(f, localRepository);
	}
	
	public BundleVersion getBundle(String symbolicName, String version) {
		for(PVRepository repo : onlineRepos) {
			for(BundleVersion v : repo.getBundleVersions()) {
				System.out.println(">> " + symbolicName + "\t" + version + "\t\t>>" + v.getSymbolicName() + "\t" + v.getVersion());
				if(v.getSymbolicName().equals(symbolicName) && v.getVersion().equals(version)) {
					return v;
				}
			}
		}
		return null;
	}

	private void loadDependencies(Resolver resolver, Resource resource) {
		System.out.println(resolver.resolve());
		if(resolver.resolve()) {
			for(Resource res : resolver.getRequiredResources()) {
				System.out.println("Deploying dependency: " + res.getPresentationName() + " (" + res.getSymbolicName() + ") " + res.getVersion());
			}
		} else {
			Reason[] reqs = resolver.getUnsatisfiedRequirements();
		    for (int i = 0; i < reqs.length; i++) {
		        System.out.println("Unable to resolve: " + reqs[i].getRequirement().getFilter());
		    }
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
			if(version.getType() != null && version.getType().equals("plugin")) {
				if(version.isInstalled() && !list.contains(version)) {
					list.add(version);
				}
			}
			
		}
		return list;
	}
	
	public List<PVBundle> getErrors() {
		List<PVBundle> list = new ArrayList<PVBundle>();
		
		for(PVRepository repo : onlineRepos) {
			for(BundleVersion version : repo.getBundleVersions()) {
				if(!version.getBundle().getStatus().isSuccess()) {
					if(!list.contains(version.getBundle())) {
						list.add(version.getBundle());
					}
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

	@Override
	public void installLocalPlugins(File bundleDir) {
		// TODO Auto-generated method stub
		
	}
}