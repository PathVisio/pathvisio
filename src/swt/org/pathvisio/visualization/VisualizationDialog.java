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
package org.pathvisio.visualization;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.data.Gex;
import org.pathvisio.data.GexSwt;
import org.pathvisio.visualization.colorset.ColorSetComposite;

/**
 Dialog to configure visualizations and ColorSets.
 This dialog contains two tabs, one for Visualizations and one for ColorSets.
 The first is implemented by VisualizationComposite,
 the second by ColorSetComposite.
 */

public class VisualizationDialog extends ApplicationWindow
{
	// tab item that will be show when opening the dialog
	private int tabItemOnOpen = 0;

	/** first tab-item: visualizations */
	public static final int TABITEM_VISUALIZATIONS = 0;
	/** second tab-item: colorsets */
	public static final int TABITEM_COLORSETS = 1;

	private final String[] tabItemNames = new String[]
	{
		"Visualizations", "Color sets"	
	};
	
	public VisualizationDialog(Shell shell)
	{
		super(shell);
		setBlockOnOpen(true);
	}
	
	public boolean close()
	{
		GexSwt.saveXML();
		return super.close();
	}

	/**
	   Use this method to preselect which tab you want to show before
	   opening the dialog.
	 */
	public void setTabItemOnOpen(int index)
	{
		tabItemOnOpen = index;
	}
	
	protected Control createContents(Composite parent)
	{
		Shell shell = getShell();
		shell.setSize(700, 600);
		
		Composite content = new Composite(parent, SWT.NULL);
		content.setLayout(new GridLayout());
		
		CTabFolder tabs = new CTabFolder(content, SWT.BORDER);
		tabs.setSimple(false);
		tabs.setSelectionBackground(new Color[] {
				tabs.getSelectionBackground(),
				tabs.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND) }, 
				new int[] { 100 }, true);
		CTabItem visTab = new CTabItem(tabs, SWT.NULL);
		visTab.setControl(new VisualizationComposite(tabs, SWT.NULL));
		visTab.setText(tabItemNames[0]);

		CTabItem colorTab = new CTabItem(tabs, SWT.NULL);
		colorTab.setControl(new ColorSetComposite(tabs, SWT.NULL));
		colorTab.setText(tabItemNames[1]);

		tabs.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Button ok = new Button(content, SWT.NULL);
		ok.setText("  Ok  ");
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});
		ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				
		tabs.setSelection(tabItemOnOpen);
		content.setFocus();
		return tabs;
	}	

}
