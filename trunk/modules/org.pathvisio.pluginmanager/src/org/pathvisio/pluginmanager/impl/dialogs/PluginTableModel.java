// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.pluginmanager.impl.dialogs;

/*
 * Developed by Panagiotis Peikidis
 * http://pekalicious.com/blog/custom-jpanel-cell-with-jbuttons-in-jtable/
 */

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.pathvisio.pluginmanager.impl.data.BundleVersion;

public class PluginTableModel extends AbstractTableModel implements TableModel{
	List<BundleVersion> bundleVersions;
	
	public PluginTableModel(List<BundleVersion> bundleVersions) {
		this.bundleVersions = bundleVersions;
	}
	
	public Class<?> getColumnClass(int columnIndex) { return BundleVersion.class; }
	public int getColumnCount() { return 1; }
	public String getColumnName(int columnIndex) { return "Plug-ins"; }
	public int getRowCount() { return (bundleVersions == null) ? 0 : bundleVersions.size(); }
	public Object getValueAt(int rowIndex, int columnIndex) { return (bundleVersions == null) ? null : bundleVersions.get(rowIndex); }
	public boolean isCellEditable(int arg0, int arg1) { return true; }
}
