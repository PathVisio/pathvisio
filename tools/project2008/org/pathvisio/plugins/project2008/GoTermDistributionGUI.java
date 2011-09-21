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
package org.pathvisio.plugins.project2008;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import org.pathvisio.data.DataException;
import org.pathvisio.go.GoMap;
import org.pathvisio.go.GoReader;
import org.pathvisio.go.GoTerm;
import org.pathvisio.go.GoTreeModel;
import org.pathvisio.model.ConverterException;

/**
 * This program shows the distribution of GO Terms. It also shows how much
 * genes it contains; and how much of these genes are found in the pathways.
 */
public class GoTermDistributionGUI {
	
	// make 2 private variables. The first one a set of strings "genidInPway",
	// which will be loaded with all the (ensemble formatted) genid's found
	// in the loaded pathways.
	// The second one, a map with a String as key and a GoTerm as value "treeString_GoTerm"
	// will be loaded with the Strings that represent the tree values as a key, and the
	// corresponding GoTerm.
	private Set<String> genidInPway = new HashSet<String>();
	private GoReader goReader = null;
	private boolean pwaysRead = false;
	private GoMap goMap = null;

	private String martexport;
	private String pgdb;
	private String pathwayroot;
	
	/**
	 *  
	 * The program requires 4 args:
	 * Gene ontology database (e.g. "C:\\gene_ontology.obo")
	 * Pathway database (e.g. "C:\\databases\\Rn_39_34i.pgdb")
	 * Pathways (e.g. "C:\\WPClient\\Rattus_norvegicus")
	 * Table from GOid to Ensembl (e.g. C:\\mart_export1.txt")	 * 
	 */
	public static void main(String[] args) throws DataException, ConverterException
	{
		GoTermDistributionGUI x = new GoTermDistributionGUI(
				new File (args[0]), args[1], args[2], args[3]);
		x.run();
	}
	
	public static void goTermDistribution(String[]args,String[]organism) throws DataException, ConverterException
	{
		GoTermDistributionGUI x = new GoTermDistributionGUI(
				new File (args[3]), args[0] + organism[0], args[1] + organism[1], args[4]);
		x.run();
	}
	
	public GoTermDistributionGUI(File oboFile, 
			String pgdb, 
			String pathwayroot, String martexport) 
	{
		goReader = new GoReader (oboFile);
		this.pgdb = pgdb;
		this.martexport = martexport;
		this.pathwayroot = pathwayroot;
	}
	
	public void readPathwayData()
	{
		try
		{
			genidInPway = GenidPway.getGenidPways(pgdb, pathwayroot);
		}
		catch(DataException e)
		{
			System.out.println("Error!");
		}
		catch(ConverterException e)
		{
			System.out.println("Error!");
		}
		
		System.out.println("Pathways read");

		goMap = new GoMap (new File (martexport));
		
		goMap.calculateNM (goReader.getRoots(), genidInPway);
		
		pwaysRead = true;	
	}

	public void run () throws DataException, ConverterException
	{
		// create a new frame
		final JFrame frame = new JFrame("GOTerm Distribution");

		// When click on exit, exit the frame
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		// set the size of the frame
		frame.setSize(350,570);
		frame.setLayout(new BorderLayout());
		
		// create a new panel
		JPanel canvasButtons = new JPanel();		
		
		GoTreeModel model = new GoTreeModel(goReader.getRoots());
		final JTree tree = new JTree(model) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String convertValueToText (
					Object value,
					boolean selected,
					boolean expended,
					boolean leaf,
					int row,
					boolean hasFocus)
			{
				if (!pwaysRead || !(value instanceof GoTerm))
				{
					return "" + value;
				}
				GoTerm goterm = (GoTerm)value;
				
				// get the number of genes in this term
				int m = goMap.getM(goterm);
				
				// get the number of genes the term overlaps with all the pathway genes
				int n = goMap.getN(goterm);
				
				// create a string with the goterm name, and the n and m values 
				// (see above) and print it to the console
				double percentage = ((double)n/(double)m)*100;
				return goterm.getName() + " " + "("+n+"/"+m+") ("+String.format("%3.1f", percentage)+"%)";
			}
		};
		
		// set the selectionmodel (only one branch can be selected at one time)
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				
		// create two new buttons, using the makeButton method
		JButton calcButton = makeButton("Calculate");
		JButton closeButton = makeButton("Close");
		
		// add the functionality to the close button
		closeButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						// close the program when the button is clicked
						frame.dispose();
						}
					}
				);
		
		// add the functionality to the calculate button
		calcButton.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent ae)
					{
						if (!pwaysRead)
						{
							readPathwayData();
							tree.invalidate(); // cause repaint
						}
					}
				});
		
		// add the buttons to the canvas
		canvasButtons.add(calcButton);
		canvasButtons.add(closeButton);	
		
		// add the canvas to the frame
		frame.add(canvasButtons, BorderLayout.SOUTH);
		
		// create a scroll pane containing the tree
        JScrollPane scrollPane = new JScrollPane(tree);
		
		// add the canvas to the frame
		frame.add(scrollPane, BorderLayout.CENTER);
		// Show the frame
		frame.setVisible(true);
	}
	
	/**
	 * create a new JButton of a preferred size, and the text centered.
	 */
	//TODO: move to utility class or deprecate
	public static JButton makeButton(String name)
	{
		// create a new button
		JButton button = new JButton(name);
		
		// set the size of the button
		Dimension sizeButton = new Dimension(130,30);
		button.setPreferredSize(sizeButton);
		
		// center the text (horizontally and vertically) in the button
		button.setVerticalTextPosition(AbstractButton.CENTER);
		button.setHorizontalTextPosition(AbstractButton.CENTER);
		
		// return the button
		return button;
	}
		
	public static void printMemUsage (String msg)
	{
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long mem = runtime.totalMemory() - runtime.freeMemory();
		System.out.println((mem >> 20) + "Mb used: " + msg);
	}

}
