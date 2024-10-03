/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.pluginmanager.impl.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.osgi.framework.Bundle;
import org.pathvisio.desktop.plugin.IPVBundle;

public class BundleVersion implements IPVBundle {

	private String jarFile;
	private String version;
	private String releaseDate;
	private String releaseNotes;
	private String license;
	private String iconUrl;
	private List<BundleAuthor> authors;
	private PVBundle bundle;
	private Boolean tmp = false;
	private Bundle osgiBundle;

	public BundleVersion() {
		authors = new ArrayList<BundleAuthor>();
	}
	
	@XmlElement(name = "jar_file_url")
	public String getJarFile() {
		return jarFile;
	}
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	@XmlElement(name = "release_date")
	public String getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}
	@XmlElement(name = "release_notes")
	public String getReleaseNotes() {
		return releaseNotes;
	}
	public void setReleaseNotes(String releaseNotes) {
		this.releaseNotes = releaseNotes;
	}
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}
	@XmlElement(name = "icon_url")
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public List<BundleAuthor> getAuthors() {
		return authors;
	}
	public void setAuthors(List<BundleAuthor> authors) {
		this.authors = authors;
	}
	public PVBundle getBundle() {
		return bundle;
	}

	public void setBundle(PVBundle bundle) {
		this.bundle = bundle;
	}
	
	public BundleVersion copyVersion() {
		BundleVersion v = new BundleVersion();
		v.setAuthors(authors);
		v.setJarFile(jarFile);
		v.setIconUrl(iconUrl);
		v.setLicense(license);
		v.setReleaseDate(releaseDate);
		v.setReleaseNotes(releaseNotes);
		v.setVersion(version);
		
		PVBundle b = new PVBundle();
		b.setCategories(bundle.getCategories());
		b.setDescription(bundle.getDescription());
		b.setFaq(bundle.getFaq());
		b.setInstalled(bundle.isInstalled());
		b.setName(bundle.getName());
		b.setShortDescription(bundle.getShortDescription());
		b.setSource(bundle.getSource());
		b.setSymbolicName(bundle.getSymbolicName());
		b.setType(bundle.getType());
		b.setWebsite(bundle.getWebsite());
		
		v.setBundle(b);
		return v;
	}
	
	@Override
	public String toString() {
		return bundle.getSymbolicName() + " - " + version;
	}

	@Override@XmlTransient
	public String getSymbolicName() {
		return getBundle().getSymbolicName();
	}

	@Override@XmlTransient
	public String getName() {
		return getBundle().getName();
	}

	@Override@XmlTransient
	public Boolean isInstalled() {
		return getBundle().isInstalled();
	}

	@Override@XmlTransient
	public String getType() {
		return getBundle().getType();
	}

	@XmlTransient
	public Boolean getTmp() {
		return tmp;
	}

	public void setTmp(Boolean tmp) {
		this.tmp = tmp;
	}

	@XmlTransient
	public Bundle getOsgiBundle() {
		return osgiBundle;
	}

	public void setOsgiBundle(Bundle osgiBundle) {
		this.osgiBundle = osgiBundle;
	}
}
