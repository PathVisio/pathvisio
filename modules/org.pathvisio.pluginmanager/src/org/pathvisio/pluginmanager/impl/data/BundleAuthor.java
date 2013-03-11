package org.pathvisio.pluginmanager.impl.data;

public class BundleAuthor {
	
	private Developer developer;
	private Affiliation affiliation;
//	private BundleVersion version;
	
	
	public Developer getDeveloper() {
		return developer;
	}
	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}
	public Affiliation getAffiliation() {
		return affiliation;
	}
	public void setAffiliation(Affiliation affiliation) {
		this.affiliation = affiliation;
	}
//	public BundleVersion getVersion() {
//		return version;
//	}
//	public void setVersion(BundleVersion version) {
//		this.version = version;
//	}	
}
