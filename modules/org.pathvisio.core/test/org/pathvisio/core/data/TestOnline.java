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
package org.pathvisio.core.data;

import junit.framework.TestCase;

public class TestOnline extends TestCase
{
	public void testPubMedQuery()
	{
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