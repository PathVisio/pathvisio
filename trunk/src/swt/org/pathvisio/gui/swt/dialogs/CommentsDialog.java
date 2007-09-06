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
package org.pathvisio.gui.swt.dialogs;

import java.util.Iterator;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.Comment;
import org.pathvisio.util.swt.TableColumnResizer;

public class CommentsDialog extends PathwayElementDialog {
	static final String[] tableHeaders = new String[] { "Source", "Comment" };
	
	TableViewer tableViewer;
		
	public CommentsDialog(Shell parent, PathwayElement e) {
		super(parent, e);
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite comp  = (Composite)super.createDialogArea(parent);
		comp.setLayout(new GridLayout(2, false));
		Composite tableComp = new Composite(comp, SWT.NONE);
		tableComp.setLayout(new FillLayout());
		GridData g = new GridData(GridData.FILL_BOTH);
		g.horizontalSpan = 2;
		g.widthHint = 300;
		g.heightHint = 200;
		tableComp.setLayoutData(g);
		
		Table t = new Table(tableComp, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI | SWT.WRAP);
		t.setHeaderVisible(true);
		TableColumn tc1 = new TableColumn(t, SWT.NONE);
		TableColumn tc2 = new TableColumn(t, SWT.NONE);
		tc1.setText(tableHeaders[0]);
		tc2.setText(tableHeaders[1]);
		tc1.setWidth(50);
		tc2.setWidth(80);
				
		new TableColumnResizer(t, tableComp, new int[] { 30, 70 });
		
		tableViewer = new TableViewer(t);
		tableViewer.setCellModifier(cellModifier);
		tableViewer.setLabelProvider(labelProvider);
		tableViewer.setColumnProperties(tableHeaders);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setCellEditors(new CellEditor[] { new TextCellEditor(t), new TextCellEditor(t) });
		Button add = new Button(comp, SWT.PUSH);
		add.setText("Add comment");
		add.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		Button remove = new Button(comp, SWT.PUSH);
		remove.setText("Remove comment");
		add.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addPressed();
			}
		});
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removePressed();
			}
		});
		tableViewer.setInput(input.getComments());
		
		return comp;
	}
	
	protected void addPressed() {
		input.addComment("Type your comment here", "");
		refresh();
	}
	
	protected void removePressed() {
		Iterator it = ((IStructuredSelection)tableViewer.getSelection()).iterator();
		while(it.hasNext()) {
			Comment c = (Comment)it.next();
			input.removeComment(c);
		}
		refresh();
	}
	
	ICellModifier cellModifier = new ICellModifier() {
		public boolean canModify(Object element, String property) {
			return true;
		}
		public Object getValue(Object element, String property) {
			Comment c = (Comment)element;
			String value = property.equals(tableHeaders[0]) ? c.getSource() : c.getComment();
			return value == null ? "" : value;
		}
		public void modify(Object element, String property, Object value) {
			if(value == null) return;
			
			Comment c = (Comment)((TableItem)element).getData();
			if(property.equals(tableHeaders[0])) {
				c.setSource((String)value);
			} else {
				c.setComment((String)value);
			}
			refresh();
		}
	};
	
	ITableLabelProvider labelProvider = new ITableLabelProvider() {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			Comment c = (Comment)element;
			return columnIndex == 0 ? c.getSource() : c.getComment();
		}
		public void addListener(ILabelProviderListener listener) {
		}
		public void dispose() {
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {
		}
	};

	protected void refresh() {
		tableViewer.refresh();
	}
}
