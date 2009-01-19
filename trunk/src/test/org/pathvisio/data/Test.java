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
package org.pathvisio.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;

import org.pathvisio.data.GdbManager.GdbEventListener;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.model.XrefWithSymbol;
import org.pathvisio.preferences.PreferenceManager;

/*
 * Coverage of src/core/org/pathvisio/data, as reported by EMMA plugin:
 * 
 *  before    - 41.2%
 *  4 Dec '08 - 46.6%
 *  19 Jan '09- 47.3%
 */
public class Test extends TestCase implements GdbEventListener
{	
	//TODO
	static final String GDB_HUMAN = 
		System.getProperty ("user.home") + File.separator + 
		"PathVisio-Data/gene databases/Hs_Derby_20080102.pgdb";
	static final String GDB_RAT = 
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

	//TODO: create test pgdb
	// suitable for mapping Hs_Apoptosis genes.

	public void testGdbManager()
	{
		// assertTrue (eventReceived);
		// test reception of event...
		//TODO
	}
	
	public void testImportInformation()
	{
		final int base1 = 26;
		final int base2 = 26 * 26 + 26;
		final int base3 = 26 * 26 * 26 + 26 * 26 + 26;
		assertEquals ("A", ImportInformation.colIndexToExcel (0));
		assertEquals ("B", ImportInformation.colIndexToExcel (1));
		assertEquals ("Z", ImportInformation.colIndexToExcel (25));
		assertEquals ("AA", ImportInformation.colIndexToExcel (base1 + 0));
		assertEquals ("BA", ImportInformation.colIndexToExcel (base1 +  1 * 26));
		assertEquals ("ZA", ImportInformation.colIndexToExcel (base1 + 25 * 26));
		assertEquals ("ZZ", ImportInformation.colIndexToExcel (base1 + 25 * 26 + 25));
		assertEquals ("AAA", ImportInformation.colIndexToExcel (base2 + 0));
		assertEquals ("ABA", ImportInformation.colIndexToExcel (base2 + 1 * 26));
		assertEquals ("ZZZ", ImportInformation.colIndexToExcel (base2 + 25 * 26 * 26 + 25 * 26 + 25));
		assertEquals ("AAAA", ImportInformation.colIndexToExcel (base3));
	}
	
	public void testGdbConnect() throws DataException
	{
		assertTrue (new File (GDB_HUMAN).exists()); // if gdb can't be found, rest of test doesn't make sense. 
		SimpleGdb gdb = SimpleGdbFactory.createInstance (GDB_HUMAN, new DataDerby(), 0);
		
		//symbol must be INSR
		Xref ref = new Xref ("3643", DataSource.ENTREZ_GENE);				
		assertEquals ("INSR", gdb.getGeneSymbol(ref));
		
		// test getting backpage
		assertTrue (gdb.getBpInfo(ref).startsWith("<TABLE border = 1><TR><TH>Gene ID:<TH>3643<TR>"));
		
		// get all crossrefs
		List<Xref> crossRefs1 = gdb.getCrossRefs(ref);
		assertTrue(crossRefs1.contains(new Xref("Hs.465744", DataSource.UNIGENE)));
		assertTrue(crossRefs1.contains(new Xref("NM_000208", DataSource.REFSEQ)));
		assertTrue(crossRefs1.contains(new Xref("P06213", DataSource.UNIPROT)));
		assertTrue(crossRefs1.size() > 10);
		
		// get specific crossrefs for specific database
		List<Xref> crossRefs2 = gdb.getCrossRefs(ref, DataSource.AFFY);		
		assertTrue(crossRefs2.contains(new Xref("1572_s_at", DataSource.AFFY)));
		assertTrue(crossRefs2.contains(new Xref("207851_s_at", DataSource.AFFY)));
		assertTrue(crossRefs2.contains(new Xref("213792_s_at", DataSource.AFFY)));
		assertTrue(crossRefs1.size() > crossRefs2.size());

		// get crossrefs by attribute
		List<Xref> crossRefs3 = gdb.getCrossRefsByAttribute("Symbol", "INSR");
		assertTrue(crossRefs3.contains(ref));

		// check symbol suggestions
		List<String> symbols1 = gdb.getSymbolSuggestions("INS", 100);
		assertTrue (symbols1.contains("INSR"));

		// check id suggestions
		List<Xref> crossRefs4 = gdb.getIdSuggestions("207851_s_", 100);
		assertTrue (crossRefs4.contains(new Xref("207851_s_at", DataSource.AFFY)));
		
		// check free search
		List<XrefWithSymbol> result5 = gdb.freeSearch ("Insulin", 100); 
		
		Xref nonExistingRef = new Xref ("bla", DataSource.OTHER); 
		assertNull (gdb.getGeneSymbol(nonExistingRef));
		
		// should return empty list, not NULL
		assertEquals (0, gdb.getCrossRefs(nonExistingRef).size());
		assertEquals (0, gdb.getCrossRefs(nonExistingRef, DataSource.AFFY).size());		
		
		gdb.close();
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
		info.setGexName(dbFileName);
		SimpleGdb gdb = SimpleGdbFactory.createInstance(GDB_HUMAN, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// no errors if all genes could be looked up.
		assertEquals (0, info.getErrorList().size());
		
		ImportInformation info2 = new ImportInformation();
		info2.setTxtFile(f);
		dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex3";
		info2.setGexName(dbFileName);
		
		gdb = SimpleGdbFactory.createInstance (GDB_RAT, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info2, null, gdb, gexManager);
		
		// 91 errors expected if no genes can be looked up.
		assertEquals (91, info2.getErrorList().size());	
	}

	public void testImportAffy() throws IOException, DataException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_affymetrix.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.guessSettings();
		
		assertEquals (info.getDataSource(), DataSource.AFFY);
		assertTrue (info.isSyscodeFixed());
		assertTrue (info.digitIsDot());
		assertEquals (info.getIdColumn(), 0);
		
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex2";
		info.setGexName(dbFileName);
		info.setSyscodeFixed(true);
		info.setDataSource(DataSource.AFFY);
		SimpleGdb gdb = SimpleGdbFactory.createInstance (GDB_RAT, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// just 6 errors if all goes well
		assertEquals (6, info.getErrorList().size());		
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
		assertTrue (info.isSyscodeFixed());
		assertTrue (info.digitIsDot());
		assertEquals (0, info.getIdColumn());
		
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex3";
		info.setGexName(dbFileName);
		
		SimpleGdb gdb = SimpleGdbFactory.createInstance(GDB_HUMAN, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// 0 errors if all goes well
		assertEquals (0, info.getErrorList().size());		
	}

	public void testImportNoHeader() throws IOException, DataException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_data_no_header.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.setFirstDataRow(0);
		info.guessSettings();
		
		assertEquals (info.getDataSource(), DataSource.ENTREZ_GENE);
		assertFalse (info.isSyscodeFixed());
		assertTrue (info.digitIsDot());
		assertEquals (0, info.getIdColumn());
		assertTrue (info.getNoHeader());
		assertEquals ("Column A", info.getColNames()[0]);
		
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex5";
		info.setGexName(dbFileName);
		
		SimpleGdb gdb = SimpleGdbFactory.createInstance(GDB_HUMAN, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// 0 errors if all goes well
		assertEquals (0, info.getErrorList().size());		
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
		assertFalse (info.isSyscodeFixed());
		assertEquals (info.getSyscodeColumn(), 1);
		assertTrue (info.digitIsDot());
		assertEquals (info.getIdColumn(), 0);
		
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex4";
		info.setGexName(dbFileName);
		
		SimpleGdb gdb = SimpleGdbFactory.createInstance(GDB_HUMAN, new DataDerby(), 0);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
		
		// 0 errors if all goes well
		assertEquals (info.getErrorList().size(), 0);		
	}

	public void gexHelper(DBConnector con, String filename) throws DataException, SQLException
	{
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + filename;

		// TODO: check if filename gets .pgex or .pgdb?
		SimpleGex sgex = new SimpleGex (dbFileName, true, con);
				
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
		assertTrue (new File(dbFileName + ".pgex").exists());

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
