import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.dialogs.*;
import org.jdom.*;
import org.jdom.output.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
	private static int ZOOM_TO_FIT = -1;
	
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
			double usedZoom = drawing.zoomFactor;
			// Set zoom to 100%
			drawing.setZoom(100);
			drawing.updateJdomElements();
			// Overwrite the existing xml file
			gmmlData.writeToXML(gmmlData.xmlFile);
			// Set zoom back
			drawing.setZoom(usedZoom);
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
			if(pctZoomFactor == ZOOM_TO_FIT) 
			{
				setText ("Zoom to fit");
				setToolTipText("Zoom mapp to fit window");
			}
			else
			{
				setText (pctZoomFactor + " %");
				setToolTipText ("Zoom mapp to " + pctZoomFactor + " %");
			}
		}
		public void run () {
			if (drawing != null)
			{
				if(pctZoomFactor == ZOOM_TO_FIT) 
				{
					Point shellSize = window.sc.getSize();
					Point drawingSize = drawing.getSize();
					pctZoomFactor = (int)Math.min(
							drawing.zoomFactor * (double)shellSize.x / drawingSize.x,
							drawing.zoomFactor * (double)shellSize.y / drawingSize.y
					);
				} 
				drawing.setZoom(pctZoomFactor);
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gmml file loaded! Open or create a new gmml file first");
			}
		}
	}
	
	private class ConvertGdbAction extends Action
	{
		GmmlVision window;
		public ConvertGdbAction (GmmlVision w)
		{
			window = w;
			setText("&Convert Gdb");
			setToolTipText ("Convert GenMAPP Gene Database to Gmml-Vision");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Open GenMAPP Gene Database");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Gene databases");
			fileDialog.setFilterExtensions(new String[] {"*.gdb","*.*"});
			fileDialog.setFilterNames(new String[] {"Gene Database","All files"});
			String fileName = fileDialog.open();
			if(fileName != null) {
				File file = new File(fileName);
				gmmlGdb.gdbFile = file;
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setMessage("Select directory to save Gmml-Vision Gene Database");
				directoryDialog.setFilterPath(file.getParent());
				String dirName = directoryDialog.open();
				if(dirName != null) {
					gmmlGdb.hdbFile = new File(dirName + File.separatorChar + 
							file.getName().substring(0,file.getName().lastIndexOf(".")));
				} else {
					gmmlGdb.hdbFile = new File(file.getParent() + File.separatorChar + 
							file.getName().substring(0,file.getName().lastIndexOf(".")));
				}
				IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Converting Gene Database",100);
						gmmlGdb.convertGdb();
						int prevProgress = 0;
						while(gmmlGdb.convertThread.progress < 100) {
							if(monitor.isCanceled()) {
								gmmlGdb.convertThread.interrupt();
								break;
							}
							if(prevProgress < gmmlGdb.convertThread.progress) {
								monitor.worked(gmmlGdb.convertThread.progress - prevProgress);
								prevProgress = gmmlGdb.convertThread.progress;
							}
						}
						monitor.done();
					}
				};
				
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, runnableWithProgress);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private ConvertGdbAction convertGdbAction = new ConvertGdbAction(this);
	
	private class SelectGdbAction extends Action
	{
		GmmlVision window;
		public SelectGdbAction(GmmlVision w)
		{
			window = w;
			setText("&Select Gdb");
			setToolTipText("Select Gene Database");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Gene Database");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Gene databases");
			fileDialog.setFilterExtensions(new String[] {"*.data","*.*"});
			fileDialog.setFilterNames(new String[] {"Gene Database","All files"});
			String file = fileDialog.open();
			if(file != null) {
				if(gmmlGdb.selectGdb(new File(file.substring(0,file.lastIndexOf("."))))) {
					setStatus("Using Gene Database: '" + gmmlGdb.props.getProperty("currentGdb") + "'");
				} else {
					MessageBox messageBox = new MessageBox(getShell(),
							SWT.ICON_ERROR| SWT.OK);
					messageBox.setMessage("Failed to load '" + file + "'");
					messageBox.setText("Error");
					messageBox.open();
				}
			}
		}
	}
	private SelectGdbAction selectGdbAction = new SelectGdbAction(this);
	
	private class LoadGexAction extends Action
	{
		GmmlVision window;
		public LoadGexAction(GmmlVision w)
		{
			window = w;
			setText("&Load gex");
			setToolTipText("Load Expression Data");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Expression Dataset");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Expression Datasets");
			fileDialog.setFilterExtensions(new String[] {"*.gex","*.*"});
			fileDialog.setFilterNames(new String[] {"Expression Dataset","All files"});
			String file = fileDialog.open();
			if(file != null) {
				gmmlGdb.loadGex(new File(file));
			}
		}
	}
	private LoadGexAction loadGexAction = new LoadGexAction(this);
	
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
	
	protected StatusLineManager createStatusLineManager() {
		return super.createStatusLineManager();
	}
	
	GmmlDrawing drawing;
	GmmlData gmmlData;
	GmmlGdb gmmlGdb = new GmmlGdb();
	
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
		addStatusLine();
		addToolBar(SWT.FLAT);
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
		viewMenu.add(new ZoomAction(this, ZOOM_TO_FIT)); //Zoom to fit
		MenuManager dataMenu = new MenuManager ("&Data");
		dataMenu.add(convertGdbAction);
		dataMenu.add(selectGdbAction);
		dataMenu.add(loadGexAction);
		MenuManager helpMenu = new MenuManager ("&Help");
		helpMenu.add(aboutAction);
		m.add(fileMenu);
		m.add(editMenu);
		m.add(viewMenu);
		m.add(dataMenu);
		m.add(helpMenu);
		return m;
	}
			
	ScrolledComposite sc;
	GmmlBpBrowser bpBrowser;
	
//	ToolItem sampleSelector;
	
	protected Control createContents(Composite parent)
	{
		Shell shell = parent.getShell();
		shell.setSize(800, 600);
		shell.setLocation(100, 100);

		shell.setText("GmmlVision");
		
// TODO: toolbar with criteria selector
//		Composite topComposite = new Composite(parent, SWT.NULL);
//		GridLayout gridLayout = new GridLayout();
//		gridLayout.verticalSpacing = 2;
//		topComposite.setLayout(gridLayout);
		
//		ToolBar toolBar = new ToolBar(topComposite, SWT.FLAT);
//		sampleSelector = new ToolItem(toolBar, SWT.DROP_DOWN);
//		sampleSelector.setToolTipText("Choose sample to color genes");
//		toolBar.pack();
		
		Composite viewComposite = new Composite(parent, SWT.NULL);
		viewComposite.setLayout(new FillLayout());
//		GridData gridData = new GridData(GridData.FILL_BOTH);
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.grabExcessVerticalSpace = true;
//		viewComposite.setLayoutData(gridData);
		
		SashForm sashForm = new SashForm(viewComposite, SWT.HORIZONTAL);
		
		sc = new ScrolledComposite (sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setFocus(); //To enable scrolling with mouse wheel
		
		bpBrowser = new GmmlBpBrowser(sashForm, SWT.NONE);
		
		sashForm.setWeights(new int[] {80, 20});
		setStatus("Using Gene Database: '" + gmmlGdb.props.getProperty("currentGdb") + "'");
		
		return parent;
		
	};
	
	/**
	 * Creates a new empty drawing and loads it in the frame 
	 */
	private void createNewDrawing()
	{
		GmmlDrawing d = new GmmlDrawing(sc, SWT.NONE);
		
		gmmlData = new GmmlData(d);
		
		d.setBrowser(bpBrowser);
		
		d.addElement(new GmmlShape(600, 200, 100, 40, GmmlShape.TYPE_RECTANGLE, new RGB (0, 0, 255), 10, d, gmmlData.doc));
		d.addElement(new GmmlLine(100, 100, 200, 200, new RGB (0, 255, 0), d, gmmlData.doc));
		d.addElement(new GmmlGeneProduct(200, 200, 200, 80, "this is a very long id", "ref", new RGB (255, 0, 0), d, gmmlData.doc));
		d.addElement(new GmmlLineShape(300, 50, 200, 500, GmmlLineShape.TYPE_LIGAND_SQUARE, new RGB (0, 128, 0), d, gmmlData.doc));
		d.addElement(new GmmlLineShape(300, 150, 200, 400, GmmlLineShape.TYPE_RECEPTOR_ROUND, new RGB (0, 128, 0), d, gmmlData.doc));
		d.addElement(new GmmlLineShape(300, 250, 200, 300, GmmlLineShape.TYPE_LIGAND_ROUND, new RGB (0, 128, 0), d, gmmlData.doc));
		d.addElement(new GmmlLabel(200, 50, 100, 80, "testlabel", "Arial", "bold", "italic", 10, new RGB (0, 0, 0), d, gmmlData.doc));
		d.addElement(new GmmlArc(50, 50, 200, 200, new RGB (255, 0, 0), 0, d, gmmlData.doc));
		d.addElement(new GmmlBrace(400, 400, 200, 60, GmmlBrace.ORIENTATION_TOP, new RGB (255, 0, 255), d, gmmlData.doc));
		d.addElement(new GmmlBrace(200, 200, 200, 60, GmmlBrace.ORIENTATION_BOTTOM, new RGB (255, 0, 255), d, gmmlData.doc));
		d.addElement(new GmmlBrace(400, 200, 200, 60, GmmlBrace.ORIENTATION_LEFT, new RGB (255, 0, 255), d, gmmlData.doc));
		d.addElement(new GmmlBrace(200, 400, 200, 60, GmmlBrace.ORIENTATION_RIGHT, new RGB (255, 0, 255), d, gmmlData.doc));
		
		sc.setContent(d);
		d.setSize(800, 600);
		
		drawing = d;
		
		XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
		try {
			xmlcode.output(gmmlData.doc,System.out);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens a GMML representation of a pathway or reaction and creates 
	 * a scrollpane of the drawing, which is loaded in the frame.
	 */
	private void openPathway(String fnPwy)
	{
		drawing = new GmmlDrawing(sc, SWT.NONE);
		drawing.setBrowser(bpBrowser);
		
		// initialize new JDOM gmml representation and read the file
		gmmlData = new GmmlData(fnPwy, drawing);
		sc.setContent(drawing);
		
	}

} // end of class