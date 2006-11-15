package gmmlVision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import data.GmmlDataObject;
import data.GmmlEvent;
import data.GmmlFormat;
import data.GmmlListener;
import data.MappFormat;
import data.ObjectType;

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

	/**
	 * This is for selecting multiple objects
	 * 
	 * TODO: currently not called anywhere. This needs
	 * to be called as objects are added to the selection.
	 */
	public void setGmmlDataObjects (List<GmmlDataObject> l)
	{
		HashMap<String, Integer> master = new HashMap<String, Integer>();
		for (GmmlDataObject o : l)
		{
			for (String attr : g.getAttributes())
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
		attributes = new ArrayList<String>();
		for (String attr : master.keySet())
		{
			if (master.get(attr) == l.size())
			{
				attributes.add(attr);
			}
		}
		System.out.println ("--------------");
		for (String attr: attributes)
		{
			System.out.println(attr);
		}
		System.out.println ("--------------");
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
			"Shape Type", "Rotation", 
			
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
			"Font Name","Font Weight","Font Style","Font Size",
			//mappinfo
			
			// mappinfo
			"MapInfo Name", "Organism", "MapInfo Data-Source",
			"Version", "Author", "Maintained-By", 
			"Email", "Last-modified", "Availability",
			"BoardWidth", "BoardHeight", "WindowWidth", "WindowHeight",

			// other
			"GraphId", "StartGraphRef", "EndGraphRef"

	});

	final static int[] attributeTypes = new int[] {
			
			// all
			STRING, STRING,

			// line, shape, brace, geneproduct, label
			COLOR, 
			
			// shape, brace, geneproduct, label
			DOUBLE, DOUBLE, DOUBLE, DOUBLE, 
			
			// shape
			TYPE, DOUBLE, 
			
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
			STRING, TYPE, TYPE, TYPE,
			
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
				types = GmmlFormat.gmmlLineTypes.toArray(
						new String[GmmlFormat.gmmlLineTypes.size()]);
			}
			else if (g.getObjectType() == ObjectType.SHAPE)
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
						return MappFormat.lDataSources.indexOf((String)value);
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
					value = MappFormat.lDataSources.get((Integer)value);
				}
			}
			
			g.setProperty(key, value);
			tableViewer.refresh();
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
						Object result = g.getProperty(key);
						if (result == null)
						{
							return "";
						}
						else
						{
							return result.toString();
						}
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
