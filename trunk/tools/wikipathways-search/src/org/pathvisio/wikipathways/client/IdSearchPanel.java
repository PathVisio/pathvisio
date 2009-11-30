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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A search panel for searching based on gene/protein/small molecule identifier.
 * This panel displays a list of available databases in addition to the search text box.
 * @author thomas
 */
public class IdSearchPanel extends SearchPanel {
	ListBox systemList;
	List<String> systemNames = new ArrayList<String>();

	public IdSearchPanel(WikiPathwaysSearch search) {
		super(search);
	}

	protected Widget createOptionsPanel() {
		systemList = new ListBox();
		systemList.setStylePrimaryName(STYLE_SYSTEMS);

		SearchServiceAsync searchSrv = GWT.create(SearchService.class);

		AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) {
				System.err.println("Unable to get available database systems");
			}
			public void onSuccess(String[] result) {
				systemList.clear();
				systemNames.clear();

				//Add value to search all systems
				systemList.addItem(SYSTEM_ALL);
				systemNames.add(SYSTEM_ALL);

				for(String r : result) {
					systemList.addItem(r);
					systemNames.add(r);
				}
			}
		};
		searchSrv.getSystemNames(callback);

		return systemList;
	}

	protected Widget createInfoPanel() {
		return new HTML(
				"Search by database identifier. <b>Example:</b> <i>201746_at</i>"
		);
	}

	protected Query getQuery() {
		Query q = super.getQuery();
		q.setField(Query.FIELD_TYPE, Query.TYPE_ID); //Make sure the query type is set correctly
		q.setField(Query.FIELD_SYSTEM, systemList.getValue(systemList.getSelectedIndex()));
		return q;
	}

	protected void setQuery(Query query) {
		super.setQuery(query);
		int i = systemNames.indexOf(query.getField(Query.FIELD_SYSTEM));
		systemList.setSelectedIndex(i);
	}

	public static final String SYSTEM_ALL = "All databases";
	private static final String STYLE_SYSTEMS = "searchpanel-systems";
}
