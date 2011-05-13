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
package org.pathvisio.gui.dialogs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.util.FontChooser;

/**
 * Dialog to modify label specific properties
 * @author thomas
 *
 */
public class LabelDialog extends PathwayElementDialog {
	JTextArea text;
	JLabel fontPreview;

	protected LabelDialog(SwingEngine swingEngine, PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		super(swingEngine, e, readonly, frame, "Label properties", locationComp);
		text.requestFocus();
	}

	protected void refresh() {
		super.refresh();
		if(getInput() != null) {
			PathwayElement input = getInput();
			text.setText(input.getTextLabel());
			int style = input.isBold() ? Font.BOLD : Font.PLAIN;
			style |= input.isItalic() ? Font.ITALIC : Font.PLAIN;
			Font f = new Font(
					input.getFontName(), style, (int)(input.getMFontSize())
			);
			fontPreview.setFont(f);
			fontPreview.setText(f.getName());
		} else {
			text.setText("");
			fontPreview.setText("");
		}
	}

	protected void addCustomTabs(JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		//Search panel elements
		panel.setLayout(new FormLayout(
			"4dlu, pref, 4dlu, pref, 4dlu, pref, pref:grow, 4dlu",
			"4dlu, pref, 4dlu, fill:pref:grow, 4dlu, pref, 4dlu"
		));

		JLabel label = new JLabel("Text label:");
		text = new JTextArea();

		fontPreview = new JLabel(getFont().getFamily());

		final JButton font = new JButton("...");
		font.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font f = FontChooser.showDialog(null, (Component)e.getSource(), fontPreview.getFont());
				if(f != null) {
					if(input != null) {
						input.setFontName(f.getFamily());
						input.setBold(f.isBold());
						input.setItalic(f.isItalic());
						input.setMFontSize(f.getSize());
						fontPreview.setText(f.getFamily());
						fontPreview.setFont(f);
					}
				}
			}
		});

		CellConstraints cc = new CellConstraints();
		panel.add(label, cc.xy(2, 2));
		panel.add(new JScrollPane(text), cc.xyw(2, 4, 6));
		panel.add(new JLabel("Font:"), cc.xy(2, 6));
		panel.add(fontPreview, cc.xy(4, 6));
		panel.add(font, cc.xy(6, 6));

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
