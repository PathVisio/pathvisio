

//package components;

/*
 * SimpleTableDemo.java requires no other files.
 */

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.xmlrpc.XmlRpcException;
import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class showMenuGUI extends JPanel {
	
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
		final String[] arguments=args;		
		final String dbDir;
		final File pwDir;
		final String outfile;
			
		/** 
		 * Check if the String[] args is given, and make Files containing the directories to
		 * the pathways and databases 
		 */
		try {
			dbDir = new String(args[0]+"Rn_39_34i.pgdb");
			pwDir = new File(args[1]+"\\Rattus_norvegicus");
			outfile=args[2];
			  
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
	 * The buttons on the menu are created using the methogs 'canvasButtond' and 
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
		JButton PomButton = makeBigButton("Pathway overlap Matrix");
		JButton LCButton = makeBigButton("Link Checker");
		JButton PugButton = makeBigButton("Percentage used genes");
		JButton GtdButton = makeBigButton("Go Term Distribution");
				
		// add the functionality to the Pathway overlap Matrix button
		PomButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.out.println("Go to Pathway overlap Matrix");
						showOverlapGUI.createAndShowOverlapGUI(arguments);
						}
					}
				);
		
		// add the functionality to the Link Checker button
		LCButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.out.println("Go to Link Checker");
						/*String[] arguments={"C:\\databases\\",
											"C:\\pathways",
											"C:\\result.html"};*/
											
						try {
							LinkChecker.main(arguments);
						} catch (ConverterException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (DataException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (XmlRpcException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						}
					}
				);
		
		// add the functionality to the Go Term Distribution button
		PugButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.out.println("Go to Percentage of used genes at WikiPathways");
						ShowPercentageGUI.createAndShowPercentageGUI(arguments);
						}
					}
				);
		
		// add the functionality to the Go Term Distribution button
		GtdButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.out.println("Go to The Go Term Distribution function");
						try {
							GoTermDistributionGUI.goTermDistribution(arguments);
						} catch (DataException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ConverterException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
					}
				);
		
		
		
		// add the buttons to the canvas
		canvasButtons.add(LCButton);
		canvasButtons.add(PomButton);
		canvasButtons.add(PugButton);
		canvasButtons.add(GtdButton);
		
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






