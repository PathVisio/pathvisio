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
package org.pathvisio.cytoscape;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.gui.swing.propertypanel.TypedProperty;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.view.VPathwayWrapperBase;

/**
 * Panel to configure attribute to GPML property mappings
 * @author thomas
 */
public class AttributeDefaultsPanel extends JPanel {
	String[] columnNames = new String[] { "Property", "Default value" };
	AttributeMapper mapper;
	
	JTable table;
	AttributeMapperTableModel tableModel;
	
	List<TypedProperty> properties = new ArrayList<TypedProperty>();
	
	public AttributeDefaultsPanel(AttributeMapper mapper) {
		setLayout(new BorderLayout());
		
		//Hack to make TypedProperty work
		Engine engine = Engine.init();
		engine.setWrapper(new VPathwayWrapperBase());
		engine.newPathway();
		SwingEngine swingEngine = SwingEngine.init(engine);
		
		this.mapper = mapper;
		tableModel = new AttributeMapperTableModel();
		
		//Get the property list
		PathwayElement dummyElement = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		for(PropertyType p : dummyElement.getAttributes(false)) {
			if(!mapper.isProtected(p)) {
				TypedProperty tp = new TypedProperty(swingEngine, p);
				Object value = mapper.getDefaultValue(p);
				if(value == null) {
					value = dummyElement.getProperty(p);
				}
				tp.setValue(value);
				properties.add(tp);
			}
		}
		
		table = new JTable(tableModel) {
			public TableCellRenderer getCellRenderer(int row, int column) {
				TableCellRenderer r = tableModel.getCellRenderer(row, column);
				return r == null ? super.getCellRenderer(row, column) : r;
			}

			public TableCellEditor getCellEditor(int row, int column) {
				TableCellEditor e = tableModel.getCellEditor(row, column);
				return e == null ? super.getCellEditor(row, column) : e;
			}
		};
		
		JEditorPane help = new JEditorPane("text/html", 
				"Default values will be used when exporting to GPML in case:<UL>" +
				"<LI>There is no attribute -> property mapping for the attribute to export" +
				"<LI>There is an attribute -> property mapping, but the attribute has no value" +
				"</UL>"
		);
		add(help, BorderLayout.PAGE_START);
		add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	class DummyPathwayElement extends PathwayElement {
		public DummyPathwayElement() {
			super(ObjectType.DATANODE);
		}
	}
	
	class AttributeMapperTableModel extends AbstractTableModel {
		public String getColumnName(int column) {
			return columnNames[column];
		}
		
		public int getColumnCount() {
			return 2;
		}
		
		public int getRowCount() {
			return properties.size();
		}
		
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			TypedProperty tp = properties.get(rowIndex);
			
			if(columnIndex == 0) {
				return tp.getType().desc();
			} else {
				return tp.getValue();
			}
		}
		
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			TypedProperty tp = properties.get(rowIndex);
			tp.setValue(value);
			mapper.setDefaultValue(tp.getType(), value);
		}
		
		public TableCellRenderer getCellRenderer(int row, int column) {
			if(column != 0) {
				TypedProperty tp = properties.get(row);
				if(tp != null) return tp.getCellRenderer();
			}
			return null;
		}

		public TableCellEditor getCellEditor(int row, int column) {
			if(column != 0) {
				TypedProperty tp = properties.get(row);
				if(tp != null) return tp.getCellEditor();
			}
			return null;
		}		
	}
}	
