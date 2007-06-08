// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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

/**
   A search node, important part of the Dijkstra algorithm
*/
class SearchNode
{
	float cost;
	SearchNode parent;

	/**
	   return the Parent of this SearchNode, which corresponds to the
	   state of the Search engine when this node was opened
	*/
	public SearchNode getParent ()
	{
		return parent;
	}
	
	PwyElt elt1; // corresponding elt in doc1

	public PwyElt getElt1()
	{
		return elt1;
	}
	
	PwyElt elt2; // corresponding elt in doc2

	public PwyElt getElt2()
	{
		return elt2;
	}
	
	/**
	   Create a new SearchNode.
	   note: parent may be null.
	*/
	public SearchNode(SearchNode _parent, PwyElt _e1, PwyElt _e2, float _cost)
	{
		cost = _cost;
		parent = _parent;
		elt1 = _e1;
		elt2 = _e2;
		assert (elt1 != null);
		assert (elt2 != null);
	}
}