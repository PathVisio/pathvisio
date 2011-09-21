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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that displays search results
 * @author thomas
 */
public class ResultsTable extends DockPanel {
	int row; //The current row

	FlexTable table;
	HorizontalPanel filterPanel;

	List<String> organisms = new ArrayList<String>();
	ListBox orgList;

	HashMap<Result, Integer> resultRows = new HashMap<Result, Integer>();

	/**
	 * Create a results table. Will automatically populate the organisms
	 * list for the filter. Call {@link #addResults(Result[])} to display
	 *  the results.
	 */
	public ResultsTable() {
		table = new FlexTable();
		add(table, CENTER);

		//Create a panel that displays controls to filter the results
		filterPanel = new HorizontalPanel();
		orgList = new ListBox();
		orgList.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				setOrganismFilter(
						orgList.getItemText(
								orgList.getSelectedIndex()
				));
			}
		});

		//Populate organism list
		SearchServiceAsync searchSrv = GWT.create(SearchService.class);

		AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) {
				System.err.println("Unable to get available organisms");
			}
			public void onSuccess(String[] result) {
				orgList.clear();
				organisms.clear();
				//Add an entry for all organisms
				orgList.addItem(ALL_ORGANISMS);
				organisms.add(ALL_ORGANISMS);

				for(String org : result) {
					orgList.addItem(org);
					organisms.add(org);
				}
			}
		};
		searchSrv.getOrganismNames(callback);

		filterPanel.add(new Label("Results for organism: "));
		filterPanel.add(orgList);
		filterPanel.setVisible(false);
		filterPanel.setStylePrimaryName(STYLE_FILTER);
		filterPanel.setVerticalAlignment(ALIGN_MIDDLE);
		add(filterPanel, NORTH);
		setCellHorizontalAlignment(filterPanel, ALIGN_LEFT);
	}

	/**
	 * Set the current organism to filter on. All results from other
	 * organisms will be hidden. Use {@link #ALL_ORGANISMS} to display
	 * all organisms.
	 */
	public void setOrganismFilter(String organism) {
		orgList.setSelectedIndex(organisms.indexOf(organism));
		for(Result r : resultRows.keySet()) {
			int row = resultRows.get(r);
			boolean visible = ALL_ORGANISMS.equals(organism) ||
								r.getOrganism().equals(organism);
			table.getRowFormatter().setVisible(row, visible);
		}
	}

	/**
	 * Add the given results to the table
	 */
	public void addResults(Result[] results) {
		for(Result r : results) {
			addLabel(r);
			addImage(r);
			resultRows.put(r, row);
			row++;
		}
		if(row > 0) {
			filterPanel.setVisible(true);
			setOrganismFilter(
				orgList.getItemText(orgList.getSelectedIndex())
			);
		}
	}

	/**
	 * Clear the contents of the table
	 */
	public void clear() {
		filterPanel.setVisible(false);
		resultRows.clear();
		table.clear();
		row = 0;
	}

	/**
	 * Add the label for the given result
	 */
	private void addLabel(Result result) {
		Panel labelPanel = new VerticalPanel();
		labelPanel.setStylePrimaryName(STYLE_LABEL);

		HTML title = new HTML(
			"<A href='" + result.getUrl() + "'>" +
			result.getTitle() + "</A>"
		);
		title.setStylePrimaryName(STYLE_TITLE);
		labelPanel.add(title);
		HTML descr = new HTML(result.getDescription());
		descr.setStylePrimaryName(STYLE_DESCRIPTION);
		labelPanel.add(descr);

		table.setWidget(row, 1, labelPanel);
	}

	/**
	 * Add the preview image for the given result
	 */
	private void addImage(Result result) {
		Image image = new Image(IMG_LOADER);
		image.setStylePrimaryName(STYLE_IMAGE);
		image.setTitle("Please wait...loading image");

		ImageLink imageLink = new ImageLink(image, result.getUrl());
		imageLink.setStylePrimaryName(STYLE_IMG_CONTAINER);
		table.setWidget(row, 0, imageLink);
		table.getCellFormatter().setAlignment(
				row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		loadImage(result, image);
	}

	/**
	 * Load the image for the result. Will wait for the image
	 * to be generated on the server and set the url of the image widget.
	 */
	void loadImage(final Result result, final Image image) {
		SearchServiceAsync srv = GWT.create(SearchService.class);
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				image.setUrl(IMG_ERROR);
				image.setTitle(caught.getMessage());
			}
			public void onSuccess(Void v) {
				String url = "./getImage?" + GET_ID +
				"=" + result.getImageId();
				image.setUrl(url);
				image.setTitle(url);
			}
		};
		srv.waitForImage(result.getImageId(), callback);
	}

	private static final String ALL_ORGANISMS = "All";

	public static final String STYLE_FILTER = "result-filter";
	public static final String STYLE_DESCRIPTION = "result-description";
	public static final String STYLE_TITLE = "result-title";
	public static final String STYLE_LABEL = "result-label";
	public static final String STYLE_IMAGE = "result-image";
	public static final String STYLE_IMG_CONTAINER = "result-image-container";

	public static final String GET_ID = "id";

	static final String IMG_LOADER = GWT.getHostPageBaseURL() + "loader.gif";
	static final String IMG_ERROR = GWT.getHostPageBaseURL() + "error.gif";
}
