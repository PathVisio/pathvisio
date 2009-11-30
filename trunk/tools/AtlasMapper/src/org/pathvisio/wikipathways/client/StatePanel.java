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

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class StatePanel extends DockPanel {
	AtlasMapper main;
	Label titleLabel;
	Button button;

	public StatePanel(String title, AtlasMapper main) {
		this.main = main;

		setStylePrimaryName(STYLE_PANEL);

		titleLabel = new Label(title);
		titleLabel.addStyleName(STYLE_TITLE);
		add(titleLabel, NORTH);

		button = new Button("Next");
		button.addStyleName(STYLE_BUTTON);
		button.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				//First store current state in history
				History.newItem(getState(false).toString(), false);
				//Then go to next panel
				History.newItem(getState(true).toString(), true);
			}
		});
		add(button, SOUTH);
		refreshButton();
	}

	protected void refreshButton() {
		button.setVisible(hasNext());
	}

	protected void setTitleLabel(String title) {
		titleLabel.setText(title);
	}

	protected boolean hasNext() {
		return true;
	}

	public abstract void setState(State state);
	public abstract State getState(boolean nextPanel);

	static final String STYLE_TITLE = "config-title";
	static final String STYLE_PANEL= "config-panel";
	static final String STYLE_BUTTON = "config-button";
}
