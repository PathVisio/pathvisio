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