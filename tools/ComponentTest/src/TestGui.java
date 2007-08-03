import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;


public class TestGui extends TestMain {
	JFrame frame;
	public TestGui(int w, int h, int nr) {
		super(w, h, nr);

		frame = new JFrame("Component test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());
		
		content.add(panel);
		frame.setSize(w, h);
	}
}
