import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class makeAChoice {
	
	public static void main(String[] args) {
		
		
		JPanel canvasLabel=new JPanel();
		JPanel canvasButtons=new JPanel(); //canvasButtons();
		
		JLabel label = new JLabel("Make a choice");
		canvasLabel.add(label);
		
		
		
		// create a new frame
		JFrame fr = new JFrame("Make a choice");
    
		// When click on exit, exit the frame
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		// set the size of the frame
		fr.setSize(400,300);
		
		fr.add(canvasLabel);
		fr.add(canvasButtons);
	

	
		
		
	
	
		// add the canvas to the frame
	
	
		// Show the frame
		fr.setVisible(true);
	}
}

