package colorSet;

import gmmlVision.GmmlVision;
import graphics.GmmlLegend;

import java.io.FileInputStream;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import colorSet.GmmlColorGradient.ColorValuePair;

import data.GmmlGex;
import data.GmmlGex.Sample;

import util.SwtUtils;

public class ColorSetWindow extends ApplicationWindow {
	public String[] cgColumnNames;
	public ArrayList<Integer> cgColumnIndex;

	public GmmlGex gmmlGex;
	
	public int lastColorSetIndex;
	
	public ColorSetWindow(Shell parent)
	{
		super(parent);
		setBlockOnOpen(true);
	}
	
	public void setColorSets(Vector colorSets)
	{
		gmmlGex.colorSets = colorSets;
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
		gmmlGex.loadColorSets();
		open();
		
		csColorGnf.dispose();
		csColorNc.dispose();
	}
	
	TableViewer coTableViewer;
	SashForm sashForm;
	GmmlLegend legend;
	SashForm topSash;
	Combo csCombo;
	Composite coTableComposite;
	
	public boolean close()
	{
		if(!saveMaximizedControl())
		{
			return false;
		}
		saveToGex();
		return super.close();
	}
	
	public boolean close(boolean save)
	{
		if(save)
			return close();
		else
			return super.close();
	}
	
	protected Control createContents(Composite parent)
	{		
		Shell shell = parent.getShell();
		shell.setLocation(parent.getLocation());
		
		shell.setText("Color Set Builder");
		
		Composite topComposite = new Composite(parent, SWT.NULL);
		topComposite.setLayout(new GridLayout(2, false));
		
		topSash = new SashForm(topComposite, SWT.HORIZONTAL);
		GridData sashGrid = new GridData(GridData.FILL_BOTH);
		sashGrid.horizontalSpan = 2;
		topSash.setLayoutData(sashGrid);
		
		Composite coComposite = new Composite(topSash, SWT.NULL);
		coComposite.setLayout(new GridLayout(1, true));
		
		GridData tableGrid = new GridData(GridData.FILL_BOTH);
		tableGrid.horizontalSpan = 2;
		GridData comboGrid = new GridData(GridData.FILL_HORIZONTAL);
		comboGrid.horizontalSpan = 3;
		comboGrid.widthHint = 100;
		
		Group csComboGroup = new Group(coComposite, SWT.SHADOW_ETCHED_IN);
		csComboGroup.setText("Color sets");
		csComboGroup.setLayout(new GridLayout(3, false));
		csComboGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		csCombo = new Combo(csComboGroup, SWT.SINGLE | SWT.READ_ONLY);
		csCombo.setItems(gmmlGex.getColorSetNames());
		csCombo.setLayoutData(comboGrid);
		csCombo.addSelectionListener(new CsComboSelectionAdapter());
		
		Button newCsButton = new Button(csComboGroup, SWT.PUSH);
		newCsButton.setText("New");
		newCsButton.addSelectionListener(new NewCsButtonAdapter());
		newCsButton.pack();
		Button editCsButton = new Button(csComboGroup, SWT.PUSH);
		editCsButton.setText("Edit");
		editCsButton.addSelectionListener(new EditCsButtonAdapter());
		editCsButton.pack();
		Button deleteCsButton = new Button(csComboGroup, SWT.PUSH);
		deleteCsButton.setText("Delete");
		deleteCsButton.addSelectionListener(new DeleteCsButtonAdapter());
		deleteCsButton.pack();

		Group coTableGroup = new Group(coComposite, SWT.SHADOW_ETCHED_IN);
		coTableGroup.setText("Color criteria");
		coTableGroup.setLayout(new GridLayout(2, false));
		coTableGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Button newCoButton = new Button(coTableGroup, SWT.PUSH);
		newCoButton.setText("New");
		newCoButton.addSelectionListener(new NewCoButtonAdapter());
		newCoButton.pack();
		Button removeCoButton = new Button(coTableGroup, SWT.PUSH);
		removeCoButton.setText("Delete");
		removeCoButton.addSelectionListener(new DeleteCoButtonAdapter());
		removeCoButton.pack();
		
		coTableComposite = new Composite(coTableGroup, SWT.NONE);
		coTableComposite.setLayout(new FillLayout());
		coTableComposite.setLayoutData(tableGrid);
		
		Table table = new Table(coTableComposite, SWT.BORDER | SWT.SINGLE);
		table.addControlListener(new TableGrowListener(table));
		TableColumn coCol = new TableColumn(table, SWT.LEFT);
		coCol.setText("Name");

		coTableViewer = new TableViewer(table);
		coTableViewer.setContentProvider(new CoTableContentProvider());
		coTableViewer.setLabelProvider(new CoTableLabelProvider());
		coTableViewer.addSelectionChangedListener(new CoTableSelectionChangedListener());
		
		sashForm = new SashForm(topSash, SWT.VERTICAL);
		sashForm.setLayout(new FillLayout());
		initiateSashForm();
		
		legend = new GmmlLegend(topSash, SWT.NONE, false);
		legend.setGmmlGex(gmmlGex);
		
		topSash.setWeights(new int[] {25, 55, 20} );
		
		DragSource ds = new DragSource(coTableViewer.getTable(), DND.DROP_MOVE);
		ds.addDragListener(new CoTableDragAdapter());
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		DropTarget dt = new DropTarget(coTableViewer.getTable(), DND.DROP_MOVE);
		dt.addDropListener(new CoTableDropAdapter());
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		

		Button cancelButton = new Button(topComposite, SWT.PUSH);
		cancelButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				restoreFromGex();
				close(false);
			}
		});
		cancelButton.pack();
		Button okButton = new Button(topComposite, SWT.PUSH);
		okButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		okButton.setText("Ok");
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveToGex();
				getShell().close();
			}
		});
		okButton.pack();
		
		csComboGroup.pack();
		coTableGroup.pack();
		legend.pack();

		shell.setSize(topSash.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 50, 500);
		
		csCombo.select(lastColorSetIndex);
		if(csCombo.getSelectionIndex() > -1)
		{
			GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
			coTableViewer.setInput(cs);
			setRightCompositeContents(cs);
		}
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
	Table sampleTable;
	TableViewer sampleTableViewer;
	List sampleList;
	ListViewer sampleListViewer;
	Group csTableGroup;
	Composite csTableComposite;
	ColorDialog csColorDialog;
	List<String> colNames = Arrays.asList(
			new String[] {"Sample name", "Type"});
	public void setCsGroupComponents()
	{		
		GridData csNameTextGrid = new GridData(GridData.FILL_HORIZONTAL);
		csNameTextGrid.horizontalSpan = 2;
		csNameTextGrid.widthHint = 100;
		GridData csCLabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		csCLabelGrid.widthHint = csCLabelGrid.heightHint = 15;
		GridData colorButtonGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		colorButtonGrid.widthHint = colorButtonGrid.heightHint = 15;
		GridData tableGroupGrid = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		tableGroupGrid.heightHint = 200;
		tableGroupGrid.widthHint = 400;
		
		Group csGroup = new Group(csComposite, SWT.SHADOW_IN);

		csGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		csGroup.setLayout(new GridLayout(3, false));
	    csGroup.setText("Color set options");
	    
	    csTableGroup = new Group(csComposite, SWT.SHADOW_IN);
	    
	    csTableGroup.setLayoutData(tableGroupGrid);
	    csTableGroup.setLayout(new GridLayout(3, false));
	    csTableGroup.setText("Color set data");
	    
	    Button csButton = new Button(csComposite, SWT.PUSH);
	    	    
	    csButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	    csButton.setText("Save");
	    csButton.addSelectionListener(new csButtonAdapter());
	    
	    // csGroup
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
	    
	    csColorNc = SwtUtils.changeColor(csColorNc, GmmlColorSet.COLOR_NO_CRITERIA_MET, getShell().getDisplay());
	    csColorGnf = SwtUtils.changeColor(csColorGnf, GmmlColorSet.COLOR_NO_GENE_FOUND, getShell().getDisplay());
	    csCLabelNc.setLayoutData(csCLabelGrid);
	    csCLabelGnf.setLayoutData(csCLabelGrid);
	    csCLabelNc.setBackground(csColorNc);
	    csCLabelNc.setBackground(csColorGnf);
	    csColorButtonNc.setLayoutData(colorButtonGrid);
	    csColorButtonGnf.setLayoutData(colorButtonGrid);
	    
	    csNameLabel.setText("Name:");
	    csNameText.setLayoutData(csNameTextGrid);
	    
	    csNameText.addSelectionListener(new inputSelectionAdapter());
	    
	    // csTableGroup    
	    Label tableLabel = new Label(csTableGroup, SWT.LEFT);
	    tableLabel.setText("Available samples");
	    GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
	    labelGrid.horizontalSpan = 2;
	    tableLabel.setLayoutData(labelGrid);
	    tableLabel.pack();
	    Label listLabel = new Label(csTableGroup, SWT.LEFT);
	    listLabel.setText("Selected samples");
	    tableLabel.pack();
	    	    
	    sampleListViewer = new ListViewer(csTableGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    sampleListViewer.setContentProvider(new CsListContentProvider());
	    sampleListViewer.setLabelProvider(new CsListLabelProvider());
	    sampleListViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    Composite buttonComposite = new Composite(csTableGroup, SWT.NONE);
	    buttonComposite.setLayout(new RowLayout(SWT.VERTICAL));
	    Button addSampleButton = new Button(buttonComposite, SWT.PUSH);
	    addSampleButton.addSelectionListener(new AddSampleButtonAdapter());
	    addSampleButton.setText(">");
	    Button removeSampleButton = new Button(buttonComposite, SWT.PUSH);
	    removeSampleButton.addSelectionListener(new RemoveSampleButtonAdapter());
	    removeSampleButton.setText("<");
	    
	    csTableComposite = new Composite(csTableGroup, SWT.NONE);
	    csTableComposite.setLayout(new FillLayout());
	    csTableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
	    sampleTable = new Table(csTableComposite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
	    
	    sampleTable.setHeaderVisible(true);
	    sampleTable.setLinesVisible(false);
	    sampleTable.addControlListener(new TableGrowListener(sampleTable));
	    
	    TableColumn nameCol = new TableColumn(sampleTable, SWT.LEFT);
	    TableColumn typeCol = new TableColumn(sampleTable, SWT.LEFT);
	    nameCol.setText(colNames.get(0));
	    typeCol.setText(colNames.get(1));

	    typeCol.setWidth(70);
	    
	    sampleTableViewer = new TableViewer(sampleTable);
	    
	    sampleTableViewer.setLabelProvider(new CsTableLabelProvider());
	    sampleTableViewer.setContentProvider(new CsTableContentProvider());
	    sampleTableViewer.setColumnProperties(colNames.toArray(new String[colNames.size()]));
	    CellEditor[] cellEditors = new CellEditor[2];
	    cellEditors[0] = new TextCellEditor(sampleTable);
	    cellEditors[1] = new ComboBoxCellEditor(sampleTable, GmmlColorSet.SAMPLE_TYPES);
	    sampleTableViewer.setCellEditors(cellEditors);
	    sampleTableViewer.setCellModifier(new CsTableCellModifier());

	    csColorDialog = new ColorDialog(getShell());
	    
	    csTableGroup.pack();
	    csGroup.pack();
	}
	
	Text cgNameText;
	Combo cgCombo;
	Button cgButton;
	Table cgColorTable;
	TableViewer cgColorTableViewer;
	Group cgGroup;
	public static final String[] cgColorTableCols = new String[] {"Color", "Value"};
	
	public void setCgGroupComponents()
	{			    
	    //TODO: add validator to colortext (only double)		
		cgGroup = new Group(cgComposite, SWT.SHADOW_IN);
		cgGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		cgGroup.setLayout(new GridLayout(2, false));
		cgGroup.setText("Color by gradient settings");
		GridData cgButtonGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
		cgButtonGrid.horizontalSpan = 2;
		
		Label cgNameLabel = new Label(cgGroup, SWT.CENTER);
	    cgNameLabel.setText("Name:");
	    
		cgNameText = new Text(cgGroup, SWT.SINGLE | SWT.BORDER);
	    cgNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
		Label cgComboLabel = new Label(cgGroup, SWT.CENTER);
	    cgComboLabel.setText("Data column:");
	    cgCombo = new Combo(cgGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
	    cgCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
	    cgNameText.addSelectionListener(new inputSelectionAdapter());
	    cgCombo.addSelectionListener(new inputSelectionAdapter());
	    
	    Button addColorButton = new Button(cgGroup, SWT.PUSH);
	    addColorButton.setText("Add color");
	    Button removeColorButton = new Button(cgGroup, SWT.PUSH);
	    removeColorButton.setText("Remove color");
	    addColorButton.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		GmmlColorGradient cg = (GmmlColorGradient)
	    		((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
	    		cg.colorValuePairs.add(cg.new ColorValuePair(new RGB(255, 0, 0), 0));
	    		cgColorTableViewer.refresh();
	    	}
	    });
	    removeColorButton.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		GmmlColorGradient cg = (GmmlColorGradient)
	    		((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
	    		ColorValuePair cvp = (ColorValuePair)
	    		((IStructuredSelection)cgColorTableViewer.getSelection()).getFirstElement();
	    		cg.colorValuePairs.remove(cvp);
	    		cgColorTableViewer.refresh();
	    	}
	    });
	    cgColorTable = new Table(cgGroup, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
	    GridData cGrid = new GridData(GridData.FILL_BOTH);
	    cGrid.horizontalSpan = 2;
	    cgColorTable.setLayoutData(cGrid);
	    
	    cgColorTable.setHeaderVisible(true);
	    cgColorTable.addControlListener(new TableGrowListener(cgColorTable));
	    TableColumn colorCol = new TableColumn(cgColorTable, SWT.LEFT);
	    TableColumn valueCol = new TableColumn(cgColorTable, SWT.LEFT);
	    valueCol.setText(cgColorTableCols[1]);
	    colorCol.setText(cgColorTableCols[0]);

	    colorCol.setWidth(40);
	    
	    cgColorTableViewer = new TableViewer(cgColorTable);
	    
	    cgColorTableViewer.setLabelProvider(new CgColorTableLabelProvider());
	    cgColorTableViewer.setContentProvider(new CgColorTableContentProvider());
	    cgColorTableViewer.setColumnProperties(cgColorTableCols);
	    CellEditor[] cellEditors = new CellEditor[2];
	    cellEditors[1] = new TextCellEditor(cgColorTable);
	    cellEditors[0] = new ColorCellEditor(cgColorTable);
	    cgColorTableViewer.setCellEditors(cellEditors);
	    cgColorTableViewer.setCellModifier(new CgColorTableCellModifier());

		Button cgButton = new Button(cgComposite, SWT.PUSH);

		cgButton.setLayoutData(cgButtonGrid);
		cgButton.setText("Save");
		cgButton.addSelectionListener(new CgButtonAdapter());
		
	    cgGroup.pack();
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
			csColorNc = SwtUtils.changeColor(csColorNc, cs.color_no_criteria_met, getShell().getDisplay());
		    csColorGnf = SwtUtils.changeColor(csColorGnf, cs.color_gene_not_found, getShell().getDisplay());
			csCLabelGnf.setBackground(csColorGnf);
			csCLabelNc.setBackground(csColorNc);
			sampleTableViewer.setInput(cs);
			sampleListViewer.setInput(cs);
			legend.colorSetIndex = gmmlGex.colorSets.indexOf(cs);
			legend.setVisible(true);
			legend.resetContents();
			sashForm.setMaximizedControl(csComposite);
			topSash.layout();
			return;
		}
		if(element instanceof GmmlColorGradient) {
			GmmlColorGradient cg = (GmmlColorGradient)element;
			cgColorTableViewer.setInput(cg);
			cgNameText.setText(cg.name);
			if(!setCgComboItems(cg.parent)) {
				return;
			}
			cgCombo.select(cgColumnIndex.indexOf(cg.getDataColumn()));
			sashForm.setMaximizedControl(cgComposite);
			legend.colorSetIndex = gmmlGex.colorSets.indexOf(cg.parent);
			legend.setVisible(true);
			legend.resetContents();
			topSash.layout();
			return;
		}
	}

	private boolean setCgComboItems(GmmlColorSet cs)
	{
		cgColumnNames = new String[cs.useSamples.size() + 1];
		cgColumnIndex = new ArrayList<Integer>();
		if(cs.useSamples.size() > 0)
		{
			cgColumnNames[0] = "All samples";
			cgColumnIndex.add(-1);
		} else {
			MessageDialog.openError(getShell(), "Error", "No samples selected for visualization\n" +
					"Select samples from list and click '>'");
			setRightCompositeContents(cs);
			return false;
		}
		for(int i = 0; i < cs.useSamples.size(); i++)
		{
			Sample s = cs.useSamples.get(i);
			if(s.dataType == Types.REAL)
			{
				cgColumnNames[i + 1] = s.name;
				cgColumnIndex.add(s.idSample);
			}
		}
		cgCombo.setItems(cgColumnNames);
		return true;
	}
	
	class TableGrowListener extends ControlAdapter {
		Table table;
		public TableGrowListener(Table table)
		{
			super();
			this.table = table;
		}
		public void controlResized(ControlEvent e) {
			TableColumn[] cols = table.getColumns();
			Rectangle area = null;
			if(table == sampleTable)
			{
				area = csTableComposite.getClientArea();
				
			} else if(table == cgColorTable) {
				area = cgGroup.getClientArea();
			} else {
				area = coTableComposite.getClientArea();
			}
			Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int width = area.width - 2*table.getBorderWidth();
			if (preferredSize.y > area.height + table.getHeaderHeight()) {
				Point vBarSize = table.getVerticalBar().getSize();
				width -= vBarSize.x;
			}
			Point oldSize = table.getSize();
			if (oldSize.x > area.width) {
				if(table == sampleTable)
				{
					cols[0].setWidth(width - cols[1].getWidth());
				} else if (table == cgColorTable) {
					cols[1].setWidth(width - cols[0].getWidth());
				} else {
					cols[0].setWidth(width);
				}
				table.setSize(area.width, area.height);
			} else {
				table.setSize(area.width, area.height);
				if(table == sampleTable)
				{
					cols[0].setWidth(width - cols[1].getWidth());
				} else if (table == cgColorTable) {
					cols[1].setWidth(width - cols[0].getWidth());
				} else {
					cols[0].setWidth(width);
				}
			}
		}
	}
	
	class inputSelectionAdapter extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e)
		{
			saveMaximizedControl();
		}
	}
	
	class CsComboSelectionAdapter extends SelectionAdapter {
		public CsComboSelectionAdapter() {
			super();
		}
		public void widgetSelected(SelectionEvent e) {
			GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
			coTableViewer.setInput(cs);
			setRightCompositeContents(cs);
			lastColorSetIndex = csCombo.getSelectionIndex();
		}
	}
	
	class CgButtonAdapter extends SelectionAdapter {
		public CgButtonAdapter() {
			super();
		}
		public void widgetSelected(SelectionEvent e) {
			saveColorGradient();
		}
	}
	
	public boolean saveMaximizedControl()
	{
		boolean ok = false;
		if(sashForm.getMaximizedControl() == cgComposite)
		{
			ok = saveColorGradient();
		} else if (sashForm.getMaximizedControl() == csComposite)
		{
			ok = saveColorSet();
		}
		return ok;
	}
	
	public boolean saveColorSet()
	{
		if(csNameText.getText().equals("")) {
			MessageDialog.openError(getShell(), "Error", "Specify a name for the color set");
			return false;
		}
		if(csCombo.getSelectionIndex() < 0) { return true; }
		GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
		cs.name = csNameText.getText();
		cs.color_gene_not_found = csColorGnf.getRGB();
		cs.color_no_criteria_met = csColorNc.getRGB();
		legend.setVisible(true);
		legend.resetContents();
		topSash.layout();
		return true;
	}

	public boolean saveColorGradient()
	{
		if(cgNameText.getText().equals("")) {
			MessageDialog.openError(getShell(), "Error", "Specify a name for the gradient");
			return false;
		}
		if(cgCombo.getText().equals("")) {
			MessageDialog.openError(getShell(), "Error", "Choose a data column for the gradient");
			return false;
		}
		GmmlColorGradient cg = (GmmlColorGradient)
		((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
		cg.name = cgNameText.getText();
		cg.setDataColumn(cgColumnIndex.get(cgCombo.getSelectionIndex()));
		
		legend.setVisible(true);
		legend.resetContents();
		topSash.layout();
		coTableViewer.refresh();
		return true;
	}
	
	public void saveToGex()
	{
		gmmlGex.saveColorSets();
	}
	public void restoreFromGex()
	{
		gmmlGex.loadColorSets();
	}
	
	class ColorButtonAdapter extends SelectionAdapter {
		public ColorButtonAdapter() {
			super();
		}
    	public void widgetSelected(SelectionEvent e) {
    		RGB rgb = csColorDialog.open();
    		if (rgb != null) {
    			if(e.widget == csColorButtonGnf) {
    				csColorGnf = SwtUtils.changeColor(csColorGnf, rgb, getShell().getDisplay());
    				csCLabelGnf.setBackground(csColorGnf);
    			}
    			if(e.widget == csColorButtonNc) {
    				csColorNc = SwtUtils.changeColor(csColorNc, rgb, getShell().getDisplay());
    				csCLabelNc.setBackground(csColorNc);
    			}
    		}
    		saveMaximizedControl();
    	}
    }
	
	class csButtonAdapter extends SelectionAdapter {
		public csButtonAdapter() {
			super();
		}
		public void widgetSelected(SelectionEvent e) {
			saveColorSet();
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
				GmmlColorSet cs = new GmmlColorSet(d.getValue(), gmmlGex);
				gmmlGex.colorSets.add(cs);
				csCombo.setItems(gmmlGex.getColorSetNames());
				csCombo.select(gmmlGex.colorSets.indexOf(cs));
				coTableViewer.setInput(cs);
				setRightCompositeContents(cs);
			}
		}
	}
    
	class EditCsButtonAdapter extends SelectionAdapter {
		
		public EditCsButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			if(csCombo.getSelectionIndex() > -1) {
				GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
				setRightCompositeContents(cs);
			}
		}
	}
	
	class NewCoButtonAdapter extends SelectionAdapter {
		NewCoDialog dialog;
		
		public NewCoButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			dialog = new NewCoDialog(Display.getCurrent().getActiveShell());
			dialog.open();
		}
	}
	
	class DeleteCsButtonAdapter extends SelectionAdapter {
		public DeleteCsButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			if(csCombo.getSelectionIndex() > -1)
			{
				gmmlGex.colorSets.remove(csCombo.getSelectionIndex());
				csCombo.setItems(gmmlGex.getColorSetNames());
				csCombo.select(0);
				if(csCombo.getSelectionIndex() > -1)
				{
					GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
					coTableViewer.setInput(cs);
					setRightCompositeContents(cs);
				}
			}
		}
	}
	
	class DeleteCoButtonAdapter extends SelectionAdapter {
		public DeleteCoButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			GmmlColorSetObject co = (GmmlColorSetObject)
			((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
			if(co != null)
			{
				co.parent.colorSetObjects.remove(co);
				coTableViewer.refresh();
			}
		}
	}
	
	class AddSampleButtonAdapter extends SelectionAdapter {
		public AddSampleButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			if(csCombo.getSelectionIndex() > -1)
			{
				GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
				Iterator it = ((IStructuredSelection)sampleListViewer.getSelection()).iterator();
				while(it.hasNext())
				{
					cs.addUseSample((Sample)it.next());
				}
				sampleTableViewer.refresh(true);
				sampleListViewer.refresh(true);
			} else {
				return;
			}
		}
	}
	
	class RemoveSampleButtonAdapter extends SelectionAdapter {
		public RemoveSampleButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			if(csCombo.getSelectionIndex() > -1)
			{
				GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
				Iterator it = ((IStructuredSelection)sampleTableViewer.getSelection()).iterator();
				while(it.hasNext())
				{
					cs.useSamples.remove((Sample)it.next());
				}
				sampleTableViewer.refresh(true);
				sampleListViewer.refresh(true);
			} else {
				return;
			}
		}
	}
	
	static final String TRANSFER_CSOBJECT = "CSOBJECT";
	static final String TRANSFER_SEP = ":";
	
    private class CoTableDragAdapter extends DragSourceAdapter {
    	public void dragStart(DragSourceEvent e) {
    		Object selected = ((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
    		if(selected == null)
    		{
    			e.doit = false;
    		}
    	}
    	
    	public void dragSetData(DragSourceEvent e) {
    		Object selected = ((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
    		e.data = "NONE";
    		if(selected instanceof GmmlColorSetObject)
    		{
    			GmmlColorSetObject cso = (GmmlColorSetObject)selected;
    			int csIndex = gmmlGex.colorSets.indexOf(cso.parent);
    			int csoIndex = cso.parent.colorSetObjects.indexOf(cso);
    			e.data = TRANSFER_CSOBJECT + TRANSFER_SEP + csIndex + TRANSFER_SEP + csoIndex;
    		}
    	}
    }
    
    private class CoTableDropAdapter extends DropTargetAdapter {
    	public void drop(DropTargetEvent e) {
    		TableItem item = (TableItem)e.item;
    		if(item != null)
    		{
    			Object selected = item.getData();
    			String[] data = ((String)e.data).split(":");
    			if(data[0].equals(TRANSFER_CSOBJECT))
    			{
    				int csIndex = Integer.parseInt(data[1]);
    				int csoIndex = Integer.parseInt(data[2]);
    				GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(csIndex);
    				GmmlColorSetObject cso = (GmmlColorSetObject)cs.colorSetObjects.get(csoIndex);
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
    		coTableViewer.refresh();
    	}
    }
    
    public void moveElement(Vector v, Object o, int newIndex)
    {
    	v.remove(o);
    	v.add(newIndex, o);
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
		    final Combo coTypeCombo = new Combo(csGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		    
		    csTextLabel.setText("Name:");
		    csText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    
			final String[] comboText = new String[] { "Color by gradient" };//, "Color by expression" };
		    csComboLabel.setText("Type:");
		    coTypeCombo.setItems(comboText);
		    coTypeCombo.setText(comboText[0]);
		    
		    final Button buttonOk = new Button(shell, SWT.PUSH);
		    buttonOk.setText("Ok");
		    buttonOk.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
		    final Button buttonCancel = new Button(shell, SWT.PUSH);
		    buttonCancel.setText("Cancel");
		    buttonCancel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		    
			buttonOk.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
		    		if(csText.getText().equals("")) {
		    			MessageDialog.openError(getShell(), "Error", "Specify a name for the Color Set");
		    			return;
		    		}
		    		if(comboText[0].equals(coTypeCombo.getText())) {
		    			GmmlColorGradient cg = new GmmlColorGradient(cs, csText.getText());
		    			cs.addObject(cg);
		    			coTableViewer.refresh();
		    			coTableViewer.setSelection(new StructuredSelection(cg));
		   
		    			shell.dispose();
		    		}
		    	}
		    });
			
			buttonCancel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					shell.dispose();
				}
			});
			
			shell.setDefaultButton(buttonOk);
		    shell.pack();
		    shell.setLocation(parent.getLocation().x + parent.getSize().x / 2 - shell.getSize().x / 2,
		    				  parent.getLocation().y + parent.getSize().y / 2 - shell.getSize().y / 2);
		    shell.open();
		    
		    Display display = parent.getDisplay();
		    while (!shell.isDisposed()){
		    	if(!display.readAndDispatch())
		    		display.sleep();
		    }
		}
	}
	
	private class CoTableSelectionChangedListener implements ISelectionChangedListener {
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
	
	private class CoTableContentProvider implements IStructuredContentProvider {
		
		public void dispose() {	}
		
		public Object[] getElements(Object inputElement) {
				return ((GmmlColorSet)inputElement).colorSetObjects.toArray();
		}
				
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			//TODO: input changed
		}
		
	}
	
	private class CoTableLabelProvider implements ITableLabelProvider {
		private java.util.List listeners;
		private Image criterionImage;
		private Image gradientImage;
				
		public CoTableLabelProvider() {
			listeners = new ArrayList();
			try {
				criterionImage = new Image(null, new FileInputStream("icons/colorset.gif"));
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		
		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}
		
		public void dispose() {
			if(criterionImage != null)
				criterionImage.dispose();
			if(gradientImage != null)
				gradientImage.dispose();
		}
		
		public Image getColumnImage(Object element, int columnIndex) { 
			if(element instanceof GmmlColorGradient) {
				gradientImage = new Image(null, createGradientImage((GmmlColorGradient)element));
				return gradientImage;
			}
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
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
	
	private ImageData createGradientImage(GmmlColorGradient cg)
	{
		PaletteData colors = new PaletteData(0xFF0000, 0x00FF00, 0x0000FF);
		ImageData data = new ImageData(16, 16, 24, colors);
		double[] minmax = cg.getMinMax();
		for(int i = 0; i < 16; i++)
		{
			RGB rgb = cg.getColor(minmax[0] + (i * (minmax[1]- minmax[0])) / 16 );
			if(rgb == null)
				rgb = new RGB(255,255,255);
			for(int j = 0; j < 16; j++)
			{
				if(j == 0 || j == 15 || i == 0 || i == 15)
					data.setPixel(i, j, colors.getPixel(new RGB(0,0,0)));
				else
					data.setPixel(i, j, colors.getPixel(rgb));
			}
		}
		return data;
	}
	
	private ImageData createColorImage(RGB rgb)
	{
		PaletteData colors = new PaletteData(new RGB[] { rgb, new RGB(0,0,0) });
		ImageData data = new ImageData(16, 16, 1, colors);
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 16; j++)
			{
				if(j == 0 || j == 15 || i == 0 || i == 15)
					data.setPixel(i, j, 1);
				else
					data.setPixel(i, j, 0);
			}
		}
		return data;
	}
	
	private class CgColorTableContentProvider implements IStructuredContentProvider {
		
		public void dispose() {	}
		
		public Object[] getElements(Object inputElement) {
				return ((GmmlColorGradient)inputElement).colorValuePairs.toArray();
		}
				
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
		
	}
	
	private class CgColorTableLabelProvider implements ITableLabelProvider {
		private java.util.List listeners;
		private Image colorImage;
				
		public CgColorTableLabelProvider() {
			listeners = new ArrayList();
		}
		
		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}
		
		public void dispose() {
			if(colorImage != null)
			{
				colorImage.dispose();
			}
		}
		
		public Image getColumnImage(Object element, int columnIndex) { 
			if(columnIndex == 0)
			{
				RGB rgb = ((ColorValuePair)element).color;
				colorImage = new Image(null, createColorImage(rgb));
				return colorImage;
			}
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if(columnIndex == 1) {
				return Double.toString(((ColorValuePair)element).value);
			}
			return null;
		}
		
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		
		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}
	}
	
	private class CgColorTableCellModifier implements ICellModifier
	{
		public boolean canModify(Object element, String property)
		{
			return true;
		}
		
		public Object getValue(Object element, String property)
		{
			ColorValuePair cvp = (ColorValuePair)element;
			if(property.equals(cgColorTableCols[0]))
			{
				return cvp.color;
			} else {
				return Double.toString(cvp.value);
			}
		}
		
		public void modify(Object element, String property, Object value)
		{
			if(element instanceof TableItem)
			{
				element = ((TableItem)element).getData();
			}
			ColorValuePair cvp = (ColorValuePair)element;
			if(property.equals(cgColorTableCols[0]))
			{
				cvp.color = (RGB)value;
			} else {
				cvp.value = Double.parseDouble((String)value);
			}
			cgColorTableViewer.refresh();
			coTableViewer.refresh();
		}
	}
	
	private class CsListContentProvider implements IStructuredContentProvider
	{
		public CsListContentProvider()
		{
			super();
		}
		public Object[] getElements(Object inputElement)
		{
			GmmlColorSet cs = (GmmlColorSet)inputElement;
			ArrayList<Sample> notUseSamples = new ArrayList<Sample>(gmmlGex.samples.values());
			for(Sample s : cs.useSamples)
			{
				notUseSamples.remove(s);
			}
			return notUseSamples.toArray();
		}
		public void dispose() {	}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	}
	}
	
	private class CsListLabelProvider extends LabelProvider
	{
		public String getText(Object element)
		{
			return ((Sample)element).name;
		}
	}
	
	private class CsTableContentProvider implements IStructuredContentProvider
	{		
		public CsTableContentProvider()
		{
			super();
		}
		
		public Object[] getElements(Object inputElement)
		{
			GmmlColorSet cs = (GmmlColorSet)inputElement;
			return cs.useSamples.toArray();
		}
		public void dispose() { }
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
	}
	
	private class CsTableLabelProvider implements ITableLabelProvider
	{
		public CsTableLabelProvider() {
			super();
		}
		public Image getColumnImage(Object element, int columnIndex) { return null; }
		public String getColumnText(Object element, int columnIndex)
		{
			Sample s = (Sample)element;
			if(csCombo.getSelectionIndex() > -1)
			{
				GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
				switch(columnIndex) {
				case 0: //Name
					return s.name;
				case 1: //Type
					return GmmlColorSet.SAMPLE_TYPES[cs.sampleTypes.get(cs.useSamples.indexOf(s))];
				}
			}
			return "";
		}
		
		public void addListener(ILabelProviderListener listener) { }
		public void dispose() { }
		public boolean isLabelProperty(Object element, String property) { return false; }
		public void removeListener(ILabelProviderListener listener) { }
		
		
	}
	
	private class CsTableCellModifier implements ICellModifier
	{
		public boolean canModify(Object element, String property)
		{
			if(!colNames.get(0).equals(property))
			{
				return true;
			}
			return false;
		}
		
		public Object getValue(Object element, String property)
		{
			Sample s = (Sample)element;
			GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());	
			switch(colNames.indexOf(property)) {
			case 0:
				return s.name;
			case 1:
				return cs.sampleTypes.get(cs.useSamples.indexOf(s));
			}
			return null;
		}
		
		public void modify(Object element, String property, Object value)
		{
			Sample s = null;
			if(element instanceof Item) {
				TableItem t = (TableItem)element;
				s = (Sample)t.getData();
			} else {
				s = (Sample)element;
			}

			GmmlColorSet cs = gmmlGex.colorSets.get(csCombo.getSelectionIndex());
			
			switch(colNames.indexOf(property)) {
			case 1:
				if(cs.useSamples.contains(s))
				{
					cs.sampleTypes.set(cs.useSamples.indexOf(s), (Integer)value);
					sampleTableViewer.refresh();
				}
			}
		}
	}
}
