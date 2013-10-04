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
package org.pathvisio.desktop.gex;

import java.util.List;

import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.data.DataException;
import org.pathvisio.data.IRow;
import org.pathvisio.data.ISample;
import org.pathvisio.gui.BackpageTextProvider.BackpageHook;
import org.pathvisio.gui.DataPaneTextProvider.DataHook;

/**
 * Shows data uploaded for each DataNode/Interaction present in the pathway
 * in tabular format. Only used in Standalone application.
 *
 * modified by @author anwesha
 */
public class BackpageExpression implements BackpageHook, DataHook {
	private final GexManager gexManager;

	public BackpageExpression(GexManager gexManager) {
		this.gexManager = gexManager;
	}

	/**
	 * Gets all available data for the given identifier and returns a
	 * string containing this data in a HTML table
	 * 
	 * @param idc
	 *            the {@link Xref} containing the id and code of the pathway element
	 *            to look for
	 * @return String containing the data in HTML format or a string
	 *         displaying a 'no data found' message in HTML format
	 */
	private static String getDataString(Xref idc, CachedData gex)
			throws IDMapperException, DataException {
		String noDataFound = "<P><I>No data found.";

		String colNames = "<TR><TH>Identifier";
		if (!gex.isConnected())
			return noDataFound;

		List<? extends IRow> pwData = gex.syncGet(idc);

		if (pwData == null || pwData.size() == 0) {
			return noDataFound;
		}

		for (IRow d : pwData) {
			colNames += "<TH>" + d.getXref().getId();
		}
		
		String dataString = "";
		for (ISample s : gex.getOrderedSamples()) {
			dataString += "<TR><TH>" + s.getName();
			for (IRow d : pwData) {
				dataString += "<TH>" + d.getSampleData(s);
			}
		}
		return "<TABLE border='1'>" + colNames + dataString + "</TABLE>";
	}

	public String getHtml(PathwayElement e) {
		return getHtml(e, gexManager.getCachedData());
	}

	public static String getHtml(PathwayElement e, CachedData gex) {
		String text = "";
		try {
			// Get the data if available
			if (gex != null) {
				text += "<br/><br/><hr/><br/><H1><font color=\"006699\">Expression data</font></H1>";
				text += getDataString(e.getXref(), gex);
			} else {
				text += "<br/><br/><hr/><br/><H1><font color=\"006699\">Expression data</font></H1>";
				text += "<br/>No data imported.</br/>";
			}
		} catch (IDMapperException ex) {
			text += "Exception occured while getting cross-references</br>"
					+ ex.getMessage();
		} catch (DataException ex) {
			text += "Exception occured while getting cross-references</br>"
					+ ex.getMessage();
		}
		return text;
	}

//	@Override
//	public String getHtml(SwingEngine swe) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}