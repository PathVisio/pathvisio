package org.pathvisio.data;

import junit.framework.TestCase;

public class Test extends TestCase {

	public void testPubMedQuery() {
		String id = "17588266";
		PubMedQuery pmq = new PubMedQuery(id);
		try {
			pmq.execute();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		PubMedResult pmr = pmq.getResult();
		assertTrue(pmr.getId().equals(id));
		assertTrue("GenMAPP 2: new features and resources for pathway analysis.".equals(pmr.getTitle()));
	}
}
