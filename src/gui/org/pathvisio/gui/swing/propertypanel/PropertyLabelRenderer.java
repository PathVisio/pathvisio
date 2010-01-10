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
package org.pathvisio.gui.swing.propertypanel;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.pathvisio.model.Property;

/**
 * The default label renderer for a Property, which will display a tooltip if that information is available.
 *
 * @author Mark Woon
 */
public class PropertyLabelRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int vColIndex) {

		PropertyView pv = ((PathwayTableModel)table.getModel()).getPropertyAt(rowIndex);
		String tooltip = ((Property)pv.getType()).getDescription();
		if (tooltip != null) {
			setToolTipText(tooltip);
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,  rowIndex, vColIndex);
	}
}
