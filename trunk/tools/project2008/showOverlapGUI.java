
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.*;
import org.pathvisio.data.DataException;
import org.pathvisio.model.ConverterException;
import java.io.File;
import java.util.List;
import RowHeaderTable.JScrollPaneAdjuster;
import RowHeaderTable.JTableRowHeaderResizer;
import RowHeaderTable.RowHeaderRenderer;


public class showOverlapGUI{

  public static void main(String[] args) throws DataException, ConverterException{
	  
	  /**
		* in the String[] args, 2 arguments are given:
		* in example:
		* "C:\\databases\\"
		* "C:\pathways"
		* 
		* The first one is the directory that contains the databases.
		* The second one is the directory that contains the pathway cache.
		*/ 
	  
	  final String[]arguments=args;
	  String dbDir=null;
	  File pwDir=null;
	  String outfile=null;
		
	  try {
		  dbDir = new String(args[0]+"Rn_39_34i.pgdb");
		  pwDir = new File(args[1]+"\\Rattus_norvegicus");
		  outfile=args[2];
		  
		  javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowOverlapGUI(arguments);
				}
			});

	  }
	  catch(ArrayIndexOutOfBoundsException e) {
		  System.out.println("String[] args not given!");
		  System.exit(0);
	  }

	   
	  
	 
      
  }
  
  public static void createAndShowOverlapGUI(String[] arguments){
	// create a new panel
	   JPanel canvasButtons=getCanvasButtons(arguments);
	 		
		// create a new panel
		JPanel canvasTable = new JPanel();
		JScrollPane scrollPane =getTable(arguments);
       canvasTable.add(scrollPane);
		
		
		
		
     
     
     
     JFrame f = new JFrame("Row Header Test");
     
     f.add(canvasButtons, BorderLayout.SOUTH);
     f.add(canvasTable, BorderLayout.NORTH);
     
     f.pack();
     f.setLocation(200, 100);
     f.setVisible(true);
 
  }
  
  public static JScrollPane getTable(String[] arguments){
	  
	  final String dbDirRn = new String(arguments[0]+"Rn_39_34i.pgdb");
	  final File pwDirRn = new File(arguments[1]+"\\Rattus_norvegicus");
	  
	  List<File> filenames = FileUtils.getFileListing(pwDirRn, ".gpml");
	  String[] columnNames=new String[filenames.size()];
      for(int i=0;i<filenames.size();i++){
    	  columnNames[i]=filenames.get(i).getName();
      }
      
      Double[][] data;
	try {
		data = GeneCounter.getOverlap(dbDirRn,pwDirRn);
		
	} catch (DataException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		data=null;
	} catch (ConverterException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		data=null;
	}
   
      DefaultTableModel headerData = new DefaultTableModel(0, 1);
          
      
      for(int i=0;i<filenames.size();i++){
    	  headerData.addRow(new Object[] {columnNames[i]});
      }
      
      //JTable table=getTable(data,columnNames);
      JTable table = new JTable(data,columnNames);
      JTable rowHeader = new JTable(headerData);

      LookAndFeel.installColorsAndFont
          (rowHeader, "TableHeader.background", 
          "TableHeader.foreground", "TableHeader.font");

      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      
      rowHeader.setIntercellSpacing(new Dimension(0, 0));
      Dimension d = rowHeader.getPreferredScrollableViewportSize();
      d.width = rowHeader.getPreferredSize().width;
      rowHeader.setPreferredScrollableViewportSize(d);
      rowHeader.setRowHeight(table.getRowHeight());
      rowHeader.setDefaultRenderer(Object.class, new RowHeaderRenderer());
      
      JScrollPane scrollPane = new JScrollPane(table);
      
      scrollPane.setRowHeaderView(rowHeader);

      JTableHeader corner = rowHeader.getTableHeader();
      corner.setReorderingAllowed(false);
      corner.setResizingAllowed(false);

      scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);

      new JScrollPaneAdjuster(scrollPane);

      new JTableRowHeaderResizer(scrollPane).setEnabled(true); 
	  
      return scrollPane;
  }
  
  public static JPanel getCanvasButtons(final String[] arguments){
	  // create a new panel
	   JPanel canvasButtons = new JPanel();
		
	  // create two new buttons, using the makeButton method
		JButton menuButton = TestFrames.makeButton("menu");
		JButton closeButton = TestFrames.makeButton("Close");
		
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
						showMenuGUI.createAndShowMenuGUI(arguments);
						System.out.println("Go to Menu");
						}
					}
				);
		
		// add the buttons to the canvas
		canvasButtons.add(menuButton);
		canvasButtons.add(closeButton);	
		
	  return canvasButtons;
  }
  
  
}
