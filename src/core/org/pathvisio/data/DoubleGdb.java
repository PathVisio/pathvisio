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
package org.pathvisio.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pathvisio.model.DataSource;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.Xref;

/**
 * Prototype of AggregateGdb.
 * We limit ourselves to two Gdb's, 
 * to simply the user interface.
 */
public class DoubleGdb implements IGdb
{
	private static final int GENE_DB = 0;
	private static final int METABOLITE_DB = 1;
	private SimpleGdb[] gdbs = {null, null};
	
	public void setMetaboliteDb (SimpleGdb gdb)
	{
		if (gdb == null) throw new NullPointerException();
		gdbs [METABOLITE_DB] = gdb;		
	}

	public void setGeneDb (SimpleGdb gdb)
	{
		if (gdb == null) throw new NullPointerException();
		gdbs [GENE_DB] = gdb;		
	}

	public void close() 
	{
		// TODO Auto-generated method stub
		
	}

	public ArrayList<Xref> ensId2Refs(String ensId, DataSource resultDs) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBackpageHTML(Xref ref, String bpHead) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBpInfo(Xref ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Xref> getCrossRefs(Xref idc) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Xref> getCrossRefs(Xref idc, DataSource resultDs) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDbName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGeneSymbol(Xref ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public String parseGeneSymbol(String bpInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<String> ref2EnsIds(Xref xref) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Map<PropertyType, String>> getIdSuggestions(String text,
			int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Map<PropertyType, String>> getSymbolSuggestions(String text,
			int limit) {
		// TODO Auto-generated method stub
		return null;
	}
}
