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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.pathvisio.pluginmanager.IPluginManager;

/**
 * implementation of the IPluginManager interface
 * functionality to install plugins, connecting to the repository,...
 * 
 * @author martina
 *
 */
public class PluginManager implements IPluginManager {

	private BundleContext context;
	private Set<Repository> onlineRepositories;
	private File bundleDir;
	private RepositoryAdmin repoAdmin;
	
	public PluginManager (BundleContext context) {
		this.context = context;
		onlineRepositories = new HashSet<Repository>();
	}
	
	public void init(File bundleDir, Set<URL> onlineRepo) {
		this.bundleDir = bundleDir;
		startLocalBundles();
		
		ServiceReference ref = context.getServiceReference(RepositoryAdmin.class.getName());
		repoAdmin = (RepositoryAdmin) context.getService(ref);

		try {
			for(URL url : onlineRepo) {
				Repository repo = repoAdmin.addRepository(url);
				onlineRepositories.add(repo);
			}
			
			for(Repository rep : repoAdmin.listRepositories()) {
				System.out.println(rep.getName() + "\t" + rep.getResources().length);
				for(Resource r : rep.getResources()) {
					System.out.print(r.getSymbolicName());
					for(Bundle b : context.getBundles()) {
						if(b.getSymbolicName().equals(r.getSymbolicName()) && b.getVersion().equals(r.getVersion())) {
							System.out.print(" is installed.");
						}
					}
					System.out.println();
				}
				System.out.println("\n");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Map<String, String> startLocalBundles() {
		Map<String, String> statusReport = new HashMap<String, String>();
		List<Bundle> bundles = new ArrayList<Bundle>();
		for(File f : bundleDir.listFiles()) {
			if(f.getName().endsWith("jar")) {
				String msg = installBundle(f, bundles);
				if(msg != null) {
					statusReport.put(f.getName(), msg);
				}
			}
		}
		
		for(Bundle b : bundles) {
			String msg = startBundle(b);
			statusReport.put(b.getSymbolicName(), msg);
		}
		
		return statusReport;
	}
	
	private String startBundle(Bundle b) {
		try {
			b.start();
			System.out.println("Bundle " + b.getSymbolicName() + " started");
			return "started";
		} catch (BundleException e) {
			System.err.println ("Could not start bundle " + b.getSymbolicName() + "\t" + e.getMessage());
			try {
				// necessary so it can be installed at a later point
				b.uninstall();
			} catch (BundleException e1) {}
			return e.getMessage();
		}
	}

	private String installBundle(File f, List<Bundle> bundles) {
		try {
			Bundle b = context.installBundle(f.toURI().toString());
			System.out.println ("Loading " + f.toURI());
			bundles.add(b);
		} catch (BundleException ex) {
			if(ex.getMessage().contains("has already been installed from")) {
				return "already installed";
			}
			System.err.println ("Could not install bundle " + ex.getMessage());
			return ex.getMessage();
		}
		return null;
	}

	/**
	 * method installs all OSGi bundles present in the
	 * provided directory
	 * returns a status report map with status information for each OSGi bundle
	 * 		key = bundle symbolic name (or file name if bundle couldn't be installed)
	 *      value = running / error message 
	 */
	public Map<String, String> installLocalPlugins(File dir) {
		if(dir != null && dir.isDirectory() && dir.listFiles().length > 0) {
			List<File> files = new ArrayList<File>();
			for(File f : dir.listFiles()) {
				if(f.getName().endsWith("jar")) {
					// copy files in localrepo
					File dest = new File(bundleDir, f.getName());
					try {
						Utils.copyFile(f, dest);
						files.add(dest);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return installLocalPlugins(files);
		} else {
			// TODO: throw exception --> directory is not valid or empty
		}
		return null;
	}
	
	public Map<String, String> installLocalPlugins(List<File> files) {
		Map<String, String> statusReport = new HashMap<String, String>();
		List<Bundle> bundles = new ArrayList<Bundle>();

		for (File file : files) {
			String msg = installBundle(file, bundles);
			if (msg != null) {
				statusReport.put(file.getName(), msg);
				file.delete();
			}
		}
		for (Bundle b : bundles) {
			String msg = startBundle(b);
			statusReport.put(b.getSymbolicName(), msg);
		}
		return statusReport;
	}

	
//	Map<String, String> status = pvDesktop.getPluginManagerExternal().downloadPLugin("org.pathvisio.htmlexport");
//	for(String key : status.keySet()) {
//		System.out.println(key + "\t" + status.get(key));
//	}
//	pvDesktop.getPluginManager().startPlugins();
	@Override
	public Map<String, String> downloadPLugin(String symbolicName) {
		try {
			Resolver resolver = repoAdmin.resolver();
			Resource[] resources = repoAdmin.discoverResources("(symbolicname=" + symbolicName + ")");
			for(Resource r : resources) {
				System.out.println(r.getSymbolicName() + "\t" + r.getVersion());
			}
			if(resources != null && resources.length > 0) {
				resolver.add(resources[0]);
				loadDependencies(resolver, resources[0]);
			}
			List<File> files = new ArrayList<File>();
			for(Resource res : resolver.getAddedResources()) {
				System.out.println("Download " + res.getSymbolicName() + "\t" + res.getURI());
				File file = Utils.downloadFile(res.getURI(), res, bundleDir);
				if(file != null) files.add(file);
			}
			for(Resource res : resolver.getRequiredResources()) {
				System.out.println("Download " + res.getSymbolicName() + "\t" + res.getURI());
				File file = Utils.downloadFile(res.getURI(), res, bundleDir);
				if(file != null) files.add(file);
			}
			
			return installLocalPlugins(files);
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void loadDependencies(Resolver resolver, Resource resource) {
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
}
