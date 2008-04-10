import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.xmlrpc.XmlRpcException;
import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;


public class chooseOrganism {
	
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
					/**
					 * Function:
					 * 0 = showOverlapGUI
					 * 1 = ShowPercentageGUI
					 * 2 = GoTermDistribution
					 */
					
					
					
					runFrame(0,arguments);
					
				}
			});

		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("String[] args not given!");
			System.exit(0);
		}
		
		
		
		
	}
	
	public static void runFrame(int function,String[]arguments){
		
		//JPanel canvasLabel=new JPanel();
		JPanel canvasButtons=canvasButtons(function,arguments);
		
		//JLabel label = new JLabel("Make a choice");
		//canvasLabel.add(label);
		
		
		
		// create a new frame
		JFrame fr = new JFrame("Make a choice");
    
		// When click on exit, exit the frame
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		// set the size of the frame
		fr.setSize(400,300);
		
		//fr.add(canvasLabel);
		fr.add(canvasButtons);
	

	
		
		
	
	
		// add the canvas to the frame
	
	
		// Show the frame
		fr.setVisible(true);
	}
	
	
	
	public static JPanel canvasButtons(final int function,final String[]arguments){
		
		// create a new panel
		JPanel canvasButtons = new JPanel();
		
		// create two new buttons, using the makeButton method
		JButton CeButton = showMenuGUI.makeBigButton("Caenorhabditis elegans");
		JButton DrButton = showMenuGUI.makeBigButton("Drosophila melanogaster");
		JButton HsButton = showMenuGUI.makeBigButton("Homo sapiens");
		JButton MmButton = showMenuGUI.makeBigButton("Mus musculus");
		JButton RnButton = showMenuGUI.makeBigButton("Rattus norvegicus");
		JButton ScButton = showMenuGUI.makeBigButton("Saccharomyces cerevisiae");
		
		// add the functionality to the Pathway overlap Matrix button
		CeButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						geenidee(0,function,arguments);
						
						}
					}
				);
		
		// add the functionality to the Pathway overlap Matrix button
		DrButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						geenidee(1,function,arguments);
						
						}
					}
				);
		
		// add the functionality to the Pathway overlap Matrix button
		HsButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						geenidee(2,function,arguments);
						
						}
					}
				);
		
		// add the functionality to the Pathway overlap Matrix button
		MmButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						geenidee(3,function,arguments);
						
						}
					}
				);
		
		// add the functionality to the Pathway overlap Matrix button
		RnButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						geenidee(4,function,arguments);
						
						}
					}
				);
		
		// add the functionality to the Pathway overlap Matrix button
		ScButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						geenidee(5,function,arguments);
						
						}
					}
				);
		
		

		
		// add the buttons to the canvas
		canvasButtons.add(CeButton);
		canvasButtons.add(DrButton);
		canvasButtons.add(HsButton);
		canvasButtons.add(MmButton);
		canvasButtons.add(RnButton);
		canvasButtons.add(ScButton);
		
		return canvasButtons;
	}
	
	
	public static void geenidee(int kindOfAnnimal,int function,String[]arguments){
		
		/**
		 * kindOfAnnimal:
		 * 0 = Caenorhabditis elegans
		 * 1 = Drosophila melanogaster
		 * 2 = Homo sapiens
		 * 3 = Mus musculus
		 * 4 = Rattus norvegicus
		 * 5 = Saccharomyces cerevisiae
		 */
		
		String[][] annimalNames=new String[6][];
		annimalNames[0]=new String[]{"Ce_20070902.pgdb","\\Caenorhabditis_elegans"};
		annimalNames[1]=new String[]{"Dr_20070817.pgdb","\\Drosophila_melanogaster"};
		annimalNames[2]=new String[]{"Hs_41_36c.pgdb","\\Homo_sapiens"};
		annimalNames[3]=new String[]{"Mm_38_35.pgdb","\\Mus_musculus"};
		annimalNames[4]=new String[]{"Rn_39_34i.pgdb","\\Rattus_norvegicus"};
		annimalNames[5]=new String[]{"Sc_41_1d.pgdb","\\Saccharomyces_cerevisiae"};
		
		String[]annimal=annimalNames[kindOfAnnimal];
		
		
		if(function==0){
			//showOverlapGUI.createAndShowOverlapGUI(arguments,annimal);
			System.out.println("showOverlapGUI.createAndShowOverlapGUI("+annimal[0]+","+annimal[1]+")");
			
		}

		
		
	}
	
	
	
	
}

