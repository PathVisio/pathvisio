package org.pathvisio.wikipathways;

import java.io.File;

import org.pathvisio.Globals;
import org.pathvisio.preferences.GlobalPreference;

public class WikiPathwaysEngine {
	public static void init() throws Exception {
		GlobalPreference.FILE_LOG.setDefault(new File(getApplicationDir(), ".wikipathwaysLog").toString());
	}
		
	private static File DIR_APPLICATION;
	/**
	 * Get the working directory of this application
	 */
	public static File getApplicationDir() {
		if(DIR_APPLICATION == null) {
			DIR_APPLICATION = new File(System.getProperty("user.home"), "." + Globals.APPLICATION_NAME);
			if(!DIR_APPLICATION.exists()) DIR_APPLICATION.mkdir();
		}
		return DIR_APPLICATION;
	}
}
