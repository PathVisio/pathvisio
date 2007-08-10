package org.pathvisio.biopax.reflect;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.pathvisio.model.GpmlFormat;

public class BiopaxProperty extends Element {	
	public static final int UNBOUND = -1;
	
	protected int maxCardinality;
	
	private BiopaxProperty() {
		setNamespace(Namespaces.BIOPAX);
	}
	
	BiopaxProperty(String name, String value, String datatype, int maxCardinality) {
		this();
		setName(name);
		setText(value);
		setDatatype(datatype);
		this.maxCardinality = maxCardinality;
	}

	BiopaxProperty(String name, String value, String datatype) {
		this(name, value, datatype, UNBOUND);
	}
	
	BiopaxProperty(PropertyType type, String value) {
		this(type.name(), value, type.datatype, type.maxCardinality);
	}
	
	BiopaxProperty(Element e) {
		this();
		setName(e.getName());
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
