package search;

import gmmlVision.GmmlVision;

import java.util.ArrayList;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import util.TableColumnResizer;

//TODO: make table items double clickable

/**
 * This class displays a table on which {@link SearchResults}s can be
 * displayed
 */
public class SearchResultTable extends Composite {
	private GmmlVision gmmlVision;
	private TableViewer tableViewer;
	
	public SearchResultTable(Composite parent, int style, GmmlVision gmmlVision) {
		super(parent, SWT.NULL);
		
		this.gmmlVision = gmmlVision;

		createContents();
	}
	
	public void setSearchResults(SearchResults srs) { 
		//Recreate the table's columns to display the search results attributes
		Table t = tableViewer.getTable();
		for(TableColumn tc : t.getColumns()) tc.dispose();
		
		ArrayList<String> attrNames = srs.getAttributeNames();
		String[] colProps = new String[attrNames.size()];
		for(int i = 0; i < attrNames.size(); i++) {
			TableColumn tc = new TableColumn(t, SWT.NULL);
			tc.setText(attrNames.get(i));
			tc.setWidth(20);
			colProps[i] = attrNames.get(i);
		}
		tableViewer.setColumnProperties(colProps);
		tableViewer.setInput(srs);
		t.layout();
	}
	
	public TableViewer getTableViewer() { return tableViewer; }
	
	private void createContents() {
		setLayout(new FillLayout());
		Table t = new Table(this, SWT.SINGLE | SWT.FULL_SELECTION);
		t.addControlListener(new TableColumnResizer(t, this));
//		t.addSelectionListener(tableSelectionListener);
		t.setHeaderVisible(true);
		tableViewer = new TableViewer(t); 
		tableViewer.setContentProvider(tableContentProvider);
		tableViewer.setLabelProvider(tableLabelProvider);
	}
	
//	private SelectionAdapter tableSelectionListener = new SelectionAdapter() {
//		public void widgetSelected(SelectionEvent e) {
//			Table t = tableViewer.getTable();
//		}
//		
//	};
	
	private IStructuredContentProvider tableContentProvider = new IStructuredContentProvider() {

		public Object[] getElements(Object inputElement) {			
			if(inputElement instanceof SearchResults) {
				SearchResults srs = (SearchResults)inputElement;
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
			SearchResults.SearchResult sr = (SearchResults.SearchResult)element;
			String name = (String)tableViewer.getColumnProperties()[columnIndex];
			
			try { return sr.getAttribute(name).getText(); } catch (Exception e) { return "error"; }
		}

		public void addListener(ILabelProviderListener listener) {	}
		public void dispose() {	}
		public boolean isLabelProperty(Object arg0, String arg1) { return false; }
		public void removeListener(ILabelProviderListener arg0) { }
		
	};
}	
