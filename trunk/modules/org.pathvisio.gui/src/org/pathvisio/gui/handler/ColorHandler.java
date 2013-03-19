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
package org.pathvisio.gui.handler;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.model.StaticPropertyType;

/**
 * This class knows how to handle colors.  It renders and edits color values.
 * The color takes up the entire cell to standardize behavior across Look-and-Feels (due to a problem with Vista, see
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4880747 for details).
 *
 * @author Mark Woon
 */
public class ColorHandler extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, TypeHandler,
		ActionListener {
	private static final String EDIT_COMMAND = "edit";
	private JLabel renderer;
	private JButton editButton;
	private Color currentColor;
	private JColorChooser colorChooser;

	public ColorHandler() {
		renderer = new JLabel();
		renderer.setOpaque(true);
		editButton = new JButton();
		editButton.setActionCommand(EDIT_COMMAND);
		editButton.addActionListener(this);
		editButton.setBorderPainted(false);
		// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4880747 for why setContentAreaFilled(false) is necessary
		editButton.setContentAreaFilled(false);
		editButton.setOpaque(true);
	}


	//-- TypeHandler methods --//

	public PropertyType getType() {
		return StaticPropertyType.COLOR;
	}

	public TableCellRenderer getLabelRenderer() {
		return null;
	}

	public TableCellRenderer getValueRenderer() {
		return this;
	}

	public TableCellEditor getValueEditor() {
		return this;
	}


	//-- TableCellRenderer methods --//

	public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Color newColor = color != null ? (Color)color : Color.WHITE;
		renderer.setBackground(newColor);

		renderer.setToolTipText("RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", " +
				newColor.getBlue());
		return renderer;
	}


	//-- TableCellEditor methods --//

	public Object getCellEditorValue() {
		return currentColor;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		currentColor = (Color)value;
		editButton.setBackground(currentColor);
		return editButton;
	}


	//-- ActionListener methods --//

	public void actionPerformed(ActionEvent e) {

		if (EDIT_COMMAND.equals(e.getActionCommand())) {
			editButton.setBackground(currentColor);

			if(colorChooser == null) {
				colorChooser = new JColorChooser();
			}
			
			// new color is selected when user clicks ok
			ActionListener alOk = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Color newColor = colorChooser.getColor();
					if (newColor != null && !newColor.equals(currentColor)) {
						currentColor = newColor;
					}
				}
			};
			// nothing happens when the user clicks cancel
			ActionListener alCancel = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {}
			};
			
			JDialog dlg = JColorChooser.createDialog(editButton, "Choose a color", true, colorChooser, alOk, alCancel);
			colorChooser.setColor(currentColor);
			dlg.pack();
			dlg.setVisible(true);

			fireEditingStopped();  // make the renderer reappear
		}
	}
}
