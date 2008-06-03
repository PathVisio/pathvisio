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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.data.GexManager;
import org.pathvisio.data.SimpleGex;
import org.pathvisio.visualization.colorset.ColorRule;
import org.pathvisio.visualization.colorset.Criterion;
import org.pathvisio.visualization.gui.ColorSetPanel.ColorSetObjectPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel for editing a color rule
 */
public class ColorRulePanel extends ColorSetObjectPanel 
{
	private static final long serialVersionUID = 1L;

	private final ColorRule cr;
	private JTextField txtExpr;
	private JLabel errorMsg;
	
	private void setExpresion()
	{
		boolean ok = cr.getCriterion().setExpression(txtExpr.getText());
		if (ok)
		{
			errorMsg.setText("Expression OK");
			errorMsg.setForeground(Color.GREEN);
		}
		else
		{
			errorMsg.setText(cr.getCriterion().getParseException().getMessage());
			errorMsg.setForeground(Color.RED);
		}
	}
	
	ColorRulePanel (ColorRule cr)
	{
		this.cr = cr;
		
		FormLayout layout = new FormLayout("4dlu, pref, 4dlu, pref, 4dlu", 
		"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");

		CellConstraints cc = new CellConstraints();
		setLayout(layout);

		txtExpr = new JTextField(40);
		txtExpr.getDocument().addDocumentListener(new DocumentListener ()
		{

			public void changedUpdate(DocumentEvent de) 
			{
				setExpresion();
				
			}

			public void insertUpdate(DocumentEvent de) 
			{
				setExpresion();
			}

			public void removeUpdate(DocumentEvent de) 
			{
				setExpresion();
			}
		});
		
		add (new JLabel ("Expression: "), cc.xy (2,2));
		add (txtExpr, cc.xy (4, 2));
		
		
		final JList lstOperators = new JList(Criterion.tokens);
		add (new JScrollPane (lstOperators), cc.xy (2,4));
		
		lstOperators.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent me) 
			{
				int selectedIndex = lstOperators.getSelectedIndex();
				if (selectedIndex >= 0)
				{
					String expr = txtExpr.getText();
					txtExpr.setText (expr + " " + Criterion.tokens[selectedIndex]);
				}
				txtExpr.requestFocus();
			}
		} );
		
		SimpleGex gex = GexManager.getCurrent().getCurrentGex();
		final List<String> sampleNames = gex.getSampleNames();
		final JList lstSamples = new JList(sampleNames.toArray());

		lstSamples.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent me) 
			{
				int selectedIndex = lstSamples.getSelectedIndex();
				if (selectedIndex >= 0)
				{
					String expr = txtExpr.getText();
					txtExpr.setText (expr + " [" + sampleNames.get(selectedIndex) + "]");
				}
				txtExpr.requestFocus();
			}
		} );
		add (lstSamples, cc.xy (4,4));
		
		errorMsg = new JLabel("Expression OK");
		add (errorMsg, cc.xyw (2, 6, 3));
		txtExpr.setText (cr.getCriterion().getExpression());
		
		final JLabel colorLabel = new JLabel(" ");
		add (colorLabel, cc.xy (2,8));
		colorLabel.setBackground(cr.getColor());
		
		JButton btnColor = new JButton ("Color...");
		btnColor.addActionListener(new ActionListener () 
		{

			public void actionPerformed(ActionEvent ae) 
			{
				Color newColor = 
					JColorChooser.showDialog(getTopLevelAncestor(), "Pick color", ColorRulePanel.this.cr.getColor());
				colorLabel.setBackground(newColor);
				ColorRulePanel.this.cr.setColor(newColor);
			}});
		
		
		add (btnColor, cc.xy (4, 8));
	}
}
