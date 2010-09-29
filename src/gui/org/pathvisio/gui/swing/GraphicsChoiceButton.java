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

import com.mammothsoftware.frwk.ddb.DropDownButton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Toggle drop-down button, intended to be used in the PathVisio toolbar.
 * <p>
 * This button consists of a regular icon button on the left, and a drop-down arrow on the right.
 * When the drop-down arrow is clicked, a popup menu is shown from which you can choose from a list
 * of possible actions.
 * <p>
 * Actions are added in groups with addButtons. Between groups you can add a heading with addLabel.
 * The action added first will be the initially selected action.
 */
public class GraphicsChoiceButton extends DropDownButton
{
	
	public GraphicsChoiceButton()
	{
		// set icon to null for now, we'll use the icon 
		// from the first action added with addButtons
		super(null);
	}
	
	private int numItemPerRow = 6;

	/**
	 * Set the number of actions per row in the pop-up.
	 * Default is 6.
	 */
	public void setNumItemsPerRow(int value)
	{
		numItemPerRow = value;
	}

	// remember if we already set an action
	private boolean noIconSet = true;
	
	/**
	 * Add a group of actions, which will be displayed in the pop-up.
	 * This can be invoked multiple times.
	 */
	public void addButtons(Action [] aa)
	{
		JPanel pane = new JPanel();
		pane.setBackground(Color.white);
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = c.NONE;
		c.weightx = 1.0;
		c.weighty = 1.0;
		
		final JPopupMenu popup = getPopupMenu();

		int i=0;
		for(final Action a : aa) {
			c.gridx = i % numItemPerRow;
			c.gridy = i / numItemPerRow;


			// clicking a button should cause the popupmenu disappear, any better way to do it?
			final ImageButton button = new ImageButton(a);
			button.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					button.setContentAreaFilled(false);
					popup.setVisible(false);
					Icon icon = (Icon)a.getValue(Action.SMALL_ICON);
					if (icon != null) 
					{
						setIcon(icon);
						setDirectAction(a);
					}
				} 
			});
			pane.add(button,c);
			i++;
		}

		// fill the rest spaces using dummy button when there are less than numItemPerRow items, any better way?
		for(;i < numItemPerRow;i++){
			c.gridx = i;
			JButton dummy = new JButton();
			Dimension dim = new Dimension(25,0);
			dummy.setPreferredSize(dim);
			dummy.setContentAreaFilled(false);
			pane.add(dummy,c);
		}
		
		addComponent(pane);
		
		if (noIconSet)
		{
			Action firstAction = aa[0];
			setIcon((Icon)firstAction.getValue(Action.SMALL_ICON));
			setDirectActionEnabled(true);
			setDirectAction(firstAction);
			noIconSet = false;
		}
		
	}

	/**
	 * add section label to the drop-down menu
	 */
	public void addLabel(String s)
	{
		JLabel title = new JLabel(s);
		title.setForeground(new Color(50,21,110));
		title.setFont(new Font("sansserif", Font.BOLD, 12));
		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(new Color(221,231,238));
		titlePanel.add(title);		
		addComponent(titlePanel);
	}

	/**
	 * add item buttons and section label to the drop-down menu
	 */
	public void addButtons(String label, Action [] aa){
		addLabel(label);
		addButtons(aa);
	}
}
