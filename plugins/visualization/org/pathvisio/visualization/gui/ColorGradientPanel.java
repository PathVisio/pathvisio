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

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.visualization.colorset.ColorGradient;
import org.pathvisio.visualization.colorset.ColorGradient.ColorValuePair;

/**
 * A panel for selecting and editing color gradients
 */
public class ColorGradientPanel extends JPanel
{

	ColorGradient gradient;

	public ColorGradientPanel(ColorGradient gradient) {
		this.gradient = gradient;

		List<ColorValuePair> colors = gradient.getColorValuePairs();
		if(colors.size() > 0) {
			StringBuilder cspecs = new StringBuilder();
			String sep = ", pref:grow, ";

			for(int i = 0; i < colors.size(); ++i) {
				cspecs.append("20dlu" + sep);
			}
			setLayout(new FormLayout(
					cspecs.substring(0, cspecs.length() - sep.length()), 
					"pref"
			));

			CellConstraints cc = new CellConstraints();

			int i = 1;
			for(final ColorValuePair cvp : colors) {
				final JTextField txt = new JTextField("" + cvp.getValue());
				txt.setText("" + cvp.getValue());
				txt.getDocument().addDocumentListener(new DocumentListener() {
					void setValue() {
						String s = txt.getText();
						try {
							cvp.setValue(Double.parseDouble(s));
						} catch(NumberFormatException e) {
							//Ignore, don't set value
						}
					}
					public void changedUpdate(DocumentEvent e) {
						setValue();
					}
					public void insertUpdate(DocumentEvent e) {
						setValue();
					}
					public void removeUpdate(DocumentEvent e) {
						setValue();
					}
				});
				Border defaultBorder = txt.getBorder();
				txt.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(cvp.getColor()),
						defaultBorder
				));
				add(txt, cc.xy(i, 1));
				i += 2;
			}
		}
	}
}
