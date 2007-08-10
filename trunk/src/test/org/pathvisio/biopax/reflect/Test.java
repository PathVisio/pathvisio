package org.pathvisio.biopax.reflect;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.jdom.Element;
import org.pathvisio.biopax.BiopaxElementManager;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;

public class Test extends TestCase {
	Pathway data;
	PathwayElement o;
	List<PathwayEvent> received;
	PathwayElement l;
	
	public void setUp()
	{
		data = new Pathway();
		o = new PathwayElement(ObjectType.DATANODE);
		data.add (o);
	}
	
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
		
	public void testReadWrite() {
		//Add to datanode
		BiopaxElementManager biopax = new BiopaxElementManager(o);
		PublicationXRef xrefObject = new PublicationXRef(biopax.getUniqueID());
		//Add one title and two authors
		xrefObject.setTitle("title");
		xrefObject.addAuthor("author1");
		xrefObject.addAuthor("author2");
		
		biopax.addElementReference(xrefObject);
		
		//Add to pathway
		biopax = new BiopaxElementManager(data.getMappInfo());
		PublicationXRef xrefPathway = new PublicationXRef(biopax.getUniqueID());
		//Add one title and two authors
		xrefPathway.setTitle("title");
		xrefPathway.addAuthor("author1");
		xrefPathway.addAuthor("author2");
		
		biopax.addElementReference(xrefPathway);
		
		//Write
		try {
			data.writeToXml(new File("testData/test-biopax.xml"), true);
		} catch(ConverterException e) {
			fail("Unable to write a pathway with PublicationXRef: " + e.toString());
		}
		
		//Read
		Pathway newData = new Pathway();
		try {
			newData.readFromXml(new File("testData/test-biopax.xml"), true);
		} catch(ConverterException e) {
			fail("Unable to read a pathway with PublicationXRef: " + e.toString());
		}
		
		biopax = new BiopaxElementManager(newData.getMappInfo());
		List<PublicationXRef> references = biopax.getPublicationXRefs();
		//There has to be one reference
		assertTrue("One literature reference, has " + references.size(), references.size() == 1);
		//With two authors
		assertTrue("Two authors", references.get(0).getAuthors().size() == 2);
	}
}
