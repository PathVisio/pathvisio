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
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.data.DataSources;
import org.pathvisio.model.PathwayElement;

public class DataNodeDialog extends PathwayElementDialog {
	public DataNodeDialog(PathwayElement e, Frame frame, Component locationComp) {
		super(e, frame, "DataNode properties", locationComp);
	}

	JTextField symText;
	JTextField idText;
	JComboBox dbCombo;
			
	public void refresh() {
		symText.setText(getInput().getTextLabel());
		idText.setText(getInput().getGeneID());
		dbCombo.setSelectedItem(getInput().getDataSource());
		pack();
	}
	
	protected void createDialogContents(Container parent) {
		parent.setLayout(new GridLayout(3, 2));
		JLabel symLabel = new JLabel("Symbol");
		JLabel idLabel = new JLabel("Identifier");
		JLabel dbLabel = new JLabel("Database");
		symText = new JTextField();
		idText = new JTextField();
		dbCombo = new JComboBox(DataSources.dataSources);
		
		parent.add(symLabel);	parent.add(symText);
		parent.add(idLabel);	parent.add(idText);
		parent.add(dbLabel);	parent.add(dbCombo);
		
		symText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { setText();	}
			public void insertUpdate(DocumentEvent e) {	setText(); }
			public void removeUpdate(DocumentEvent e) { setText(); }
			private void setText() {
				getInput().setTextLabel(symText.getText());
			}
		});
	}
}
