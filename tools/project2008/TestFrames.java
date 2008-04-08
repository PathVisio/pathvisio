import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;



public class TestFrames {
	
	private static Map<String,List<String>> geneByGO = new HashMap<String,List<String>>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
		DefaultMutableTreeNode top = TreeReader();
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
		
		
		public static DefaultMutableTreeNode TreeReader(){
			DefaultMutableTreeNode top = new DefaultMutableTreeNode("GOTerm Distribution");

			// read all GoTerms
			Set<GoTerm> terms = new HashSet<GoTerm>();
			terms = GoReader.readGoDatabase("D:\\My Documents\\TUe\\BiGCAT\\gene_ontology.obo");
			
			// get the Roots of the GoTerms
			Set<GoTerm> roots= new HashSet<GoTerm>();
			roots=GoReader.getRoots(terms);
			String s; 
			List<String[]> arrayGOgenes = new ArrayList<String[]>();
			try {
				 FileReader fr = new FileReader("D:\\My Documents\\TUe\\BiGCAT\\mart_export.txt");
			     BufferedReader br = new BufferedReader(fr);
			     while((s = br.readLine()) != null){
			    	 arrayGOgenes.add(s.split("\t"));
				      }
				      fr.close();
			    }
				    catch(Exception e) {
				      System.out.println("Exception: " + e);
				}
			    /**In this method the map "goByGene" is created. In this map, the gene-Id's are the keys and the GO-Id's are the values.*/	
				Map<String,List<String>> goByGene=genesGOid.goByGene(arrayGOgenes);		
				geneByGO=genesGOid.geneByGO(goByGene);
				String goId="GO:0008020";
				System.out.println(geneByGO.get("GO:0008020"));
				top = makeTree(roots, terms, top);

			return top;
		}
		
		
		
		public static DefaultMutableTreeNode makeTree(Set<GoTerm> parents, Set<GoTerm> allTerms, DefaultMutableTreeNode top){
			// loop trough all given GoTerms
		
			/*In this method all Gene-Id's for the given GO-ID are returned in a List*/ 
			
			
			for(GoTerm parent : parents){
				
				
				//System.out.println(geneByGO.get("GO:0008020"));
				
				DefaultMutableTreeNode par = new DefaultMutableTreeNode(parent.getName());

				
				// add the new parent to the top structure
				top.add(par);
		

				// make a list of all children
				Set<GoTerm> children = new HashSet<GoTerm>();
				children = parent.getChildren();
				
				
			
				
				// if a children list is not empty, set the children as new parents, and put them in
				// this method again
				if(!children.isEmpty()){
					
					// give some output to show where you are
					//System.out.println("mk Tree "+level);
					
					// set the maximum level of children
					//if (level.length() < 4){
						
						// create a new tree and add it; when an error occures, catch it
						DefaultMutableTreeNode childrenNodes = makeTree(children, allTerms, par);
						try{
							par.add(childrenNodes);
							}
						catch(IllegalArgumentException e) {
							System.out.println("kind is voorouder"); // deze error komt steeds als je een stapje naar beneden gaat.
							}
						//}
					
					}
	
				}
			
			return top;	
			
		}
}
