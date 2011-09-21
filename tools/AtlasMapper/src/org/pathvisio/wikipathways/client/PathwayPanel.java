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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;

public class PathwayPanel extends StatePanel {
	ListBox organismBox;
	TextBox pathwayTextBox;
	SuggestBox pathwayBox;
	MultiWordSuggestOracle pathwayOracle;

	Panel pathwayProgress;

	State currState;

	public PathwayPanel(AtlasMapper main) {
		super("Select pathway", main);
		Grid grid = new Grid(2, 3);
		grid.setWidget(1, 0, new Label("Pathway:"));

		pathwayProgress = new HorizontalPanel();
		pathwayProgress.addStyleName(STYLE_PROGRESS);
		pathwayProgress.add(new Image("loader.gif"));
		pathwayProgress.add(new Label("Loading pathways..."));

		grid.setWidget(1, 2, pathwayProgress);

		organismBox = new ListBox();

		pathwayOracle = new MultiWordSuggestOracle();
		pathwayTextBox = new TextBox();
		pathwayBox = new SuggestBox(pathwayOracle, pathwayTextBox);
		pathwayTextBox.setEnabled(false);

		queryPathways();

		grid.setWidget(1, 1, pathwayBox);

		add(grid, CENTER);
	}

	public State getState(boolean nextPanel) {
		State state = new State();
		if(currState != null) {
			state = new State(currState.toString());
		}
		//Try to find the pathway id
		//Unfortunately we can't use java.util.regex
		String txt = pathwayBox.getText() + ID_SEP;
		String id = txt.substring(0, txt.indexOf(ID_SEP));
		state.setValue(State.KEY_PATHWAY, id);
		if(nextPanel) {
			state.setValue(State.KEY_PANEL, State.PANEL_FACTOR);
		} else {
			state.setValue(State.KEY_PANEL, State.PANEL_PATHWAY);
		}
		return state;
	}

	public void setState(State state) {
		currState = state;
		pathwayBox.setText(state.getValue(State.KEY_PATHWAY));
	}

	AtlasMapperServiceAsync getService() {
		return main.getService();
	}

	void queryPathways() {
		AsyncCallback<PathwayInfo[]> callback = new AsyncCallback<PathwayInfo[]>() {
			public void onFailure(Throwable caught) {
				pathwayProgress.setVisible(false);
				Window.alert("Unable to get pathways");
			}
			public void onSuccess(PathwayInfo[] result) {
				pathwayOracle.clear();
				for(PathwayInfo p : result) {
					pathwayOracle.add(
						p.id + ID_SEP + "( " + p.name + ", " + p.organism + " )"
					);
				}
				pathwayTextBox.setEnabled(true);
				pathwayProgress.setVisible(false);
			}
		};
		pathwayProgress.setVisible(true);
		getService().getPathways(callback);
	}

	static final String ID_SEP = " ";
	static final String ALL_ORG = "All organisms";

	static final String STYLE_PROGRESS = "pathwaypanel-progress";
}
