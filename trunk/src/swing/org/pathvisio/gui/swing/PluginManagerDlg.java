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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.pathvisio.Globals;
import org.pathvisio.Revision;
import org.pathvisio.plugin.Plugin;

/**
 * Creates and displays the About dialog,
 * showing some general information about the application.
 */
public class PluginManagerDlg 
{
	private PvDesktop pvDesktop;

	
	
	/**
	 * call this to open the dialog
	 */
	public void createAndShowGUI()
	{
		final JFrame aboutDlg = new JFrame();
		
		FormLayout layout = new FormLayout(
				"4dlu, pref, 4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");
		
		JTextArea label = new JTextArea();
		label.setEditable(false);
		label.setBackground(UIManager.getColor("Label.background"));
		
		CellConstraints cc = new CellConstraints();
		
		JPanel dialogBox = new JPanel();
		dialogBox.setLayout (layout);
		dialogBox .add (label, cc.xy(4,2));
		
		JButton btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				aboutDlg.setVisible (false);
				aboutDlg.dispose();
			}
		});
		
		dialogBox.add (btnOk, cc.xyw (2, 6, 3, "center, top"));			
		
		label.append ("Locations:\n");
		for (String location : pvDesktop.getPluginManager().getLocations())
		{
			label.append("\t" + location + "\n");
		}

		label.append ("Plugin Classes:\n");
		for (Class<Plugin> pluginClass : pvDesktop.getPluginManager().getPluginClasses())
		{
			label.append("\t" + pluginClass + "\n");
		}

		aboutDlg.setTitle("About Plugins");
		aboutDlg.add (dialogBox);
		aboutDlg.pack();
		aboutDlg.setLocationRelativeTo(pvDesktop.getFrame());
		aboutDlg.setVisible(true);		
	}
		
	public PluginManagerDlg(PvDesktop desktop) 
	{
		this.pvDesktop = desktop;
	}
}
