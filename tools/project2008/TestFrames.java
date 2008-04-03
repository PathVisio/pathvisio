import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;


public class TestFrames {

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
			List<GoTerm> terms = new ArrayList<GoTerm>();
			terms = GoReader.readGoDatabase("C:\\gene_ontology.obo");
			
			// get the Roots of the GoTerms
			List<GoTerm> roots = new ArrayList<GoTerm>();
			roots=GoReader.getRoots(terms);
			
			top = makeTree(roots, terms, top, "r");

			return top;
		}
		
		public static DefaultMutableTreeNode makeTree(List<GoTerm> parents, List<GoTerm> allTerms, DefaultMutableTreeNode top, String level){
			// loop trough all given GoTerms
			for(GoTerm parent : parents){
				
				// create a new parent branch
				DefaultMutableTreeNode par = new DefaultMutableTreeNode(parent.getName());
				
				// add the new parent to the top structure
				top.add(par);
		

				// make a list of all children
				List<GoTerm> children = new ArrayList<GoTerm>();
				children = GoReader.getChildren(allTerms, parent.getId());
				
				// if a children list is not empty, set the children as new parents, and put them in
				// this method again
				if(!children.isEmpty()){
					
					// give some output to show where you are
					System.out.println("mk Tree "+level);
					
					// set the maximum level of children
					if (level.length() < 4){
						
						// create a new tree and add it; when an error occures, catch it
						DefaultMutableTreeNode childrenNodes = makeTree(children, allTerms, par, level+"*");
						try{
							par.add(childrenNodes);
							}
						catch(IllegalArgumentException e) {
							System.out.println("kind is voorouder"); // deze error komt steeds als je een stapje naar beneden gaat.
							}
						}
					
					}
	
				}
			
			return top;	
			
		}
}
