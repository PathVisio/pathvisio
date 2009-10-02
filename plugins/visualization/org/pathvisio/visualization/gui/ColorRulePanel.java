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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.gex.GexManager;
import org.pathvisio.gex.SimpleGex;
import org.pathvisio.util.swing.TextFieldUtils;
import org.pathvisio.visualization.colorset.ColorRule;
import org.pathvisio.visualization.colorset.Criterion;

/**
 * A panel for editing a color rule
 * 
 * This panel has a text field for showing the expression,
 * a label and a button for showing and selecting a color,
 * Two lists to help with entering sample names and operators
 */
public class ColorRulePanel extends JPanel 
{
	private JLabel colorLabel; 
	private ColorRule cr = null;
	private JTextField txtExpr;
	private JLabel errorMsg;
	private JList lstOperators;
	private JList lstSamples;
	private JButton btnColor;

	/**
	 * Called whenever the txtExpr textfield changes
	 * Only call this when cr is not null
	 */
	private void setExpresion()
	{
		List<String> sampleNames = gexManager.getCurrentGex().getSampleNames();
		String expr = txtExpr.getText();
		
		String error = cr.setExpression(expr, sampleNames);
		if (error == null)
		{
			errorMsg.setText("Rule logic OK");
			errorMsg.setForeground(Color.GREEN);
		}
		else
		{
			errorMsg.setText(error);
			errorMsg.setForeground(Color.RED);
		}
	}
	
	/**
	 * Set the colorRule that is currently being edited or created.
	 * This may be set to null, in which case the panel is disabled.
	 */
	public void setInput (ColorRule cr)
	{
		this.cr = cr;
		refresh();
	}
	
	/**
	 * Get the colorRule that is currently being edited or created.
	 */
	public ColorRule getInput ()
	{
		return cr;
	}
	
	/**
	 * Update the panel components based on the contents of the input.
	 * Called whenever a new input is set or the input is otherwise changed externally.
	 */
	private void refresh()
	{
		boolean active = (cr != null);
		if (active)
		{
			colorLabel.setBackground(cr.getColor ());
			txtExpr.setText (cr.getExpression());
		}
		else
		{
			colorLabel.setBackground(Color.BLACK);
			txtExpr.setText ("");
		}
		txtExpr.setEnabled(active);
		btnColor.setEnabled(active);
		lstOperators.setEnabled(active);
		lstSamples.setEnabled(active);
	}
	
	private final GexManager gexManager;
	
	ColorRulePanel (GexManager gexManager)
	{
		this.gexManager = gexManager;
		FormLayout layout = new FormLayout("4dlu, pref, 4dlu, pref, 4dlu", 
		"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");

		CellConstraints cc = new CellConstraints();
		setLayout(layout);

		txtExpr = new JTextField(40);
		txtExpr.getDocument().addDocumentListener(new DocumentListener ()
		{

			public void changedUpdate(DocumentEvent de) 
			{
				if (cr != null) setExpresion();
			}

			public void insertUpdate(DocumentEvent de) 
			{ 
				if (cr != null) setExpresion();				
			}

			public void removeUpdate(DocumentEvent de) 
			{
				if (cr != null) setExpresion();				
			}
		});
		
		add (new JLabel ("Rule logic: "), cc.xy (2,2));
		add (txtExpr, cc.xy (4, 2));
		
		lstOperators = new JList(Criterion.TOKENS);
		add (new JScrollPane (lstOperators), cc.xy (2,4));
		
		lstOperators.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent me) 
			{
				int selectedIndex = lstOperators.getSelectedIndex();
				if (selectedIndex >= 0)
				{
					TextFieldUtils.insertAtCursorWithSpace(txtExpr, Criterion.TOKENS[selectedIndex]);
				}
				txtExpr.requestFocus();
			}
		} );
		
		SimpleGex gex = gexManager.getCurrentGex();
		final List<String> sampleNames = gex.getSampleNames();
		lstSamples = new JList(sampleNames.toArray());

		lstSamples.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent me) 
			{
				int selectedIndex = lstSamples.getSelectedIndex();
				if (selectedIndex >= 0)
				{
					TextFieldUtils.insertAtCursorWithSpace(txtExpr, "[" + sampleNames.get(selectedIndex) + "]");
				}
				txtExpr.requestFocus();
			}
		} );
		add (new JScrollPane (lstSamples), cc.xy (4,4));
		
		errorMsg = new JLabel("Rule logic OK");
		add (errorMsg, cc.xyw (2, 6, 3));
		
		colorLabel = new JLabel(" ");
		add (colorLabel, cc.xy (2,8));
		colorLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		colorLabel.setOpaque(true);		
		
		btnColor = new JButton ("Color...");
		btnColor.addActionListener(new ActionListener () 
		{
			public void actionPerformed(ActionEvent ae) 
			{
				// sanity check, button should have been disabled when cr == null
				if (cr == null) throw new NullPointerException();
				Color newColor = 
					JColorChooser.showDialog(getTopLevelAncestor(), "Pick color", cr.getColor());
				colorLabel.setBackground(newColor);
				cr.setColor(newColor);
			}});
		
		add (btnColor, cc.xy (4, 8));
		
		refresh();
	}
}
