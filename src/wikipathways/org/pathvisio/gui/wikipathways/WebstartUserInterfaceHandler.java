package org.pathvisio.gui.wikipathways;

import java.awt.Component;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

import org.pathvisio.debug.Logger;

public class WebstartUserInterfaceHandler extends SwingUserInterfaceHandler {
	
	public WebstartUserInterfaceHandler(Component parent) {
		super(parent);
	}

	public void showDocument(URL url, String target) {
		try {
			BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
			bs.showDocument(url);
		} catch (UnavailableServiceException e) {
			Logger.log.error("Unable to get javax.jnlp.BasicService, are you not using webstart?");
		} 
	}
}
