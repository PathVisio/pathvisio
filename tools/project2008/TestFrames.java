import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;



public class TestFrames {
	
	private static Set<String> genidInPway = new HashSet<String>();

	/**
	 * Program requires 4 args:
	 * Gene ontology database (e.g. "C:\\gene_ontology.obo")
	 * Pathway database (e.g. "C:\\databases\\Rn_39_34i.pgdb")
	 * Pathways (e.g. "C:\\WPClient\\Rattus_norvegicus")
	 * Table from GOid to Ensembl (e.g. C:\\mart_export1.txt")	 * 
	 */
	public static void main(String[] args) throws DataException, ConverterException{
		// create a new frame
		JFrame frame = new JFrame("GOTerm Distribution");

		// When click on exit, exit the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// set the size of the frame
		frame.setSize(350,570);
		
		// create a new panel
		JPanel canvasButtons = new JPanel();
		
		// create two new buttons, using the makeButton method
		JButton calcButton = makeButton("Calculate");
		JButton closeButton = makeButton("Close");
		
		// add the functionality to the close button
		closeButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.exit(0);
						}
					}
				);
		
		// add the functionality to the calculate button
		calcButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.out.println("Calculate Button pressed");
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
		
		// create a tree
		DefaultMutableTreeNode top = TreeReader(args[0],args[1],args[2],args[3]);
		JTree tree = new JTree(top);
		
		// create a scroll pane
        JScrollPane scrollPane = new JScrollPane(tree);
        Dimension scrollPaneSize = new Dimension(315,485);
        scrollPane.setPreferredSize(scrollPaneSize);
		canvasTree.add(scrollPane);
		
		frame.add(canvasTree, BorderLayout.NORTH);
		
		
		// Show the frame
		frame.setVisible(true);
	}
	
	/**
	 * create a new JButton of a preferred size, and with the text centered.
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
		
		
		public static DefaultMutableTreeNode TreeReader(String obo, String pgdb, String pathwayroot, String martexport) throws DataException, ConverterException{
			DefaultMutableTreeNode top = new DefaultMutableTreeNode("GOTerm Distribution");

			// read all GoTerms
			Set<GoTerm> terms = new HashSet<GoTerm>();
			terms = GoReader.readGoDatabase(obo);
			
			// get the Roots of the GoTerms
			Set<GoTerm> roots= new HashSet<GoTerm>();
			roots=GoReader.getRoots(terms);
			
			terms = addGenes(terms,martexport);
			genidInPway = getN.getSetGenIdsInPways(pgdb,pathwayroot);
			System.out.println("Pathways read");
			
			top = makeTree(roots, terms, top);

			return top;
		}
		
		public static DefaultMutableTreeNode makeTree(Set<GoTerm> parents, Set<GoTerm> allTerms, DefaultMutableTreeNode top){
			// loop trough all given GoTerms
			for(GoTerm parent : parents){
				
				// create a new parent branch
				int m = parent.getNumberOfGenes();
				int n = parent.getOverlapGenes(genidInPway);
				DefaultMutableTreeNode par = new DefaultMutableTreeNode(parent.getName() + " " + "("+n+"/"+m+")");
				
				// add the new parent to the top structure
				top.add(par);
		
				// if a children list is not empty, set the children as new parents, and put them in
				// this method again
				if(parent.hasChildren()){
				
					// make a list of all children
					Set<GoTerm> children = new HashSet<GoTerm>();
					children = parent.getChildren();
					
					// create a new tree and add it; when an error occures, catch it
					DefaultMutableTreeNode childrenNodes = makeTree(children, allTerms, par);
					try{
						par.add(childrenNodes);
					}
					catch(IllegalArgumentException e) {
						//System.out.println("kind is voorouder"); // deze error komt steeds als je een stapje naar beneden gaat.
					}
				}
			}
			return top;	
		}
		
		public static Set<GoTerm> addGenes(Set<GoTerm> terms, String martexport){
			// create a new map; the key is the GoTerm's id, the set of strings are the gene strings
			Map<String,Set<String>> geneByGO=genesGOid.geneByGO(genesGOid.goByGene(genesGOid.readDatabase(martexport)));
			
			for (GoTerm term: terms){

				Set<String> genes = new HashSet<String>();
				try{
					genes = geneByGO.get(term.getId());
					for (String gene : genes){
						term.addGene(gene);
					}
				}
				catch (NullPointerException e){
					//System.out.println("set is leeg");
				}
			}
						 
			return terms;
		}
}

		