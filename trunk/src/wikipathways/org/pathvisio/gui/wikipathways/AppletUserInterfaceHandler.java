// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
