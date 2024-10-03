/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.gui.handler;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.model.StaticPropertyType;

/**
 * This class knows how to handle booleans.
 *
 * @author Mark Woon
 */
public class BooleanHandler extends DefaultCellEditor implements TableCellRenderer, TypeHandler {
	private JCheckBox renderer;


	/**
	 * Constructor.
	 */
	BooleanHandler() {
		super(new JCheckBox());
		((JCheckBox)getComponent()).setBorder(BorderFactory.createEmptyBorder());
		renderer = new JCheckBox();
	}


	//-- TypeHandler methods --//

	public PropertyType getType() {
		return StaticPropertyType.BOOLEAN;
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

	/**
	 * Overriden to format value.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (isSelected) {
			renderer.setForeground(table.getSelectionForeground());
			renderer.setBackground(table.getSelectionBackground());
		} else {
			renderer.setForeground(table.getForeground());
			renderer.setBackground(table.getBackground());
		}
		if (value instanceof Boolean) {
			renderer.setSelected((Boolean)value);
		} else {
			renderer.setSelected(Boolean.parseBoolean((String)value));
		}
		return renderer;
	}
}
