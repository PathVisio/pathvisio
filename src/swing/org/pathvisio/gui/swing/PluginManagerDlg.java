// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.pathvisio.plugin.Plugin;
import org.pathvisio.plugin.PluginManager.PluginInfo;

/**
 * Creates and displays the Plugin Manager dialog,
 * showing information about each plugin,
 * where they were found, and if there was an error during initialization
 */
public class PluginManagerDlg 
{
	private PvDesktop pvDesktop;

	
	/**
	 * call this to open the dialog
	 */
	public void createAndShowGUI()
	{
		final JFrame dlg = new JFrame();
		
		JTextArea label = new JTextArea();
		label.setEditable(false);
		label.setBackground(UIManager.getColor("Label.background"));
		
		dlg.setLayout (new BorderLayout());
		dlg.add (new JScrollPane (label), BorderLayout.CENTER);
		
		JButton btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dlg.setVisible (false);
				dlg.dispose();
			}
		});
		
		dlg.add (btnOk, BorderLayout.SOUTH);			
		
//		label.append ("Locations:\n");
//		for (String location : pvDesktop.getPluginManager().getLocations())
//		{
//			label.append("\t" + location + "\n");
//		}

		label.append ("Plugin Classes:\n");
		for (Class<Plugin> pluginClass : pvDesktop.getPluginManager().getPluginClasses())
		{
			label.append("\t" + pluginClass + "\n");
		}
		
		label.append ("Plugin Info:\n");
		{
			for (PluginInfo inf : pvDesktop.getPluginManager().getPluginInfo())
			{
				label.append ("\tParam: " + inf.param + "\n");				
				label.append ("\tClass: " + inf.plugin + "\n");				
				if (inf.jar != null) label.append ("\tJar: " + inf.jar.getAbsolutePath() + "\n");
				if (inf.error != null) label.append ("\tError: " + inf.error + "\n");
				label.append ("\n");
			}
		}

		dlg.setTitle("About Plugins");
		dlg.pack();
		dlg.setLocationRelativeTo(pvDesktop.getFrame());
		dlg.setVisible(true);		
	}
		
	public PluginManagerDlg(PvDesktop desktop) 
	{
		this.pvDesktop = desktop;
	}
}
