package org.pathvisio.pluginmanager.impl.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.pathvisio.pluginmanager.impl.StatusMessage;

public class PVBundle implements Comparable<PVBundle> {

	private String name;
	private String symbolicName;
	private String shortDescription;
	private String description;
	private String source;
	private String website;
	private String faq;
	private String type; 

	private Boolean installed = false;
	private StatusMessage status;
	
//	private List<BundleVersion> versions;
	private List<Category> categories;
	
	public PVBundle() {
//		versions = new ArrayList<BundleVersion>();
		categories = new ArrayList<Category>();
		status = new StatusMessage();
	}

	@XmlTransient
	public StatusMessage getStatus() {
		return status;
	}

	public void setStatus(StatusMessage status) {
		this.status = status;
	}

	public String getSymbolicName() {
		return symbolicName;
	}
	
	public String getName() {
		return name;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlTransient
	public Boolean isInstalled() {
		return installed;
	}

	public void setInstalled(Boolean installed) {
		this.installed = installed;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

//	@XmlTransient
//	public List<BundleVersion> getVersions() {
//		return versions;
//	}
//
//	public void setVersions(List<BundleVersion> versions) {
//		this.versions = versions;
//	}
	
	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getFaq() {
		return faq;
	}

	public void setFaq(String faq) {
		this.faq = faq;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

//	public boolean hasVersion(String version) {
//		for(BundleVersion ver : versions) {
//			if(Utils.compare(ver.getVersion(), version) == 0) {
//				return true;
//			}
//		}
//		return false;
//	}

	@Override
	public int compareTo(PVBundle plugin) {
		return plugin.getName().compareTo(name);
	}

//	@Override
//	public Version getStableVersion() {
//		Version v = null;
//		for(BundleVersion ver : versions) {
//			if(v == null) {
//				
//				v = new Version(ver.getVersion());
//			} else if (Utils.compare(ver.getVersion().toString(), v.toString()) > 0) {
//				v = new Version(ver.getVersion());
//			}
//		}
//		return v;
//	}
	
//	public BundleVersion getBundleVersion(String version) {
//		for(BundleVersion ver : versions) {
//			if(ver.getVersion().equals(version)) {
//				return ver;
//			}
//		}
//		return null;
//	}
//	
//	public BundleVersion getStablePluginVersion() {
//		BundleVersion version = null;
//		for(BundleVersion ver : versions) {
//			if(version == null) {
//				version = ver;
//			} else if (Utils.compare(ver.getVersion().toString(), version.toString()) > 0) {
//				version = ver;
//			}
//		}
//		return version;
//	}
}
