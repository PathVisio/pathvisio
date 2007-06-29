package org.pathvisio.gui.swing.propertypanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.Engine;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.PathwayListener;
import org.pathvisio.model.PropertyType;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.SelectionBox;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

public class PathwayTableModel extends AbstractTableModel implements SelectionListener, PathwayListener {
	TableCellEditor defaultEditor = new DefaultCellEditor(new JTextField());
	
	Collection<PathwayElement> input;
	List<TypedProperty> properties;
	
	public PathwayTableModel() {
		input = new HashSet<PathwayElement>();
		properties = new ArrayList<TypedProperty>();
		SelectionBox.addListener(this);
	}
	
	private void clearInput() {
		for(PathwayElement e : input) {
			e.removeListener(this);
		}
		input.clear();
		refresh();
	}
	
	private void removeInput(PathwayElement pwElm) {
		input.remove(pwElm);
		pwElm.removeListener(this);
		refresh();
	}
	
	private void addInput(PathwayElement pwElm) {
		input.add(pwElm);
		pwElm.addListener(this);
		refresh();
	}
	
	protected void refresh() {
		properties = generateProperties(input);
		fireTableDataChanged();
	}
	
	protected List<TypedProperty> generateProperties(Collection<PathwayElement> elements) {
		List<TypedProperty> properties = new ArrayList<TypedProperty>();
		List<PropertyType> propTypes = getProperties(elements);
		for(PropertyType pt : propTypes) {
			TypedProperty value = getAggregateProperty(pt, elements);
			properties.add(value);
		}
		return properties;
	}
	
	protected List<PropertyType> getProperties(Collection<PathwayElement> elements) {
		ArrayList<PropertyType> properties = null;
		ArrayList<PropertyType> remove = new ArrayList<PropertyType>();
		for(PathwayElement e : elements) {
			if(properties == null) {
				properties = new ArrayList<PropertyType>();
				properties.addAll(e.getAttributes());
				continue;
			}
			remove.clear();
			List<PropertyType> attributes = e.getAttributes();
			for(PropertyType p : properties) {
				if(!attributes.contains(p)) {
					remove.add(p);
				}
			}
			properties.removeAll(remove);
		}
		return properties == null ? new ArrayList<PropertyType>() : properties;
	}
	
	TypedProperty getAggregateProperty(PropertyType key, Collection<PathwayElement> elements) {
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
		Engine.getActiveVPathway().redrawDirtyRect();
	}
	
	public String getColumnName(int column) {
		if(column == 0) return "Property";
		return "Value";
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}
		
	public void drawingEvent(SelectionEvent e) {
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
			System.out.println("Getting cell renderer" + tp);
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
}
