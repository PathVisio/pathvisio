// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.biopax.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pathvisio.biopax.BiopaxManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;

public class BiopaxDialog extends Dialog {
	Pathway pathway;
	BiopaxManager biopax;
	Text text;

	public BiopaxDialog(Shell shell) {
		super(shell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
	}

	public void setPathway(Pathway p) {
		pathway = p;
		biopax = new BiopaxManager(p.getBiopax().getBiopax());
		update();
	}

	protected String getBiopaxString() {
		return text.getText();
	}

	public void update() {
		if(text != null && !text.isDisposed()) {
			if(biopax != null) {
				try {
					text.setText(biopax.getXml());
				} catch(ConverterException e) {
					text.setText(e.toString());
					Logger.log.error("Unable to set BioPAX text", e);
				}
			} else {
				text.setText("");
			}
		}
	}

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
            IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL, false);
    }

	protected Control createDialogArea(Composite parent) {
		 Composite comp = (Composite) super.createDialogArea(parent);
		 comp.setLayout(new FillLayout());

		 text = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		 text.setSize(500, 500);
		 update();
		 return comp;
	}

	protected void updateBiopax() {
		biopax.setModel(BiopaxManager.modelFromString(getBiopaxString()));
	}
	protected void updatePathway() throws ConverterException {
		pathway.getBiopax().setBiopax(biopax.getDocument());
	}

	protected void okPressed() {
		try {
			updateBiopax();
			updatePathway();
		} catch(Exception e) {
			MessageDialog.openError(getShell(),
					"Invalid BioPAX code",
					"The BioPAX code is invalid:\n" + e.getMessage());
			return;
		}
		super.okPressed();
	}
}
