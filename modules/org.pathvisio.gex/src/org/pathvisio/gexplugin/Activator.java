package org.pathvisio.gexplugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {

		GexPlugin plugin = new GexPlugin();
		context.registerService(Plugin.class.getName(), plugin, null);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
