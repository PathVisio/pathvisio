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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bridgedb.DataSource;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.GpmlFormatReader;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.StaticProperty;
import org.pathvisio.core.view.ShapeRegistry;

/**
 * A patch contains a series of deletions, insertions and modifications
 * and can be read from an Dgpml XML file, and then applied to a Pathway object.
 * This class does not handle writing modifications, that is done by DgpmlOutputter.
 */
class Patch
{
	private class Change
	{
		public String attr;
		public String oldValue;
		public String newValue;
	}

	private class ModDel
	{
		boolean isDeletion; // true when this is a deletion, false when this is a modification
		public PathwayElement oldElt;
		List<Change> changes = new ArrayList<Change> (); // only used when isDeletion == false

		public PathwayElement makeNewElt()
		{
			if (isDeletion) throw new IllegalArgumentException();
			PathwayElement result = oldElt.copy();
			for (Change ch : changes)
			{
				StaticProperty pt = StaticProperty.getByTag(ch.attr);
				switch (pt.type())
				{
				case STRING:
					result.setStaticProperty(pt, ch.newValue);
					break;
				case DOUBLE:
					result.setStaticProperty(pt, Double.parseDouble (ch.newValue));
					break;
				case SHAPETYPE:
					// setProperty expects to get ShapeType for this Shape.
					result.setStaticProperty(pt, ShapeRegistry.fromName(ch.newValue));
					break;
				case DATASOURCE:
					result.setStaticProperty(pt, DataSource.getByFullName(ch.newValue));
					break;
				case DB_ID:
					result.setStaticProperty(pt, ch.newValue);
					break;
				case GENETYPE:
					result.setStaticProperty(pt, ch.newValue);
					break;
				default:
					Logger.log.error (ch.attr + " of type " + pt.getType() + " not supported");
					assert (false);
				}
			}
			return result;
		}
	}

	// store deletions and modifications together,
	// because they should both be looked up in the old Pwy
	private Map <PathwayElement, ModDel> modifications = new HashMap <PathwayElement, ModDel>();

	// store insertions separately, as they don't need to be looked up
	// in the old Pwy, they just need to be added afterwards.
	private List<PathwayElement> insertions = new ArrayList <PathwayElement>();

	public void readFromReader (Reader in) throws JDOMException, IOException, ConverterException
	{
		SAXBuilder builder = new SAXBuilder ();
		Document doc = builder.build (in);

		for (Object o : doc.getRootElement().getChildren())
		{
			Element e = ((Element)o);
			if (e.getName().equals("Modify"))
			{
				ModDel mod = new ModDel();
				mod.isDeletion = false;

				for (Object p : ((Element)e).getChildren())
				{
					Element f = ((Element)p);
					if (!f.getName().equals("Change"))
					{
						GpmlFormatReader reader = GpmlFormat.getReaderForNamespace(
								f.getNamespace());
						mod.oldElt = reader.mapElement(f);
					}
					else
					{
						Change chg = new Change();
						chg.attr = f.getAttributeValue("attr");
						chg.oldValue = f.getAttributeValue("old");
						chg.newValue = f.getAttributeValue("new");
						mod.changes.add(chg);
					}
				}
				if (mod.oldElt == null)
				{
					// this doesn't have a valid gpml subelement. Ignore.
					Logger.log.warn ("Skipping one invalid Modify element");
					continue; // skip
				}
				modifications.put (mod.oldElt, mod);
			}
			else if (e.getName().equals("Insert"))
			{
				Element f = (Element)e.getChildren().get(0);
				GpmlFormatReader reader = GpmlFormat.getReaderForNamespace(f.getNamespace());
				PathwayElement ins = reader.mapElement (f);
				insertions.add (ins);
			}
			else if (e.getName().equals("Delete"))
			{
				ModDel mod = new ModDel();
				mod.isDeletion = true;
				mod.changes = null;
				Element f = (Element)e.getChildren().get(0);
				GpmlFormatReader reader = GpmlFormat.getReaderForNamespace(f.getNamespace());
				mod.oldElt = reader.mapElement (f);
				modifications.put (mod.oldElt, mod);
			}
			else
			{
				assert (false); // no other options
			}
		}
	}

	void reverse()
	{
	}

	public void applyTo (PwyDoc aPwy, int fuzz)
	{
		SearchNode current = null;
		SimilarityFunction simFun = new BasicSim();
		CostFunction costFun = new BasicCost();

		// scan modifications / deletions for correspondence
		for (ModDel mod : modifications.values())
		{
			current = findCorrespondence (current, aPwy, mod.oldElt, simFun, costFun);
		}

		// insertions are easy, just add them
		for (PathwayElement ins : insertions)
		{
			aPwy.add (ins);
		}

	    // now modifications and deletions:
		// Start back from current
		while (current != null)
		{
			// check for modification
			ModDel mod = modifications.get (current.oldElt);
			assert (mod != null);
			// is this a deletion or a modification?)
			if (mod.isDeletion)
			{
				// mod goes to /dev/null
				aPwy.remove (current.newElt);
			}
			else
			{
				// remove the old element.

				// Note that current.oldElt is the copy of the element in the Modification object,
				// and current.newElt is the matching element in the pathway.
				aPwy.remove (current.newElt);

				// create new elt with applied modifications
				PathwayElement newElt = mod.makeNewElt();

				// and add it to the pwy.
				aPwy.add (newElt);
			}

			current = current.getParent();
		}

		aPwy.apply();
	}

	/**
	   This is very similar to PwyDoc.SearchCorrespondence, only
	   it doesn't compare two pathways: it compares elements mentioned in the dgpml with pathways.
	 */
	private SearchNode findCorrespondence(SearchNode currentNode, PwyDoc oldDoc, PathwayElement newElt, SimilarityFunction simFun, CostFunction costFun)
	{
		int maxScore = 0;
		PathwayElement maxOldElt = null;
		for (PathwayElement oldElt : oldDoc.getElts())
		{
			// if it's the first node, or if the newElt is not yet in the searchpath
			if (currentNode == null || !currentNode.ancestryHasElt (newElt))
			{
				int score = simFun.getSimScore (oldElt, newElt);
				if (score > maxScore)
				{
					maxOldElt = oldElt;
					maxScore = score;
				}
			}
		}

		if (maxOldElt != null && maxScore > 70)
		{
			// add pairing to search tree.
			SearchNode newNode = new SearchNode (currentNode, newElt, maxOldElt, 0);
			currentNode = newNode;
		}
		return currentNode;
	}
}