
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
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
		final String dbDir;
		  final File pwDir;
			
		  try {
			  dbDir = new String(args[0]);
			  pwDir = new File(args[1]);
			  
			  javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						createAndShowPercentageGUI(dbDir,pwDir);
					}
				});

		  }
		  catch(ArrayIndexOutOfBoundsException e) {
			  System.out.println("String[] args not given!");
			  System.exit(0);
		  }
        
            
    }
	
    public static void createAndShowPercentageGUI(String dbDir,File pwDir) {
        
        
        try {
			double percentageUsedgenes=GeneCounter.getUsedGenes(dbDir,pwDir);
			//Create and set up the window.
	        JFrame frame = new JFrame("Percentage of Used Genes");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
	      //Add the ubiquitous "Hello World" label.
	        JLabel label = new JLabel("Percentage of used genes at http://www.wikipathways.org = "+percentageUsedgenes+"%");
	        frame.getContentPane().add(label);

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

    
}
