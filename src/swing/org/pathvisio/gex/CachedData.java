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
package org.pathvisio.gex;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bridgedb.Xref;


/**
 * This class represents cached expression data for a pathway.
 * The caching of expression data will occur when a pathway is opened and an expression dataset is loaded.
 * The cache will be refreshed when another dataset is selected, another gene database is selected or another
 * pathway is opened.
 * A CachedData object will contain a list of {@link ReporterData} object for every gene-product on the pathway for 
 * which data is available in the expression dataset
 */
public class CachedData {
	//Data objects for gene-products on the pathway
	Map<Xref, List<ReporterData>> data = new HashMap<Xref, List<ReporterData>>(); 
	
	/**
	 * Check whether the cached data contains data for the given gene-product
	 * @param pwId The IdCodePair that represents the gene-product
	 * @return true if data is available for the gene-product, false if not
	 */
	public boolean hasData(Xref pwId) {
		return data.containsKey(pwId);
	}
	
	/**
	 * Get the cached data the given gene-product
	 * @param idc The IdCodePair that represents the gene-product for which the data has to be returned
	 * @return a list of {@link ReporterData} object containing the cached data, or null when no data is available
	 */
	public List<ReporterData> getData(Xref idc) {
		return data.get(idc);
	}
	
	/**
	 * Add cached data for the given gene-product
	 * @param idc The IdCodePair that represents the gene-product for which the data has to be added
	 * @param d The data that has to be added
	 */
	protected void addData(Xref idc, ReporterData d) {
		List<ReporterData> dlist = data.get(idc);
		if(dlist == null) 
			data.put(idc, dlist = new ArrayList<ReporterData>());
		dlist.add(d);
	}
}
