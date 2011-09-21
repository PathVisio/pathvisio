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
package org.pathvisio.desktop.plugin;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.pathvisio.desktop.PvDesktop;

public class LocalRepository {

	private URL url;
	private PvDesktop pvDesktop;
	private List<Resource> resources;
	private String name;
	
	
	public LocalRepository(URL url, PvDesktop pvDesktop) {
		this.url = url;
		resources = new ArrayList<Resource>();
		this.pvDesktop = pvDesktop;
	}
	
	public void initialize() {
		BundleContext context = pvDesktop.getContext();
		RepositoryAdmin admin = (RepositoryAdmin) context.getService(context.getServiceReference(RepositoryAdmin.class.getName()));
		if(admin != null) {
			try {
				admin.addRepository(url);
				org.apache.felix.bundlerepository.Repository [] repos = admin.listRepositories();
				for(org.apache.felix.bundlerepository.Repository rep : repos) {
					if(rep.getURI().toString().equals(url.toString())) {
						name = rep.getName();
//						System.out.println(name + "\t" + rep.getResources().length);
						for(Resource res : rep.getResources()) {
							resources.add(res);
							installResource(res);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean installResource(Resource resource) {
		if(resources.contains(resource)) {
			BundleContext context = pvDesktop.getContext();
			RepositoryAdmin admin = (RepositoryAdmin) context.getService(context.getServiceReference(RepositoryAdmin.class.getName()));
			Resolver resolver = admin.resolver();
			try {
				Resource[] resources = admin.discoverResources("(symbolicname=" + resource.getSymbolicName()+ ")");
				if(resources != null && resources.length > 0) {
					resolver.add(resources[0]);
					loadDependencies(resolver, resources[0]);
					
					resolver.deploy(1);
					
					for(int i = 0; i < context.getBundles().length; i++) {
						boolean found = false;
						for(Resource res : resolver.getAddedResources()) {
							if(res.getSymbolicName().equals(context.getBundles()[i].getSymbolicName())) {
								context.getBundles()[i].start();
								found = true;
							}
						}
						if(!found) {
							for(Resource res : resolver.getRequiredResources()) {
								if(res.getSymbolicName().equals(context.getBundles()[i].getSymbolicName())) {
									context.getBundles()[i].start();
								}
							}
						}
					}
					if(pvDesktop.getPluginManager() != null) {
						pvDesktop.getPluginManager().startPlugins();
					}
				}
			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BundleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private void loadDependencies(Resolver resolver, Resource resource) {
		if(resolver.resolve()) {
			for(Resource res : resolver.getRequiredResources()) {
//				System.out.println("Deploying dependency: " + res.getPresentationName() + " (" + res.getSymbolicName() + ") " + res.getVersion());
			}
		} else {
			Reason[] reqs = resolver.getUnsatisfiedRequirements();
		    for (int i = 0; i < reqs.length; i++)
		    {
//		        System.out.println("Unable to resolve: " + reqs[i].getRequirement().getFilter());
		    }
		}
	}
	
	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean uninstallResource(Resource resource) {
		BundleContext context = pvDesktop.getContext();
		for(Bundle bundle : context.getBundles()) {
			if(bundle.getSymbolicName().equals(resource.getSymbolicName())) {
				try {
					bundle.stop();
					bundle.uninstall();
					// TODO: add done functionality - removing menu items

					File file = new File(resource.getURI().replace("file:", ""));
					if(file.exists()) {
						file.delete();
						RepositoryManager manager = pvDesktop.getPluginManager().getRepositoryManager();
						manager.getInstalledResources().remove(resource);
						manager.updateLocalRepository(pvDesktop.getContext());
						
						manager.updateResourcesList();
						return true;
					}
				} catch (BundleException e) {
					e.printStackTrace();
					return false;
				}
				
			}
		}
		
		return false;
	}

	@Override
	public String toString() {
		return url.toString();
	}
}
