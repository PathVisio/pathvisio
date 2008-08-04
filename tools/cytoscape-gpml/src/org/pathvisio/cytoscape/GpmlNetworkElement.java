package org.pathvisio.cytoscape;

import giny.view.GraphView;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.PathwayElement;

import cytoscape.data.CyAttributes;

public abstract class GpmlNetworkElement<T> {
	T parent; //A CyNode or CyEdge
	
	protected PathwayElement pwElmOrig; 	//Original patwhay element
							  			//Saves the state after importing
	private PathwayElement pwElmCy;		//Pathway element used to synchronize with
										//corresponding Cytoscape element
	
	public GpmlNetworkElement(T parent, PathwayElement pwElm) {
		this(parent, pwElm, null);
	}
		
	protected GpmlNetworkElement(T parent, PathwayElement pwElm, AttributeMapper attributeMapper) {
		this.parent = parent;
		this.pwElmOrig = pwElm;
		pwElmCy = pwElmOrig.copy();
		if(attributeMapper != null) resetToGpml(attributeMapper);
	}
		
	/**
	 * Get the pathway element containing the GPML data.
	 * @note the returned pathway element may be out of sync
	 * with the corresponding Cytoscape element. Use {@link #getPathwayElement(GraphView)}
	 * to ensure that the properties are up to date with the given view.
	 * @return The pathway element containing the GPML data
	 */
	public PathwayElement getPathwayElement() {
		return getPathwayElement(null, null);
	}
	
	/**
	 * Get the pathway element containing the GPML data.
	 * @param view If not null, the pathway element will be updated
	 * with the properties of it's parent in that view (e.g. coordinates)
	 * @return The pathway element containing the GPML data
	 */
	public PathwayElement getPathwayElement(GraphView view, AttributeMapper attributeMapper) {
		if(view != null) {
			updateFromCytoscape(view, attributeMapper);
		}
		return pwElmCy;
	}
	
	protected PathwayElement getPwElmCy() {
		return pwElmCy;
	}
	
	public T getParent() {
		return parent;
	}
		
	public abstract String getParentIdentifier();
	
	public abstract CyAttributes getCyAttributes();
	
	public void updateFromCytoscape(GraphView view, AttributeMapper attributeMapper) {
		Logger.log.trace("Updating " + this + " from cytoscape");
		if(attributeMapper != null) {
			Logger.log.trace("Transfer attributes");
			attributeMapper.attributesToProperties(getParentIdentifier(), getPwElmCy(), getCyAttributes());
		}
	}
	
	public void resetToGpml(AttributeMapper attributeMapper) {
		Logger.log.trace("Resetting " + this + " to GPML");
		attributeMapper.propertiesToAttributes(getParentIdentifier(), pwElmOrig, getCyAttributes());
		pwElmCy = pwElmOrig.copy();
	}
	
	public void resetToGpml(AttributeMapper attributeMapper, GraphView view) {
		resetToGpml(attributeMapper);
	}
	
//	/**
//	 * Transfer the GPML information to the Cytoscape attributes of the parent
//	 * network element
//	 * @param attr	The attributes to transfer the information to
//	 */
//	private void transferAttributes() {
//		String id = getParentIdentifier();
//		CyAttributes attr = getCyAttributes();
//		try {
//			Element e = null;
//			if(pwElmOrig.getObjectType() == ObjectType.MAPPINFO) { //Special treatment for MappInfo
//				e = new Element("MappInfo");
//				//TODO: Transfer pathway info
//			} else {
//				e = GpmlFormat.createJdomElement(pwElmOrig, Namespace.getNamespace(""));
//				attr.setAttribute(id, "GpmlElement", e.getName());
//			}
//			transferAttributes(id, e, attr, null);
//		} catch(Exception e) {
//			Logger.log.error("Unable to add attributes for " + pwElmOrig, e);
//		}
//		//Set canonicalName to text label...TODO: make this configurable
//		attr.setAttribute(id, "canonicalName", pwElmOrig.getTextLabel());
//	}
//    
//    private void transferAttributes(String id, Element e, CyAttributes attr, String key) {
//    	List<Attribute> attributes = e.getAttributes();
//    	for(int i = 0; i < attributes.size(); i++) {
//    		Attribute a = attributes.get(i);
//    		if(key == null) {
//    			attr.setAttribute(id, a.getName(), a.getValue());
//    		} else {
//        	    attr.setAttribute(id, key + '.' + a.getName(), a.getValue());
//    		}
//    	}
//    }
}
