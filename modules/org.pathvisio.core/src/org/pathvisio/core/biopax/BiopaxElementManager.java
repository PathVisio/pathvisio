// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.biopax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jdom.Document;
import org.jdom.Element;
import org.pathvisio.core.biopax.reflect.BiopaxElement;
import org.pathvisio.core.biopax.reflect.BiopaxProperty;
import org.pathvisio.core.biopax.reflect.Namespaces;
import org.pathvisio.core.biopax.reflect.PropertyType;
import org.pathvisio.core.biopax.reflect.PublicationXref;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;

/**
 * This class keeps track of all BioPAX elements in the pathway
 */
public class BiopaxElementManager extends PathwayElement 
{
	/**
	 * Constructor for this class. Builds a map of all biopax
	 * elements and their references
	 */
	public BiopaxElementManager()
	{
		super(ObjectType.BIOPAX);
		biopax = new HashMap<String, BiopaxElement>();
		ordinal = new HashMap<Class<? extends BiopaxElement>, Map<String, Integer>>();
		refresh();
	}
	
	private Document document;
	
	private Random random = new Random(); //Used to generate unique id's
	private Map<String, BiopaxElement> biopax;
	/**
	 * Keeps track of the order of the loaded biopax elements per subclass.
	 */
	private Map<Class<? extends BiopaxElement>, Map<String, Integer>> ordinal;

	/**
	 * Check if the pathway element that contains the biopax document has changed
	 * and update the biopax hashmap if needed.
	 */
	public void refresh() 
	{
		if(parent == null) return;
		
		Logger.log.trace("Refreshing biopax");
		biopax.clear();
		ordinal.clear();

		Logger.log.trace("Biopax element found");
		
		if(document != null) 
		{
			Map<BiopaxElement, Element> oldElements = new HashMap<BiopaxElement, Element>();
			Element root = document.getRootElement();
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
				root.addContent(bpe.getWrapped());
				root.removeContent(oldElements.get(bpe));
			}
		}
	}

	/**
	 * Remove a biopax element from the biopax document within the GPML file.
	 * Note: references to this element will <B>NOT</B> be removed!
	 * @param e
	 */
	public void removeElement(BiopaxElement e) {
		Document doc = getDocument();
		System.err.println("removed: " + doc.getRootElement().removeContent(e.getWrapped()));
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
		for(PathwayElement pwe : parent.getDataObjects()) {
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
		d.getRootElement().addContent(elm.getWrapped());
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
		refresh();
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
	private Document getDocument() 
	{
		if (document == null)
		{
			//Create a biopax document
			Element root = new Element("RDF", Namespaces.RDF);
			root.addNamespaceDeclaration(Namespaces.RDFS);
			root.addNamespaceDeclaration(Namespaces.RDF);
			root.addNamespaceDeclaration(Namespaces.OWL);
			root.addNamespaceDeclaration(Namespaces.BIOPAX);
			document = new Document(root);
		}
		return document;
	}
	
	/** @deprecated backwards compatibility hack for pwy.getBiopax().getBiopax() */
	public Document getBiopax()
	{
		return getDocument();
	}
	
	/** @deprecated backwards compatibility hack for pwy.getBiopax().setBiopax() */
	public void setBiopax(Document d)
	{
		document = d;
		refresh();
	}

	public void mergeBiopax(BiopaxElementManager bpnew) 
	{
		if(bpnew == null) return;

		Document dNew = bpnew.getBiopax();
		Document dOld = getDocument();

		if(dNew == null) {
			return; //Nothing to merge
		}

		//Create a map of existing biopax elements with an id
		Map<String, Element> bpelements = new HashMap<String, Element>();
		for(Object o : dOld.getRootElement().getContent()) {
			if(o instanceof Element) {
				Element e = (Element)o;
				String id = e.getAttributeValue("id", GpmlFormat.RDF);
				if(id != null) bpelements.put(id, e);
			}
		}

		//Replace existing elements with the new one, or add if none exist yet
		for(Object o : dNew.getRootElement().getContent()) {
			if(o instanceof Element) {
				Element eNew = (Element)o;
				String id = eNew.getAttributeValue("id", GpmlFormat.RDF);
				Element eOld = bpelements.get(id);
				if(eOld != null) { //If an elements with the same id exist, remove it
					dOld.getRootElement().removeContent(eOld);
				}
				dOld.getRootElement().addContent((Element)eNew.clone());
			}
		}
	}

}