// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.util.swing;

import java.io.File;
import java.util.List;

import org.pathvisio.data.XrefWithSymbol;
import org.pathvisio.model.Pathway;
import org.pathvisio.util.Utils;

/**
 * The result of a pathway search.
 */
public class MatchResult implements RowWithProperties<SearchTableModel.Column>
{
	private File file;
	private List<String> idsFound;
	private List<String> namesFound;
	private List<XrefWithSymbol> matches;
	
	MatchResult (File f, List<String> idsFound, List<String> namesFound, List<XrefWithSymbol> matches)
	{
		if (f == null) throw new NullPointerException();
		file = f;
		this.idsFound = idsFound;
		this.namesFound = namesFound;
		this.matches = matches;
	}
	
	public String getProperty (SearchTableModel.Column prop)
	{
		switch (prop)
		{
		case DIRECTORY: 
			return getDirectory(); 
		case IDS:
			return idsFound == null ? "" : Utils.collection2String(idsFound, "", " ");
		case PATHWAY_NAME:
			return getName();
		case NAMES:
			return namesFound == null ? "" : Utils.collection2String(namesFound, "", " ");
		}
		// all cases should have been handled at this point.
		throw new IllegalArgumentException();
	}
	
	// name of pwy found
	String getName() { return file.getName(); }
	
	// file of pwy found
	public File getFile() { return file; }
	
	// dir of pwy found
	String getDirectory() { return file.getParentFile().getName(); }
	
	// id's found
	List<String> getIdsFound() { return idsFound; }
	
	public List<XrefWithSymbol> getMatches() { return matches; }
	
	// names found (when searching by symbol
	List<String> getNamesFound() { return namesFound; }
	
	public Pathway open() 
	{
		// TODO Auto-generated method stub
		return null;
	}

}