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
package org.pathvisio.biopax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.pathvisio.biopax.reflect.BiopaxElement;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

/**
 * This class handles all biopax references for a given pathway element
 * @author thomas
 *
 */
public class BiopaxReferenceManager {
	private BiopaxElementManager elementManager;
	private PathwayElement pwElement;
	
	/**
	 * Constructor for this class
	 * @param mgr	The BiopaxElementManager that manages the biopax elements
	 * for the pathway pathway element <code>e</code> belongs to
	 * @param e The pathway element to handle the biopax references for
	 */
	public BiopaxReferenceManager(BiopaxElementManager mgr, PathwayElement e) {
		pwElement = e;
		elementManager = mgr;
	}
	
	/**
	 * Get all biopax elements that the pathway element has
	 * a reference to
	 * @return A List with all referred biopax element, or an empty list
	 * if no elements have been found
	 */
	public List<BiopaxElement> getReferences() {
		List<String> refs = pwElement.getBiopaxRefs();
		List<BiopaxElement> bpElements = new ArrayList<BiopaxElement>();
		for(String ref : refs) {
			BiopaxElement bpe = elementManager.getElement(ref);
			if(bpe != null) {
				bpElements.add(bpe);
			}
		}
		return bpElements;
	}
	
	/**
	 * Get all publications that the pathway element has a reference to
	 * @return A List with all referred publications, or an empty list
	 * if no elements have been found
	 */
	public List<PublicationXRef> getPublicationXRefs() {
		List<PublicationXRef> xrefs = new ArrayList<PublicationXRef>();
		for(BiopaxElement e : getReferences()) {
			if(e instanceof PublicationXRef) xrefs.add((PublicationXRef)e);
		}
		return xrefs;
	}
	
	/**
	 * Add a reference to the given biopax element for the pathway
	 * element this class manages.
	 * @param e The biopax element to add a reference to.
	 */
	public void addElementReference(BiopaxElement e) {
		//Will be added to the BioPAX document if not already in there
		elementManager.addElement(e);
		
		//Add a reference to the biopax element
		pwElement.addBiopaxRef(e.getId());
	}
	
	/**
	 * Remove the reference to the given biopax element from the
	 * pathway element this class manages.
	 * @param e The biopax reference to remove the reference for
	 */
	public void removeElementReference(BiopaxElement e) {
		//Remove the reference to the element
		pwElement.removeBiopaxRef(e.getId());
		
		//Remove element from the biopax GPML element
		//Only if there are no references to this element
		if(!elementManager.hasReferences(e)) {
			elementManager.removeElement(e);
		}
	}
}
