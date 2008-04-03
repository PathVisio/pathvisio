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
		DefaultMutableTreeNode top = makeTree();
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
		
		
		public static DefaultMutableTreeNode makeTree(){
			DefaultMutableTreeNode top = new DefaultMutableTreeNode("GOTerm Distribution");
			DefaultMutableTreeNode category = null;
		    DefaultMutableTreeNode book = null;

			// read all GoTerms
			List<GoTerm> terms = new ArrayList<GoTerm>();
			terms = GoReader.readGoDatabase("C:\\gene_ontology.obo");
			
			// get the Roots of the GoTerms
			List<GoTerm> roots = new ArrayList<GoTerm>();
			roots=GoReader.getRoots(terms);
			
			top = makeTree2(roots, terms, top, "r");

			return top;
		}
		
		public static DefaultMutableTreeNode makeTree2(List<GoTerm> parents, List<GoTerm> allTerms, DefaultMutableTreeNode top, String level){
			
			for(GoTerm parent : parents){
				DefaultMutableTreeNode par = new DefaultMutableTreeNode(parent.getName());
				top.add(par);
				
				List<GoTerm> children = new ArrayList<GoTerm>();
				children = GoReader.getChildren(allTerms, parent.getId());
				
				if(!children.isEmpty()){
					System.out.println("mk Tree "+level);
					if (level.length() < 2){
						try{
							DefaultMutableTreeNode childrenNodes = makeTree2(children, allTerms, top, level+"*");
							par.add(childrenNodes);
						}
						catch(IllegalArgumentException e) {
							System.out.println("kind is voorouder");
						}
					}
					
					
					}
				
				}
			
			return top;
			
		}
}
