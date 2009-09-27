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
package org.pathvisio.visualization.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathvisio.visualization.VisualizationMethod;

/**
 * Wraps around the configuration panel of a VisualizationMethod
 */
public class MethodPanel extends JPanel implements ActionListener {
	VisualizationMethod method;
	JPanel configPanel;
	JCheckBox checkBox;
	
	public MethodPanel(VisualizationMethod method) {
		this.method = method;
		
		JPanel top = new JPanel();
		FormLayout layout = new FormLayout(
				"pref, 4dlu, pref, 2dlu, pref",
				"pref"
		);
		top.setLayout(layout);

		checkBox = new JCheckBox();
		checkBox.addActionListener(this);
		JLabel nameLabel = new JLabel(method.getName() + ":");
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		CellConstraints cc = new CellConstraints();
		top.add(checkBox, cc.xy(1,1));
		top.add(nameLabel, cc.xy(3, 1));
		top.add(new JLabel(method.getDescription()), cc.xy(5, 1));
		
		setLayout(new FormLayout(
			"fill:pref:grow",
			"pref, 4dlu, pref"
		));
		
		add(top, cc.xy(1, 1));
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new FormLayout(
			"15dlu, fill:pref:grow",
			"pref"
		));
		configPanel = method.getConfigurationPanel();
		configPanel.setBorder(BorderFactory.createEtchedBorder());
		bottom.add(configPanel, cc.xy(2, 1));
		add(bottom, cc.xy(1, 3));
		
		//Initial values
		configPanel.setVisible(method.isActive());
		checkBox.setSelected(method.isActive());
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == checkBox) {
			method.setActive(checkBox.isSelected());
			if(method.isConfigurable()) {
				configPanel.setVisible(checkBox.isSelected());
				revalidate();
			}
		}
	}
}
