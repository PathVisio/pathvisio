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
package org.pathvisio.wikipathways.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * A widget that displays information on the search results
 * @author thomas
 */
public class ResultInfobar extends HorizontalPanel {
	public ResultInfobar() {
		setStylePrimaryName(STYLE_INFOBAR);
	}

	/**
	 * Set the search results to display the information for. This will
	 * clear all currently displayed information.
	 * @param results The results
	 * @param query The query that was used to get the results
	 */
	public void setResults(Result[] results, Query query) {
		clear();

		String label = "";

		if(results.length == 0) {
			label = "No results for '<B>" + query.getText() + "</B>'";
		} else {
			label = "The query '<I>" + query.getText() +
					"</I>' returned <B>" + results.length + "</B> result" +
					(results.length > 1 ? "s" : "") + ".";
		}
		HTML numberResults = new HTML(label);
		numberResults.setStyleName(STYLE_INFOBAR_NUMBER);
		add(numberResults);
		setCellHorizontalAlignment(numberResults, ALIGN_RIGHT);
	}

	static final String STYLE_INFOBAR = "search-infobar";
	static final String STYLE_INFOBAR_NUMBER = "search-infobar-number";
}
