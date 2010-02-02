package org.pathvisio.model;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class TestGpml extends TestCase 
{
	/**
	 * Test reading 2010a file, then writing as 2008a
	 */
	public static void testWrite2010() throws IOException, ConverterException
	{
		File in = new File ("testData/WP248_2008a.gpml");
		assertTrue (in.exists());
		
		Pathway pwy = new Pathway();
		pwy.readFromXml(in, true);
		
		File tmp = File.createTempFile("test", "gpml");
		GpmlFormat2010a.GPML_2010A.writeToXml(pwy, tmp, true);		
	}
	
	/**
	 * Test reading 2008a file, then writing as 2010a
	 */
	public static void testRead2010() throws ConverterException, IOException
	{
		File in = new File ("testData/WP248_2010a.gpml");
		assertTrue (in.exists());
		
		Pathway pwy = new Pathway();
		pwy.readFromXml(in, true);
		
		File tmp = File.createTempFile("test", "gpml");		
		GpmlFormat200X.GPML_2008A.writeToXml(pwy, tmp, true);		
	}
	
	private static final File FILE1 = 
		new File ("testData/2008a-deprecation-test.gpml");
	
	public void testDeprecatedFields() throws ConverterException
	{
		assertTrue (FILE1.exists());
		
		Pathway pwy = new Pathway();
		GpmlFormat.readFromXml(pwy, FILE1, true);
		
		PathwayElement dn = pwy.getElementById("e4fa1");
		assertEquals ("This is a backpage head", dn.getDynamicProperty("org.pathvisio.model.BackpageHead"));
	}
	
}
