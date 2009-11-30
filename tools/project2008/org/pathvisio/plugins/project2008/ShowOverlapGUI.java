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
package org.pathvisio.plugins.project2008;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;
import org.pathvisio.util.FileUtils;

import org.pathvisio.util.rowheader.JScrollPaneAdjuster;
import org.pathvisio.util.rowheader.JTableRowHeaderResizer;
import org.pathvisio.util.rowheader.RowHeaderRenderer;

/**
 * This class is used when in the MenuGUI the button 'pathway Overlap matrix' is chosen. When
 * this button is chosen, a new menu (overlapGUI) is shown. In this menu the user can choose for
 * which organism the overlap must be calculated. When the organism is chosen, the class
 * calculates and shows the overlap table.
 */

public class ShowOverlapGUI{

  public static void main(String[] args) throws DataException, ConverterException{

	  /**
		* in the String[] args, 5 arguments are given:
		* in example:
		* "C:\\databases\\"
		* "C:\pathways"
		* "C:\\result.html"
		* "C:\\gene_ontology.obo"
		* "C:\\mart_export1.txt"
		*
		* The first one is the directory that contains the databases.
		* The second one is the directory that contains the pathway cache.
		* The third one is the filename (note the html extension) of where the results are stored.
		*/

	  	final String[]organism={"Rn_39_34i.pgdb","\\Rattus_norvegicus"};
		String[]arg=new String[5];

		/**
		 * Check if the String[] args is given, and make Files containing the directories to
		 * the pathways and databases
		 */
		try {
			arg[0] = new String(args[0]);
			arg[1] = new String(args[1]);
			arg[2] = new String(args[2]);
			arg[3] = new String(args[3]);
			arg[4] = new String(args[4]);
			final String[]arguments=arg;

			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowOverlapGUI(arguments,organism);
				}
			});

		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("String[] args not given!");
			System.exit(0);
		}

  }

  /**
   * In this method, a menu is created. In this menu the user can choose for which organism the
   * overlap table has to be shown.
   */
  public static void createAndShowOverlapGUI(String[] arguments,String[]organism){

	  String dbBaseDir = arguments[0];
	  String pwBaseDir = arguments[1];

	  JFrame f = new JFrame("Pathway overlap table");

	  // When click on exit, exit the frame
	  f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

	  // create a new panel
	  JPanel canvasButtons=getCanvasButtons(f);

	  // create a new panel
	  JPanel canvasTable = new JPanel();
	  JScrollPane scrollPane =getTable(dbBaseDir,pwBaseDir,organism);
      canvasTable.add(scrollPane);


     f.add(canvasButtons, BorderLayout.SOUTH);
     f.add(canvasTable, BorderLayout.NORTH);

     f.pack();
     f.setLocation(200, 100);
     f.setVisible(true);

  }

  /**
   * In the method 'getTable' a table is made. In this table, for each gene pair the overlap is
   * shown.
   */
  public static JScrollPane getTable(String dbBaseDir,String pwBaseDir,String[]organism){

	  final String dbDir = new String(dbBaseDir+organism[0]);
	  final File pwDir = new File(pwBaseDir+organism[1]);

	  List<File> filenames = FileUtils.getFiles(pwDir, "gpml", true);
	  String[] columnNames=new String[filenames.size()];
      for(int i=0;i<filenames.size();i++){
    	  columnNames[i]=filenames.get(i).getName();
      }

      Double[][] data;
	try {
		data = GeneCounter.getOverlap(dbDir,pwDir);

	} catch (DataException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		data=null;
	} catch (ConverterException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		data=null;
	}

      DefaultTableModel headerData = new DefaultTableModel(0, 1);


      for(int i=0;i<filenames.size();i++){
    	  headerData.addRow(new Object[] {columnNames[i]});
      }

      //JTable table=getTable(data,columnNames);
      JTable table = new JTable(data,columnNames);
      JTable rowHeader = new JTable(headerData);

      LookAndFeel.installColorsAndFont
          (rowHeader, "TableHeader.background",
          "TableHeader.foreground", "TableHeader.font");

      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      rowHeader.setIntercellSpacing(new Dimension(0, 0));
      Dimension d = rowHeader.getPreferredScrollableViewportSize();
      d.width = rowHeader.getPreferredSize().width;
      rowHeader.setPreferredScrollableViewportSize(d);
      rowHeader.setRowHeight(table.getRowHeight());
      rowHeader.setDefaultRenderer(Object.class, new RowHeaderRenderer());

      JScrollPane scrollPane = new JScrollPane(table);

      scrollPane.setRowHeaderView(rowHeader);

      JTableHeader corner = rowHeader.getTableHeader();
      corner.setReorderingAllowed(false);
      corner.setResizingAllowed(false);

      scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);

      new JScrollPaneAdjuster(scrollPane);

      new JTableRowHeaderResizer(scrollPane).setEnabled(true);

      return scrollPane;
  }

  /**
   * In this method, the buttons are added to the menu. First the 'close' and 'menu' buttons are
   * added.
   */
  public static JPanel getCanvasButtons(final JFrame frame){
	  // create a new panel
	   JPanel canvasButtons = new JPanel();

	  // create two new buttons, using the makeButton method
		JButton closeButton = GoTermDistributionGUI.makeButton("Close");

		// add the functionality to the close button
		closeButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						frame.dispose();
						}
					}
				);



		// add the buttons to the canvas
		canvasButtons.add(closeButton);

	  return canvasButtons;
  }


}
