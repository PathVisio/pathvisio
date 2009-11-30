// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import org.pathvisio.Engine;
import org.pathvisio.data.DBConnDerby;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdbFactory;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;

public class Test extends TestCase
{
	public void creationTest (SimpleGdb gdbMaker) throws DataException, ClassNotFoundException
	{
		Engine.init();
		gdbMaker.createGdbTables();
		gdbMaker.preInsert();
		int error = 0;
		Xref right = new Xref ("1234", DataSource.ENTREZ_GENE);
		error += gdbMaker.addGene(right, "<b>test</b>");
		error += gdbMaker.addLink(new Xref ("ENS001", DataSource.ENSEMBL), right);
		int geneCount = gdbMaker.getGeneCount();
		gdbMaker.finalize ();
		assertEquals (error, 0);
		assertEquals (geneCount, 1);
	}

	public void testDerby() throws DataException, ClassNotFoundException
	{
		SimpleGdb gdbMaker = SimpleGdbFactory.createInstance("test", new DBConnDerby(), DBConnector.PROP_RECREATE);
		creationTest (gdbMaker);
	}
}