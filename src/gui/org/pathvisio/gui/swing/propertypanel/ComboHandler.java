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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.model.PropertyType;

/**
 * This class knows how to handle an enumeration.  It renders and edits enumerated values using a combobox.
 *
 * @author Mark Woon
 */
public class ComboHandler extends DefaultCellEditor implements TableCellRenderer, TypeHandler {
	private PropertyType type;
	private JComboBox renderer;
	private JComboBox editor;
	private boolean useIndex;
	private Map<Object, Object> label2value;
	private Map<Object, Object> value2label;


	public ComboHandler(PropertyType aType, List labels, boolean aUseIndex) {
		this(aType, labels.toArray(), aUseIndex);
	}

	public ComboHandler(PropertyType aType, Object[] labels, boolean aUseIndex) {
		super(new JComboBox(labels));
		editor = (JComboBox)getComponent();
		editor.setBorder(BorderFactory.createEmptyBorder());
		renderer = new JComboBox(labels);
		type = aType;
		useIndex = aUseIndex;
	}


	public ComboHandler(PropertyType aType, Object[] labels, Object[] values) {
		this(aType, labels, false);
		if (labels.length != values.length) {
			throw new IllegalArgumentException("Number of labels doesn't equal number of values");
		}
		label2value = new HashMap<Object, Object>();
		value2label = new HashMap<Object, Object>();
		for (int i = 0; i < labels.length; i++) {
			label2value.put(labels[i], values[i]);
			value2label.put(values[i], labels[i]);
		}
	}


	public void setEditable(boolean isEditable) {
		editor.setEditable(isEditable);
	}


	//-- TypeHandler methods --//

	public PropertyType getType() {
		return type;
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

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (isSelected) {
			renderer.setForeground(table.getSelectionForeground());
			renderer.setBackground(table.getSelectionBackground());
		} else {
			renderer.setForeground(table.getForeground());
			renderer.setBackground(table.getBackground());
		}

		if (value2label != null) {
			value = value2label.get(value);
		}
		if (useIndex) {
			renderer.setSelectedIndex((Integer)value);
		} else {
			renderer.setSelectedItem(value);
		}
		return renderer;
	}


	//-- TableCellEditor methods --//

	public Object getCellEditorValue() {

		if (label2value == null) {
			return useIndex ? editor.getSelectedIndex() : editor.getSelectedItem();
		} else {
			return label2value.get(editor.getSelectedItem());
		}
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value2label != null) {
			value = value2label.get(value);
		}
		if (useIndex) {
			editor.setSelectedIndex((Integer)value);
		} else {
			editor.setSelectedItem(value);
		}
		return editor;
	}
}
