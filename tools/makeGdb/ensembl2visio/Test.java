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
package ensembl2visio;

import junit.framework.TestCase;
import java.sql.SQLException;

public class Test extends TestCase
{
	public void creationTest (GdbMaker gdbMaker) throws SQLException, ClassNotFoundException
	{
		gdbMaker.connect (true);
		gdbMaker.createTables();
		gdbMaker.preInsert();
		int error = 0;
		error += gdbMaker.addGene("ENS001", "1234", "L", "<b>test</b>");
		error += gdbMaker.addLink("ENS001", "1234", "L");
		int geneCount = gdbMaker.getGeneCount();
		gdbMaker.close();
		gdbMaker.postInsert ();
		assertEquals (error, 0);
		assertEquals (geneCount, 1);		
	}
	
	public void testDerby() throws SQLException, ClassNotFoundException
	{
		GdbMaker gdbMaker = new DerbyGdbMaker ("test.pgdb");
		creationTest (gdbMaker);
	}

	public void testH2() throws SQLException, ClassNotFoundException
	{
		GdbMaker gdbMaker = new H2GdbMaker ("test.pgdb");
		creationTest (gdbMaker);
	}

	public void testHsqldb() throws SQLException, ClassNotFoundException
	{
		GdbMaker gdbMaker = new HsqldbGdbMaker ("test.pgdb");
		creationTest (gdbMaker);
	}
}