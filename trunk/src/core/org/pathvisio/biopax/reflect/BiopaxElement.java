package org.pathvisio.biopax.reflect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.pathvisio.model.GpmlFormat;

public class BiopaxElement extends Element {
	private Set<PropertyType> validProperties;
	private List<BiopaxProperty> properties;
	
	public BiopaxElement() {
		setNamespace(GpmlFormat.BIOPAX);
		validProperties = new HashSet<PropertyType>();
		properties = new ArrayList<BiopaxProperty>();
	}
	
	public BiopaxElement(String name, String id) {
		this();
		setName(name);
		setId(id);
	}
	
	protected void setValidProperties(PropertyType[] valid) {
		validProperties = new HashSet<PropertyType>();
		for(PropertyType pt : valid) validProperties.add(pt);
	}
	
	public void addProperty(BiopaxProperty p) {
		//Check if property is valid
		PropertyType pt = PropertyType.valueOf(p.getName());
		if(!validProperties.contains(pt)) {
			throw new IllegalArgumentException("Property " + p.getName() + " is not valid for " + this);
		}
		List<BiopaxProperty> existingProps = getProperties(p.getName());
		if(p.getMaxCardinality() != BiopaxProperty.UNBOUND &&
				existingProps.size() >= p.getMaxCardinality()) {
			//Replace the first occuring property
			int first = getFirstPropertyIndex(p.getName());
			properties.remove(first);
			properties.add(first, p);
		} else {
			properties.add(p);
		}
		addContent(p);
	}
	
	public void removeProperty(BiopaxProperty p) {
		BiopaxProperty existing = properties.get(properties.indexOf(p));
		if(existing != null) {
			properties.remove(p);
			removeContent(p);
		}
	}
	
	private int getFirstPropertyIndex(String name) {
		int i = 0;
		for(BiopaxProperty p : properties) {
			if(p.getName().equals(name)) break;
			i++;
		}
		return i;
	}
	
	public List<BiopaxProperty> getProperties(String name) {
		List<BiopaxProperty> props = new ArrayList<BiopaxProperty>();
		for(BiopaxProperty p : properties) {
			if(p.getName().equals(name)) {
				props.add(p);
			}
		}
		return props;
	}
	
	/**
	 * Returns the first property with the given name
	 * @param name
	 * @return
	 */
	public BiopaxProperty getProperty(String name) {
		for(BiopaxProperty p : properties) {
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	public String getId() {
		return getAttributeValue("id", Namespaces.RDF);
	}

	public void setId(String id) {
		setAttribute("id", id, Namespaces.RDF);
	}
	
	public static BiopaxElement fromXML(Element xml) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class c = Class.forName("org.pathvisio.biopax.reflect." + xml.getName());
		BiopaxElement elm = (BiopaxElement)c.newInstance();
		elm.loadXML(xml);
		return elm;
	}
	
	void loadXML(Element xml) {
		setName(xml.getName());
		setNamespace(xml.getNamespace());
		setId(xml.getAttributeValue("id", Namespaces.RDF));
		for(Object child : xml.getChildren()) {
			if(child instanceof Element) {
				addProperty(new BiopaxProperty((Element)child));
			}
		}
	}
		
	public Document addToDocument(Document d) {
		if(d == null) {
			//Create a biopax document
			Element root = new Element("RDF", Namespaces.RDF);
			root.addNamespaceDeclaration(Namespaces.RDFS);
			root.addNamespaceDeclaration(Namespaces.RDF);
			root.addNamespaceDeclaration(Namespaces.OWL);
			root.addNamespaceDeclaration(Namespaces.BIOPAX);
			d = new Document(root);
		}
		
		//Check if this is a valid biopax document
		Element root = d.getRootElement();
		if(!root.getNamespace().equals(Namespaces.RDF)) {
			throw new IllegalArgumentException("Invalid root element: " + root);
		}
		
		//Add this element to the document
		d.getRootElement().addContent(this);
		return d;
	}

	public void removeFromDocument(Document d) {
		if(d == null) return;
		d.getRootElement().removeContent(this);
	}
}
