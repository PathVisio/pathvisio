import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


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
		JPanel canvas = new JPanel();
		
		// create a new button
		JButton calcButton = new JButton("Calculate");
		
		// set the size of the button
		calcButton.setBounds(10,30,104,150);
		
		// center the text (horizontally and vertically) in the button
		calcButton.setVerticalTextPosition(AbstractButton.CENTER);
		calcButton.setHorizontalTextPosition(AbstractButton.CENTER);
		
	       
        JLabel label = new JLabel("Label");
        canvas.add(label);
		
		
		// add the button to the canvas
		canvas.add(calcButton);
		
		// add the canvas to the frame
		frame.add(canvas);

		
		// Show the frame
		frame.setVisible(true);
	}

}
