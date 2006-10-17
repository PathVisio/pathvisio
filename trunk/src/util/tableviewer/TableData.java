package util.tableviewer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class holds a collection of {@link Row}s sharing the same {@link Column}s
 */
public class TableData {
	private ArrayList<Column> columnTemp;
	
	private ArrayList<Row> rows;
	
	public TableData() {
		columnTemp = new ArrayList<Column>();
		rows = new ArrayList<Row>();
	}
	
	public ArrayList<Row> getResults() { return rows; }
	public ArrayList<String> getAttributeNames() { 
		return getAttributeNames(false); 
	}
	
	public ArrayList<String> getAttributeNames(boolean showHidden) {
		ArrayList<String> attrNames = new ArrayList<String>();
		for(Column at : columnTemp) {
			if(!showHidden) { if(at.isVisible()) attrNames.add(at.name); }
			else attrNames.add(at.name);
		}
		return attrNames;
	}
	
	public void addAttribute(String name, int type) {
		columnTemp.add(new Column(name, type, true));
	}
	public void addAttribute(String name, int type, boolean visible) {
		columnTemp.add(new Column(name, type, visible));
	}
	
	public void addResult(Row rs) { rows.add(rs); }
	
	/**
	 * This class contains a single result from a search
	 */
	public class Row {
		private HashMap<String, Column> attributes;
		
		public Row() { 
			attributes = new HashMap<String, Column>();
			for(Column at : columnTemp) {
				attributes.put(at.getName(),
						new Column(at.getName(), at.type, at.isVisible()));
			}
			rows.add(this);
		}
		
		public void setAttribute(String name, String value) {
			if(attributes.containsKey(name)) attributes.get(name).setText(value);
		}
		
		public void setAttribute(String name, double value) {
			if(attributes.containsKey(name)) attributes.get(name).setNumeric(value);
		}
		
		public void setAttribute(String name, ArrayList value) {
			if(attributes.containsKey(name)) attributes.get(name).setArray(value);
		}
		
		public Column getAttribute(String name) throws Exception {
			if(attributes.containsKey(name)) return attributes.get(name);
			throw new Exception("Attribute " + name + " does not exist");
		}
		
		public ArrayList<Column> getAttributes(boolean onlyVisible) {
			ArrayList<Column> attr = new ArrayList<Column>();
			for(Column at : columnTemp) {
				Column atr = attributes.get(at.getName());
				if(atr.isVisible()) attr.add(atr);
			}
			return attr;
		}
	}
	
	/**
	 * This class represents a column, which can either
	 * have a numeric or text value
	 */
	public class Column {
		public static final int TYPE_TEXT = 0;
		public static final int TYPE_NUM  = 1;
		public static final int TYPE_ARRAYLIST = 2;
		
		private String name;
		
		private String textValue;
		private double numValue;
		private ArrayList arrayValue;
		
		private int type;
		private boolean visible;
		
		public Column(String n, int t) { name = n; type = t; textValue = ""; this.visible = true; }
		public Column(String n, int t, boolean visible) { this(n, t); this.visible = visible; }
		
		public String getName() { return name; }
		public String getText() { 
			String text = "";
			switch(type) {
			case TYPE_TEXT: text = textValue; break;
			case TYPE_NUM: text = Double.toString(numValue); break;
			case TYPE_ARRAYLIST: text = arrayValue.toString();
			}
			return text;
		}
		
		public double getNumeric() { return numValue; }
		public ArrayList getArray() { return arrayValue; }
		public void setText(String value) { textValue = value; }
		public void setNumeric(double value) { numValue = value; }
		public void setArray(ArrayList value) { arrayValue = value; }
		public boolean isVisible() { return visible; }
	}
}
