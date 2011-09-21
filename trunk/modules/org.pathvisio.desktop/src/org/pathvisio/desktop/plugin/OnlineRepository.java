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
package org.pathvisio.desktop.plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.desktop.PvDesktop;

public class OnlineRepository {

	private URL url;
	private PvDesktop pvDesktop;
	private List<Resource> resources;
	private String name;
	
	public OnlineRepository(URL url, PvDesktop pvDesktop) {
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
				}
				
				for(Resource res : resolver.getAddedResources()) {
					downloadFile(res.getURI(), res);
				}
				for(Resource res : resolver.getRequiredResources()) {
					downloadFile(res.getURI(), res);
				}
				
				resolver.resolve();
				
				RepositoryManager manager = pvDesktop.getPluginManager().getRepositoryManager();
				manager.updateLocalRepository(context);
				manager.getLocalRepository().initialize();
				manager.getInstalledResources().add(resource);
				manager.updateResourcesList();
				pvDesktop.getPluginManager().startPlugins();
			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	final static int size=1024;
	
	private void downloadFile(String uri, Resource resource) {
		File localFile = new File(GlobalPreference.getPluginDir(),resource.getSymbolicName()+"-"+resource.getVersion()+".jar");
		OutputStream outStream = null;
		URLConnection  uCon = null;

		InputStream is = null;
		try {
				
			byte[] buf;
			int ByteRead;
			outStream = new BufferedOutputStream(new FileOutputStream(localFile));
			URL fileLocation = new URL(uri);
			uCon = fileLocation.openConnection();
			is = uCon.getInputStream();
			buf = new byte[size];
			while ((ByteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, ByteRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				outStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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

	/**
	 * it is not possible to uninstall a resource
	 * from an online repository
	 */
	public boolean uninstallResource(Resource resource) {
		return false;
	}
	
	public String toString() {
		return url.toString();
	}
}
