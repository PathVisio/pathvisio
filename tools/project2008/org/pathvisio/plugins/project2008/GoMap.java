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
package org.pathvisio.plugins.project2008;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * maintains mappings from go to ensembl and vice versa
 */
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

	Map<GoTerm, Integer> nMap = new HashMap<GoTerm, Integer>();
	Map<GoTerm, Integer> mMap = new HashMap<GoTerm, Integer>();
	
	/**
	 * go through the tree depth-first.
	 * Build a set of all genes represented by root, recursively.
	 * Calculate n, m for root,
	 * and for the children that you encounter as well.
	 */
	public void calculateNM(Collection<GoTerm> roots, Set<String> genidInPway)
	{
		for (GoTerm root : roots)
		{
			calculateNMRecursive(root, genidInPway);
		}
	}

	private Set<String> calculateNMRecursive (GoTerm term, Set<String> pwyGenes)
	{
		Set<String> myGenes = new HashSet<String>();

		String id = term.getId();
		
		if (geneByGO.containsKey(id))
		{
			myGenes.addAll (geneByGO.get (id));
		}

		// for each child
		for (GoTerm child : term.getChildren())
		{
			myGenes.addAll (calculateNMRecursive (child, pwyGenes));
			
		}		
		
		int m = myGenes.size();
		int n = 0;
		
		for (String gene : myGenes)
		{
			if (pwyGenes.contains(gene)) n++;
		}
		
		mMap.put (term, m);
		nMap.put (term, n);
		
		return myGenes;
	}

	/**
	 * Get 'M': the total number of genes corresponding to
	 * this term and child terms, recursively.
	 */
	public int getM(GoTerm term)
	{
		return mMap.get (term);
	}
		
	/**
	 * Get 'N': the number of genes that occur in pathways corresponding to
	 * this term and child terms, recursively.
	 */
	public int getN(GoTerm term)
	{
		return nMap.get (term);
	}
	
}
