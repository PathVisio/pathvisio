package org.pathvisio.biopax.reflect;

import java.util.List;

import junit.framework.TestCase;

import org.jdom.Element;

public class Test extends TestCase {
	public void testProperties() {
		PublicationXRef xref = new PublicationXRef("test");
		
		//Check cardinality
		BiopaxProperty p1 = PropertyType.TITLE.getProperty("title 1");
		BiopaxProperty p2 = PropertyType.TITLE.getProperty("title 2");
		List<BiopaxProperty> properties = null;
		xref.addProperty(p1);
		properties = xref.getProperties(p1.getName());
		assertTrue(properties.size() == 1);
		xref.addProperty(p2);
		properties = xref.getProperties(p2.getName());
		assertTrue(properties.size() == 1);
		assertTrue(properties.get(0) == p2);
		
		
		//Add a valid property
		try {
			xref.addProperty(PropertyType.TITLE.getProperty("a title"));
		} catch(IllegalArgumentException e) {
			fail("Failed to add a valid property: " + e.getMessage());
		}
		//Add an invalid property
		try {
			xref.addProperty(new BiopaxProperty("doesntexist", "value", "datatype"));
			fail("Succeeded to add an invalid property");
		} catch(IllegalArgumentException e) { }
	}
	
	public void testFromXML() {
		//Valid element
		Element xml = new Element("PublicationXRef", Namespaces.BIOPAX);
		xml.setAttribute("id", "testid", Namespaces.RDF);
		Element prop = new Element("TITLE", Namespaces.BIOPAX);
		prop.setAttribute("datatype", PropertyType.TITLE.datatype, Namespaces.RDF);
		prop.setText("a title");
		xml.addContent(prop);
		
		try {
			BiopaxElement.fromXML(xml);
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to create BiopaxElement from valid XML: " + e.getMessage());
		}
		
		//Invalid element
		prop.setName("doesntexist");
		prop.removeAttribute("datatype", Namespaces.RDF);
		try {
			BiopaxElement.fromXML(xml);
			fail("Succeeded to create BiopaxElement from invalid XML");
		} catch(Exception e) {	}
	}
}
