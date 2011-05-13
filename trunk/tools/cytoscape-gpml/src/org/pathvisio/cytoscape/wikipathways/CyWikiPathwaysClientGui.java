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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import cytoscape.data.webservice.CyWebServiceEvent;
import cytoscape.data.webservice.CyWebServiceEvent.WSEventType;
import cytoscape.data.webservice.CyWebServiceException;
import cytoscape.data.webservice.WebServiceClientManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.cytoscape.wikipathways.CyWikiPathwaysClient.FindPathwaysByTextParameters;
import org.pathvisio.cytoscape.wikipathways.CyWikiPathwaysClient.GetPathwayParameters;
import org.pathvisio.desktop.util.ListWithPropertiesTableModel;
import org.pathvisio.desktop.util.RowWithProperties;
import org.pathvisio.wikipathways.webservice.WSSearchResult;

/**
 * GUI for accessing the WikiPathways webservice,
 * lets the user query for a list of Pathways
 */
public class CyWikiPathwaysClientGui extends JPanel implements ActionListener {
	final CyWikiPathwaysClient client;

	JComboBox organismCombo;
	JTextField searchText;
	JTable resultTable;
	ListWithPropertiesTableModel<ResultProperty, ResultRow> tableModel;

	public CyWikiPathwaysClientGui(CyWikiPathwaysClient c) {
		client = c;

		organismCombo = new JComboBox();
		resetOrganisms();

		searchText = new JTextField();
		searchText.setActionCommand(ACTION_SEARCH);
		searchText.addActionListener(this);

		JButton searchBtn = new JButton("Search");
		searchBtn.setActionCommand(ACTION_SEARCH);
		searchBtn.addActionListener(this);

		resultTable = new JTable();
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = resultTable.getSelectedRow();
					ResultRow selected = tableModel.getRow(row);
					openNetwork(selected);
				}
			}
		});

		setLayout(new FormLayout(
				"4dlu, pref, 2dlu, fill:pref:grow, 4dlu, pref, 4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, fill:pref:grow, 4dlu"
		));
		CellConstraints cc = new CellConstraints();
		add(new JLabel("Search:"), cc.xy(2, 2));
		add(searchText, cc.xy(4, 2));
		add(organismCombo, cc.xy(6, 2));
		add(searchBtn, cc.xy(8, 2));
		add(new JScrollPane(resultTable), cc.xyw(2, 4, 7));
	}

	protected void resetOrganisms() {
		List<String> organisms = new ArrayList<String>();
		organisms.add(ORGANISM_ALL);
		try {
			organisms.addAll(Arrays.asList(client.listOrganisms()));
		} catch (Exception e) {
			Logger.log.error("Unable to get organisms for WikiPathways client", e);
		}

		organismCombo.setModel(new DefaultComboBoxModel(organisms.toArray()));
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(ACTION_SEARCH.equals(action)) {
			FindPathwaysByTextParameters request = new FindPathwaysByTextParameters();
			request.query = searchText.getText();
			String org = organismCombo.getSelectedItem().toString();
			if(!ORGANISM_ALL.equals(org)) {
				request.species = Organism.fromLatinName(org);
			}
			try {
				WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(
					new CyWebServiceEvent<FindPathwaysByTextParameters>(
						client.getClientID(),
						WSEventType.SEARCH_DATABASE,
						request
					)
				);
			} catch (CyWebServiceException ex) {
				switch(ex.getErrorCode()) {
				case NO_RESULT:
					JOptionPane.showMessageDialog(
							this, "The search didn't return any results",
							"No results", JOptionPane.INFORMATION_MESSAGE
					);
					break;
				case OPERATION_NOT_SUPPORTED:
				case REMOTE_EXEC_FAILED:
					JOptionPane.showMessageDialog(
						this, "Error: " + ex.getErrorCode() + ". See log for details",
						"Error", JOptionPane.ERROR_MESSAGE
					);
					break;
				}
				ex.printStackTrace();
			}
		}
	}

	private void openNetwork(ResultRow selected) {
		try {
			GetPathwayParameters request = new GetPathwayParameters();
			WSSearchResult result = selected.getResult();
			request.id = result.getId();
			request.revision = Integer.parseInt(result.getRevision());
			WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(
				new CyWebServiceEvent(
						client.getClientID(), WSEventType.IMPORT_NETWORK,
						request
				)
			);
		} catch (CyWebServiceException ex) {
			JOptionPane.showMessageDialog(
				CyWikiPathwaysClientGui.this, "Error: " + ex.getErrorCode() + ". See error log for details",
				"Error", JOptionPane.ERROR_MESSAGE
			);
		}
	}

	public void setResults(WSSearchResult[] results) {
		tableModel =
			new ListWithPropertiesTableModel<ResultProperty, ResultRow>();
		if(results != null) {
			tableModel.setColumns(new ResultProperty[] {
					ResultProperty.NAME,
					ResultProperty.ORGANISM,
			});
			resultTable.setModel(tableModel);
			for(WSSearchResult r : results) {
				tableModel.addRow(new ResultRow(r));
			}
		}
		resultTable.setModel(tableModel);
	}

	/**
	 * Represents a hit, a single row in the query results table.
	 */
	class ResultRow implements RowWithProperties<ResultProperty> {
		WSSearchResult result;

		public ResultRow(WSSearchResult result) {
			this.result = result;
		}

		public WSSearchResult getResult() {
			return result;
		}

		public String getProperty(ResultProperty prop) {
			switch(prop) {
			case NAME: return result.getName();
			case ORGANISM: return result.getSpecies();
			case SCORE: return Double.toString(result.getScore());
			case URL: return result.getUrl();
			}
			return null;
		}
	}

	private static final String ACTION_SEARCH = "Search";
	private static final String ORGANISM_ALL = "All organisms";
}
