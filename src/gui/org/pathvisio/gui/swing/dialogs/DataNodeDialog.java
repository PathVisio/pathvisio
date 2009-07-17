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
package org.pathvisio.gui.swing.dialogs;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.rdb.IDMapperRdb;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.data.XrefWithSymbol;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.DataSourceModel;
import org.pathvisio.gui.swing.ProgressDialog;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.gui.swing.completer.CompleterQueryTextField;
import org.pathvisio.gui.swing.completer.OptionProvider;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.swing.PermissiveComboBox;

/**
 * Dialog for editing DataNodes. In addition to the standard comments and literature tabs,
 * this has a tab for looking up accession numbers for genes and metabolites.
 */
public class DataNodeDialog extends PathwayElementDialog {

	protected DataNodeDialog(SwingEngine swingEngine, PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		super(swingEngine, e, readonly, frame, "DataNode properties", locationComp);
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();
	}

	CompleterQueryTextField symText;
	CompleterQueryTextField idText;
	private PermissiveComboBox dbCombo;
	private DataSourceModel dsm;	

	public void refresh() {
		super.refresh();
		symText.setText(getInput().getTextLabel());
		idText.setText(getInput().getGeneID());
//		if(input.getDataSource() != null) {
			dsm.setSelectedItem(input.getDataSource());
//		} else {
//			dsm.setSelectedIndex(-1);
//		}
		pack();
	}

	private void applyAutoFill(XrefWithSymbol ref) 
	{
		String sym = ref.getSymbol();
		if (sym == null || sym.equals ("")) sym = ref.getId();
		symText.setText(sym);
		idText.setText(ref.getId());
		dsm.setSelectedItem(ref.getDataSource());
	}

	/**
	 * Search for symbols or ids in the synonym databases that match
	 * the given text
	 */
	private void search(final String text) {
		if(text == null || "".equals(text)) {
			JOptionPane.showMessageDialog(this, "No search term specified, " +
			"please type something in the 'Search' field");
			return;
		}

		final ProgressKeeper progress = new ProgressKeeper();
		ProgressDialog dialog = new ProgressDialog(null, "Searching", progress, true, true);
		dialog.setLocationRelativeTo(this);

		SwingWorker<List<XrefWithSymbol>, Void> sw = new SwingWorker<List<XrefWithSymbol>, Void>() {
			private static final int QUERY_LIMIT = 200;
			
			protected List<XrefWithSymbol> doInBackground() throws IDMapperException
			{
				IDMapperRdb gdb = swingEngine.getGdbManager().getCurrentGdb();

			    //The result set
				List<XrefWithSymbol> result = new ArrayList<XrefWithSymbol>(); 
			    
		    	Set<Xref> tempset = new HashSet<Xref>();
		    	tempset.addAll( gdb.freeSearch( text, QUERY_LIMIT ) );
		    	tempset.addAll( gdb.freeAttributeSearch( text, "Symbol", QUERY_LIMIT ) );
		    	for (Xref x : tempset)
		    	{
		    		for (String s : gdb.getAttributes (x, "Symbol"))
		    		{
		    			result.add (new XrefWithSymbol (x, s));
			    		break; // only put the first symbol found
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
							DatabaseSearchDialog resultDialog = new DatabaseSearchDialog("Results", results);
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
						JOptionPane.showMessageDialog(DataNodeDialog.this, 
								"Exception occurred while searching,\n" +
								"see error log for details.", "Error", 
								JOptionPane.ERROR_MESSAGE);
						Logger.log.error("Error while searching", e);
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

		symText = new CompleterQueryTextField(new OptionProvider() {
			public List<String> provideOptions(String text) {
				if(text == null) return Collections.emptyList();

				IDMapperRdb gdb = swingEngine.getGdbManager().getCurrentGdb();
				List<String> symbols = new ArrayList<String>();
				try
				{
					for (Xref ref : gdb.freeAttributeSearch(text, "Symbol", 10))
					{
						symbols.addAll (gdb.getAttributes(ref, "Symbol"));
					}
				}
				catch (IDMapperException ignore) {}
				return symbols;
			}
		}, true);

		idText = new CompleterQueryTextField(new OptionProvider() {
			public List<String> provideOptions(String text) {
				if(text == null) return Collections.emptyList();

				IDMapperRdb gdb = swingEngine.getGdbManager().getCurrentGdb();
				Set<Xref> refs = new HashSet<Xref>();
				try
				{
					refs = gdb.freeSearch(text, 100);
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

		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = c.ipady = 5;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		fieldPanel.add(symLabel, c);
		fieldPanel.add(idLabel, c);
		fieldPanel.add(dbLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		fieldPanel.add(symText, c);
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
				getInput().setGeneID(idText.getText());
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
		
//		dbCombo.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				DataSource item = (DataSource)dbCombo.getSelectedItem();
//				getInput().setDataSource(item);
//			}
//		});

		symText.setEnabled(!readonly);
		idText.setEnabled(!readonly);
		dbCombo.setEnabled(!readonly);

		parent.add("Annotation", panel);
		parent.setSelectedComponent(panel);
	}
}
