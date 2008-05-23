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

import javax.swing.*;

import org.pathvisio.visualization.colorset.*;
import org.pathvisio.visualization.colorset.ColorGradient.ColorValuePair;

/**
 * Panel for editing a color set, a combination of
 * rules and gradients
 */
public class ColorSetPanel extends JPanel 
{
	private static final long serialVersionUID = 1L;

	private ColorSet colorSet;
	
	ColorSetPanel (ColorSet cs)
	{
		setLayout (new BorderLayout());
		
		colorSet = cs;
		
		JPanel colorObjectsPanel = new JPanel();
		add (new JScrollPane(colorObjectsPanel), BorderLayout.CENTER);
		
		for (ColorSetObject cso : colorSet.getObjects())
		{
			colorObjectsPanel.add (createColorSetObjectPanel(cso));
		}
		
		JPanel btnPanel = new JPanel();
		btnPanel.add (new JButton ("Add Gradient"));
		btnPanel.add (new JButton ("Add Rule"));
		
		add (btnPanel, BorderLayout.SOUTH);
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
