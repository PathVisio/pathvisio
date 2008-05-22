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

import javax.swing.*;

/**
 * Global dialog for setting the user preferences.
 */
public class PreferencesDlg 
{
	/**
	 * call this to open the dialog
	 */
	public static void createAndShowGUI()
	{
		final JFrame frame = new JFrame();
		
		
		JButton OkBtn = new JButton();
		OkBtn.setText ("OK");
		
		OkBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{
				frame.setVisible (false);
				frame.dispose();
			}
		}
		);
		
		JLabel label = new JLabel(); 
		label.setText ("Preferences dialog not yet implemented");
		
		Box box = Box.createVerticalBox();
		box.add (label);
		box.add (OkBtn);
		
		frame.add (box);
		frame.pack();
		frame.setTitle ("preferences");
		frame.setVisible (true);
	}
}
