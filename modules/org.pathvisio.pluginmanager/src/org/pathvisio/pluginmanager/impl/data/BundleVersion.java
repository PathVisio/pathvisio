package org.pathvisio.pluginmanager.impl.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.pathvisio.desktop.plugin.IPVBundle;
import org.pathvisio.pluginmanager.impl.Utils;

public class BundleVersion implements Comparable<BundleVersion>, IPVBundle {

	private String jarFile;
	private String version;
	private String releaseDate;
	private String releaseNotes;
	private String license;
	private String iconUrl;
	private List<BundleAuthor> authors;
	private PVBundle bundle;

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
		System.out.println("copy");
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

	/**
	 * compares version numbers
	 * returns 0 if versions are the same
	 * returns < 0 if when the current version is smaller as the new
	 * returns > 0 if the current version is bigger than the new
	 */
	@Override
	public int compareTo(BundleVersion version) {
		return Utils.compare(this.version.toString(), version.getVersion().toString());
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
}
