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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.progress.ProgressDialog;
import org.pathvisio.gui.swing.progress.SwingProgressKeeper;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.model.XrefWithSymbol;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.swing.MatchResult;
import org.pathvisio.util.swing.SearchMethods;
import org.pathvisio.util.swing.SearchTableModel;
import org.pathvisio.util.swing.SearchMethods.ByPatternMatcher;
import org.pathvisio.util.swing.SearchMethods.ByXrefMatcher;
import org.pathvisio.util.swing.SearchMethods.SearchException;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
	private SearchTableModel srs;
	
	public SearchPane()
	{	
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
		cbSyscode = new JComboBox();
		for (DataSource d : DataSource.getDataSources())
		{
			cbSyscode.addItem(d.getFullName());
		}
		
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
		txtDir.setText (Engine.getCurrent().getPreferences().get(GlobalPreference.DIR_PWFILES));
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
		box6.add (new JLabel ("Highlight found genes"));
		resultPanel.add (box6, BorderLayout.NORTH);
		resultPanel.add (new JScrollPane(tblResult), BorderLayout.CENTER);
		
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
		final SwingProgressKeeper pk = new SwingProgressKeeper(ProgressKeeper.PROGRESS_UNKNOWN);
		
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(SwingEngine.getCurrent().getApplicationPanel()), 
				"", pk, false, true);
				
		SwingWorker<Boolean, Boolean> sw = new SwingWorker<Boolean, Boolean>() {
			protected Boolean doInBackground() throws Exception {
				pk.setTaskName("Opening pathway");
				try {
					Engine.getCurrent().setWrapper (SwingEngine.getCurrent().createWrapper());
					Engine.getCurrent().openPathway(mr.getFile());
					
					
					Rectangle2D interestingRect = null;
					
					if (chkHighlight.isSelected())
					{
						VPathway vpy = Engine.getCurrent().getActiveVPathway();
						for (VPathwayElement velt : vpy.getDrawingObjects())
						{
							if (velt instanceof GeneProduct)
							{
								GeneProduct gp = (GeneProduct)velt;
								for (Xref id : mr.getMatches())
								{
									if (id.equals(gp.getPathwayElement().getXref()))
									{
										gp.highlight();
										Logger.log.info ("Highlighted " + gp.getPathwayElement().getXref());
										if (interestingRect == null)
										{
											interestingRect = gp.getVBounds();
										}
										else
										{
											interestingRect.add(gp.getVBounds());
										}
										break;
									}
								}
							}
						}
						vpy.getWrapper().scrollTo (interestingRect.getBounds());
					}

					return true;
				} catch(ConverterException e) {
					SwingEngine.getCurrent().handleConverterException(e.getMessage(), null, e);
					return false;
				} finally {
					pk.finished();
				}
			}
		};
		
		SwingEngine.getCurrent().processTask(pk, d, sw);

		
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
				pathwaysContainingGeneSymbol (
						txtSymbol.getText(), 
						new File (txtDir.getText()), 
						tblResult, 
						getTopLevelAncestor());
				
			}
			else
			{
				pathwaysContainingGeneID(
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
		SearchMethods.searchHelper (new ByXrefMatcher (ref), folder, srs, parent);
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
		SearchMethods.searchHelper (new ByPatternMatcher (regex), folder, srs, parent);
	}

	/**
	 * Invoked when you hit the browse button
	 */
	private void doBrowse()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(Engine.getCurrent().getPreferences().getFile(GlobalPreference.DIR_PWFILES));
		int result = fc.showDialog(getTopLevelAncestor(), "Select");
		if (result == JFileChooser.APPROVE_OPTION)
		{
			txtDir.setText("" + fc.getSelectedFile());
		}
	}
	
}
