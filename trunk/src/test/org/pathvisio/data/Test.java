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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.pathvisio.data.GdbManager.GdbEventListener;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.preferences.PreferenceManager;

public class Test extends TestCase implements GdbEventListener
{	
	//TODO
	static final String gdbHuman = 
		System.getProperty ("user.home") + File.separator + 
		"PathVisio-Data/gene databases/Hs_Derby_20080102.pgdb";
	static final String gdbRat = 
		System.getProperty ("user.home") + File.separator + 
		"PathVisio-Data/gene databases/Rn_Derby_20080102.pgdb";

	boolean eventReceived = false;

	GexManager gexManager = null;
	
	public void setUp()
	{
		gexManager = GexManager.getCurrent();
	}
	
	public void gdbEvent (GdbEvent e)
	{
		if (e.getType() == GdbEvent.GDB_CONNECTED)
		{
			eventReceived = true;
		}
	}
	
	public void testGdbConnect() throws DataException
	{
		//TODO: create test pgdb
		// suitable for mapping Hs_Apoptosis genes.
		
		assertTrue (new File (gdbHuman).exists()); // if gdb can't be found, rest of test doesn't make sense. 
		SimpleGdb gdb = SimpleGdbFactory.createInstance (gdbHuman, new DataDerby(), 0);
		gdb.close();
		
		// assertTrue (eventReceived);
		// test reception of event...
	}
	
	public void testPatterns()
	{
		assertTrue (DataSourcePatterns.getDataSourceMatches("1.1.1.1").contains(DataSource.ENZYME_CODE));
		assertTrue (DataSourcePatterns.getDataSourceMatches("50-99-7").contains(DataSource.CAS));
		assertTrue (DataSourcePatterns.getDataSourceMatches("HMDB00122").contains(DataSource.HMDB));
		assertTrue (DataSourcePatterns.getDataSourceMatches("C00031").contains(DataSource.KEGG_COMPOUND));
		assertTrue (DataSourcePatterns.getDataSourceMatches("CHEBI:17925").contains(DataSource.CHEBI));
	}
	
	public void testImportSimple() throws IOException, DataException
	{
		PreferenceManager.init();
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_data_1.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex2";
		info.setDbName(dbFileName);
		SimpleGdb gdb = SimpleGdbFactory.createInstance(gdbHuman, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// no errors if all genes could be looked up.
		assertEquals (info.getErrorList().size(), 0);
		
		ImportInformation info2 = new ImportInformation();
		info2.setTxtFile(f);
		dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex3";
		info2.setDbName(dbFileName);
		
		gdb = SimpleGdbFactory.createInstance (gdbRat, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// 91 errors expected if no genes can be looked up.
		assertEquals (info.getErrorList().size(), 91);	
	}

	public void testImportAffy() throws IOException, DataException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_affymetrix.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.guessSettings();
		
		assertEquals (info.getDataSource(), DataSource.AFFY);
		assertFalse (info.getSyscodeColumn());
		assertTrue (info.digitIsDot());
		assertEquals (info.getIdColumn(), 0);
		
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex2";
		info.setDbName(dbFileName);
		info.setSyscodeColumn(false);
		info.setDataSource(DataSource.AFFY);
		SimpleGdb gdb = SimpleGdbFactory.createInstance (gdbRat, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// just 6 errors if all goes well
		assertEquals (info.getErrorList().size(), 6);		
	}

	/**
	 * Column headers have lenghts of over 50.
	 * Make sure this doesn't lead to problems when setting sample names
	 */
	public void testImportLongHeaders() throws IOException, DataException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_data_long_headers.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.guessSettings();
		
		assertEquals (info.getDataSource(), DataSource.ENTREZ_GENE);
		assertFalse (info.getSyscodeColumn());
		assertTrue (info.digitIsDot());
		assertEquals (info.getIdColumn(), 0);
		
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex3";
		info.setDbName(dbFileName);
		
		SimpleGdb gdb = SimpleGdbFactory.createInstance(gdbHuman, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// 0 errors if all goes well
		assertEquals (info.getErrorList().size(), 0);		
	}

	/**
	 * Test dataset contains two columns with textual data
	 * make sure this doesn't give problems during import
	 */
	public void testImportWithText() throws IOException, DataException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_data_with_text.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.guessSettings();
		
		assertEquals (info.getDataSource(), DataSource.ENTREZ_GENE);
		assertTrue (info.getSyscodeColumn());
		assertEquals (info.getCodeColumn(), 1);
		assertTrue (info.digitIsDot());
		assertEquals (info.getIdColumn(), 0);
		
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex4";
		info.setDbName(dbFileName);
		
		SimpleGdb gdb = SimpleGdbFactory.createInstance(gdbHuman, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// 0 errors if all goes well
		assertEquals (info.getErrorList().size(), 0);		
	}

	public void gexHelper(DBConnector con, String filename) throws DataException, SQLException
	{
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + filename;

		// TODO: check if filename gets .pgex or .pgdb?
		SimpleGex sgex = new SimpleGex (dbFileName, true, con);
		
		assertTrue (new File(dbFileName).exists());
		
		sgex.prepare();
		sgex.addSample(55, "mysample", 99);
		sgex.addExpr(new Xref ("abc_at", DataSource.AFFY), "55", "3.141", 77);
		
		// TODO: this is messy. call finalize on writeable db, not close...
		sgex.finalize();
		
		// read data back
		sgex = new SimpleGex (dbFileName, false, con);
		
		Sample s = sgex.getSample(55);
		assertEquals (s.getName(), "mysample");
		assertEquals (s.getDataType(), 99);
		
		//TODO: test data value as well.
		
		sgex.close();
	}
	
	public void testGexDerby() throws DataException, SQLException
	{
		gexHelper (new DataDerby(), "tempgex1a");
	}

	//TODO: re-enable
	public void disabled_testGexDirectory() throws DataException, SQLException
	{
		gexHelper (new DataDerbyDirectory(), "tempgex1b");
	}

}
