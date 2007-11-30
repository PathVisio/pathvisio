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
package org.pathvisio.gui.swing.dialogs;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.data.DataSource;
import org.pathvisio.data.DataSources;
import org.pathvisio.model.PathwayElement;

public class DataNodeDialog extends PathwayElementDialog {
	private static final long serialVersionUID = 1L;
	
	public DataNodeDialog(PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		super(e, readonly, frame, "DataNode properties", locationComp);
	}

	JTextField symText;
	JTextField idText;
	JComboBox dbCombo;
			
	public void refresh() {
		super.refresh();
		symText.setText(getInput().getTextLabel());
		idText.setText(getInput().getGeneID());
		dbCombo.setSelectedItem(getInput().getDataSource());
		pack();
	}
	
	protected void addCustomTabs(JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		JLabel symLabel = new JLabel("Symbol");
		JLabel idLabel = new JLabel("Identifier");
		JLabel dbLabel = new JLabel("Database");
		symText = new JTextField();
		idText = new JTextField();
		dbCombo = new JComboBox(DataSources.dataSources);
		
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = c.ipady = 5;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		panel.add(symLabel, c);
		c.gridy = 1;
		panel.add(idLabel, c);
		c.gridy = 2;
		panel.add(dbLabel, c);
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(symText, c);
		c.gridy = 1;
		panel.add(idText, c);
		c.gridy = 2;
		panel.add(dbCombo, c);

		symText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { setText();	}
			public void insertUpdate(DocumentEvent e) {	setText(); }
			public void removeUpdate(DocumentEvent e) { setText(); }
			private void setText() {
				getInput().setTextLabel(symText.getText());
			}
		});
		
		idText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { setText();	}
			public void insertUpdate(DocumentEvent e) {	setText(); }
			public void removeUpdate(DocumentEvent e) { setText(); }
			private void setText() {
				getInput().setGeneID(idText.getText());
			}
		});
		
		dbCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getInput().setDataSource(DataSource.getByFullName(dbCombo.getSelectedItem().toString()));
			}
		});
		
		symText.setEnabled(!readonly);
		idText.setEnabled(!readonly);
		dbCombo.setEnabled(!readonly);
		
		parent.add("Annotation", panel);
		parent.setSelectedComponent(panel);
	}

}
