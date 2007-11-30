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
package org.pathvisio.data;

import junit.framework.TestCase;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine.ApplicationEventListener;

public class Test extends TestCase implements ApplicationEventListener
{
	
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

	boolean eventReceived = false;

	public void applicationEvent (ApplicationEvent e)
	{
		if (e.getType() == ApplicationEvent.GDB_CONNECTED)
		{
			eventReceived = true;
		}
	}
	
	public void testDataSource()
	{
		DataSource ds = DataSource.ENSEMBL;
		assertEquals (ds.getFullName(), "Ensembl");
		assertEquals (ds.getSystemCode(), "En");
		
		DataSource.register("@@", "ZiZaZo", null, null, null);
		
		DataSource ds2 = DataSource.getBySystemCode ("@@");
		DataSource ds3 = DataSource.getByFullName ("ZiZaZo");
		assertEquals (ds2, ds3);
		
		// assert that you can refer to 
		// undeclared systemcodes if necessary.
		assertNotNull (DataSource.getBySystemCode ("##"));
		
		DataSource ds4 = DataSource.getBySystemCode ("En");
		assertEquals (ds, ds4);
		
		DataSource ds5 = DataSource.getByFullName ("Entrez Gene");
		assertEquals (ds5, DataSource.ENTREZ_GENE);
	}
	
	public void testGdbConnect()
	{
		//TODO: create test pgdb
		// suitable for mapping Hs_Apoptosis genes.
		
		// Gdb.connect ("test.pgdb");

		// assertTrue (eventReceived);
		// test reception of event...
	}
	
	
	
}
