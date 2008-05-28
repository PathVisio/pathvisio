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
import org.pathvisio.gui.BackpageListener;
import org.pathvisio.gui.BackpageTextProvider;

/**
 * Backpage browser - side panel that shows the backpage information when a GeneProduct is double-clicked
 */
public class BackpagePanel extends Composite implements BackpageListener {
	private Browser bpBrowser;
	
	/**
	 * Constructor for this class
	 * @param parent	Parent {@link Composite} for the Browser widget
	 * @param style		Style for the Browser widget
	 */
	public BackpagePanel(Composite parent, int style, BackpageTextProvider bpt) {
		super(parent, style);

		setLayout(new FillLayout());
		bpBrowser = new Browser(this, style); //Set the Browser widget
		bpt.addListener(this);
	}
	
	private void setText(final String text) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				bpBrowser.setText(text);	
			}
		});
	}

	public void textChanged(String oldText, String newText) {
		setText(newText);
	}
}
