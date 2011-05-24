package org.pathvisio.desktop;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pathvisio.core.Engine;
import org.pathvisio.gui.SwingEngine;

public class Activator implements BundleActivator {

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("ACTIVATOR SWING PLUGIN");
		
//		ServiceReference ref = context.getServiceReference(Engine.class.getName());
//		Engine engine = (Engine) context.getService(ref);
//	
//		SwingEngine swingEngine = new SwingEngine(engine);
//		context.registerService(SwingEngine.class.getName(), swingEngine, null);

		Engine engine = new Engine();
		System.out.println("new engine: " + engine);
		
		SwingEngine swingEngine = new SwingEngine(engine);
		System.out.println("new swingengine: " + swingEngine);
		
		final PvDesktop pvDesktop = new PvDesktop(swingEngine);
		System.out.println("new pvdesktop: " + pvDesktop);
		context.registerService(PvDesktop.class.getName(), pvDesktop, null);
		
		final GuiMain gui = new GuiMain();
		
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				System.out.println("start Gui");
				gui.init(pvDesktop);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

}
