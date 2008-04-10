// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

// import the things needed to run this java file.
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;


public class GoTermDistributionGUI {
	
	// make 2 private variables. The first one a set of strings "genidInPway",
	// which will be loaded with all the (ensemble formatted) genid's found
	// in the loaded pathways.
	// The second one, a map with a String as key and a GoTerm as value "treeString_GoTerm"
	// will be loaded with the Strings that represent the tree values as a key, and the
	// corresponding GoTerm.
	private static Set<String> genidInPway = new HashSet<String>();
	private static Map<String, GoTerm> treeString_GoTerm = new HashMap<String, GoTerm>();

	/**
	 * This program shows the distribution of GO Terms. It also shows how much
	 * genes it contains; and how much of these genes are found in the pathways.
	 * 
	 * The program requires 4 args:
	 * Gene ontology database (e.g. "C:\\gene_ontology.obo")
	 * Pathway database (e.g. "C:\\databases\\Rn_39_34i.pgdb")
	 * Pathways (e.g. "C:\\WPClient\\Rattus_norvegicus")
	 * Table from GOid to Ensembl (e.g. C:\\mart_export1.txt")	 * 
	 */
	public static void main(String[] args) throws DataException, ConverterException{
		run(args);
	}
	
	public static void goTermDistribution(String[]args) throws DataException, ConverterException{
		
		String[]arguments=new String[4];
		arguments[0]=args[4];
		arguments[1]=args[0]+"Rn_39_34i.pgdb";
		arguments[2]=args[2]+"\\Rattus_norvegicus";
		arguments[3]=args[5];
		
		run(arguments);
		
		
	}
	
	public static void run(String[] args) throws DataException, ConverterException{
		// create a new frame
		JFrame frame = new JFrame("GOTerm Distribution");

		// When click on exit, exit the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// set the size of the frame
		frame.setSize(350,570);
		
		// create a new panel
		JPanel canvasButtons = new JPanel();
		
		// create the tree, using the TreeReader method
		DefaultMutableTreeNode top = TreeReader(args[0],args[1],args[2],args[3]);
		final JTree tree = new JTree(top);
		
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
						System.exit(0);
						}
					}
				);
		
		// add the functionality to the calculate button
		calcButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						// get the string which represent the name of the branch selected
						String selectedString = tree.getSelectionPath().getLastPathComponent().toString();
						
						// get the goterm belonging to the branch
						GoTerm goterm = treeString_GoTerm.get(selectedString);
						
						// get the number of genes in this term
						String m = goterm.getNumberOfGenes()+"";
						
						// get the number of genes the term overlaps with all the pathway genes
						String n = goterm.getOverlapGenes(genidInPway)+"";
						
						// create a string with the goterm name, and the n and m values 
						// (see above) and print it to the console
						String calcString = goterm.getName() + " " + "("+n+"/"+m+")";
						System.out.println(calcString);
						}
					}
				);
		
		// add the buttons to the canvas
		canvasButtons.add(calcButton);
		canvasButtons.add(closeButton);	
		
		// add the canvas to the frame
		frame.add(canvasButtons, BorderLayout.SOUTH);
		
		// create a new panel
		JPanel canvasTree = new JPanel();
		
		// create a scroll pane containing the tree, set the preferred size and add it to the canvas
        JScrollPane scrollPane = new JScrollPane(tree);
        Dimension scrollPaneSize = new Dimension(315,485);
        scrollPane.setPreferredSize(scrollPaneSize);
		canvasTree.add(scrollPane);
		
		// add the canvas to the frame
		frame.add(canvasTree, BorderLayout.NORTH);
		
		// Show the frame
		frame.setVisible(true);
	}
	
	
	
	
	
		/**
		 * create a new JButton of a preferred size, and the text centered.
		 */
		public static JButton makeButton(String name){
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
		
		
		
		
		
		/**
		 * read goterms and genids, and create the tree
		 */
		public static DefaultMutableTreeNode TreeReader(String obo, String pgdb, String pathwayroot, String martexport) throws DataException, ConverterException{
			// create the top of the tree
			DefaultMutableTreeNode top = new DefaultMutableTreeNode("GOTerm Distribution");

			// read all GoTerms, using method readGoDatabase from the GoReader class
			Set<GoTerm> terms = new HashSet<GoTerm>();
			terms = GoReader.readGoDatabase(obo);
			
			// get the Roots of the GoTerms
			Set<GoTerm> roots= new HashSet<GoTerm>();
			roots=GoReader.getRoots(terms);
			
			// add the genes to the terms, using the addGenes method
			terms = addGenes(terms,martexport);
			
			// read all the pathways, extract the genId and show in the console that it happened
			// to read all these genid's from pathways, the method getSetGenIdsInPways from the 
			// GenidPway class is used.
			genidInPway = GenidPway.getGenidPways(pgdb,pathwayroot);
			System.out.println("Pathways read");
			
			// now the genes and goterms are read; make the tree
			top = makeTree(roots, terms, top);

			// return this tree
			return top;
		}
		
		
		
		
		
		/**
		 * create the tree using the goterms and genes (recursive)
		 */
		public static DefaultMutableTreeNode makeTree(Set<GoTerm> parents, Set<GoTerm> allTerms, DefaultMutableTreeNode top){
			// loop trough all given GoTerms
			for(GoTerm parent : parents){
				
				// *****[create a new parent branch]*****
				
				// get the number of genes in this term
				String m = parent.getNumberOfGenes()+"";
				
				// get the number of genes the term overlaps with all the pathway genes
				String n = parent.getOverlapGenes(genidInPway)+"";
				//String n = "n"; // uncomment to only show "n" instead of the number.
				
				// create the string that has to be printed when the branch is showed
				String treeString = parent.getName() + " " + "("+n+"/"+m+")";
				
				// create a branch with the string
				DefaultMutableTreeNode par = new DefaultMutableTreeNode(treeString);
				
				// add this string and its corresponding GoTerm to the map
				treeString_GoTerm.put(treeString, parent);
				
				// add the new parent to the top structure
				top.add(par);
		
				// if a parent has children, set the children as new parents, and put them in
				// this method again (so this method is recursive
				if(parent.hasChildren()){
				
					// make a list of all children
					Set<GoTerm> children = new HashSet<GoTerm>();
					children = parent.getChildren();
					
					// the method is restricted to 10 levels; this is done because
					// a Heap Space error occures when you create all levels.
					// this has to be fixed!
					if (par.getLevel() < 11){
						
						// create a new tree
						DefaultMutableTreeNode childrenNodes = makeTree(children, allTerms, par);

						// try to add the new tree to the branch
						try{
							par.add(childrenNodes);
						}
						catch(IllegalArgumentException e) {
							// this error occures every thime we go to a lower level.
							System.out.println("child is ancestor"); 
						}	
					}
				}
			}
			// return the tree
			return top;	
		}
		
		
		
		
		
		/**
		 * add the genes to the set of terms
		 */
		public static Set<GoTerm> addGenes(Set<GoTerm> terms, String martexport){
			// create a new map; the key is the GoTerm's id, the set of strings are the gene strings
			// various methods of the genesGOid class are used to do so.
			Map<String,Set<String>> geneByGO=genesGOid.geneByGO(genesGOid.goByGene(genesGOid.readDatabase(martexport)));
			
			// loop through all GoTerms
			for (GoTerm term: terms){

				// load the genes beloning to the goTerm and add them to this goterm, using a loop
				Set<String> genes = new HashSet<String>();
				try{
					genes = geneByGO.get(term.getId());
					for (String gene : genes){
						term.addGene(gene);
					}
				}
				catch (NullPointerException e){
					System.out.println("set is null"); // no idea why this error occures
				}
			}
			// return the goterms with the added genes			 
			return terms;
		}
}

		