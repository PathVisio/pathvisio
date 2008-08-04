package org.pathvisio.cytoscape;

import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;

import cytoscape.data.CyAttributes;


public interface AttributeMapper {
	public void setMapping(String attr, PropertyType prop);
	public void attributesToProperties(String id, PathwayElement elm, CyAttributes attr);
	public void propertiesToAttributes(String id, PathwayElement elm, CyAttributes attr);
}
