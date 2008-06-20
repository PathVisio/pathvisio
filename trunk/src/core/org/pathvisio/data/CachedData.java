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

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pathvisio.model.Xref;


/**
 * This class represents cached expression data for a pathway.
 * The caching of expression data will occur when a pathway is opened and an expression dataset is loaded.
 * The cache will be refreshed when another dataset is selected, another gene database is selected or another
 * pathway is opened.
 * A CachedData object will contain a list of {@link Data} object for every gene-product on the pathway for 
 * which data is available in the expression dataset
 * @author Thomas
 * @see SimpleGex#cacheData
 */
public class CachedData {
	
	HashMap<Xref, List<Data>> data; //Data objects for gene-products on the pathway
		
	protected CachedData() {
		data = new HashMap<Xref, List<Data>>();
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
		List<Data> d = data.get(pwId);
		if(d != null) {
			return d.size() > 1;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the cached data the given gene-product
	 * @param idc The IdCodePair that represents the gene-product for which the data has to be returned
	 * @return a list of {@link Data} object containing the cached data, or null when no data is available
	 */
	public List<Data> getData(Xref idc) {
		return data.get(idc);
	}
	
	/**
	 * Get the first {@link Data} instance of the cached data for this gene-product.
	 * @param idc The IdCodePair that represents the gene-product for which the data has to be returned
	 * @return a {@link Data} instance that contains the cached data
	 */
	public Data getSingleData(Xref idc) {
		List<Data> dlist = data.get(idc);
		if(dlist != null && dlist.size() > 0) return dlist.get(0);
		return null;
	}
	
	/**
	 * Add cached data for the given gene-product
	 * @param idc The IdCodePair that represents the gene-product for which the data has to be added
	 * @param d The data that has to be added
	 */
	protected void addData(Xref idc, Data d) {
		List<Data> dlist = data.get(idc);
		if(dlist == null) 
			data.put(idc, dlist = new ArrayList<Data>());
		dlist.add(d);
	}
	
	/**
	 * Get the averaged sample data for the given gene-product
	 * @param idc The IdCodePair that represents the gene-product to get the data for
	 * @return a HashMap where the keys represent the sample ids and the values the averaged data
	 * @see Data#getSampleData()
	 */
	public HashMap<Integer, Object> getAverageSampleData(Xref idc)
	{
		HashMap<Integer, Object> averageData = new HashMap<Integer, Object>();
		List<Data> dlist = data.get(idc);
		if(dlist != null) {
			HashMap<Integer, Sample> samples = GexManager.getCurrent().getCurrentGex().getSamples();
			for(int idSample : samples.keySet())
			{
				int dataType = samples.get(idSample).getDataType();
				if(dataType == Types.REAL) {
					averageData.put(idSample, averageDouble(dlist, idSample));
				} else {
					averageData.put(idSample, averageString(dlist, idSample));
				}
			}
		}
		return averageData;
	}
	
	
	private Object averageDouble(List<Data> dlist, int idSample)
	{
		double avg = 0;
		int n = 0;
		for(Data d : dlist) {
			try { 
				Double value = (Double)d.getSampleData(idSample);
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
	
	private Object averageString(List<Data> dlist, int idSample)
	{
		StringBuilder sb = new StringBuilder();
		for(Data d : dlist) {
			sb.append(d.getSampleData(idSample) + ", ");
		}
		int end = sb.lastIndexOf(", ");
		return end < 0 ? "" : sb.substring(0, end).toString();
	}
	
	/**
	 * This class represents cached expression data for a reporter in the dataset.
	 * The data is stored in a {@link HashMap} where the keys are the sample ids and the value
	 * is an object of class {@link String} or {@link Double} for text and numeric data respectively.
	 * @author Thomas
	 */
	public static class Data {
		Xref idc;
		int group;
		HashMap<Integer, Object> sampleData;
		
		/**
		 * Constructor for this class. Creates a new {@link Data} object for the given reporter
		 * @param ref The IdCodePair that represents the reporter
		 * @param groupId An id that groups the expression data from duplicate reporters
		 */
		protected Data(Xref ref, int groupId) {
			idc = ref;
			group = groupId;
			sampleData = new HashMap<Integer, Object>();
		}
		
		public void setXref(Xref value) { idc = value; }
		
		/**
		 * Get the reporter this object contains data for
		 * @return The IdCodePair that represents the reporter this object contains data for
		 */
		public Xref getXref() { return idc; }
		
		/**
		 * Get the group id for this object
		 * @return a group id that can be used to distinct identical reporters that occur more
		 * than once in the dataset
		 */
		public int getGroup() { return group; }
		
		/**
		 * Get the data for each sample
		 * @return A {@link HashMap} that contains the data for each sample. The key is a sampleId and value
		 * is an object of class {@link String} or {@link Double}, depending on the data type of the sample.
		 * @see Sample#getDataType()
		 * @see Sample#getId()
		 */
		public HashMap<Integer, Object> getSampleData() {
			return sampleData;
		}
		
		/**
		 * Get the cached data for the given sample (shortcut for getSampleData().get(sampleId))
		 * @param sampleId The id of the sample to get the data for
		 * @return An object of class {@link String} or {@link Double}, depending on the datatype of the sample.
		 * @see Sample#getDataType()
		 * @see Sample#getId()
		 */
		public Object getSampleData(int sampleId) {
			return sampleData.get(sampleId);
		}
		
		/**
		 * Set the data for the given sample. Data will be parsed to double if possible
		 * @param sampleId The id of the sample to set the data for
		 * @param data The {@link String} representation of the data to add
		 * @see SimpleGex#cacheData
		 */
		protected void setSampleData(int sampleId, String data) {
			Object parsedData = null;
			try { parsedData = Double.parseDouble(data); }
			catch(Exception e) { parsedData = data; }
			sampleData.put(sampleId, parsedData);
		}
		
	}
}
