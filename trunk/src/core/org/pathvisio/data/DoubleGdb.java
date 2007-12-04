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

import java.util.List;
import java.util.Map;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.Xref;
import org.pathvisio.preferences.GlobalPreference;

/**
 * Prototype of AggregateGdb.
 * We limit ourselves to two SimpleGdb's, 
 * to simply the user interface.
 * 
 * The two databases are called metabolite database and gene database,
 * although in principle there is no difference between the two.
 * DoubleGdb won't complain if you try to set two different
 * Gene databases and no metabolite database.
 * 
 * The implementation of the IGdb interface is per method:
 * if the method returns a single result, usually it is 
 * from the first child database that has a sensible result.
 * This also measns that the child databases have a definitive
 * ordering: the first one shadows the second one for some results.
 * 
 * If the method returns a list, DoubleGdb joins
 * the result from all connected child databases together.
 */
public class DoubleGdb implements Gdb
{
	private static final int GENE_DB = 0;
	private static final int METABOLITE_DB = 1;
	private SimpleGdb[] gdbs = {null, null};
	
	/**
	 * set the metabolite database
	 * closes any pre-existing metabolite databases.
	 */
	public void setMetaboliteDb (SimpleGdb gdb)
	{
		if (gdb == gdbs[METABOLITE_DB]) return;

		if (gdb == null) throw new NullPointerException();
		try
		{
			if (gdbs [METABOLITE_DB] != null) gdbs[METABOLITE_DB].close();
		}
		catch (DataException e)
		{
			Logger.log.error ("Problem closing metabolite database", e);
		}
		gdbs [METABOLITE_DB] = gdb;		
		GlobalPreference.DB_METABDB_CURRENT.setValue(gdb.getDbName());
	}

	/**
	 * Set the gene database.
	 * Closes any pre-existing gene databases.
	 */
	public void setGeneDb (SimpleGdb gdb)
	{
		if (gdb == gdbs[GENE_DB]) return;
		
		if (gdb == null) throw new NullPointerException();
		try
		{
			if (gdbs [GENE_DB] != null) gdbs[GENE_DB].close();
		}
		catch (DataException e)
		{
			Logger.log.error ("Problem closing gene database", e);
		}
		gdbs [GENE_DB] = gdb;		
		GlobalPreference.DB_GDB_CURRENT.setValue(gdb.getDbName());
	}

	/**
	 * closes all child databases. 
	 */
	public void close() throws DataException 
	{
		for (SimpleGdb child : gdbs)
		{
			if (child != null)
			{
				child.close();
				child = null; // garbage collect
			}
		}
	}

	/**
	 * Return the aggregate of the child results.
	 * @deprecated
	 */
	public List<Xref> ensId2Refs(String ensId, DataSource resultDs) 
	{
		List<Xref> result = null;
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.ensId2Refs (ensId, resultDs);
				else
					result.addAll (child.ensId2Refs (ensId, resultDs));
			}
		}
		return result;
	}

	/**
	 * returs backpage html containing the gene information
	 * from the first child that has it,
	 * and the crossref table composed of all child results.
	 */
	public String getBackpageHTML(Xref ref, String bpHead) 
	{
		String text = SimpleGdb.getBackpagePanelHeader();
		if (text == null) text = "";
		
		if( ref == null || ref.getId() == null || ref.getDataSource() == null) return text;
		
		if (bpHead == null) bpHead = "";
		text += "<H1>Gene information</H1><P>";
		text += bpHead.equals("") ? bpHead : "<H2>" + bpHead + "</H2><P>";
	
		// find first gene database that has non-null bpInfo.
		String bpInfo = null;
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				bpInfo = child.getBpInfo(ref);
				if (bpInfo != null) break;
			}
		}		
		text += bpInfo == null ? "<I>No gene information found</I>" : bpInfo;

		// get crossReferences from all registerd gdb's
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				text += child.getCrossRefText(ref);
			}
		}

		return text + "</body></html>";
	}

	/**
	 * Return the aggregate of the child results.
	 */
	public List<Xref> getCrossRefs(Xref idc) 
	{	
		List<Xref> result = null;
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.getCrossRefs (idc);
				else
					result.addAll (child.getCrossRefs (idc));
			}
		}
		return result;
	}

	/**
	 * Return the aggregate of the child results.
	 */
	public List<Xref> getCrossRefs(Xref idc, DataSource resultDs) 
	{
		List<Xref> result = null;
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.getCrossRefs (idc, resultDs);
				else
					result.addAll (child.getCrossRefs (idc, resultDs));
			}
		}
		return result;
	}

	/**
	 * This implementation concatenates the dbname's of all
	 * connected child databases.
	 */
	public String getDbName() 
	{
		String result = null;
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.getDbName();
				else
					result = result + " and " + child.getDbName();
			}
		}
		return result;		
	}

	/**
	 * This implementation iterates over all child databases
	 * and returns the first one that gives a non-null result.
	 */
	public String getGeneSymbol(Xref ref) 
	{
		String result = null;
		// return the first database with a result.
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				result = child.getGeneSymbol(ref);
				if (result != null) return result;
			}
		}
		// failure
		return null;
	}

	/**
	 * Returns true if at least one of the child databases
	 * are connected.
	 */
	public boolean isConnected() 
	{
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * returns the aggregate of all child results.
	 * @deprecated
	 */
	public List<String> ref2EnsIds(Xref xref) 
	{
		List<String> result = null;
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.ref2EnsIds (xref);
				else
					result.addAll (child.ref2EnsIds (xref));
			}
		}
		return result;
	}

	/**
	 * returns the aggregate of all child results.
	 */
	public List<Map<PropertyType, String>> getIdSuggestions(String text,
			int limit) {
		List<Map<PropertyType, String>> result = null;
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.getIdSuggestions (text, limit);
				else
					result.addAll (child.getIdSuggestions (text, limit));
			}
			// don't need to continue if we already reached limit.
			if (result.size() >= limit) break; 
		}
		return result;
	}

	/**
	 * returns the aggregate of all child results.
	 * TODO: doesn't respect limit for aggregate
	 */
	public List<Map<PropertyType, String>> getSymbolSuggestions(String text,
			int limit) {
		List<Map<PropertyType, String>> result = null;
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.getSymbolSuggestions (text, limit);
				else
					result.addAll (child.getSymbolSuggestions (text, limit));
			}
		}
		return result;
	}
}
