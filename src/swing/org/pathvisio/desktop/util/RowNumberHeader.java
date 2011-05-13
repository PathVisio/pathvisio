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
package org.pathvisio.desktop.util;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.table.AbstractTableModel;

/**
//Source: http://www.java2s.com/Code/Java/Swing-JFC/RowNumberTableHeader.htm
 */
public class RowNumberHeader extends JTable
{

	  protected JTable mainTable;
	public RowNumberHeader(JTable table) {
	  super();
	  mainTable = table;
	  setModel(new RowNumberTableModel());
	  setPreferredScrollableViewportSize(getMinimumSize());
	  setRowSelectionAllowed(false);
	  JComponent renderer = (JComponent) getDefaultRenderer(Object.class);
	  LookAndFeel.installColorsAndFont(renderer, "TableHeader.background",
	      "TableHeader.foreground", "TableHeader.font");
	  LookAndFeel.installBorder(this, "TableHeader.cellBorder");
	}

	public int getRowHeight(int row) {
	  return mainTable.getRowHeight();
	}

	/** Simple Table model: just one column, cell values are numbers starting from 1 */
	private class RowNumberTableModel extends AbstractTableModel {

	  public int getRowCount() {
	    return mainTable.getModel().getRowCount();
	  }

	  public int getColumnCount() {
	    return 1;
	  }

	  public Object getValueAt(int row, int column) {
	    return new Integer(row + 1);
	  }

	}
}