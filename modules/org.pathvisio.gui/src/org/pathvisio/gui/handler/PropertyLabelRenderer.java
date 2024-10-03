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

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The default label renderer for a Property, which will display a tooltip if that information is available.
 *
 * @author Mark Woon
 */
public class PropertyLabelRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int vColIndex) {

		PropertyView pv = ((PathwayTableModel)table.getModel()).getPropertyAt(rowIndex);
		String tooltip = pv.getDescription();
		if (tooltip != null) {
			setToolTipText(tooltip);
		}
		return super.getTableCellRendererComponent(table, pv.getName(), isSelected, hasFocus,  rowIndex, vColIndex);
	}
}
