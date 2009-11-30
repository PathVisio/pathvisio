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


//package components;

/*
 * SimpleTableDemo.java requires no other files.
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;

/**
 * When running this class, it is possible to choose which function will be executed. A little
 * screen is shown with four buttons:
 * -Link checker,
 * -Pathway overlap Matrix,
 * -Percentage used genes,
 * -Go term distribution.
 */
public class ShowMenuGUI extends JPanel {

	private static final long serialVersionUID = -1L;

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
					createAndShowMenuGUI(arguments);
				}
			});

		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("String[] args not given!");
			System.exit(0);
		}


	}

	/**
	 * In the method 'createAndShowMenuGUI' the MenuGUI is created and shown.
	 * The buttons on the menu are created using the methods 'canvasButtond' and
	 * 'canvasCloseButton'
	 * @param arguments
	 */
	public static void createAndShowMenuGUI(String[] arguments){

		// create a new frame
		JFrame frame = new JFrame("Main Menu");

		// When click on exit, exit the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set the size of the frame
		frame.setSize(400,300);

		// create a new panel
		JPanel canvasTop=new JPanel();
		JPanel canvasButtons=canvasButtons(arguments);
		JPanel canvasCloseButton=canvasCloseButton();

		JLabel label = new JLabel("Main Menu");
		canvasTop.add(label);


		// add the canvas to the frame
		frame.add(canvasTop, BorderLayout.NORTH);
		frame.add(canvasButtons, BorderLayout.CENTER);
		frame.add(canvasCloseButton, BorderLayout.SOUTH);


		// Show the frame
		frame.setVisible(true);
	}

	/**
	 * In the method 'canvasButtons' the buttons are created for the Main Menu.
	 * First, the buttons are created, without functionality.
	 * Then, for each button the functionality is added.
	 * At last, the buttons are added to the canvas.These buttons are returned when the method
	 * is executed.
	 */
	public static JPanel canvasButtons(final String[] arguments){

		// create a new panel
		JPanel canvasButtons = new JPanel();

		// create two new buttons, using the makeButton method
		JButton btnPom = makeBigButton("Pathway overlap Matrix");
		JButton btnLC = makeBigButton("Link Checker");
		JButton btnPug = makeBigButton("Percentage used genes");
		JButton btnGtd = makeBigButton("Go Term Distribution");

		// add the functionality to the Pathway overlap Matrix button
		btnPom.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.out.println("Go to Pathway overlap Matrix");
						String[] organism=ChooseOrganism.getOrganism();
						ShowOverlapGUI.createAndShowOverlapGUI(arguments,organism);
						}
					}
				);

		// add the functionality to the Link Checker button
		btnLC.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae)
					{
						System.out.println("Go to Link Checker");
//						LinkChecker.main(arguments);
					}
				}
			);

		// add the functionality to the Go Term Distribution button
		btnPug.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						String[] organism=ChooseOrganism.getOrganism();
						ShowPercentageGUI.createAndShowPercentageGUI(arguments,organism);
						}
					}
				);

		// add the functionality to the Go Term Distribution button
		btnGtd.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.out.println("Go to The Go Term Distribution function");
						String[] organism=ChooseOrganism.getOrganism();
						try {
							GoTermDistributionGUI.goTermDistribution(arguments,organism);
						} catch (DataException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ConverterException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});



		// add the buttons to the canvas
		canvasButtons.add(btnLC);
		canvasButtons.add(btnPom);
		canvasButtons.add(btnPug);
		canvasButtons.add(btnGtd);

		return canvasButtons;
	}

	/**
	 * In the method 'canvasCloseButton' the Close button for the Main menu is created.
	 */
	public static JPanel canvasCloseButton(){
		// create a new panel
		JPanel canvasCloseButton = new JPanel();

		// create two new buttons, using the makeButton method
		JButton closeButton = GoTermDistributionGUI.makeButton("Close");

		// add the functionality to the close button
		closeButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.exit(0);
						}
					}
				);



		// add the buttons to the canvas
		canvasCloseButton.add(closeButton);

		return canvasCloseButton;
	}

	/**
	 * In the method 'makeBigButton' a new JButton of a preferred size and with the text
	 * centered is created.
	 */
		public static JButton makeBigButton(String name){
			// create a new button
			JButton button = new JButton(name);

			// set the size of the button
			Dimension sizeButton = new Dimension(180,30);
			button.setPreferredSize(sizeButton);

			// center the text (horizontally and vertically) in the button
			button.setVerticalTextPosition(AbstractButton.CENTER);
			button.setHorizontalTextPosition(AbstractButton.CENTER);

			// return the button
			return button;
		}
}






