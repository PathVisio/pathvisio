package search;

import gmmlVision.GmmlVision;

import java.io.File;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import data.GmmlData;

public class SearchWindow extends Window {
	GmmlVision gmmlVision;
	HashMap<String, SearchComposite> searchControls;
	
	public SearchWindow(GmmlVision gmmlVision) {
		super(gmmlVision.getShell());
		this.gmmlVision = gmmlVision;
	}
	
	public Composite createContents(Composite parent) {
		searchControls = new HashMap<String, SearchComposite>();
		
		parent.setLayout(new GridLayout());
		
		TabFolder tabFolder = new TabFolder(parent, SWT.NULL);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite comp;
		//Pathway search tabitem
		TabItem ti = new TabItem(tabFolder, SWT.NULL);
		ti.setText("Pathway search");
		comp = pathwaySearch(tabFolder);
	
		
		ti.setControl(comp);
		//... other tabitems

		return parent;
	}
	
	public void selectControl(String id) throws Exception {
		if(searchControls.containsKey(id))
			searchControls.get(id).select();
		else throw new Exception("No composite found for: " + id);
	}
	
	public void selectControl(String id, HashMap<String, String> controlValues) 
	throws Exception {
		if(searchControls.containsKey(id)) {
			SearchComposite sc = searchControls.get(id);
			sc.select();
			sc.setContents(controlValues);
		} else throw new Exception("No composite found for: " + id);
	}
	
	StackLayout pathwaySearchStack;
	public Composite pathwaySearch(Composite parent) {
		String[] soLabels = new String[] { "gene id" };
		final HashMap<String, String> labelMappings = new HashMap<String, String>();
		labelMappings.put(soLabels[0], "pathwaysContainingGene");
		
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout(2, false));
		Label label = new Label(comp, SWT.CENTER);
		label.setText("Search by:");
		final Combo combo = new Combo(comp, SWT.READ_ONLY);
		combo.setItems(soLabels);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridData span2cols = new GridData(GridData.FILL_BOTH);
		span2cols.horizontalSpan = 2;
		
		Group sGroup = new Group(comp, SWT.NULL);
		pathwaySearchStack = new StackLayout();
		sGroup.setLayout(pathwaySearchStack);
		sGroup.setLayoutData(span2cols);
				
		//Add search options composites to stacklayout
		final Composite[] searchOptionControls = new Composite[1];
		searchOptionControls[0] = pathwaysContainingGene(sGroup);
		
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selection = combo.getSelectionIndex();
				if(selection > -1)
					searchControls.get(labelMappings.get(combo.getText())).select();
			}
		});
		
		//Set initial selection
		pathwaySearchStack.topControl = searchOptionControls[0];
		combo.select(0);
		
		return comp;
	}
	
	public Composite pathwaysContainingGene(Composite parent) {
		SearchComposite comp = new SearchComposite(parent, SWT.NULL)
		{
			void select() {
				pathwaySearchStack.topControl = this;
			}
			
			public Composite createContents(Composite parent) {
				setLayout(new GridLayout(3, false));
				
				GridData span2cols = new GridData(GridData.FILL_HORIZONTAL);
				span2cols.horizontalSpan = 2;
				
				Label idLabel = new Label(parent, SWT.CENTER);
				idLabel.setText("Gene id:");
				final Text idText = new Text(parent, SWT.SINGLE | SWT.BORDER);
				idText.setLayoutData(span2cols);
				
				Label systemLabel = new Label(parent, SWT.CENTER);
				systemLabel.setText("Id system:");
				final Combo systemCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY);
				systemCombo.setItems(GmmlData.systemNames);
				systemCombo.setLayoutData(span2cols);
				
				Label dirLabel = new Label(parent, SWT.CENTER);
				dirLabel.setText("Directory to search");
				final Text dirText = new Text(parent, SWT.SINGLE | SWT.BORDER);
				dirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				Button dirButton = new Button(parent, SWT.PUSH);
				dirButton.setText("Browse");
				dirButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						DirectoryDialog dd = new DirectoryDialog(getShell());
						String dirName = dd.open();
						if(dirName != null) dirText.setText(dirName);
					}
				});
				
				Button searchButton = new Button(parent, SWT.PUSH);
				searchButton.setText("Search");
				GridData span3cols = new GridData(GridData.HORIZONTAL_ALIGN_END);
				span3cols.horizontalSpan = 3;
				searchButton.setLayoutData(span3cols);
				searchButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						String id = idText.getText();
						int codeIndex = systemCombo.getSelectionIndex();
						String code =  codeIndex == -1 ? "" : GmmlData.systemCodes[codeIndex];
						String folder = dirText.getText();
						if(id.equals("") || code.equals("") || folder.equals("")) {
							MessageDialog.openError(getShell(), "error", "please specify id, code and pathway folder"); 
							return;
						}
						int nrRes = gmmlVision.search.pathwaysContainingGene(id, code, new File(folder), 
								gmmlVision.searchResults);
						if(nrRes == 0) MessageDialog.openInformation(getShell(), "Search result", "Nothing found");
						else gmmlVision.searchPanel.restore();
						close();
					}
				});
				
				//Add controls to hash to enable preset values
				name2Control.put("idText", idText);
				name2Control.put("systemCombo", systemCombo);
				name2Control.put("dirText", dirText);
				return parent;
			}
		};
		
		searchControls.put("pathwaysContainingGene", comp); //Add to available search options
		return comp;
	}
	
	public abstract class SearchComposite extends Composite {
		HashMap<String, Control> name2Control;
		
		public SearchComposite(Composite parent, int style) {
			super(parent, style);
			name2Control = new HashMap<String,Control>();
			createContents(this);
		}
		
		public void setContents(HashMap<String, String> name2Value) {
			for(String key : name2Control.keySet()) {
				if(name2Value.containsKey(key)) {
					Control c = name2Control.get(key);
					if(c instanceof Text) 		((Text)c).setText(name2Value.get(key));
					else if(c instanceof Combo) ((Combo)c).setText(name2Value.get(key));
				}
			}
		}
		
		abstract void select();
		public abstract Composite createContents(Composite parent);
	}
}
