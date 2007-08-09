package org.pathvisio.biopax.reflect;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.pathvisio.model.GpmlFormat;

public class BiopaxProperty extends Element {	
	public static final int UNBOUND = -1;
	
	protected int maxCardinality;
	
	BiopaxProperty(String name, String value, String datatype, int maxCardinality) {
		setName(name);
		setText(value);
		setDatatype(datatype);
		this.maxCardinality = maxCardinality;
	}

	BiopaxProperty(String name, String value, String datatype) {
		this(name, value, datatype, UNBOUND);
	}
	
	BiopaxProperty(Element e) {
		name = e.getName();
		PropertyType pt = PropertyType.valueOf(name);
		if(pt == null) {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
		setText(e.getText());
		setDatatype(pt.datatype);
		maxCardinality = pt.maxCardinality;
	}
	
	public void setDatatype(String datatype) {
		setAttribute("datatype", datatype, Namespaces.RDF);
	}
	
	public String getDatatype() {
		return getAttributeValue("datatype", Namespaces.RDF);
	}
	
	public int getMaxCardinality() {
		return maxCardinality;
	}
}
