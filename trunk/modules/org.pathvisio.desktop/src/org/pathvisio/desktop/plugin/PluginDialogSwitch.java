package org.pathvisio.desktop.plugin;

import org.pathvisio.core.preferences.Preference;

public enum PluginDialogSwitch implements Preference {
	PLUGIN_DIALOG_SWITCH;

	@Override
	public String getDefault() {
		return Boolean.toString(false);
	}
}
