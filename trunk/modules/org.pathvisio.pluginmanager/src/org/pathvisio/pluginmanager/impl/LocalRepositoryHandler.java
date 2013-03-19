package org.pathvisio.pluginmanager.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.pluginmanager.impl.data.BundleVersion;
import org.pathvisio.pluginmanager.impl.data.PVRepository;
import org.pathvisio.pluginmanager.impl.io.RepoXmlFactory;

/**
 * handler of the local bundle repository
 * @author martina
 *
 */
public class LocalRepositoryHandler {

	private PVRepository localRepository;
	private File localRepoLocation;
	private File localRepoXmlFile;
	private boolean initSuccess = true;
	private BundleContext context;
	private Map<String, Bundle> startedBundles;
	private PluginManager manager;
	
	public LocalRepositoryHandler(BundleContext context, PluginManager manager) {
		this.context = context;
		this.manager = manager;
		startedBundles = new HashMap<String, Bundle>();
	}
	
	/**
	 * initializes the local repository
	 * if pathvisio.xml exists it reads the file
	 * otherwise it creates an empty repository file
	 */
	public void init(File localRepoLocation) {
		this.localRepoLocation = localRepoLocation;

		localRepoXmlFile = new File(localRepoLocation, "pathvisio.xml");
		if(localRepoXmlFile.exists()) {
			try {
				Logger.log.info("Read local repository file and start bundles.");
				localRepository = new RepoXmlFactory().readXml(localRepoXmlFile);
				setUpLocalRepo();
			} catch (Exception e) {
				Logger.log.error("Could not set up local repository (" + e.getMessage() + ")");
				initSuccess = false;
			}
		} else {
			newLocalRepo();
		}
	}
	
	/**
	 * creates new repository file if it does not exist yet
	 */
	private void newLocalRepo() {
		Logger.log.info("Initialize local repository file " + localRepoLocation.getAbsolutePath() + ".");
		if(!localRepoLocation.exists()) {
			localRepoLocation.mkdirs();
		}
		localRepository = new PVRepository();
		localRepository.setUrl(localRepoLocation.getAbsolutePath());
	}
	
	/**
	 * installs and starts the bundles in the local repository
	 */
	private void setUpLocalRepo() {
		List<Bundle> bundleList = new ArrayList<Bundle>();
		
		for(BundleVersion version : localRepository.getBundleVersions()) {
			Logger.log.info("Install bundle from local repository (" + version.getSymbolicName() + ", " + version.getVersion() + ")");
			try {
				File file = new File(version.getJarFile());
				if(file.exists()) {
					Bundle b = context.installBundle(file.toURI().toString());
					bundleList.add(b);
					version.getBundle().getStatus().setBundle(b);
				} else {
					version.getBundle().getStatus().setSuccess(false);
					version.getBundle().getStatus().setMessage("Could not install plugin " + version.getJarFile());
					manager.getProblems().add(version);
					Logger.log.error("Could not install plugin " + version.getJarFile() + " (File does not exist)");
				}
			} catch (BundleException e) {
				e.printStackTrace();
				version.getBundle().getStatus().setSuccess(false);
				version.getBundle().getStatus().setMessage("Could not install plugin " + version.getJarFile());
				File file = new File(version.getJarFile());
				file.delete();
				manager.getProblems().add(version);
				Logger.log.error("Could not install plugin " + version.getJarFile() + " (" + e.getMessage() + ")");
			}
		}
		
		for(Bundle b : bundleList) {
			BundleVersion bundleVersion = localRepository.getBundle(b.getSymbolicName(), Utils.formatVersion(b.getVersion().toString()));
			try {
				b.start();
				
				startedBundles.put(bundleVersion.getSymbolicName(), b);
				Logger.log.info("Bundle started " + bundleVersion.getSymbolicName());
				
				bundleVersion.getBundle().getStatus().setSuccess(true);
				bundleVersion.getBundle().getStatus().setMessage("Installed");
				bundleVersion.getBundle().setInstalled(true);
				
			} catch (BundleException e) {
				bundleVersion.getBundle().getStatus().setSuccess(false);
				bundleVersion.getBundle().getStatus().setMessage("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
				
				Logger.log.error("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
				try {
					// necessary to uninstall and delete file
					// so it can be installed at a later point
					b.uninstall();
					File file = new File(b.getLocation());
					file.delete();
					manager.getProblems().add(bundleVersion);
				} catch (BundleException e1) {}
			}
		}
	}
	
	/**
	 * checks if a bundle is installed
	 */
	public BundleVersion containsBundle(String symbolicName) {
		for(BundleVersion version : localRepository.getBundleVersions()) {
			if(version.getSymbolicName().equals(symbolicName)) {
				return version;
			}
		}
		return null;
	}
	
	public void removeBundleVersion(BundleVersion version) {
		localRepository.getBundleVersions().remove(version);
		startedBundles.remove(version.getSymbolicName());
	}
	
	public void clean(List<BundleVersion> problems) {
		localRepository.getBundleVersions().removeAll(problems);
	}
	
	/**
	 * returns a list of all installed plugins
	 * @return
	 */
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
	
	/**
	 * updates local xml repository file
	 * when a new plugin is installed or 
	 * a plugin is uninstalled
	 */
	public void updateLocalXml() {
		if(localRepoXmlFile.exists()) {
			localRepoXmlFile.delete();
		}
		try {
			new RepoXmlFactory().writeXml(localRepoXmlFile, localRepository);
		} catch (Exception e) {
			Logger.log.error("Could not update local repository (" + e.getMessage() + ")");
		}
	}

	// SETTERS & GETTERS
	
	public PVRepository getLocalRepository() {
		return localRepository;
	}

	public Map<String, Bundle> getStartedBundles() {
		return startedBundles;
	}

	public boolean isInitSuccess() {
		return initSuccess;
	}

	public File getLocalRepoLocation() {
		return localRepoLocation;
	}

	public File getLocalRepoXmlFile() {
		return localRepoXmlFile;
	}
}
