// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.plugin;

import org.pathvisio.gui.swing.PvDesktop;

/**
 * This interface needs to be implemented by PathVisio plugins
 */
public interface Plugin {
	/**
	 * Called on loading the plugin
	 */
	public void init(PvDesktop desktop);

	/**
	 * Called on unloading plugin.
	 * Give plugin chance to unregister listeners, stop timers, close connections, etc.
	 */
	public void done();
}
