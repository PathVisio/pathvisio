package org.pathvisio.plugins.project2008;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;

/**
 * In this class the user can choose the organism what for the function has to be executed.
 */
public class chooseOrganism {
	
	/**
	 * In this method the screen is created with 6 buttons, one for each organism. 
	 */
	static private class OrganismDialog
	{
		public JPanel canvasButtons(){
			
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
							System.out.println("CE");
							org=0;
							
							}
						}
					);
			
			// add the functionality to the Pathway overlap Matrix button
			DrButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent ae){
							System.out.println("DR");
							org=1;
							}
						}
					);
			
			// add the functionality to the Pathway overlap Matrix button
			HsButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent ae){
							System.out.println("HS");
							org=2;
							}
						}
					);
			
			// add the functionality to the Pathway overlap Matrix button
			MmButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent ae){
							System.out.println("MM");
							org=3;
							}
						}
					);
			
			// add the functionality to the Pathway overlap Matrix button
			RnButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent ae){
							System.out.println("RN");
							org=4;
							
							}
						}
					);
			
			// add the functionality to the Pathway overlap Matrix button
			ScButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent ae){
							System.out.println("SC");
							org=5;
							
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
		
		public int org =-1;
	}
	
	
	public static String[] getOrganism(){
		
		final JDialog dialog = new JDialog((JFrame)null, true);
		
		dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		OrganismDialog dlg = new OrganismDialog();
		JPanel canvasButtons=dlg.canvasButtons();
		JPanel menuCloseButtons=menuCloseButtons(dialog);
		
		dialog.add(canvasButtons, BorderLayout.CENTER);
		dialog.add(menuCloseButtons, BorderLayout.SOUTH);
		
		dialog.setSize(400,300);
		
		dialog.setVisible(true);
		
		String[] kindOfOrganism=null;
		
		if(dlg.org>=0){
			kindOfOrganism = getOrganismInfo (dlg.org);
		}
		
		return kindOfOrganism;
	}
	

	/**
	 * In this method the 'menu' and 'close' buttons for the screen are created.
	 */
	public static JPanel menuCloseButtons(final JDialog dialog){
	  	  // create a new panel
	  	   JPanel canvasButtons = new JPanel();
	  		
	  	  // create two new buttons, using the makeButton method
	  		JButton menuButton = GoTermDistributionGUI.makeButton("Run");
	  		final JButton closeButton = GoTermDistributionGUI.makeButton("Exit");
	  		
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
	  						dialog.dispose();
	  						
	  						}
	  					}
	  				);
	  		
	  		// add the buttons to the canvas
	  		canvasButtons.add(menuButton);
	  		canvasButtons.add(closeButton);	
	  		
	  	  return canvasButtons;
	    }
	
	/**
	 * In this method, the information about the chosen organism is returned. This information 
	 * is necessary for opening the right directory.
	 */
	public static String[]getOrganismInfo (int organism){
		
		String[][] annimalNames=new String[6][];
		annimalNames[0]=new String[]{"Ce_20070902.pgdb","\\Caenorhabditis_elegans"};
		annimalNames[1]=new String[]{"Dr_20070817.pgdb","\\Drosophila_melanogaster"};
		annimalNames[2]=new String[]{"Hs_41_36c.pgdb","\\Homo_sapiens"};
		annimalNames[3]=new String[]{"Mm_38_35.pgdb","\\Mus_musculus"};
		annimalNames[4]=new String[]{"Rn_39_34i.pgdb","\\Rattus_norvegicus"};
		annimalNames[5]=new String[]{"Sc_41_1d.pgdb","\\Saccharomyces_cerevisiae"};
		
		String[]organismInfo=annimalNames[organism];
		
		return organismInfo;
		
	}

	
	
	
}

