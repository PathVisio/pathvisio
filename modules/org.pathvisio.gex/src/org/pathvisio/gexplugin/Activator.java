package org.pathvisio.gexplugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("ACTIVATOR GexPlugin PLUGIN");

//		ServiceReference refPvDesktop = context.getServiceReference(PvDesktop.class.getName());
//		PvDesktop pvDesktop = (PvDesktop) context.getService(refPvDesktop);
		GexPlugin plugin = new GexPlugin();
		context.registerService(Plugin.class.getName(), plugin, null);
		
		
//		plugin.init(pvDesktop);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
