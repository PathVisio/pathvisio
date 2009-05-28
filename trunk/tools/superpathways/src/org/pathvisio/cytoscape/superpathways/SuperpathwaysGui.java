package org.pathvisio.cytoscape.superpathways;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import CyWikiPathwaysClientGui.ResultRow;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.bridgedb.bio.Organism;
import org.pathvisio.util.swing.ListWithPropertiesTableModel;
import org.pathvisio.util.swing.RowWithProperties;


import org.pathvisio.wikipathways.webservice.WSSearchResult;
import org.pathvisio.cytoscape.superpathways.ResultProperty;

import org.pathvisio.cytoscape.wikipathways.CyWikiPathwaysClient.FindPathwaysByTextParameters;
import org.pathvisio.cytoscape.wikipathways.CyWikiPathwaysClient.GetPathwayParameters;

//import org.pathvisio.cytoscape.superpathways.SuperpathwaysClient.FindPathwaysByTextParameters;
//import org.pathvisio.cytoscape.superpathways.SuperpathwaysClient.GetPathwayParameters;
import org.pathvisio.debug.Logger;

import cytoscape.Cytoscape;
import cytoscape.data.webservice.CyWebServiceEvent;
import cytoscape.data.webservice.CyWebServiceException;
import cytoscape.data.webservice.WebServiceClientManager;
import cytoscape.data.webservice.CyWebServiceEvent.WSEventType;
import cytoscape.util.CytoscapeAction;

public class SuperpathwaysGui extends JFrame implements ActionListener{ 

	final SuperpathwaysClient client;
	
	private static final int WINDOW_WIDTH=400;
	private static final int WINDOW_HEIGHT=500;
	private static String ACTION_SEARCH = "Search";
	//private static String ACTION_SEARCH = "Superpathways Search";
	private static String ORGANISM_ALL = "All organisms";
	
	
	JFrame window;
	JComboBox organismCombo;
	JTextField searchText;
	JTable resultTable;
	ListWithPropertiesTableModel<ResultProperty, ResultRow> tableModel;
	JScrollPane pane;
	
	public SuperpathwaysGui(SuperpathwaysClient c) {
	
		client = c;
		
		window =new JFrame ("Superpathways Plugin");
//		JPanel window = new JPanel();
//		this.window.setContentPane(window);
		window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		organismCombo= new JComboBox();
		resetOrganisms();  		
	
		searchText = new JTextField();
		searchText.setActionCommand(ACTION_SEARCH);
		searchText.addActionListener(this);
	
		JButton searchBtn = new JButton("Search");
		searchBtn.setActionCommand(ACTION_SEARCH);
		searchBtn.addActionListener(this);
	
		resultTable = new JTable();
		System.out.println ("Hi!" + resultTable);
		new Throwable().printStackTrace();
		
		//Added by jiaming
		tableModel =new ListWithPropertiesTableModel<ResultProperty, ResultRow>();	
		tableModel.setColumns(new ResultProperty[] {ResultProperty.NAME,ResultProperty.ORGANISM,});
		resultTable.setModel(tableModel);
		//End added by jiaming
		
		
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

		window.setLayout(new FormLayout(
				"4dlu, pref, 2dlu, fill:pref:grow, 4dlu, pref, 4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, fill:pref:grow, 4dlu"));
		CellConstraints cc = new CellConstraints();
		window.add(new JLabel("Search:"), cc.xy(2, 2));
		window.add(searchText, cc.xy(4, 2));
		window.add(organismCombo, cc.xy(6, 2));
		window.add(searchBtn, cc.xy(8, 2));
		
		pane = new JScrollPane (resultTable);
		window.add(resultTable, cc.xyw(2, 4, 7));
	
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
					new CyWebServiceEvent<FindPathwaysByTextParameters>(client.getClientID(),WSEventType.SEARCH_DATABASE,request)
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
				new CyWebServiceEvent(client.getClientID(), WSEventType.IMPORT_NETWORK, request)
			);
		} catch (CyWebServiceException ex) {
			JOptionPane.showMessageDialog(
				SuperpathwaysGui.this, "Error: " + ex.getErrorCode() + ". See error log for details", 
				"Error", JOptionPane.ERROR_MESSAGE	
			);
		}
	}
	
	public void setResults(final WSSearchResult[] results) {
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
			tableModel =new ListWithPropertiesTableModel<ResultProperty, ResultRow>();	
			if(results != null) {
				tableModel.setColumns(new ResultProperty[] {ResultProperty.NAME,ResultProperty.ORGANISM,});
				
				resultTable.setModel(tableModel);
				for(WSSearchResult r : results) {
					tableModel.addRow(new ResultRow(r));
				}
			}
				resultTable.setModel(tableModel);
				window.invalidate();
				window.repaint();
				window.pack();
				resultTable.invalidate();
				resultTable.doLayout();
				resultTable.repaint();		
				resultTable.invalidate();
				resultTable.doLayout();
				resultTable.repaint();
				System.out.println (resultTable);
				System.out.println (tableModel.getRowCount());
				System.out.println (tableModel.getValueAt(3, 1));
				System.out.println (resultTable.getModel());
				System.out.println (tableModel);
				System.out.println (resultTable.getRowHeight());
				System.out.println (resultTable.getRowHeight(3));
				System.out.println (resultTable.getHeight());
				System.out.println (pane.getHeight());
			}
		});
	}
	
    
 }



