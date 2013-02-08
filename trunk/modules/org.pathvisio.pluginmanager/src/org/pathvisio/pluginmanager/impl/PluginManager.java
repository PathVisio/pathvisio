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

import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
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
	
	public PluginManager (BundleContext context) {
		this.context = context;
		onlineRepositories = new HashSet<Repository>();
	}
	
	public void init(File bundleDir, Set<URL> onlineRepo) {
		this.bundleDir = bundleDir;
		startLocalBundles();
		
		ServiceReference ref = context.getServiceReference(RepositoryAdmin.class.getName());
		RepositoryAdmin admin = (RepositoryAdmin) context.getService(ref);

		try {
			for(URL url : onlineRepo) {
				Repository repo = admin.addRepository(url);
				onlineRepositories.add(repo);
			}
			
			for(Repository rep : admin.listRepositories()) {
				System.out.println(rep.getName() + "\t" + rep.getResources().length);
				for(Resource r : rep.getResources()) {
					System.out.println(r.getSymbolicName());
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

		Map<String, String> statusReport = new HashMap<String, String>();
		List<Bundle> bundles = new ArrayList<Bundle>();
		if(dir != null && dir.isDirectory() && dir.listFiles().length > 0) {
			// copy files in localrepo
			List<File> copiedFiles = new ArrayList<File>();
			for(File file : dir.listFiles()) {
				File dest = new File(bundleDir, file.getName());
				try {
					Utils.copyFile(file, dest);
					copiedFiles.add(dest);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for(File file : copiedFiles) {
				String msg = installBundle(file, bundles);
				if(msg != null) {
					statusReport.put(file.getName(), msg);
					file.delete();
				} 
			}
			for(Bundle b : bundles) {
				String msg = startBundle(b);
				statusReport.put(b.getSymbolicName(), msg);
			}
		} else {
			// TODO: throw exception --> directory is not valid or empty
		}
		return statusReport;
	}
}
