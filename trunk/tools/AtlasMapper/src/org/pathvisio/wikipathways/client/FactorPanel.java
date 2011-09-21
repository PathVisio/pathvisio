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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class FactorPanel extends StatePanel {
	String pathway;

	ListBox factorBox;
	FlexTable conditionPanel;
	Panel centerPanel;

	Map<String, FactorInfo> factors = new HashMap<String, FactorInfo>();
	Map<String, Set<String>> selectedFactors = new HashMap<String, Set<String>>();

	State currentState;

	public FactorPanel(AtlasMapper main) {
		super("Select conditions", main);

		centerPanel = new VerticalPanel();
		add(centerPanel, CENTER);

		HorizontalPanel factorPanel = new HorizontalPanel();
		factorPanel.add(new Label("Condition type:"));

		factorBox = new ListBox();
		factorBox.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				factorChanged();
			}
		});

		factorPanel.add(factorBox);

		centerPanel.add(factorPanel);

		conditionPanel = new FlexTable();
		centerPanel.add(conditionPanel);
	}

	public State getState(boolean nextPanel) {
		State state = new State();
		if(pathway != null) {
			state.setValue(State.KEY_PATHWAY, pathway);
		}
		String factorType = factorBox.getItemText(factorBox.getSelectedIndex());
		if(factorType != null) {
			state.setValue(State.KEY_FACTOR_TYPE, factorType);
			Set<String> selected = selectedFactors.get(factorType);
			if(selected != null) {
				String factorValue = "";
				for(String s : selected) {
					factorValue += s + SEP_FACTOR;
				}
				if(factorValue.length() > SEP_FACTOR.length()) {
					state.setValue(
							State.KEY_FACTOR_VALUES,
							factorValue.substring(0, factorValue.length() - SEP_FACTOR.length())
					);
				}
			}
		}
		if(nextPanel) {
			state.setValue(State.KEY_PANEL, State.PANEL_IMAGE);
		} else {
			state.setValue(State.KEY_PANEL, State.PANEL_FACTOR);
		}
		return state;
	}

	public void setState(State state) {
		currentState = state;
		pathway = state.getValue(State.KEY_PATHWAY);
		queryFactors();
	}

	void factorChecked(CheckBox box) {
		String factorType = factorBox.getItemText(factorBox.getSelectedIndex());
		Set<String> selected = selectedFactors.get(factorType);
		if(box.isChecked()) {
			if(selected == null) {
				selectedFactors.put(factorType, selected = new HashSet<String>());
			}
			selected.add(box.getText());
		} else {
			if(selected != null) {
				selected.remove(box.getText());
			}
		}
	}

	void factorChanged() {
		conditionPanel.clear();
		String name = factorBox.getItemText(factorBox.getSelectedIndex());
		Set<String> selected = selectedFactors.get(name);
		if(name != null && factors.containsKey(name)) {
			String[] values = factors.get(name).values;

			int row = 0;
			int col = 0;
			for(String v : values) {
				if(col == NR_COL) {
					row++;
					col = 0;
				}
				final CheckBox check = new CheckBox(v);
				check.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						factorChecked(check);
					}
				});
				conditionPanel.setWidget(row, col++, check);
				check.setChecked(selected != null && selected.contains(v));
			}
		}
	}

	void refreshFactors() {
		factorBox.clear();
		conditionPanel.clear();

		//Create a checkbox for each factor
		int selected = 0;
		if(factors != null && factors.size() > 0) {
			List<FactorInfo> flist = new ArrayList<FactorInfo>(factors.values());
			Collections.sort(flist, new Comparator<FactorInfo>() {
				public int compare(FactorInfo o1, FactorInfo o2) {
					return o1.name.compareTo(o2.name);
				}
			});
			for(FactorInfo fi : flist) {
				factorBox.addItem(fi.name);
			}
			String factorType = currentState.getValue(State.KEY_FACTOR_TYPE);
			if(factorType != null) {
				selected = flist.indexOf(factors.get(factorType));
			}
			factorBox.setSelectedIndex(selected < 0 ? 0 : selected);
			factorChanged();
		} else { //No data found for this pathway
			centerPanel.clear();
			centerPanel.add(new Label("No data found on Atlas for this pathway."));
			State changeState = new State();
			changeState.setValue(State.KEY_PANEL, State.PANEL_PATHWAY);
			centerPanel.add(new Hyperlink("Select a different pathway", changeState.toString()));
		}
		refreshButton();
	}

	protected boolean hasNext() {
		return factors != null && factors.size() > 0;
	}

	void queryFactors() {
		AsyncCallback<FactorInfo[]> callback = new AsyncCallback<FactorInfo[]>() {
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				Window.alert("Unable to download experiment\n" + caught.getMessage());
			}
			public void onSuccess(FactorInfo[] result) {
				factors.clear();

				//Create a map of all factors
				for(FactorInfo fi : result) {
					factors.put(fi.name, fi);
				}

				//Find the selected factors
				selectedFactors.clear();

				if(currentState != null) {
					String factorType = currentState.getValue(State.KEY_FACTOR_TYPE);
					String valueString = currentState.getValue(State.KEY_FACTOR_VALUES);
					FactorInfo factorInfo = factors.get(factorType);

					if(valueString != null && factorInfo != null) {
						Set<String> existValues = new HashSet<String>();
						for(String v : factorInfo.values) existValues.add(v);

						String[] values = valueString.split(SEP_FACTOR);
						Set<String> selected = new HashSet<String>();
						for(String v : values) {
							if(existValues.contains(v)) selected.add(v);
						}
						if(selected.size() > 0) {
							selectedFactors.put(factorType, selected);
						}
					}
				}

				refreshFactors();
				main.stopProgress(FactorPanel.this);
			}
		};
		main.startProgress();
		main.getService().getFactors(pathway, callback);
	}

	static final int NR_COL = 3;
	static final String SEP_FACTOR = ";";
}
