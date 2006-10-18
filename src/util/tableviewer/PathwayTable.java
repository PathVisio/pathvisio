package util.tableviewer;

import gmmlVision.GmmlVision;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import util.TableColumnResizer;
import util.tableviewer.TableData.Row;


/**
 * This composite displays a table on which {@link TableData} can be
 * displayed and opened
 */
public class PathwayTable extends Composite {
	public static final String COLNAME_FILE = "fileName";
	
	private TableViewer tableViewer;
	private TableColumnResizer columnResizer;
	private Display display;
	
	public PathwayTable(Composite parent, int style) {
		super(parent, SWT.NULL);
		display = getShell().getDisplay();

		createContents();
	}
	
	public void setTableData(final TableData srs) {
		display.asyncExec(new Runnable() { 
			//asyncExec, because can be accessed from seperate thread
			public void run() {
				//Recreate the table's columns to display the TableData columns
				Table t = tableViewer.getTable();
				for(TableColumn tc : t.getColumns()) tc.dispose();
				
				ArrayList<String> attrNames = srs.getColNames();
				String[] colProps = new String[attrNames.size()];
				for(int i = 0; i < attrNames.size(); i++) {
					final TableColumn tc = new TableColumn(t, SWT.NULL);
					tc.setText(attrNames.get(i));
					tc.setWidth(20);
					colProps[i] = attrNames.get(i);
					tc.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							tableViewer.setSorter(new PathwaySorter(tc.getText()));
						}
					});
				}
				tableViewer.setColumnProperties(colProps);
				tableViewer.setInput(srs);
				columnResizer.doResize();
			}
		});

	}
	
	public void refreshTableViewer(final boolean updateLabels) {
		display.asyncExec(new Runnable() { 
			//asyncExec, because can be accessed from seperate thread
			public void run() {
				tableViewer.refresh(updateLabels);
			}
		});
	}
	
	public int getNrRows() { return tableViewer.getTable().getItemCount(); }
	
	public TableViewer getTableViewer() { return tableViewer; }
		
	private void createContents() {
		setLayout(new GridLayout(1, false));		
		initTable();
	}
	
	protected void initTable() {
		Table t = new Table(this, SWT.SINGLE | SWT.FULL_SELECTION);
		t.setLayoutData(new GridData(GridData.FILL_BOTH));
		columnResizer = new TableColumnResizer(t, this);
		t.addControlListener(columnResizer);
		t.addMouseListener(tableMouseListener);
		t.setHeaderVisible(true);
		tableViewer = new TableViewer(t); 
		tableViewer.setContentProvider(tableContentProvider);
		tableViewer.setLabelProvider(tableLabelProvider);
	}
	
	private MouseAdapter tableMouseListener = new MouseAdapter() {
		public void mouseDoubleClick(MouseEvent e) {
			Row sr = (Row)
			((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
			if(sr == null) return;
			try {
				String pw = sr.getColumn(COLNAME_FILE).getText();
				GmmlVision.openPathway(pw);
			} catch(Exception ex) { 
				GmmlVision.log.error("when trying to open pathway from pathway table", ex);
			}
		}
	};
	
	private IStructuredContentProvider tableContentProvider = new IStructuredContentProvider() {

		public Object[] getElements(Object inputElement) {			
			if(inputElement instanceof TableData) {
				TableData srs = (TableData)inputElement;
				return srs.getResults().toArray();
			}
			return new Object[] {};
		}

		public void dispose() {	}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
		
	};
	
	private ITableLabelProvider tableLabelProvider = new ITableLabelProvider() {

		public Image getColumnImage(Object element, int columnIndex) { return null;	}

		public String getColumnText(Object element, int columnIndex) {
			TableData.Row sr = (TableData.Row)element;
			String name = (String)tableViewer.getColumnProperties()[columnIndex];
			
			try { return sr.getColumn(name).getText(); } catch (Exception e) { return "error"; }
		}

		public void addListener(ILabelProviderListener listener) {	}
		public void dispose() {	}
		public boolean isLabelProperty(Object arg0, String arg1) { return false; }
		public void removeListener(ILabelProviderListener arg0) { }
		
	};
	
	private class PathwaySorter extends ViewerSorter {
		String property;
		int propertyIndex;
		
		public PathwaySorter(String sortByProperty) {
			property = sortByProperty;
		}
		
		public int compare(Viewer viewer, Object e1, Object e2) {
			Row r1 = (Row)e1;
			Row r2 = (Row)e2;
			return r1.getColumn(property).compareTo(r2.getColumn(property));
		}
	}
}	
