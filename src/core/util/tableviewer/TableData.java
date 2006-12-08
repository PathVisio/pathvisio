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
package util.tableviewer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class holds a collection of {@link Row}s sharing the same {@link Cell}s
 */
public class TableData {
	private HashMap<String, Column> colHash;
	private ArrayList<Column> cols;
	private ArrayList<Row> rows;
	
	public TableData() {
		colHash = new HashMap<String, Column>();
		cols = new ArrayList<Column>();
		rows = new ArrayList<Row>();
	}
		
	public ArrayList<Row> getResults() { return rows; }
	public ArrayList<String> getColNames() { 
		return getColNames(false); 
	}
	
	public ArrayList<String> getColNames(boolean showHidden) {
		ArrayList<String> attrNames = new ArrayList<String>();
		for(Column c : cols) {
			if(!showHidden) { if(c.isVisible()) attrNames.add(c.getName()); }
			else attrNames.add(c.getName());
		}
		return attrNames;
	}
	
	public void addColumn(String name) {
		addColumn(name, true);
	}
	public void addColumn(String name, boolean visible) {
		Column c = new Column(name, visible);
		cols.add(c);
		colHash.put(name, c);	
	}
	
	public Column colByName(String colName) {
		return colHash.get(colName);
	}
	
	private void addResult(Row rs) { rows.add(rs); }
	
	/**
	 * This class contains a single result from a search
	 */
	public class Row {
		private HashMap<Column, Cell> cells;
		
		public Row() { 
			cells = new HashMap<Column, Cell>();
			for(Column c : cols) {
				cells.put(c,
						new Cell(c, Cell.TYPE_TEXT));
			}
			addResult(this);
		}
		
		public void setCell(String name, String value) {
			Cell c = cells.get(colByName(name));
			if(c != null) c.setText(value);
		}
		
		public void setCell(String name, double value) {
			Cell c = cells.get(colByName(name));
			if(c != null) c.setNumeric(value);
		}
		
		public void setCell(String name, ArrayList value) {
			Cell c = cells.get(colByName(name));
			if(c != null) c.setArray(value);
		}
				
		public Cell getCell(String name) throws IllegalArgumentException {
			Cell c = cells.get(colByName(name));
			if(c != null) return c;
			else 
				throw new IllegalArgumentException("Attribute " + name + " does not exist");
		}
	}
	
	/**
	 * This class represents a column
	 */
	public class Column {
		String name;
		boolean visible;
		
		public Column(String name, boolean visible) {
			this.name = name;
			this.visible = visible;
		}
		
		public Column(String name) {
			this(name, true);
		}
		
		boolean isVisible() { return visible; }
		String getName() { return name; }
	}
	
	/**
	 * This class represents a cell, which can either
	 * have a numeric or text value
	 */
	public class Cell implements Comparable {
		Column col;
		
		public static final int TYPE_TEXT = 0;
		public static final int TYPE_ARRAYLIST = 1;
		public static final int TYPE_NUM  = 2;
				
		private String textValue;
		private double numValue;
		private ArrayList arrayValue;
		
		private int type;
		
		public Cell(Column c, int t) {
			col = c;
			type = t; 
			textValue = ""; 
		}
		
		public Column getColumn() { return col; }
		
		public void setType(int type) { 
			this.type = type;
		}
		
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
		
		public void setText(String value) {
			setType(TYPE_TEXT);
			textValue = value; 
		}
		public void setNumeric(double value) {
			setType(TYPE_NUM);
			numValue = value; 
		}
		public void setArray(ArrayList value) { 
			setType(TYPE_ARRAYLIST);
			arrayValue = value; 
		}
				
		public int compareTo(Object o) {
			Cell c = (Cell)o;
						
			if(type == c.type) {
				switch(type) {
				case TYPE_TEXT: return textValue.compareTo(c.textValue);
				case TYPE_NUM:
					//Make sure NaNs are lowest
					if(Double.isNaN(numValue)) return -1;
					if(Double.isNaN(c.numValue)) return 1;
					return Double.compare(numValue, c.numValue);
				case TYPE_ARRAYLIST: return arrayValue.size() - c.arrayValue.size();
				default: return -1;
				}
			} else return type - c.type;
		}
	}
}
