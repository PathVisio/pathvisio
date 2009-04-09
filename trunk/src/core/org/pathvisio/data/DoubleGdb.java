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
package org.pathvisio.data;

import java.util.ArrayList;
import java.util.List;

import org.bridgedb.DataException;
import org.bridgedb.DataSource;
import org.bridgedb.Gdb;
import org.bridgedb.SimpleGdb;
import org.bridgedb.Xref;
import org.bridgedb.XrefWithSymbol;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;

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
	 * 
	 * @param gdb pass null to close the connection.
	 */
	public void setMetaboliteDb (SimpleGdb gdb)
	{
		if (gdb == gdbs[METABOLITE_DB]) return;

		try
		{
			if (gdbs [METABOLITE_DB] != null) gdbs[METABOLITE_DB].close();
		}
		catch (DataException e)
		{
			Logger.log.error ("Problem closing metabolite database", e);
		}
		gdbs [METABOLITE_DB] = gdb;
		if (gdb != null)
		{
			PreferenceManager.getCurrent().set(GlobalPreference.DB_METABDB_CURRENT, gdb.getDbName());
		}
	}

	/**
	 * Set the gene database.
	 * Closes any pre-existing gene databases.
	 */
	public void setGeneDb (SimpleGdb gdb)
	{
		if (gdb == gdbs[GENE_DB]) return;
		
		try
		{
			if (gdbs [GENE_DB] != null) gdbs[GENE_DB].close();
		}
		catch (DataException e)
		{
			Logger.log.error ("Problem closing gene database", e);
		}
		gdbs [GENE_DB] = gdb;		
		if (gdb != null)
		{
			PreferenceManager.getCurrent().set(GlobalPreference.DB_GDB_CURRENT, gdb.getDbName());
		}
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
	 * Check if the reference exists in either one of the 
	 * child databases
	 * @throws DataException 
	 */
	public boolean xrefExists(Xref xref) throws DataException 
	{
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if(child.xrefExists(xref)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Return the aggregate of the child results.
	 */
	public List<Xref> getCrossRefs(Xref idc) throws DataException 
	{	
		List<Xref> result = new ArrayList<Xref>();
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				result.addAll (child.getCrossRefs (idc));
			}
		}
		return result;
	}

	/**
	 * Return the aggregate of the child results.
	 */
	public List<Xref> getCrossRefs(Xref idc, DataSource resultDs) throws DataException
	{
		List<Xref> result = new ArrayList<Xref>();
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				result.addAll (child.getCrossRefs (idc, resultDs));
			}
		}
		return result;
	}

	public List<Xref> getCrossRefsByAttribute(String attrName, String attrValue) throws DataException {
		List<Xref> result = null;
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.getCrossRefsByAttribute (attrName, attrValue);
				else
					result.addAll (child.getCrossRefsByAttribute (attrName, attrValue));
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
	 * @throws DataException 
	 */
	public String getGeneSymbol(Xref ref) throws DataException 
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
	 * @throws DataException 
	 */
	public List<Xref> getIdSuggestions(String text,
			int limit) throws DataException 
	{
		List<Xref> result = new ArrayList<Xref>();
		
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
	 * @throws DataException 
	 */
	public List<String> getSymbolSuggestions(String text,
			int limit) throws DataException 
	{
		List<String> result = new ArrayList<String>();
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				if (result == null)
					result = child.getSymbolSuggestions (text, limit);
				else
					result.addAll (child.getSymbolSuggestions (text, limit));
			}
			// don't need to continue if we already reached limit.
			if (result.size() >= limit) break;
		}
		return result;
	}

	/**
	 * return first non-null child result
	 * @throws DataException 
	 */
	public String getBpInfo(Xref ref) throws DataException 
	{
		String result = null;
		// return the first database with a result.
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				result = child.getBpInfo(ref);
				if (result != null) return result;
			}
		}
		// failure
		return null;
	}

	public List<XrefWithSymbol> freeSearch(String text, int limit) throws DataException
	{
		List<XrefWithSymbol> result = new ArrayList<XrefWithSymbol>();
		
		for (SimpleGdb child : gdbs)
		{
			if (child != null && child.isConnected())
			{
				result.addAll (child.freeSearch(text, limit));
			}
			// don't need to continue if we already reached limit.
			if (result.size() >= limit) break;
		}
		return result;
	}
}