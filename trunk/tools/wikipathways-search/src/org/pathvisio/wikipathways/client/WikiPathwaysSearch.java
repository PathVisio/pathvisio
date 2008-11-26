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
package org.pathvisio.wikipathways.client;


import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WikiPathwaysSearch implements EntryPoint, HistoryListener {
	private TextBox searchText;
	private DockPanel mainPanel;
	private Panel progressPanel;
	
	private ResultsTable pathwayResults;
	
	private SearchServiceAsync searchSrv;
	
	public void onModuleLoad() {
		History.addHistoryListener(this);
		
		mainPanel = new DockPanel();

		Panel searchPanel = createSearchPanel();
		Panel progressPanel = createProgressPanel();
		Panel resultsPanel = createResultsPanel();

		mainPanel.add(searchPanel, DockPanel.NORTH);
		mainPanel.add(progressPanel, DockPanel.NORTH);
		mainPanel.add(resultsPanel, DockPanel.CENTER);

		mainPanel.setWidth("100%");
		mainPanel.setCellHorizontalAlignment(searchPanel, DockPanel.ALIGN_CENTER);
		RootPanel.get().add(mainPanel);
		
		//Process url parameters
		String query = Window.Location.getHash();
		if(query != null && !"".equals(query)) {
			if(query.startsWith("#")) query = query.substring(1);
			searchText.setText(query);
			search();
		}
	}

	public void onHistoryChanged(String historyToken) {
		searchText.setText(historyToken);
		if(historyToken == null || "".equals(historyToken)) {
			stopSearch(); //Clear all
		} else {
			search();
		}
	}
	
	private void showError(String title, String message) {
		DialogBox dlg = new DialogBox();
		dlg.setText(message);
		dlg.setTitle(title);
		mainPanel.add(dlg);
	}
	
	private void search() {
		stopSearch();
		doSearch(searchText.getText());
	}

	private void stopSearch() {
		activeSearches.clear();
		pathwayResults.clear();
		refreshProgressDialog();
	}
	
	private Set<AsyncCallback<Result[]>> activeSearches = new HashSet<AsyncCallback<Result[]>>();
	
	private void doSearch(String query) {
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
	
	private Panel createSearchPanel() {
		//Panel to contain search box / button
		HorizontalPanel searchBoxPanel = new HorizontalPanel();
		searchBoxPanel.setStylePrimaryName(STYLE_SEARCHBOX);
		
		searchText = new TextBox();
		searchText.setFocus(true);
		searchText.addKeyboardListener(new KeyboardListenerAdapter() {
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KEY_ENTER) {
					search();
				}
			}
		});
		Button searchButton = new Button(LABEL_SEARCH_BUTTON);
		searchButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				search();
			}
		});

		searchBoxPanel.add(searchText);
		searchBoxPanel.add(searchButton);

		return searchBoxPanel;
	}

	private Panel createProgressPanel() {
		progressPanel = new HorizontalPanel();
		progressPanel.setStylePrimaryName(STYLE_PROGRESS);
		progressPanel.add(new Label("Searching..."));
		progressPanel.setVisible(false);
		return progressPanel;
	}
	
	private Panel createResultsPanel() {
		VerticalPanel resultsPanel = new VerticalPanel();
		resultsPanel.setTitle(TITLE_RESULTS);
		
		pathwayResults = new ResultsTable();
		
		resultsPanel.add(pathwayResults);
		return resultsPanel;
	}

	private static final String TITLE_RESULTS = "Results";
	private static final String LABEL_SEARCH_BUTTON = "Search";
	
	public static final String STYLE_PROGRESS = "search-progress";
	public static final String STYLE_SEARCHBOX = "search-box";
}