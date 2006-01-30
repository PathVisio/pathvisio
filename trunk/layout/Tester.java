import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.applet.Applet;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Tester extends JApplet{
    static protected JLabel label;
    public TestPanel testPanel;
   

/*	public void drawstep(int[][] coord, int[][] link) {
		testPanel.rectCoord = coord;
		testPanel.lines = link;
		testPanel.repaint();
	} */

    public void init(){
	 //Initialize the layout.

        //label = new JLabel("Drag the points to adjust the curve.");
        //getContentPane().add("South", label);
        testPanel.repaint();
	}
}

class TestPanel extends JPanel {
	int[][] rectCoord = new int[0][2];
	int[][] lines = new int[0][2];

	BufferedImage bi;
	Graphics2D big;
	int x, y;

	boolean firstTime = true;
	boolean pressOut = false;

	public TestPanel() {

      setBackground(Color.white);
      setSize(1000,1000);
      setPreferredSize(new Dimension(1400,1050));

		System.out.println("creating testpanel");
	}

	public void paintComponent(Graphics g){
      super.paintComponent(g);
		update(g);
	}

	public void update(Graphics g){
		
		//System.out.println("updating graphics");
		Graphics2D g2 = (Graphics2D)g;
		Dimension dim = getSize();
		int w = dim.width;
      int h = dim.height;
      int scale = 6;
                 
    	if(firstTime){
			bi = (BufferedImage)createImage(1400, 1050);
			big = bi.createGraphics();
		}
		
		for (int i=0; i<lines.length; i++) {
			big.setColor(Color.green);
			big.setStroke(new BasicStroke(1.0f));
			double x1 = (rectCoord[lines[i][0]-1][0]/scale)+3;
			double y1 = (rectCoord[lines[i][0]-1][1]/scale)+3;
			double x2 = (rectCoord[lines[i][1]-1][0]/scale)+3;
			double y2 = (rectCoord[lines[i][1]-1][1]/scale)+3;
			big.draw(new Line2D.Double(x1,y1,x2,y2));
		}
		
		// Draws and fills the newly positioned rectangle to the buffer.
		for (int i=0; i<rectCoord.length; i++) {
			big.setColor(Color.blue);
			big.setStroke(new BasicStroke(2.0f));
			Rectangle temp = new Rectangle(rectCoord[i][0]/scale,rectCoord[i][1]/scale,6,6);
			big.draw(temp);
			big.setColor(Color.orange);
			big.fill(temp);
		}
		
		
		// Draws the buffered image to the screen.
		g2.drawImage(bi, 0, 0, this);

	}

}
