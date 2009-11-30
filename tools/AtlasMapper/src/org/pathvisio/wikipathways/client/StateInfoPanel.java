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

import java.util.Arrays;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

public class StateInfoPanel extends FlexTable {
	private Label pathway;
	private Label factors;

	private Hyperlink changePathway;
	private Hyperlink changeFactors;

	private AtlasMapper main;

	public StateInfoPanel(AtlasMapper main) {
		this.main = main;
		addStyleName(STYLE_INFOPANEL);
	}

	public void setState(State state) {
		clear();

		String pathwayValue = state.getValue(State.KEY_PATHWAY);
		String factorType = state.getValue(State.KEY_FACTOR_TYPE);
		String factorValues = state.getValue(State.KEY_FACTOR_VALUES);

		if(pathwayValue == null) {
			//Show a short description
			HTML description = new HTML(
				"Visualize differentially expressed genes from " +
				"<a href='http://www.ebi.ac.uk/microarray-as/atlas/' target='_blank'>" +
				"ArrayExpress Atlas</a> on " +
				"a <a href='http://www.wikipathways.org' target='_blank'>WikiPathways</a> pathway. " +
				"For help and more information, " +
				"<a href='http://www.wikipathways.org/index.php/Help:WikiPathways_Webservice/AtlasMapper'>" +
				"see here.</a>"
			);
			description.addStyleName(STYLE_VAR_VALUE);
			setWidget(0, 0, description);
		}

		if(pathwayValue != null) {
			Label pathwayLabel = new Label("Selected pathway:");
			pathwayLabel.addStyleName(STYLE_VAR_LABEL);
			setWidget(0, 0, pathwayLabel);

			pathway = new Label(pathwayValue);
			pathway.addStyleName(STYLE_VAR_VALUE);
			setWidget(0, 1, pathway);
			main.getService().getPathwayInfo(pathwayValue, new AsyncCallback<PathwayInfo>() {
				public void onFailure(Throwable caught) {
					pathway.setText(pathway.getText() + " (error: " + caught.getMessage() + ")");
				}
				public void onSuccess(PathwayInfo result) {
					pathway.setText(
						result.id + " (" + result.name + ", " + result.organism + ")"
					);
				}
			});

			State changeState = new State(state.toString());
			changeState.setValue(State.KEY_PANEL, State.PANEL_PATHWAY);
			changePathway = new Hyperlink("change", changeState.toString());
			changePathway.setStyleName(STYLE_EDIT);
			setWidget(0, 2, changePathway);
		}

		if(factorType != null && factorValues != null) {
			Label factorLabel = new Label("Selected conditions:");
			factorLabel.addStyleName(STYLE_VAR_LABEL);
			setWidget(1, 0, factorLabel);

			String factorString = Arrays.toString(
					factorValues.split(FactorPanel.SEP_FACTOR)
			);
			factorString += " (" + factorType + ")";
			factors = new Label(factorString);
			factors.addStyleName(STYLE_VAR_VALUE);
			setWidget(1, 1, factors);

			State changeState = new State(state.toString());
			changeState.setValue(State.KEY_PANEL, State.PANEL_FACTOR);
			changeFactors = new Hyperlink("change", changeState.toString());
			changeFactors.setStyleName(STYLE_EDIT);
			setWidget(1, 2, changeFactors);
		}

	}

	static final String STYLE_VAR_LABEL = "var-label";
	static final String STYLE_VAR_VALUE = "var-value";
	static final String STYLE_EDIT = "var-edit";
	static final String STYLE_INFOPANEL = "info-panel";
}
