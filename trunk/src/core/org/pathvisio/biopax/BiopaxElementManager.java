package org.pathvisio.biopax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jdom.Document;
import org.jdom.Element;
import org.pathvisio.biopax.reflect.BiopaxElement;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class BiopaxElementManager {
	Random random = new Random();
	
	private PathwayElement pwElement;
	private HashMap<String, BiopaxElement> biopax;
	
	public BiopaxElementManager(PathwayElement e) {
		pwElement = e;
		biopax = new HashMap<String, BiopaxElement>();
		
		PathwayElement bp = e.getParent().getBiopax();
		if(bp != null) {
			Document d = bp.getBiopax();
			if(d != null) {
				Element root = d.getRootElement();
				for(Object child : root.getChildren()) {
					if(child instanceof Element) {
						try {
							BiopaxElement bpe = BiopaxElement.fromXML((Element)child);
							biopax.put(bpe.getId(), bpe);
						} catch(Exception ex) {
							Logger.log.error("Biopax element " + child + " ignored", ex);
						}
					}
				}
			}
		}
	}
	
	public List<BiopaxElement> getReferences() {
		List<String> refs = pwElement.getBiopaxRefs();
		List<BiopaxElement> bpElements = new ArrayList<BiopaxElement>();
		for(BiopaxElement e : biopax.values()) {
			if(refs.contains(e.getId())) bpElements.add(e);
		}
		return bpElements;
	}
	
	public List<PublicationXRef> getPublicationXRefs() {
		List<PublicationXRef> xrefs = new ArrayList<PublicationXRef>();
		for(BiopaxElement e : getReferences()) {
			if(e instanceof PublicationXRef) xrefs.add((PublicationXRef)e);
		}
		return xrefs;
	}
	
	public void addElementReference(BiopaxElement e) {
		//Add element to the biopax GPML element
		PathwayElement bpe = pwElement.getParent().getBiopax();
		if(bpe == null) {
			pwElement.getParent().createBiopax();
			bpe = pwElement.getParent().getBiopax();
		}
		Document bpDoc = pwElement.getParent().getBiopax().getBiopax();
		Document newDoc = e.addToDocument(bpDoc);
		if(bpDoc != newDoc) pwElement.getParent().getBiopax().setBiopax(newDoc);
		
		//Add a reference to the biopax element
		pwElement.addBiopaxRef(e.getId());
		biopax.put(e.getId(), e);
	}
	
	public void removeElementReference(BiopaxElement e) {
		//Remove the reference to the element
		pwElement.removeBiopaxRef(e.getId());
		
		//Remove element from the biopax GPML element
		//Only if there are no references to this element
		if(!hasReferences(pwElement.getParent(), e)) {
			PathwayElement bpe = pwElement.getParent().getBiopax();
			if(bpe != null) {
				e.removeFromDocument(bpe.getBiopax());
			}
		}
		biopax.remove(e.getId());
	}
	
	public boolean hasReferences(Pathway p, BiopaxElement e) {
		//Check for references in child objects
		for(PathwayElement pwe : p.getDataObjects()) {
			if(pwe.getBiopaxRefs().contains(e.getId())) {
				return true;
			}
		}
		return false;
	}
	
	public String getUniqueID() {
		String id = createId(random);
		int mod = 0x600; // 3 hex letters
		int min = 0xa00; // has to start with a letter
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
	
	private String createId(Random r) {
		return "GPML_" + r.nextLong();
	}
}
