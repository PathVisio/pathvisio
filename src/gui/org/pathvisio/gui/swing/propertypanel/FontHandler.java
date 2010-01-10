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
import java.awt.GraphicsEnvironment;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.model.PropertyType;
import org.pathvisio.model.StaticPropertyType;
import org.pathvisio.util.swing.FontNameRenderer;

/**
 * This class knows how to handle fonts.  It renders and edits fonts.
 *
 * @author Mark Woon
 */
public class FontHandler extends DefaultCellEditor implements TableCellRenderer, TableCellEditor, TypeHandler {
	JLabel renderer;
	JComboBox editor;


	public FontHandler() {
		super(new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
		editor = (JComboBox)getComponent();
		editor.setRenderer(new FontNameRenderer());
		renderer = new JLabel();
	}


	//-- TypeHandler methods --//

	public PropertyType getType() {
		return StaticPropertyType.FONT;
	}

	public TableCellRenderer getLabelRenderer() {
		return this;
	}

	public TableCellRenderer getValueRenderer() {
		return this;
	}

	public TableCellEditor getValueEditor() {
		return this;
	}


	//-- TableCellRenderer methods --//

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		renderer.setText((String)value);
		renderer.setFont(FontNameRenderer.buildFont((String)value));
		return renderer;
	}


	//-- TableCellEditor methods --//

	public Object getCellEditorValue() {
		return editor.getSelectedItem();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		editor.setSelectedItem(value);
		return editor;
	}
}
