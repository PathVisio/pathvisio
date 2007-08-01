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
package org.pathvisio.gui.swing.propertypanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.PathwayListener;
import org.pathvisio.model.PropertyType;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

public class PathwayTableModel extends AbstractTableModel implements SelectionListener, 
									PathwayListener, 
									ApplicationEventListener {
	TableCellEditor defaultEditor = new DefaultCellEditor(new JTextField());
	
	Collection<PathwayElement> input;
	List<TypedProperty> properties;
	
	public PathwayTableModel() {
		input = new HashSet<PathwayElement>();
		properties = new ArrayList<TypedProperty>();
		
		Engine.getCurrent().addApplicationEventListener(this);
		VPathway vp = Engine.getCurrent().getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);
	}
	
	private void clearInput() {
		for(PathwayElement e : input) {
			e.removeListener(this);
		}
		input.clear();
		refresh(true);
	}
	
	private void removeInput(PathwayElement pwElm) {
		input.remove(pwElm);
		pwElm.removeListener(this);
		refresh(true);
	}
	
	private void addInput(PathwayElement pwElm) {
		input.add(pwElm);
		pwElm.addListener(this);
		refresh(true);
	}
	
	protected void refresh(boolean recreate) {
		if(recreate) {
			properties = generateProperties(input);
		} else {
			refreshPropertyValues();
		}
		fireTableDataChanged();
	}
	
	protected void refresh() {
		refresh(false);
	}
	
	protected void refreshPropertyValues() {
		for(TypedProperty p : properties) {
			Object value = getAggregateValue(p.getType(), input);
			if(value instanceof TypedProperty) {
				p.setHasDifferentValues(true);
			} else {
				p.setValue(value, false);
			}
		}
	}
	
	protected List<TypedProperty> generateProperties(Collection<PathwayElement> elements) {
		List<TypedProperty> properties = new ArrayList<TypedProperty>();
		List<PropertyType> propTypes = getProperties(elements);
		for(PropertyType pt : propTypes) {
			TypedProperty value = getAggregateProperty(pt, elements);
			properties.add(value);
		}
		Collections.sort(properties);
		return properties;
	}
	
	protected List<PropertyType> getProperties(Collection<PathwayElement> elements) {
		ArrayList<PropertyType> properties = null;
		ArrayList<PropertyType> remove = new ArrayList<PropertyType>();
		for(PathwayElement e : elements) {
			if(properties == null) {
				properties = new ArrayList<PropertyType>();
				List<PropertyType> attr = e.getAttributes(GlobalPreference.getValueBoolean(GlobalPreference.SHOW_ADVANCED_ATTRIBUTES));
				properties.addAll(attr);
			}
			remove.clear();
			List<PropertyType> attributes = e.getAttributes(
					GlobalPreference.getValueBoolean(GlobalPreference.SHOW_ADVANCED_ATTRIBUTES));
			for(PropertyType p : properties) {
				if(!attributes.contains(p) || p.isHidden()) {
					remove.add(p);
				}
			}
			properties.removeAll(remove);
		}
		if(properties == null) properties = new ArrayList<PropertyType>();
		return properties;
	}
	
	/**
	 * Gets the aggregated value of the property of the given pathway element.
	 * If te values are different, this method returns an object of class TypedProperty
	 * @param key
	 * @param elements
	 * @return
	 */
	Object getAggregateValue(PropertyType key, Collection<PathwayElement> elements) {
		Object value = null;
		boolean first = true;
		for(PathwayElement e : elements) {
			Object o = e.getProperty(key);
			if(!first && (o == null || !o.equals(value))) {
				return new TypedProperty(elements, key);
			}
			value = o;
			first = false;
		}
		return value;
	}
	
	TypedProperty getAggregateProperty(PropertyType key, Collection<PathwayElement> elements) {
		Object value = getAggregateValue(key, elements);
		if(value instanceof TypedProperty) return (TypedProperty)value;
		return new TypedProperty(elements, value, key);
	}
		
	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return properties.size();
	}

	public TypedProperty getPropertyAt(int row) {
		return properties.get(row);
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		TypedProperty p = getPropertyAt(rowIndex);
		if(columnIndex == 0) return p.getType().desc();
		else return p.getValue();
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex != 0) {
			TypedProperty p = getPropertyAt(rowIndex);
			p.setValue(aValue);
		}
		Engine.getCurrent().getActiveVPathway().redrawDirtyRect();
	}
	
	public String getColumnName(int column) {
		if(column == 0) return "Property";
		return "Value";
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}
		
	public void selectionEvent(SelectionEvent e) {
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			if(e.affectedObject instanceof Graphics)
				addInput(((Graphics)e.affectedObject).getGmmlData());
			break;
		case SelectionEvent.OBJECT_REMOVED:
			if(e.affectedObject instanceof Graphics)
				removeInput(((Graphics)e.affectedObject).getGmmlData());
			break;
		case SelectionEvent.SELECTION_CLEARED:
			 clearInput();
			break;
		}		
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		if(column != 0) {
			TypedProperty tp = getPropertyAt(row);
			if(tp != null) return tp.getCellRenderer();
		}
		return null;
	}

	public TableCellEditor getCellEditor(int row, int column) {
		if(column != 0) {
			TypedProperty tp = getPropertyAt(row);
			if(tp != null) return tp.getCellEditor();
		}
		return null;
	}
	
	public void gmmlObjectModified(PathwayEvent e) {
		refresh();
	}

	public void applicationEvent(ApplicationEvent e) {
		if(e.type == ApplicationEvent.VPATHWAY_CREATED) {
			((VPathway)e.source).addSelectionListener(this);
		}
	}
}
