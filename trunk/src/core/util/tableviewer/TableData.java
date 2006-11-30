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
	public ArrayList<String> getColNames() { 
		return getColNames(false); 
	}
	
	public ArrayList<String> getColNames(boolean showHidden) {
		ArrayList<String> attrNames = new ArrayList<String>();
		for(Column at : columnTemp) {
			if(!showHidden) { if(at.isVisible()) attrNames.add(at.name); }
			else attrNames.add(at.name);
		}
		return attrNames;
	}
	
	public void addColumn(String name, int type) {
		columnTemp.add(new Column(name, type, true));
	}
	public void addColumn(String name, int type, boolean visible) {
		columnTemp.add(new Column(name, type, visible));
	}
	
	private void addResult(Row rs) { rows.add(rs); }
	
	/**
	 * This class contains a single result from a search
	 */
	public class Row {
		private HashMap<String, Column> columns;
		
		public Row() { 
			columns = new HashMap<String, Column>();
			for(Column at : columnTemp) {
				columns.put(at.getName(),
						new Column(at.getName(), at.type, at.isVisible()));
			}
			addResult(this);
		}
		
		public void setColumn(String name, String value) {
			if(columns.containsKey(name)) columns.get(name).setText(value);
		}
		
		public void setColumn(String name, double value) {
			if(columns.containsKey(name)) columns.get(name).setNumeric(value);
		}
		
		public void setColumn(String name, ArrayList value) {
			if(columns.containsKey(name)) columns.get(name).setArray(value);
		}
		
		public void overrideColumn(Column col) {
			columns.put(col.getName(), col);
		}
		
		public Column getColumn(String name) throws IllegalArgumentException {
			if(columns.containsKey(name)) return columns.get(name);
			throw new IllegalArgumentException("Attribute " + name + " does not exist");
		}
		
		public ArrayList<Column> getColumns(boolean onlyVisible) {
			ArrayList<Column> attr = new ArrayList<Column>();
			for(Column at : columnTemp) {
				Column atr = columns.get(at.getName());
				if(atr.isVisible()) attr.add(atr);
			}
			return attr;
		}
	}
	
	/**
	 * This class represents a column, which can either
	 * have a numeric or text value
	 */
	public class Column implements Comparable {
		public static final int TYPE_TEXT = 0;
		public static final int TYPE_ARRAYLIST = 1;
		public static final int TYPE_NUM  = 2;
		
		private String name;
		
		private String textValue;
		private double numValue;
		private ArrayList arrayValue;
		
		private int type;
		private boolean visible;
		
		public Column(String n, int t) { name = n; type = t; textValue = ""; this.visible = true; }
		public Column(String n, int t, boolean visible) { this(n, t); this.visible = visible; }
		
		public void setType(int type) { this.type = type; }
		
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
		
		public int compareTo(Object o) {
			Column c = (Column)o;
			
//			switch(type) {
//			case TYPE_TEXT: 
//				{
//					System.out.println("COMPARE: " + c.getText() + ", " + c.getText());
//					//Try to treat as numeric
//					double numThis = 0;
//					double numThat = 0;
//					boolean isNumThis = true;
//					boolean isNumThat = true;
//					try { numThis = Double.parseDouble(textValue); } 
//					catch(NumberFormatException e) { isNumThis = false; }
//					try { numThat = Double.parseDouble(c.getText()); } 
//					catch(NumberFormatException e) { isNumThat = false; }
//					
//					if(isNumThis && isNumThat) 	{
//						double diff = numThis - numThat;
//						return (int)(diff > 0 ? Math.ceil(diff) : Math.floor(diff));
//					}
//					if(isNumThis) return 1;
//					if(isNumThat) return -1;
//					//Both are strings
//					return textValue.compareTo(c.getText());
//				}
//			case TYPE_NUM: return (int)(numValue - c.getNumeric());
//			case TYPE_ARRAYLIST: return arrayValue.size() - c.getArray().size();
//			default: return -1;
			
			if(type == c.type) {
				switch(type) {
				case TYPE_TEXT: return textValue.compareTo(c.textValue);
				case TYPE_NUM: return (int)Math.ceil(numValue - c.numValue);
				case TYPE_ARRAYLIST: return arrayValue.size() - c.arrayValue.size();
				default: return -1;
				}
			} else return type - c.type;
		}
		
		
	}
}
