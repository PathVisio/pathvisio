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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.data.Gdb;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.gui.swing.completer.CompleterQueryTextField;
import org.pathvisio.gui.swing.completer.OptionProvider;
import org.pathvisio.gui.swing.progress.ProgressDialog;
import org.pathvisio.gui.swing.progress.SwingProgressKeeper;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;
import org.pathvisio.model.XrefWithSymbol;
import org.pathvisio.util.RunnableWithProgress;


public class DataNodeDialog extends PathwayElementDialog {

	private final SwingEngine swingEngine;
	
	protected DataNodeDialog(SwingEngine swingEngine, PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		super(swingEngine, e, readonly, frame, "DataNode properties", locationComp);
		this.swingEngine = swingEngine;
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();
	}

	CompleterQueryTextField symText;
	CompleterQueryTextField idText;
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

	private void applyAutoFill(XrefWithSymbol ref) 
	{
		String sym = ref.getSymbol();
		symText.setText(sym);
		idText.setText(ref.getId());
		dbCombo.setSelectedItem(ref.getDataSource());
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

		final SwingProgressKeeper progress = new SwingProgressKeeper(SwingProgressKeeper.PROGRESS_UNKNOWN);
		ProgressDialog dialog = new ProgressDialog(null, "Searching", progress, true, true);
		dialog.setLocationRelativeTo(this);

		final RunnableWithProgress<List<XrefWithSymbol>> task = new RunnableWithProgress<List<XrefWithSymbol>>() {
			public List<XrefWithSymbol> excecuteCode() 
			{
				final int QUERY_LIMIT = 200;
				Gdb gdb = swingEngine.getGdbManager().getCurrentGdb();

				List<XrefWithSymbol> result = gdb.freeSearch(text, QUERY_LIMIT); 

				progress.finished();
				return result;
			}
		};

		SwingWorker<List<XrefWithSymbol>, Void> sw = new SwingWorker<List<XrefWithSymbol>, Void>() {
			protected List<XrefWithSymbol> doInBackground() throws Exception {
				task.run();
				task.getProgressKeeper().finished();
				return task.get();
			}
		};
		sw.execute();
		dialog.setVisible(true);

		List<XrefWithSymbol> results = null;
		try {
			results = sw.get();
		} catch (InterruptedException e) {
			//Ignore, cancel pressed
		} catch (ExecutionException e) {
			JOptionPane.showMessageDialog(this, "Error while searching: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			Logger.log.error("Error while searching", e);
		}

		//TODO: messy. Why not InterruptedException? Why not sw.isCancelled?
		if (!progress.isCancelled())
		{
			//Show results to user
			if(results.size() > 0) {
				DatabaseSearchDialog resultDialog = new DatabaseSearchDialog("Results", results);
				resultDialog.setVisible(true);
				XrefWithSymbol selected = resultDialog.getSelected();
				if(selected != null) {
					applyAutoFill(selected);
				}
			} else {
				JOptionPane.showMessageDialog(this, "No results for '" + text + "'");
			}
		}
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
			public Object[] provideOptions(String text) {
				if(text == null) return new Object[0];

				Gdb gdb = swingEngine.getGdbManager().getCurrentGdb();
				List<String> symbols = gdb.getSymbolSuggestions(text, 100);
				return symbols.toArray();
			}
		}, true);

		idText = new CompleterQueryTextField(new OptionProvider() {
			public Object[] provideOptions(String text) {
				if(text == null) return new Object[0];

				Gdb gdb = swingEngine.getGdbManager().getCurrentGdb();
				List<Xref> refs = gdb.getIdSuggestions(text, 100);
				//Only take identifiers
				String[] ids = new String[refs.size()];
				for(int i = 0; i < refs.size(); i++) {
					ids[i] = refs.get(i).getId();
				}
				return ids;
			}
		}, true);
		symText.setCorrectCase(false);
		idText.setCorrectCase(false);

		dbCombo = new JComboBox(DataSource.getDataSources().toArray());

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
