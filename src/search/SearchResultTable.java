package search;

import gmmlVision.GmmlVision;
import graphics.GmmlDrawing;
import graphics.GmmlDrawingObject;
import graphics.GmmlGeneProduct;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import util.TableColumnResizer;
import util.tableviewer.PathwayTable;
import util.tableviewer.TableData.Row;


/**
 * This composite displays a table on which {@link SearchResults}s can be
 * displayed
 */
public class SearchResultTable extends PathwayTable {
	private TableViewer tableViewer;
	private TableColumnResizer columnResizer;
	private Display display;
	
	public SearchResultTable(Composite parent, int style) {
		super(parent, SWT.NULL);
	}
		
	public int getNrResults() { return getNrRows(); }
	
	public TableViewer getTableViewer() { return tableViewer; }
	
	public void highlightResults(boolean highlight) {
		GmmlDrawing drawing = GmmlVision.getDrawing();
		if(drawing == null) return; //No drawing open
		
		if(highlight) { 
			Row sr = (Row) //Get selected searchresult
			((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
			if(sr == null) return; //Nothing selected
			
			try {
				ArrayList idsFound = sr.getAttribute("idsFound").getArray();
				GmmlGeneProduct gp = null;
				for(GmmlDrawingObject o : drawing.getDrawingObjects()) {
					if(o instanceof GmmlGeneProduct) {
						gp = (GmmlGeneProduct)o;
						if(idsFound.contains(gp.getID())) gp.highlight();
					}
				}
				drawing.redraw();
			} catch(Exception ex) { 
				GmmlVision.log.error("when highlighting genes from search result table", ex);
			}
		}
		else drawing.resetHighlight();
	}
	
	Button highlightButton;
	private void createContents() {
		setLayout(new GridLayout(1, false));
		
		Composite optionsComposite = new Composite(this, SWT.NULL);
		optionsComposite.setLayout(new GridLayout(2, false));
		
		highlightButton = new Button(optionsComposite, SWT.CHECK);
		highlightButton.setSelection(true);
		highlightButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				highlightResults(highlightButton.getSelection());
			}
		});
		
		Label highlightLabel = new Label(optionsComposite, SWT.CENTER);
		highlightLabel.setText("Highlight found genes");
		
		initTable();
	}
}	
