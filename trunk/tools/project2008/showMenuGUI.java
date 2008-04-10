

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
		* in the String[] args, 2 arguments are given:
		* in example:
		* "C:\\databases\\"
		* "C:\pathways"
		* 
		* The first one is the directory that contains the databases.
		* The second one is the directory that contains the pathway cache.
		*/ 
		final String[] arguments=args;		
		final String dbDir;
		final File pwDir;
		final String outfile;
			
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
	
	
	public static JPanel canvasButtons(final String[] arguments){
		
		final String dbDirRn = new String(arguments[0]+"Rn_39_34i.pgdb");
		final File pwDirRn = new File(arguments[1]+"\\Rattus_norvegicus");
		
		
		
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
						showOverlapGUI.createAndShowOverlapGUI(dbDirRn,pwDirRn,arguments);
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
						ShowPercentageGUI.createAndShowPercentageGUI(dbDirRn,pwDirRn,arguments);
						}
					}
				);
		
		// add the functionality to the Go Term Distribution button
		GtdButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						System.out.println("The Go Term Distribution function is not available");
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
	
	public static JPanel canvasCloseButton(){
		// create a new panel
		JPanel canvasCloseButton = new JPanel();
		
		// create two new buttons, using the makeButton method
		JButton closeButton = TestFrames.makeButton("Close");

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
	 * create a new JButton of a preferred size, and with the text centered.
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






