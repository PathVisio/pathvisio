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
			setText ("&New@Ctrl+N");
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
			setText ("&Open@Ctrl+O");
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
			setText ("&Save@Ctrl+S");
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
			setText ("Save &As");
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
			setText("&Select Gdb");
			setToolTipText("Select Gene Database");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Gene Database");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Gene databases");
			fileDialog.setFilterExtensions(new String[] {"*.properties","*.*"});
			fileDialog.setFilterNames(new String[] {"Gene Database","All files"});
			String file = fileDialog.open();
			if(file != null) {
				if(gmmlGdb.connect(new File(file))) {
					setStatus("Using Gene Database: '" + gmmlGdb.props.getProperty("currentGdb") + "'");
				} else {
					MessageBox messageBox = new MessageBox(getShell(),
							SWT.ICON_ERROR| SWT.OK);
					messageBox.setMessage("Failed to load '" + file + "'");
					messageBox.setText("Error");
					messageBox.open();
				}
			}
			cacheExpressionData();
		}
	}
	private SelectGdbAction selectGdbAction = new SelectGdbAction(this);
	
	private class SelectGexAction extends Action
	{
		GmmlVision window;
		public SelectGexAction(GmmlVision w)
		{
			window = w;
			setText("&Select Expression data");
			setToolTipText("Select Expression Data");
		}
		
		public void run () {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			fileDialog.setText("Select Expression Dataset");
			fileDialog.setFilterPath("C:\\GenMAPP 2 Data\\Expression Datasets");
			fileDialog.setFilterExtensions(new String[] {"*.properties","*.*"});
			fileDialog.setFilterNames(new String[] {"Expression Dataset","All files"});
			String file = fileDialog.open();
			if(file != null) {
				gmmlGex.gexFile = new File(file);
				gmmlGex.connect();
				if(gmmlGex.con != null)
				{
					gmmlGex.setSamples();
					gmmlGex.loadColorSets();
					cacheExpressionData();
					showColorSetCombo(true);
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
			setText("&Convert GenMAPP Gex");
			setToolTipText("Convert GenMAPP gex to GmmlVisio expression dataset");
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
	
	private class ColorSetManagerAction extends Action
	{
		GmmlVision window;
		public ColorSetManagerAction (GmmlVision w)
		{
			window = w;
			setText("&Color Set manager");
			setToolTipText("Create and edit color sets");
		}
		public void run () {
			if(window.gmmlGex.con != null)
			{
				colorSetWindow.run();
				showColorSetCombo(true);
				if(drawing != null)
				{
					drawing.redraw();
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
	
	private class SwitchEditModeAction extends Action
	{
		GmmlVision window;
		public SwitchEditModeAction (GmmlVision w)
		{
			super("&Edit mode", IAction.AS_CHECK_BOX);
			window = w;
		}
		
		public void run () {
			if(drawing != null)
			{
				if(isChecked())
				{
					removeNewItemActions(getToolBarManager());
					addNewItemActions(getToolBarManager());			
					sashFormSplit.setMaximizedControl(propertyTable.tableViewer.getTable());
					drawing.setEditMode(true);
				}
				else
				{
					removeNewItemActions(getToolBarManager());
					showColorSetCombo(true);
					sashFormSplit.setMaximizedControl(bpBrowser);
					drawing.setEditMode(false);
				}
			}
				else
				{
					setChecked(false);
				}
			}
		}
		private SwitchEditModeAction switchEditModeAction = new SwitchEditModeAction(this);
	
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
	
	protected StatusLineManager createStatusLineManager() {
		return super.createStatusLineManager();
	}
	
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		toolBarManager.add(new GroupMarker("group.commonActions"));
		toolBarManager.appendToGroup("group.commonActions", newAction);
		toolBarManager.appendToGroup("group.commonActions", openAction);
		toolBarManager.appendToGroup("group.commonActions", saveAction);
		toolBarManager.add(new Separator("group.editMode"));
		toolBarManager.add(switchEditModeAction);
		toolBarManager.add(new Separator("group.newElements"));
		
		newItemActions = new HashMap();
		newItemOrder = new Vector();
		newItemActions.put("newGeneProduct", new NewElementAction(GmmlDrawing.NEWGENEPRODUCT));
		newItemOrder.add("newGeneProduct");
		newItemActions.put("newLabel", new NewElementAction(GmmlDrawing.NEWLABEL));
		newItemOrder.add("newLabel");
		newItemActions.put("newLineMenu", new NewElementAction(GmmlDrawing.NEWLINEMENU));
		newItemOrder.add("newLineMenu");
		newItemActions.put("newRectangle", new NewElementAction(GmmlDrawing.NEWRECTANGLE));
		newItemOrder.add("newRectangle");
		newItemActions.put("newOval", new NewElementAction(GmmlDrawing.NEWOVAL));
		newItemOrder.add("newOval");
		newItemActions.put("newArc", new NewElementAction(GmmlDrawing.NEWARC));
		newItemOrder.add("newArc");
		newItemActions.put("newBrace", new NewElementAction(GmmlDrawing.NEWBRACE));
		newItemOrder.add("newBrace");
		newItemActions.put("newTbar", new NewElementAction(GmmlDrawing.NEWTBAR));
		newItemOrder.add("newTbar");
		newItemActions.put("newLineShapeMenu", new NewElementAction(GmmlDrawing.NEWLINESHAPEMENU));
		newItemOrder.add("newLineShapeMenu");
		
		toolBarManager.update(true);
		
		return toolBarManager;
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
		dataMenu.add(selectGdbAction);
		dataMenu.add(selectGexAction);
		dataMenu.add(colorSetManagerAction);
		dataMenu.add(convertGexAction);
		MenuManager helpMenu = new MenuManager ("&Help");
		helpMenu.add(aboutAction);
		m.add(fileMenu);
		m.add(editMenu);
		m.add(viewMenu);
		m.add(dataMenu);
		m.add(helpMenu);
		return m;
	}

	public HashMap newItemActions;
	public Vector newItemOrder;

	protected void addNewItemActions(ToolBarManager toolBarManager)
	{
		Iterator it = newItemOrder.iterator();
		while(it.hasNext())
		{
			toolBarManager.appendToGroup("group.newElements",
					(Action)newItemActions.get((String)it.next()));
		}
		toolBarManager.update(true);
	}

	protected void removeNewItemActions(ToolBarManager toolBarManager)
	{
		for(int i = 0; i < newItemOrder.size(); i++)
		{
				toolBarManager.remove("newItemAction");
		}
		toolBarManager.update(true);
	}

	public void deselectNewItemActions()
	{
		Iterator it = newItemActions.values().iterator();
		while(it.hasNext())
		{
			((Action)it.next()).setChecked(false);
		}
		((Action)newItemActions.get("newGeneProduct")).run();
	}
	
	Combo colorSetCombo;
	ToolItem colorSetLabel;
	ToolItem colorSetComboItem;
	ToolItem colorSetSeparator;
	ToolItem colorSetManagerButton;
	final static String COMBO_NO_COLORSET = "No colorset";
	Image colorSetImage;
	private void showColorSetCombo(boolean show)
	{
		ToolBar toolBar = (ToolBar)getToolBarControl();
		if(show)
		{	
			if(colorSetCombo != null && !colorSetCombo.isDisposed())
			{
				colorSetLabel.dispose();
				colorSetCombo.dispose();
				colorSetSeparator.dispose();
				colorSetComboItem.dispose();
				colorSetManagerButton.dispose();
			}
			if(colorSetImage == null)
			{
				try {
				colorSetImage = new Image(null, new FileInputStream("icons/colorset.gif"));
				} catch(Exception e) { System.out.println("colorsetimage not found");}
			}
			
			String[] colorSets = gmmlGex.getColorSetNames();
			String[] comboItems = new String[colorSets.length + 1];
			comboItems[0] = COMBO_NO_COLORSET;
			System.arraycopy(colorSets, 0, comboItems, 1, colorSets.length);

			colorSetSeparator = new ToolItem(toolBar, SWT.SEPARATOR);
			
			colorSetCombo = new Combo(toolBar, SWT.DROP_DOWN | SWT.READ_ONLY);
			colorSetCombo.addSelectionListener(new ColorSetComboListener());
			
			colorSetLabel = new ToolItem(toolBar, SWT.NONE);
			colorSetLabel.setText("Color by:");
			colorSetComboItem = new ToolItem(toolBar, SWT.SEPARATOR);
			colorSetComboItem.setControl(colorSetCombo);
			
			colorSetCombo.setItems(comboItems);
			if(drawing != null)
				colorSetCombo.select(drawing.colorSetIndex + 1);
			else
				colorSetCombo.select(0);
			colorSetCombo.pack();
			
			colorSetComboItem.setWidth(colorSetCombo.getBounds().width);
			colorSetComboItem.setControl(colorSetCombo);
			
			colorSetCombo.setVisible(true);
			
			colorSetManagerButton = new ToolItem(toolBar, SWT.PUSH);
			colorSetManagerButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e)
				{
					colorSetManagerAction.run();
				}
			});
			if(colorSetImage != null)
			{
				colorSetManagerButton.setImage(colorSetImage);
			}
		}
		else
		{
			colorSetCombo.setVisible(false);
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
				}
			}
		}
	}
	
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
		addToolBar(SWT.FLAT | SWT.RIGHT);
	}

	/**
	 * Main method which will be carried out when running the program
	 */
	public static void main(String[] args)
	{
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

		return parent;
		
	};
	
	/**
	 * Creates a new empty drawing and loads it in the frame 
	 */
	private void createNewDrawing()
	{		
		drawing = new GmmlDrawing(sc, SWT.NO_BACKGROUND);
		drawing.setGmmlVision(this);
		
		gmmlData = new GmmlData(drawing);
		
		sc.setContent(drawing);
		drawing.setSize(800, 600);
		
		switchEditModeAction.setChecked(true);
		switchEditModeAction.run();
	}
	
	/**
	 * Opens a GMML representation of a pathway or reaction and creates 
	 * a scrollpane of the drawing, which is loaded in the frame.
	 */
	private void openPathway(String fnPwy)
	{		
		drawing = new GmmlDrawing(sc, SWT.NO_BACKGROUND);
		drawing.setGmmlVision(this);
		drawing.editMode = switchEditModeAction.isChecked();
		
		// initialize new JDOM gmml representation and read the file
		gmmlData = new GmmlData(fnPwy, drawing);
		
		if(gmmlGex.con != null)
		{
			gmmlGex.mappIds = drawing.getMappIds();
			if(gmmlGdb.con != null)
			{
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, gmmlGex.cacheRunnable);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		sc.setContent(drawing);
		
	}

} // end of class