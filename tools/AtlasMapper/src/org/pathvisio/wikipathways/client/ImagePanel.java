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

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ImagePanel extends StatePanel implements MouseListener {
	State currentState;
	Image image;

	GeneInfo[] geneInfo;
	Label description;

	public ImagePanel(AtlasMapper main) {
		super("", main);

		HorizontalPanel legend = new HorizontalPanel();
		legend.addStyleName(STYLE_LEGEND);

		SimplePanel down = new SimplePanel();
		down.addStyleName(STYLE_LEGEND_COLOR);
		down.addStyleName(STYLE_LEGEND_DOWN);
		Label downLabel = new Label("= down");
		downLabel.addStyleName(STYLE_LEGEND_LABEL);
		legend.add(down);
		legend.add(downLabel);

		SimplePanel up = new SimplePanel();
		up.addStyleName(STYLE_LEGEND_COLOR);
		up.addStyleName(STYLE_LEGEND_UP);
		Label upLabel = new Label("= up");
		upLabel.addStyleName(STYLE_LEGEND_LABEL);
		legend.add(up);
		legend.add(upLabel);

		SimplePanel none = new SimplePanel();
		none.addStyleName(STYLE_LEGEND_COLOR);
		none.addStyleName(STYLE_LEGEND_UPDOWN);
		Label noneLabel = new Label("= up/down");
		noneLabel.addStyleName(STYLE_LEGEND_LABEL);
		legend.add(none);
		legend.add(noneLabel);

		add(legend, NORTH);

		description = new Label(
				"Loading gene data..."
		);
		description.addStyleName(STYLE_DESCRIPTION);
		add(description, NORTH);

		image = new Image();
		image.addMouseListener(this);
		add(image, CENTER);
	}

	protected boolean hasNext() {
		return false;
	}

	public State getState(boolean nextPanel) {
		return currentState;
	}

	public void setState(State state) {
		currentState = state;
		main.startProgress();

		String pathway = state.getValue(State.KEY_PATHWAY);
		String factorType = state.getValue(State.KEY_FACTOR_TYPE);
		String[] factorValues = null;

		String valueString = state.getValue(State.KEY_FACTOR_VALUES);
		if(valueString != null) {
			factorValues = valueString.split(FactorPanel.SEP_FACTOR);
		}

		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Error: " + caught.getMessage());
				main.stopProgress(ImagePanel.this);
			}
			public void onSuccess(String result) {
				image.setUrl(result);
				main.stopProgress(ImagePanel.this);
			}
		};
		main.getService().getImageUrl(pathway, factorType, factorValues, callback);

		//Query gene info
		AsyncCallback<GeneInfo[]> callback2 = new AsyncCallback<GeneInfo[]>() {
			public void onFailure(Throwable caught) {
				Window.alert("Unable to get gene data: " + caught.getMessage());
				description.setText("Unable to get gene data: " + caught.getMessage());
			}
			public void onSuccess(GeneInfo[] result) {
				description.setText("Click on a gene to see details.");
				image.setTitle("Click a gene box for details");
				geneInfo = result;
			}
		};

		Set<String> factorSet = new HashSet<String>();
		for(String f : factorValues) factorSet.add(f);

		main.getService().getGeneInfo(pathway, factorType, factorSet, callback2);
	}

	private Set<GeneInfo> getGenesAt(int x, int y) {
		Set<GeneInfo> genes = new HashSet<GeneInfo>();
		if(geneInfo == null) return genes;

		double rx = (double)x / image.getWidth();
		double ry = (double)y / image.getHeight();

		//Find the genes at this location
		for(GeneInfo gi : geneInfo) {
			if(rx >= gi.getLeft() && rx <= gi.getRight()) {
				if(ry >= gi.getTop() && ry <= gi.getBottom()) {
					genes.add(gi);
				}
			}
		}
		return genes;
	}

	public void onMouseDown(Widget sender, int x, int y) {
		Set<GeneInfo> genes = getGenesAt(x, y);
		if(genes.size() > 0) {
			showTooltip(image.getAbsoluteLeft() + x, image.getAbsoluteTop() + y, genes);
		}
	}

	public void onMouseEnter(Widget sender) {
	}

	public void onMouseLeave(Widget sender) {
	}

	public void onMouseMove(Widget sender, final int x, final int y) {
		if(getGenesAt(x, y).size() > 0) {
			image.addStyleName(STYLE_GENE_HOVER);
		} else {
			image.removeStyleName(STYLE_GENE_HOVER);
		}
	}

	public void onMouseUp(Widget sender, int x, int y) {
	}

	private void showTooltip(int x, int y, Set<GeneInfo> genes) {
		PopupPanel tooltip = new PopupPanel(true, false);
		tooltip.setPopupPosition(x, y);
		tooltip.setWidget(new GeneTooltip(genes));
		tooltip.show();
	}

	public static final String STYLE_GENE_HOVER = "gene-hover";
	public static final String STYLE_LEGEND_COLOR = "legend-color";
	public static final String STYLE_LEGEND_DOWN = "legend-down";
	public static final String STYLE_LEGEND_UP = "legend-up";
	public static final String STYLE_LEGEND_UPDOWN = "legend-updown";
	public static final String STYLE_LEGEND_LABEL = "legend-label";
	public static final String STYLE_LEGEND = "legend";
	public static final String STYLE_DESCRIPTION = "image-description";
}
