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
package org.pathvisio.gui.dialogs;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.pathvisio.core.data.XrefWithSymbol;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.gui.DataSourceModel;
import org.pathvisio.gui.ProgressDialog;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.completer.CompleterQueryTextField;
import org.pathvisio.gui.completer.OptionProvider;
import org.pathvisio.gui.util.PermissiveComboBox;
import org.xml.sax.SAXException;

public class LineDialog extends PathwayElementDialog implements ItemListener {

	/**
	 * Dialog for editing Reactions/ Interactions. In addition to the standard
	 * comments and literature tabs, this has a tab for looking up accession
	 * numbers of reactions/interactions.
	 */
	private static final long serialVersionUID = 1L;
	private CompleterQueryTextField idText;
	private String startNodeRef;
	private String endNodeRef;
	private PermissiveComboBox dbCombo;
	private PermissiveComboBox typeCombo;
	private DataSourceModel dsm;
	// private XrefWithSymbol ref;
	private String rheaWS = "http://www.rhea-db.org/rest/1.0/ws/reaction/cmlreact?q=";
	private Pathway pathway;

	protected LineDialog(final SwingEngine swingEngine, final PathwayElement e,
			final boolean readonly, final Frame frame,
			final Component locationComp) {
		super(swingEngine, e, readonly, frame, "Interaction properties",
				locationComp);
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();

	}

	public final void refresh() {
		super.refresh();
		pathway = getInput().getPathway();
		startNodeRef = getInput().getStartGraphRef();
		endNodeRef = getInput().getEndGraphRef();
		idText.setText(getInput().getElementID());
		dsm.setSelectedItem(input.getDataSource());
		String lType = getInput().getEndLineType().toString();
		typeCombo.setSelectedItem(LineType.fromName(lType));
		dsm.setInteractionFilter(true);
		pack();
	}

	/**
	 * Search for identifiers for the selected interaction in Rhea
	 * (http://www.rhea-db.org/home) based on identifiers of the nodes that are
	 * connected by the interaction
	 * 
	 * @param pathway2
	 */
	private void search(Pathway pwy, final String startNode,
			final String endNode) {

		String startNodeId = getElementId(startNode, pwy);
		String endNodeId = getElementId(endNode, pwy);

		if ((startNodeId == null || "".equals(startNodeId.trim()))
				&& (endNodeId == null || "".equals(endNodeId.trim()))) {
			JOptionPane
					.showMessageDialog(
							this,
							"Interactors not annotated, "
									+ "please annotate the interacting datanodes by double-clicking on them");
			return;
		}
		String query = "";
		// Eg. query:
		// http://www.rhea-db.org/rest/1.0/ws/reaction?q=CHEBI:17632+CHEBI:16301

		if (startNodeId == null || "".equals(startNodeId.trim())) {
			query = rheaWS + endNodeId.trim();
		} else if (endNodeId == null || "".equals(endNodeId.trim())) {
			query = rheaWS + startNodeId.trim();
		} else {
			query = rheaWS + startNodeId.trim() + "+" + endNodeId.trim();
		}

		// String query = rheaWS + startNodeId.trim() + "+" + endNodeId.trim();
		final String text = query.trim();
		System.out.println("query:" + text);

		final ProgressKeeper progress = new ProgressKeeper();
		ProgressDialog dialog = new ProgressDialog(this, "Searching", progress,
				true, true);
		dialog.setLocationRelativeTo(this);

		SwingWorker<List<XrefWithSymbol>, Void> sw = new SwingWorker<List<XrefWithSymbol>, Void>() {
			private static final int QUERY_LIMIT = 200;

			protected List<XrefWithSymbol> doInBackground()
					throws IDMapperException {

				// querying Rhea using webservice

				// The result set
				List<XrefWithSymbol> result = new ArrayList<XrefWithSymbol>();
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();

					URL queryText = new URL(text);
					org.w3c.dom.Document doc = db.parse(queryText.openStream());

					String text2parse = doc.getDocumentElement()
							.getTextContent();
					text2parse = text2parse.replaceAll(
							"^\\s+|\\s+$|\\s*(\n)\\s*|(\\s)\\s*", "$1$2")
							.replace("\t", " ");
					String[] parsedText = text2parse.split("\n");

					for (int i = 0; i < parsedText.length; i = i + 8) {
						/*
						 * Get id
						 */
						// System.out.println("id" + parsedText[i]);
						Xref intxref = new Xref(parsedText[i],
								DataSource.getExistingBySystemCode("Rh"));

						/*
						 * Get uri
						 */
						String interactionUri = parsedText[i + 2];
						// System.out.println("uri" + interactionUri);
						//
						result.add(new XrefWithSymbol(intxref, "reaction"));
					}
					//
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return result;
			}

			private void applyAutoFill(XrefWithSymbol ref) {
				input.setElementID(ref.getId());
				input.setDataSource(ref.getDataSource());
				idText.setText(ref.getId());
				dsm.setSelectedItem(ref.getDataSource());
			}

			@Override
			public void done() {
				progress.finished();
				if (!progress.isCancelled()) {
					List<XrefWithSymbol> results = null;
					try {
						results = get();
						// Show results to user
						if (results != null && results.size() > 0) {
							DatabaseSearchDialog resultDialog = new DatabaseSearchDialog(
									"Results", results);
							resultDialog.setVisible(true);
							XrefWithSymbol selected = resultDialog
									.getSelected();
							if (selected != null) {
								applyAutoFill(selected);
							}
						} else {
							JOptionPane.showMessageDialog(LineDialog.this,
									"No results for '" + text + "'");
						}
					} catch (InterruptedException e) {
						// Ignore, thread interrupted. Same as cancel.
					} catch (ExecutionException e) {
						JOptionPane.showMessageDialog(LineDialog.this,
								"Exception occurred while searching,\n"
										+ "see error log for details.",
								"Error", JOptionPane.ERROR_MESSAGE);
						Logger.log.error("Error while searching", e);
					}
				}
			}
		};
		sw.execute();
		dialog.setVisible(true);
	}

	protected final void addCustomTabs(final JTabbedPane parent) {

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JPanel searchPanel = new JPanel();
		JPanel fieldPanel = new JPanel();
		searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
		fieldPanel.setBorder(BorderFactory.createTitledBorder("Manual entry"));
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.gridx = 0;
		panelConstraints.weightx = 1;
		panelConstraints.weighty = 1;
		panelConstraints.insets = new Insets(2, 2, 2, 2);
		panelConstraints.gridy = GridBagConstraints.RELATIVE;

		panel.add(searchPanel, panelConstraints);
		panel.add(fieldPanel, panelConstraints);

		// Search panel elements
		searchPanel.setLayout(new GridBagLayout());

		final JLabel searchText = new JLabel("Search in Rhea");
		final JButton searchButton = new JButton("Search");
		// final String startNodeId = getElementId(startNodeRef);
		// final String endNodeId = getElementId(endNodeRef);
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				search(getInput().getPathway(), getInput().getStartGraphRef(),
						getInput().getEndGraphRef());
			}
		});
		searchButton
				.setToolTipText("Search the online Rhea database for references, based on the identifiers of the interactors");

		GridBagConstraints searchConstraints = new GridBagConstraints();
		searchConstraints.gridx = GridBagConstraints.RELATIVE;
		searchConstraints.fill = GridBagConstraints.HORIZONTAL;

		searchConstraints.weightx = 1;
		searchPanel.add(searchText, searchConstraints);

		searchConstraints.weightx = 0;
		searchPanel.add(searchButton, searchConstraints);

		// Manual entry panel elements
		fieldPanel.setLayout(new GridBagLayout());

		JLabel typeLabel = new JLabel("Biological Type");
		JLabel idLabel = new JLabel("Identifier");
		JLabel dbLabel = new JLabel("Database");

		idText = new CompleterQueryTextField(new OptionProvider() {
			public List<String> provideOptions(final String text) {
				if (text == null) {
					return Collections.emptyList();
				}

				IDMapperStack gdb = swingEngine.getGdbManager().getCurrentGdb();
				Set<Xref> refs = new HashSet<Xref>();
				try {
					refs = gdb.freeSearch(text, 100);
				} catch (IDMapperException ignore) {
				}

				// Only take identifiers
				List<String> ids = new ArrayList<String>();
				for (Xref ref : refs)
					ids.add(ref.getId());
				return ids;
			}
		}, true);

		idText.setCorrectCase(false);

		dsm = new DataSourceModel();
		dsm.setPrimaryFilter(true);
		dsm.setSpeciesFilter(swingEngine.getCurrentOrganism());
		dbCombo = new PermissiveComboBox(dsm);
		typeCombo = new PermissiveComboBox(LineType.getValues());

		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = c.ipady = 5;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		fieldPanel.add(typeLabel, c);
		fieldPanel.add(idLabel, c);
		fieldPanel.add(dbLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;

		fieldPanel.add(typeCombo, c);
		fieldPanel.add(idText, c);
		fieldPanel.add(dbCombo, c);

		idText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(final DocumentEvent e) {
				setText();
			}

			public void insertUpdate(final DocumentEvent e) {
				setText();
			}

			public void removeUpdate(final DocumentEvent e) {
				setText();
			}

			private void setText() {
				getInput().setElementID(idText.getText());
			}
		});

		dsm.addListDataListener(new ListDataListener() {

			public void contentsChanged(final ListDataEvent arg0) {
				getInput().setDataSource((DataSource) dsm.getSelectedItem());

			}

			public void intervalAdded(final ListDataEvent arg0) {
			}

			public void intervalRemoved(final ListDataEvent arg0) {
			}
		});

		typeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				LineType item = (LineType) typeCombo.getSelectedItem();
				getInput().setEndLineType(item);
				refresh();
			}
		});

		idText.setEnabled(!readonly);
		dbCombo.setEnabled(!readonly);
		typeCombo.setEnabled(!readonly);

		parent.add("Annotation", panel);
		parent.setSelectedComponent(panel);
	}

	private String getElementId(String nodeRef, Pathway pwy) {
		System.out.println("ref " + nodeRef);
		String id = "";
		// System.out.println(pathway.getMappInfo().getMapInfoName());
		for (PathwayElement pe : pwy.getDataObjects()) {
			if (!(pe.getGraphId() == null)) {
				if (pe.getGraphId().equalsIgnoreCase(nodeRef)) {
					id = pe.getElementID();
					System.out.println("id " + id);
					if (pe.getDataSource() != DataSource
							.getExistingBySystemCode("Ce")) {
						id = pe.getTextLabel();
					}
				} else {
					// TODO
					/*
					 * handle anchors
					 */
					if (pe.getObjectType() == ObjectType.LINE) {
						// System.out.println(pe.getMPoints());
					}
				}
				// System.out.println("node graph id "+pe.getGraphId());
			}
		}
		return id;
	}

	@Override
	public void itemStateChanged(final ItemEvent arg0) {
		// TODO Auto-generated method stub
	}
}