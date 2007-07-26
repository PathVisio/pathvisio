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
package org.pathvisio.gui.swt;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.Gex;
import org.pathvisio.data.Gdb.IdCodePair;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.SelectionBox;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

/**
 * Backpage browser - side panel that shows the backpage information when a GeneProduct is double-clicked
 */
public class BackpagePanel extends Composite implements SelectionListener {
	private String text = "";
	
	private Browser bpBrowser;
	
	private GeneProduct geneProduct;
	
	/**
	 * Constructor for this class
	 * @param parent	Parent {@link Composite} for the Browser widget
	 * @param style		Style for the Browser widget
	 */
	public BackpagePanel(Composite parent, int style) {
		super(parent, style);

		setLayout(new FillLayout());
		bpBrowser = new Browser(this, style); //Set the Browser widget
		refresh();
		
		SelectionBox.addListener(this);
	}
	
	private void setText(String text) {
		this.text = text;
		refresh();
	}
	
	public void setGeneProduct(final GeneProduct gp) 
	{ 
		if(geneProduct == gp) return;
		
		Thread fetchThread = new Thread() {
			public void run() {
				geneProduct = gp;
				if(gp == null) {
					setText(Gdb.getBackpageHTML(null, null, null));
				} else {
				// Get the backpage text
				PathwayElement e = gp.getGmmlData();
				String text = Gdb.getBackpageHTML(
						e.getGeneID(), 
						e.getSystemCode(), 
						e.getBackpageHead());
				//Short hack because Gex is not in core package
				if(Gex.isConnected()) {
					text = text.substring(0, text.length() - "</body></html>".length());
					text += "<H1>Expression data</H1><P>";
					String gexText = Gex.getDataString(new IdCodePair(e.getGeneID(), e.getSystemCode()));
					text += gexText == null ? "<I>No expression data found</I>" : gexText;
					text += "</body></html>";
				}
				setText(text);
				}
			}
		};
		
		//Run in seperate thread so that this method can return
		fetchThread.start();
	}
			
	/**
	 * Refreshes the text displayed in the browser
	 */
	public void refresh() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				bpBrowser.setText(text);	
			}
		});
	}

	public void drawingEvent(SelectionEvent e) {
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			//Just take the first GeneProduct in the selection
			for(VPathwayElement o : e.selection) {
				if(o instanceof GeneProduct) {
					if(geneProduct != o) setGeneProduct((GeneProduct)o);
					break; //Selects the first, TODO: use setGmmlDataObjects
				}
			}
			break;
		case SelectionEvent.OBJECT_REMOVED:
			if(e.selection.size() != 0) break;
		case SelectionEvent.SELECTION_CLEARED:
			setGeneProduct(null);
			break;
		}
	}
}
