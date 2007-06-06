package org.pathvisio.gpmldiff;

import java.io.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import java.util.*;

/**
   Contains an entire Pathway Document
*/   
class PwyDoc
{
		private Document doc;
		private	List<PwyElt> elts = new ArrayList<PwyElt>();

		/**
		   Return a list of all PwyElts contained in this documents
		*/
		public List<PwyElt> getElts() { return elts; }
		
		/**
		   Construct a new PwyDoc from a certain file
		   Returns null if there is a JDOM or IO exception
		   TODO: We may want to pass on the exception?
		*/
		static public PwyDoc read(File f)
		{
				PwyDoc result = null;
				try
				{
						SAXBuilder saxb = new SAXBuilder();		
						result = new PwyDoc();
						result.doc = saxb.build (f);
				}
				catch (JDOMException e) { e.printStackTrace(); return null; }
				catch (IOException e) { return null; }
				
				Element root = result.doc.getRootElement();
				// turn all first-level elements into a PwyElt 
				for (Element e : (List<Element>)root.getChildren())
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