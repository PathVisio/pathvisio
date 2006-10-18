package data;

import java.io.*;
import java.util.*;

import debug.Logger;
import junit.framework.TestCase;

public class Test extends TestCase implements GmmlListener {

	GmmlData data;
	GmmlDataObject o;
	List<GmmlEvent> received;
	GmmlDataObject l;
	
	public void setUp()
	{
		data = new GmmlData();
		o = new GmmlDataObject(ObjectType.GENEPRODUCT);
		received = new ArrayList<GmmlEvent>();
		o.addListener(this);
		o.setParent(data);
		l = new GmmlDataObject(ObjectType.LINE);		
		l.setParent(data);
	}
	
	public void testFields ()
	{
		o.setCenterX(1.0);
		
		assertEquals ("test set/get CenterX", 1.0, o.getCenterX(), 0.0001);		
		
		assertEquals ("Setting CenterX should generate single event", received.size(), 1);
		assertEquals ("test getProperty()", 1.0, (Double)o.getProperty("CenterX"), 0.0001);
		
		try 
		{
			o.setProperty("CenterX", null);
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
			o.setProperty("asdfg", new Object());
			fail("Setting non-existing property should generate exception");
		}
		catch (Exception e) {}

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
		assertTrue ("Setting parent adds to container", data.getDataObjects().contains(o));
		
		o.setParent(null);
		assertFalse ("Setting parent null removes from container", data.getDataObjects().contains(o));
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
		o2.setParent(data);
		
		// create link in opposite order
		o.setGraphId("2");
		l.setEndGraphRef("2");		
		assertTrue ("reference created (2)", data.getReferringObjects("2").contains(l));
	}
	
	public void testRefUniq()
	{
		// test for uniqueness
		o.setGraphId("1");

		GmmlDataObject o2 = new GmmlDataObject(ObjectType.GENEPRODUCT);
		o2.setParent(data);
		try
		{
			
			o2.setGraphId("1");
			fail("graphId's should be unique");
		}
		catch (IllegalArgumentException e) {}	
	}
	
	public void testRef2()
	{
		o.setGraphId("1");

		GmmlDataObject o2 = new GmmlDataObject(ObjectType.GENEPRODUCT);		
		// note: parent not set yet!		
		o2.setGraphId ("3");
		o2.setParent(data); // reference should now be created

		assertNull ("default endGraphRef is null", l.getEndGraphRef());
		
		l.setEndGraphRef("3");

		assertTrue ("reference created through setparent", data.getReferringObjects("3").contains(l));
	}
	
	public void testXml() throws IOException, ConverterException
	{
		data.readFromXml(new File("testData/test.gmml"), false);
		assertTrue ("Loaded a bunch of objects from xml", data.getDataObjects().size() > 20);
		File temp = File.createTempFile ("data.test", ".gmml");
		temp.deleteOnExit();
		data.writeToXml(temp, false);
	}

	public void testMapp() throws IOException, ConverterException
	{
		// TODO: this first line shouldn't be necessary
		MappFormat.log = new Logger();
		data.readFromMapp(new File("testData/test.mapp"));
		assertTrue ("Loaded a bunch of objects from mapp", data.getDataObjects().size() > 20);
		File temp = File.createTempFile ("data.test", ".mapp");
		temp.deleteOnExit();
		data.writeToMapp(temp);
	}

	// event listener
	public void gmmlObjectModified(GmmlEvent e) 
	{
		// store all received events
		received.add(e);
	}
}
