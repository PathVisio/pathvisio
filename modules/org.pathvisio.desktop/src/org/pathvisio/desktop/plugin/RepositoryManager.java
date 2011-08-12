package org.pathvisio.desktop.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.BundleContext;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.desktop.PvDesktop;

public class RepositoryManager {
	private LocalRepository localRepository;
	private List<OnlineRepository> repositories;
	
	private List<Resource> availableResources;
	private List<Resource> installedResources;
	
	
	private PvDesktop pvDesktop;
	
	public RepositoryManager(PvDesktop pvDesktop) {
		this.pvDesktop = pvDesktop;
		availableResources = new ArrayList<Resource>();
		installedResources = new ArrayList<Resource>();
		repositories = new ArrayList<OnlineRepository>();
	}
	
	public void loadRepositories() {
		try {
			// local repository 
			// TODO: read repository URL from properties file
			// this is although always the same and can not be changed!
			File file = new File(GlobalPreference.getPluginDir() + "/repository.xml");
			if(!file.exists()) {
				try {
					file.createNewFile();
					updateLocalRepository(pvDesktop.getContext());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			LocalRepository localRep = new LocalRepository(new URL("file://" + file.getAbsolutePath()), pvDesktop);
			localRep.initialize();
			setLocalRepository(localRep);
			
			// loads online repositories
			// TODO: read repository URL from properties file!
			OnlineRepository rep = new OnlineRepository(new URL("http://www.bigcat.unimaas.nl/~martina/plugins/repository.xml"), pvDesktop);
			rep.initialize();
			addRepository(rep);

			// all local resources are installed
			installedResources.addAll(localRep.getResources());
			// get all available resources that are not yet installed
			updateResourcesList();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * gets all plug-ins available in the different
	 * repositories except those which are already installed 
	 * locally
	 * TODO: what happens with same resource but different version??
	 * = update and not new resource!
	 */
	public void updateResourcesList() {
		availableResources.clear();
		installedResources.clear();
		
		for(Resource resource : localRepository.getResources()) {
			installedResources.add(resource);
		}
		
		for(OnlineRepository repo : getRepositories()) {
			for(Resource resource : repo.getResources()) {
				if(!installedResources.contains(resource)) {
					if(!availableResources.contains(resource)) {
						availableResources.add(resource);
					}
				}
			}
		}
	}
	
	public void addRepository(OnlineRepository repository) {
		if(repository != null && !repositories.contains(repository)) {
			repositories.add(repository);
		}
	}
	
	public OnlineRepository getRepository(Resource resource) {
		for(OnlineRepository repo : repositories) {
			if(repo.getResources().contains(resource)) {
				return repo;
			}
		}
		return null;
	}
	
	public void updateLocalRepository(BundleContext context) {
		try {
			File file = GlobalPreference.getPluginDir();
			int i = 0;
			for(File f : file.listFiles()) {
				if(f.getName().endsWith(".jar")) i++;
			}
			System.out.println("update local repository " + i);
			String command = "java -jar " + GlobalPreference.getApplicationDir() + "/bindex.jar -n LocalPathVisioPlugins -r " + GlobalPreference.getPluginDir() +"/repository.xml " + GlobalPreference.getPluginDir() +"/";
			Process process = Runtime.getRuntime ().exec(command);
			process.waitFor();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			LocalRepository localRep = new LocalRepository(new URL("file://" + GlobalPreference.getPluginDir() + "/repository.xml"), pvDesktop);
			setLocalRepository(localRep);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public LocalRepository getLocalRepository() {
		return localRepository;
	}
	public void setLocalRepository(LocalRepository localRepository) {
		this.localRepository = localRepository;
	}
	public List<OnlineRepository> getRepositories() {
		return repositories;
	}
	public void setRepositories(List<OnlineRepository> repositories) {
		this.repositories = repositories;
	}

	public List<Resource> getAvailableResources() {
		return availableResources;
	}

	public void setAvailableResources(List<Resource> availableResources) {
		this.availableResources = availableResources;
	}

	public List<Resource> getInstalledResources() {
		return installedResources;
	}

	public void setInstalledResources(List<Resource> installedResources) {
		this.installedResources = installedResources;
	}
}
