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
package org.pathvisio.gpmldiff;

import java.io.*;
import java.util.*;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.ConverterException;

/**
   Wrapper for org.pathvisio.model.Pathway that adds some extra
   functionality for gpmldiff
*/   
class PwyDoc
{
	Pathway pwy;

	/**
	   return the wrapped Pathway.
	 */
	Pathway getPathway()
	{
		return pwy;
	}
	
	private	List<PwyElt> elts = new ArrayList<PwyElt>();

	/**
	   Return a list of all PwyElts contained in this documents
	*/
	public List<PwyElt> getElts() { return elts; }
		
	/**
	   Construct a new PwyDoc from a certain file
	   Returns null if there is an  IO exception
	   TODO: We may want to pass on the exception?
	*/
	static public PwyDoc read(File f)
	{
		PwyDoc result = new PwyDoc();
		result.pwy = new Pathway();
		try
		{
			result.pwy.readFromXml (f, false);
		}
		catch (ConverterException e) { return null; }
		
		for (PathwayElement e : result.pwy.getDataObjects())
		{
			result.elts.add (new PwyElt (e));
		}
		return result;
	}
		
	/**
	   Finds correspondence set with the lowest cost using Dijkstra's algorithm
	*/
	SearchNode findCorrespondence(PwyDoc other, SimilarityFunction simFun, CostFunction costFun)
	{
		SearchNode currentNode = null;
				
		for (PwyElt e1 : elts)
		{						
			int maxScore = 0;
			PwyElt maxElt = null;
			for (PwyElt e2: other.getElts())
			{
				int score = simFun.getSimScore (e1, e2);
				if (score > maxScore)
				{
					maxElt = e2;
					maxScore = score;
				}
			}

			// add pairing to search tree.
			SearchNode newNode = new SearchNode (currentNode, e1, maxElt, 0);
			currentNode = newNode;

		}
		return currentNode;
	}
		
	/**
	   Output the Diff after the best correspondence has been calculated.
	*/
	void writeResult (SearchNode result, PwyDoc other, DiffOutputter out)
	{
		Set<PwyElt> both1 = new HashSet<PwyElt>();
		Set<PwyElt> both2 = new HashSet<PwyElt>();
				
		SearchNode current = result;
		while (current != null)
		{
			// check for modification
			current.getElt1().writeModifications(current.getElt2(), out);
			both1.add (current.getElt1());
			both2.add (current.getElt2());
			current = current.getParent();
		}

		for (PwyElt e : elts)
		{
			if (!both1.contains(e))
			{
				out.insert (e);
			}
		}

		for (PwyElt e : other.elts)
		{
			if (!both2.contains(e))
			{
				out.delete (e);
			}
		}
	}
}