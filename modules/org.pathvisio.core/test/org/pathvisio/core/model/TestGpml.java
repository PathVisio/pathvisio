// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.model;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class TestGpml extends TestCase 
{
	private static final File PATHVISIO_BASEDIR = new File ("../..");
	/**
	 * Test reading 2010a file, then writing as 2008a
	 */
	public static void testWrite2010() throws IOException, ConverterException
	{
		File in = new File (PATHVISIO_BASEDIR, "testData/WP248_2008a.gpml");
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
		File in = new File (PATHVISIO_BASEDIR, "testData/WP248_2010a.gpml");
		assertTrue (in.exists());
		
		Pathway pwy = new Pathway();
		pwy.readFromXml(in, true);
	}
	
	private static final File FILE1 = 
		new File (PATHVISIO_BASEDIR, "testData/2008a-deprecation-test.gpml");
	
	public void testDeprecatedFields() throws ConverterException
	{
		assertTrue (FILE1.exists());
		
		Pathway pwy = new Pathway();
		GpmlFormat.readFromXml(pwy, FILE1, true);
		
		PathwayElement dn = pwy.getElementById("e4fa1");
		assertEquals ("This is a backpage head", dn.getDynamicProperty("org.pathvisio.model.BackpageHead"));
	}
	
}
