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
package org.pathvisio.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.Revision;

/**
 * Creates and displays the About dialog,
 * showing some general information about the application.
 */
public class AboutDlg 
{
	private static URL IMG_ABOUT_LOGO = Engine.getCurrent().getResourceURL("logo.jpg");	
	
	/**
	 * call this to open the dialog
	 */
	public static void createAndShowGUI()
	{
		final JFrame aboutDlg = new JFrame();
		
		JLabel versionLabel = new JLabel (Engine.getCurrent().getApplicationName());
		JLabel revisionLabel = new JLabel (Revision.REVISION);
		JTextArea label = new JTextArea();
		label.setText("R.M.H. Besseling\nS.P.M.Crijns\nI. Kaashoek\nM.M. Palm\n" +
			"E.D. Pelgrim\nT.A.J. Kelder\nM.P. van Iersel\nE. Neuteboom\nE.J. Creusen\nP. Moeskops\nBiGCaT");
		label.setBackground(UIManager.getColor("Label.background"));
		JLabel iconLbl = new JLabel(new ImageIcon (IMG_ABOUT_LOGO));
		
		Box mainContentBox = Box.createHorizontalBox();
		mainContentBox.add (iconLbl);	
		mainContentBox.add (label);
		
		JButton btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				aboutDlg.setVisible (false);
				aboutDlg.dispose();
			}
		});
		
		Box dialogBox = Box.createVerticalBox();
		dialogBox.add (versionLabel);
		dialogBox.add (revisionLabel);
		dialogBox.add (mainContentBox);
		dialogBox.add (btnOk);			
		
		aboutDlg.setResizable(false);
		aboutDlg.setTitle("About " + Globals.APPLICATION_NAME);
		aboutDlg.add (dialogBox);
		aboutDlg.pack();
		aboutDlg.setVisible(true);
	}
}
