// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.search;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.debug.Logger;
import org.pathvisio.util.tableviewer.PathwayTable;
import org.pathvisio.util.tableviewer.TableData.Row;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;


/**
 * This composite displays a table on which SearchResults can be
 * displayed
 */
public class SearchResultTable extends PathwayTable implements ApplicationEventListener {
	public static String COLUMN_FOUND_IDS = "idsFound";
	
	public SearchResultTable(Composite parent, int style) {
		super(parent, SWT.NULL);
		Engine.getCurrent().addApplicationEventListener(this);
	}
		
	public int getNrResults() { return getNrRows(); }
		
	public void highlightResults(boolean highlight) {
		VPathway drawing = Engine.getCurrent().getActiveVPathway();
		if(drawing == null) return; //No drawing open
		
		if(highlight) { 
			Row sr = (Row) //Get selected search result
			((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
			if(sr == null) return; //Nothing selected
			
			try {
				ArrayList<String> idsFound = sr.getCell(COLUMN_FOUND_IDS).getArray();
				GeneProduct gp = null;
				for(VPathwayElement o : drawing.getDrawingObjects()) {
					if(o instanceof GeneProduct) {
						gp = (GeneProduct)o;
						if(idsFound.contains(gp.getPathwayElement().getGeneID())) gp.highlight();
					}
				}
				drawing.redraw();
			} catch(Exception ex) { 
				Logger.log.error("when highlighting genes from search result table", ex);
			}
		}
		else drawing.resetHighlight();
	}
	
	Button highlightButton;
	protected void createContents() {
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
		
		Composite tableComposite = new Composite(this, SWT.NULL);
		tableComposite.setLayout(new FillLayout());
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		initTable(tableComposite);
	}

	public void applicationEvent(ApplicationEvent e) {
		if(e.getType() == ApplicationEvent.PATHWAY_OPENED) {
			highlightButton.getDisplay().asyncExec(new Runnable() {
				public void run() {
					highlightResults(highlightButton.getSelection());	
				}
			});
		}				
	}
}	
