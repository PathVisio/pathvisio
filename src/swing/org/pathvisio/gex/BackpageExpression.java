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

import java.util.List;

import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.data.GdbManager;
import org.pathvisio.gui.BackpageTextProvider.BackpageHook;
import org.pathvisio.model.PathwayElement;

/**
 * Backpage hook to show gene expression data in tabular format.
 * Only used in Standalone application.
 */
public class BackpageExpression implements BackpageHook
{
	private final IDMapper mapper;
	private final GexManager gexManager;
	
	public BackpageExpression (GdbManager gdbManager, GexManager gexManager)
	{
		this.mapper = gdbManager.getCurrentGdb();
		this.gexManager = gexManager;
	}

	/**
	 * Gets all available expression data for the given gene id and returns a string
	 * containing this data in a HTML table
	 * @param idc	the {@link Xref} containing the id and code of the geneproduct to look for
	 * @return		String containing the expression data in HTML format or a string displaying a
	 * 'no expression data found' message in HTML format
	 */
	private static String getDataString(Xref idc, IDMapper gdb, CachedData gex) throws IDMapperException
	{
		String noDataFound = "<P><I>No expression data found";
		String exprInfo = "<P><B>Gene id on mapp: " + idc.getId() + "</B><TABLE border='1'>";
		
		String colNames = "<TR><TH>Sample name";
		if(!gex.isConnected()) return noDataFound;
		
		List<ReporterData> pwData = gex.syncGet(idc);
		
		if(pwData == null) return noDataFound;
		
		for(ReporterData d : pwData){
			colNames += "<TH>" + d.getXref().getId();
		}
		
		String dataString = "";
		for(Sample s : gex.getOrderedSamples())
		{
			dataString += "<TR><TH>" + s.getName();
			for(ReporterData d : pwData)
			{
				dataString += "<TH>" + d.getSampleData(s);
			}
		}
		
		return exprInfo + colNames + dataString + "</TABLE>";
	}

	public String getHtml(PathwayElement e) 
	{
		return getHtml(e, mapper, gexManager.getCachedData());
	}
	
	public static String getHtml(PathwayElement e, IDMapper gdb, CachedData gex) {
		String text = "";
		try
		{
			//Get the expression data information if available
			if(gex != null) {
				text += "<H1>Expression data</H1>";
				text += getDataString(e.getXref(), gdb, gex);
			}				
		}
		catch (IDMapperException ex)
		{
			text += "Exception occured while getting cross-references</br>" 
				+ ex.getMessage();
		}
		return text;
	}
}