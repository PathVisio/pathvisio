package gmmlVision;

import graphics.*;
import data.*;
import colorSet.*;

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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.dialogs.*;
import org.jdom.*;
import org.jdom.output.*;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
//~ import java.awt.Color;

/**
 * This class is the main class in the GMML project. 
 * It acts as a container for pathwaydrawings and facilitates
 * loading, creating and saving drawings to and from GMML.
 */
public class GmmlVision extends ApplicationWindow
{
	private static final long serialVersionUID = 1L;
	private static int ZOOM_TO_FIT = -1;
	
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
			setText ("&Save mapp@Ctrl+S");
			setToolTipText ("Save mapp");
			setImageDescriptor(ImageDescriptor.createFromFile(null,"icons/save.gif"));
		}
		
		public void run () {
			double usedZoom = drawing.zoomFactor * 100;
			// Set zoom to 100%
			drawing.setZoom(100);
			drawing.updateJdomElements();
			// Overwrite the existing xml file
			if (gmmlData.xmlFile != null)
			{
				gmmlData.writeToXML(gmmlData.xmlFile);
			}
			else
			{
				saveAsAction.run();
			}
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
			setText ("Save mapp &As");
			setToolTipText ("Save mapp with new file name");
		}
		public void run () {
			if (drawing != null)
			{
				FileDialog fd = new FileDialog(window.getShell(), SWT.SAVE);
				fd.setText("Save");
				fd.setFilterExtensions(new String[] {"*.xml","*.*"});
				fd.setFilterNames(new String[] {"Gmml file", "All files"});
				String fileName = fd.open();
				if(fileName != null) 
				{
					if(!fileName.endsWith(".xml"))
					{
						fileName += ".xml";
					}
					File checkFile = new File(fileName);
					boolean confirmed = true;
					if(checkFile.exists())
					{
						if(!MessageDialog.openQuestion(window.getShell(),"",
								"File already exists, overwrite?"))
						{
							confirmed = false;
						}
					}
					if(confirmed)
					{
						double usedZoom = drawing.zoomFactor * 100;
						// Set zoom to 100%
						drawing.setZoom(100);
						drawing.updateJdomElements();
						// Overwrite the existing xml file
						gmmlData.writeToXML(checkFile);
						// Set zoom back
						drawing.setZoom(usedZoom);
					}
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
				double newPctZoomFactor = pctZoomFactor;
				if(pctZoomFactor == ZOOM_TO_FIT) 
				{
					Point shellSize = window.sc.getSize();
					Point drawingSize = drawing.getSize();
					newPctZoomFactor = (int)Math.min(
							drawing.zoomFactor * 100 * (double)shellSize.x / drawingSize.x,
							drawing.zoomFactor * 100 * (double)shellSize.y / drawingSize.y
					);
				} 
				drawing.setZoom(newPctZoomFactor);
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gmml file loaded! Open or create a new gmml file first");
			}
		}
	}
		
	private class SelectGdbAction extends Action
	{
		GmmlVision window;
		public SelectGdbAction(GmmlVision w)
		{
			window = w;
			setText("&Select Gene Database");
			setToolTipText("Select Gene Database");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Gene Database");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Gene databases");
			fileDialog.setFilterExtensions(new String[] {"*.properties","*.*"});
			fileDialog.setFilterNames(new String[] {"Gene Database","All files"});
			String file = fileDialog.open();
			String error = null;
			if(file != null) {
				error = gmmlGdb.connect(new File(file));
			} else { return; }

			if(error == null)
			{
				setStatus("Using Gene Database: '" + gmmlGdb.props.getProperty("currentGdb") + "'");
				cacheExpressionData();
			} else {
				MessageBox messageBox = new MessageBox(getShell(),
						SWT.ICON_ERROR| SWT.OK);
				messageBox.setMessage("Failed to load '" + file + "'\nError: " + error);
				messageBox.setText("Error");
				messageBox.open();
			}
		}
	}
	private SelectGdbAction selectGdbAction = new SelectGdbAction(this);
	
	private class SelectGexAction extends Action
	{
		GmmlVision window;
		public SelectGexAction(GmmlVision w)
		{
			window = w;
			setText("&Select Expression Data");
			setToolTipText("Select Expression Data");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Expression Dataset");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Expression Datasets");
			fileDialog.setFilterExtensions(new String[] {"*.properties","*.*"});
			fileDialog.setFilterNames(new String[] {"Expression Dataset","All files"});
			String file = fileDialog.open();
			String error = null;
			if(file != null) {
				gmmlGex.gexFile = new File(file);
				error = gmmlGex.connect();
				if(gmmlGex.con != null)
				{
					gmmlGex.setSamples();
					gmmlGex.loadColorSets();
					cacheExpressionData();
					showColorSetActionsCI(true);
				}			
				if(error != null)
				{
					MessageBox messageBox = new MessageBox(getShell(),
							SWT.ICON_ERROR| SWT.OK);
					messageBox.setMessage("Failed to load '" + file + "'\nError: " + error);
					messageBox.setText("Error");
					messageBox.open();
				}
			}
		}
	}
	private SelectGexAction selectGexAction = new SelectGexAction(this);
	
	private void cacheExpressionData()
	{
		if(drawing != null)
		{
			gmmlGex.mappIds = drawing.getMappIds();
			if(gmmlGex.con != null && gmmlGdb.con != null)
			{
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, gmmlGex.cacheRunnable);
					drawing.redraw();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
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
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Expression Dataset to convert");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Expression Datasets");
			fileDialog.setFilterExtensions(new String[] {"*.gex","*.*"});
			fileDialog.setFilterNames(new String[] {"Expression Dataset","All files"});
			String file = fileDialog.open();
			if(file != null) {
				gmGexFile = new File(file);
			} else {
				return;
			}
			FileDialog saveDialog = new FileDialog(window.getShell(), SWT.SAVE);
			saveDialog.setText("Save");
			saveDialog.setFilterExtensions(new String[] {"*.properties", "*.*"});
			saveDialog.setFilterNames(new String[] {"Gmml Vision Gex", "All files"});
			saveDialog.setFileName(gmGexFile.getName().replace(".gex", ".properties"));
			String fileName = saveDialog.open();
			if(fileName != null) 
			{
				System.out.println(fileName);
				gexFile = new File(fileName);
				boolean confirmed = true;
				if(gexFile.exists())
				{
					if(!MessageDialog.openQuestion(window.getShell(),"",
							"File already exists, overwrite?"))
					{
						confirmed = false;
					}
				}
				if(confirmed)
				{
					gmmlGex.gexFile = gexFile;
					gmmlGex.gmGexFile = gmGexFile;
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
					try {
						dialog.run(true, true, gmmlGex.convertRunnable);
					} catch(Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
	}
	private ConvertGexAction convertGexAction = new ConvertGexAction(this);
	
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
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Gene database to convert");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Gene Databases");
			fileDialog.setFilterExtensions(new String[] {"*.gdb","*.*"});
			fileDialog.setFilterNames(new String[] {"Gene database","All files"});
			String file = fileDialog.open();
			if(file != null) {
				gmGdbFile = new File(file);
			} else {
				return;
			}
			FileDialog saveDialog = new FileDialog(window.getShell(), SWT.SAVE);
			saveDialog.setText("Save");
			saveDialog.setFilterExtensions(new String[] {"*.properties", "*.*"});
			saveDialog.setFilterNames(new String[] {"Gmml Vision Gdb", "All files"});
			saveDialog.setFileName(gmGdbFile.getName().replace(".gdb", ".properties"));
			String fileName = saveDialog.open();
			if(fileName != null) 
			{
				System.out.println(fileName);
				gdbFile = new File(fileName);
				boolean confirmed = true;
				if(gdbFile.exists())
				{
					if(!MessageDialog.openQuestion(window.getShell(),"",
							"File already exists, overwrite?"))
					{
						confirmed = false;
					}
				}
				if(confirmed)
				{
					gmmlGdb.convertGdbFile = gdbFile;
					gmmlGdb.convertGmGdbFile = gmGdbFile;
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
					try {
						dialog.run(true, true, gmmlGdb.convertRunnable);
					} catch(Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
	}
	private ConvertGdbAction convertGdbAction = new ConvertGdbAction(this);
	
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
				getCoolBarManager().add(colorSetActionsCI);
				if(drawing != null)
				{
					drawing.redraw();
					drawing.legend.resetContents();
				}
				showColorSetActionsCI(true);
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
				"No expression data loaded, load a gex file first");
			}
		}
	}
	private ColorSetManagerAction colorSetManagerAction = new ColorSetManagerAction(this);
	
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
	
	final static String COMBO_NO_COLORSET = "No colorset";
	Combo colorSetCombo;
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
					if(gmmlGdb.con == null)
					{
						MessageDialog.openWarning(getShell(), "Warning", "No gene database selected");
					}
				}
			}
		}
	}
	
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
					drawing.setEditMode(true);
					showEditActionsCI(true);
					sashFormSplit.setMaximizedControl(propertyTable.tableViewer.getTable());
				}
				else
				{
					drawing.setEditMode(false);
					showEditActionsCI(false);
					sashFormSplit.setMaximizedControl(bpBrowser);
				}
			}
			else
			{
				setChecked(false);
			}
			getCoolBarManager().update(true);
		}
	}
	private SwitchEditModeAction switchEditModeAction = new SwitchEditModeAction(this);
	
	public class ShowLegendAction extends Action
	{
		GmmlVision window;
		public ShowLegendAction (GmmlVision w)
		{
			super("&Show legend", IAction.AS_CHECK_BOX);
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
	
	private class NewElementAction extends Action
	{
		GmmlVision window;
		int element;

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
				drawing.newGraphics = element;
			}
			else
			{	
				drawing.newGraphics = GmmlDrawing.NEWNONE;
			}
		}
		
	}
	
	private class NewItemMenuCreator implements IMenuCreator {
		private Menu menu;
		int element;
		
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
			Vector actions = new Vector();
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
	}
	
	ToolBarContributionItem commonActionsCI;
	ToolBarContributionItem editActionsCI;
	ToolBarContributionItem colorSetActionsCI;
	ToolBarContributionItem switchActionsCI;
	protected CoolBarManager createCoolBarManager(int style)
	{
		commonActionsCI = createCommonActionsCI();
		editActionsCI = createEditActionsCI();
		colorSetActionsCI = createColorSetActionsCI();
		switchActionsCI = createSwitchActionsCI();
		
		CoolBarManager coolBarManager = new CoolBarManager(style);
		coolBarManager.setLockLayout(true);
		
		coolBarManager.add(commonActionsCI);
		coolBarManager.add(switchActionsCI);
		return coolBarManager;
	}
	
	protected ToolBarContributionItem createSwitchActionsCI()
	{
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(switchEditModeAction);
		
		return new ToolBarContributionItem(toolBarManager, "SwitchActions");
	}
	
	protected ToolBarContributionItem createColorSetActionsCI()
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
				colorSetCombo.pack();
				return colorSetCombo;
			}	});
		
		toolBarManager.add(colorSetManagerAction);
		toolBarManager.add(showLegendAction);
		ToolBarContributionItem ci = new ToolBarContributionItem(toolBarManager, "ColorSetActions");
		return ci;
	}
	
	private void showEditActionsCI(boolean show)
	{
		if(show) {
			showColorSetActionsCI(false);
			getCoolBarManager().add(editActionsCI);
		}
		else { 
			getCoolBarManager().remove(editActionsCI);
			showColorSetActionsCI(true);
		}
		getCoolBarManager().update(true);
	}
	
	private void showColorSetActionsCI(boolean show)
	{
		if(show) {
			if(drawing != null) { 
				if(drawing.editMode) { return; }
			}
			if(gmmlGex.con == null) { return; }
			colorSetActionsCI = createColorSetActionsCI();
			getCoolBarManager().add(colorSetActionsCI);
			getCoolBarManager().update(true);
			String[] colorSets = gmmlGex.getColorSetNames();
			String[] comboItems = new String[colorSets.length + 1];
			comboItems[0] = COMBO_NO_COLORSET;
			System.arraycopy(colorSets, 0, comboItems, 1, colorSets.length);
			colorSetCombo.setItems(comboItems);
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
	
	protected ToolBarContributionItem createCommonActionsCI()
	{
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(newAction);
		toolBarManager.add(openAction);
		toolBarManager.add(saveAction);
		return new ToolBarContributionItem(toolBarManager, "CommonActions");
	}
	
	protected ToolBarContributionItem createEditActionsCI()
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
		
		return new ToolBarContributionItem(toolBarManager, "EditModeActions");
	}

	
//	protected ToolBarManager createToolBarManager(int style) {
//		ToolBarManager toolBarManager = new ToolBarManager(style);
//		toolBarManager.add(new GroupMarker("group.commonActions"));
//		toolBarManager.appendToGroup("group.commonActions", newAction);
//		toolBarManager.appendToGroup("group.commonActions", openAction);
//		toolBarManager.appendToGroup("group.commonActions", saveAction);
//		toolBarManager.add(new Separator("group.editMode"));
//		toolBarManager.add(switchEditModeAction);
//		toolBarManager.add(new Separator("group.newElements"));
//		
//		newItemActions = new HashMap();
//		newItemOrder = new Vector();
//		newItemActions.put("newGeneProduct", new NewElementAction(GmmlDrawing.NEWGENEPRODUCT));
//		newItemOrder.add("newGeneProduct");
//		newItemActions.put("newLabel", new NewElementAction(GmmlDrawing.NEWLABEL));
//		newItemOrder.add("newLabel");
//		newItemActions.put("newLineMenu", new NewElementAction(GmmlDrawing.NEWLINEMENU));
//		newItemOrder.add("newLineMenu");
//		newItemActions.put("newRectangle", new NewElementAction(GmmlDrawing.NEWRECTANGLE));
//		newItemOrder.add("newRectangle");
//		newItemActions.put("newOval", new NewElementAction(GmmlDrawing.NEWOVAL));
//		newItemOrder.add("newOval");
//		newItemActions.put("newArc", new NewElementAction(GmmlDrawing.NEWARC));
//		newItemOrder.add("newArc");
//		newItemActions.put("newBrace", new NewElementAction(GmmlDrawing.NEWBRACE));
//		newItemOrder.add("newBrace");
//		newItemActions.put("newTbar", new NewElementAction(GmmlDrawing.NEWTBAR));
//		newItemOrder.add("newTbar");
//		newItemActions.put("newLineShapeMenu", new NewElementAction(GmmlDrawing.NEWLINESHAPEMENU));
//		newItemOrder.add("newLineShapeMenu");
//		
//		toolBarManager.update(true);
//		
//		return toolBarManager;
//	}

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
		editMenu.add(propertyAction);
		MenuManager viewMenu = new MenuManager ("&View");
		viewMenu.add(showLegendAction);
		viewMenu.add(new ZoomAction(this, 50));
		viewMenu.add(new ZoomAction(this, 75));
		viewMenu.add(new ZoomAction(this, 100));
		viewMenu.add(new ZoomAction(this, 125));
		viewMenu.add(new ZoomAction(this, 150));
		viewMenu.add(new ZoomAction(this, 200));
		viewMenu.add(new ZoomAction(this, ZOOM_TO_FIT));
		MenuManager dataMenu = new MenuManager ("&Data");
		dataMenu.add(selectGdbAction);
		dataMenu.add(selectGexAction);
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

//	public HashMap newItemActions;
//	public Vector newItemOrder;

//	protected void addNewItemActions(ToolBarManager toolBarManager)
//	{
//		Iterator it = newItemOrder.iterator();
//		while(it.hasNext())
//		{
//			toolBarManager.appendToGroup("group.newElements",
//					(Action)newItemActions.get((String)it.next()));
//		}
//		toolBarManager.update(true);
//	}

//	protected void removeNewItemActions(ToolBarManager toolBarManager)
//	{
//		for(int i = 0; i < newItemOrder.size(); i++)
//		{
//				toolBarManager.remove("newItemAction");
//		}
//		toolBarManager.update(true);
//	}

	GmmlDrawing drawing;
	public GmmlData gmmlData;
	public GmmlGdb gmmlGdb = new GmmlGdb();
	public GmmlGex gmmlGex = new GmmlGex(gmmlGdb);
	
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
		addCoolBar(SWT.FLAT | SWT.LEFT);
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
	   
	   window.gmmlGdb.close();
	   window.gmmlGex.close();
	   Display.getCurrent().dispose();
	}
	
	public ScrolledComposite sc;
	public GmmlBpBrowser bpBrowser;
	public GmmlPropertyTable propertyTable;
	ToolItem editSwitch;
	SashForm sashForm;
	SashForm sashFormSplit;
	ColorSetWindow colorSetWindow;
	protected Control createContents(Composite parent)
	{
		Shell shell = parent.getShell();
		shell.setSize(800, 600);
		shell.setLocation(100, 100);
		
		shell.setText("GmmlVision");
		
		Composite viewComposite = new Composite(parent, SWT.NULL);
		viewComposite.setLayout(new FillLayout());
		
		sashForm = new SashForm(viewComposite, SWT.HORIZONTAL);
		
		sc = new ScrolledComposite (sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setFocus();
		
		sashFormSplit = new SashForm (sashForm, SWT.VERTICAL);
		sashForm.setWeights(new int[] {80, 20});
		
		propertyTable = new GmmlPropertyTable(sashFormSplit, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		
		bpBrowser = new GmmlBpBrowser(sashFormSplit, SWT.NONE);
		
		sashFormSplit.setMaximizedControl(bpBrowser);
		
		setStatus("Using Gene Database: '" + gmmlGdb.props.getProperty("currentGdb") + "'");
		
		colorSetWindow = new ColorSetWindow(shell);
		colorSetWindow.setGmmlGex(gmmlGex);

		loadImages();
		
		return parent;
		
	};
	
	static final RGB TRANSPARENT_COLOR = new RGB(255, 0, 255);
	public ImageRegistry imageRegistry;
	void loadImages()
	{
		//TODO: put all images and icons in the progam in imageRegistry
		imageRegistry = new ImageRegistry(getShell().getDisplay());
		
		// Labels for color by expressiondata (mRNA and Protein)
		ImageData img = new ImageData("images/mrna.bmp");
		img.transparentPixel = img.palette.getPixel(TRANSPARENT_COLOR);
		imageRegistry.put("data.mRNA",
				new Image(getShell().getDisplay(), img));
		img = new ImageData("images/protein.bmp");
		img.transparentPixel = img.palette.getPixel(TRANSPARENT_COLOR);
		imageRegistry.put("data.protein",
				new Image(getShell().getDisplay(), img));	
		
	}
	
	/**
	 * Creates a new empty drawing and loads it in the frame 
	 */
	private void createNewDrawing()
	{		
		drawing = new GmmlDrawing(sc, SWT.NO_BACKGROUND);
		drawing.setGmmlVision(this);
		
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
	private void openPathway(String fnPwy)
	{		
		drawing = new GmmlDrawing(sc, SWT.NO_BACKGROUND);
		drawing.setGmmlVision(this);
		
		// initialize new JDOM gmml representation and read the file
		gmmlData = new GmmlData(fnPwy, drawing);
		
		if(drawing != null)
		{
			drawing.editMode = switchEditModeAction.isChecked();
			if(gmmlGex.con != null)
			{
				cacheExpressionData();
				if(!drawing.editMode) drawing.setColorSetIndex(colorSetCombo.getSelectionIndex() - 1);
			}
			sc.setContent(drawing);
		}	
	}

} // end of class