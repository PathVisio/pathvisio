package org.pathvisio.desktop;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.core.Engine;
import org.pathvisio.gui.SwingEngine;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
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
