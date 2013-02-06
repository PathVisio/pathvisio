// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2013 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.pathvisio.pluginmanager;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * interface which will be registered as an OSGi service
 * in the Activator class
 * 
 * will contain all functions related to the plugin manager
 * and repository of PathVisio
 * 
 * @author martina
 *
 */
public interface IPluginManager {

	/**
	 * installs all bundle jars in the given directory
	 * @param bundleDir
	 * @return status report map - status info for each bundle
	 */
	public Map<String, String> runLocalPlugin(File bundleDir);
	public void init(URL localRepo, Set<URL> onlineRepo);
}
