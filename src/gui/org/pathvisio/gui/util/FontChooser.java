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

package org.pathvisio.gui.util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.pathvisio.gui.dialogs.OkCancelDialog;

public class FontChooser extends OkCancelDialog {
	public static Font showDialog(Frame frame, Component locationComp, Font font) {
		FontChooser fc = new FontChooser(frame, locationComp, font);
		fc.setVisible(true);
		return fc.font;
	}

	Font font;
	JComboBox fontCombo;
	JComboBox styleCombo;
	JComboBox sizeCombo;
	JCheckBox boldCheck;
	JCheckBox italicCheck;

	public FontChooser(Frame frame, Component locationComp, Font font) {
		super(frame, "Select font", locationComp, true, true);
		this.font = font;
		if(font == null) {
			font = UIManager.getFont("Label.font");
		}

		fontCombo = new JComboBox(
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
		);
		fontCombo.setRenderer(new FontNameRenderer());
		fontCombo.setSelectedItem(font.getFamily());

		sizeCombo = new JComboBox(
				new Integer[] {
						4, 6, 7, 8, 10, 11, 12,
						14, 16, 18, 20, 24,36,48
				}
		);
		sizeCombo.setEditable(true);

		((JTextField)sizeCombo.getEditor().getEditorComponent()).setDocument(
				new IntegerDocument()
		);
		sizeCombo.setSelectedItem(font.getSize() + "");

		boldCheck = new JCheckBox();
		boldCheck.setSelected(font.isBold());
		italicCheck = new JCheckBox();
		italicCheck.setSelected(font.isItalic());

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
				"pref, 8dlu, pref:grow", ""
		));
		builder.setDefaultDialogBorder();
		builder.append("Font:", fontCombo);
		builder.nextLine();
		builder.append("Size:", sizeCombo);
		builder.nextLine();
		builder.append("Bold:", boldCheck);
		builder.nextLine();
		builder.append("Italic:", italicCheck);

		setDialogComponent(builder.getPanel());
		pack();
	}

	protected void okPressed() {
		String name = fontCombo.getSelectedItem().toString();
		int size = 8;
		try {
			size = Integer.parseInt(sizeCombo.getSelectedItem().toString());
		} catch(NumberFormatException e) {
			//Ignore, use default
		}
		int style = Font.PLAIN;
		if(boldCheck.isSelected()) {
			style |= Font.BOLD;
		}
		if(italicCheck.isSelected()) {
			style |= Font.ITALIC;
		}
		font = new Font(name, style, size);

		super.okPressed();
	}

	protected void cancelPressed() {
		font = null;
		super.cancelPressed();
	}

	private class IntegerDocument extends PlainDocument {
		int currentValue = 0;

		public int getValue() {
			return currentValue;
		}

		public void insertString(int offset, String string,
				AttributeSet attributes) throws BadLocationException {

			if (string == null) {
				return;
			} else {
				String newValue;
				int length = getLength();
				if (length == 0) {
					newValue = string;
				} else {
					String currentContent = getText(0, length);
					StringBuffer currentBuffer =
						new StringBuffer(currentContent);
					currentBuffer.insert(offset, string);
					newValue = currentBuffer.toString();
				}
				currentValue = checkInput(newValue, offset);
				super.insertString(offset, string, attributes);
			}
		}
		public void remove(int offset, int length)
		throws BadLocationException {
			int currentLength = getLength();
			String currentContent = getText(0, currentLength);
			String before = currentContent.substring(0, offset);
			String after = currentContent.substring(length+offset,
					currentLength);
			String newValue = before + after;
			currentValue = checkInput(newValue, offset);
			super.remove(offset, length);
		}
		public int checkInput(String proposedValue, int offset)
		throws BadLocationException {
			if (proposedValue.length() > 0) {
				try {
					int newValue = Integer.parseInt(proposedValue);
					return newValue;
				} catch (NumberFormatException e) {
					throw new BadLocationException(proposedValue, offset);
				}
			} else {
				return 0;
			}
		}
	}
}
