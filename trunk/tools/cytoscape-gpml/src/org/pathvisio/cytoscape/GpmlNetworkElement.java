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
package org.pathvisio.cytoscape;

import cytoscape.data.CyAttributes;

import giny.view.GraphView;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.PathwayElement;

public abstract class GpmlNetworkElement<T> {
	public static final String ATTR_TYPE = "gpml-type";

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
//		if(attributeMapper != null) resetToGpml(attributeMapper);
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

	protected void setPwElmCy(PathwayElement pwElmCy) {
		this.pwElmCy = pwElmCy;
	}

	protected void setPwElmOrig(PathwayElement pwElmOrig) {
		this.pwElmOrig = pwElmOrig;
		setPwElmCy(pwElmOrig.copy());
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

	public void updateFromGpml(AttributeMapper attributeMapper) {
		Logger.log.trace("Resetting " + this + " to GPML");
		attributeMapper.propertiesToAttributes(getParentIdentifier(), pwElmOrig, getCyAttributes());
		getCyAttributes().setAttribute(getParentIdentifier(), ATTR_TYPE, pwElmOrig.getObjectType().ordinal());
		pwElmCy = pwElmOrig.copy();
	}

	public void updateFromGpml(AttributeMapper attributeMapper, GraphView view) {
		updateFromGpml(attributeMapper);
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
