import java.awt.*;
import java.awt.event.*;

public class BigCat extends Frame
{
	public BigCat()
	{
		this.addWindowListener	(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				dispose();
				System.exit(0);
			}
		});
	}
	
	public static void main(String args[])
	{
		System.out.println("Starting App");
		BigCat f = new BigCat();
		f.setSize(100,100);
		f.show();
	}
}