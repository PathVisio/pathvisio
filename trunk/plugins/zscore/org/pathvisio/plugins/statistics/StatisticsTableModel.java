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
package org.pathvisio.plugins.statistics;

import java.awt.Component;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.plugins.statistics.StatisticsPlugin.StatisticsPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.swing.ListWithPropertiesTableModel;

/**
 * Table Model for showing statistics results
 */
class StatisticsTableModel extends ListWithPropertiesTableModel<Column, StatisticsPathwayResult>
{
	private static final long serialVersionUID = 1L;

	// array of columns we are going to save
	private Column[] saveColumns;

	public StatisticsTableModel() {
		if(PreferenceManager.getCurrent().getBoolean(
				StatisticsPreference.STATS_RESULT_INCLUDE_FILENAME)) {
			saveColumns = new Column[] {
					Column.PATHWAY_NAME, Column.FILE_NAME, Column.R,
					Column.N, Column.TOTAL, Column.PCT, Column.ZSCORE,
			};
		} else {
			saveColumns = new Column[] {
					Column.PATHWAY_NAME, Column.R, Column.N,
					Column.TOTAL, Column.PCT, Column.ZSCORE,
			};
		}
	}

	public void printData(PrintStream out) throws IOException
	{

		// print table header
		{
			boolean first = true;
			for (Column col : saveColumns)
			{
				if (!first)
				{
					out.print ("\t");
				}
				first = false;
				out.print (col.title);
			}
			out.println ();
		}

		// print table rows
		for (StatisticsPathwayResult sr : rows)
		{
			boolean first = true;
			for (Column col : saveColumns)
			{
				if (!first)
				{
					out.print ("\t");
				}
				first = false;
				out.print (sr.getProperty(col));
			}
			out.println();
		}
	}

	/**
	 * Sort results on z-score
	 */
	public void sort()
	{
		Collections.sort (rows, new Comparator<StatisticsPathwayResult>()
		{

			public int compare(StatisticsPathwayResult arg0, StatisticsPathwayResult arg1)
			{
				double z1 = arg1.getZScore();
				double z0 = arg0.getZScore();

				if (Double.isNaN(z1) && Double.isNaN(z0)) return 0;
				if (Double.isNaN(z1)) return -1;
				if (Double.isNaN(z0)) return 1;
				return Double.compare(z1, z0);
			}
		});
		fireTableDataChanged();
	}

	/**
    // Returns the preferred height of a row.
    // The result is equal to the tallest cell in the row.
     */
    public static int getPreferredRowHeight(JTable table, int rowIndex, int margin) {
        // Get the current default height for all rows
        int height = table.getRowHeight();

        // Determine highest cell in the row
        for (int c=0; c<table.getColumnCount(); c++) {
            TableCellRenderer renderer = table.getCellRenderer(rowIndex, c);
            Component comp = table.prepareRenderer(renderer, rowIndex, c);
            int h = comp.getPreferredSize().height + 2*margin;
            height = Math.max(height, h);
        }
        return height;
    }

    /**
     * The height of each row is set to the preferred height of the
     * tallest cell in that row.
     */
    static public void packRows(JTable table, int margin)
    {
        packRows(table, 0, table.getRowCount(), margin);
    }

    /**
    // For each row >= start and < end, the height of a
    // row is set to the preferred height of the tallest cell
    // in that row.
     */
    public static void packRows(JTable table, int start, int end, int margin) {
        for (int r=0; r<table.getRowCount(); r++) {
            // Get the preferred height
            int h = getPreferredRowHeight(table, r, margin);

            // Now set the row height using the preferred height
            if (table.getRowHeight(r) != h) {
                table.setRowHeight(r, h);
            }
        }
    }

}