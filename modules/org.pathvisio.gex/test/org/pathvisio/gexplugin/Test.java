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
package org.pathvisio.gexplugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.rdb.construct.DBConnector;
import org.bridgedb.rdb.construct.DataDerby;
import org.bridgedb.rdb.construct.DataDerbyDirectory;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.desktop.gex.CachedData;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.ReporterData;
import org.pathvisio.desktop.gex.Sample;
import org.pathvisio.desktop.gex.SimpleGex;

public class Test extends TestCase
{
	//TODO
	static final String GDB_HUMAN =
		System.getProperty ("user.home") + File.separator +
		"PathVisio-Data/gene databases/Hs_Derby_20080102.pgdb";
	static final String GDB_RAT =
		System.getProperty ("user.home") + File.separator +
		"PathVisio-Data/gene databases/Rn_Derby_20080102.pgdb";

	GexManager gexManager = null;

	public void setUp() throws ClassNotFoundException
	{
		gexManager = new GexManager();
		Class.forName ("org.bridgedb.rdb.IDMapperRdb");
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

	public void testImportSimple() throws IOException, IDMapperException
	{
		PreferenceManager.init();
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_data_1.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex2";
		info.setGexName(dbFileName);
		IDMapper gdb = BridgeDb.connect("idmapper-pgdb:" + GDB_HUMAN);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);

		// no errors if all genes could be looked up.
		assertEquals (0, info.getErrorList().size());

		// Now test caching data
		SimpleGex gex = gexManager.getCurrentGex();
		Xref ref1 = new Xref("7124", BioDataSource.ENTREZ_GENE);
		Xref ref2 = new Xref("1909_at", BioDataSource.AFFY);
		List<Xref> refs = Arrays.asList(new Xref[] { ref1, ref2 });
		CachedData cache = new CachedData(gex);
		cache.setMapper(gdb);
		cache.preSeed(refs);

		Sample s = gex.getSample(1);
		assertEquals (1, s.getId());

		assertEquals ("Control 2", s.getName());

		//check that there is only one row of data
		List<ReporterData> data1 = cache.getData(ref1);
		assertEquals (1, data1.size());

		// looking up a particular data point in two different ways: L:7124, sample "Control 2"
		assertEquals (0.993159836, (Double)data1.get(0).getSampleData(s), 0.001);
		assertEquals (0.993159836, (Double)data1.get(0).getByName().get("Control 2"), 0.001);

		// test for aggregating data (in this case we're averaging over just one row)
		ReporterData row = ReporterData.createListSummary(cache.getData(ref2));
		// check data point for X:1909_at, which corresponds to L:596
		assertEquals (0.045334852, (Double)(row.getSampleData().get(s)), 0.001);

	}


	public void testImportSimplyWrong() throws IOException, IDMapperException
	{
		ImportInformation info2 = new ImportInformation();
		File f = new File ("example-data/sample_data_1.txt");
		assertTrue (f.exists());
		info2.setTxtFile(f);

		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex3";
		info2.setGexName(dbFileName);

		IDMapper gdb = BridgeDb.connect("idmapper-pgdb:" + GDB_HUMAN);
		GexTxtImporter.importFromTxt(info2, null, gdb, gexManager);

		// 91 errors expected if no genes can be looked up.
		assertEquals (91, info2.getErrorList().size());
	}

	public void testImportAffy() throws IOException, IDMapperException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_affymetrix.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.guessSettings();

		assertEquals (info.getDataSource(), BioDataSource.AFFY);
		assertTrue (info.isSyscodeFixed());
		assertTrue (info.digitIsDot());
		assertEquals (info.getIdColumn(), 0);

		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex2";
		info.setGexName(dbFileName);
		info.setSyscodeFixed(true);
		info.setDataSource(BioDataSource.AFFY);
		IDMapper gdb = BridgeDb.connect("idmapper-pgdb:" + GDB_RAT);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);

		// just 6 errors if all goes well
		assertEquals (6, info.getErrorList().size());
	}

	/**
	 * Column headers have lenghts of over 50.
	 * Make sure this doesn't lead to problems when setting sample names
	 */
	public void testImportLongHeaders() throws IOException, IDMapperException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_data_long_headers.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.guessSettings();

		assertEquals (info.getDataSource(), BioDataSource.ENTREZ_GENE);
		assertTrue (info.isSyscodeFixed());
		assertTrue (info.digitIsDot());
		assertEquals (0, info.getIdColumn());

		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex3";
		info.setGexName(dbFileName);

		IDMapper gdb = BridgeDb.connect("idmapper-pgdb:" + GDB_HUMAN);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);

		// 0 errors if all goes well
		assertEquals (0, info.getErrorList().size());
	}

	public void testImportNoHeader() throws IOException, IDMapperException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_data_no_header.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.setFirstDataRow(0);
		info.guessSettings();

		assertEquals (info.getDataSource(), BioDataSource.ENTREZ_GENE);
		assertFalse (info.isSyscodeFixed());
		assertTrue (info.digitIsDot());
		assertEquals (0, info.getIdColumn());
		assertTrue (info.getNoHeader());
		assertEquals ("Column A", info.getColNames()[0]);

		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex5";
		info.setGexName(dbFileName);

		IDMapper gdb = BridgeDb.connect("idmapper-pgdb:" + GDB_HUMAN);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);

		// 0 errors if all goes well
		assertEquals (0, info.getErrorList().size());
	}

	/**
	 * Test dataset contains two columns with textual data
	 * make sure this doesn't give problems during import
	 */
	public void testImportWithText() throws IOException, IDMapperException
	{
		ImportInformation info = new ImportInformation();
		File f = new File ("example-data/sample_data_with_text.txt");
		assertTrue (f.exists());
		info.setTxtFile(f);
		info.guessSettings();

		assertEquals (info.getDataSource(), BioDataSource.ENTREZ_GENE);
		assertFalse (info.isSyscodeFixed());
		assertEquals (info.getSyscodeColumn(), 1);
		assertTrue (info.digitIsDot());
		assertEquals (info.getIdColumn(), 0);

		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + "tempgex4";
		info.setGexName(dbFileName);

		IDMapper gdb = BridgeDb.connect("idmapper-pgdb:" + GDB_HUMAN);
		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);

		// 0 errors if all goes well
		assertEquals (info.getErrorList().size(), 0);
	}

	public void gexHelper(DBConnector con, String filename) throws IDMapperException, SQLException
	{
		String dbFileName = System.getProperty("java.io.tmpdir") + File.separator + filename;

		// TODO: check if filename gets .pgex or .pgdb?
		SimpleGex sgex = new SimpleGex (dbFileName, true, con);

		sgex.prepare();
		sgex.addSample(55, "mysample", 99);
		sgex.addExpr(new Xref ("abc_at", BioDataSource.AFFY), "55", "3.141", 77);

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

	public void testGexDerby() throws IDMapperException, SQLException
	{
		gexHelper (new DataDerby(), "tempgex1a");
	}

	//TODO: re-enable
	public void disabled_testGexDirectory() throws IDMapperException, SQLException
	{
		gexHelper (new DataDerbyDirectory(), "tempgex1b");
	}
}
