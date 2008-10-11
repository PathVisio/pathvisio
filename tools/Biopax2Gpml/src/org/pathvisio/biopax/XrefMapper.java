package org.pathvisio.biopax;

import org.biopax.paxtools.model.level2.entity;
import org.pathvisio.model.PathwayElement;

/**
 * Implement this interface to provide mappings
 * for BioPAX xrefs to GPML xrefs.
 * @author thomas
 */
public interface XrefMapper {
	/**
	 * Map the xref property for the given BioPAX element
	 * to the GPML pathway element.
	 * @param e The BioPAX element
	 * @param pwElm The GPML element
	 */
	public void mapXref(entity e, PathwayElement pwElm);
}
