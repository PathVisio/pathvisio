import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class GmmlPropertyTable {
	TableViewer tableViewer;
	CellEditor[] cellEditors = new CellEditor[2];
	TextCellEditor textEditor;
	ColorCellEditor colorEditor;
	ComboBoxCellEditor comboBoxEditor;
	
	GmmlGraphics g;
	
	final static String[] colNames = new String[] {"Property", "Value"};
	
	// Types
	final static int DOUBLE = 0;
	final static int INTEGER = 1;
	final static int TYPE = 2;
	final static int LINESTYLE = 3;
	final static int COLOR = 4;
	final static int STRING = 5;
	
	// Type mappings
	final static String[] attributes = new String[] {
		"CenterX", "CenterY", "StartX", "StartY", "EndX", "EndY", "Width", "Height", 
		"Color", "Style", "Type", "Rotation", "Orientation", "PicPointOffset",
		"GeneID", "Xref", "TextLabel", "FontName", "FontWeight", "FontStyle", "FontSize"
	};
	final static int[] attributeTypes = new int[] {
		DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, 
		COLOR, LINESTYLE, TYPE, DOUBLE, INTEGER, DOUBLE,
		STRING, STRING, STRING, STRING, STRING, STRING, INTEGER
	};
	
	Hashtable typeMappings;
	
	GmmlPropertyTable(Composite parent, int style)
	{
		Table t = new Table(parent, style);
		TableColumn tcName = new TableColumn(t, SWT.LEFT);
		TableColumn tcValue = new TableColumn(t, SWT.LEFT);
		tcName.setText(colNames[0]);
		tcValue.setText(colNames[1]);
		tcName.setWidth(80);
		tcValue.setWidth(80);
		tableViewer = new TableViewer(t);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer.setContentProvider(tableContentProvider);
		tableViewer.setLabelProvider(tableLabelProvider);
		
		cellEditors[1] = cellEditors[0] = textEditor = new TextCellEditor(tableViewer.getTable());
		colorEditor = new ColorCellEditor(tableViewer.getTable());
		comboBoxEditor = new ComboBoxCellEditor(tableViewer.getTable(), new String[] {""});
		
		tableViewer.setCellEditors(cellEditors);
		tableViewer.setColumnProperties(colNames);
		tableViewer.setCellModifier(cellModifier);
		
		typeMappings = new Hashtable();
		for(int i = 0; i < attributes.length; i++)
		{
			typeMappings.put(attributes[i], attributeTypes[i]);
		}
	}
	
	public void setGraphics(GmmlGraphics g)
	{
		this.g = g;
	}
	
	private CellEditor getCellEditor(Object element)
	{
		int type = (Integer)typeMappings.get((String)element);
		switch(type)
		{
		case STRING:
		case DOUBLE:
		case INTEGER: 	return textEditor;
		case COLOR: 	return colorEditor;
		case TYPE:
			String[] types = new String[] {""};
			if (g instanceof GmmlLine)
			{
				types = new String[] {"Line", "Arrow"};
			}
			else if (g instanceof GmmlLineShape)
			{
				types = new String[] {"TBar", "Receptor round",
						"Ligand round", "Receptor square", "Ligand square"};
			}
			else if (g instanceof GmmlShape)
			{
				types = new String[] {"Rectangle", "Oval"};
			}
			comboBoxEditor.setItems(types);
			return comboBoxEditor;
		case LINESTYLE:
			comboBoxEditor.setItems(types = new String[] {"Solid", "Dashed"});
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
			if(g.propItems.containsKey(key))
			{
				Object value = g.propItems.get(key);
				switch((Integer)typeMappings.get(key))
				{
				case DOUBLE:
				case INTEGER: return value.toString();
				case STRING: return (String)value;
				case COLOR: return (RGB)value;
				case LINESTYLE:
				case TYPE: return (Integer)value;
				}
			}
			return null;
		}
		
		public void modify(Object element, String property, Object value) {
			String key = (String)((TableItem)element).getData();
			
			switch((Integer)typeMappings.get(key))
			{
			case DOUBLE: 	value = Double.parseDouble((String)value); break;
			case INTEGER: 	value = Integer.parseInt((String)value); break;
			}
			
			g.propItems.put(key, value);
			g.updateFromPropItems();
			tableViewer.refresh();
		}
	};
	
	private IStructuredContentProvider tableContentProvider = new IStructuredContentProvider()
	{
		public Object[] getElements(Object inputElement) {
			g.propItems = (Hashtable)inputElement;
			Hashtable m = (Hashtable)inputElement;
			return m.keySet().toArray();
		}
		
		public void dispose() { }
		
		public void inputChanged(
				Viewer viewer,
				Object oldInput,
				Object newInput) { }
	};
	
	private ITableLabelProvider tableLabelProvider = new ITableLabelProvider() {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			String key = (String)element;
			switch(columnIndex) {
				case 0:
					return key;
				case 1:
					//TODO: prettier labels for different value types
					if(g.propItems.containsKey(key))
					{
						return g.propItems.get(key).toString();
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
}
