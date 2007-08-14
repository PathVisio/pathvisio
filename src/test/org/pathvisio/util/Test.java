package org.pathvisio.util;

import junit.framework.TestCase;

public class Test extends TestCase {
	
	public void testConverter() {
		try {
			Converter.main(new String[] { "testData/test.gpml", "testData/test.svg" });
		} catch(Exception e) {
			e.printStackTrace(System.err);
			fail("Unable to convert GPML file to SVG");
		}
	}
}
