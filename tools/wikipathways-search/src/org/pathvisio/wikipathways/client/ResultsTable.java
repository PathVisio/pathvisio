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


import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ResultsTable extends FlexTable {
	int row;
	
	public void addResults(Result[] results) {
		for(Result r : results) {
			addLabel(r);
			addImage(r);
			row++;
		}
	}
	
	public void clear() {
		super.clear();
		row = 0;
	}
	
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
		
		String p = "<UL>";
		for(String key : result.getProperties().keySet()) {
			p += "<LI><B>" + key + "</B>: " + Arrays.toString(result.getProperties().get(key));
		}
		p += "</UL>";
		HTML props = new HTML(p);
		labelPanel.add(props);
		
		setWidget(row, 1, labelPanel);
	}
	
	private void addImage(Result result) {
		Image image = new Image(IMG_LOADER);
		image.setStylePrimaryName(STYLE_IMAGE);
		image.setTitle("Please wait...loading image");
		
		ImageLink imageLink = new ImageLink(image, result.getUrl());
		imageLink.setStylePrimaryName(STYLE_IMG_CONTAINER);
		setWidget(row, 0, imageLink);
		getCellFormatter().setAlignment(
				row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		loadImage(result, image);
	}
	
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
	
	public static final String STYLE_DESCRIPTION = "result-description";
	public static final String STYLE_TITLE = "result-title";
	public static final String STYLE_LABEL = "result-label";
	public static final String STYLE_IMAGE = "result-image";
	public static final String STYLE_IMG_CONTAINER = "result-image-container";
	
	public static final String GET_ID = "id";
	
	static final String IMG_LOADER = GWT.getHostPageBaseURL() + "loader.gif";
	static final String IMG_ERROR = GWT.getHostPageBaseURL() + "error.gif";
}
