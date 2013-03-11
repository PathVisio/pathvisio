package org.pathvisio.pluginmanager.impl.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://www.pathvisio.org")
public class PVRepository {

	private String name;
	private String url;
	private List<BundleVersion> bundleVersions;
	
	public PVRepository () {
		bundleVersions = new ArrayList<BundleVersion>();
	}
	
	public void addPluginVersion(BundleVersion version) {
		if(version != null && !bundleVersions.contains(version)) {
			bundleVersions.add(version);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public File getRepoLocation() {
		File file = new File(url);
		if(file.exists()) {
			return file;
		}
		return null;
	}
	
	public boolean containsBundle(String symbolicName) {
		for(BundleVersion version : bundleVersions) {
			if(version.getBundle().getSymbolicName().equals(symbolicName)) {
				return true;
			}
		}
		return false;
	}
	
	public BundleVersion getBundle(String symbolicName, String version) {
		for(BundleVersion v : bundleVersions) {
			if(v.getSymbolicName().equals(symbolicName) && v.getVersion().equals(version)) {
				return v;
			}
		}
		return null;
	}

	// XmLElementWrapper generates a wrapper element around XML representation
	@XmlElementWrapper(name = "bundle_version_list")
	// XmlElement sets the name of the entities
	@XmlElement(name = "pv_bundle_version")
	public List<BundleVersion> getBundleVersions() {
		return bundleVersions;
	}

	public void setBundleVersions(List<BundleVersion> bundleVersions) {
		this.bundleVersions = bundleVersions;
	}
}
