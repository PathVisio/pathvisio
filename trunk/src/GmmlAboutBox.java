import java.awt.Color;
import java.awt.Container;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JFrame;




public class GmmlAboutBox
{
	
	private static final long serialVersionUID = 1L;

	public GmmlAboutBox() 
	{
		JFrame f = new JFrame("About GMML-Vision");
		f.setSize(325, 180);

		Container content = f.getContentPane();
		content.setBackground(Color.black);

		//Add the label
		String text = "<html><center><h1>GMML-Vision</h1>H.C. Achterberg<br>" +
				"R.M.H. Besseling<br>S.P.M.Crijns<br>I.Kaashoek<br>M.M.Palm<br>" +
				"E.D Pelgrim<br>BiGCaT<br><center>";
		ImageIcon logo = new ImageIcon("images/logo.jpg", "GMML-Vision Logo");
		JLabel label = new JLabel(text, logo, JLabel.CENTER);
		label.setFont(new Font("Arial", Font.BOLD, 12));
		label.setForeground(new Color(0x88AAFF));
		
		f.getContentPane().add(label);
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		f.setVisible(true);
	}
}
