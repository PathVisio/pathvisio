// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.data.DataSourcePatterns;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.GdbManager;
import org.pathvisio.gui.swing.completer.CompleterQueryTextField;
import org.pathvisio.gui.swing.completer.OptionProvider;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;


public class DataNodeDialog extends PathwayElementDialog {
	private static final long serialVersionUID = 1L;
	
	public DataNodeDialog(PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		super(e, readonly, frame, "DataNode properties", locationComp);
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();
	}

	CompleterQueryTextField symText;
	JTextField idText;
	JComboBox dbCombo;
			
	public void refresh() {
		super.refresh();
		symText.setText(getInput().getTextLabel());
		idText.setText(getInput().getGeneID());
		if(input.getDataSource() != null) {
			dbCombo.setSelectedItem(input.getDataSource());
		} else {
			dbCombo.setSelectedIndex(-1);
		}
		pack();
	}
	
	/**
	 * Auto fill all fields based on the given information.
	 * If an id is provided, it will be used to find the symbol and datasource.
	 * If a symbol is provided, it will be used to find the id and datasource.
	 * If id and symbol are provided, the id will be used to update the symbol.
	 * @param id
	 * @param symbol
	 * @param datasource
	 */
	private void autoFill(String id, String symbol, DataSource datasource) {
		Set<Xref> options = new HashSet<Xref>();
					
		Gdb gdb = GdbManager.getCurrentGdb();
		
		//Try to use filled in identifier first
		if(!"".equals(id)) {
			Xref fillXref = null;
			if(datasource != null) {
				//Check if the current id/datasource exists
				fillXref = new Xref(id, datasource);
				if(gdb.xrefExists(fillXref)) {
					options.add(fillXref);
				} else {
					fillXref = null;
				}
			}
			if(fillXref == null) {
				//Filled in ref doesn't exist, try to find correct datasource
				for(DataSource d : DataSource.getDataSources()) {
					Pattern p = DataSourcePatterns.getPatterns().get(d);
					if(p != null && p.matcher(id).matches()) {
						options.add(new Xref(id, d));
					}
				}
			} else {
				options.add(fillXref);
			}
		} else {
			//Fall back on symbol
			List<Xref> refs = gdb.getCrossRefsByAttribute("Symbol", symbol);
			//Filter by datasource if needed
			if(datasource != null) {
				for(Xref r : refs) {
					//Only add if xref has the selected datasource
					if(datasource.equals(r.getDataSource())) {
						options.add(r);
					}
				}
			}
			if(options.size() == 0) { 
				//If there are no results for the selected datasource, use other results
				options.addAll(refs);
			}
		}
		
		//Process the options
		if(options.size() == 0) {
			//Notify the user that at least the symbol or id needs to be filled in
			JOptionPane.showMessageDialog(
					getRootPane(), 
					"Could not find any symbols or identifiers based on the available information", 
					"No matches", JOptionPane.INFORMATION_MESSAGE);
		} else if(options.size() == 1) {
			//Only one option, fill in the empty fields
			applyAutoFill(options.iterator().next());
		} else {
			//Multiple options, show dialog and let user choose
			//TODO
			applyAutoFill(options.iterator().next());
		}
	}
	
	private void applyAutoFill(Xref ref) {
		Gdb gdb = GdbManager.getCurrentGdb();
		String sym = gdb.getGeneSymbol(ref);
		symText.setText(sym);
		idText.setText(ref.getId());
		dbCombo.setSelectedItem(ref.getDataSource());
	}
	
	protected void addCustomTabs(JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		JLabel symLabel = new JLabel("Symbol");
		JLabel idLabel = new JLabel("Identifier");
		JLabel dbLabel = new JLabel("Database");
		
		final JButton fillSymbol = new JButton("Lookup identifier");
		fillSymbol.setToolTipText(
				"Fill in the identifier and database " +
				"fields based on the given symbol");
		final JButton fillId = new JButton("Lookup symbol");
		fillSymbol.setToolTipText(
				"Fill in the symbol and database " +
				"fields based on the given identifier");
		fillSymbol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				autoFill("", symText.getText(), (DataSource)dbCombo.getSelectedItem());
			}
		});
		fillId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				autoFill(idText.getText(), "", (DataSource)dbCombo.getSelectedItem());
			}
		});
		
		symText = new CompleterQueryTextField(new OptionProvider() {
			public Object[] provideOptions(String text) {
				Gdb gdb = GdbManager.getCurrentGdb();
				List<String> symbols = gdb.getSymbolSuggestions(text, 100);
				return symbols.toArray();
			}
		}, true);
		
		idText = new CompleterQueryTextField(new OptionProvider() {
			public Object[] provideOptions(String text) {
				Gdb gdb = GdbManager.getCurrentGdb();
				List<Xref> refs = gdb.getIdSuggestions(text, 100);
				//Only take identifiers
				String[] ids = new String[refs.size()];
				for(int i = 0; i < refs.size(); i++) {
					ids[i] = refs.get(i).getId();
				}
				return ids;
			}
		}, true);
		
		DocumentListener autofillListener = new DocumentListener() {
			public void refresh() {
				fillSymbol.setEnabled(symText.getText() != null && !symText.getText().equals(""));
				fillId.setEnabled(idText.getText() != null && !idText.getText().equals(""));
			}
			public void changedUpdate(DocumentEvent e) {
				refresh();
			}
			public void insertUpdate(DocumentEvent e) {
				refresh();
			}
			public void removeUpdate(DocumentEvent e) {
				refresh();
			}
		};
		
		symText.getDocument().addDocumentListener(autofillListener);
		idText.getDocument().addDocumentListener(autofillListener);
		
		dbCombo = new JComboBox(DataSource.getDataSources().toArray());
		
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = c.ipady = 5;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		panel.add(symLabel, c);
		panel.add(idLabel, c);
		panel.add(dbLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(symText, c);
		panel.add(idText, c);
		panel.add(dbCombo, c);
		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		panel.add(fillSymbol, c);
		panel.add(fillId, c);

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
		
		dbCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataSource item = (DataSource)dbCombo.getSelectedItem();
				getInput().setDataSource(item);
			}
		});
		
		symText.setEnabled(!readonly);
		idText.setEnabled(!readonly);
		dbCombo.setEnabled(!readonly);
		
		parent.add("Annotation", panel);
		parent.setSelectedComponent(panel);
	}
}
