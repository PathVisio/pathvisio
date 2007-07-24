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
import org.pathvisio.debug.Logger;

/**
   Wrapper for org.pathvisio.model.Pathway that adds some extra
   functionality for gpmldiff
*/   
class PwyDoc
{
	Pathway pwy = null;
	File src = null;
	
	/**
	   return the wrapped Pathway.
	 */
	Pathway getPathway()
	{
		return pwy;
	}

	File getSourceFile ()
	{
		return src;
	}
	
	private	List<PathwayElement> elts = new ArrayList<PathwayElement>();

	public void add (PathwayElement value)
	{
		elts.add (value);
	}

	public void remove (PathwayElement value)
	{
		elts.remove (value);
	}
	
	/**
	   Return a list of all PwyElts contained in this documents
	*/
	public List<PathwayElement> getElts() { return elts; }
		
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
		catch (ConverterException e)
		{
			Logger.log.error ("Converter exception", e);
			return null;
		}
		
		for (PathwayElement e : result.pwy.getDataObjects())
		{
			result.add (e);
		}
		
		result.src = f;
		return result;
	}
		
	/**
	   Finds correspondence set with the lowest cost using Dijkstra's algorithm
	   //TODO: currently not using Dijkstra's algorithm but ad-hoc.
	   
	   Call this on the OLD doc
	   @param newDoc the New doc
	   @param simFun similarity function
	   @param costFun cost function (for deciding which correspondence set to choose)
	*/
	SearchNode findCorrespondence(PwyDoc newDoc, SimilarityFunction simFun, CostFunction costFun)
	{
		SearchNode currentNode = null;
				
		for (PathwayElement oldElt : elts)
		{						
			int maxScore = 0;
			PathwayElement maxNewElt = null;
			for (PathwayElement newElt : newDoc.getElts())
			{
				// if it's the first node, or if the newElt is not yet in the searchpath
				if (currentNode == null || !currentNode.ancestryHasElt (newElt))
				{
					int score = simFun.getSimScore (oldElt, newElt);
					if (score > maxScore)
					{
						maxNewElt = newElt;
						maxScore = score;
					}
				}
			}

			if (maxNewElt != null && maxScore > 70)
			{
				// add pairing to search tree.
				SearchNode newNode = new SearchNode (currentNode, oldElt, maxNewElt, 0);
				currentNode = newNode;
			}

		}
		return currentNode;
	}
		
	/**
	   Output the Diff after the best correspondence has been
	   calculated.  call this on the OLD doc.

	   @param result result of findcorrespondence
	   @param newPwy the new pathway compared against
	   @param out DiffOutputter that absorbs the results and puts it
	   somewhere depending on the type of DiffOutputter
	*/
	void writeResult (SearchNode result, PwyDoc newPwy, DiffOutputter out)
	{
		Set<PathwayElement> bothOld = new HashSet<PathwayElement>();
		Set<PathwayElement> bothNew = new HashSet<PathwayElement>();
				
		SearchNode current = result;
		while (current != null)
		{
			// check for modification
			PwyElt.writeModifications(current.getOldElt(), current.getNewElt(), out);
			bothOld.add (current.getOldElt());
			bothNew.add (current.getNewElt());
			current = current.getParent();
		}

		for (PathwayElement oldElt : elts)
		{
			// if the oldElt doesn't have a corresponding newElt...
			if (!bothOld.contains(oldElt))
			{
				// then we have a deletion
				out.delete (oldElt);
			}
		}

		for (PathwayElement newElt : newPwy.elts)
		{
			// if the newElt doesn't have a corresponding oldElt
			if (!bothNew.contains(newElt))
			{
				// then we have an insertioncurrent.getOldElt()
				out.insert (newElt);
			}
		}
	}
}