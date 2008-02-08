package org.pathvisio.gui.swing.dialogs;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.pathvisio.data.GdbManager;
import org.pathvisio.gui.swing.dialogs.DataNodeDialog.XrefWithSymbol;
import org.pathvisio.model.Xref;

public class DatabaseSearchDialog extends OkCancelDialog {
	List<XrefWithSymbol> xrefs = new ArrayList<XrefWithSymbol>();
	
	public DatabaseSearchDialog(String title, List<XrefWithSymbol> xrefs) {
		super(null, title, null, true);
		this.xrefs = xrefs;
		Collections.sort(xrefs, new Comparator<XrefWithSymbol>() {
			public int compare(XrefWithSymbol o1, XrefWithSymbol o2) {
				int result = 0;
				if(o1.getSymbol() != null)
					result = o1.getSymbol().compareTo(o2.getSymbol());
				if(o1.getId() != null)
					if(result == 0) o1.getId().compareTo(o2.getId());
				if(o1.getDatabaseName() != null)
					if(result == 0) result = o1.getDatabaseName().compareTo(o2.getDatabaseName());
				return result;
			}
		});
		((XRefTableModel)table.getModel()).refresh();
		validate();
	}

	JTable table;
	Xref selected;
	
	public Xref getSelected() {
		return selected;
	}
	
	protected Component createDialogPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		JLabel label = new JLabel("Please select one of the references and press Ok");
		table = new JTable(new XRefTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane(table);

		GridBagConstraints grid = new GridBagConstraints();
		grid.insets = new Insets(10, 5, 10, 5);
		grid.gridwidth = 1;
		grid.gridheight = 2;
		grid.gridx = 0;
		grid.weightx = 1;
		grid.gridy = GridBagConstraints.RELATIVE;
		grid.fill = GridBagConstraints.HORIZONTAL;
		panel.add(label, grid);
		grid.fill = GridBagConstraints.BOTH;
		grid.weighty = 1;
		panel.add(scroll, grid);
		return panel;
	}
	
	private class XRefTableModel extends AbstractTableModel {
		public String getColumnName(int column) {
			switch(column) {
			case 0: return "Name";
			case 1: return "Identifier";
			case 2: return "Datasource";
			default: return "";
			}
		}
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return xrefs == null ? 0 : xrefs.size();
		}

		public Object getValueAt(int row, int col) {
			Xref xr = xrefs.get(row);
			switch(col) {
			case 0:
				return GdbManager.getCurrentGdb().getGeneSymbol(xr);
			case 1:
				return xr.getId();
			case 2:
				return xr.getDataSource();
			}
			return null;
		}
		
		public void refresh() {
			fireTableDataChanged();
		}
	}
	
	
	protected void okPressed() {
		//Store selected value
		int row = table.getSelectedRow();
		if(row > -1) {
			selected = xrefs.get(row);
		} else {
			selected = null;
		}
		super.okPressed();
	}
	
	protected void cancelPressed() {
		selected = null;
		super.cancelPressed();
	}
}
