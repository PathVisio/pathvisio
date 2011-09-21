import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.PrintWriter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

class Main extends JPanel
{
	static final String KITTY =
		"M 487.28125,410.34375 C 467.92059,410.36864 439.49832,431.6392 413.1875,467.84375 C 396.60478,461.52191 378.68892,458.0625 360,458.0625 C 338.39321,458.0625 317.83036,462.70264 299.15625,471.03125 C 259.62049,420.76245 215.60001,399.09165 199.3125,422.71875 C 185.00239,443.47738 196.69397,492.53451 225.3125,540.03125 C 212.84087,563.12076 205.71875,589.7321 205.71875,618.0625 C 205.71875,706.3825 274.83428,778.0625 360,778.0625 C 445.16572,778.0625 514.28127,706.3825 514.28125,618.0625 C 514.28125,584.03715 504.01389,552.50141 486.53125,526.5625 C 508.72684,482.55443 517.03884,439.11209 504.84375,419.90625 C 500.7162,413.40584 494.65755,410.33427 487.28125,410.34375 z M 325.12425,640.06448 C 353.69568,682.92163 359.40997,680.06448 359.40997,680.06448 L 385.12425,642.92163 L 325.12425,640.06448 z M 310.40619,626.076 L 264.46956,611.56795M 310.41802,648.94498 L 273.02916,645.84183M 310.40463,660.14094 L 275.8997,683.2173M 399.2988,619.52317 L 445.23543,605.01512M 399.28697,642.39215 L 436.67583,639.289M 399.30036,653.58811 L 433.80529,676.66447M 295.58816,549.02042 C 293.63331,554.51846 291.42896,562.73008 291.68191,571.45792 C 291.8879,578.56582 293.69499,584.69073 295.58816,589.20792 C 298.00617,589.8241 300.42704,590.20424 302.80691,590.42667 C 304.75417,584.84073 306.95769,576.5828 306.71316,568.14542 C 306.53582,562.02648 305.16028,556.58171 303.58816,552.27042 C 301.02826,550.84095 298.31987,549.79095 295.58816,549.02042 z M 326.53799,589.22837 C 326.66283,589.18334 302.92317,596.23658 286.30056,585.52101 C 269.67794,574.80543 266.52585,550.76252 266.31111,550.40381 C 266.0216,549.9202 289.92592,543.39559 306.54855,554.11117 C 323.17116,564.82674 326.644,590.3832 326.53799,589.22837 z M 415.71707,548.19116 C 417.67192,553.6892 419.87627,561.90082 419.62332,570.62866 C 419.41733,577.73656 417.61024,583.86147 415.71707,588.37866 C 413.29906,588.99484 410.87819,589.37498 408.49832,589.59741 C 406.55106,584.01147 404.34754,575.75354 404.59207,567.31616 C 404.76941,561.19722 406.14495,555.75245 407.71707,551.44116 C 410.27697,550.01169 412.98536,548.96169 415.71707,548.19116 z M 384.76724,588.39911 C 384.6424,588.35408 408.38206,595.40732 425.00467,584.69175 C 441.62729,573.97617 444.77938,549.93326 444.99412,549.57455 C 445.28363,549.09094 421.37931,542.56633 404.75668,553.28191 C 388.13407,563.99748 384.66123,589.55394 384.76724,588.39911 z ";
	static final String SIMPLIFIED[] =
	{
		"M 227.0, 139.0 C 227.0, 155.0 220.0, 169.0 211.0, 169.0 C 202.0, 169.0 194.0, 155.0 194.0, 139.0 C 194.0, 123.0 202.0, 109.0 211.0, 109.0 C 220.0, 109.0 227.0, 123.0 227.0, 139.0 z ",
		"M 220.0, 94.0 C 214.0, 112.0 204.0, 123.0 197.0, 119.0 C 191.0, 115.0 190.0, 97.0 196.0, 80.0 C 202.0, 62.0 212.0, 51.0 219.0, 55.0 C 225.0, 59.0 226.0, 77.0 220.0, 94.0 z M 240.0, 63.0 C 225.0, 116.0 203.0, 156.0 189.0, 152.0 C 176.0, 147.0 177.0, 101.0 192.0, 47.0 C 207.0, -6.0 230.0, -46.0 243.0, -41.0 C 256.0, -37.0 255.0, 10.0 240.0, 63.0 z ",
		"M 220.0, 94.0 C 214.0, 112.0 204.0, 123.0 197.0, 119.0 C 191.0, 115.0 190.0, 97.0 196.0, 80.0 C 202.0, 62.0 212.0, 51.0 219.0, 55.0 C 225.0, 59.0 226.0, 77.0 220.0, 94.0 z M 240.0, 63.0 C 225.0, 116.0 203.0, 156.0 189.0, 152.0 C 176.0, 147.0 177.0, 101.0 192.0, 47.0 C 207.0, -6.0 230.0, -46.0 243.0, -41.0 C 256.0, -37.0 255.0, 10.0 240.0, 63.0 z "
	};

	static final String PATH[] =
	{
		"M 226.8463,139.00732 C 226.8463,155.48353 219.58722,168.85552 " +
        "210.64299,168.85552 C 201.69876,168.85552 194.43968,155.48353 " +
        "194.43968,139.00732 C 194.43968,122.53112 201.69876,109.15913 " +
        "210.64299,109.15913 C 219.58722,109.15913 226.8463,122.53112 " +
        "226.8463,139.00732 z",

		"M 219.8743,994.27966 C 213.94517,1011.9431 203.82434,1022.9856 " +
        "197.28316,1018.9282 C 190.74198,1014.8708 190.24524,997.24235 " +
        "196.17437,979.57895 C 202.10349,961.91556 212.22432,950.873 " +
        "218.7655,954.9304 C 225.30669,958.9878 225.80343,976.61627 " +
        "219.8743,994.27966 z M 240.08057,962.90051 C " +
        "225.37358,1016.1844 202.712,1055.9778 189.49669,1051.725 C " +
        "176.28138,1047.4723 177.49203,1000.7759 192.19902,947.492 C " +
        "206.90601,894.20812 229.56758,854.41473 242.78289,858.66748 C " +
        "255.9982,862.92023 254.78756,909.61663 240.08057,962.90051 z",

		"M 462.19521,1121.3044 C 462.19521,1132.4343 452.83856,1141.4672 " +
        "441.30983,1141.4672 C 429.7811,1141.4672 420.42446,1132.4343 " +
        "420.42446,1121.3044 C 420.42446,1110.1746 429.7811,1101.1417 " +
        "441.30983,1101.1417 C 452.83856,1101.1417 462.19521,1110.1746 " +
        "462.19521,1121.3044 z M 464.99209,1121.3044 C " +
        "464.99209,1133.933 454.38246,1144.1822 441.30988,1144.1822 C " +
        "428.2373,1144.1822 417.62766,1133.933 417.62766,1121.3044 C " +
        "417.62766,1108.6758 428.2373,1098.4266 441.30988,1098.4266 C " +
        "454.38246,1098.4266 464.99209,1108.6758 464.99209,1121.3044 z",

		"M 533.85634 676.2749 A 40.081867 188.47005 0 1 1 453.69261,676.2749 A " +
        "40.081867 188.47005 0 1 1 533.85634 676.2749 z",

		"M 554.58627,1033.4711 C 545.84018,1006.4858 540.3078,951.48969 " +
        "540.3078,907.75011 C 540.3078,865.14636 546.15128,800.64477 " +
        "554.85453,772.6508 C 555.2037,764.15253 565.93866,752.66995 " +
        "578.05172,752.66995 C 589.35968,752.66995 599.6922,762.53754 " +
        "599.6922,775.19331 C 598.99356,787.91976 589.60241,798.32872 " +
        "572.15745,796.12265 C 563.36386,822.66692 562.53878,854.18071 " +
        "562.53878,896.29383 C 562.53878,939.52962 563.16536,979.27862 " +
        "572.16222,1006.1756 C 589.89243,1006.2335 599.5934,1017.4158 " +
        "599.5934,1030.4534 C 599.5934,1042.2798 585.80704,1052.1547 " +
        "576.2186,1052.0536 C 566.61166,1051.9563 554.98881,1040.8563 " +
        "554.58627,1033.4711 z"
	};

	class PathPane extends JPanel
	{
		int xco, yco;

		public void paintPath (Graphics2D target, String pathString)
		{
			try
			{
				ShapeEmitter em = new ShapeEmitter();
				Parser p = new Parser(pathString, em);
				p.parse ();
				Shape sh = em.getShape();
			    Rectangle r = sh.getBounds();
				AffineTransform x = AffineTransform.getTranslateInstance (-r.x + xco, -r.y + yco);
				Shape sh2 = x.createTransformedShape (sh);
				target.draw (sh2);
				xco += r.width;
			}
			catch (PathParseException e)
			{
				e.printStackTrace();
			}
		}

		public void paintComponent (Graphics g)
		{
			xco = 0;
			yco = 0;
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor (Color.BLACK);
			g2d.setStroke (new BasicStroke (2));
			paintPath (g2d, PATH[0]);
			paintPath (g2d, PATH[1]);
			paintPath (g2d, PATH[2]);
			paintPath (g2d, PATH[3]);
			paintPath (g2d, PATH[4]);
		}
	}

	public Main()
	{
		super ();

		JPanel drawingPane = new PathPane();
		drawingPane.setBackground (Color.WHITE);
		drawingPane.setSize (1000, 1000);

		JScrollPane scroller = new JScrollPane(drawingPane);
		scroller.setPreferredSize (new Dimension (800, 600));

		add (scroller);
	}

	public void printPath(String pathString)
	{
		try
		{
			ShapeEmitter em = new ShapeEmitter();
			Parser p = new Parser(pathString, em);
			p.parse ();
			Shape sh = em.getShape();
			Rectangle r = sh.getBounds();
			JavaEmitter em2 = new JavaEmitter (new PrintWriter (System.out), null);
			em2.setOffset (r.x, r.y);
			em2.setFormat ("%.2ff");
			p = new Parser(pathString, em2);
			p.parse ();
		}
		catch (PathParseException pe)
		{
			pe.printStackTrace();
		}
	}

	public void run()
	{
// 		printPath (PATH[0]);
// 		printPath (PATH[1]);
// 		printPath (PATH[2]);
// 		printPath (PATH[3]);
// 		printPath (PATH[4]);
		printPath (KITTY);
	}

	public static void printUsage()
	{
		System.out.println (
			"path2visio\n" +
			"Creates a series of java statements based on an svg path/p attribute.\n\n" +
			"Usage:\n"+
			"    path2visio \"PATH DATA\"\n");
	}

	public static void main(String[] argv)
	{
		if (argv.length > 0)
		{
			Main m = new Main();
			m.printPath(argv[0]);
		}
		else
		{
			printUsage();
		}
/*
		JFrame frame = new JFrame("Path Parser Demo");
		frame.setSize (1000, 1000);

		JComponent newContentPane = new Main();
		newContentPane.setOpaque(true);
		frame.setContentPane (newContentPane);

		frame.pack();
		frame.setVisible (true);
*/
    }

}