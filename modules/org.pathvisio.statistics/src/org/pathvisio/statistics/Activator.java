package org.pathvisio.statistics;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;

public class Activator implements BundleActivator {


	public void start(BundleContext context) throws Exception {
		
		StatisticsPlugin plugin = new StatisticsPlugin();
		context.registerService(Plugin.class.getName(), plugin, null);
		
	}

	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
