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

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for the search panel. This panel displays a single text box and search button.
 * Subclasses can implement {@link #createOptionsPanel()} and {@link #createInfoPanel()} to
 * add custom widgets.
 * @author thomas
 */
public class SearchPanel extends DockPanel {
	private TextBox searchText;
	private WikiPathwaysSearch search;

	public SearchPanel(final WikiPathwaysSearch search) {
		this.search = search;
		setStylePrimaryName(STYLE_SEARCH_PANEL);

		//Add info/example panel
		Widget info = createInfoPanel();
		info.setStylePrimaryName(STYLE_SEARCH_INFO);
		add(info, NORTH);

		//Search panel
		Panel searchPanel = new HorizontalPanel();
		searchPanel.setStylePrimaryName(STYLE_SEARCH_CONTAINER);

		//Add search text
		searchText = createSearchText();
		searchText.setStylePrimaryName(STYLE_SEARCH_TEXT);
		searchPanel.add(searchText);

		//Add options panel
		Widget options = createOptionsPanel();
		if(options != null) {
			options.setStylePrimaryName(STYLE_SEARCH_OPTIONS);
			searchPanel.add(options);
		}

		//Add search button
		Button searchButton = new Button(LABEL_SEARCH_BUTTON);
		searchButton.setStylePrimaryName(STYLE_SEARCH_BUTTON);
		searchButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				search.search(getQuery());
			}
		});
		searchPanel.add(searchButton);

		add(searchPanel, CENTER);
	}

	/**
	 * Create a panel that will be displayed above the
	 * search box and provides info/examples.
	 */
	protected Widget createInfoPanel() {
		return new HTML(
				"Search by keyword. <b>Example:</b> <i>apoptosis</i>"
		);
	}

	/**
	 * Override this method to create an optional
	 * panel between the search text and button.
	 * Default implementation returns null.
	 */
	protected Widget createOptionsPanel() {
		return null;
	}

	/**
	 * Creates the search text box, including the necessary
	 * keyboard listeners.
	 */
	protected TextBox createSearchText() {
		final TextBox searchText = new TextBox();
		searchText.setFocus(true);
		searchText.addKeyboardListener(new KeyboardListenerAdapter() {
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KEY_ENTER) {
					search.search(getQuery());
				}
			}
		});
		return searchText;
	}

	/**
	 * Get the query based on the current widget states. Default
	 * implementation adds the text in the search box to the
	 * query.
	 * Subclasses may override this method to add additional information
	 * to the query or change the query type.
	 */
	protected Query getQuery() {
		return new Query(Query.TYPE_TEXT, searchText.getText());
	}

	/**
	 * Set the widget states to reflect the given query.
	 * Subclasses may override this method to set the state of
	 * custom widgets
	 */
	protected void setQuery(Query query) {
		searchText.setText(query.getField(Query.FIELD_TEXT));
	}

	protected TextBox getSearchText() {
		return searchText;
	}

	protected WikiPathwaysSearch getSearchEngine() {
		return search;
	}

	private static final String LABEL_SEARCH_BUTTON = "Search";

	private static final String STYLE_SEARCH_CONTAINER = "searchpanel-container";
	private static final String STYLE_SEARCH_TEXT = "searchpanel-text";
	private static final String STYLE_SEARCH_OPTIONS = "searchpanel-options";
	private static final String STYLE_SEARCH_BUTTON = "searchpanel-button";
	private static final String STYLE_SEARCH_PANEL = "searchpanel";
	private static final String STYLE_SEARCH_INFO = "searchpanel-info";
}
