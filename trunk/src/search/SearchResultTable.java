package search;

import gmmlVision.GmmlVision;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import search.SearchResults.SearchResult;
import util.TableColumnResizer;


/**
 * This composite displays a table on which {@link SearchResults}s can be
 * displayed
 */
public class SearchResultTable extends Composite {
	private GmmlVision gmmlVision;
	private TableViewer tableViewer;
	private TableColumnResizer columnResizer;
	private Display display;
	
	public SearchResultTable(Composite parent, int style, GmmlVision gmmlVision) {
		super(parent, SWT.NULL);
		display = getShell().getDisplay();
		this.gmmlVision = gmmlVision;

		createContents();
	}
	
	public void setSearchResults(final SearchResults srs) {
		display.asyncExec(new Runnable() { 
			//asyncExec, because can be accessed from seperate thread
			public void run() {
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
	
	public int getNrResults() { return tableViewer.getTable().getItemCount(); }
	
	public TableViewer getTableViewer() { return tableViewer; }
	
	private void createContents() {
		setLayout(new FillLayout());
		
		Table t = new Table(this, SWT.SINGLE | SWT.FULL_SELECTION);
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
			SearchResult sr = (SearchResult)
			((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
			if(sr == null) return;
			try {
				String pw = sr.getAttribute("file").getText();
				gmmlVision.openPathway(pw);
			} catch(Exception ex) { 
				GmmlVision.log.error("when trying to open pathway from search results", ex);
			}
		}
	};
	
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
