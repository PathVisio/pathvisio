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

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import preferences.PreferenceLoader;
import util.SwtUtils;
import util.TableColumnResizer;
import colorSet.GmmlColorGradient.ColorValuePair;
import data.GmmlGex;
import data.GmmlGex.Sample;

/**
 * This class displays the colorset manager window where colorsets can be created and edited
 */
public class ColorSetWindow extends ApplicationWindow {
	
	/**
	 *names of samples to apply a colorset object to
	 */
	private String[] coSampleNames;
	
	/**
	 *id of samples to apply a colorset object to
	 */
	private ArrayList<Integer> coSampleIndex;
	
	/**
	 *names of numeric samples
	 */
	private String[] coNumSampleNames;
	
	/**
	 *id of numeric samples
	 */
	private ArrayList<Integer> coNumSampleIndex;
	
	private GmmlGex gmmlGex;
	
	/**
	 * Constructor of this class
	 * @param parent
	 */
	public ColorSetWindow(Shell parent)
	{
		super(parent);
		setBlockOnOpen(true);
	}

	/**
	 * creates a reference to a {@link GmmlGex}
	 * @param gmmlGex the {@link GmmlGex} to create a reference to
	 */
	public void setGmmlGex(GmmlGex gmmlGex)
	{
		this.gmmlGex = gmmlGex;
	}
	/**
	 * opens the colorset manager and disposes the system resources on close
	 */
	public void run()
	{
		setCoNumSamples();
		open();
		
		csColorGnf.dispose();
		csColorNc.dispose();
		csColorDnf.dispose();
		ccColor.dispose();
	}
		
	/**
	 * Closes the window and saves the colorsets to the expression database
	 */
	public boolean close()
	{
		if(!saveMaximizedComposite(false))
		{
			return false;
		}
		saveToGex();
		return super.close();
	}
	
	/**
	 * Closes the window with option to save the colorset data
	 * @param save true/false, either save or discard the colorset data
	 * @return returns true/false if the window is closed or not
	 */
	public boolean close(boolean save)
	{
		if(save)
			return close();
		else
			return super.close();
	}
	
	/**
	 * This method gets all samples containing numeric data from the
	 * expression data and saves it into the fields {@link #coNumSampleIndex}
	 * and {@link #coNumSampleIndex}
	 */
	public void setCoNumSamples() 
	{
		HashMap<Integer, Sample> samples = gmmlGex.getSamples();
		ArrayList<Integer> keys = new ArrayList<Integer>(samples.keySet());
		Collections.sort(keys);
		
		coNumSampleIndex = new ArrayList<Integer>();
		coNumSampleNames = new String[keys.size()];
		for(int id : keys)
		{
			coNumSampleIndex.add(id);
			coNumSampleNames[id] = samples.get(id).getName();
		}
	}
	
	private SashForm topSash; //divides the ColorSetWindow in three columns 
	private Composite coTableComposite; //holds the coTableViewer, displayed in left column
	private TableViewer coTableViewer; //shows the ColorSetObjects for the selected colorset
	private SashForm middleSash; //sash for editing properties of the selected object, displayed in middle column
	private GmmlLegend legend; //Legend displayed in right column
	
	private Combo csCombo; //for selecting the colorset
	
	protected Control createContents(Composite parent)
	{		
		Shell shell = parent.getShell();
		shell.setLocation(parent.getLocation());
		shell.setText("Color Set Builder");
		
		//holds the topSash and 2 buttons (ok, cancel)
		Composite topComposite = new Composite(parent, SWT.NULL);
		topComposite.setLayout(new GridLayout(2, false));
		
		topSash = new SashForm(topComposite, SWT.HORIZONTAL);
		GridData sashGrid = new GridData(GridData.FILL_BOTH);
		sashGrid.horizontalSpan = 2;
		topSash.setLayoutData(sashGrid);
		
		//holds the csComboGroup and CoTableGroup
		Composite coComposite = new Composite(topSash, SWT.NULL);
		coComposite.setLayout(new GridLayout(1, true));
		
		GridData tableGrid = new GridData(GridData.FILL_BOTH);
		tableGrid.horizontalSpan = 2;
		GridData comboGrid = new GridData(GridData.FILL_HORIZONTAL);
		comboGrid.horizontalSpan = 3;
		comboGrid.widthHint = 100;
		
		//create csComboGroup elements
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

		//create coTableGroup elements
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
		TableColumn coCol = new TableColumn(table, SWT.LEFT);
		coCol.setText("Name");

		table.addControlListener(new TableColumnResizer(table, coTableComposite));
				
		coTableViewer = new TableViewer(table);
		coTableViewer.setContentProvider(new CoTableContentProvider());
		coTableViewer.setLabelProvider(new CoTableLabelProvider());
		coTableViewer.addSelectionChangedListener(new CoTableSelectionChangedListener());
		
		middleSash = new SashForm(topSash, SWT.VERTICAL);
		middleSash.setLayout(new FillLayout());
		initiateSashForm();
		
		legend = new GmmlLegend(topSash, SWT.NONE, gmmlGex);
		
		topSash.setWeights(new int[] {25, 55, 20} );
		
		//Drag and drop support for coTableViewer
		DragSource ds = new DragSource(coTableViewer.getTable(), DND.DROP_MOVE);
		ds.addDragListener(new CoTableDragAdapter());
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		DropTarget dt = new DropTarget(coTableViewer.getTable(), DND.DROP_MOVE);
		dt.addDropListener(new CoTableDropAdapter());
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		
		//Ok and cancel buttons
		Button cancelButton = new Button(topComposite, SWT.PUSH);
		cancelButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//restore colorset information and exit without saving
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
				//save colorset information and close shell
				close();
			}
		});
		okButton.pack();
		
		csComboGroup.pack();
		coTableGroup.pack();
		legend.pack();

		//TODO: calculate correct size
		shell.setSize(topSash.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 50, 500);
		
		//Select last edited colorset
		csCombo.select(gmmlGex.getColorSetIndex() == -1 && gmmlGex.getColorSets().size() > 0 ?
				0 : gmmlGex.getColorSetIndex());
		//selectionListener is not triggered, so perform actions triggered when selecting a colorset
		if(csCombo.getSelectionIndex() > -1)
		{
			GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
			coTableViewer.setInput(cs);
			setMiddleCompositeContents(cs);
			legend.resetContents();
		}
		return parent;
	}

	private Composite cnComposite; //empty composite when nothing is selected
	private Composite csComposite; //display when editing color set
	private Composite cgComposite; //display when editing color gradient
	private Composite ccComposite; //display when editing color criteria (by expression)
	/**
	 * Initiates {@link Composite}s in middleSash
	 */
	void initiateSashForm() {
		cnComposite = new Composite(middleSash, SWT.NONE);
		csComposite = new Composite(middleSash, SWT.NONE);
		cgComposite = new Composite(middleSash, SWT.NONE);
		ccComposite = new Composite(middleSash, SWT.NONE);
		csComposite.setLayout(new GridLayout(1, true));
		cgComposite.setLayout(new GridLayout(1, true));
		ccComposite.setLayout(new GridLayout(1, true));
		
		setCsComposite();
	    setCgComposite();
	    setCcComposite();
	    
		middleSash.setMaximizedControl(cnComposite); //Start with empty composite
	}
	
	private Text csNameText; //name of colorset
	private CLabel csCLabelNc; //displays color for 'no criteria met'
	private Button csColorButtonNc; //button for selecting color for 'no criteria met'
	private CLabel csCLabelGnf; //displays color for 'gene not found'
	private Button csColorButtonGnf;//button for selecting color for 'gene not found'
	private CLabel csCLabelDnf; //displays color for 'no data found'
	private Button csColorButtonDnf; //button for selecting color for 'no data found'
	private Color csColorNc; //color for 'no criteria met'
	private Color csColorGnf; //color for 'gene not found'
	private Color csColorDnf; //color for 'no data found'
	private Table sampleTable; //table that displays samples used for visualization
	private TableViewer sampleTableViewer; //TableViewer for sampleTable
	private ListViewer sampleListViewer; //ListViewer for sampleList
	private Group csSampleGroup; //Group containing list and table to select samples
	private Composite sampleTableComposite; //Composite to layout sampleTableViewer
	private ColorDialog csColorDialog; //Colordialog for picking 'gene not found' and 'no criteria met' colors
	private List<String> stColNames = Arrays.asList(
			new String[] {"Sample name", "Type"}); //Column names of sampleTable
	/**
	 * sets the components of the csComposite used for editing colorset properties
	 */
	void setCsComposite()
	{
		//LayoutData
		GridData csNameTextGrid = new GridData(GridData.FILL_HORIZONTAL);
		csNameTextGrid.horizontalSpan = 2;
		csNameTextGrid.widthHint = 100;
		GridData colorGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		colorGrid.widthHint = colorGrid.heightHint = 15;
		GridData tableGroupGrid = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		tableGroupGrid.heightHint = 200;
		tableGroupGrid.widthHint = 400;
		
		Group csGroup = new Group(csComposite, SWT.SHADOW_IN);
		csGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		csGroup.setLayout(new GridLayout(3, false));
	    csGroup.setText("Color set options");
	    
	    csSampleGroup = new Group(csComposite, SWT.SHADOW_IN);
	    csSampleGroup.setLayoutData(tableGroupGrid);
	    csSampleGroup.setLayout(new GridLayout(3, false));
	    csSampleGroup.setText("Color set data");
	    
	    Button csButton = new Button(csComposite, SWT.PUSH);
	    csButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	    csButton.setText("Save");
	    csButton.addSelectionListener(new csButtonAdapter());
	    
	    // create csGroup components
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
	    Label csColorLabelDnf = new Label(csGroup, SWT.CENTER);
	    csCLabelDnf = new CLabel(csGroup, SWT.SHADOW_IN);
	    csColorButtonDnf = new Button(csGroup, SWT.PUSH);
	    csColorButtonDnf.addSelectionListener(new ColorButtonAdapter());
	    
	    csColorLabelNc.setText("No criteria met color:");
	    csColorLabelGnf.setText("Gene not found color:");
	    csColorLabelDnf.setText("No data found color:");
	    csColorButtonNc.setText("...");
	    csColorButtonGnf.setText("...");
	    csColorButtonDnf.setText("...");
	    
	    csColorNc = SwtUtils.changeColor(csColorNc, 
	    		PreferenceLoader.getColorProperty("colors.no_criteria_met"), getShell().getDisplay());
	    csColorGnf = SwtUtils.changeColor(csColorGnf, 
	    		PreferenceLoader.getColorProperty("colors.no_gene_found"), getShell().getDisplay());
	    csColorDnf = SwtUtils.changeColor(csColorDnf, 
	    		PreferenceLoader.getColorProperty("colors.no_data_found"), getShell().getDisplay());
	    csCLabelNc.setLayoutData(colorGrid);
	    csCLabelGnf.setLayoutData(colorGrid);
	    csCLabelDnf.setLayoutData(colorGrid);
	    csCLabelNc.setBackground(csColorNc);
	    csCLabelGnf.setBackground(csColorGnf);
	    csCLabelDnf.setBackground(csColorGnf);
	    csColorButtonNc.setLayoutData(colorGrid);
	    csColorButtonGnf.setLayoutData(colorGrid);
	    csColorButtonDnf.setLayoutData(colorGrid);
	    
	    csNameLabel.setText("Name:");
	    csNameText.setLayoutData(csNameTextGrid);
	    
	    csNameText.addSelectionListener(new inputSelectionAdapter());
	    
	    // create csSampleGroup components    
	    Label tableLabel = new Label(csSampleGroup, SWT.LEFT);
	    tableLabel.setText("Available samples");
	    GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
	    labelGrid.horizontalSpan = 2;
	    tableLabel.setLayoutData(labelGrid);
	    tableLabel.pack();
	    Label listLabel = new Label(csSampleGroup, SWT.LEFT);
	    listLabel.setText("Selected samples");
	    tableLabel.pack();
	    	    
	    sampleListViewer = new ListViewer(csSampleGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    sampleListViewer.setContentProvider(new SampleListContentProvider());
	    sampleListViewer.setLabelProvider(new SampleListLabelProvider());
	    sampleListViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    Composite buttonComposite = new Composite(csSampleGroup, SWT.NONE);
	    buttonComposite.setLayout(new RowLayout(SWT.VERTICAL));
	    Button addSampleButton = new Button(buttonComposite, SWT.PUSH);
	    addSampleButton.addSelectionListener(new AddSampleButtonAdapter());
	    addSampleButton.setText(">");
	    Button removeSampleButton = new Button(buttonComposite, SWT.PUSH);
	    removeSampleButton.addSelectionListener(new RemoveSampleButtonAdapter());
	    removeSampleButton.setText("<");
	    
	    sampleTableComposite = new Composite(csSampleGroup, SWT.NONE);
	    sampleTableComposite.setLayout(new FillLayout());
	    sampleTableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
	    sampleTable = new Table(sampleTableComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
	    
	    sampleTable.setHeaderVisible(true);
	    sampleTable.setLinesVisible(false);
	    
	    TableColumn nameCol = new TableColumn(sampleTable, SWT.LEFT);
	    TableColumn typeCol = new TableColumn(sampleTable, SWT.LEFT);
	    nameCol.setText(stColNames.get(0));
	    typeCol.setText(stColNames.get(1));
	    
	    sampleTable.addControlListener(new TableColumnResizer(sampleTable, sampleTableComposite, 
	    		new int[] { 60, 40 }));
	    
	    sampleTableViewer = new TableViewer(sampleTable);
	    
	    sampleTableViewer.setLabelProvider(new SampleTableLabelProvider());
	    sampleTableViewer.setContentProvider(new SampleTableContentProvider());
	    sampleTableViewer.setColumnProperties(stColNames.toArray(new String[stColNames.size()]));
	    CellEditor[] cellEditors = new CellEditor[2];
	    cellEditors[0] = new TextCellEditor(sampleTable);
	    cellEditors[1] = new ComboBoxCellEditor(sampleTable, GmmlColorSet.SAMPLE_TYPES, SWT.READ_ONLY);
	    sampleTableViewer.setCellEditors(cellEditors);
	    sampleTableViewer.setCellModifier(new SampleTableCellModifier());

	    csColorDialog = new ColorDialog(getShell());
	    
	    csSampleGroup.pack();
	    csGroup.pack();
	}
	
	private Text ccNameText; //name of criterion
	private Combo ccCombo; //samples to apply criterion on
	private CLabel ccCLabel; //color label for criterion
	private Color ccColor; //color for criterion
	private Button ccColorButton; //button for selecting color
	private Text ccExpression; //expression
	private org.eclipse.swt.widgets.List ccSampleList; //List containing samples (with numeric datatype)
	private org.eclipse.swt.widgets.List ccOpsList; //List containing operators
	/**
	 * sets the components of the ccComposite used for editing color by expression properties
	 */
	void setCcComposite() {
		GridData span2ColsGrid = new GridData(GridData.FILL_HORIZONTAL);
		span2ColsGrid.horizontalSpan = 2;
		GridData colorGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		colorGrid.widthHint = colorGrid.heightHint = 15;
		
		Group ccGroup = new Group(ccComposite, SWT.SHADOW_IN);
		ccGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		ccGroup.setLayout(new GridLayout(3, false));
		
		Label ccNameLabel = new Label(ccGroup, SWT.CENTER);
	    ccNameLabel.setText("Name:");
	
		ccNameText = new Text(ccGroup, SWT.SINGLE | SWT.BORDER);
	    ccNameText.setLayoutData(span2ColsGrid);
	    
	    Label ccComboLabel = new Label(ccGroup, SWT.CENTER);
	    ccComboLabel.setText("Apply to sample: ");
	    
	    ccCombo = new Combo(ccGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
	    ccCombo.setLayoutData(span2ColsGrid);
	    	    
	    Label ccColorLabel = new Label(ccGroup, SWT.CENTER);
	    ccColorLabel.setText("Color:");
	    
	    ccColor = SwtUtils.changeColor(ccColor, new RGB(0,0,0), getShell().getDisplay());
	    
	    ccCLabel = new CLabel(ccGroup, SWT.SHADOW_IN);
	    ccCLabel.setBackground(ccColor);
	    ccCLabel.setLayoutData(colorGrid);
	    
	    ccColorButton = new Button(ccGroup, SWT.PUSH);
	    ccColorButton.addSelectionListener(new ColorButtonAdapter());
	    ccColorButton.setLayoutData(colorGrid);
	    ccColorButton.setText("...");
	    
	    Group criterionGroup = new Group(ccGroup, SWT.SHADOW_IN);
	    criterionGroup.setLayout(new GridLayout(2, false));
	    GridData groupGrid = new GridData(GridData.FILL_BOTH);
	    groupGrid.horizontalSpan = 3;
	    criterionGroup.setLayoutData(groupGrid);
	    
	    Label expressionLabel = new Label(criterionGroup, SWT.CENTER);
	    expressionLabel.setText("Expression:");
	    ccExpression = new Text(criterionGroup, SWT.SINGLE | SWT.BORDER);
	    ccExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
	    Label opsLabel = new Label(criterionGroup, SWT.CENTER);
	    opsLabel.setText("Operators:");
	    Label sampleLabel = new Label(criterionGroup, SWT.CENTER);
	    sampleLabel.setText("Samples:");

	    ccOpsList = new org.eclipse.swt.widgets.List
    	(criterionGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    ccOpsList.setLayoutData(new GridData(GridData.FILL_VERTICAL));
	    ccOpsList.setItems(GmmlColorCriterion.tokens);
	    ccOpsList.addMouseListener(new MouseAdapter() {
	    	public void mouseDoubleClick(MouseEvent e) {
	    		String[] selection = ccOpsList.getSelection();
	    		if(selection != null && selection.length > 0) ccExpression.insert(selection[0] + " ");
	    	}
	    });
	    
	    ccSampleList = new org.eclipse.swt.widgets.List
	    	(criterionGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    ccSampleList.setLayoutData(new GridData(GridData.FILL_BOTH));
	    ccSampleList.setItems(coNumSampleNames);
	    ccSampleList.addMouseListener(new MouseAdapter() {
	    	public void mouseDoubleClick(MouseEvent e) {
	    		String[] selection = ccSampleList.getSelection();
	    		if(selection != null && selection.length > 0)
	    			ccExpression.insert("[" + selection[0] + "] ");
	    	}
	    });
	    
	    Button ccButton = new Button(ccGroup, SWT.PUSH);
	    ccButton.setText("Save");
	    GridData buttonGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
	    buttonGrid.horizontalSpan = 3;
	    ccButton.setLayoutData(buttonGrid);
	    ccButton.addSelectionListener(new inputSelectionAdapter());
	    
	    ccNameText.addSelectionListener(new inputSelectionAdapter());
	    ccCombo.addSelectionListener(new inputSelectionAdapter());
	    ccExpression.addSelectionListener(new inputSelectionAdapter());
	    
	}
	
	private Text cgNameText; //name of color gradient
	private Combo cgCombo; //samples to apply color gradient on
	private Table cgColorTable; //contains the color/value pairs used for the gradient
	private TableViewer cgColorTableViewer; //TableViewer for cgColorTable
	private Group cgGroup;
	private static final String[] cgColorTableCols = new String[] {"Color", "Value"}; //Column names for cgColorTable
	/**
	 * sets the components of the cgComposite used for editing color gradient properties
	 */
	void setCgComposite()
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
	    cgComboLabel.setText("Apply to sample:");
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
	    		cg.addColorValuePair(cg.new ColorValuePair(new RGB(255, 0, 0), 0));
	    		cgColorTableViewer.refresh();
	    	}
	    });
	    removeColorButton.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		GmmlColorGradient cg = (GmmlColorGradient)
	    		((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
	    		ColorValuePair cvp = (ColorValuePair)
	    		((IStructuredSelection)cgColorTableViewer.getSelection()).getFirstElement();
	    		cg.removeColorValuePair(cvp);
	    		cgColorTableViewer.refresh();
	    	}
	    });
	    
	    cgColorTable = new Table(cgGroup, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
	    GridData cGrid = new GridData(GridData.FILL_BOTH);
	    cGrid.horizontalSpan = 2;
	    cgColorTable.setLayoutData(cGrid);
	    
	    cgColorTable.setHeaderVisible(true);
	    TableColumn colorCol = new TableColumn(cgColorTable, SWT.LEFT);
	    TableColumn valueCol = new TableColumn(cgColorTable, SWT.LEFT);
	    valueCol.setText(cgColorTableCols[1]);
	    colorCol.setText(cgColorTableCols[0]);

	    colorCol.setWidth(45);
	    colorCol.setResizable(false);
	    
	    cgColorTable.addControlListener(
	    		new TableColumnResizer(cgColorTable, cgGroup, new int[] { 0, 100 }));
	    
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
	
	/**
	 * Maximizes one of the composites in {@link middleSash}
	 * @param element an element of instance {@link GmmlColorSet}, 
	 * {@link GmmlColorGradient} or {@link GmmlColorCriterion}
	 *  for which respectively {@link csComposite}, {@link cgComposite} or {@link ccComposite} is maximized
	 */
	public void setMiddleCompositeContents(Object element) {	
		if(element == null) {
			middleSash.setMaximizedControl(cnComposite);
			legend.setVisible(false);
			return;
		}
		if(element instanceof GmmlColorSet) {
			GmmlColorSet cs = (GmmlColorSet)element;
			csNameText.setText(cs.name);
			csColorNc = SwtUtils.changeColor(csColorNc, cs.color_no_criteria_met, getShell().getDisplay());
		    csColorGnf = SwtUtils.changeColor(csColorGnf, cs.color_no_gene_found, getShell().getDisplay());
		    csColorDnf = SwtUtils.changeColor(csColorDnf, cs.color_no_data_found, getShell().getDisplay());
			csCLabelGnf.setBackground(csColorGnf);
			csCLabelNc.setBackground(csColorNc);
			csCLabelDnf.setBackground(csColorDnf);
			sampleTableViewer.setInput(cs);
			sampleListViewer.setInput(cs);
			legend.setVisible(true);
			legend.resetContents();
			middleSash.setMaximizedControl(csComposite);
			topSash.layout();
			return;
		}
		if(element instanceof GmmlColorGradient) {
			GmmlColorGradient cg = (GmmlColorGradient)element;
			cgColorTableViewer.setInput(cg);
			cgNameText.setText(cg.getName());
			if(!setCoComboItems(cg.getParent())) {
				return;
			}
			cgCombo.select(coSampleIndex.indexOf(cg.useSample));
			middleSash.setMaximizedControl(cgComposite);
			legend.setVisible(true);
			legend.resetContents();
			topSash.layout();
			return;
		}
		if(element instanceof GmmlColorCriterion) {
			GmmlColorCriterion cc = (GmmlColorCriterion)element;
			ccNameText.setText(cc.getName());
			ccColor = SwtUtils.changeColor(ccColor, cc.getColor(), getShell().getDisplay());
			ccExpression.setText(cc.getExpression());
			ccCLabel.setBackground(ccColor);
			if(!setCoComboItems(cc.getParent())) {
				return;
			}
			ccCombo.select(coSampleIndex.indexOf(cc.useSample));
			middleSash.setMaximizedControl(ccComposite);
			legend.setVisible(true);
			legend.resetContents();
			topSash.layout();
			return;
		}
	}

	/**
	 * Sets the samples used for visualization to the color gradient combo (cgCombo)
	 * @param cs	{@link ColorSet} to get samples from
	 * @return	returns true if the colorset contains samples used for visualisation;
	 * displays warning message and returns false if no samples are selected for visualization in the
	 * selected colorset
	 */
	private boolean setCoComboItems(GmmlColorSet cs)
	{
		ArrayList<String> noStringSamples = new ArrayList<String>();
		coSampleIndex = new ArrayList<Integer>();
//		if(cs.useSamples.size() == 0)
//		{ //No samlpes are selected for visalization, makes no sense to create a gradient, so display warning
//			MessageDialog.openError(getShell(), "Error", "No samples selected for visualization\n" +
//					"Select samples from list and click '>'");
//			setMiddleCompositeContents(cs);
//			return false;
//		}
		noStringSamples.add("All samples");
		coSampleIndex.add(GmmlColorGradient.USE_SAMPLE_ALL);
		//Get the selected samples and store their name and index
		for(int i = 0; i < cs.useSamples.size(); i++)
		{
			Sample s = cs.useSamples.get(i);
			if(s.dataType == Types.REAL) //Filter out samples containing string data
			{
				noStringSamples.add(s.getName());
				coSampleIndex.add(s.idSample);
			}
		}
		coSampleNames = noStringSamples.toArray(new String[noStringSamples.size()]);
		cgCombo.setItems(coSampleNames);
		ccCombo.setItems(coSampleNames);
		return true;
	}
	
	/**
	 * {@link SelectionAdapter} for any control in the middle composites to
	 * save changes when the control is selected
	 */
	class inputSelectionAdapter extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e)
		{
			saveMaximizedComposite(true);
		}
		public void widgetDefaultSelected(SelectionEvent e) { 
			saveMaximizedComposite(false);
		}
	}
	
	/**
	 *{@link SelectionAdapter} for the colorset combo (csCombo);
	 * selects the colorset and maximizes the csComposite
	 */
	class CsComboSelectionAdapter extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			int selection = csCombo.getSelectionIndex();
			GmmlColorSet cs = gmmlGex.getColorSets().get(selection);
			gmmlGex.setColorSetIndex(selection);
			coTableViewer.setInput(cs);
			setMiddleCompositeContents(cs);
		}
	}
	
	/**
	 *{@link SelectionAdapter} for the save color gradient button
	 */
	class CgButtonAdapter extends SelectionAdapter {
		public CgButtonAdapter() {
			super();
		}
		public void widgetSelected(SelectionEvent e) {
			saveColorGradient(false);
		}
	}
	
	/**
	 * saves the information on the composite in the middle column that is currently maximized
	 * (csComposite or cgComposite)
	 * @param silent set true to prevent this method from generating error dialogs
	 * @return true if the information is saved, false if not
	 */
	public boolean saveMaximizedComposite(boolean silent)
	{
		if(middleSash.getMaximizedControl() == cgComposite)
		{
			return saveColorGradient(silent);
		}
		if(middleSash.getMaximizedControl() == csComposite)
		{
			return saveColorSet(silent);
		}
		if(middleSash.getMaximizedControl() == ccComposite)
		{
			return saveColorCriterion(silent);
		}
		return true;
	}
	
	/**
	 * saves the information on the csComposite to the currently selected colorset
	 * @param silent set true to prevent this method from generating error dialogs
	 * @return true if the information is saved, false if not
	 */
	public boolean saveColorSet(boolean silent)
	{
		if(csNameText.getText().equals("")) { //Complain if name field is empty
			if(!silent)
				MessageDialog.openError(getShell(), "Error", "Specify a name for the color set");
			return false;
		}
		if(csCombo.getSelectionIndex() < 0) return true; //Colorset doesn't exist anymore
		GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
		//Save the control information to the colorset
		cs.name = csNameText.getText();
		cs.color_no_gene_found = csColorGnf.getRGB();
		cs.color_no_criteria_met = csColorNc.getRGB();
		cs.color_no_data_found = csColorDnf.getRGB();
		//Update the ui components
		legend.setVisible(true);
		legend.resetContents();
		topSash.layout();
		return true;
	}

	/**
	 * saves the information on the cgComposite to the currently selected color gradient
	 * @param silent set true to prevent this method from generating error dialogs
	 * @return true if the information is saved, false if not
	 */
	public boolean saveColorGradient(boolean silent)
	{
		if(cgNameText.getText().equals("")) { //Complain if name field is empty
			if(!silent) 
				MessageDialog.openError(getShell(), "Error", "Specify a name for the gradient");
			return false;
		}
		if(cgCombo.getText().equals("")) { //Complain if no samples are selected
			if(!silent) 
				MessageDialog.openError(getShell(), "Error", "Choose a data column for the gradient");
			return false;
		}
		GmmlColorGradient cg = (GmmlColorGradient)
		((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
		if(cg == null) return true; //No gradient is selected (this should't happen)
		// save the control information to the gradient
		cg.setName(cgNameText.getText());
		cg.useSample = coSampleIndex.get(cgCombo.getSelectionIndex());
//		cg.setUseSample(cgColumnIndex.get(cgCombo.getSelectionIndex()));
		//Update the ui components
		legend.setVisible(true);
		legend.resetContents();
		topSash.layout();
		coTableViewer.refresh();
		return true;
	}
	
	/**
	 * saves the information on the ccComposite to the currently selected color criterion
	 * @param silent set true to prevent this method from generating error dialogs
	 * @return true if the information is saved, false if not
	 */
	public boolean saveColorCriterion(boolean silent)
	{
		if(ccNameText.getText().equals("")) { //Complain if name field is empty
			if(!silent) 
				MessageDialog.openError(getShell(), "Error", "Specify a name for the criterion");
			return false;
		}
		if(ccCombo.getText().equals("")) { //Complain if no samples are selected
			if(!silent) 
				MessageDialog.openError(getShell(), "Error", "Choose a data column for the criterion");
			return false;
		}
		GmmlColorCriterion cc = (GmmlColorCriterion)
		((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
		if(cc == null) return true; //No gradient is selected (this should't happen)
		String error = cc.setExpression(ccExpression.getText());
		if(error != null) {
			if(!silent) 
				MessageDialog.openError(getShell(), "Error", "Expression syntax is not valid: " + error);
			return false;
		}
		cc.setName(ccNameText.getText());
		cc.useSample = coSampleIndex.get(ccCombo.getSelectionIndex());
		cc.setColor(ccColor.getRGB());
		return true;
	}
	
	/**
	 *saves all colorsets in memory to the expression database
	 *@see GmmlGex.saveColorSets()
	 */
	public void saveToGex()
	{
		gmmlGex.saveColorSets();
	}
	/**
	 * restores all colorsets from the expression database
	 * @see GmmlGex.loadColorSets()
	 */
	public void restoreFromGex()
	{
		gmmlGex.loadColorSets();
	}
	
	/**
	 *{@link SelectionAdapter} that opens a color chooser
	 */
	class ColorButtonAdapter extends SelectionAdapter {
    	public void widgetSelected(SelectionEvent e) {
    		RGB rgb = csColorDialog.open();
    		if (rgb != null) {
    			if(e.widget == csColorButtonGnf) {
    				csColorGnf = SwtUtils.changeColor(csColorGnf, rgb, getShell().getDisplay());
    				csCLabelGnf.setBackground(csColorGnf);
    			}
    			else if(e.widget == csColorButtonNc) {
    				csColorNc = SwtUtils.changeColor(csColorNc, rgb, getShell().getDisplay());
    				csCLabelNc.setBackground(csColorNc);
    			}
    			else if(e.widget == ccColorButton) {
    				ccColor = SwtUtils.changeColor(ccColor, rgb, getShell().getDisplay());
    				ccCLabel.setBackground(ccColor);
    			}
    			else if(e.widget == csColorButtonDnf) {
    				csColorDnf = SwtUtils.changeColor(csColorDnf, rgb, getShell().getDisplay());
    				csCLabelDnf.setBackground(csColorDnf);
    			}
    		}
    		saveMaximizedComposite(true);
    	}
    }
	
	/**
	 * {@link SelectionAdapter} for the csButton (to save a colorset)
	 */
	class csButtonAdapter extends SelectionAdapter {
		public csButtonAdapter() {
			super();
		}
		public void widgetSelected(SelectionEvent e) {
			saveColorSet(false);
		}
	}
	
	/**
	 * {@link SelectionAdapter} for the newCsButton (to create a new colorset)
	 */
	class NewCsButtonAdapter extends SelectionAdapter {
		
		public NewCsButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			//Open dialog to specify name of colorset
			InputDialog d = new InputDialog(Display.getCurrent().getActiveShell(),
					  "New Color Set", "Name of new Color Set:", "", null);
			int rc = d.open();
			if(rc == Window.OK) {
				GmmlColorSet cs = new GmmlColorSet(d.getValue(), gmmlGex);
				gmmlGex.getColorSets().add(cs);
				csCombo.setItems(gmmlGex.getColorSetNames());
				csCombo.select(gmmlGex.getColorSets().indexOf(cs));
				coTableViewer.setInput(cs);
				setMiddleCompositeContents(cs);
			}
		}
	}
    
	/**
	 *{@link SelectionAdapter} for the editCsButton (to edit a colorset)
	 */
	class EditCsButtonAdapter extends SelectionAdapter {
		
		public EditCsButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			if(csCombo.getSelectionIndex() > -1) {
				GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
				setMiddleCompositeContents(cs);
			}
		}
	}
	
	/**
	 *{@link SelectionAdapter} for the newCoButton (to add a colorset object)
	 */
	class NewCoButtonAdapter extends SelectionAdapter {
		NewCoDialog dialog;
		
		public NewCoButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			int csIndex = csCombo.getSelectionIndex();
			if(csIndex >= gmmlGex.getColorSets().size()) return;
			GmmlColorSet cs = gmmlGex.getColorSets().get(csIndex);
			if(cs.useSamples.size() == 0)
			{ //No samlpes are selected for visalization, makes no sense to create a gradient, so display warning
				MessageDialog.openError(getShell(), "Error", "No samples selected for visualization\n" +
						"Select samples from list and click '>'");
				return;
			}
			dialog = new NewCoDialog(Display.getCurrent().getActiveShell());
			dialog.open();
		}
	}
	
	/**
	 *{@link SelectionAdapter} for the deleteCsButton (to remove a colorset)
	 */
	class DeleteCsButtonAdapter extends SelectionAdapter {
		public DeleteCsButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			if(csCombo.getSelectionIndex() > -1)
			{
				gmmlGex.removeColorSet(csCombo.getSelectionIndex());
				csCombo.setItems(gmmlGex.getColorSetNames());
				csCombo.select(gmmlGex.getColorSetIndex());
				if(csCombo.getSelectionIndex() > -1)
				{
					GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
					coTableViewer.setInput(cs);
					setMiddleCompositeContents(cs);
				} else { //No colorset left
					setMiddleCompositeContents(null);
					coTableViewer.setInput(null);
				}
			}
		}
	}
	
	/**
	 *{@link SelectionAdapter} for the deleteCsButton (to remove a colorset object)
	 */
	class DeleteCoButtonAdapter extends SelectionAdapter {
		public DeleteCoButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			GmmlColorSetObject co = (GmmlColorSetObject)
			((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
			if(co != null)
			{
				co.getParent().colorSetObjects.remove(co);
				coTableViewer.refresh();
			}
		}
	}
	
	/**
	 *{@link SelectionAdapter} for the addSampleButton (to add a sample to the sampleTable)
	 */
	class AddSampleButtonAdapter extends SelectionAdapter {
		public AddSampleButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			if(csCombo.getSelectionIndex() > -1)
			{
				GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
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
	
	/**
	 *{@link SelectionAdapter} for the removeSampleButton (to remove a sample to the sampleTable)
	 */
	class RemoveSampleButtonAdapter extends SelectionAdapter {
		public RemoveSampleButtonAdapter() {
			super();
		}
		
		public void widgetSelected(SelectionEvent e) {
			if(csCombo.getSelectionIndex() > -1)
			{
				GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
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
	
	//constants for drag and drop support of the colorsetobjects
	static final String TRANSFER_CSOBJECT = "CSOBJECT";
	static final String TRANSFER_SEP = ":";
	/**
	 *{@DragSourceAdapter} for drag support in the coTableViewer (displays color set objects);
	 *used to change order of objects
	 */
    private class CoTableDragAdapter extends DragSourceAdapter {
    	public void dragStart(DragSourceEvent e) {
    		Object selected = ((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
    		if(selected == null) e.doit = false; //only start transfer if an object is selected
    	}
    	
    	public void dragSetData(DragSourceEvent e) {
    		Object selected = ((IStructuredSelection)coTableViewer.getSelection()).getFirstElement();
    		e.data = "NONE";
    		if(selected instanceof GmmlColorSetObject)
    		{
    			GmmlColorSetObject cso = (GmmlColorSetObject)selected;
    			int csIndex = gmmlGex.getColorSets().indexOf(cso.getParent());
    			int csoIndex = cso.getParent().colorSetObjects.indexOf(cso);
    			//Put object type and index in a string that is transferred
    			e.data = TRANSFER_CSOBJECT + TRANSFER_SEP + csIndex + TRANSFER_SEP + csoIndex;
    		}
    	}
    }
    
	/**
	 *{@DragSourceAdapter} for drop support in the coTableViewer (displays color set objects);
	 *used to change order of objects
	 */
    private class CoTableDropAdapter extends DropTargetAdapter {
    	public void drop(DropTargetEvent e) {
    		TableItem item = (TableItem)e.item;
    		if(item != null)
    		{
    			Object selected = item.getData();
    			//Split the transfer string
    			String[] data = ((String)e.data).split(TRANSFER_SEP);
    			if(data[0].equals(TRANSFER_CSOBJECT))
    			{
    				int csIndex = Integer.parseInt(data[1]); //The parent index
    				int csoIndex = Integer.parseInt(data[2]); //The colorset object index
    				GmmlColorSet cs = (GmmlColorSet)gmmlGex.getColorSets().get(csIndex);
    				GmmlColorSetObject cso = (GmmlColorSetObject)cs.colorSetObjects.get(csoIndex);
    				moveElement(cs.colorSetObjects, cso, cs.colorSetObjects.indexOf(selected));
    			}
    		}
    		coTableViewer.refresh();
    	}
    }
    
    /**
     * Moves an element in a {@link Vector}
     * @param 	v the vector the object is in
     * @param 	o the object that has to be moved
     * @param 	newIndex the index to move the object to
     */
    public void moveElement(Vector v, Object o, int newIndex)
    {
    	v.remove(o);
    	v.add(newIndex, o);
    }
	
    /**
     * {@link Dialog} shown when creating a new colorset object;
     *  enables the user to specify the name and type
     */
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
		    
			final String[] comboText = new String[] { "Color by gradient" , "Color by expression" };
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
		    		GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
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
		    			return;
		    		}
		    		if(comboText[1].equals(coTypeCombo.getText())) {
		    			GmmlColorCriterion cc = new GmmlColorCriterion(cs, csText.getText());
		    			cs.addObject(cc);
		    			coTableViewer.refresh();
		    			coTableViewer.setSelection(new StructuredSelection(cc));
		    			
		    			shell.dispose();
		    			return;
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
	
	/**
	 * {@link ISelectionChangedListener} for {@link coTableViewer}
	 * maximizes one of the composites in {@link middleSash} depending on the object selected
	 */
	private class CoTableSelectionChangedListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent e)
		{
			if(e.getSelection().isEmpty()) {
				setMiddleCompositeContents(null);
			} else {
				Object s = ((IStructuredSelection)e.getSelection()).getFirstElement();
				setMiddleCompositeContents(s);
			}
		}
	}
	
	/**
	 * {@link IStructuredContentProvider} for {@link coTable}
	 */
	private class CoTableContentProvider implements IStructuredContentProvider {
		
		public void dispose() {	}
		
		public Object[] getElements(Object inputElement) {
				return ((GmmlColorSet)inputElement).colorSetObjects.toArray();
		}
				
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
	}
	
	/**
	 * {@link ITabbleLabelPovider for {@link coTable}
	 */
	private class CoTableLabelProvider implements ITableLabelProvider {
		private java.util.List listeners;
		private Image criterionImage;
		private Image gradientImage;
				
		public CoTableLabelProvider() {
			listeners = new ArrayList();
			try {
				criterionImage = new Image(null, new FileInputStream("icons/colorset.gif"));
			} catch (Exception e) { 
				GmmlVision.log.error("Unable to open image 'icons/colorset.gif'", e);
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
			if(element instanceof GmmlColorCriterion) {
				criterionImage = new Image(null, createColorImage(
						((GmmlColorCriterion)element).getColor()));
				return criterionImage;
			}
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof GmmlColorSetObject)
				return ((GmmlColorSetObject)element).getName();
			return "";
		}
		
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		
		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * creates a 16x16 image representing the given {@link GmmlColorGradient}
	 * @param cg the gradient to create the image from
	 * @return imagedata representing the gradient
	 */
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
				if(j == 0 || j == 15 || i == 0 || i == 15) //Black border
					data.setPixel(i, j, colors.getPixel(new RGB(0,0,0)));
				else
					data.setPixel(i, j, colors.getPixel(rgb));
			}
		}
		return data;
	}
	
	/**
	 * creates an 16x16 image filled with the given color
	 * @param rgb the color to fill the image with
	 * @return imagedata of a 16x16 image filled with the given color
	 */
	private ImageData createColorImage(RGB rgb)
	{
		PaletteData colors = new PaletteData(new RGB[] { rgb, new RGB(0,0,0) });
		ImageData data = new ImageData(16, 16, 1, colors);
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 16; j++)
			{
				if(j == 0 || j == 15 || i == 0 || i == 15) //Black border
					data.setPixel(i, j, 1);
				else
					data.setPixel(i, j, 0);
			}
		}
		return data;
	}
	
	/**
	 * {@link IStructuredContentProvider} for {@link cgColorTable}
	 */
	private class CgColorTableContentProvider implements IStructuredContentProvider {
		
		public void dispose() {	}
		
		public Object[] getElements(Object inputElement) {
				return ((GmmlColorGradient)inputElement).getColorValuePairs().toArray();
		}
				
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
		
	}
	
	/**
	 * {@link ITabbleLabelPovider} for {@link cgColorTable}
	 */
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
	
	/**
	 * {@link ICellModifier} for {@link cgColorTable}
	 */
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
			legend.resetContents();
		}
	}
	
	/**
	 *{@link IStructuredContentProvider} for {@link sampleList}
	 */
	private class SampleListContentProvider implements IStructuredContentProvider
	{
		public SampleListContentProvider()
		{
			super();
		}
		public Object[] getElements(Object inputElement)
		{
			GmmlColorSet cs = (GmmlColorSet)inputElement;
			ArrayList<Sample> notUseSamples = new ArrayList<Sample>(gmmlGex.getSamples().values());
			Collections.sort(notUseSamples);
			for(Sample s : cs.useSamples)
			{
				notUseSamples.remove(s);
			}
			return notUseSamples.toArray();
		}
		public void dispose() {	}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	}
	}
	
	/**
	 *{@link ILabelProvider} for {@link sampleList}
	 */
	private class SampleListLabelProvider extends LabelProvider
	{
		public String getText(Object element)
		{
			return ((Sample)element).getName();
		}
	}
	
	/**
	 *{@link IStructuredContentProvider} for {@link sampleTable}
	 */
	private class SampleTableContentProvider implements IStructuredContentProvider
	{		
		public SampleTableContentProvider()
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
	
	/**
	 *{@link ITableLabelProvider} for {@link sampleTable}
	 */
	private class SampleTableLabelProvider implements ITableLabelProvider
	{
		public SampleTableLabelProvider() {
			super();
		}
		public Image getColumnImage(Object element, int columnIndex) { return null; }
		public String getColumnText(Object element, int columnIndex)
		{
			Sample s = (Sample)element;
			if(csCombo.getSelectionIndex() > -1)
			{
				GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
				switch(columnIndex) {
				case 0: //Name
					return s.getName();
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
	
	/**
	 *{@link ITableLabelProvider} for {@link sampleTable}
	 */
	private class SampleTableCellModifier implements ICellModifier
	{
		public boolean canModify(Object element, String property)
		{
			if(!stColNames.get(0).equals(property))
			{
				return true;
			}
			return false;
		}
		
		public Object getValue(Object element, String property)
		{
			Sample s = (Sample)element;
			GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());	
			switch(stColNames.indexOf(property)) {
			case 0:
				return s.getName();
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

			GmmlColorSet cs = gmmlGex.getColorSets().get(csCombo.getSelectionIndex());
			
			switch(stColNames.indexOf(property)) {
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
