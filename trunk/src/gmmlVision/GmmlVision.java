package gmmlVision;

import gmmlVision.sidepanels.TabbedSidePanel;
import graphics.GmmlDrawing;
import graphics.GmmlGeneProduct;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import search.PathwaySearchComposite;
import search.SearchMethods;
import colorSet.ColorSetWindow;
import data.GmmlData;
import data.GmmlGdb;
import data.GmmlGex;
import data.ImportExprDataWizard;
import debug.Logger;


/**
 * This class is the main class in the GMML project. 
 * It acts as a container for pathwaydrawings and facilitates
 * loading, creating and saving drawings to and from GMML.
 */
public class GmmlVision extends ApplicationWindow
{
	private static final long serialVersionUID = 1L;
	private static int ZOOM_TO_FIT = -1;
	
	public static Logger log;
	
	/**
	 * {@link Action} to create a new gmml pathway
	 */
	private class NewAction extends Action 
	{
		GmmlVision window;
		public NewAction (GmmlVision w)
		{
			window = w;
			setText ("&New mapp@Ctrl+N");
			setToolTipText ("Create new mapp");
			setImageDescriptor(ImageDescriptor.createFromFile(null,"icons/new.gif"));
		}
		public void run () {
			createNewDrawing();
		}
	}
	private NewAction newAction = new NewAction (this);
	
	/**
	 * {@link Action} to open an gmml pathway
	 */
	private class OpenAction extends Action 
	{
		GmmlVision window;
		public OpenAction (GmmlVision w)
		{
			window = w;
			setText ("&Open mapp@Ctrl+O");
			setToolTipText ("Open mapp");
			setImageDescriptor(ImageDescriptor.createFromFile(null,"icons/open.gif"));
		}
		public void run () 
		{
			FileDialog fd = new FileDialog(window.getShell(), SWT.OPEN);
			fd.setText("Open");
			fd.setFilterPath("C:\\GenMAPP 2 Data\\MAPPs");
			fd.setFilterExtensions(new String[] {"*.xml","*.*"});
			fd.setFilterNames(new String[] {"Gmml file", "All files"});
	        String fnMapp = fd.open();
	        // Only open pathway if user selected a file
	        if(fnMapp != null) { openPathway(fnMapp); }
		}
	}
	private OpenAction openAction = new OpenAction (this);
	
	/**
	 * {@link Action} to save a gmml pathway
	 */
	private class SaveAction extends Action 
	{
		GmmlVision window;
		public SaveAction (GmmlVision w)
		{
			window = w;
			setText ("&Save mapp@Ctrl+S");
			setToolTipText ("Save mapp");
			setImageDescriptor(ImageDescriptor.createFromFile(null,"icons/save.gif"));
		}
		
		public void run () {
			double usedZoom = drawing.getZoomFactor() * 100;
			// Set zoom to 100%
			drawing.setPctZoom(100);
			drawing.updateJdomElements();
			// Overwrite the existing xml file
			if (gmmlData.getXmlFile() != null)
			{
				gmmlData.writeToXML(gmmlData.getXmlFile());
			}
			else
			{
				saveAsAction.run();
			}
			// Set zoom back
			drawing.setPctZoom(usedZoom);
		}
	}
	private SaveAction saveAction = new SaveAction(this);
	
	/**
	 * {@link Action} to save a gmml pathway to a file specified by the user
	 */
	private class SaveAsAction extends Action 
	{
		GmmlVision window;
		public SaveAsAction (GmmlVision w)
		{
			window = w;
			setText ("Save mapp &As");
			setToolTipText ("Save mapp with new file name");
		}
		public void run () {
			// Check if a gmml pathway is loaded
			if (drawing != null)
			{
				FileDialog fd = new FileDialog(window.getShell(), SWT.SAVE);
				fd.setText("Save");
				fd.setFilterExtensions(new String[] {"*.xml","*.*"});
				fd.setFilterNames(new String[] {"Gmml file", "All files"});
				String fileName = fd.open();
				// Only proceed if user selected a file
				if(fileName == null) return;
				// Append .xml extension if not already present
				if(!fileName.endsWith(".xml")) fileName += ".xml";
				
				File checkFile = new File(fileName);
				boolean confirmed = true;
				// If file exists, ask overwrite permission
				if(checkFile.exists())
				{
					confirmed = MessageDialog.openQuestion(window.getShell(),"",
					"File already exists, overwrite?");
				}
				if(confirmed)
				{
					double usedZoom = drawing.getZoomFactor() * 100;
					// Set zoom to 100%
					drawing.setPctZoom(100);
					drawing.updateJdomElements();
					// Overwrite the existing xml file
					gmmlData.writeToXML(checkFile);
					// Set zoom back
					drawing.setPctZoom(usedZoom);
				}
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gmml file loaded! Open or create a new gmml file first");
			}			
		}
	}
	private SaveAsAction saveAsAction = new SaveAsAction (this);
		
	/**
	 * {@link Action} to close the gmml pathway (does nothing yet)
	 */
	private class CloseAction extends Action 
	{
		GmmlVision window;
		public CloseAction (GmmlVision w)
		{
			window = w;
			setText ("&Close mapp@Ctrl+W");
			setToolTipText ("Close this map");
		}
		public void run () {
			//TODO: unload drawing, ask to save
		}
	}
	private CloseAction closeAction = new CloseAction(this);
	
	/**
	 * {@link Action} to exit the application
	 */
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
			//TODO: ask to save pathway if content is changed
		}
	}
	private ExitAction exitAction = new ExitAction(this);

	
//	private class PropertyAction extends Action 
//	{
//		GmmlVision window;
//		public PropertyAction (GmmlVision w)
//		{
//			window = w;
//			setText ("&Properties");
//			setToolTipText ("View properties");
//		}
//		public void run () {
//			if(drawing != null)
//			{
//				if(drawing.selectedGraphics != null)
//				{
//					//~ new GmmlPropertyInspector(drawing.selectedGraphics);
//				}
//				else
//				{
//					MessageDialog.openError (window.getShell(), "Error", 
//						"No GMMLGraphics selected!");
//				}
//			}
//			else
//			{
//				MessageDialog.openError (window.getShell(), "Error", 
//					"No gmml file loaded! Open or create a new gmml file first");
//			}
//		}
//	}
//	private PropertyAction propertyAction = new PropertyAction(this);

	/**
	 * {@link Action} that zooms a mapp to the specified zoomfactor
	 */
	private class ZoomAction extends Action 
	{
		GmmlVision window;
		int pctZoomFactor;
		
		/**
		 * Constructor for this class
		 * @param w {@link GmmlVision} window this action belongs to
		 * @param newPctZoomFactor the zoom factor as percentage of original
		 */
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
				double newPctZoomFactor = pctZoomFactor;
				if(pctZoomFactor == ZOOM_TO_FIT) 
				{
					Point shellSize = window.sc.getSize();
					Point drawingSize = drawing.getSize();
					newPctZoomFactor = (int)Math.min(
							drawing.getZoomFactor() * 100 * (double)shellSize.x / drawingSize.x,
							drawing.getZoomFactor() * 100 * (double)shellSize.y / drawingSize.y
					);
				} 
				drawing.setPctZoom(newPctZoomFactor);
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gmml file loaded! Open or create a new gmml file first");
			}
		}
	}
	
	/**
	 * {@link Action} to select a Gene Database
	 */
	private class SelectGdbAction extends Action
	{
		GmmlVision window;
		public SelectGdbAction(GmmlVision w)
		{
			window = w;
			setText("Select &Gene Database");
			setToolTipText("Select Gene Database");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Gene Database");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Gene databases");
			fileDialog.setFilterExtensions(new String[] {"*.properties","*.*"});
			fileDialog.setFilterNames(new String[] {"Gene Database","All files"});
			String file = fileDialog.open();
			// Only proceed if user selected a file
			if(file == null) return;
			// Connect returns null when connection is established
			try {
				gmmlGdb.connect(new File(file));
				setStatus("Using Gene Database: '" + gmmlGdb.getProps().getProperty("currentGdb") + "'");
				cacheExpressionData();
			} catch(Exception e) {
				MessageDialog.openError(getShell(), "Failed to open Gene Database", e.getMessage());
			}
		}
	}
	private SelectGdbAction selectGdbAction = new SelectGdbAction(this);
	
	/**
	 * {@link Action} to select an expression dataset
	 */
	private class SelectGexAction extends Action
	{
		GmmlVision window;
		public SelectGexAction(GmmlVision w)
		{
			window = w;
			setText("Select &Expression Data");
			setToolTipText("Select Expression Data");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Expression Dataset");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Expression Datasets");
			fileDialog.setFilterExtensions(new String[] {"*.properties","*.*"});
			fileDialog.setFilterNames(new String[] {"Expression Dataset","All files"});
			String file = fileDialog.open();
			// Only proceed if user selected a file
			if(file == null) return;
			gmmlGex.gexFile = new File(file);
			try {
				gmmlGex.connect();
				cacheExpressionData();
				showColorSetActionsCI(true);
			} catch(Exception e) {
				MessageDialog.openError(getShell(), "Failed to open Expression Dataset", e.getMessage());
			}
			
		}
	}
	private SelectGexAction selectGexAction = new SelectGexAction(this);
	
	/**
	 * Loads expression data for all {@link GmmlGeneProduct}s in the loaded pathway
	 */
	private void cacheExpressionData()
	{
		if(drawing != null)
		{
			//Check for neccesary connections
			if(gmmlGex.con != null && gmmlGdb.getCon() != null)
			{
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, gmmlGex.createCacheRunnable(drawing.getMappIds(), drawing.getSystemCodes()));
					drawing.redraw();
				} catch(Exception e) {
					GmmlVision.log.error("while caching expression data: " + e.getMessage(), e);
				}
			}
		}
	}
	
	/**
	 * {@link Action} that opens an {@link ImportExprDataWizard} that guides the user
	 * through the steps required to create a new
	 * expression dataset
	 */
	private class CreateGexAction extends Action
	{
		GmmlVision window;
		public CreateGexAction(GmmlVision w)
		{
			window = w;
			setText("&Create new Expression Dataset");
			setToolTipText("Create a new Expression Dataset from a tab delimited text file");
		}
		
		public void run() {
			if(gmmlGdb.getCon() == null)
			{
				MessageDialog.openWarning(getShell(), "Warning", "No gene database selected, " +
						"select gene database before creating a new expression dataset");
				return;
			}
			WizardDialog dialog = new WizardDialog(getShell(), new ImportExprDataWizard(window));
			dialog.setBlockOnOpen(true);
			dialog.open();
		}
	}
	private CreateGexAction createGexAction = new CreateGexAction(this);
	
	/**
	 *{@link Action} to start conversion of a GenMAPP gex to an expression database in
	 *hsqldb format
	 */
	private class ConvertGexAction extends Action
	{
		GmmlVision window;
		public ConvertGexAction(GmmlVision w)
		{
			window = w;
			setText("&Gex to Gmml-Vision");
			setToolTipText("Convert from GenMAPP 2 Gex to Gmml-Vision Expression Data");
		}
		
		public void run () {
			File gexFile = null;
			File gmGexFile = null;
			
			// Initialize filedialog to open GenMAPP gex
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Expression Dataset to convert");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Expression Datasets");
			fileDialog.setFilterExtensions(new String[] {"*.gex","*.*"});
			fileDialog.setFilterNames(new String[] {"Expression Dataset","All files"});
			String file = fileDialog.open();
			// Only proceed if user selected a file
			if(file == null) return;
			gmGexFile = new File(file);
			
			// Initialize filedialog to save new Expression dataset
			FileDialog saveDialog = new FileDialog(window.getShell(), SWT.SAVE);
			saveDialog.setText("Save");
			saveDialog.setFilterExtensions(new String[] {"*.properties", "*.*"});
			saveDialog.setFilterNames(new String[] {"Gmml Vision Gex", "All files"});
			saveDialog.setFileName(gmGexFile.getName().replace(".gex", ".properties"));
			String fileName = saveDialog.open();
			// Only proceed if user selected a file
			if(file == null) return; 
			gexFile = new File(fileName);
			boolean confirmed = true;
			if(gexFile.exists())
			{
				confirmed = MessageDialog.openQuestion(window.getShell(),"",
				"File already exists, overwrite?");
			}
			if(confirmed)
			{
				gmmlGex.gexFile = gexFile;
				gmmlGex.gmGexFile = gmGexFile;
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, gmmlGex.convertRunnable);
				} catch(Exception e) {
					GmmlVision.log.error("while converting GenMAPP gex: " + e.getMessage(), e);
				}
				
			}
		}
	}
	private ConvertGexAction convertGexAction = new ConvertGexAction(this);
	
	/**
	 * {@link Action} to start conversion of a GenMAPP Gene database to a gene database 
	 * in hsqldb format
	 */
	private class ConvertGdbAction extends Action
	{
		GmmlVision window;
		public ConvertGdbAction(GmmlVision w)
		{
			window = w;
			setText("&Gdb to Gmml-Vision");
			setToolTipText("Convert from GenMAPP 2 Gene database to Gmml-Vision Gene database");
		}
		
		public void run () {
			File gdbFile = null;
			File gmGdbFile = null;
			// Initialize filedialog to open GenMAPP gdb
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Gene database to convert");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Gene Databases");
			fileDialog.setFilterExtensions(new String[] {"*.gdb","*.*"});
			fileDialog.setFilterNames(new String[] {"Gene database","All files"});
			String file = fileDialog.open();
			// Only proceed if user selected a file
			if(file == null) return;
			gmGdbFile = new File(file);
			// Initialize filedialog to save new Gene database
			FileDialog saveDialog = new FileDialog(window.getShell(), SWT.SAVE);
			saveDialog.setText("Save");
			saveDialog.setFilterExtensions(new String[] {"*.properties", "*.*"});
			saveDialog.setFilterNames(new String[] {"Gmml Vision Gdb", "All files"});
			saveDialog.setFileName(gmGdbFile.getName().replace(".gdb", ".properties"));
			String fileName = saveDialog.open();
			// Only proceed if user selected a file
			if(fileName == null) return;
				gdbFile = new File(fileName);
				boolean confirmed = true;
				if(gdbFile.exists())
				{
					confirmed = MessageDialog.openQuestion(window.getShell(),"",
							"File already exists, overwrite?");
				}
				if(confirmed)
				{
					gmmlGdb.convertGdbFile = gdbFile;
					gmmlGdb.convertGmGdbFile = gmGdbFile;
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
					try {
						dialog.run(true, true, gmmlGdb.convertRunnable);
					} catch(Exception e) {
						GmmlVision.log.error("while converting GenMAPP gene database: " + e.getMessage(), e);
					}

				}
		}
	}
	private ConvertGdbAction convertGdbAction = new ConvertGdbAction(this);
	
	/**
	 * {@link Action} to open the {@link ColorSetWindow}
	 */
	private class ColorSetManagerAction extends Action
	{
		GmmlVision window;
		public ColorSetManagerAction (GmmlVision w)
		{
			window = w;
			setText("&Color Set manager");
			setToolTipText("Create and edit color sets");
			setImageDescriptor(ImageDescriptor.createFromFile(null,"icons/colorset.gif"));
		}
		public void run () {
			if(window.gmmlGex.con != null)
			{
				colorSetWindow.run();
				showColorSetActionsCI(true);
				if(drawing != null)
				{
					drawing.redraw();
					drawing.legend.resetContents();
				}
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
				"No expression data loaded, load a gex file first");
			}
		}
	}
	private ColorSetManagerAction colorSetManagerAction = new ColorSetManagerAction(this);
	
	/**
	 * {@link Action} to open a {@link GmmlAboutBox} window
	 */
	private class AboutAction extends Action 
	{
		GmmlVision window;
		public AboutAction (GmmlVision w)
		{
			window = w;
			setText ("&About@F1");
			setToolTipText ("About Gmml-Vision");
		}
		public void run () {
			GmmlAboutBox gmmlAboutBox = new GmmlAboutBox(window.getShell(), SWT.NONE);
			gmmlAboutBox.open();
		}
	}
	private AboutAction aboutAction = new AboutAction(this);
	
	/**
	 * String displayed in the colorset combo when no colorset is selected
	 */
	final static String COMBO_NO_COLORSET = "No colorset";
	Combo colorSetCombo;
	/**
	 *{@link ControlContribution} to display the {@link Combo} for selecting a 
	 *colorset on the toolbar
	 */
	private class ColorSetComboContributionItem extends ControlContribution
	{
		public ColorSetComboContributionItem(String id)
		{
			super(id);
		}
		protected Control createControl(Composite parent) {
			Label label = new Label(parent, SWT.LEFT);
			label.setText("Color by:");
			label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
			label.pack();
			
			colorSetCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
			colorSetCombo.addSelectionListener(new ColorSetComboListener());
			colorSetCombo.pack();

			return colorSetCombo;
		}
		
	}
	
	/**
	 *{@link SelectionAdapter} to handle {@link SelectionEvent}s for the 
	 *{@link ColorSetComboContributionItem}
	 */
	private class ColorSetComboListener extends SelectionAdapter
	{
		public ColorSetComboListener()
		{
			super();
		}
		public void widgetSelected(SelectionEvent e)
		{
			if(drawing != null)
			{
				if(colorSetCombo.getText().equals(COMBO_NO_COLORSET))
				{
					drawing.setColorSetIndex(-1);
				}
				else
				{
					drawing.setColorSetIndex(colorSetCombo.getSelectionIndex() - 1);
					if(gmmlGdb.getCon() == null)
					{
						MessageDialog.openWarning(getShell(), "Warning", "No gene database selected");
					}
				}
			}
		}
	}
	
	/**
	 * {@link Action} to switch between edit and view mode
	 */
	private class SwitchEditModeAction extends Action
	{
		GmmlVision window;
		public SwitchEditModeAction (GmmlVision w)
		{
			super("&Edit mode", IAction.AS_CHECK_BOX);
			setImageDescriptor(ImageDescriptor.createFromFile(null,"icons/edit.gif"));
			window = w;
		}
		
		public void run () {
			if(drawing != null)
			{
				if(isChecked())
				{
					//Switch to edit mode: show edit toolbar, show property table in sidebar
					drawing.setEditMode(true);
					showEditActionsCI(true);
					rightPanel.getTabFolder().setSelection(1);
				}
				else
				{
					//Switch to view mode: hide edit toolbar, show backpage browser in sidebar
					drawing.setEditMode(false);
					showEditActionsCI(false);
					rightPanel.getTabFolder().setSelection(0);
				}
			}
			else //No gmml pathway loaded, deselect action and do nothing
			{
				setChecked(false);
			}
			getCoolBarManager().update(true);
		}
	}
	private SwitchEditModeAction switchEditModeAction = new SwitchEditModeAction(this);
	
	/**
	 * {@link Action} to show or hide a legend for the selected colorset
	 */
	public class ShowLegendAction extends Action
	{
		GmmlVision window;
		public ShowLegendAction (GmmlVision w)
		{
			super("Show &legend", IAction.AS_CHECK_BOX);
			setImageDescriptor(ImageDescriptor.createFromFile(null,"icons/legend.gif"));
			window = w;
		}
		
		public void run () {
			if(drawing == null || gmmlGex.con == null)
			{
				setChecked(false);
			}
			else
			{
				drawing.showLegend(isChecked());			
			}
		}
	}
	public ShowLegendAction showLegendAction = new ShowLegendAction(this);
	
	/**
	 * {@link Action} to show or hide the right sidepanel
	 */
	public class ShowRightPanelAction extends Action
	{
		GmmlVision window;
		public ShowRightPanelAction (GmmlVision w)
		{
			super("Show &information panel", IAction.AS_CHECK_BOX);
			window = w;
			setChecked(true);
		}
		
		public void run() {
			if(isChecked()) rightPanel.restore();
			else rightPanel.hide();
		}
	}
	public ShowRightPanelAction showRightPanelAction = new ShowRightPanelAction(this);
	
	/**
	 * {@link Action} to add a new element to the gmml pathway
	 */
	private class NewElementAction extends Action
	{
		GmmlVision window;
		int element;
		
		/**
		 * Constructor for this class
		 * @param e	type of element this action adds; a {@link GmmlDrawing} field constant
		 */
		public NewElementAction (int e)
		{
			element = e;
		
			String toolTipText;
			String image;
			toolTipText = image = null;
			switch(element) {
			case GmmlDrawing.NEWLINE: 
				toolTipText = "Draw new line";
				image = "icons/newline.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWLINEARROW:
				toolTipText = "Draw new arrow";
				image = "icons/newarrow.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWLINEDASHED:
				toolTipText = "Draw new dashed line";
				image = "icons/newdashedline.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWLINEDASHEDARROW:
				toolTipText = "Draw new dashed arrow";
				image = "icons/newdashedarrow.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWLABEL:
				toolTipText = "Draw new label";
				image = "icons/newlabel.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWARC:
				toolTipText = "Draw new arc";
				image = "icons/newarc.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWBRACE:
				toolTipText = "Draw new brace";
				image = "icons/newbrace.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWGENEPRODUCT:
				toolTipText = "Draw new geneproduct";
				image = "icons/newgeneproduct.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWRECTANGLE:
				image = "icons/newrectangle.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWOVAL:
				toolTipText = "Draw new oval";
				image = "icons/newoval.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWTBAR:
				toolTipText = "Draw new TBar";
				image = "icons/newtbar.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWRECEPTORROUND:
				toolTipText = "Draw new round receptor";
				image = "icons/newreceptorround.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWRECEPTORSQUARE:
				toolTipText = "Draw new square receptor";
				image = "icons/newreceptorsquare.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWLIGANDROUND:
				toolTipText = "Draw new round ligand";
				image = "icons/newligandround.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWLIGANDSQUARE:
				toolTipText = "Draw new square ligand";
				image = "icons/newligandsquare.gif";
				setChecked(false);
				break;
			case GmmlDrawing.NEWLINEMENU:
				setMenuCreator(new NewItemMenuCreator(GmmlDrawing.NEWLINEMENU));
				image = "icons/newlinemenu.gif";
				toolTipText = "Draw new line or arrow";
				break;
			case GmmlDrawing.NEWLINESHAPEMENU:
				setMenuCreator(new NewItemMenuCreator(GmmlDrawing.NEWLINESHAPEMENU));
				image = "icons/newlineshapemenu.gif";
				toolTipText = "Draw new ligand or receptor";
				break;
			}
			setToolTipText(toolTipText);
			setId("newItemAction");
			setImageDescriptor(ImageDescriptor.createFromFile(null,image));
		}
				
		public void run () {
			if(isChecked())
			{
				deselectNewItemActions();
				setChecked(true);
				drawing.setNewGraphics(element);
			}
			else
			{	
				drawing.setNewGraphics(GmmlDrawing.NEWNONE);
			}
		}
		
	}
	
	/**
	 * {@link IMenuCreator} that creates the drop down menus for 
	 * adding new line-type and -shape elements
	 */
	private class NewItemMenuCreator implements IMenuCreator {
		private Menu menu;
		int element;
		
		/**
		 * Constructor for this class
		 * @param e	type of menu to create; one of {@link GmmlDrawing}.NEWLINEMENU
		 * , {@link GmmlDrawing}.NEWLINESHAPEMENU
		 */
		public NewItemMenuCreator(int e) 
		{
			element = e;
		}
		
		public Menu getMenu(Menu parent) {
			return null;
		}

		public Menu getMenu(Control parent) {
			if (menu != null)
				menu.dispose();
			
			menu = new Menu(parent);
			Vector<Action> actions = new Vector<Action>();
			switch(element) {
			case GmmlDrawing.NEWLINEMENU:
				actions.add(new NewElementAction(GmmlDrawing.NEWLINE));
				actions.add(new NewElementAction(GmmlDrawing.NEWLINEARROW));
				actions.add(new NewElementAction(GmmlDrawing.NEWLINEDASHED));
				actions.add(new NewElementAction(GmmlDrawing.NEWLINEDASHEDARROW));
				break;
			case GmmlDrawing.NEWLINESHAPEMENU:
				actions.add(new NewElementAction(GmmlDrawing.NEWLIGANDROUND));
				actions.add(new NewElementAction(GmmlDrawing.NEWRECEPTORROUND));
				actions.add(new NewElementAction(GmmlDrawing.NEWLIGANDSQUARE));
				actions.add(new NewElementAction(GmmlDrawing.NEWRECEPTORSQUARE));
			}
			
			Iterator it = actions.iterator();
			while(it.hasNext())
			{
				addActionToMenu(menu, (Action)it.next());
			}

			return menu;
		}
		
		protected void addActionToMenu(Menu parent, Action a)
		{
			 ActionContributionItem item= new ActionContributionItem(a);
			 item.fill(parent, -1);
		}
		
		public void dispose() 
		{
			if (menu != null)  {
				menu.dispose();
				menu = null;
			}
		}
	}
	
	/**
	 * Deselects all {@link NewElementAction}s on the toolbar and sets 
	 * {@link GmmlDrawing}.newGraphics to {@link GmmlDrawing}.NEWNONE
	 */
	public void deselectNewItemActions()
	{
		IContributionItem[] items = editActionsCI.getToolBarManager().getItems();
		for(int i = 0; i < items.length; i++)
		{
			if(items[i] instanceof ActionContributionItem)
			{
				((ActionContributionItem)items[i]).getAction().setChecked(false);
			}
		}
		drawing.setNewGraphics(GmmlDrawing.NEWNONE);
	}
	
	// Elements of the coolbar
	ToolBarContributionItem commonActionsCI;
	ToolBarContributionItem editActionsCI;
	ToolBarContributionItem colorSetActionsCI;
	ToolBarContributionItem viewActionsCI;
	protected CoolBarManager createCoolBarManager(int style)
	{
		createCommonActionsCI();
		createEditActionsCI();
		createViewActionsCI();
		
		CoolBarManager coolBarManager = new CoolBarManager(style);
		coolBarManager.setLockLayout(true);
		
		coolBarManager.add(commonActionsCI);
		coolBarManager.add(viewActionsCI);
		return coolBarManager;
	}
	
	/**
	 * Creates element of the coolbar containing common actions as new, save etc.
	 */
	protected void createCommonActionsCI()
	{
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(newAction);
		toolBarManager.add(openAction);
		toolBarManager.add(saveAction);
		commonActionsCI = new ToolBarContributionItem(toolBarManager, "CommonActions");
	}
	
	/**
	 * Creates element of the coolbar only shown in edit mode (new element actions)
	 */
	protected void createEditActionsCI()
	{
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);		
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWGENEPRODUCT));
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWLABEL));
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWLINEMENU));
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWRECTANGLE));
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWOVAL));
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWARC));
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWBRACE));
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWTBAR));
		toolBarManager.add(new NewElementAction(GmmlDrawing.NEWLINESHAPEMENU));
		
		editActionsCI = new ToolBarContributionItem(toolBarManager, "EditModeActions");
	}
	
	/**
	 * Creates element of the coolbar containing controls related to viewing a pathway
	 */
	protected void createViewActionsCI()
	{
		final GmmlVision window = this;
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		//Add zoomCombo
		toolBarManager.add(new ControlContribution("ZoomCombo") {
			protected Control createControl(Composite parent) {
				final Combo zoomCombo = new Combo(parent, SWT.DROP_DOWN);
				zoomCombo.setItems(new String[] { "200%", "100%", "75%", "50%", "Zoom to fit" });
				zoomCombo.setText("100%");
				zoomCombo.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						int pctZoom = 100;
						String zoomText = zoomCombo.getText().replace("%", "");
						try {
							pctZoom = Integer.parseInt(zoomText);
						} catch (Exception ex) { 
							if(zoomText.equals("Zoom to fit"))
									{ pctZoom = ZOOM_TO_FIT; } else { return; }
						}
						new ZoomAction(window, pctZoom).run();
					}
					public void widgetDefaultSelected(SelectionEvent e) { widgetSelected(e); }
				});
				return zoomCombo;
			}
		});
		//Add swich to editmode
		toolBarManager.add(switchEditModeAction);
		
		viewActionsCI =  new ToolBarContributionItem(toolBarManager, "SwitchActions");
	}
	
	/**
	 * Creates element of the coolbar containing actions related to color sets;  
	 * only shown when expression data is loaded
	 */
	protected void createColorSetActionsCI()
	{
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(new ControlContribution("ColorSetLabel") {
			protected Control createControl(Composite parent) {
				Composite comp = new Composite(parent, SWT.NONE);
				comp.setLayout(new GridLayout(1, true));
				Label label = new Label(comp, SWT.LEFT);
				label.setText("Color by:");
				label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
				label.pack();
				return comp;
			}	});
		toolBarManager.add(new ControlContribution("ColorSetCombo") {
			protected Control createControl(Composite parent) {				
				colorSetCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
				colorSetCombo.addSelectionListener(new ColorSetComboListener());
				String[] colorSets = gmmlGex.getColorSetNames();
				if(colorSets != null) {
					String[] comboItems = new String[colorSets.length + 1];
					comboItems[0] = COMBO_NO_COLORSET;
					System.arraycopy(colorSets, 0, comboItems, 1, colorSets.length);
					colorSetCombo.setItems(comboItems);
				}
				colorSetCombo.pack();
				return colorSetCombo;
			}
		});
		
		toolBarManager.add(colorSetManagerAction);
		toolBarManager.add(showLegendAction);
		colorSetActionsCI = new ToolBarContributionItem(toolBarManager, "ColorSetActions");
	}
	
	/**
	 * Shows or hides the editActionsCI
	 * @param show	true/false for either show or hide
	 */
	private void showEditActionsCI(boolean show)
	{
		if(show) {
			// Hide the colorSetActionsCI if displayed
			showColorSetActionsCI(false);
			getCoolBarManager().add(editActionsCI);
		}
		else { 
			getCoolBarManager().remove(editActionsCI);
			// Show the colorSetActionsCI if needed
			showColorSetActionsCI(true);
		}
		getCoolBarManager().update(true);
	}
	
	/**
	 * Shows or hides the colorSetActionsCI
	 * @param show	true/false for either show or hide
	 */
	private void showColorSetActionsCI(boolean show)
	{
		if(show) {
			//Check if drawing is in edit mode if loaded
			if(drawing != null) { 
				if(drawing.isEditMode()) return;
			}
			//Check if expression data is loaded
			if(gmmlGex.con == null) return;
			//Re-create the colorSetActions (to recreate disposed items)
			createColorSetActionsCI();
			//Add the elements to the coolbar and update
			getCoolBarManager().add(colorSetActionsCI);
			getCoolBarManager().update(true);

			//Select the colorset used in the drawing if loaded
			if(drawing != null) { 
				colorSetCombo.select(drawing.colorSetIndex + 1); 
			} else {
				colorSetCombo.select(0);
			}
		}
		else { 
			getCoolBarManager().remove(colorSetActionsCI);
		}
		getCoolBarManager().update(true);
	}

	protected StatusLineManager createStatusLineManager() {
		return super.createStatusLineManager();
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
		editMenu.add(switchEditModeAction);
//		editMenu.add(propertyAction);
		MenuManager viewMenu = new MenuManager ("&View");
		viewMenu.add(showLegendAction);
		viewMenu.add(showRightPanelAction);
		MenuManager zoomMenu = new MenuManager("&Zoom");
		zoomMenu.add(new ZoomAction(this, 50));
		zoomMenu.add(new ZoomAction(this, 75));
		zoomMenu.add(new ZoomAction(this, 100));
		zoomMenu.add(new ZoomAction(this, 125));
		zoomMenu.add(new ZoomAction(this, 150));
		zoomMenu.add(new ZoomAction(this, 200));
		zoomMenu.add(new ZoomAction(this, ZOOM_TO_FIT));
		viewMenu.add(zoomMenu);
		MenuManager dataMenu = new MenuManager ("&Data");
		dataMenu.add(selectGdbAction);
		dataMenu.add(selectGexAction);
		dataMenu.add(createGexAction);
		dataMenu.add(colorSetManagerAction);
		MenuManager convertMenu = new MenuManager("&Convert from GenMAPP 2");
		convertMenu.add(convertGexAction);
		convertMenu.add(convertGdbAction);
		dataMenu.add(convertMenu);
		
		MenuManager helpMenu = new MenuManager ("&Help");
		helpMenu.add(aboutAction);
		m.add(fileMenu);
		m.add(editMenu);
		m.add(viewMenu);
		m.add(dataMenu);
		m.add(helpMenu);
		return m;
	}

	/**
	 * The visual representation of the gmml pathway
	 */
	private GmmlDrawing drawing;
	
	public GmmlDrawing getDrawing() { return drawing; }

	/**
	 * {@link GmmlData} object containing JDOM representation of the gmml pathway 
	 * and handle gmml related actions
	 */
	public GmmlData gmmlData;
	/**
	 * {@link GmmlGdb} object to handle gene database related actions
	 */
	public GmmlGdb gmmlGdb = new GmmlGdb();
	/**
	 * {@link GmmlGex} object to handle expression data related actions
	 */
	public GmmlGex gmmlGex = new GmmlGex(gmmlGdb);
	/**
	 * {@link SearchMethods} object holding search operations
	 */
	public SearchMethods search = new SearchMethods(gmmlGdb);
	
	public GmmlVision()
	{
		this(null);
	}
	
	/**
	 *Constructor for the GmmlVision class
	 *Initializes new GmmlVision and sets properties for frame
	 */
	public GmmlVision(Shell shell)
	{
		super(shell);
		addMenuBar();
		addStatusLine();
		addCoolBar(SWT.FLAT | SWT.LEFT);
		
		log = new Logger();
		try { log.setStream(new PrintStream("log.txt")); } catch(Exception e) {}
		log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level
	}

	/**
	 * Main method which will be carried out when running the program
	 */
	public static void main(String[] args)
	{
//		//<DEBUG to find undisposed system resources>
//		DeviceData data = new DeviceData();
//		data.tracking = true;
//		Display display = new Display(data);
//		debug.Sleak sleak = new debug.Sleak();
//		sleak.open();
//		
//		Shell shell = new Shell(display);
//		GmmlVision window = new GmmlVision(shell);
//		//</DEBUG>
		
	   GmmlVision window = new GmmlVision();
	   window.setBlockOnOpen(true);
	   window.open();
	   
	   //Close database connections
	   window.gmmlGdb.close();
	   window.gmmlGex.close();
	   Display.getCurrent().dispose();
	   GmmlVision.log.getStream().close();
	}
	
	public ScrolledComposite sc;
	public GmmlBpBrowser bpBrowser; //Browser for showing backpage information
	public GmmlPropertyTable propertyTable;	//Table showing properties of GmmlGraphics objects
	SashForm sashForm; //SashForm containing the drawing area and sidebar
	ColorSetWindow colorSetWindow; //Window containing the colorset manager
	TabbedSidePanel rightPanel; //side panel containing backbage browser and property editor
	PathwaySearchComposite pwSearchComposite; //Composite that handles pathway searches and displays results
	protected Control createContents(Composite parent)
	{
		loadImages();
		
		Shell shell = parent.getShell();
		shell.setSize(800, 600);
		shell.setLocation(100, 100);
		
		shell.setText("GmmlVision");

		Composite viewComposite = new Composite(parent, SWT.NULL);
		viewComposite.setLayout(new FillLayout());
		
		sashForm = new SashForm(viewComposite, SWT.HORIZONTAL);
		
		sc = new ScrolledComposite (sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setFocus();
		
		rightPanel = new TabbedSidePanel(sashForm, SWT.NULL, this);
		
		//rightPanel controls
		bpBrowser = new GmmlBpBrowser(rightPanel.getTabFolder(), SWT.NONE, this);
		propertyTable = new GmmlPropertyTable(
				rightPanel.getTabFolder(), SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		pwSearchComposite = new PathwaySearchComposite(rightPanel.getTabFolder(), SWT.NONE, this);
		
		rightPanel.addTab(bpBrowser, "Backpage");
		rightPanel.addTab(propertyTable, "Properties");
		rightPanel.addTab(pwSearchComposite, "Pathway Search");
		
		sashForm.setWeights(new int[] {60, 40});
		
		rightPanel.getTabFolder().setSelection(0); //select backpage browser tab
		
		setStatus("Using Gene Database: '" + gmmlGdb.getProps().getProperty("currentGdb") + "'");
		
		colorSetWindow = new ColorSetWindow(shell);
		colorSetWindow.setGmmlGex(gmmlGex);
		
		return parent;
		
	};
	
	/**
	 * the transparent color used in the icons for visualization of protein/mrna data
	 */
	static final RGB TRANSPARENT_COLOR = new RGB(255, 0, 255);
	public static ImageRegistry imageRegistry;
	/**
	 * Loads images used throughout the applications into an {@link ImageRegistry}
	 */
	void loadImages()
	{
		//TODO: put all images and icons in the progam in imageRegistry
		imageRegistry = new ImageRegistry(getShell().getDisplay());
		
		// Labels for color by expressiondata (mRNA and Protein)
		ImageData img = new ImageData("images/mRNA.bmp");
		img.transparentPixel = img.palette.getPixel(TRANSPARENT_COLOR);
		imageRegistry.put("data.mRNA",
				new Image(getShell().getDisplay(), img));
		img = new ImageData("images/protein.bmp");
		img.transparentPixel = img.palette.getPixel(TRANSPARENT_COLOR);
		imageRegistry.put("data.protein",
				new Image(getShell().getDisplay(), img));
		img = new ImageData("icons/minimize.gif");
		imageRegistry.put("sidepanel.minimize",
				new Image(getShell().getDisplay(), img));
		img = new ImageData("icons/close.gif");
		imageRegistry.put("sidepanel.hide",
				new Image(getShell().getDisplay(), img));
		
	}
	
	/**
	 * Creates a new empty drawing and loads it in the frame 
	 */
	private void createNewDrawing()
	{		
		drawing = new GmmlDrawing(sc, SWT.NO_BACKGROUND, this);
		
		gmmlData = new GmmlData(drawing);
		
		drawing.setSize(800, 600);
		switchEditModeAction.setChecked(true);
		switchEditModeAction.run();
		sc.setContent(drawing);
		
	}
	
	/**
	 * Opens a GMML representation of a pathway or reaction and creates 
	 * a scrollpane of the drawing, which is loaded in the frame.
	 */
	public void openPathway(String fnPwy)
	{
		drawing = new GmmlDrawing(sc, SWT.NO_BACKGROUND, this);
		
		// initialize new JDOM gmml representation and read the file
		try { gmmlData = new GmmlData(fnPwy, drawing); } catch(Exception e) {
			MessageDialog.openError(getShell(), "Unable to open Gmml file", e.getMessage());
			drawing = null;
		}
		
		if(drawing != null)
		{
			drawing.setEditMode(switchEditModeAction.isChecked());
			
			if(gmmlGex.con != null)
			{
				cacheExpressionData();
				if(!drawing.isEditMode()) drawing.setColorSetIndex(colorSetCombo.getSelectionIndex() - 1);
			}		
			sc.setContent(drawing);
		}	
	}

} // end of class