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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class AtlasMapper implements EntryPoint, HistoryListener {
	private AtlasMapperServiceAsync service;

	private PathwayPanel pathwayPanel;
	private FactorPanel factorPanel;
	private ImagePanel imagePanel;

	private Panel progressPanel;
	private DeckPanel deckPanel;
	private StateInfoPanel infoPanel;

	public void onModuleLoad() {
		History.addHistoryListener(this);

		DockPanel mainPanel = new DockPanel();
		mainPanel.addStyleName(STYLE_ROOT);

		//Main title
		Label title = new Label("WikiPathways-Atlas Mapper");
		title.addStyleName(STYLE_TITLE);
		mainPanel.add(title, DockPanel.NORTH);

		//Current state info
		infoPanel = new StateInfoPanel(this);
		mainPanel.add(infoPanel, DockPanel.NORTH);

		deckPanel = new DeckPanel();
		deckPanel.addStyleName(STYLE_CENTER);

		progressPanel = createProgressPanel();
		deckPanel.add(progressPanel);

		pathwayPanel = createPathwayPanel();
		deckPanel.add(pathwayPanel);

		factorPanel = createFactorPanel();
		deckPanel.add(factorPanel);

		imagePanel = createImagePanel();
		deckPanel.add(imagePanel);

		mainPanel.add(deckPanel, DockPanel.CENTER);
		RootPanel.get().add(mainPanel);

		deckPanel.showWidget(deckPanel.getWidgetIndex(pathwayPanel));

		//Process url parameters
		String text = Window.Location.getHash();
		if(text == null) text = "";
		if(text.startsWith("#")) text = text.substring(1);
		text = URL.decode(text);
		onHistoryChanged(text);
	}

	public void onHistoryChanged(String historyToken) {
		State state = new State(historyToken);

		//Find the right panel to show
		System.err.println("Setting state " + state);

		infoPanel.setState(state);

		StatePanel showPanel = pathwayPanel; //Show pathway panel by default

		String statePanel = state.getValue(State.KEY_PANEL);
		if(State.PANEL_IMAGE.equals(statePanel)) {
			//Check if we have enough info for image panel
			if(state.getValue(State.KEY_FACTOR_TYPE) != null &&
					state.getValue(State.KEY_FACTOR_VALUES) != null) {
				showPanel = imagePanel;
			} else {
				//If not, try previous panel
				state.setValue(State.KEY_PANEL, State.PANEL_FACTOR);
			}
		}
		if(State.PANEL_FACTOR.equals(statePanel)) {
			//Check if we have enough info for factor panel
			if(state.getValue(State.KEY_PATHWAY) != null) {
				showPanel = factorPanel;
			} else {
				state.setValue(State.KEY_PANEL, State.PANEL_PATHWAY);
				showPanel = pathwayPanel;
			}
		}
		showPanel(showPanel);
		showPanel.setState(state);
	}

	private void showPanel(Widget w) {
		deckPanel.showWidget(deckPanel.getWidgetIndex(w));
	}

	private Panel createProgressPanel() {
		HorizontalPanel progressPanel = new HorizontalPanel();
		progressPanel.add(new Image("loader.gif"));
		progressPanel.add(new Label("Loading..."));
		progressPanel.addStyleName(STYLE_PROGRESS);
		return progressPanel;
	}

	private PathwayPanel createPathwayPanel() {
		PathwayPanel pathwayPanel = new PathwayPanel(this);
		return pathwayPanel;
	}

	private FactorPanel createFactorPanel() {
		FactorPanel factorPanel = new FactorPanel(this);
		return factorPanel;
	}

	private ImagePanel createImagePanel() {
		ImagePanel imagePanel = new ImagePanel(this);
		return imagePanel;
	}

	protected void startProgress() {
		deckPanel.showWidget(deckPanel.getWidgetIndex(progressPanel));
	}

	protected void stopProgress(Widget widget) {
		deckPanel.showWidget(deckPanel.getWidgetIndex(widget));
	}

	public AtlasMapperServiceAsync getService() {
		if(service == null) {
			service = GWT.create(AtlasMapperService.class);
		}
		return service;
	}

	static final String STYLE_PROGRESS = "progress";
	static final String STYLE_ROOT = "root-panel";
	static final String STYLE_CENTER = "center-panel";
	static final String STYLE_TITLE = "root-title";
	static final String STYLE_DESCRIPTION = "root-description";
}
