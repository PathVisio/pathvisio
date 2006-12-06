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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
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
import data.GmmlDataObject;
import data.GmmlEvent;
import data.GmmlFormat;
import data.GmmlListener;
import data.MappFormat;
import data.ObjectType;

public class GmmlPropertyTable extends Composite implements GmmlListener, SelectionListener {
	public TableViewer tableViewer;
	CellEditor[] cellEditors = new CellEditor[2];
	TextCellEditor textEditor;
	ColorCellEditor colorEditor;
	ComboBoxCellEditor comboBoxEditor;
	
	private List<GmmlDataObject> dataObjects;
	
	private List<String> attributes;
	
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
	
	Object getAggregateValue(String key) {
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
		HashMap<String, Integer> master = new HashMap<String, Integer>();
		for (GmmlDataObject o : dataObjects)
		{
			for (String attr : o.getAttributes())
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
		for (String attr : master.keySet())
		{
			if (master.get(attr) == dataObjects.size())
			{
				attributes.add(attr);
			}
		}
		sortAttributes();
//		System.out.println ("--------------");
//		for (String attr: attributes)
//		{
//			System.out.println(attr);
//		}
//		System.out.println ("--------------");
	}
	
	void sortAttributes() {
		Collections.sort(attributes, new Comparator() {
			public int compare(Object o1, Object o2) {
				return totalAttributes.indexOf(o1) - totalAttributes.indexOf(o2);
			}
		});
	}

	final static String[] colNames = new String[] {"Property", "Value"};
	
	// Types
	final static int DOUBLE = 0;
	final static int INTEGER = 1;
	final static int TYPE = 2;
	final static int LINESTYLE = 3;
	final static int COLOR = 4;
	final static int STRING = 5;
	final static int ORIENTATION = 6;
	
	final static List<String> totalAttributes = GmmlDataObject.attributes;
	
	// TODO: this is nearly redundant with GmmlDataObject.attributes
	final static List<String> labelMappings = Arrays.asList(new String[] {
			
			// all
			"Notes", "Comment",

			// line, shape, brace, geneproduct, label
			"Color", 
			
			// shape, brace, geneproduct, label
			"Center X", "Center Y", "Width", "Height", 
			
			// shape
			"FillColor", "Shape Type", "Rotation", 
			
			// line
			"Start X", "Start Y", "End X", "End Y",			
			"Line Type", "Line Style",
			
			// brace
			"Orientation",
			
			// gene product
			"ID", "Data-Source (ID system)", "Gene Symbol", 
			"Link (xref)", "Backpage Header", "Type", 
			
			// label
			"Label Text", 
			"Font Name", "Font Weight", "Font Style", "Font Size",
			//mappinfo
			
			// mappinfo
			"MapInfo Name", "Organism", "MapInfo Data-Source",
			"Version", "Author", "Maintained-By", 
			"Email", "Last-modified", "Availability",
			"BoardWidth", "BoardHeight", "WindowWidth", "WindowHeight",

			// other
			"GraphId", "StartGraphRef", "EndGraphRef",
					
			"Transparent"

	});

	final static int[] attributeTypes = new int[] {
			
			// all
			STRING, STRING,

			// line, shape, brace, geneproduct, label
			COLOR, 
			
			// shape, brace, geneproduct, label
			DOUBLE, DOUBLE, DOUBLE, DOUBLE, 
			
			// shape
			TYPE, COLOR, TYPE, DOUBLE, 
			
			// line
			DOUBLE, DOUBLE, DOUBLE, DOUBLE,			
			TYPE, TYPE,
			
			// brace
			INTEGER,
			
			// gene product
			STRING, TYPE, STRING, 
			STRING, STRING, STRING, 
			
			// label
			STRING, 
			STRING, TYPE, TYPE, DOUBLE,
			
			// mappinfo
			STRING, STRING, STRING,
			STRING, STRING, STRING,
			STRING, STRING, STRING,
			DOUBLE, DOUBLE, DOUBLE, DOUBLE,
			
			//other
			STRING, STRING, STRING
	};
	
	Hashtable<String, Integer> typeMappings;
	
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
		
		typeMappings = new Hashtable<String, Integer>();
		for(int i = 0; i < totalAttributes.size(); i++)
		{
			typeMappings.put(totalAttributes.get(i), attributeTypes[i]);
		}
		
		dataObjects = new ArrayList<GmmlDataObject>();
		attributes = new ArrayList<String>();
		tableViewer.setInput(attributes);
		
		GmmlSelectionBox.addListener(this);
	}
	
	private CellEditor getCellEditor(Object element)
	{
		String key = (String)element;
		int type = (Integer)typeMappings.get(key);
		switch(type)
		{
		case STRING:
		case DOUBLE:
		case INTEGER: 	return textEditor;
		case COLOR: 	return colorEditor;
		case TYPE:
			String[] types = new String[] {""};
			int objType = getAggregateType();
			if (objType == ObjectType.LINE)
			{
				types = GmmlFormat.gmmlLineTypes.toArray(
						new String[GmmlFormat.gmmlLineTypes.size()]);
			}
			else if (objType == ObjectType.SHAPE)
			{
				types = new String[] {"Rectangle", "Oval", "Arc"};
			}
			else if (key.equals("GeneProduct-Data-Source"))
			{
				types = MappFormat.dataSources;
			}
			else
			{
				return textEditor;
			}
			comboBoxEditor.setItems(types);
			return comboBoxEditor;
		case ORIENTATION:
			comboBoxEditor.setItems(new String[] {"Top", "Right", "Bottom", "Left"});
			return comboBoxEditor;
		case LINESTYLE:
			comboBoxEditor.setItems(new String[] {"Solid", "Dashed"});
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
			String key = (String)element;
			Object value = getAggregateValue(key);
			
			int type = (Integer)typeMappings.get(key);
			switch(type)
			{
			case DOUBLE:
			case INTEGER: return value.toString();
			case STRING: return value == null ? "" : (String)value;
			case COLOR: return (RGB)value;
			case LINESTYLE:
			case ORIENTATION:
			case TYPE: 
				int t = getAggregateType();
				if (key.equals("GeneProduct-Data-Source"))
					return MappFormat.lDataSources.indexOf((String)value);
				else if (t == ObjectType.MAPPINFO || t == ObjectType.GENEPRODUCT)
					return (String)value;
				else
					return (Integer)value;
			}
			return null;
		}
		
		public void modify(Object element, String property, Object value) {
			String key = (String)((TableItem)element).getData();
			
			switch((Integer)typeMappings.get(key))
			{
			case DOUBLE: 	try { value = Double.parseDouble((String)value); break; } 
				catch(Exception e) { GmmlVision.log.error("GmmlPropertyTable: Unable to parse double", e); 
					return; }
			case INTEGER: 	try { value = Integer.parseInt((String)value); break; }
				catch(Exception e) { GmmlVision.log.error("GmmlPropertyTable: Unable to parse int", e); 
					return; }
			case TYPE:
				if(key.equals("GeneProduct-Data-Source")) {
					if((Integer)value == -1) return; //Nothing selected
					value = MappFormat.lDataSources.get((Integer)value);
				}
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
			String key = (String)element;
			switch(columnIndex) {
				case 0:
					if(totalAttributes.contains(key))
					{
						if(key.equals("Name"))
						{
							if(getAggregateType() == ObjectType.GENEPRODUCT)
							{
								return "Gene ID";
							}
						}
						return (String)labelMappings.get(totalAttributes.indexOf(key));
					}
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

