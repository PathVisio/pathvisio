package colorSet;

import gmmlVision.GmmlVision;
import graphics.GmmlLegend;

import java.io.FileInputStream;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import data.GmmlDb;
import data.GmmlGex;
import data.GmmlGex.Sample;

public class ColorSetWindow extends ApplicationWindow {
	public String[] dColumnNames;
	public ArrayList<Integer> dColumnIndex;

	public GmmlGex gmmlGex;
	
	public ColorSetWindow(Shell parent)
	{
		super(parent);
		setBlockOnOpen(true);
	}
	
	public void setColorSets(Vector colorSets)
	{
		gmmlGex.colorSets = colorSets;
	}
	
	public void setColumnNames()
	{
		ArrayList<String> names = new ArrayList<String>();
		dColumnIndex = new ArrayList<Integer>();
		Vector<Sample> samples = new Vector<Sample>(gmmlGex.samples.values());
		Collections.sort(samples);
		for(Sample s : samples)
		{
			if(s.dataType == Types.REAL)
			{
				names.add(s.name);
				dColumnIndex.add(s.idSample);
			}
		}
		dColumnNames = new String[names.size()];
		names.toArray(dColumnNames);
	}
	
	public void setGmmlGex(GmmlGex gmmlGex)
	{
		this.gmmlGex = gmmlGex;
	}
	
	public Vector getColorSets()
	{
		return gmmlGex.colorSets;
	}
	
	public void run()
	{
		setColumnNames();
		gmmlGex.loadColorSets();
		addToolBar(SWT.FLAT);
		open();
		
		csColorGnf.dispose();
		csColorNc.dispose();
		cgColor1.dispose();
		cgColor2.dispose();
		
		gmmlGex.saveColorSets();
	}
	
	TreeViewer treeViewer;
	SashForm sashForm;
	GmmlLegend legend;
	SashForm topSash;
	protected Control createContents(Composite parent)
	{		
		Shell shell = parent.getShell();
		shell.setLocation(parent.getLocation());
		
		shell.setText("Color Set Builder");
		
		topSash = new SashForm(parent, SWT.HORIZONTAL);
		treeViewer = new TreeViewer(topSash);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.addSelectionChangedListener(new TreeSelectionChangedListener());
		treeViewer.getTree().addMouseListener(new TreeMouseListener());
		treeViewer.setInput(this);
				
		sashForm = new SashForm(topSash, SWT.VERTICAL);
		sashForm.setLayout(new FillLayout());
		initiateSashForm();
		
		legend = new GmmlLegend(topSash, SWT.NONE);
		legend.setGmmlGex(gmmlGex);
		
		topSash.setWeights(new int[] {25, 40, 35} );
		
		DragSource ds = new DragSource(treeViewer.getTree(), DND.DROP_MOVE);
		ds.addDragListener(new TreeDragAdapter());
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		DropTarget dt = new DropTarget(treeViewer.getTree(), DND.DROP_MOVE);
		dt.addDropListener(new TreeDropAdapter());
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		
		shell.setSize(550, 350);
		return parent;
	}
	
	Composite cnComposite;
	Composite csComposite;
	Composite cgComposite;
	Composite ccComposite;
	
	private void initiateSashForm() {
		cnComposite = new Composite(sashForm, SWT.NONE);
		csComposite = new Composite(sashForm, SWT.NONE);
		cgComposite = new Composite(sashForm, SWT.NONE);
		ccComposite = new Composite(sashForm, SWT.NONE);
		csComposite.setLayout(new GridLayout(1, true));
		cgComposite.setLayout(new GridLayout(1, true));
		ccComposite.setLayout(new GridLayout(1, true));
		
		setCsGroupComponents();
	    setCgGroupComponents();
	    
		sashForm.setMaximizedControl(cnComposite);
	}
	
	Text csNameText;
	CLabel csCLabelNc;
	Button csColorButtonNc;
	CLabel csCLabelGnf;
	Button csColorButtonGnf;
	Color csColorNc;
	Color csColorGnf;
	public void setCsGroupComponents()
	{		
		GridData span2ColsGrid = new GridData(GridData.FILL_HORIZONTAL);
		span2ColsGrid.horizontalSpan = 2;
		GridData csCLabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		csCLabelGrid.widthHint = csCLabelGrid.heightHint = 15;
		GridData colorButtonGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		colorButtonGrid.widthHint = colorButtonGrid.heightHint = 15;
		
		Group csGroup = new Group(csComposite, SWT.SHADOW_IN);

		csGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		csGroup.setLayout(new GridLayout(3, false));
	    csGroup.setText("Color set options");
	    
	    Button csButton = new Button(csComposite, SWT.PUSH);
	    	    
	    csButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	    csButton.setText("Save");
	    csButton.addSelectionListener(new csButtonAdapter());
	    
	    Label csNameLabel = new Label(csGroup, SWT.CENTER);
	    csNameText = new Text(csGroup, SWT.SINGLE | SWT.BORDER);
	    
	    Label csColorLabelNc = new Label(csGroup, SWT.CENTER);
	    csCLabelNc = new CLabel(csGroup, SWT.SHADOW_IN);
	    csColorButtonNc = new Button(csGroup, SWT.PUSH);
	    csColorButtonNc.addSelectionListener(new ColorButtonAdapter());
	    Label csColorLabelGnf = new Label(csGroup, SWT.CENTER);
	    csCLabelGnf = new CLabel(csGroup, SWT.SHADOW_IN);
	    csColorButtonGnf = new Button(csGroup, SWT.PUSH);
	    csColorButtonGnf.addSelectionListener(new ColorButtonAdapter());
	    
	    csColorLabelNc.setText("No criteria met color:");
	    csColorLabelGnf.setText("Gene not found color:");
	    csColorButtonNc.setText("...");
	    csColorButtonGnf.setText("...");
	    
	    csColorNc = new Color(getShell().getDisplay(), GmmlColorSet.COLOR_NO_CRITERIA_MET);
	    csColorGnf = new Color(getShell().getDisplay(), GmmlColorSet.COLOR_NO_GENE_FOUND);
	    csCLabelNc.setLayoutData(csCLabelGrid);
	    csCLabelGnf.setLayoutData(csCLabelGrid);
	    csCLabelNc.setBackground(csColorNc);
	    csCLabelNc.setBackground(csColorGnf);
	    csColorButtonNc.setLayoutData(colorButtonGrid);
	    csColorButtonGnf.setLayoutData(colorButtonGrid);
	    
	    csNameLabel.setText("Name:");
	    csNameText.setLayoutData(span2ColsGrid);
	}
	
	Text cgNameText;
	Combo cgCombo;
	Text cgColorText1;
	Text cgColorText2;
	Button cgColorButton1;
	Button cgColorButton2;
	CLabel cgCLabel1;
	CLabel cgCLabel2;
	Color cgColor1;
	Color cgColor2;
	ColorDialog cgColorDialog;
	Button cgButton;
	
	public void setCgGroupComponents()
	{			    
	    //TODO: add validator to colortext
	    
		GridData span4ColsGrid = new GridData(GridData.FILL_HORIZONTAL);
		span4ColsGrid.horizontalSpan = 4;
		GridData cgCLabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		cgCLabelGrid.widthHint = cgCLabelGrid.heightHint = 15;
		GridData colorTextGrid = new GridData(GridData.FILL_HORIZONTAL);
		GridData colorButtonGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		colorButtonGrid.widthHint = colorButtonGrid.heightHint = 15;
		GridData cgButtonGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
		cgButtonGrid.horizontalSpan = 5;
		
		cgColor1 = new Color(getShell().getDisplay(), 255, 0, 0);
	    cgColor2 = new Color(getShell().getDisplay(), 0, 255, 0);
	    cgColorDialog = new ColorDialog(getShell(), SWT.NONE);
	    
	    Group cgGroup = new Group(cgComposite, SWT.SHADOW_IN);
	    Button cgButton = new Button(cgComposite, SWT.PUSH);
	    
	    Label cgNameLabel = new Label(cgGroup, SWT.CENTER);
	    cgNameText = new Text(cgGroup, SWT.SINGLE | SWT.BORDER);
	    Label cgComboLabel = new Label(cgGroup, SWT.CENTER);
	    cgCombo = new Combo(cgGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
	    Label cgColorLabel1 = new Label(cgGroup, SWT.CENTER);
	    cgCLabel1 = new CLabel(cgGroup, SWT.SHADOW_IN);
	    cgColorButton1 = new Button(cgGroup, SWT.PUSH);
	    cgColorButton1.addSelectionListener(new ColorButtonAdapter());
	    Label cgValueLabel1 = new Label(cgGroup, SWT.CENTER);
	    cgColorText1 = new Text(cgGroup, SWT.SINGLE | SWT.BORDER);
	    Label cgColorLabel2 = new Label(cgGroup, SWT.CENTER);
	    cgCLabel2 = new CLabel(cgGroup, SWT.SHADOW_IN);
	    cgColorButton2 = new Button(cgGroup, SWT.PUSH);
	    cgColorButton2.addSelectionListener(new ColorButtonAdapter());
	    Label cgValueLabel2 = new Label(cgGroup, SWT.CENTER);
	    cgColorText2 = new Text(cgGroup, SWT.SINGLE | SWT.BORDER);
	    
	    cgColorText1.setLayoutData(colorTextGrid);
	    cgColorText2.setLayoutData(colorTextGrid);
	    cgGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		cgGroup.setLayout(new GridLayout(5, false));
	    cgGroup.setText("Color by gradient settings");
	    
	    cgNameLabel.setText("Name:");
	    cgNameText.setLayoutData(span4ColsGrid);
	    
	    cgComboLabel.setText("Data column:");
	    cgCombo.setItems(dColumnNames);
	    cgCombo.setLayoutData(span4ColsGrid);
	    
	    cgColorLabel1.setText("Start color:");
	    cgColorLabel2.setText("End color:");
	    cgCLabel1.setBackground(cgColor1);
	    cgCLabel2.setBackground(cgColor2);
	    cgCLabel1.setLayoutData(cgCLabelGrid);
	    cgCLabel2.setLayoutData(cgCLabelGrid);
	    cgColorButton1.setText("...");
	    cgColorButton2.setText("...");
	    cgColorButton1.setLayoutData(colorButtonGrid);
	    cgColorButton2.setLayoutData(colorButtonGrid);
	   
	    cgValueLabel1.setText("at value:");
	    cgValueLabel2.setText("at value:");
	    
	    cgButton.setLayoutData(cgButtonGrid);
	    cgButton.setText("Save");
	    cgButton.addSelectionListener(new CgButtonAdapter());
	}
	
	class CgButtonAdapter extends SelectionAdapter {
		public CgButtonAdapter() {
			super();
		}
		public void widgetSelected(SelectionEvent e) {
			if(cgNameText.getText().equals("")) {
				MessageDialog.openError(getShell(), "Error", "Specify a name");
				return;
			}
			if(cgCombo.getText().equals("")) {
				MessageDialog.openError(getShell(), "Error", "Choose a data column");
				return;
			}
			GmmlColorGradient cg = (GmmlColorGradient)
    		((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
			cg.name = cgNameText.getText();
			cg.dataColumn = dColumnIndex.get(cgCombo.getSelectionIndex());
			cg.colorStart = cgColor1.getRGB();
			cg.colorEnd = cgColor2.getRGB();
			cg.valueStart = Double.parseDouble(cgColorText1.getText());
			cg.valueEnd = Double.parseDouble(cgColorText2.getText());
			legend.setVisible(true);
			legend.redraw();
			treeViewer.refresh();
		}
	}
	
	class ColorButtonAdapter extends SelectionAdapter {
		public ColorButtonAdapter() {
			super();
		}
    	public void widgetSelected(SelectionEvent e) {
    		RGB rgb = cgColorDialog.open();
    		if (rgb != null) {
    			if(e.widget == cgColorButton1) {
    				cgColor1 = new Color(getShell().getDisplay(), rgb);
    				cgCLabel1.setBackground(cgColor1);
    			}
    			if(e.widget == cgColorButton2) {
    				cgColor2 = new Color(getShell().getDisplay(), rgb);
    				cgCLabel2.setBackground(cgColor2);
    			}
    			if(e.widget == csColorButtonGnf) {
    				csColorGnf = new Color(getShell().getDisplay(), rgb);
    				csCLabelGnf.setBackground(csColorGnf);
    			}
    			if(e.widget == csColorButtonNc) {
    				csColorNc = new Color(getShell().getDisplay(), rgb);
    				csCLabelNc.setBackground(csColorNc);
    			}
    		}
    	}
    }
	
	public void setRightCompositeContents(Object element) {	
		if(element == null) {
			sashForm.setMaximizedControl(cnComposite);
			legend.setVisible(false);
			return;
		}
		if(element instanceof GmmlColorSet) {
			GmmlColorSet cs = (GmmlColorSet)element;
			csNameText.setText(cs.name);
			csColorGnf = new Color(getShell().getDisplay(), cs.color_gene_not_found);
			csColorNc = new Color(getShell().getDisplay(), cs.color_no_criteria_met);
			legend.colorSetIndex = gmmlGex.colorSets.indexOf(cs);
			legend.setVisible(true);
			legend.redraw();
			sashForm.setMaximizedControl(csComposite);
			topSash.layout();
			return;
		}
		if(element instanceof GmmlColorGradient) {
			GmmlColorGradient cg = (GmmlColorGradient)element;
			cgNameText.setText(cg.name);
			cgCombo.select(dColumnIndex.indexOf(cg.dataColumn));
			cgColorText1.setText(Double.toString(cg.valueStart));
			cgColorText2.setText(Double.toString(cg.valueEnd));
			cgColor1 = new Color(getShell().getDisplay(), cg.colorStart);
			cgColor2 = new Color(getShell().getDisplay(), cg.colorEnd);
			cgCLabel1.setBackground(cgColor1);
			cgCLabel2.setBackground(cgColor2);
			sashForm.setMaximizedControl(cgComposite);
			legend.colorSetIndex = gmmlGex.colorSets.indexOf(cg.parent);
			legend.setVisible(true);
			legend.redraw();
			topSash.layout();
			return;
		}
	}
	
	class csButtonAdapter extends SelectionAdapter {
		public csButtonAdapter() {
			super();
		}
		public void widgetSelected(SelectionEvent e) {
			if(csNameText.getText().equals("")) {
				MessageDialog.openError(getShell(), "Error", "Specify a name");
				return;
			}
			GmmlColorSet cs = (GmmlColorSet)
			((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
			cs.name = csNameText.getText();
			cs.color_gene_not_found = csColorGnf.getRGB();
			cs.color_no_criteria_met = csColorNc.getRGB();
			legend.setVisible(true);
			legend.redraw();
		}
	}
	
	class NewCsButtonAdapter extends SelectionAdapter {
		
		public NewCsButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			InputDialog d = new InputDialog(Display.getCurrent().getActiveShell(),
					  "New Color Set", "Name of new Color Set:", "", null);
			int rc = d.open();
			if(rc == Window.OK) {
				GmmlColorSet cs = new GmmlColorSet(d.getValue());
				gmmlGex.colorSets.add(cs);
				treeViewer.refresh();
				treeViewer.setSelection(new StructuredSelection(cs), true);
			}
		}
	}
    
	static final String TRANSFER_COLORSET = "COLORSET";
	static final String TRANSFER_CSOBJECT = "CSOBJECT";
	static final String TRANSFER_SEP = ":";
	
    private class TreeDragAdapter extends DragSourceAdapter {
    	public void dragStart(DragSourceEvent e) {
    		Object selected = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
    		if(selected == null)
    		{
    			e.doit = false;
    		}
    	}
    	
    	public void dragSetData(DragSourceEvent e) {
    		Object selected = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
    		e.data = "NONE";
    		if(selected instanceof GmmlColorSet)
    		{
    			e.data = TRANSFER_COLORSET + TRANSFER_SEP + gmmlGex.colorSets.indexOf(selected);
    		}
    		else if(selected instanceof GmmlColorSetObject)
    		{
    			GmmlColorSetObject cso = (GmmlColorSetObject)selected;
    			int csIndex = gmmlGex.colorSets.indexOf(cso.parent);
    			int csoIndex = cso.parent.colorSetObjects.indexOf(cso);
    			e.data = TRANSFER_CSOBJECT + TRANSFER_SEP + csIndex + TRANSFER_SEP + csoIndex;
    		}
    		System.out.println(e.data);
    	}
    }
    
    private class TreeDropAdapter extends DropTargetAdapter {
    	public void drop(DropTargetEvent e) {
    		TreeItem item = (TreeItem)e.item;
    		if(item != null)
    		{
    			Object selected = item.getData();
    			String[] data = ((String)e.data).split(":");
    			if(data[0].equals(TRANSFER_COLORSET))
    			{
    				int i = Integer.parseInt(data[1]);
    				GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(i);
    				if (selected instanceof GmmlColorSetObject)
    				{
    					selected = ((GmmlColorSetObject)selected).parent;
    				}
    					System.out.println(gmmlGex.colorSets.indexOf(cs) + "," + gmmlGex.colorSets.indexOf(selected));
    					moveElement(gmmlGex.colorSets, cs, gmmlGex.colorSets.indexOf(selected));
    					System.out.println(gmmlGex.colorSets.indexOf(cs));
    			}
    			else if(data[0].equals(TRANSFER_CSOBJECT))
    			{
    				int csIndex = Integer.parseInt(data[1]);
    				int csoIndex = Integer.parseInt(data[2]);
    				GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(csIndex);
    				GmmlColorSetObject cso = (GmmlColorSetObject)cs.colorSetObjects.get(csoIndex);
    				if(selected instanceof GmmlColorSet)
    				{
    					GmmlColorSet csNew = (GmmlColorSet)selected;
    					csNew.addObject(cso);
    					cs.colorSetObjects.remove(cso);
    					cso.parent = csNew;
    				}
    				else if (selected instanceof GmmlColorSetObject)
    				{
    					System.out.println(((GmmlColorSetObject)selected).parent+ "," +cs);
    					if(((GmmlColorSetObject)selected).parent == cs)
    					{
    						moveElement(cs.colorSetObjects, cso, cs.colorSetObjects.indexOf(selected));
    					}
    					else
    					{
    						GmmlColorSet csNew = ((GmmlColorSetObject)selected).parent;
    						csNew.colorSetObjects.add(csNew.colorSetObjects.indexOf(selected), cso);
    						cs.colorSetObjects.remove(cso);
    						cso.parent = csNew;
    					}
    				}
    			}
    			treeViewer.refresh();
    		}
    	}
    }
    
    public void moveElement(Vector v, Object o, int newIndex)
    {
    	v.remove(o);
    	v.add(newIndex, o);
    }
    
	private class TreeMouseListener extends MouseAdapter {
		public TreeMouseListener() {
			super();
		}
		public void mouseDown(MouseEvent e) {
			if(e.button == 3)
			{
				Tree tree = treeViewer.getTree();
				TreeItem item = tree.getItem(new Point(e.x, e.y));
				if(item != null)
				{
					MenuManager mgr = new MenuManager();
					mgr.add(new DeleteAction(item.getData()));
					Menu m = mgr.createContextMenu(tree);
					tree.setMenu(m);
				}
			}
		}
	}
	
	private class DeleteAction extends Action 
	{
		Object object;
		public DeleteAction (Object o)
		{
			this.object = o;
			setText ("&Delete");
			setToolTipText ("Delete item");
		}
		public void run () {
			if(object instanceof GmmlColorSet)
			{
				gmmlGex.colorSets.remove(object);
			}
			else if (object instanceof GmmlColorSetObject)
			{
				((GmmlColorSetObject)object).parent.colorSetObjects.remove(object);
			}
			treeViewer.refresh();
		}
	}
	
	private class NewCsAction extends Action
	{
		public NewCsAction () 
		{	
			setText("&New color set");
			setToolTipText("Add a new color set");
		}
		
		public void run()
		{
			InputDialog d = new InputDialog(Display.getCurrent().getActiveShell(),
					  "New Color Set", "Name of new Color Set:", "", null);
			int rc = d.open();
			if(rc == Window.OK) {
				GmmlColorSet cs = new GmmlColorSet(d.getValue());
				gmmlGex.colorSets.add(cs);
				treeViewer.refresh();
				treeViewer.setSelection(new StructuredSelection(cs), true);
			}
		}
	}
	
	private class NewCoAction extends Action
	{
		NewCoDialog dialog;
		public NewCoAction()
		{
			setText("New criterion");
			setToolTipText("Add a new criterion to the selected colorset");
		}
		
		public void run()
		{
			dialog = new NewCoDialog(Display.getCurrent().getActiveShell());
			dialog.open();
		}
	}
	
	protected ToolBarManager createToolBarManager(int style) 
	{
		ToolBarManager toolBarManager = new ToolBarManager(style);
		toolBarManager.add(new NewCsAction());
		toolBarManager.add(new NewCoAction());
		
		return toolBarManager;
	}
	
	private class NewCoDialog extends Dialog
	{		
		public NewCoDialog(Shell parent)
		{
			super(parent);
		}
		
		public void open()
		{
			Shell parent = getParent();
			final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
			
			shell.setLayout(new GridLayout(2, false));
			
			Group csGroup = new Group(shell, SWT.SHADOW_IN);
			csGroup.setText("New color criterion");
			csGroup.setLayout(new GridLayout(2, false));
			GridData csGroupGrid = new GridData(GridData.FILL_BOTH);
			csGroupGrid.horizontalSpan = 2;
			csGroup.setLayoutData(csGroupGrid);
			
			Label csTextLabel = new Label(csGroup, SWT.CENTER);
		    final Text csText = new Text(csGroup, SWT.SINGLE | SWT.BORDER);
		    Label csComboLabel = new Label(csGroup, SWT.CENTER);
		    final Combo csCombo = new Combo(csGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		    
		    csTextLabel.setText("Name:");
		    csText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    
			final String[] comboText = new String[] { "Color by gradient", "Color by expression" };
		    csComboLabel.setText("Type:");
		    csCombo.setItems(comboText);
		    csCombo.setText(comboText[0]);
		    
		    final Button buttonOk = new Button(shell, SWT.PUSH);
		    buttonOk.setText("Ok");
		    buttonOk.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
		    final Button buttonCancel = new Button(shell, SWT.PUSH);
		    buttonCancel.setText("Cancel");
		    buttonCancel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		    
			buttonOk.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		Object s = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
		    		GmmlColorSet cs = null;
		    		if(s instanceof GmmlColorSet)
		    		{
		    			cs = (GmmlColorSet)s;
		    		} else if (s instanceof GmmlColorSetObject)
		    		{
		    			cs = ((GmmlColorSetObject)s).parent;
		    		}
		    		if(csText.getText().equals("")) {
		    			MessageDialog.openError(getShell(), "Error", "Specify a name for the Color Set");
		    			return;
		    		}
		    		if(comboText[0].equals(csCombo.getText())) {
		    			GmmlColorGradient cg = new GmmlColorGradient(cs, csText.getText());
		    			cs.addObject(cg);
		    			treeViewer.refresh();
		    			treeViewer.setSelection(new StructuredSelection(cg));
		   
		    			shell.dispose();
		    		}
		    	}
		    });
			
			buttonCancel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					shell.dispose();
				}
			});
			
		    shell.pack();
		    shell.open();
		    
		    Display display = parent.getDisplay();
		    while (!shell.isDisposed()){
		    	if(!display.readAndDispatch())
		    		display.sleep();
		    }
		}
	}
	
	private class TreeSelectionChangedListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent e)
		{
			if(e.getSelection().isEmpty()) {
				setRightCompositeContents(null);
			} else {
				Object s = ((IStructuredSelection)e.getSelection()).getFirstElement();
				setRightCompositeContents(s);
			}
		}
	}
	private class TreeContentProvider implements ITreeContentProvider {
		
		public void dispose() {	}
		
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof GmmlColorSet)
				return ((GmmlColorSet)parentElement).getColorSetObjects().toArray();
			return new Object[] {};
		}
		
		public Object[] getElements(Object inputElement) {
				return gmmlGex.colorSets.toArray();
		}
		
		public Object getParent(Object element) {
			if(element instanceof GmmlColorSetObject)
				return ((GmmlColorSetObject)element).getParent();
			return null;
		}
		
		public boolean hasChildren(Object element) {
			if(element instanceof GmmlColorSet)
				return ((GmmlColorSet)element).getColorSetObjects().size() > 0;
			return false;
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			//TODO: rename
		}
		
	}
	
	private class TreeLabelProvider implements ILabelProvider {
		private java.util.List listeners;
		private Image colorSetImage;
		private Image gradientImage;
		
		
		public TreeLabelProvider() {
			listeners = new ArrayList();
			try {
				colorSetImage = new Image(null, new FileInputStream("icons/colorset.gif"));
				gradientImage = new Image(null, new FileInputStream("icons/colorgradient.gif"));
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		
		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}
		
		public void dispose() {
			if(colorSetImage != null)
				colorSetImage.dispose();
			if(gradientImage != null)
				gradientImage.dispose();
		}
		
		public Image getImage(Object element) { 
			if(element instanceof GmmlColorSet) {
				return colorSetImage;
			}
			if(element instanceof GmmlColorGradient) {
				return gradientImage;
			}
			return null;
		}
		
		public String getText(Object element) {
			if(element instanceof GmmlColorSet)
				return ((GmmlColorSet)element).name;
			if(element instanceof GmmlColorSetObject)
				return ((GmmlColorSetObject)element).name;
			return "";
		}
		
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		
		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}
	}
}
