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
package org.pathvisio.gui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.border.Border;

import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.preferences.swing.SwingPreferences.SwingPreference;
import org.pathvisio.util.swing.SearchMethods;

/**
 * A side panel which displays search results.
 */
public class SearchPane extends JPanel 
{	
	private static final long serialVersionUID = 1L;
	private JButton btnSearch;
	private JButton btnBrowse;
	private JTextField txtDir;
	private JTextField txtId;
	private JComboBox cbSyscode;
	private JTextField txtSymbol;
	private JCheckBox chkHighlight;
	private JTable tblResult;
	final private JComboBox cbSearchBy;

	public SearchPane()
	{		
		Box symbolOpt = Box.createVerticalBox();
		Box box2 = Box.createHorizontalBox();
		box2.add (new JLabel("Gene Symbol:"));
		txtSymbol = new JTextField();
		box2.add (txtSymbol);
		symbolOpt.add (box2);
		
		Box idOpt = Box.createVerticalBox();
		Box box4 = Box.createHorizontalBox();
		box4.add (new JLabel("Gene Id:"));
		txtId = new JTextField();
		box4.add (txtId);
		Box box5 = Box.createHorizontalBox();
		box5.add (new JLabel ("System Code:"));
		cbSyscode = new JComboBox();
		for (DataSource d : DataSource.getDataSources())
		{
			cbSyscode.addItem(d.getFullName());
		}
		box5.add (cbSyscode);
		idOpt.add (box4);
		idOpt.add (box5);

		final JPanel opts = new JPanel();
		final CardLayout optCards = new CardLayout();
		opts.setLayout (optCards);
		opts.add (symbolOpt, "SYMBOL");
		opts.add (idOpt, "ID");
		
		JPanel searchOptBox = new JPanel();
		searchOptBox.setLayout (new BoxLayout(searchOptBox, BoxLayout.PAGE_AXIS));
		Border etch = BorderFactory.createEtchedBorder();
		searchOptBox.setBorder (BorderFactory.createTitledBorder (etch, "Search options"));
		Box box1 = Box.createHorizontalBox();
		box1.add (new JLabel ("Search by"));
		cbSearchBy = new JComboBox();
		cbSearchBy.addItem ("Gene Symbol");
		cbSearchBy.addItem ("Gene ID");
		cbSearchBy.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae) 
			{
				int i = cbSearchBy.getSelectedIndex();
				if (i == 0)
				{
					optCards.show(opts, "SYMBOL");
				}
				else
				{
					optCards.show(opts, "ID");
				}
				
			}
		}
		);
		box1.add (cbSearchBy);
		searchOptBox.add (box1);
		searchOptBox.add (opts);
		Box box3 = Box.createHorizontalBox();
		box3.add (new JLabel("Directory to search:"));
		txtDir = new JTextField();
		txtDir.setText (SwingPreference.SWING_DIR_PWFILES.getValue());
		box3.add (txtDir);
		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener()
		{
			public void actionPerformed (ActionEvent ae)
			{
				doBrowse();
			}
		});
		box3.add (btnBrowse);
		searchOptBox.add (box3);
		btnSearch = new JButton("Search");
		searchOptBox.add (btnSearch);
		btnSearch.addActionListener(new ActionListener()
		{
			public void actionPerformed (ActionEvent ae)
			{
				doSearch();
			}
		});

		JPanel resultPanel = new JPanel();
		resultPanel.setBorder (BorderFactory.createTitledBorder(etch, "Results"));
		resultPanel.setLayout (new BorderLayout());
		chkHighlight = new JCheckBox();
		tblResult = new JTable();
		tblResult.getTableHeader().setVisible(true);
		Box box6 = Box.createHorizontalBox();
		box6.add (chkHighlight);
		box6.add (new JLabel ("Highlight found genes"));
		resultPanel.add (box6, BorderLayout.NORTH);
		resultPanel.add (new JScrollPane(tblResult), BorderLayout.CENTER);
		
		setLayout (new BorderLayout());
		add (searchOptBox, BorderLayout.NORTH);
		add (resultPanel, BorderLayout.CENTER);		
	}
	
	/**
	 * Invoked when you hit the search button
	 */
	private void doSearch()
	{
		int i = cbSearchBy.getSelectedIndex();
		try
		{
			if (i == 0)
			{
				SearchMethods.pathwaysContainingGeneSymbol (
						txtSymbol.getText(), 
						new File (txtDir.getText()), 
						tblResult, 
						getTopLevelAncestor());
				
			}
			else
			{
				SearchMethods.pathwaysContainingGeneID(
						new Xref (txtId.getName(), DataSource.getByFullName("" + cbSyscode.getSelectedItem())), 
						new File (txtDir.getText()), 
						tblResult, 
						getTopLevelAncestor());
			}
		}
		catch (SearchMethods.SearchException e)
		{
			JOptionPane.showMessageDialog(
					getTopLevelAncestor(), 
					e.getMessage(), 
					"Search warning", 
					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * Invoked when you hit the browse button
	 */
	private void doBrowse()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int result = fc.showDialog(getTopLevelAncestor(), "Select");
		if (result == JFileChooser.APPROVE_OPTION)
		{
			txtDir.setText("" + fc.getSelectedFile());
		}
	}
	
}
