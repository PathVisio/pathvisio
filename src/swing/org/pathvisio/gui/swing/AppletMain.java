package org.pathvisio.gui.swing;

import javax.swing.JApplet;

public class AppletMain extends JApplet {
	public void init() {
		super.init();
		GuiInit.init();
		MainPanel mainPanel = SwingEngine.getApplicationPanel();
		add(mainPanel);
	}
	
	public void start() {
		// TODO Auto-generated method stub
		super.start();
	}
	
	public void stop() {
		// TODO Auto-generated method stub
		super.stop();
	}
}
