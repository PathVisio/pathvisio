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

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.gex.GexManager;
import org.pathvisio.visualization.colorset.ColorGradient;
import org.pathvisio.visualization.colorset.ColorRule;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetEvent;
import org.pathvisio.visualization.colorset.ColorSetManager.ColorSetListener;
import org.pathvisio.visualization.colorset.ColorSetObject;

/**
 * Panel for editing a color set, a combination of
 * rules and/or a gradient
 */
public class ColorSetPanel extends JPanel implements ActionListener
{
	static final String ACTION_GRADIENT = "Gradient:";
	static final String ACTION_ADD_RULE = "Add rule";
	static final String ACTION_REMOVE_RULE = "Remove rule";
	static final String ACTION_COMBO = "combo";

	private ColorSet colorSet;
	private ColorGradientCombo gradientCombo;
	private JCheckBox gradientCheck;
	private ColorRulePanel rulesPanel;
	private ColorRuleTableModel crtm;
	private JPanel valuesPanel;
	private JPanel gradientPanel;

	private ColorGradient gradient;

	private JTable rulesTable;

	ColorSetPanel (ColorSet cs, GexManager gexManager)
	{
		colorSet = cs;

		setLayout (new FormLayout(
				"pref:grow",
				"pref, 3dlu, pref, 3dlu, [pref,100dlu], 3dlu, pref, 3dlu, pref"
		));

		gradientPanel = new JPanel();

		CellConstraints cc = new CellConstraints();
		add(gradientPanel, cc.xy(1, 1));

		gradientPanel.setLayout(new FormLayout(
			"pref, 3dlu, pref:grow",
			"pref, pref, pref"
		));

		gradientCheck = new JCheckBox(ACTION_GRADIENT);
		gradientCheck.setActionCommand(ACTION_GRADIENT);
		gradientCheck.addActionListener(this);

		gradientCombo = new ColorGradientCombo();

		gradientCombo.setActionCommand(ACTION_COMBO);
		gradientCombo.addActionListener(this);

		gradientPanel.add(gradientCheck, cc.xy(1,1));
		gradientPanel.add(gradientCombo, cc.xy(3, 1));

		add(new JLabel("Rules:"), cc.xy(1, 3, "l, c"));
		rulesPanel = new ColorRulePanel(gexManager);

		rulesPanel.setBorder(BorderFactory.createEtchedBorder());

		crtm = new ColorRuleTableModel(colorSet);
		rulesTable = new JTable(crtm);
		rulesTable.setDefaultRenderer(Color.class, new ColorRenderer(true));

		add(new JScrollPane(rulesTable), cc.xy(1, 5));

		add(new JScrollPane(rulesPanel), cc.xy(1, 7));

		JButton add = new JButton(ACTION_ADD_RULE);
		add.setActionCommand(ACTION_ADD_RULE);
		add.addActionListener(this);
		JButton remove = new JButton(ACTION_REMOVE_RULE);
		remove.setActionCommand(ACTION_REMOVE_RULE);
		remove.addActionListener(this);

		JPanel btnPanel = ButtonBarFactory.buildAddRemoveBar(add, remove);

		add(btnPanel, cc.xy(1, 9));

		refresh();
	}

	/** TableCellRenderer that displays a single RGB color */
	public class ColorRenderer extends JLabel implements TableCellRenderer
	{

		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ColorRenderer(boolean isBordered)
		{
			this.isBordered = isBordered;
			setOpaque(true); //MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(
		     JTable table, Object color,
		     boolean isSelected, boolean hasFocus,
		     int row, int column)
		{
			Color newColor = (Color)color;
			setBackground(newColor);
			if (isBordered)
			{
				if (isSelected)
				{
					if (selectedBorder == null)
					{
						selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
			                       table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				}
				else
				{
					if (unselectedBorder == null)
					{
						unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
			                       table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}

			setToolTipText("RGB value: " + newColor.getRed() + ", "
			              + newColor.getGreen() + ", "
			              + newColor.getBlue());
			return this;
		}
	}


	private static class ColorRuleTableModel extends AbstractTableModel implements ColorSetListener
	{
		private final ColorSet cs;
		private List<ColorRule> colorRules;

		ColorRuleTableModel (ColorSet cs)
		{
			this.cs = cs;
			cs.getColorSetManager().addListener(this);
			colorRules = cs.getColorRules();
		}

		ColorRule getRule (int index)
		{
			return colorRules.get (index);
		}

		public int getColumnCount() { return 2; }

		public int getRowCount()
		{
			return colorRules.size();
		}

		public Object getValueAt(int row, int col)
		{
			switch (col)
			{
			case 0:
				return colorRules.get(row).getColor();
			default:
				return "" + colorRules.get(row).getExpression();
			}
		}

		public String getColumnName (int col)
		{
			final String colNames[] = {"Color", "Rule" };
			return colNames[col];
		}

		public Class<?> getColumnClass (int col)
		{
			switch (col)
			{
			case 0:
				return Color.BLACK.getClass();
			default:
				return "".getClass();
			}
		}

		// triggered by changes in any color set
		// TODO: distinghuish changes in # of rows and changes in row data
		public void colorSetEvent(ColorSetEvent e)
		{
			fireTableDataChanged();
//			fireTableRowsUpdated(0, getRowCount());
		}
	}

	private void refresh() {
		//Get default gradients
		List<ColorGradient> gradients = ColorGradient.createDefaultGradients();

		//Set gradients
		gradient = colorSet.getGradient();
		if(gradient != null) {
			gradientCheck.setSelected(true);
			ColorGradient preset = null;
			for(ColorGradient cg : gradients) {
				if(cg.equalsPreset(gradient)) {
					preset = cg;
				}
			}
			gradients.remove(preset);
			gradients.add(gradient);
		} else {
			gradientCheck.setSelected(false);
		}
		gradientCombo.setGradients(gradients);
		gradientCombo.setSelectedGradient(gradient);

		//Refresh gradient values
		refreshValuesPanel();

		//Generate rules panel
		rulesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{

			public void valueChanged(ListSelectionEvent e)
			{
				int selected = rulesTable.getSelectedRow();
				rulesPanel.setInput (selected >= 0 ? crtm.getRule(selected) : null);
			}
		}
		);

		revalidate();
	}

	private void refreshValuesPanel() {
		if(valuesPanel != null) {
			gradientPanel.remove(valuesPanel);
		}
		if(gradient != null) {
			valuesPanel = new ColorGradientPanel(gradient);
			gradientPanel.add(valuesPanel, new CellConstraints().xy(3, 2));
		}
		revalidate();
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		Logger.log.info(action);
		if(ACTION_ADD_RULE.equals(action))
		{
			colorSet.addRule(new ColorRule());
			int selected = crtm.getRowCount() - 1;
			rulesTable.getSelectionModel().setSelectionInterval(selected, selected);
		}
		else if(ACTION_REMOVE_RULE.equals(action))
		{
			int lead = rulesTable.getSelectedRow();
			for (int index : rulesTable.getSelectedRows())
			{
				ColorRule cr = crtm.getRule(index);
				colorSet.removeRule(cr);
			}
			if (lead > crtm.getRowCount()) lead = crtm.getRowCount() - 1;
			rulesTable.getSelectionModel().setSelectionInterval (lead, lead);
		}
		else if(ACTION_GRADIENT.equals(action))
		{
			if(gradientCheck.isSelected()) {
				gradientCombo.setSelectedIndex(0);
			} else {
				gradientCombo.setSelectedIndex(-1);
			}
		}
		else if(ACTION_COMBO.equals(action))
		{
			gradient = gradientCombo.getSelectedGradient();
			colorSet.setGradient(gradient);
			if(gradient != null) {
				gradientCheck.setSelected(true);
			} else {
				gradientCheck.setSelected(false);
			}
			refreshValuesPanel();
		}
	}
}
