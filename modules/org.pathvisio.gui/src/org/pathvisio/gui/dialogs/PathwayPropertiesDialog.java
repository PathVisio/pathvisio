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
package org.pathvisio.gui.dialogs;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.bridgedb.bio.Organism;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.util.PermissiveComboBox;

/**
 * Dialog to easily edit the properties of a pathway, such as the pathway title, organism, etc.
 */
public class PathwayPropertiesDialog extends PathwayElementDialog {
	private PermissiveComboBox organismComboBox;
	private JTextField titleField; 
	
	protected PathwayPropertiesDialog(SwingEngine swingEngine, PathwayElement e,
			boolean readonly, Frame frame, String title, Component locationComp) {
		super(swingEngine, e, readonly, frame, "Pathway properties", locationComp);
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();
	}
	
	protected void addCustomTabs(JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		JPanel fieldPanel = new JPanel();
		fieldPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.weightx = 1;
		panelConstraints.weighty = 1;
		panel.add(fieldPanel, panelConstraints);

		fieldPanel.setLayout(new GridBagLayout());

		JLabel titleFieldLabel = new JLabel("Title");
		JLabel orgComboLabel = new JLabel ("Organism ");
		
		titleField = new JTextField();
		titleField.setText(swingEngine.getEngine().getActivePathway().getMappInfo().getMapInfoName());
		organismComboBox = new PermissiveComboBox(Organism.latinNamesArray());
		organismComboBox.setSelectedItem(swingEngine.getEngine().getActivePathway().getMappInfo().getOrganism());

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		fieldPanel.add(titleFieldLabel, c);
		fieldPanel.add(orgComboLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		fieldPanel.add(titleField, c);
		fieldPanel.add(organismComboBox, c);
				
		parent.add("Properties", panel);
		parent.setSelectedComponent(panel);
	}
	
	protected void okPressed() {
		super.okPressed();
		swingEngine.getEngine().getActivePathway().getMappInfo().setMapInfoName(titleField.getText());
		
		String itemSelectedFromDropDown = (String)organismComboBox.getSelectedItem();
		if(itemSelectedFromDropDown != null)
			swingEngine.getEngine().getActivePathway().getMappInfo().setOrganism(itemSelectedFromDropDown);
	}
}
