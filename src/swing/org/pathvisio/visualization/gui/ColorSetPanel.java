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
package org.pathvisio.visualization.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.pathvisio.debug.Logger;
import org.pathvisio.visualization.colorset.ColorGradient;
import org.pathvisio.visualization.colorset.ColorRule;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetObject;
import org.pathvisio.visualization.colorset.ColorGradient.ColorValuePair;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Panel for editing a color set, a combination of
 * rules and gradients
 */
public class ColorSetPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	static final String ACTION_GRADIENT = "Gradient:";
	static final String ACTION_ADD_RULE = "Add rule";
	static final String ACTION_REMOVE_RULE = "Remove rule";
	static final String ACTION_COMBO = "combo";
	
	private ColorSet colorSet;
	private ColorGradientCombo gradientCombo;
	private JCheckBox gradientCheck;
	private JPanel rulesPanel;
	private JPanel valuesPanel;
	
	private ColorGradient gradient;
	
	ColorSetPanel (ColorSet cs)
	{
		colorSet = cs;

		setLayout (new FormLayout(
				"pref:grow", 
				"pref, 4dlu, pref, 2dlu, fill:pref:grow, 2dlu, pref"
		));
		
		JPanel gradientPanel = new JPanel();
		
		CellConstraints cc = new CellConstraints();
		add(gradientPanel, cc.xy(1, 1));
		
		gradientPanel.setLayout(new FormLayout(
			"pref, 2dlu, pref:grow",
			"pref, pref, pref"
		));
		
		gradientCheck = new JCheckBox(ACTION_GRADIENT);
		gradientCheck.setActionCommand(ACTION_GRADIENT);
		gradientCheck.addActionListener(this);
		
		gradientCombo = new ColorGradientCombo();
		
		gradientCombo.setActionCommand(ACTION_COMBO);
		gradientCombo.addActionListener(this);

		gradientPanel.add(gradientCheck, cc.xy(1,1));
		gradientPanel.add(gradientCombo, cc.xy(3, 1));
		
		add(new JLabel("Rules:"), cc.xy(1, 3, "l, c"));
		rulesPanel = new JPanel();
		rulesPanel.setBorder(BorderFactory.createEtchedBorder());
		add(new JScrollPane(rulesPanel), cc.xy(1, 5));
		
		JButton add = new JButton(ACTION_ADD_RULE);
		add.setActionCommand(ACTION_ADD_RULE);
		add.addActionListener(this);
		JButton remove = new JButton(ACTION_REMOVE_RULE);
		remove.setActionCommand(ACTION_REMOVE_RULE);
		remove.addActionListener(this);
		
		JPanel btnPanel = ButtonBarFactory.buildAddRemoveBar(add, remove);
		add(btnPanel, cc.xy(1, 7));
		
		refresh();
	}

	private void refresh() {
		//Get default gradients
		List<ColorGradient> gradients = ColorGradient.createDefaultGradients();
		
		//Set gradients
		gradient = null;
		for(ColorSetObject cso : colorSet.getObjects()) {
			if(cso instanceof ColorGradient) {
				gradient = (ColorGradient)cso;
			}
		}
		if(gradient != null) {
			gradientCheck.setSelected(true);
			if(!gradients.contains(gradient)) {
				gradients.add(0, gradient);
			}
		} else {
			gradientCheck.setSelected(false);
		}
		gradientCombo.setGradients(gradients);
		gradientCombo.setSelectedItem(gradient);
		
		//Generate rules panel
		rulesPanel.removeAll();
		FormLayout layout = new FormLayout("fill:pref:grow");
		DefaultFormBuilder builder = 
			new DefaultFormBuilder(layout, rulesPanel);
		for (ColorSetObject cso : colorSet.getObjects()) {
			Logger.log.trace("Adding panel for " + cso);
			builder.append(createColorSetObjectPanel(cso));
			builder.nextLine();
		}
		revalidate();
	}
	
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		Logger.log.info(action);
		if(ACTION_ADD_RULE.equals(action)) {
			ColorSetObject cso = new ColorRule(colorSet);
			colorSet.addObject(cso);
			refresh();
		} else if(ACTION_REMOVE_RULE.equals(action)) {
			//TODO: remove
			JOptionPane.showMessageDialog(this, "Not implemented");
		} else if(ACTION_GRADIENT.equals(action)) {
			if(gradientCheck.isSelected()) {
				gradientCombo.setSelectedIndex(0);
			} else {
				gradientCombo.setSelectedIndex(-1);
			}
		} else if(ACTION_COMBO.equals(action)) {
			gradient = gradientCombo.getSelectedGradient();
			Logger.log.trace("" + gradient);
			gradientCheck.setSelected(gradient != null);
		}

	}
	
	private ColorSetObjectPanel createColorSetObjectPanel (ColorSetObject cso)
	{
		if (cso instanceof ColorRule)
		{
			return new ColorRulePanel ((ColorRule)cso);
		}
		return null;
	}
	
	private static class ColorSetObjectPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
	}
	private static class ColorRulePanel extends ColorSetObjectPanel
	{
		private static final long serialVersionUID = 1L;
		private ColorRule cr;
		
		ColorRulePanel (ColorRule cr)
		{
			this.cr = cr;
			add (new JLabel ("Expression: " + cr.getCriterion().getExpression()));
			add (new JLabel ("Color Rule Panel not implemented"));
		}
		
	}
}
