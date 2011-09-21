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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pathvisio.core.biopax.reflect.BiopaxElement;
import org.pathvisio.core.biopax.reflect.PublicationXref;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.PathwayElement;

/**
 * This class handles all biopax references for a given pathway element
 * @author thomas
 *
 */
public class BiopaxReferenceManager {
	private PathwayElement pwElement;

	/**
	 * Constructor for this class
	 * @param mgr	The BiopaxElementManager that manages the biopax elements
	 * for the pathway pathway element <code>e</code> belongs to
	 * @param e The pathway element to handle the biopax references for
	 */
	public BiopaxReferenceManager(PathwayElement e) {
		pwElement = e;
	}

	public BiopaxElementManager getBiopaxElementManager() {
		return pwElement.getParent().getBiopaxElementManager();
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
			BiopaxElement bpe = getBiopaxElementManager().getElement(ref);
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
		for(BiopaxElement e : getReferences()) {
			if(e instanceof PublicationXref) xrefs.add((PublicationXref)e);
		}
		Collections.sort(xrefs, new Comparator<PublicationXref>() {
			public int compare(PublicationXref o1, PublicationXref o2) {
				BiopaxElementManager elmMgr = getBiopaxElementManager();
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
	public void addElementReference(BiopaxElement e) {
		//Will be added to the BioPAX document if not already in there
		getBiopaxElementManager().addElement(e);

		//Add a reference to the biopax element
		pwElement.addBiopaxRef(e.getId());

		fireBiopaxEvent(new BiopaxEvent(this));
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
		if(!getBiopaxElementManager().hasReferences(e)) {
			getBiopaxElementManager().removeElement(e);
		}

		fireBiopaxEvent(new BiopaxEvent(this));
	}

	private void fireBiopaxEvent(BiopaxEvent e) {
		for(BiopaxListener l : listeners) {
			l.biopaxEvent(e);
		}
	}

	List<BiopaxListener> listeners = new ArrayList<BiopaxListener>();

	public void addBiopaxListener(BiopaxListener l) {
		if(!listeners.contains(l)) listeners.add(l);
	}

	public void removeBiopaxListener(BiopaxListener l) {
		listeners.remove(l);
	}

	public void copyBiopaxListeners(BiopaxReferenceManager refMgr) {
		for(BiopaxListener l : refMgr.listeners) {
			addBiopaxListener(l);
		}
	}
}
