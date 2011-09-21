// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.desktop;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.core.Engine;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.gui.SwingEngine;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		// PreferenceManager needs to be initialized before
		// Engine object is created
		PreferenceManager.init();
		
		Engine engine = new Engine();
		SwingEngine swingEngine = new SwingEngine(engine);
		
		final PvDesktop pvDesktop = new PvDesktop(swingEngine, context);
		context.registerService(PvDesktop.class.getName(), pvDesktop, null);
		
		final GuiMain gui = new GuiMain();
		
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gui.init(pvDesktop);
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
	}

}
