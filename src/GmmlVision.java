import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.dialogs.*;
import java.util.*;
//~ import java.awt.Color;

/**
 * This class is the main class in the GMML project. 
 * It acts as a container for pathwaydrawings and facilitates
 * loading, creating and saving drawings to and from GMML.
 */
class GmmlVision extends ApplicationWindow
{
	private static final long serialVersionUID = 1L;
	
	private class NewAction extends Action 
	{
		GmmlVision window;
		public NewAction (GmmlVision w)
		{
			window = w;
			setText ("&New@Ctrl+N");
			setToolTipText ("Create new drawing");
		}
		public void run () {
			createNewDrawing();
		}
	}
	private NewAction newAction = new NewAction (this);

	private class OpenAction extends Action 
	{
		GmmlVision window;
		public OpenAction (GmmlVision w)
		{
			window = w;
			setText ("&Open@Ctrl+O");
			setToolTipText ("Open Application");
		}
		public void run () 
		{
			FileDialog fd = new FileDialog(window.getShell(), SWT.OPEN);
	        // TODO: set proper file filter for xml files
			fd.setText("Open");
			// TODO: check if user pressed cancel
	        String fnMapp = fd.open();			
			openPathway(fnMapp);
		}
	}
	private OpenAction openAction = new OpenAction (this);
	
	private class SaveAction extends Action 
	{
		GmmlVision window;
		public SaveAction (GmmlVision w)
		{
			window = w;
			setText ("&Save@Ctrl+S");
			setToolTipText ("Save mapp");
		}
		public void run () {			
			// reset zoom to 100%
			
			// Overwrite the existing xml file
			document.writeToXML(document.xmlFile);
		}
	}
	private SaveAction saveAction = new SaveAction(this);
	
	private class SaveAsAction extends Action 
	{
		GmmlVision window;
		public SaveAsAction (GmmlVision w)
		{
			window = w;
			setText ("Save &As");
			setToolTipText ("Save mapp with new file name");
		}
		public void run () {
			if (drawing != null)
			{
				//~ JFileChooser chooser = new JFileChooser();
				//~ chooser.setFileFilter(new GmmlFilter());
				//~ int returnVal = chooser.showSaveDialog(null);
				//~ if(returnVal == JFileChooser.APPROVE_OPTION) 
				//~ {
					//~ String file = chooser.getSelectedFile().getPath();
					//~ if(!file.endsWith(".xml")) 
					//~ {
						//~ file = file+".xml";
					//~ }
					
					//~ int confirmed = 1;
					//~ File tempfile = new File(file);
					
					//~ if(tempfile.exists())
					//~ {
						//~ String[] options = { "OK", "CANCEL" };
						//~ confirmed = JOptionPane.showOptionDialog(null, 
								//~ "The selected file already exists, overwrite?", 
								//~ "Warning", 
								//~ JOptionPane.DEFAULT_OPTION, 
								//~ JOptionPane.WARNING_MESSAGE, null, 
								//~ options, options[0]);
					//~ } 
					//~ else
					//~ {
						//~ confirmed = 0;
					//~ }
					
					//~ if (confirmed == 0) 
					//~ {
						//~ document.writeToXML(tempfile);
						//~ System.out.println("Saved");
					//~ } else {
						//~ System.out.println("Canceled");
					//~ }
				//~ }
			} 
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gmml file loaded! Open or create a new gmml file first");
			}			
		}
	}
	private SaveAsAction saveAsAction = new SaveAsAction (this);
		
	private class CloseAction extends Action 
	{
		GmmlVision window;
		public CloseAction (GmmlVision w)
		{
			window = w;
			setText ("&Close@Ctrl+W");
			setToolTipText ("Close this map");
		}
		public void run () {
			
		}
	}
	private CloseAction closeAction = new CloseAction(this);
	
	private class ExitAction extends Action 
	{
		GmmlVision window;
		public ExitAction (GmmlVision w)
		{
			window = w;
			setText ("E&xit@Ctrl+X");
			setToolTipText ("Exit Application");
		}
		public void run () {
			window.close();
		}
	}
	private ExitAction exitAction = new ExitAction(this);

	private class PropertyAction extends Action 
	{
		GmmlVision window;
		public PropertyAction (GmmlVision w)
		{
			window = w;
			setText ("&Properties");
			setToolTipText ("View properties");
		}
		public void run () {
			if(drawing != null)
			{
				if(drawing.selectedGraphics != null)
				{
					//~ new GmmlPropertyInspector(drawing.selectedGraphics);
				}
				else
				{
					MessageDialog.openError (window.getShell(), "Error", 
						"No GMMLGraphics selected!");
				}
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gmml file loaded! Open or create a new gmml file first");
			}
		}
	}
	private PropertyAction propertyAction = new PropertyAction(this);

	private class ZoomAction extends Action 
	{
		GmmlVision window;
		int pctZoomFactor;
		
		public ZoomAction (GmmlVision w, int newPctZoomFactor)
		{
			window = w;
			pctZoomFactor = newPctZoomFactor;
			setText (pctZoomFactor + " %");
			setToolTipText ("Zoom mapp to " + pctZoomFactor + " %");
		}
		public void run () {
			if (drawing != null)
			{
				drawing.setZoom(pctZoomFactor);
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gmml file loaded! Open or create a new gmml file first");
			}						
		}
	}
		
	private class AboutAction extends Action 
	{
		GmmlVision window;
		public AboutAction (GmmlVision w)
		{
			window = w;
			setText ("&About@F1");
			setToolTipText ("about Cellular Automata");
		}
		public void run () {
			// TODO
			//~ MessageDialog.openInformation(window.getShell(), "About", "(c) 2006 by Martijn van Iersel");
			GmmlAboutBox gmmlAboutBox = new GmmlAboutBox(window.getShell(), SWT.NONE);
			gmmlAboutBox.open();
		}
	}
	private AboutAction aboutAction = new AboutAction(this);
	
	GmmlDrawing drawing;
	GmmlData document;

	public GmmlVision()
	{
		this(null);
	}
	
	/**
	 *Constructor for thGmmlVision class
	 *Initializes new GmmlVision and sets properties for frame
	 */
	public GmmlVision(Shell shell)
	{
		super(shell);
		addMenuBar();				
	}

	/**
	 * Main method which will be carried out when running the program
	 */
	public static void main(String[] args)
	{
	   GmmlVision window = new GmmlVision();
	   window.setBlockOnOpen(true);
	   window.open();
	   Display.getCurrent().dispose();
	}
	
	/**
	 *Builds and ads a menu to the GmmlVision frame
	 */
	protected MenuManager createMenuManager()
	{
		MenuManager m = new MenuManager();
		MenuManager fileMenu = new MenuManager ("&File");
		fileMenu.add(newAction);
		fileMenu.add(openAction);
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(closeAction);
		fileMenu.add(new Separator());
		fileMenu.add(exitAction);
		MenuManager editMenu = new MenuManager ("&Edit");
		editMenu.add(propertyAction);
		MenuManager viewMenu = new MenuManager ("&View");
		viewMenu.add(new ZoomAction(this, 50));
		viewMenu.add(new ZoomAction(this, 75));
		viewMenu.add(new ZoomAction(this, 100));
		viewMenu.add(new ZoomAction(this, 125));
		viewMenu.add(new ZoomAction(this, 150));
		viewMenu.add(new ZoomAction(this, 200));
		MenuManager helpMenu = new MenuManager ("&Help");
		helpMenu.add(aboutAction);
		m.add(fileMenu);
		m.add(editMenu);
		m.add(viewMenu);
		m.add(helpMenu);
		return m;
	}
			

	ScrolledComposite sc;
		
	protected Control createContents(Composite parent)
	{
		Shell shell = parent.getShell();
		shell.setSize(800, 600);
		shell.setLocation(100, 100);

		shell.setText("GmmlVision");

		sc = new ScrolledComposite (parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
		return parent;
		
	};
	
	/**
	 * Creates a new empty drawing and loads it in the frame 
	 */
	private void createNewDrawing()
	{
		GmmlDrawing d = new GmmlDrawing(sc, SWT.NONE);
		
		d.addElement(new GmmlShape(600, 200, 100, 40, GmmlShape.TYPE_RECTANGLE, new RGB (0, 0, 255), 10, d));
		d.addElement(new GmmlLine(100, 100, 200, 200, new RGB (0, 255, 0), d));
		d.addElement(new GmmlGeneProduct(200, 200, 200, 80, "this is a very long id", "ref", new RGB (255, 0, 0), d));
		d.addElement(new GmmlLineShape(300, 50, 200, 500, GmmlLineShape.TYPE_LIGAND_SQUARE, new RGB (0, 128, 0), d));
		d.addElement(new GmmlLineShape(300, 150, 200, 400, GmmlLineShape.TYPE_RECEPTOR_ROUND, new RGB (0, 128, 0), d));
		d.addElement(new GmmlLineShape(300, 250, 200, 300, GmmlLineShape.TYPE_LIGAND_ROUND, new RGB (0, 128, 0), d));
		d.addElement(new GmmlLabel(200, 50, 100, 80, "testlabel", "Arial", "bold", "italic", 10, new RGB (0, 0, 0), d));
		d.addElement(new GmmlArc(50, 50, 200, 200, new RGB (255, 0, 0), 0, d));
		d.addElement(new GmmlBrace(400, 400, 200, 60, GmmlBrace.ORIENTATION_TOP, new RGB (255, 0, 255), d));
		d.addElement(new GmmlBrace(200, 200, 200, 60, GmmlBrace.ORIENTATION_BOTTOM, new RGB (255, 0, 255), d));
		d.addElement(new GmmlBrace(400, 200, 200, 60, GmmlBrace.ORIENTATION_LEFT, new RGB (255, 0, 255), d));
		d.addElement(new GmmlBrace(200, 400, 200, 60, GmmlBrace.ORIENTATION_RIGHT, new RGB (255, 0, 255), d));
		
		sc.setContent(d);
		d.setSize(800, 600);
		
		drawing = d;
	}
	
	/**
	 * Opens a GMML representation of a pathway or reaction and creates 
	 * a scrollpane of the drawing, which is loaded in the frame.
	 */
	private void openPathway(String fnPwy)
	{
		drawing = new GmmlDrawing(sc, SWT.NONE);

		// initialize new JDOM gmml representation and read the file
		document = new GmmlData(fnPwy, drawing);
		
		sc.setContent(drawing);
	}

} // end of class