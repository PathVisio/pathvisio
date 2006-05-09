package gmmlVision;

import graphics.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;


public class GmmlPropertyTable {
	public TableViewer tableViewer;
	CellEditor[] cellEditors = new CellEditor[2];
	TextCellEditor textEditor;
	ColorCellEditor colorEditor;
	ComboBoxCellEditor comboBoxEditor;
	
	public GmmlGraphics g;

	final static String[] colNames = new String[] {"Property", "Value"};
	
	// Types
	final static int DOUBLE = 0;
	final static int INTEGER = 1;
	final static int TYPE = 2;
	final static int LINESTYLE = 3;
	final static int COLOR = 4;
	final static int STRING = 5;
	final static int ORIENTATION = 6;
	
	// Type mappings
	final static List<String> attributes = Arrays.asList(new String[] {
			"CenterX", "CenterY", "StartX", "StartY", "EndX", "EndY", "Width", "Height", 
			"Color", "Style", "Type", "Rotation", "Orientation", "PicPointOffset",
			"GeneID", "Xref", "TextLabel", "FontName", "FontWeight", "FontStyle", "FontSize",
			"Name", "Organism", "Data-Source", "Version", "Author", "Maintained-By", "Email",
			"Availability", "Last-Modified", "Notes", "BackPageHead", "GeneProduct-Data-Source",
			"BoardWidth", "BoardHeight", "WindowWidth", "WindowHeight"
	});
	
	final static List labelMappings = Arrays.asList(new String[] {
			"Center X", "Center Y", "Start X", "Start Y", "End X", "End Y", "Width", "Height", 
			"Color", "Style", "Type", "Rotation", "Orientation", "Pic point offset",
			"Gene label", "Link (xref)", "Label text", "Font name", "Font weight", "Font style", "Font size",
			"Name", "Organism", "Data source", "Version", "Author", "Maintained by", "E-mail",
			"Availability", "Last modified", "Notes", "Backpage header", "System",
			"Board Width", "Board Height", "Window Width", "Window Height"
	});

	final static int[] attributeTypes = new int[] {
		DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, 
		COLOR, LINESTYLE, TYPE, DOUBLE, ORIENTATION, DOUBLE,
		STRING, STRING, STRING, STRING, STRING, STRING, INTEGER,STRING, 
		STRING, STRING, STRING, STRING, STRING, STRING, STRING, STRING,
		STRING, STRING, TYPE, INTEGER, INTEGER, INTEGER, INTEGER
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
		tcValue.setWidth(70);
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
		for(int i = 0; i < attributes.size(); i++)
		{
			typeMappings.put(attributes.get(i), attributeTypes[i]);
		}
	}
	
	public void setGraphics(GmmlGraphics g)
	{
		this.g = g;
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
			else if (key.equals("GeneProduct-Data-Source"))
			{
				types = (String[])GmmlGeneProduct.dataSources.toArray();
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
				case ORIENTATION:
				case TYPE: 
					if (key.equals("GeneProduct-Data-Source"))
						return GmmlGeneProduct.dataSources.indexOf((String)value);
					else if (g instanceof GmmlMappInfo || g instanceof GmmlGeneProduct)
						return (String)value;
					else
						return (Integer)value;
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
			case TYPE:
				if(key.equals("GeneProduct-Data-Source"))
					value = (String)GmmlGeneProduct.dataSources.get((Integer)value);
			}
			
			g.propItems.put(key, value);
			g.updateFromPropItems();
			tableViewer.refresh();
		}
	};
	
	private IStructuredContentProvider tableContentProvider = new IStructuredContentProvider()
	{
		public Object[] getElements(Object inputElement) {
			if(inputElement != null)
			{
				g = (GmmlGraphics)inputElement;
				return g.getAttributes().toArray();
			}
			return null;
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
					if(attributes.contains(key))
					{
						if(key.equals("Name"))
						{
							if(g instanceof GmmlGeneProduct)
							{
								return "Gene ID";
							}
						}
						return (String)labelMappings.get(attributes.indexOf(key));
					}
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
