
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;        

import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;

public class ShowPercentageGUI {
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
	
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
		
		final String[] arguments={args[0],args[1],"C:\\result.html"};
		final String dbDir;
		final File pwDir;
					
		try {
			dbDir = new String(args[0]+"Rn_39_34i.pgdb");
			pwDir = new File(args[1]+"\\Rattus_norvegicus");
						  
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowPercentageGUI(arguments);
				}
			});
			
		}
		
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("String[] args not given!");
			System.exit(0);
		}
        
            
    }
	
    public static void createAndShowPercentageGUI(final String[] arguments) {
    	
    	final String dbDirRn = new String(arguments[0]+"Rn_39_34i.pgdb");
  	  	final File pwDirRn = new File(arguments[1]+"\\Rattus_norvegicus");
        
        try {
			double percentageUsedgenes=GeneCounter.getUsedGenes(dbDirRn,pwDirRn);
			
			// create a new panel
			   JPanel canvasButtons=getCanvasButtons(arguments);
			//Create and set up the window.
	        JFrame frame = new JFrame("Percentage of Used Genes");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
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
    
    public static JPanel getCanvasButtons(final String[] arguments){
  	  // create a new panel
  	   JPanel canvasButtons = new JPanel();
  		
  	  // create two new buttons, using the makeButton method
  		JButton menuButton = GoTermDistributionGUI.makeButton("menu");
  		JButton closeButton = GoTermDistributionGUI.makeButton("Close");
  		
  		// add the functionality to the close button
  		closeButton.addActionListener(
  				new ActionListener(){
  					public void actionPerformed(ActionEvent ae){
  						System.exit(0);
  						}
  					}
  				);
  		
  		// add the functionality to the calculate button
  		menuButton.addActionListener(
  				new ActionListener(){
  					public void actionPerformed(ActionEvent ae){
  						showMenuGUI.createAndShowMenuGUI(arguments);
  						System.out.println("Go to Menu");
  						}
  					}
  				);
  		
  		// add the buttons to the canvas
  		canvasButtons.add(menuButton);
  		canvasButtons.add(closeButton);	
  		
  	  return canvasButtons;
    }

    
}
