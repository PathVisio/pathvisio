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
package org.pathvisio.plugins;

import edu.stanford.ejalbert.BrowserLauncher;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.Pathway;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.swing.VPathwaySwing;


/**
 * A side panel to which Putative Pathway Parts can be added.
 */
public class PppPane extends JPanel
{
	static final String TITLE = "PPP";
	SwingEngine se;
	JPanel panel;
	
	/**
	 * Add a new Pathway part to the panel, with the given description displayed above it.
	 */
	public void addPart(String desc, Pathway part)
	{
		JScrollPane scroller = new JScrollPane();
		panel.add (new JLabel(desc));
		panel.add (scroller);
		VPathwaySwing wrapper = new VPathwaySwing(scroller);
		VPathway vPwy = wrapper.createVPathway();
		vPwy.setEditMode(false);
		vPwy.setPctZoom(75);
		vPwy.fromModel(part);
		scroller.add(wrapper);

		panel.repaint();
		
		JTabbedPane pane = se.getApplicationPanel().getSideBarTabbedPane();
		int index = pane.indexOfTab(TITLE);
		if (index > 0)
		{
			pane.setSelectedIndex (index);
		}		
	}
	
	/**
	 * Create a new Ppp Pane with Help button. Parts can be added later.
	 */
	public PppPane (final SwingEngine se)
	{
		this.se = se;
		
		setLayout (new BorderLayout());
	
		panel = new JPanel ();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add (new JScrollPane (panel), BorderLayout.CENTER);
		
		// help button
		JButton help = new JButton("Help");
		panel.add (help);
		
		help.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				new Thread (new Runnable() 
				{		
					public void run() 
					{
						try
						{
							BrowserLauncher bl = new BrowserLauncher(null);
							bl.openURLinBrowser("http://www.pathvisio.org/Ppp");
						}
						catch (Exception ex)
						{
							javax.swing.SwingUtilities.invokeLater(new Runnable() 
							{		
								public void run() 
								{
									JOptionPane.showMessageDialog (se.getFrame(), 
											"Could not launch browser\nSee error log for details.", 
											"Error", JOptionPane.ERROR_MESSAGE);
								}
							});
						}
					}
				}).start();
			}
		});
		
	}	
}

