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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.pathvisio.debug.Logger;
import org.pathvisio.visualization.VisualizationMethod;
import org.pathvisio.visualization.colorset.ColorGradient;
import org.pathvisio.visualization.colorset.ColorRule;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetObject;
import org.pathvisio.visualization.colorset.ColorGradient.ColorValuePair;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Panel for editing a color set, a combination of
 * rules and gradients
 */
public class ColorSetPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	static final String ACTION_GRADIENT = "Add Gradient";
	static final String ACTION_RULE = "Add Rule";
	
	private ColorSet colorSet;
	private JPanel colorObjectsPanel;
	
	ColorSetPanel (ColorSet cs)
	{
		setLayout (new BorderLayout());
		
		colorSet = cs;
		
		colorObjectsPanel = new JPanel();
		add (new JScrollPane(colorObjectsPanel), BorderLayout.CENTER);
		
		JButton btnGradient = new JButton(ACTION_GRADIENT);
		btnGradient.setActionCommand(ACTION_GRADIENT);
		btnGradient.addActionListener(this);
		JButton btnRule = new JButton(ACTION_RULE);
		btnRule.setActionCommand(ACTION_RULE);
		btnRule.addActionListener(this);
		
		JPanel btnPanel = new JPanel();
		btnPanel.add (btnGradient);
		btnPanel.add (btnRule);
		
		add (btnPanel, BorderLayout.SOUTH);
		refresh();
	}

	private void refresh() {
		colorObjectsPanel.removeAll();
		FormLayout layout = new FormLayout("fill:pref:grow");
		DefaultFormBuilder builder = 
			new DefaultFormBuilder(layout, colorObjectsPanel);
		for (ColorSetObject cso : colorSet.getObjects()) {
			Logger.log.trace("Adding panel for " + cso);
			builder.append(createColorSetObjectPanel(cso));
			builder.nextLine();
		}
		revalidate();
	}
	
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		ColorSetObject cso = null;
		if(ACTION_GRADIENT.equals(action)) {
			cso = new ColorGradient(colorSet);
		} else {
			cso = new ColorRule(colorSet);
		}
		if(cso != null) {
			colorSet.addObject(cso);
			refresh();
		}
	}
	
	private ColorSetObjectPanel createColorSetObjectPanel (ColorSetObject cso)
	{
		if (cso instanceof ColorRule)
		{
			return new ColorRulePanel ((ColorRule)cso);
		}
		if (cso instanceof ColorGradient)
		{
			return new ColorGradientPanel ((ColorGradient)cso);
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
	
	private static class ColorGradientPanel extends ColorSetObjectPanel
	{
		private static final long serialVersionUID = 1L;
		ColorGradient cg;
		
		ColorGradientPanel (ColorGradient cg)
		{
			this.cg = cg;
			for (ColorValuePair cvp : cg.getColorValuePairs())
			{
				JLabel lbl = new JLabel();
				lbl.setOpaque(true);
				lbl.setBackground(cvp.getColor());
				lbl.setText ("" + cvp.getValue());
				add (lbl);
			}
			add (new JLabel ("Color Gradient Panel not implemented"));
		}
	}
}
