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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.model.PathwayElement;

/**
 * Dialog to modify label specific properties
 * @author thomas
 *
 */
public class LabelDialog extends PathwayElementDialog {
	JTextField text;
	
	protected LabelDialog(PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		super(e, readonly, frame, "Label properties", locationComp);
		text.requestFocus();
	}
	
	protected void refresh() {
		super.refresh();
		if(getInput() != null) {
			text.setText(getInput().getTextLabel());
		} else {
			text.setText("");
		}
	}
	
	protected void addCustomTabs(JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		//Search panel elements
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Text label:");
		text = new JTextField();

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = GridBagConstraints.RELATIVE;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0;
		panel.add(label, constraints);

		constraints.weightx = 1;
		panel.add(text, constraints);

		text.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				saveText();
			}
			public void insertUpdate(DocumentEvent e) {
				saveText();
			}
			public void removeUpdate(DocumentEvent e) {
				saveText();
			}
			private void saveText() {
				if(getInput() != null) getInput().setTextLabel(text.getText());
			}
		});
		text.setEnabled(!readonly);

		parent.add("Label text", panel);
		parent.setSelectedComponent(panel);
	}
}
