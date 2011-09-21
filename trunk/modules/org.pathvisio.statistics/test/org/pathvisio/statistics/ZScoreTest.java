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
package org.pathvisio.statistics;

import java.io.File;

import junit.framework.TestCase;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.bio.BioDataSource;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.visualization.Criterion;

public class ZScoreTest extends TestCase {
	static final File PATHWAY_DIR = new File("testData/zscore/gpml");
	static final File DATASET_FILE = new File("testData/zscore/data/Myometrium_input.pgex");

	//TODO: replace this with web service
	static final File IDMAPPER_FILE = new File("testData/zscore/data/idmapper.bridge");

	static final String CRITERION = "[24hrs-v14 fold]  > 1.2";
	static final File TEST_PATHWAY = new File(PATHWAY_DIR, "testAlternative.gpml");

	GexManager gexMgr;
	IDMapper idMapper;

	@Override
	protected void setUp() throws Exception {
		PreferenceManager.init();
		BioDataSource.init();

		//Load the expression data
		gexMgr = new GexManager();
		gexMgr.setCurrentGex(DATASET_FILE.getAbsolutePath(), false);

		Class.forName("org.bridgedb.rdb.IDMapperRdb");
		idMapper = BridgeDb.connect("idmapper-pgdb:" + IDMAPPER_FILE.getAbsolutePath());
	}

	public void testAlternative() {
		try {
			Criterion crit = new Criterion();
			crit.setExpression(CRITERION, gexMgr.getCurrentGex().getSampleNames());
			ZScoreCalculator calc = new ZScoreCalculator(crit, PATHWAY_DIR, gexMgr.getCachedData(), idMapper, null);
			StatisticsResult result = calc.calculateAlternative();

			assertEquals("bigN doesn't equal number rows in data", gexMgr.getCurrentGex().getNrRow(), result.getBigN());

			//Check the pathway with known results
			StatisticsPathwayResult pathRes = null;
			for(StatisticsPathwayResult r : result.getPathwayResults()) {
				if(r.getFile().equals(TEST_PATHWAY)) {
					pathRes = r;
					break;
				}
			}
			result.getPathwayResults();
			int n = Integer.parseInt(pathRes.getProperty(Column.N));
			int nE = 9; //8 probes measured
			int r = Integer.parseInt(pathRes.getProperty(Column.R));
			int rE = 2; //2 measured probes significant
			assertEquals("expected n = " + nE + ", got n = " + n, nE, n);
			assertEquals("expected r = " + rE + ", got r = " + r, rE, r);

		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testMAPPFinder() {
		try {
			Criterion crit = new Criterion();
			crit.setExpression(CRITERION, gexMgr.getCurrentGex().getSampleNames());
			ZScoreCalculator calc = new ZScoreCalculator(crit, PATHWAY_DIR, gexMgr.getCachedData(), idMapper, null);
			StatisticsResult result = calc.calculateMappFinder();

			//Check the pathway with known results
			StatisticsPathwayResult pathRes = null;
			for(StatisticsPathwayResult r : result.getPathwayResults()) {
				if(r.getFile().equals(TEST_PATHWAY)) {
					pathRes = r;
					break;
				}
			}
			result.getPathwayResults();
			int n = Integer.parseInt(pathRes.getProperty(Column.N));
			int nE = 5; //5 genes measured
			int r = Integer.parseInt(pathRes.getProperty(Column.R));
			int rE = 2; //2 measured genes significant
			assertEquals("expected n = " + nE + ", got n = " + n, nE, n);
			assertEquals("expected r = " + rE + ", got r = " + r, rE, r);

		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
