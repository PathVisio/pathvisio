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
package gmmlVision;

import graphics.GmmlGraphics;
import graphics.GmmlSelectionBox;
import graphics.GmmlSelectionBox.SelectionEvent;
import graphics.GmmlSelectionBox.SelectionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import util.TableColumnResizer;
import data.*;

public class GmmlPropertyTable extends Composite implements GmmlListener, SelectionListener {
	public TableViewer tableViewer;
	CellEditor[] cellEditors = new CellEditor[2];
	TextCellEditor textEditor;
	ColorCellEditor colorEditor;
	ComboBoxCellEditor comboBoxEditor;
	
	private List<GmmlDataObject> dataObjects;
	
	private List<PropertyType> attributes;
	
	final static int TYPES_DIFF = ObjectType.MIN_VALID -1;
	final static Object VALUE_DIFF = new Object() {
		public boolean equals(Object o) { return false; }
		public String toString() { return "different values"; }
	};

	/**
	 * Add a {@link GmmlDataObject} to the list of objects of which 
	 * the properties are displayed
	 * @param o
	 */
	public void addGmmlDataObject(GmmlDataObject o) {
		if(!dataObjects.contains(o)) {
			if(dataObjects.add(o)) {
				o.addListener(this);
				refresh();
			}
		}
	}
	
	/**
	 * Remove a {@link GmmlDataObject} from the list of objects of which 
	 * the properties are displayed
	 * @param o
	 */
	public void removeGmmlDataObject(GmmlDataObject o) {
		if(dataObjects.remove(o)) {
			o.removeListener(this);
			refresh();
		}
	}
	
	/**
	 * Clear the list of objects of which the properties are displayed
	 */
	public void clearGmmlDataObjects() {
		for(GmmlDataObject o : dataObjects) o.removeListener(this);
		dataObjects.clear();
		refresh();
	}
	
	/**
	 * Refresh the table and attributes to display
	 */
	void refresh() {
		setAttributes();
		tableViewer.refresh();
	}
	
	int getAggregateType() {
		int type = TYPES_DIFF;
		for(int i = 0; i < dataObjects.size(); i++) {
			GmmlDataObject g = dataObjects.get(i);
			
			if(i != 0 && type != g.getObjectType()) return TYPES_DIFF;
			
			type = g.getObjectType();
		}
		return type;
	}
	
	Object getAggregateValue(PropertyType key) {
		Object value = VALUE_DIFF;
		for(int i = 0; i < dataObjects.size(); i++) {
			GmmlDataObject g = dataObjects.get(i);
			Object o = g.getProperty(key);
			if(i != 0 && (o == null || !o.equals(value))) return VALUE_DIFF;

			value = o;
		}
		return value;
	}
		
	/**
	 * Sets the attributes for the selected objects
	 * Only attributes that are present in all objects in the selection will be
	 * added to the attributes list and shown in the property table
	 */
	public void setAttributes ()
	{
		HashMap<PropertyType, Integer> master = new HashMap<PropertyType, Integer>();
		for (GmmlDataObject o : dataObjects)
		{
			for (PropertyType attr : o.getAttributes())
			{
				if (master.containsKey(attr))
				{
					master.put(attr, master.get(attr) + 1);
				}
				else
				{
					master.put(attr, 1);
				}
			}
		}
		attributes.clear();
		for (PropertyType attr : master.keySet())
		{
			if (master.get(attr) == dataObjects.size())
			{
				attributes.add(attr);
			}
		}
		// sortAttributes();
		Collections.sort (attributes);
		
//		System.out.println ("--------------");
//		for (String attr: attributes)
//		{
//			System.out.println(attr);
//		}
//		System.out.println ("--------------");
	}
	
//	void sortAttributes() {
//		Collections.sort(attributes, new Comparator() {
//			public int compare(Object o1, Object o2) {
//				return o1.ordinal() - o2.ordinal();
//			}
//		});
//	}

	final static String[] colNames = new String[] {"Property", "Value"};
				
	GmmlPropertyTable(Composite parent, int style)
	{
		super(parent, style);
		setLayout(new FillLayout());
		Table t = new Table(this, style);
		TableColumn tcName = new TableColumn(t, SWT.LEFT);
		TableColumn tcValue = new TableColumn(t, SWT.LEFT);
		tcName.setText(colNames[0]);
		tcValue.setText(colNames[1]);
		tcName.setWidth(80);
		tcValue.setWidth(70);
		tableViewer = new TableViewer(t);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.setContentProvider(tableContentProvider);
		tableViewer.setLabelProvider(tableLabelProvider);
		
		cellEditors[1] = cellEditors[0] = textEditor = new TextCellEditor(tableViewer.getTable());
		colorEditor = new ColorCellEditor(tableViewer.getTable());
		comboBoxEditor = new ComboBoxCellEditor(tableViewer.getTable(), new String[] {""});
		
		tableViewer.setCellEditors(cellEditors);
		tableViewer.setColumnProperties(colNames);
		tableViewer.setCellModifier(cellModifier);
		
		t.addControlListener(new TableColumnResizer(t, t.getParent()));
		
		dataObjects = new ArrayList<GmmlDataObject>();
		attributes = new ArrayList<PropertyType>();
		tableViewer.setInput(attributes);
		
		GmmlSelectionBox.addListener(this);
	}
	
	private CellEditor getCellEditor(Object element)
	{
		PropertyType key = (PropertyType)element;
		int type = key.type();
		switch(type)
		{
		case PropertyClass.FONT:
		case PropertyClass.GENETYPE:
		case PropertyClass.STRING:
		case PropertyClass.DOUBLE:
		case PropertyClass.INTEGER: 	return textEditor;
		case PropertyClass.COLOR: 	return colorEditor;
		case PropertyClass.LINETYPE:
			comboBoxEditor.setItems(new String[] {
					"Line", "Arrow", "TBar", "Receptor", "LigandSquare", 
					"ReceptorSquare", "LigandRound", "ReceptorRound"});
			return comboBoxEditor;
		case PropertyClass.SHAPETYPE:
			comboBoxEditor.setItems(new String[] {"Rectangle", "Oval", "Arc"});
			return comboBoxEditor;
		case PropertyClass.DATASOURCE:			
			comboBoxEditor.setItems(MappFormat.dataSources);
			return comboBoxEditor;
		case PropertyClass.ORIENTATION:
			comboBoxEditor.setItems(new String[] {"Top", "Right", "Bottom", "Left"});
			return comboBoxEditor;
		case PropertyClass.LINESTYLE:
			comboBoxEditor.setItems(new String[] {"Solid", "Dashed"});
			return comboBoxEditor;
		case PropertyClass.BOOLEAN:
			comboBoxEditor.setItems(new String[] {"false", "true"});
			return comboBoxEditor;
		}
		return textEditor;
	}
	
	private ICellModifier cellModifier = new ICellModifier()
	{
		public boolean canModify(Object element, String property) {
			if (!colNames[1].equals(property))
			{
				return false;
			}
			
			cellEditors[1] = getCellEditor(element);
			return true;
		}

		public Object getValue(Object element, String property) {
			PropertyType key = (PropertyType)element;
			Object value = getAggregateValue(key);
			
			switch(key.type())
			{
				case PropertyClass.DOUBLE:
				case PropertyClass.INTEGER: 
					return value.toString();
				case PropertyClass.STRING: 
					return value == null ? "" : (String)value;
				case PropertyClass.COLOR: 
					return (RGB)value;	
				case PropertyClass.DATASOURCE:
					return MappFormat.lDataSources.indexOf((String)value);					
				
				// for all combobox types:
				case PropertyClass.BOOLEAN:
					return (Boolean)value;
				case PropertyClass.LINETYPE:
				case PropertyClass.SHAPETYPE:
				case PropertyClass.ORIENTATION:
				case PropertyClass.LINESTYLE:
					return (Integer)value;
			}
			return null;
		}
		
		public void modify(Object element, String property, Object value) {
			PropertyType key = (PropertyType)((TableItem)element).getData();
			
			switch(key.type())
			{
			case PropertyClass.DOUBLE: 	
				try 
				{ 
					value = Double.parseDouble((String)value); 
					break; 
				} 
				catch(Exception e) 
				{ 
					GmmlVision.log.error("GmmlPropertyTable: Unable to parse double", e); 
					return; 
				}
			case PropertyClass.INTEGER: 	
				try 
				{ 
					value = Integer.parseInt((String)value); 
					break; 
				}
				catch(Exception e) 
				{ 
					GmmlVision.log.error("GmmlPropertyTable: Unable to parse int", e); 
					return; 
				}
			case PropertyClass.DATASOURCE:
				if((Integer)value == -1) return; //Nothing selected
				value = MappFormat.lDataSources.get((Integer)value);
				break;
//			case PropertyClass.BOOLEAN:
//				try 
//				{ 
//					value = Boolean.parseBoolean((String)value); 
//					break; 
//				}
//				catch(Exception e) 
//				{ 
//					GmmlVision.log.error("GmmlPropertyTable: Unable to parse boolean", e); 
//					return; 
//				}
			}
			
			for(GmmlDataObject o : dataObjects) {
				o.setProperty(key, value);
			}
			tableViewer.refresh();
			GmmlVision.getDrawing().redrawDirtyRect();
		}
	};
	
	private IStructuredContentProvider tableContentProvider = new ArrayContentProvider();
	
	private ITableLabelProvider tableLabelProvider = new ITableLabelProvider() {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			PropertyType key = (PropertyType)element;
			switch(columnIndex) {
				case 0:
					return key.desc();					
				case 1:
					//TODO: prettier labels for different value types
					if(attributes.contains(key))
					{
						Object value = getAggregateValue(key);
						return value == null ? null : value.toString();
					}
			}
			return null;
			}
		
		public void addListener(ILabelProviderListener listener) { }
		public void dispose() {}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) { }
	};

	public void gmmlObjectModified(GmmlEvent e) {
		tableViewer.refresh();
	}

	public void drawingEvent(SelectionEvent e) {
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			if(e.affectedObject instanceof GmmlGraphics)
				addGmmlDataObject(((GmmlGraphics)e.affectedObject).getGmmlData());
			break;
		case SelectionEvent.OBJECT_REMOVED:
			if(e.affectedObject instanceof GmmlGraphics)
				removeGmmlDataObject(((GmmlGraphics)e.affectedObject).getGmmlData());
			break;
		case SelectionEvent.SELECTION_CLEARED:
			 clearGmmlDataObjects();
			break;
		}
		
	}
}

