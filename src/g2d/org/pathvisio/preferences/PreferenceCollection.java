package org.pathvisio.preferences;

import java.io.IOException;

public interface PreferenceCollection {	
	public Preference byName(String name);
	
	public void save() throws IOException;
}
