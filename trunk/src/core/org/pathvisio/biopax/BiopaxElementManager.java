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
package org.pathvisio.biopax;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jdom.Document;
import org.jdom.Element;
import org.pathvisio.biopax.reflect.BiopaxElement;
import org.pathvisio.biopax.reflect.BiopaxProperty;
import org.pathvisio.biopax.reflect.Namespaces;
import org.pathvisio.biopax.reflect.PropertyType;
import org.pathvisio.biopax.reflect.PublicationXref;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

/**
 * This class keeps track of all BioPAX elements in the pathway
 * @author thomas
 *
 */
public class BiopaxElementManager {
	Random random = new Random(); //Used to generate unique id's

	private Pathway pathway;
	private PathwayElement bpElm;
	private Map<String, BiopaxElement> biopax;
	/**
	 * Keeps track of the order of the loaded biopax elements per subclass.
	 */
	private Map<Class<? extends BiopaxElement>, Map<String, Integer>> ordinal;

	/**
	 * Constructor for this class. Builds a map of all biopax
	 * elements and their references
	 * @param p The pathway that contains the biopax elements
	 */
	public BiopaxElementManager(Pathway p) {
		pathway = p;
		biopax = new HashMap<String, BiopaxElement>();
		ordinal = new HashMap<Class<? extends BiopaxElement>, Map<String, Integer>>();
		refresh();
	}

	/**
	 * Check if the pathway element that contains the biopax document has changed
	 * and update the biopax hashmap if needed.
	 */
	public void refresh() {
		refresh(false);
	}

	private void refresh(boolean force) {
		PathwayElement bp = pathway.getBiopax();
		if(bpElm != bp || force) { //Only refresh if element differs or forced
			Logger.log.trace("Refreshing biopax");
			bpElm = bp;
			biopax.clear();
			ordinal.clear();

			if(bp != null) {
				Logger.log.trace("Biopax element found");
				Document d = bp.getBiopax();
				if(d != null) {
					Map<BiopaxElement, Element> oldElements = new HashMap<BiopaxElement, Element>();
					Element root = d.getRootElement();
					for(Object child : root.getChildren()) {
						if(child instanceof Element) {
							try {
								BiopaxElement bpe = BiopaxElement.fromXML((Element)child);
								biopax.put(bpe.getId(), bpe);
								addToOrdinal(bpe);
								//Remember link between new and old element
								oldElements.put(bpe, (Element)child);
							} catch(Exception ex) {
								Logger.log.error("Biopax element " + child + " ignored", ex);
							}
						}
					}
					//Remove old instances of the elements, replace with
					//BiopaxElement instances
					for(BiopaxElement bpe : oldElements.keySet()) {
						root.addContent(bpe);
						root.removeContent(oldElements.get(bpe));
					}
				}
			}
		}
	}

	/**
	 * Get the pathway that this instance manages the biopax elements for
	 * @return
	 */
	public Pathway getPathway() {
		return pathway;
	}

	/**
	 * Remove a biopax element from the biopax document within the GPML file.
	 * Note: references to this element will <B>NOT</B> be removed!
	 * @param e
	 */
	public void removeElement(BiopaxElement e) {
		Document doc = getDocument();
		System.err.println("removed: " + doc.getRootElement().removeContent(e));
//		doc.getRootElement().removeContent(e);
		biopax.remove(e.getId());
		rebuildOrdinal();
	}

	/**
	 * Checks if there are any references to the given biopax
	 * element in the pathway.
	 * This method will do a linear search on all pathway elements,
	 * so could be slow!
	 * @param p
	 * @param e
	 * @return
	 */
	public boolean hasReferences(BiopaxElement e) {
		//Check for references in child objects
		for(PathwayElement pwe : pathway.getDataObjects()) {
			if(pwe.getBiopaxRefs().contains(e.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the biopax element for the given identifier
	 * @param id the identifier
	 * @return the biopax element, or null if no element exists for the
	 * given identifier
	 */
	public BiopaxElement getElement(String id) {
		return biopax.get(id);
	}

	/**
	 * Add a biopax element to the biopax document, but ignore it
	 * in the BiopaxElementManager. This method can be used for elemens that
	 * are not used by PathVisio but still have to be included in the GPML
	 * file.
	 * @param e
	 */
	public void addPassiveElement(Element e) {
		if(!Namespaces.BIOPAX.equals(e.getNamespace())) {
			throw new IllegalArgumentException("Namespace is not BioPAX");
		}
		getDocument().getRootElement().addContent((Element)e.clone());
	}

	/**
	 * Adds an element to the biopax document. Also sets the id if
	 * not specified, or not unique. This method only applies to BioPAX
	 * elements which have a reflecting Java class in the package org.pathvisio.biopax.reflect.
	 * Other elements can be added using {@link #addPassiveElement(Element)}.
	 * @param elm
	 */
	public void addElement(BiopaxElement elm) {
		Document d = getDocument();

		//Check if this is a valid biopax document
		Element root = d.getRootElement();
		if(!root.getNamespace().equals(Namespaces.RDF)) {
			throw new IllegalArgumentException("Invalid root element: " + root);
		}
		//Set the id if not already set, or not unique
		if(elm.getId() == null || !isUniqueID(elm.getId())) {
			elm.setId(getUniqueID());
		}
		//Add this element to the document if it's not already in there
		for(BiopaxElement e : getElements()) {
			System.out.println("Comparing: " + e + " with " + elm);
			if(e.getName().equalsIgnoreCase(elm.getName())) { //Check for equal element name
				//Check for properties
				if(e instanceof PublicationXref) {
					//If both publicationxrefs have a pubmed id, compare only
					//by pubmed id
					BiopaxProperty bp1 = e.getProperty(PropertyType.ID.name());
					BiopaxProperty bp2 = elm.getProperty(PropertyType.ID.name());
					if(bp1 != null && bp2 != null && bp1.getValue().equals(bp2.getValue())) {
						Logger.log.trace("Equal pubmed id!");
						elm.setId(e.getId());
						return;
					}
				}
				Logger.log.trace("Equal properties!");
				//If we found this property equal to another one,
				//change the id and return
				if(e.propertyEquals(elm)) {
					elm.setId(e.getId());
					return;
				}
			}

		}
		d.getRootElement().addContent(elm);
		biopax.put(elm.getId(), elm);
		addToOrdinal(elm);
	}

	private void addToOrdinal(BiopaxElement e) {
		Map<String, Integer> classOrdinal = ordinal.get(e.getClass());
		if(classOrdinal == null) {
			classOrdinal = new HashMap<String, Integer>();
			ordinal.put(e.getClass(), classOrdinal);
		}
		classOrdinal.put(e.getId(), classOrdinal.size() + 1);
	}

	private void rebuildOrdinal() {
		refresh(true);
	}

	/**
	 * Get the position of the biopax element in the document, relative
	 * to other elements of the same class.
	 */
	public int getOrdinal(BiopaxElement bpe) {
		Map<String, Integer> classOrdinal = ordinal.get(bpe.getClass());
		if(classOrdinal != null) {
			return classOrdinal.get(bpe.getId());
		} else {
			return -1;
		}
	}

	/**
	 * Get all biopax elements for the pathway
	 * @return
	 */
	public Collection<BiopaxElement> getElements() {
		return biopax.values();
	}

	private boolean isUniqueID(String id) {
		return !biopax.containsKey(id);
	}

	private String getUniqueID() {
		int mod = 0x600; // 3 hex letters
		int min = 0xa00; // has to start with a letter
		String id = "";
		// in case this map is getting big, do more hex letters
		if ((biopax.size()) > 1000)
		{
			mod = 0x60000;
			min = 0xa0000;
		}

		do
		{
			id = Integer.toHexString(Math.abs(random.nextInt()) % mod + min);
		}
		while (biopax.containsKey(id));
		return id;
	}

	/**
	 * Get the Document instance that contains the biopax code for the pathway.
	 * The document will be created and added to the pathway if it doesn't exist yet
	 * @return
	 */
	private Document getDocument() {
		PathwayElement biopax = pathway.getBiopax();
		if(biopax == null) {
			biopax = PathwayElement.createPathwayElement(ObjectType.BIOPAX);
			pathway.add(biopax);
		}
		Document biopaxDoc = biopax.getBiopax();
		if(biopaxDoc == null) {
			//Create a biopax document
			Element root = new Element("RDF", Namespaces.RDF);
			root.addNamespaceDeclaration(Namespaces.RDFS);
			root.addNamespaceDeclaration(Namespaces.RDF);
			root.addNamespaceDeclaration(Namespaces.OWL);
			root.addNamespaceDeclaration(Namespaces.BIOPAX);
			biopaxDoc = new Document(root);
			pathway.getBiopax().setBiopax(biopaxDoc);
		}
		return biopaxDoc;
	}
}