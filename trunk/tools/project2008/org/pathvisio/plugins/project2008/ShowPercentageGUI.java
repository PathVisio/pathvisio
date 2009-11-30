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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;

/**
 * This class is used when in the MenuGUI the button 'percentage used genes' is chosen. When
 * this button is chosen, a new menu (percentageGUI) is shown. In this menu the user can choose
 * for which organism the percentage must be calculated. When the organism is chosen, this class
 * calculates and shows the percentage of used genes.
 * For thread safety, this method should be invoked from the event-dispatching thread.
 */
public class ShowPercentageGUI {


	public static void main(String[] args) {
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
					createAndShowPercentageGUI(arguments,organism);
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
    * percentage must be calculated. The percentage is being calculated using the method
    * 'getUsedGenes' in the class 'GeneCounter'.
    */

	public static void createAndShowPercentageGUI(final String[] arguments,String[]organism) {

    	final String dbDir = new String(arguments[0]+organism[0]);
  	  	final File pwDir = new File(arguments[1]+organism[1]);

        try {
			double percentageUsedgenes=GeneCounter.getUsedGenes(dbDir,pwDir);


			//Create and set up the window.
	        JFrame frame = new JFrame("Percentage of Used Genes");
	        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

	     // create a new panel
			JPanel canvasButtons=getCanvasButtons(frame);

	      //Add the ubiquitous "Hello World" label.
	        JLabel label = new JLabel("Percentage of used genes at http://www.wikipathways.org = "+percentageUsedgenes+"%");
	        frame.getContentPane().add(label);
	        frame.add(canvasButtons, BorderLayout.SOUTH);

	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);


		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


    }


    /**
     * In this method, the buttons are added to the menu. First the 'close' and 'menu' buttons
     * are added.
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
