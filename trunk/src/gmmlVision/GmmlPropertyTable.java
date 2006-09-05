package gmmlVision;

import graphics.*;
import data.*;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import util.TableColumnResizer;
import data.GmmlData;

public class GmmlPropertyTable extends Composite implements GmmlListener {
	public TableViewer tableViewer;
	CellEditor[] cellEditors = new CellEditor[2];
	TextCellEditor textEditor;
	ColorCellEditor colorEditor;
	ComboBoxCellEditor comboBoxEditor;
	
	private GmmlDataObject g = null;
	private List<String> attributes;
	
	public GmmlDataObject getGmmlDataObject ()
	{
		return g;
	}
	
	public void setGmmlDataObject (GmmlDataObject o)
	{
		if (o != g)
		{
			if (g != null) { g.removeListener(this); }
			g = o;
			attributes = g.getAttributes();
			tableViewer.setInput(g);			
			tableViewer.refresh();
			g.addListener(this);
		}
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
	
	// Type mappings
	final static List<String> totalAttributes = Arrays.asList(new String[] {
			"CenterX", "CenterY", "StartX", "StartY", "EndX", "EndY", "Width", "Height", 
			"Color", "LineStyle", "Type", "Rotation", "Orientation", "PicPointOffset",
			"GeneID", "Xref", "TextLabel", "FontName", "FontWeight", "FontStyle", "FontSize",
			"Name", "Organism", "Data-Source", "Version", "Author", "Maintained-By", "Email",
			"Availability", "Last-Modified", "Notes", "BackpageHead", "GeneProduct-Data-Source",
			"BoardWidth", "BoardHeight", "WindowWidth", "WindowHeight", "MapInfoLeft", "MapInfoTop",
			"Comment", "LineType"
	});
	
	final static List labelMappings = Arrays.asList(new String[] {
			"Center X", "Center Y", "Start X", "Start Y", "End X", "End Y", "Width", "Height", 
			"Color", "Line Style", "Type", "Rotation", "Orientation", "Pic point offset",
			"Gene label", "Link (xref)", "Label text", "Font name", "Font weight", "Font style", "Font size",
			"Name", "Organism", "Data source", "Version", "Author", "Maintained by", "E-mail",
			"Availability", "Last modified", "Notes", "Backpage header", "System",
			"Board Width", "Board Height", "Window Width", "Window Height", "Location X", "Location Y",
			"Comment", "Line Type"
	});

	final static int[] attributeTypes = new int[] {
		DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, 
		COLOR, LINESTYLE, TYPE, DOUBLE, ORIENTATION, DOUBLE,
		STRING, STRING, STRING, STRING, STRING, STRING, INTEGER, STRING, 
		STRING, STRING, STRING, STRING, STRING, STRING, STRING, STRING,
		STRING, STRING, TYPE, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER,
		STRING, TYPE, TYPE
	};
	
	//System names converted to arraylist for easy index lookup
	final static List<String> systemNames = Arrays.asList(GmmlData.systemNames);
	
	Hashtable typeMappings;
	
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
		
		typeMappings = new Hashtable();
		for(int i = 0; i < totalAttributes.size(); i++)
		{
			typeMappings.put(totalAttributes.get(i), attributeTypes[i]);
		}
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
			if (g.getObjectType() == ObjectType.LINE)
			{
				types = new String[] {"Line", "Arrow", "TBar", "Receptor round",
						"Ligand round", "Receptor square", "Ligand square"};
			}
			else if (g.getObjectType() == ObjectType.SHAPE)
			{
				types = new String[] {"Rectangle", "Oval", "Arc"};
			}
			else if (key.equals("GeneProduct-Data-Source"))
			{
				types = (String[])systemNames.toArray();
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
//			if(g.propItems.containsKey(key))
//			{
				Object value = g.getProperty(key);
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
						return systemNames.indexOf((String)value);
					else if (g.getObjectType() == ObjectType.MAPPINFO || g.getObjectType() == ObjectType.GENEPRODUCT)
						return (String)value;
					else
						return (Integer)value;
				}
//			}
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
					value = (String)systemNames.get((Integer)value);
				}
			}
			
			g.setProperty(key, value);			
		}
	};
	
	private IStructuredContentProvider tableContentProvider = new IStructuredContentProvider()
	{
		public Object[] getElements(Object inputElement) {
			if(inputElement != null)
			{
				g = (GmmlDataObject)inputElement;
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
					if(totalAttributes.contains(key))
					{
						if(key.equals("Name"))
						{
							if(g.getObjectType() == ObjectType.GENEPRODUCT)
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
						return g.getProperty(key).toString();
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
		GmmlVision.drawing.redrawDirtyRect();
	}
}
