package org.pathvisio.desktop.dialog;

/*
 * Developed by Panagiotis Peikidis
 * http://pekalicious.com/blog/custom-jpanel-cell-with-jbuttons-in-jtable/
 */

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.felix.bundlerepository.Resource;

public class ResourceTableModel extends AbstractTableModel implements TableModel{
	List<Resource> resources;
	
	public ResourceTableModel(List<Resource> resources) {
		this.resources = resources;
	}
	
	public Class<?> getColumnClass(int columnIndex) { return Resource.class; }
	public int getColumnCount() { return 1; }
	public String getColumnName(int columnIndex) { return "Plug-ins"; }
	public int getRowCount() { return (resources == null) ? 0 : resources.size(); }
	public Object getValueAt(int rowIndex, int columnIndex) { return (resources == null) ? null : resources.get(rowIndex); }
	public boolean isCellEditable(int arg0, int arg1) { return true; }
}
