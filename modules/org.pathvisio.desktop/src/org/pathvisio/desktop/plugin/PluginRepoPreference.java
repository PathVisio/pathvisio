package org.pathvisio.desktop.plugin;

import java.io.File;

import org.pathvisio.core.preferences.Preference;

public enum PluginRepoPreference implements Preference {

	ONLINE_REPO_URL(new String("http://repository.pathvisio.org/repository.xml"));

	
	PluginRepoPreference(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	PluginRepoPreference(File defaultValue) {
		this.defaultValue = "" + defaultValue;
	}
	
	private String defaultValue;

	@Override
	public String getDefault() {
		return defaultValue;
	}
}
