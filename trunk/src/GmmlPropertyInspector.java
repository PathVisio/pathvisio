import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * This class provides a property inspector
 * box for GmmlGraphics objects.
 */
public class GmmlPropertyInspector 
{
	private static final long serialVersionUID = 1L;
	
	GmmlGraphics g;
	JTable properties;
	
	/**
	 * Constructor for this class
	 * @param g - the GmmlGraphics object to get 
	 * the properties from
	 */
	public GmmlPropertyInspector(GmmlGraphics g)
	{
		JFrame f = new JFrame("Property Inspector");
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setLayout(new BorderLayout());
		f.setLocation(200, 200);
		
		this.g = g;		
		properties = g.getPropertyTable();
		
		int nofcols = properties.getColumnCount();
		f.setSize(nofcols*60, 150);
		
		JScrollPane s = new JScrollPane(properties);
		
		s.setBackground(Color.gray);
		s.setVerticalScrollBar(s.createVerticalScrollBar());
		s.setHorizontalScrollBar(s.createHorizontalScrollBar());
		s.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		s.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		f.add(s, BorderLayout.CENTER);
		
		JToolBar t = createToolbar();
		f.getContentPane().add(t, BorderLayout.NORTH);
		
		f.setVisible(true);
	}
	
	private JToolBar createToolbar()
	{
		JToolBar t	= new JToolBar();
		JButton b	= new JButton("Apply!");
		final GmmlGraphics gg = this.g;
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					gg.updateFromPropertyTable(properties);
				}
			}
		);
		
		t.add(b);
		return t;
	}


	

	

	

	

	

}
