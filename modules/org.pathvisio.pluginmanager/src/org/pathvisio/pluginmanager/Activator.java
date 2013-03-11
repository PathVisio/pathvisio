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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.IPluginManager;
import org.pathvisio.pluginmanager.impl.PluginManager;

/**
 * Activator for the plugin manager package
 * of PathVisio
 * independent jar implementing functionality
 * to load plugins
 * 
 * @author martina
 *
 */
public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		IPluginManager manager = new PluginManager(context);
		context.registerService(IPluginManager.class.getName(), manager, null);
	}

	public void stop(BundleContext context) throws Exception {
	}
}