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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.net.URL;

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
		applet.getContentPane().removeAll();
		applet.getContentPane().add(label, BorderLayout.CENTER);
		applet.getContentPane().validate();
		applet.getContentPane().repaint();
	}

	public void showDocument(URL url, String target) {
		applet.getAppletContext().showDocument(url, target);
	}
}
