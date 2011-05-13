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
package org.pathvisio.cytoscape;

import cytoscape.data.CyAttributes;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.pathvisio.core.model.StaticProperty;

/**
 * Panel to configure attribute to GPML property mappings
 * @author thomas
 */
public class AttributeMapperPanel extends JPanel {
	private static final String NONE = "<none>";

	String[] columnNames = new String[] { "Attribute", "Property" };
	AttributeMapper mapper;

	Map<String, StaticProperty> desc2prop = new HashMap<String, StaticProperty>();

	JTable table;
	AttributeMapperTableModel tableModel;

	String[] attributeNames;

	public AttributeMapperPanel(AttributeMapper mapper, CyAttributes attributes) {
		this.mapper = mapper;
		attributeNames = attributes.getAttributeNames();
		Arrays.sort(attributeNames);

		tableModel = new AttributeMapperTableModel();
		table = new JTable(tableModel);

		List<String> propNames = new ArrayList<String>();
		for(StaticProperty prop : StaticProperty.values()) {
			if(!mapper.isProtected(prop)) {
				desc2prop.put(prop.getDescription(), prop);
				propNames.add(prop.getDescription());
			}
		}
		propNames.add(NONE);

		Collections.sort(propNames);
		JComboBox combo = new JComboBox(propNames.toArray());
		table.setDefaultEditor(Object.class, new ComboBoxCellEditor(combo));

		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	/**
	 * 2-Column table model.
	 * Left column contains Cytoscape attributes, and is not editable
	 * Right column contains corresponding GPML properties
	 */
	class AttributeMapperTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getRowCount() {
			return attributeNames.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex == 0) {
				return attributeNames[rowIndex];
			} else {
				StaticProperty prop = mapper.getMapping(attributeNames[rowIndex]);
				if(prop != null) {
					return prop.getDescription();
				} else {
					return null;
				}
			}
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if(columnIndex == 1) {
				String attr = (String)getValueAt(rowIndex, 0);
				if(NONE.equals(attr)) {
					mapper.setAttributeToPropertyMapping(attr, null);
				} else {
					mapper.setAttributeToPropertyMapping(
							attr, desc2prop.get((String)value)
					);
				}
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}
	}
}
