/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.biopax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.PathwayElement;

/**
 * This class handles all biopax references for a given pathway element
 * @author thomas
 *
 */
public class BiopaxReferenceManager {
	private final PathwayElement pwElement;

	/**
	 * Constructor for this class
	 * @param mgr	The BiopaxElementManager that manages the biopax elements
	 * for the pathway pathway element <code>e</code> belongs to
	 * @param e The pathway element to handle the biopax references for
	 */
	public BiopaxReferenceManager(PathwayElement e) {
		pwElement = e;
	}

	public BiopaxElement getBiopaxElementManager() {
		return pwElement.getParent().getBiopaxElementManager();
	}

	/**
	 * Get all biopax elements that the pathway element has
	 * a reference to
	 * @return A List with all referred biopax element, or an empty list
	 * if no elements have been found
	 */
	public List<BiopaxNode> getReferences() {
		List<String> refs = pwElement.getBiopaxRefs();
		List<BiopaxNode> bpElements = new ArrayList<BiopaxNode>();
		for(String ref : refs) {
			BiopaxNode bpe = getBiopaxElementManager().getElement(ref);
			if(bpe != null) {
				bpElements.add(bpe);
			} else {
				Logger.log.warn("Reference to non existing biopax element found: " + ref);
			}
		}
		return bpElements;
	}

	/**
	 * Get all publications that the pathway element has a reference to
	 * @return A List with all referred publications, or an empty list
	 * if no elements have been found
	 */
	public List<PublicationXref> getPublicationXRefs() {
		List<PublicationXref> xrefs = new ArrayList<PublicationXref>();
		for(BiopaxNode e : getReferences()) {
			if(e instanceof PublicationXref) xrefs.add((PublicationXref)e);
		}
		Collections.sort(xrefs, new Comparator<PublicationXref>() {
			public int compare(PublicationXref o1, PublicationXref o2) {
				BiopaxElement elmMgr = getBiopaxElementManager();
				return elmMgr.getOrdinal(o1) - elmMgr.getOrdinal(o2);
			}
		});
		return xrefs;
	}

	/**
	 * Add a reference to the given biopax element for the pathway
	 * element this class manages.
	 * @param e The biopax element to add a reference to.
	 */
	public void addElementReference(BiopaxNode e) {
		//Will be added to the BioPAX document if not already in there
		getBiopaxElementManager().addElement(e);

		//Add a reference to the biopax element
		pwElement.addBiopaxRef(e.getId());
	}

	/**
	 * Remove the reference to the given biopax element from the
	 * pathway element this class manages.
	 * @param e The biopax reference to remove the reference for
	 */
	public void removeElementReference(BiopaxNode e) {
		//Remove the reference to the element
		pwElement.removeBiopaxRef(e.getId());

		//Remove element from the biopax GPML element
		//Only if there are no references to this element
		if(!getBiopaxElementManager().hasReferences(e)) {
			getBiopaxElementManager().removeElement(e);
		}
	}

}
