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
package org.pathvisio.cytoscape.wikipathways;

import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

import giny.view.NodeView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.pathvisio.cytoscape.wikipathways.CyWikiPathwaysClient.SearchInteractionsTask;

public class NodeInteractionsAction extends AbstractAction {
	InteractionQuery query;
	CyWikiPathwaysClient client;

	public NodeInteractionsAction(CyWikiPathwaysClient client, NodeView nv) {
		this.query = new InteractionQuery(nv, "canonicalName"); //TODO: make attribute a property
		this.client = client;

		putValue(NAME, "Find interactions for " + query.getAttributeValue());
	}

	public void actionPerformed(ActionEvent e) {
		SearchInteractionsTask task = client.new SearchInteractionsTask(query);
		JTaskConfig config = new JTaskConfig();
		config.displayCancelButton(false);
		config.setModal(true);
		TaskManager.executeTask(task, config);
	}
}
