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
	
	Map<Xref, List<ReporterData>> data; //Data objects for gene-products on the pathway
		
	protected CachedData() {
		data = new HashMap<Xref, List<ReporterData>>();
	}
	
	/**
	 * Check whether the cached data contains data for the given gene-product
	 * @param pwId The IdCodePair that represents the gene-product
	 * @return true if data is available for the gene-product, false if not
	 */
	public boolean hasData(Xref pwId) {
		return data.containsKey(pwId);
	}
	
	/**
	 * Check whether the cached data contains multiple data instances for the given gene-product.
	 * This can occur when multiple reporters in the dataset correspond to the same gene-product.
	 * @param pwId The IdCodePair that represents the gene-product
	 * @return true if multiple data is available for the gene-product, false if not
	 */
	public boolean hasMultipleData(Xref pwId) {
		List<ReporterData> d = data.get(pwId);
		if(d != null) {
			return d.size() > 1;
		} else {
			return false;
		}
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
	 * Get the first {@link ReporterData} instance of the cached data for this gene-product.
	 * @param idc The IdCodePair that represents the gene-product for which the data has to be returned
	 * @return a {@link ReporterData} instance that contains the cached data
	 */
	public ReporterData getSingleData(Xref idc) {
		List<ReporterData> dlist = data.get(idc);
		if(dlist != null && dlist.size() > 0) return dlist.get(0);
		return null;
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
	
	/**
	 * Generates a summary-ReporterData by summarizing a List of ReporterData.
	 * Strings are concatenated, doubles are averaged.
	 * @param dlist list to summarize.
	 * @return summary ReporterData
	 */
	public static ReporterData getAverageSampleData(List<ReporterData> dlist)
	{
		ReporterData result = new ReporterData(null, -1);
		if(dlist != null && dlist.size() > 0) {
			for(Sample key : dlist.get(0).getSampleData().keySet())
			{
				int dataType = key.getDataType();
				if(dataType == Types.REAL) {
					result.setSampleAsObject(key, averageDouble(dlist, key));
				} else {
					result.setSampleAsObject(key, averageString(dlist, key));
				}
			}
		}
		return result;
	}
	
	
	private static Object averageDouble(List<ReporterData> dlist, Sample s)
	{
		double avg = 0;
		int n = 0;
		for(ReporterData d : dlist) {
			try { 
				Double value = (Double)d.getSampleData(s);
				if( !value.isNaN() ) {
					avg += value;
					n++;
				}
			} catch(Exception e) { }
		}
		if(n > 0) {
			return avg / n;
		} else {
			return Double.NaN;
		}
	}
	
	private static Object averageString(List<ReporterData> dlist, Sample s)
	{
		StringBuilder sb = new StringBuilder();
		for(ReporterData d : dlist) {
			sb.append(d.getSampleData(s) + ", ");
		}
		int end = sb.lastIndexOf(", ");
		return end < 0 ? "" : sb.substring(0, end).toString();
	}
}
