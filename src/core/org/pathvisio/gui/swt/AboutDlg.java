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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Globals;

/**
 * 
 * A simple dialog box that shows about information
 */
public class AboutDlg extends Dialog
{
	private static final long serialVersionUID = 1L;

	public AboutDlg(Shell parent) 
	{
		super (parent);
	}

	public AboutDlg(Shell parent, int style) 
	{
		super (parent, style);
	}
	
	public void open()
	{
		Shell parent = getParent();
		final Shell shell = new Shell (parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);

		shell.setText ("About " + Globals.APPLICATION_VERSION_NAME);		
		GridLayout ly = new GridLayout();
		ly.numColumns = 2;
		shell.setLayout (ly);
		
		Label lbl = new Label (shell, SWT.NULL);
		lbl.setText (Globals.APPLICATION_VERSION_NAME + "\nRevision: " + Revision.REVISION);
		GridData gd = new GridData (GridData.HORIZONTAL_ALIGN_CENTER);
		gd.horizontalSpan = 2;		
		lbl.setLayoutData (gd);
		
		lbl = new Label (shell, SWT.NULL);
		lbl.setImage (SwtEngine.getImageRegistry().get("about.logo"));

		lbl = new Label (shell, SWT.NULL);
		lbl.setText ("R.M.H. Besseling\nS.P.M.Crijns\nI.Kaashoek\nM.M.Palm\n" +
				"E.D Pelgrim\nT.A.J. Kelder\nM.P. van Iersel\nBiGCaT");
		
		final Button btnOk = new Button (shell, SWT.PUSH);
		btnOk.setText ("OK");
		gd = new GridData (GridData.HORIZONTAL_ALIGN_CENTER);
		gd.horizontalSpan = 2;
		gd.widthHint = 60;
		btnOk.setLayoutData (gd);
		
		btnOk.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
					shell.dispose();
			}
		});
			
		shell.pack();
		shell.open();
		
		Display display = parent.getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();			
		}
	}
}
