// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.pathvisio.core.Engine;

/**
 * A side panel which displays all objects.
 */
public class ObjectsPane extends JPanel
{
	private Engine engine;
	private SwingEngine swingEngine;
	private JPanel currentPane;

	public ObjectsPane(SwingEngine swingEngine)
	{
		this.engine = swingEngine.getEngine();
		this.swingEngine = swingEngine;
		//setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));		
		currentPane=this;
	}

	/**
	 * add item buttons to a pane, multiple items per row
	 */
	public void addButtons(Action [] aa, String label, int numItemPerRow)
	{
		JPanel pane = new JPanel();

		Border etch = BorderFactory.createEtchedBorder();
		pane.setBorder (BorderFactory.createTitledBorder (etch, label));
		
		pane.setBackground(Color.white);
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1.0;
		c.weighty = 1.0;

		int i=0;
		for(Action a : aa) {
			c.gridx = i%numItemPerRow;
			c.gridy = i/numItemPerRow;		

			final ImageButton button= new ImageButton(a);
			button.addActionListener(new ActionListener() { 
				  public void actionPerformed(ActionEvent e) {
					  button.setContentAreaFilled(false);
				  } 
			});
			pane.add(button,c);
			i++;
		}

		for(;i < numItemPerRow;i++){
			c.gridx = i;
			JButton dummy = new JButton();
			Dimension dim = new Dimension(25,0);
			dummy.setPreferredSize(dim);
			dummy.setContentAreaFilled(false);
			pane.add(dummy,c);
		}
		//add (pane);
		currentPane.setLayout (new BorderLayout());
		currentPane.add(pane,BorderLayout.NORTH);
		JPanel paneNext = new JPanel();
		currentPane.add(paneNext,BorderLayout.CENTER);
		currentPane = paneNext;		
	}
}
