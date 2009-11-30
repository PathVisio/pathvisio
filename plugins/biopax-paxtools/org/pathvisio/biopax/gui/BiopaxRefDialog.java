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

import org.biopax.paxtools.model.level2.BioPAXElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.pathvisio.biopax.BiopaxManager;

public class BiopaxRefDialog extends Dialog {
	BiopaxManager biopax;
	String ref;

	TableViewer tableViewer;

	public BiopaxRefDialog(Shell shell, BiopaxManager bp, String ref) {
		super(shell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
		biopax = bp;
		this.ref = ref;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String r) {
		ref = r;
	}

	protected Control createDialogArea(Composite parent) {
		 Composite comp = (Composite) super.createDialogArea(parent);
		 comp.setLayout(new GridLayout());


		 tableViewer = new TableViewer(comp, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		 Table t = tableViewer.getTable();
		 t.setHeaderVisible(true);
		 TableColumn tcElm = new TableColumn(t, SWT.LEFT);
		 tcElm.setText("Element");
		 tcElm.setWidth(500);
		 TableColumn tcID = new TableColumn(t, SWT.LEFT);
		 tcID.setText("ID");
		 tcID.setWidth(100);

		 tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object input) {
				BiopaxManager bpm = (BiopaxManager)input;
				if(bpm != null) {
					return bpm.getModel().getObjects().toArray();
				} else {
					return new Object[] {};
				}
			}

			public void dispose() { }

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	}

		 });
		 tableViewer.setLabelProvider(new ITableLabelProvider() {
			public void addListener(ILabelProviderListener l) { }
			public void dispose() { }
			public boolean isLabelProperty(Object value, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener l) { }
			public Image getColumnImage(Object value, int col) { return null; }
			public String getColumnText(Object value, int col) {
				BioPAXElement bpe = (BioPAXElement)value;
				switch(col) {
				case 0:
					return bpe.getRDFId();
				case 1:
					return bpe.getClass().toString();
				default: return "";
				}
			}
		 });
		 tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				BioPAXElement selected = getSelectedObject(e.getSelection());
				setRef(selected.getRDFId());
			}
		 });

		 init();

		 return comp;
	}

	BioPAXElement getSelectedObject(ISelection s) {
		return (BioPAXElement)
			((IStructuredSelection)s).getFirstElement();
	}

	private void update() {
		if(tableViewer != null) {
			tableViewer.setInput(biopax);
			tableViewer.refresh();
		}
	}

	private void init() {
		tableViewer.setInput(biopax);
		BioPAXElement pe = biopax.getModel().getIdMap().get(ref);
		if(pe != null) {
			tableViewer.setSelection(new StructuredSelection(pe));
		}
	}

}