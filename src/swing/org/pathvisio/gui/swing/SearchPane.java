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
package org.pathvisio.gui.swing;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.Engine;
import org.pathvisio.data.XrefWithSymbol;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.swing.MatchResult;
import org.pathvisio.util.swing.SearchMethods;
import org.pathvisio.util.swing.SearchMethods.ByPatternMatcher;
import org.pathvisio.util.swing.SearchMethods.ByXrefMatcher;
import org.pathvisio.util.swing.SearchMethods.SearchException;
import org.pathvisio.util.swing.SearchTableModel;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;

/**
 * A side panel which displays search results.
 */
public class SearchPane extends JPanel 
{	
	private JButton btnSearch;
	private JButton btnBrowse;
	private JTextField txtDir;
	private JTextField txtId;
	private JComboBox cbSyscode;
	private JTextField txtSymbol;
	private JCheckBox chkHighlight;
	private JTable tblResult;
	private JLabel lblNumFound;
	final private JComboBox cbSearchBy;
	private SearchTableModel srs;
	
	private Engine engine;
	private SwingEngine swingEngine;
	
	public SearchPane(SwingEngine swingEngine)
	{
		this.engine = swingEngine.getEngine();
		this.swingEngine = swingEngine;
		
		txtSymbol = new JTextField();
		txtSymbol.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent ae) 
			{
				doSearch();
			}
		});

		DefaultFormBuilder symbolOptBuilder = new DefaultFormBuilder(
				new FormLayout("pref, 4dlu, fill:pref:grow")
		);
		symbolOptBuilder.append("Symbol:", txtSymbol);
		JPanel symbolOpt = symbolOptBuilder.getPanel();
		
		txtId = new JTextField();
		txtId.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent ae) 
			{
				doSearch();
			}
		});
		cbSyscode = new JComboBox(new DataSourceModel());
		
		DefaultFormBuilder idOptBuilder = new DefaultFormBuilder(
			new FormLayout("pref, 4dlu, fill:pref:grow")
		);
		idOptBuilder.append("Id:", txtId);
		idOptBuilder.append("System Code:", cbSyscode);
		JPanel idOpt = idOptBuilder.getPanel();
		
		final JPanel opts = new JPanel();
		final CardLayout optCards = new CardLayout();
		opts.setLayout (optCards);
		opts.add (symbolOpt, "SYMBOL");
		opts.add (idOpt, "ID");
		
		JPanel searchOptBox = new JPanel();
		FormLayout layout = new FormLayout(
				"4dlu, pref, 4dlu, fill:pref:grow, 4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");
		CellConstraints cc = new CellConstraints();
		
		searchOptBox.setLayout (layout);
		Border etch = BorderFactory.createEtchedBorder();
		searchOptBox.setBorder (BorderFactory.createTitledBorder (etch, "Search options"));
		
		searchOptBox.add (new JLabel ("Search by:"), cc.xy (2,2));
		
		cbSearchBy = new JComboBox();
		cbSearchBy.addItem ("Symbol");
		cbSearchBy.addItem ("ID");
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
		searchOptBox.add (cbSearchBy, cc.xyw (4,2,3));
		searchOptBox.add (opts, cc.xyw(2,4,5));
		
		searchOptBox.add (new JLabel("Directory to search:"), cc.xy (2,6));
		txtDir = new JTextField();
		txtDir.setText (PreferenceManager.getCurrent().get(GlobalPreference.DIR_LAST_USED_SEARCHPANE));
		searchOptBox.add (txtDir, cc.xyw(2,8,3));
		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener()
		{
			public void actionPerformed (ActionEvent ae)
			{
				doBrowse();
			}
		});
		searchOptBox.add (btnBrowse, cc.xy(6,8));
		btnSearch = new JButton("Search");
		searchOptBox.add (btnSearch, cc.xyw(2,10,5, "center, top"));
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
		chkHighlight.setSelected(true);
		chkHighlight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chkHighlight.isSelected()) {
					int row = tblResult.getSelectedRow();
					if(row >= 0) {
						MatchResult mr = srs.getRow(row);
						highlightResults(mr);
					}
				} else {
					removeHighlight();
				}
			}
		});
		
		tblResult = new JTable();
		tblResult.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		tblResult.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent me) 
			{
				showSelectedPwy();
			}

		});
		tblResult.getTableHeader().setVisible(true);
		Box box6 = Box.createHorizontalBox();
		box6.add (chkHighlight);
		box6.add (new JLabel ("Highlight matches"));
		resultPanel.add (box6, BorderLayout.NORTH);
		resultPanel.add (new JScrollPane(tblResult), BorderLayout.CENTER);
		lblNumFound = new JLabel();
		resultPanel.add (lblNumFound, BorderLayout.SOUTH);
		
		setLayout (new BorderLayout());
		add (searchOptBox, BorderLayout.NORTH);
		add (resultPanel, BorderLayout.CENTER);		
	}
	
	private void showSelectedPwy()
	{
		int row = tblResult.getSelectedRow();
		final MatchResult mr = srs.getRow(row);
		
		//TODO: here I want to use SwingEngine.openPathway, but I need to 
		// be able to wait until the process is finished!
		final ProgressKeeper pk = new ProgressKeeper();
		
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(swingEngine.getApplicationPanel()), 
				"", pk, false, true);
				
		SwingWorker<Boolean, Boolean> sw = new SwingWorker<Boolean, Boolean>() {
			protected Boolean doInBackground() throws Exception {
				pk.setTaskName("Opening pathway");
				try {
					engine.setWrapper (swingEngine.createWrapper());
					engine.openPathway(mr.getFile());
					
					if(chkHighlight.isSelected()) {
						highlightResults(mr);
					}

					return true;
				} catch(ConverterException e) {
					swingEngine.handleConverterException(e.getMessage(), null, e);
					return false;
				} finally {
					pk.finished();
				}
			}
		};
		
		swingEngine.processTask(pk, d, sw);
	}

	private void highlightResults(MatchResult mr) {
		Rectangle2D interestingRect = null;

		VPathway vpy = engine.getActiveVPathway();
		for (VPathwayElement velt : vpy.getDrawingObjects())
		{
			if (velt instanceof GeneProduct)
			{
				GeneProduct gp = (GeneProduct)velt;
				for (XrefWithSymbol id : mr.getMatches())
				{
					XrefWithSymbol ref = new XrefWithSymbol(
							gp.getPathwayElement().getXref(), 
							gp.getPathwayElement().getTextLabel()); 
					if (id.equals(ref))
					{
						gp.highlight();
						Logger.log.info ("Highlighted " + ref);
						//scroll to first item found
						if (interestingRect == null)
						{
							interestingRect = gp.getVBounds();
						}
						break;
					}
				}
			}
		}
		if (interestingRect != null)
			vpy.getWrapper().scrollTo (interestingRect.getBounds());
		vpy.redrawDirtyRect();
	}

	private void removeHighlight() {
		VPathway vpy = engine.getActiveVPathway();
		if(vpy != null) {
			vpy.resetHighlight();
		}
	}
	
	/**
	 * Invoked when you hit the search button
	 */
	private void doSearch()
	{
		lblNumFound.setText("");
		int i = cbSearchBy.getSelectedIndex();
		try
		{
			if (i == 0)
			{
				pathwaysContainingGeneSymbol (
						txtSymbol.getText(), 
						new File (txtDir.getText()), 
						tblResult, 
						getTopLevelAncestor());
				
			}
			else
			{
				pathwaysContainingGeneID(
						new Xref (txtId.getText(), DataSource.getByFullName("" + cbSyscode.getSelectedItem())), 
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
	 * Search for pathways containing the given gene and display result in given result table
	 * @param id	Gene identifier to search for
	 * @param code	System code of the gene identifier
	 * @param folder	Directory to search (includes sub-directories)
	 * @param srt	to display the results in
	 * @param pmon containing the monitor responsible for
	 * displaying the progress
	 */
	public void pathwaysContainingGeneID (Xref ref, File folder, 
			JTable srt, Component parent) 
			throws SearchException
	{
		srs = new SearchTableModel ();
		srt.setModel(srs);
		srs.setColumns (new SearchTableModel.Column[] {
				SearchTableModel.Column.PATHWAY_NAME, 
				SearchTableModel.Column.DIRECTORY
				});
		SearchMethods.searchHelper (
				new ByXrefMatcher (swingEngine.getGdbManager().getCurrentGdb(), ref), 
				folder, srs, lblNumFound, parent);
	}

	/**
	 * Search for pathways containing a symbol that matches the given regex
	 * and display result in given result table
	 * @param id	Gene identifier to search for
	 * @param code	System code of the gene identifier
	 * @param folder	Directory to search (includes sub-directories)
	 * @param srt	to display the results in
	 * @param pmon containing the monitor responsible for
	 * displaying the progress
	 */
	public void pathwaysContainingGeneSymbol (
			String regex, File folder, 
			JTable srt, Component parent) 
	{
		srs = new SearchTableModel ();
		srt.setModel(srs);
		srs.setColumns (new SearchTableModel.Column[] {
				SearchTableModel.Column.PATHWAY_NAME, 
				SearchTableModel.Column.DIRECTORY,
				SearchTableModel.Column.NAMES
				});
		SearchMethods.searchHelper (new ByPatternMatcher (regex), folder, srs, lblNumFound, parent);
	}

	/**
	 * Invoked when you hit the browse button
	 */
	private void doBrowse()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(new File(txtDir.getText()));
		int result = fc.showDialog(getTopLevelAncestor(), "Select");
		if (result == JFileChooser.APPROVE_OPTION)
		{
			txtDir.setText("" + fc.getSelectedFile());
			PreferenceManager.getCurrent().setFile(GlobalPreference.DIR_LAST_USED_SEARCHPANE, fc.getCurrentDirectory());
		}
	}
	
}
