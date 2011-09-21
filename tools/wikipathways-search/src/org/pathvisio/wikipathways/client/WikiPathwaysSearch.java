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
package org.pathvisio.wikipathways.client;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The entry point for the WikiPathways search page. Creates and displays
 * the search widgets.
 * @author thomas
 */
public class WikiPathwaysSearch implements EntryPoint, HistoryListener {
	private DockPanel mainPanel; //The base panel
	private Panel progressPanel; //The panel that will be shown during searching
	private ResultInfobar infoPanel; //Panel that displays search result info
	private TabPanel tabPanel; //The tab panel container for the search panel widgets

	private ResultsTable pathwayResults; //The table that displays the search results

	private SearchServiceAsync searchSrv; //The RPC search service

	public void onModuleLoad() {
		History.addHistoryListener(this);

		mainPanel = new DockPanel();

		Widget searchPanel = createSearchPanel();
		Panel progressPanel = createProgressPanel();
		infoPanel = new ResultInfobar();
		Panel resultsPanel = createResultsPanel();

		mainPanel.add(searchPanel, DockPanel.NORTH);
		mainPanel.add(progressPanel, DockPanel.NORTH);
		mainPanel.add(infoPanel, DockPanel.NORTH);
		mainPanel.add(resultsPanel, DockPanel.CENTER);

		mainPanel.setWidth("100%");
		mainPanel.setCellHorizontalAlignment(searchPanel, DockPanel.ALIGN_CENTER);
		RootPanel.get().add(mainPanel);

		//Process url parameters
		String text = Window.Location.getHash();
		if(text != null && !"".equals(text)) {
			if(text.startsWith("#")) text = text.substring(1);
			Query query = Query.fromString(text);
			showQueryPanel(query);
			search(query);
		}
	}

	public void onHistoryChanged(String historyToken) {
		Query query = Query.fromString(historyToken);
		if(historyToken == null || "".equals(historyToken)) {
			stopSearch(); //Clear all
		} else {
			showQueryPanel(query);
			search(query);
		}
	}

	/**
	 * Show an error.
	 * @param title The error title
	 * @param message The detailed error message
	 */
	private void showError(String title, String message) {
		Window.alert(title + "\n" + message);
	}

	/**
	 * Clear the current results and start the search.
	 * @param query The search query
	 */
	protected void search(Query query) {
		stopSearch();
		doSearch(query);
	}

	/**
	 * Stop the current search and clear the results
	 */
	private void stopSearch() {
		infoPanel.clear();
		activeSearches.clear();
		pathwayResults.clear();
		refreshProgressDialog();
	}

	private Set<AsyncCallback<Result[]>> activeSearches = new HashSet<AsyncCallback<Result[]>>();

	private void doSearch(final Query query) {
		History.newItem(query.toString(), false);
		if(searchSrv == null) {
			searchSrv = GWT.create(SearchService.class);
		}

		AsyncCallback<Result[]> callback = new AsyncCallback<Result[]>() {
			public void onFailure(Throwable caught) {
				removePendingSearch(this);
				showError("Unable to perform search", caught.getMessage());
			}
			public void onSuccess(Result[] results) {
				//Only add results if user didn't cancel search
				if(activeSearches.contains(this)) {
					infoPanel.setResults(results, query);
					pathwayResults.addResults(results);
				}
				removePendingSearch(this);
			}
		};
		addPendingSearch(callback);
		searchSrv.search(query, callback);
	}

	private void removePendingSearch(AsyncCallback<Result[]> callback) {
		activeSearches.remove(callback);
		refreshProgressDialog();
	}

	private void addPendingSearch(AsyncCallback<Result[]> callback) {
		activeSearches.add(callback);
		refreshProgressDialog();
	}

	private void refreshProgressDialog() {
		progressPanel.setVisible(
			activeSearches.size() > 0
		);
	}

	HashMap<String, SearchPanel> type2panel = new HashMap<String, SearchPanel>();

	private void showQueryPanel(Query query) {
		SearchPanel sp = type2panel.get(query.getType());
		if(sp != null) {
			sp.setQuery(query);
			tabPanel.selectTab(tabPanel.getWidgetIndex(sp));
		} else {
			tabPanel.selectTab(0);
		}
	}


	/**
	 * Create the search panels.
	 */
	private Widget createSearchPanel() {
		//Deck panel for all searches
		tabPanel = new TabPanel();
		tabPanel.setStylePrimaryName(STYLE_TABPANEL);

		//Text search
		SearchPanel textPanel = new SearchPanel(this);
		type2panel.put(Query.TYPE_TEXT, textPanel);
		tabPanel.add(textPanel, "Text search");
		textPanel.setSize("auto", "auto");

		//Id search
		SearchPanel idPanel = new IdSearchPanel(this);
		type2panel.put(Query.TYPE_ID, idPanel);
		tabPanel.add(idPanel, "Identifier search");
		idPanel.setSize("auto", "auto");

		mainPanel.add(tabPanel, DockPanel.CENTER);
		tabPanel.selectTab(0);
		return tabPanel;
	}

	/**
	 * Create the panel that can be used to show search progress.
	 */
	private Panel createProgressPanel() {
		progressPanel = new HorizontalPanel();
		progressPanel.setStylePrimaryName(STYLE_PROGRESS);
		progressPanel.add(new Label("Searching..."));
		progressPanel.setVisible(false);
		return progressPanel;
	}

	/**
	 * Create the panel that will be used to display the results
	 */
	private Panel createResultsPanel() {
		pathwayResults = new ResultsTable();
		pathwayResults.setStylePrimaryName(STYLE_RESULT);
		pathwayResults.setTitle(TITLE_RESULTS);
		return pathwayResults;
	}

	private static final String TITLE_RESULTS = "Results";

	public static final String STYLE_PROGRESS = "search-progress";
	public static final String STYLE_TABPANEL = "search-tabpanel";
	public static final String STYLE_RESULT = "search-results";
}