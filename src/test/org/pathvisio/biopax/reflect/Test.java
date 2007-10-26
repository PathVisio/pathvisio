// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.biopax.reflect;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

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
		
		Pathway newData = readWrite(data);
		
		biopax = new BiopaxElementManager(newData.getMappInfo());
		List<PublicationXRef> references = biopax.getPublicationXRefs();
		//There has to be one reference
		assertTrue("One literature reference, has " + references.size(), references.size() == 1);
		//With two authors
		assertTrue("Two authors", references.get(0).getAuthors().size() == 2);
		
		//Test added 30-08, because of bug where biopax was lost after
		//saving/loading/saving sequence
		//Add another reference to Pathway
		xrefPathway = new PublicationXRef(biopax.getUniqueID());
		//Add one title and one author
		xrefPathway.setTitle("title3");
		xrefPathway.addAuthor("author3");
		
		biopax.addElementReference(xrefPathway);
		
		newData = readWrite(newData);
		
		biopax = new BiopaxElementManager(newData.getMappInfo());
		references = biopax.getPublicationXRefs();
		//There have to be two references now
		assertTrue("Two literature references, has " + references.size(), references.size() == 2);
		//Where the one we last added has one author
		PublicationXRef xref = (PublicationXRef)biopax.getElementById(xrefPathway.getId());
		assertTrue("One author", xref.getAuthors().size() == 1);
		
	}
	
	public Pathway readWrite(Pathway data) {
		//Write
		try {
			data.writeToXml(new File("testData/test-biopax.xml"), true);
		} catch(ConverterException e) {
			fail("Unable to write a pathway: " + e.toString());
		}
		
		//Read
		Pathway newData = new Pathway();
		try {
			newData.readFromXml(new File("testData/test-biopax.xml"), true);
		} catch(ConverterException e) {
			fail("Unable to read a pathway: " + e.toString());
		}
		return newData;
	}
}
