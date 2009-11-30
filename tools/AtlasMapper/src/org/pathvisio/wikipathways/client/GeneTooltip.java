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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GeneTooltip extends VerticalPanel {
	static final int ROUND_TO = 3;

	public GeneTooltip(Set<GeneInfo> genes) {
		Map<String, Set<GeneInfo>> byLabel = new TreeMap<String, Set<GeneInfo>>();
		for(GeneInfo gi : genes) {
			Set<GeneInfo> labelGenes = byLabel.get(gi.getLabel());
			if(labelGenes == null) {
				byLabel.put(gi.getLabel(), labelGenes = new HashSet<GeneInfo>());
			}
			labelGenes.add(gi);
		}

		for(String label : byLabel.keySet()) {
			Panel p = createPanelForLabel(label, byLabel.get(label));
			p.addStyleName(STYLE_LABEL_PANEL);
			add(p);
		}
	}

	private Panel createPanelForLabel(String label, Set<GeneInfo> genes) {
		VerticalPanel mainPanel = new VerticalPanel();
		Label title = new Label(label);
		title.addStyleName(STYLE_LABEL);
		mainPanel.add(title);

		for(GeneInfo gi : genes) {
			String ensLink = "<a target='_blank' title='View gene in Ensembl' " +
					"href='" + gi.getGeneLink() + "'>" + gi.getGeneLinkName() + "</a>";
			String whLink = "<a target='_blank' title='View expression profile in ArrayExpress Warehouse' " +
					"href='" + gi.getWarehouseLink() + "'>Warehouse</a>";
			HTML ens = new HTML(gi.getId() + " " + ensLink + " " + whLink);
			ens.addStyleName(STYLE_ENSEMBL);
			mainPanel.add(ens);

			Panel data = createDataPanel(gi);
			data.addStyleName(STYLE_DATA);
			mainPanel.add(data);
		}
		return mainPanel;
	}

	private Panel createDataPanel(GeneInfo geneInfo) {
		Map<String, Set<ExpressionValue>> data = geneInfo.getData();

		List<String> experiments = new ArrayList<String>();
		for(Set<ExpressionValue> evs : data.values()) {
			for(ExpressionValue ev : evs) {
				if(!experiments.contains(ev.getExperiment())) {
					experiments.add(ev.getExperiment());
				}
			}
		}
		Collections.sort(experiments);

		Grid grid = new Grid(data.size() + 1, experiments.size() + 1);

		NumberFormat format = NumberFormat.getFormat("0.###E0");

		int row = 0;

		//Create header
		if(data.size() > 0) {
			Label lbl = new Label("Condition");
			lbl.addStyleName(STYLE_DATA_HEADER);
			grid.setWidget(row, 0, lbl);
			for(int i = 0; i < experiments.size(); i++) {
				lbl = new Label(experiments.get(i));
				lbl.addStyleName(STYLE_DATA_HEADER);
				grid.setWidget(row, i + 1, lbl);
			}
			row++;
		}

		for(String factor : data.keySet()) {
			Set<ExpressionValue> values = data.get(factor);
			Label flbl = new Label(factor);
			flbl.addStyleName(STYLE_DATA_FACTOR);

			for(ExpressionValue value : values) {
				int col = 1 + experiments.indexOf(value.getExperiment());
				String valueStr = format.format(value.getPvalue());
				Label vlbl = new Label(valueStr);
				vlbl.addStyleName(STYLE_DATA_VALUE);
				if(value.getSign() > 0) {
					vlbl.addStyleName(STYLE_DATA_VALUE_UP);
				} else {
					vlbl.addStyleName(STYLE_DATA_VALUE_DOWN);
				}
				grid.setWidget(row, col, vlbl);
			}
			grid.setWidget(row++, 0, flbl);
		}
		return grid;
	}

	static final String STYLE_LABEL = "tooltip-label";
	static final String STYLE_LABEL_PANEL = "tooltip-labelpanel";
	static final String STYLE_ENSEMBL = "tooltip-ensembl";
	static final String STYLE_DATA = "tooltip-data";
	static final String STYLE_DATA_FACTOR = "tooltip-data-factor";
	static final String STYLE_DATA_VALUE = "tooltip-data-value";
	static final String STYLE_DATA_HEADER = "tooltip-data-header";
	static final String STYLE_DATA_VALUE_UP = "tooltip-data-value-up";
	static final String STYLE_DATA_VALUE_DOWN = "tooltip-data-value-down";
}
