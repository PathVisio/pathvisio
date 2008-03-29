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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class AppletUserInterfaceHandler extends SwingUserInterfaceHandler {
	JApplet applet;
	
	public AppletUserInterfaceHandler(JApplet applet) {
		super(JOptionPane.getFrameForComponent(applet));
		this.applet = applet;
	}
	
	public Component getParent() {
		parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		parent = SwingUtilities.getRoot(parent);
		return parent;
	}
	
	public void showExitMessage(String msg) {
		JLabel label = new JLabel(msg, JLabel.CENTER);
		Enumeration<Applet> applets = applet.getAppletContext().getApplets();
		while(applets.hasMoreElements()) {
			Applet a = applets.nextElement();
			if(a instanceof PathwayPageApplet) {
				PathwayPageApplet pa = (PathwayPageApplet)a;
				pa.getContentPane().removeAll();
				pa.getContentPane().add(label, BorderLayout.CENTER);
				pa.getContentPane().validate();
				pa.getContentPane().repaint();
			}
		}

	}

	public void showDocument(URL url, String target) {
		applet.getAppletContext().showDocument(url, target);
	}
}
