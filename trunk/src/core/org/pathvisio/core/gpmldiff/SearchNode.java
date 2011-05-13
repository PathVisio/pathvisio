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
package org.pathvisio.core.gpmldiff;

import java.util.HashSet;
import java.util.Set;

import org.pathvisio.core.model.PathwayElement;

/**
   A search node, important part of the Dijkstra algorithm
*/
public class SearchNode
{
	float cost;
	SearchNode parent;

	// pwy elts in use by parent search path, both old and new
	Set<PathwayElement> parentSet = new HashSet<PathwayElement>();

	/**
	   return the Parent of this SearchNode, which corresponds to the
	   state of the Search engine when this node was opened
	*/
	public SearchNode getParent ()
	{
		return parent;
	}

	PathwayElement oldElt; // corresponding elt in old doc

	public PathwayElement getOldElt()
	{
		return oldElt;
	}

	PathwayElement newElt; // corresponding elt in new doc

	public PathwayElement getNewElt()
	{
		return newElt;
	}

	/**
	   Determine if a certain pwy elt is already in the ancestry of this Search Node.
	   (old or new doesn't matter, as these sets never overlap)
	 */
	public boolean ancestryHasElt(PathwayElement elt)
	{
		return parentSet.contains (elt);
	}

	/**
	   Create a new SearchNode.
	   note: parent may be null for the first SearchNode.
	   This will mark oldElt and newElt so they can be added only once.
	*/
	public SearchNode(SearchNode aParent, PathwayElement anOldElt, PathwayElement aNewElt, float aCost)
	{
		cost = aCost;
		parent = aParent;
		oldElt = anOldElt;
		newElt = aNewElt;
		assert (oldElt != null);
		assert (newElt != null);
		if (parent != null)
		{
			parentSet = parent.parentSet;
		}
		assert (!parentSet.contains(oldElt));
		assert (!parentSet.contains(newElt));
		parentSet.add (oldElt);
		parentSet.add (newElt);
	}
}