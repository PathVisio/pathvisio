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
package org.pathvisio.R;

import org.pathvisio.gmmlVision.GmmlVision;
import org.pathvisio.gmmlVision.sidepanels.TabbedSidePanel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;

import org.pathvisio.util.tableviewer.PathwayTable;
import org.pathvisio.util.tableviewer.TableData;
import org.pathvisio.R.RCommands.RException;
import org.pathvisio.R.RCommands.RObjectContainer;

public class RDataIn {
	RObjectContainer inFileObjects;
		
	public RDataIn() {
		inFileObjects = new RObjectContainer();
	}
	
	public void setRDataFile(String fn) { inFileObjects.setDataFile(new File(fn)); }
	public String getRDataFile() { return inFileObjects.getDataFile().toString(); }
	public RObjectContainer getUsedObjects() 	{ return inFileObjects; }
	
	public void checkValid() throws Exception {
		if(!inFileObjects.getDataFile().canRead()) 
			throw new Exception("Can't read file " + inFileObjects.getDataFile());
	}
	
	public void load() throws Exception {
		checkValid();
		inFileObjects.loadFromFile();
	}
	
	public static String[] listPathwaySets() throws RException {
		return RCommands.ls("PathwaySet");
	}
	
	public static String[] listDataSets() throws RException {
		return RCommands.ls("DataSet");
	}
	
	public static String[] listResultSets() throws RException {
		return RCommands.ls("ResultSet");
	}
	
	public static List<ResultSet> loadResultSets(File f) throws RException {
		RCommands.load(f);
		String[] resultVars = listResultSets();
		if(resultVars.length == 0) throw new RException(null, "No result sets in " + f);
		List<ResultSet> results = new ArrayList<ResultSet>();
		for(String resVar : resultVars) results.add(new ResultSet(resVar));
		return results;
	}
	
	public static List<ResultSet> getResultSets(String symbol) throws RException {
		List<ResultSet> results = new ArrayList<ResultSet>();
		
		String cls = RCommands.eval("class(" + symbol + ")", true).asString();
		
		if(cls.equals("ResultSet")) {
			results.add(new ResultSet(symbol));
		} else if (cls.equals("list")) {
			int length = RCommands.eval("length(" + symbol + ")", true).asInt();
			for(int i = 1; i < length + 1; i++) {
				String elm = symbol + "[[" + i + "]]";
				String var = RCommands.format(RCommands.eval("name(" + elm + ")", true).asString()) 
								+ "_" + symbol;
				RCommands.eval(var + " = " + elm);
				results.add(new ResultSet(var));
			}
		}
		
		return(results);
		
	}
	
	public static void displayResults(List<ResultSet> results, String tabName) {		
		TabbedSidePanel sp = GmmlVision.getWindow().getSidePanel();
		
		StatsResultTable srt = new StatsResultTable(sp.getTabFolder(), SWT.NULL);
		srt.setResults(results);
		
		String nm = getTabItemName(tabName, sp);
		sp.addTab(srt, nm, true);
		sp.selectTab(nm);
	}
	
	private static String getTabItemName(String prefName, TabbedSidePanel tsp) {
		HashMap<String, CTabItem> tabItems = tsp.getTabItemHash();
		if(!tabItems.containsKey(prefName)) return prefName;
		SortedSet<String> matches = new TreeSet<String>();
		for(CTabItem ti : tabItems.values())
			if(ti.getText().startsWith(prefName)) matches.add(ti.getText());
		String last = matches.last();
		int replaceFrom = last.lastIndexOf("(");
		if(replaceFrom < 0) return last + " (1)";
		
		int num = Integer.parseInt(last.substring(replaceFrom + 1, replaceFrom + 2));
		return last.substring(0, replaceFrom) + " (" + ++num + ")";
	}
	
	public static class ResultSet extends TableData {
		String name;
		String varName;
		String[] globalNames;
		String[] globalValues;
		
		public ResultSet(String varName) throws RException {
			this.varName = varName;
			loadFromR();
		}
		
		public String getName() { return name; }
		public String getVarName() { return varName; }
		public String[] getGlobalNames() { return globalNames; }
		public String[] getGlobalValues() { return globalValues; }
		
		private void loadFromR() throws RException {
			name = RCommands.eval("name(" + varName + ")", true).asString();
			globalNames = RCommands.eval("globals(" + varName + ")[,1]", true).asStringArray();
			globalValues = RCommands.eval("globals(" + varName + ")[,2]", true).asStringArray();
			String[] cols = RCommands.colnames(varName);
			for(String col : cols) {
				addColumn(col, !col.equalsIgnoreCase(PathwayTable.COLNAME_FILE));
			}
			int nrow = RCommands.dim(varName)[0];
			for(int i = 1; i < nrow + 1; i++) {
				String[] data = RCommands.eval(varName + "[" + i + ",]", true).asStringArray();
				Row row = new Row();
				for(int j = 0; j < data.length; j++) {
					try { //Try to parse data as number
						double num = Double.parseDouble(data[j]);
						row.setCell(cols[j], num);
					} catch(NumberFormatException e) {
						row.setCell(cols[j], data[j]);
					}
					
				}
			}
		}
	}
}
