package org.pathvisio.plugins.project2008;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class GoMap 
{

	GoMap (File martexport)
	{
		if (!martexport.exists())
		{
			throw new IllegalArgumentException("Can't read " + martexport);
		}
		readMartData (martexport);
	}
	
	Map<String,Set<String>> geneByGO = null;
	
	
	/**
	 * add the genes to the set of terms
	 */
	private void readMartData (File martexport)
	{
		// create a new map; the key is the GoTerm's id, the set of strings are the gene strings
		// various methods of the genesGOid class are used to do so.
		geneByGO = GenesGOid.geneByGO (GenesGOid.goByGene(martexport));
	}

	/**
	 * Get 'M': the total number of genes corresponding to
	 * this term and child terms, recursively.
	 */
	//TODO: check for duplicate genes in tree
	public int getM(GoTerm term)
	{
		String id = term.getId();
		int n = 0;
		
		if (geneByGO.containsKey(id))
		{
			n = geneByGO.get (term.getId()).size();
		}
		
		for (GoTerm child : term.getChildren())
		{
			n += getM(child);
		}
		
		return n;
	}
	
	/**
	 * Get 'N': the number of genes that occur in pathways corresponding to
	 * this term and child terms, recursively.
	 */
	//TODO: check for duplicate genes in tree
	public int getN(GoTerm term, Set<String> genidInPway)
	{	
		int n = 0;
		
		String id = term.getId();

		if (geneByGO.containsKey(id))
		{
			for (String Gene : geneByGO.get(term.getId()))
			{
				if (genidInPway.contains(Gene))
				{
					n++;
				}
			}
		}
		
		for (GoTerm child : term.getChildren())
		{
			n += getN(child, genidInPway);
		}
		
		return n;
	}

	
	
}
