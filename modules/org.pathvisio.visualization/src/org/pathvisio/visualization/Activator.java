package org.pathvisio.visualization;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.visualization.plugins.VisualizationPlugin;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {

		VisualizationPlugin plugin = new VisualizationPlugin();
		context.registerService(Plugin.class.getName(), plugin, null);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
