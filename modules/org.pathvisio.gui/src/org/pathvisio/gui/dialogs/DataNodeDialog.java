/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.gui.dialogs;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.bridgedb.AttributeMapper;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.pathvisio.core.data.XrefWithSymbol;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.gui.DataSourceModel;
import org.pathvisio.gui.ProgressDialog;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.completer.CompleterQueryTextArea;
import org.pathvisio.gui.completer.CompleterQueryTextField;
import org.pathvisio.gui.completer.OptionProvider;
import org.pathvisio.gui.handler.DataSourceHandler;
import org.pathvisio.gui.util.PermissiveComboBox;

/**
 * Dialog for editing DataNodes. In addition to the standard comments and literature tabs,
 * this has a tab for looking up accession numbers for genes and metabolites.
 */
public class DataNodeDialog extends PathwayElementDialog {

	protected DataNodeDialog(SwingEngine swingEngine, PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		super(swingEngine, e, readonly, frame, "DataNode properties", locationComp);
		curDlg = this;
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();
	}

	CompleterQueryTextArea symText;
	CompleterQueryTextField idText;
	private PermissiveComboBox dbCombo;
	private PermissiveComboBox typeCombo;
	private DataSourceModel dsm;
	private DataNodeDialog curDlg;

	public void refresh() {
		super.refresh();
		symText.setText(getInput().getTextLabel());
		idText.setText(getInput().getElementID());
		dsm.setSelectedItem(input.getDataSource());
		String dnType = getInput().getDataNodeType();
		typeCombo.setSelectedItem(DataNodeType.byName(dnType));
		String[] dsType = null; // null is default: no filtering
		if (DataSourceHandler.DSTYPE_BY_DNTYPE.containsKey(dnType)) dsType = 
			DataSourceHandler.DSTYPE_BY_DNTYPE.get(dnType);
		dsm.setTypeFilter(dsType);
		pack();
	}

	private void applyAutoFill(XrefWithSymbol ref)
	{
		String sym = ref.getSymbol();
		if (sym == null || sym.equals ("")) sym = ref.getId();
		symText.setText(sym);
		idText.setText(ref.getId());
		String type = ref.getDataSource().getType();
		if ("metabolite".equals(type))
			typeCombo.setSelectedItem(DataNodeType.METABOLITE);
		else if ("gene".equals(type))
			typeCombo.setSelectedItem(DataNodeType.GENEPRODUCT);
		else if ("protein".equals(type))
			typeCombo.setSelectedItem(DataNodeType.PROTEIN);
		else if ("pathway".equals(type))
			typeCombo.setSelectedItem(DataNodeType.PATHWAY);
		dsm.setSelectedItem(ref.getDataSource());
	}

	/**
	 * Search for symbols or ids in the synonym databases that match
	 * the given text
	 */
	private void search(String aText)
	{
		if(aText == null || "".equals(aText.trim())) {
			JOptionPane.showMessageDialog(this, "No search term specified, " +
			"please type something in the 'Search' field");
			return;
		}
		final String text = aText.trim();

		final ProgressKeeper progress = new ProgressKeeper();
		ProgressDialog dialog = new ProgressDialog(this, "Searching", progress, true, true);
		dialog.setLocationRelativeTo(this);

		SwingWorker<List<XrefWithSymbol>, Void> sw = new SwingWorker<List<XrefWithSymbol>, Void>() {
			private static final int QUERY_LIMIT = 200;

			protected List<XrefWithSymbol> doInBackground() throws IDMapperException
			{
				IDMapperStack gdb = swingEngine.getGdbManager().getCurrentGdb();

			    //The result set
				List<XrefWithSymbol> result = new ArrayList<XrefWithSymbol>();
	    		
	    		for (Map.Entry<Xref, String> i :
		    		gdb.freeAttributeSearch( text, AttributeMapper.MATCH_ID, QUERY_LIMIT).entrySet())
		    	{
	    			// GO terms are annotated as symbols in BridgeDb databases
		    		// those are filtered from the results
		    		if(!i.getKey().getDataSource().getType().equals("ontology") ||
		    				!i.getKey().getDataSource().getType().equals("probe")) {
		    			result.add (new XrefWithSymbol (i.getKey(), i.getValue()));
		    		}
		    	}
		    	for (Map.Entry<Xref, String> i :
		    		gdb.freeAttributeSearch( text, "Symbol", QUERY_LIMIT).entrySet())
		    	{
		    		// GO terms are annotated as symbols in BridgeDb databases
		    		// those are filtered from the results
		    		if(!i.getKey().getDataSource().getType().equals("ontology") &&
		    				!i.getKey().getDataSource().getType().equals("probe")) {
		    			result.add (new XrefWithSymbol (i.getKey(), i.getValue()));	
		    		}
		    		
		    	}
				return result;
			}

			@Override
			public void done()
			{
				progress.finished();
				if (!progress.isCancelled())
				{
					List<XrefWithSymbol> results = null;
					try
					{
						results = get();
						//Show results to user
						if(results != null && results.size() > 0) {
							DatabaseSearchDialog resultDialog = new DatabaseSearchDialog("Results", results, curDlg);
							resultDialog.setVisible(true);
							XrefWithSymbol selected = resultDialog.getSelected();
							if(selected != null) {
								applyAutoFill(selected);
							}
						} else {
							JOptionPane.showMessageDialog(DataNodeDialog.this,
									"No results for '" + text + "'");
						}
					} catch (InterruptedException e) {
						//Ignore, thread interrupted. Same as cancel.
					}
					catch (ExecutionException e) {
						if(swingEngine.getGdbManager().getCurrentGdb().getMappers().size() == 0) {
							JOptionPane.showMessageDialog(DataNodeDialog.this,
									"No identifier mapping database loaded.", "Error",
									JOptionPane.ERROR_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(DataNodeDialog.this,
									"Exception occurred while searching,\n" +
									"see error log for details.", "Error",
									JOptionPane.ERROR_MESSAGE);
							Logger.log.error("Error while searching", e);
						}
					}
				}
			}
		};
		sw.execute();
		dialog.setVisible(true);
	}

	protected void addCustomTabs(JTabbedPane parent) {
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

		//Search panel elements
		searchPanel.setLayout(new GridBagLayout());

		final JTextField searchText = new JTextField();
		final JButton searchButton = new JButton("Search");

		//Key listener to search when user presses Enter
		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchButton.requestFocus();
					search(searchText.getText());
				}
			}
		});

		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				search(searchText.getText());
			}
		});
		searchButton.setToolTipText("Search the synonym database for references, based on the text label");

		GridBagConstraints searchConstraints = new GridBagConstraints();
		searchConstraints.gridx = GridBagConstraints.RELATIVE;
		searchConstraints.fill = GridBagConstraints.HORIZONTAL;
		searchConstraints.weightx = 1;
		searchPanel.add(searchText, searchConstraints);

		searchConstraints.weightx = 0;
		searchPanel.add(searchButton, searchConstraints);

		//Manual entry panel elements
		fieldPanel.setLayout(new GridBagLayout());

		JLabel symLabel = new JLabel("Text label");
		JLabel idLabel = new JLabel("Identifier");
		JLabel dbLabel = new JLabel("Database");
		JLabel typeLabel = new JLabel ("Biological Type");
		symText = new CompleterQueryTextArea(new OptionProvider() {
			public List<String> provideOptions(String text) {
				if(text == null) return Collections.emptyList();

				IDMapperStack gdb = swingEngine.getGdbManager().getCurrentGdb();
				List<String> symbols = new ArrayList<String>();
				try
				{
					if(gdb.getMappers().size() > 0) {
						symbols.addAll
							(gdb.freeAttributeSearch(text, "Symbol", 10).values());
					}
				}
				catch (IDMapperException ignore) {}
				return symbols;
			}
		}, true);
		symText.setColumns(20);
		symText.setRows(2);
		idText = new CompleterQueryTextField(new OptionProvider() {
			public List<String> provideOptions(String text) {
				if(text == null) return Collections.emptyList();

				IDMapperStack gdb = swingEngine.getGdbManager().getCurrentGdb();
				Set<Xref> refs = new HashSet<Xref>();
				try
				{
					if(gdb.getMappers().size() > 0) refs = gdb.freeSearch(text, 100);
				}
				catch (IDMapperException ignore) {}

				//Only take identifiers
				List<String> ids = new ArrayList<String>();
				for (Xref ref : refs) ids.add(ref.getId());
				return ids;
			}
		}, true);
		symText.setCorrectCase(false);
		idText.setCorrectCase(false);

		dsm = new DataSourceModel();
		dsm.setPrimaryFilter(true);
		dsm.setSpeciesFilter(swingEngine.getCurrentOrganism());
		dbCombo = new PermissiveComboBox(dsm);
		typeCombo = new PermissiveComboBox(DataNodeType.getValues());

		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = c.ipady = 5;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		fieldPanel.add(symLabel, c);
		fieldPanel.add(typeLabel, c);
		fieldPanel.add(idLabel, c);
		fieldPanel.add(dbLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		fieldPanel.add(new JScrollPane(symText), c);
		fieldPanel.add(typeCombo, c);
		fieldPanel.add(idText, c);
		fieldPanel.add(dbCombo, c);

		symText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { setText();	}
			public void insertUpdate(DocumentEvent e) {	setText(); }
			public void removeUpdate(DocumentEvent e) { setText(); }
			private void setText() {
				getInput().setTextLabel(symText.getText());
			}
		});

		idText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { setText();	}
			public void insertUpdate(DocumentEvent e) {	setText(); }
			public void removeUpdate(DocumentEvent e) { setText(); }
			private void setText() {
				getInput().setElementID(idText.getText());
			}
		});

		dsm.addListDataListener(new ListDataListener()
		{

			public void contentsChanged(ListDataEvent arg0)
			{
				getInput().setDataSource((DataSource)dsm.getSelectedItem());

			}

			public void intervalAdded(ListDataEvent arg0) {	}

			public void intervalRemoved(ListDataEvent arg0) { }
		});

		typeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataNodeType item = (DataNodeType)typeCombo.getSelectedItem();
				getInput().setDataNodeType(item);
				refresh();
			}
		});

		symText.setEnabled(!readonly);
		idText.setEnabled(!readonly);
		dbCombo.setEnabled(!readonly);
		typeCombo.setEnabled(!readonly);

		parent.add("Annotation", panel);
		parent.setSelectedComponent(panel);
	}
}
