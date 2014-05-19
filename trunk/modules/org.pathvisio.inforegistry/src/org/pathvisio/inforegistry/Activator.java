// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2014 BiGCaT Bioinformatics
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
package org.pathvisio.inforegistry;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.inforegistry.impl.InfoRegistryPlugin;

/**
 * InfoRegistry module is currently still implemented as a plugin but 
 * will be core module in the future.
 * 
 * This module will provide a side panel and plugins can register 
 * as providers of additional information about data nodes in a pathway
 * 
 * @author mkutmon
 *
 */
public class Activator implements BundleActivator {

	private InfoRegistryPlugin plugin;
	
	@Override
	public void start(BundleContext context) throws Exception {
		plugin = new InfoRegistryPlugin();
		context.registerService(Plugin.class.getName(), plugin, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin.done();
	}

}
