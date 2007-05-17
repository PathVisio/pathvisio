package org.pathvisio.biopax.gui;

import org.biopax.paxtools.model.level2.BioPAXElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.jdom.Document;
import org.pathvisio.biopax.BiopaxManager;

public class BiopaxDialog extends Dialog {
	BiopaxManager biopax;
	TableViewer tableViewer;
	
	public BiopaxDialog(Shell shell) {
		super(shell);
	}

	public void setBiopax(Document bp) {
		setBiopax(new BiopaxManager(bp));
	}
	
	public void setBiopax(BiopaxManager bp) {
		biopax = bp;
		update();
	}
	
	private void update() {
		if(tableViewer != null) {
			tableViewer.setInput(biopax);
			tableViewer.refresh();
		}
	}
	
	protected Control createDialogArea(Composite parent) {
		 Composite comp = (Composite) super.createDialogArea(parent);
		 comp.setLayout(new FillLayout());
		 
		 tableViewer = new TableViewer(comp);
		 Table t = tableViewer.getTable();
		 t.setHeaderVisible(true);
		 TableColumn tcElm = new TableColumn(t, SWT.LEFT);
		 tcElm.setText("Element");
		 tcElm.setWidth(500);
		 
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
					return bpe.toString();
				default: return "";
				}
			}
		 });
		 
		 tableViewer.setInput(biopax);
		 
		 return comp;
	}
}
