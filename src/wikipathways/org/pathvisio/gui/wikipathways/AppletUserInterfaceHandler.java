package org.pathvisio.gui.wikipathways;

import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.pathvisio.debug.Logger;

public class AppletUserInterfaceHandler extends SwingUserInterfaceHandler {
	JApplet applet;
	
	public AppletUserInterfaceHandler(JApplet applet) {
		super(JOptionPane.getFrameForComponent(applet));
		this.applet = applet;
	}
	
	public void showExitMessage(String msg) {
		JLabel label = new JLabel(msg, JLabel.CENTER);
		applet.getContentPane().add(label);
		applet.getContentPane().validate();
		
		URL url = applet.getDocumentBase();
	        try {
			url = new URL("javascript:window.location.reload();");
		} catch(Exception ex) {
			Logger.log.error("Unable to create javascript url", ex);
		}
		applet.getAppletContext().showDocument(url, "_top");
	}

	public void showDocument(URL url, String target) {
		applet.getAppletContext().showDocument(url, target);
	}
}
