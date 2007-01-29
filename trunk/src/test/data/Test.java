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
package data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class Test extends TestCase implements GmmlListener {

	GmmlData data;
	GmmlDataObject o;
	List<GmmlEvent> received;
	GmmlDataObject l;
	
	public void setUp()
	{
		data = new GmmlData();
		data.addListener(this);
		o = new GmmlDataObject(ObjectType.GENEPRODUCT);
		received = new ArrayList<GmmlEvent>();
		o.addListener(this);
		data.add (o);
		l = new GmmlDataObject(ObjectType.LINE);		
		data.add(l);
		received.clear();
	}
	
	public void testFields ()
	{
		o.setMCenterX(1.0);
		
		assertEquals ("test set/get CenterX", 1.0, o.getMCenterX(), 0.0001);		
		
		assertEquals ("Setting CenterX should generate single event", received.size(), 1);
		assertEquals ("test getProperty()", 1.0, (Double)o.getProperty(PropertyType.CENTERX), 0.0001);
		
		try 
		{
			o.setProperty(PropertyType.CENTERX, null);
			fail("Setting centerx property to null should generate exception");
		}
		catch (Exception e) {}
		
		// however, you should be able to set graphRef to null
		
		assertNull ("graphref null by default", l.getStartGraphRef());
		l.setStartGraphRef(null);
		assertNull ("can set graphRef to null", l.getStartGraphRef());
	}
	
	public void testProperties()
	{
		try 
		{
			o.setProperty(null, new Object());
			fail("Setting null property should generate exception");
		}
		catch (Exception e) {}
	}
	
	public void testColor()
	{
		try
		{
			o.setColor(null);
			fail("Shouldn't be able to set color null");
		}
		catch (Exception e) {}
	}
	
	public void testObjectType()
	{
		assertEquals ("getObjectType() test", o.getObjectType(), ObjectType.GENEPRODUCT);
		
		try
		{
			new GmmlDataObject (-1);
			fail ("Shouldn't be able to set invalid object type");
		}
		catch (IllegalArgumentException e)
		{
		}
		
		try
		{
			new GmmlDataObject (100);
			fail ("Shouldn't be able to set invalid object type");
		}
		catch (IllegalArgumentException e)
		{
		}
	}
	
	public void testParent()
	{				
		// remove
		data.remove (o);
		assertNull ("removing object set parents null", o.getParent());
		assertEquals (received.size(), 1);
		assertEquals ("Event type should be DELETED", received.get(0).getType(), GmmlEvent.DELETED); 
		
		// re-add
		data.add(o);
		assertEquals ("adding sets parent", o.getParent(), data);
		assertEquals (received.size(), 2);
		assertEquals ("Event type should be ADDED", received.get(1).getType(), GmmlEvent.ADDED); 
	}

	/**
	 * Test graphRef's and graphId's
	 *
	 */
	public void testRef()
	{	
		assertNull ("query non-existing list of ref", data.getReferringObjects("abcde"));
		
		// create link
		o.setGraphId("1");
		l.setStartGraphRef("1");		
		assertTrue ("reference created", data.getReferringObjects("1").contains(l));
		
		l.setStartGraphRef("2");
		assertNull ("reference removed", data.getReferringObjects("1"));
		
		GmmlDataObject o2 = new GmmlDataObject(ObjectType.GENEPRODUCT);
		data.add (o2);
		
		// create link in opposite order
		o.setGraphId("2");
		l.setEndGraphRef("2");		
		assertTrue ("reference created (2)", data.getReferringObjects("2").contains(l));
	}
	
	/**
	 * Test for maintaining list of unique id's per GmmlData.
	 *
	 */
	public void testRefUniq()
	{
		// test for uniqueness
		o.setGraphId("1");

		GmmlDataObject o2 = new GmmlDataObject(ObjectType.GENEPRODUCT);
		data.add (o2);
		try
		{			
			// try setting the same id again
			o2.setGraphId("1");
			fail("shouldn't be able to set the same id twice");
		}
		catch (IllegalArgumentException e) {}
		
		// test random id
		String x = data.getUniqueId();
		try
		{			
			// test that we can use it as unique id
			o.setGraphId(x);
			assertEquals (x, o.getGraphId());
			// test that we can't use the same id twice
			o2.setGraphId(x);
			fail("shouldn't be able to set the same id twice");
		}
		catch (IllegalArgumentException e) {}
		
		// test that a second random id is unique again
		x = data.getUniqueId();
		o2.setGraphId(x);
		assertEquals (x, o2.getGraphId());
		
		// test setting id first, then parent
		GmmlDataObject o3 = new GmmlDataObject(ObjectType.GENEPRODUCT);
		x = data.getUniqueId();
		o3.setGraphId(x);
		data.add (o3);
		assertEquals (o3.getGraphId(), x);
		
		try
		{			
			GmmlDataObject o4 = new GmmlDataObject(ObjectType.GENEPRODUCT);
			// try setting the same id again
			o4.setGraphId(x);
			data.add (o4);
			fail("shouldn't be able to set the same id twice");
		}
		catch (IllegalArgumentException e) {}
	}
	
	public void testRef2()
	{
		o.setGraphId("1");

		GmmlDataObject o2 = new GmmlDataObject(ObjectType.GENEPRODUCT);		
		// note: parent not set yet!		
		o2.setGraphId ("3");
		data.add(o2); // reference should now be created

		assertNull ("default endGraphRef is null", l.getEndGraphRef());
		
		l.setEndGraphRef("3");

		assertTrue ("reference created through adding", data.getReferringObjects("3").contains(l));
	}
	
	public void testXml() throws IOException, ConverterException
	{
		data.readFromXml(new File("testData/test.gpml"), false);
		assertTrue ("Loaded a bunch of objects from xml", data.getDataObjects().size() > 20);
		File temp = File.createTempFile ("data.test", ".gpml");
		temp.deleteOnExit();
		data.writeToXml(temp, false);

		try {
			data.readFromXml(new File ("testData/test.mapp"), false);
			fail ("Loading wrong format, Exception expected");
		} catch (Exception e) {}
	}

	/**
	 * test exporting of .mapp (genmapp format)
	 * Note: this test is only run whenever os.name starts with Windows
	 */
	public void testMapp() throws IOException, ConverterException
	{
		if (System.getProperty("os.name").startsWith("Windows"))
		{
			data.readFromMapp(new File("testData/test.mapp"));
			assertTrue ("Loaded a bunch of objects from mapp", data.getDataObjects().size() > 20);
			File temp = File.createTempFile ("data.test", ".mapp");
			temp.deleteOnExit();
			data.writeToMapp(temp);
			
			try {
				data.readFromMapp(new File ("testData/test.gpml"));
				fail ("Loading wrong format, Exception expected");
			} catch (Exception e) {}
		}	
	}

	/**
	 * Test that there is one and only one MAPPINFO object
	 *
	 */
	public void testMappInfo()
	{
		GmmlDataObject mi;

		mi = data.getMappInfo();
		assertEquals (mi.getObjectType(), ObjectType.MAPPINFO); 

		try
		{
			mi = new GmmlDataObject(ObjectType.MAPPINFO);
			data.add (mi);
			fail("data should already have a MAPPINFO and shouldn't accept more");
		}
		catch (IllegalArgumentException e) {}
		
		mi = data.getMappInfo();
		try
		{
			data.remove(mi);
			fail ("Shouldn't be able to remove mappinfo object!");
		}
		catch (IllegalArgumentException e) {}
	}
	
	// event listener
	// receives events generated on objects o and data
	public void gmmlObjectModified(GmmlEvent e) 
	{
		// store all received events
		received.add(e);
	}
}
