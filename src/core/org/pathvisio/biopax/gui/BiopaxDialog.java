package org.pathvisio.biopax.gui;

import java.util.List;

import org.biopax.paxtools.model.level2.BioPAXElement;
import org.biopax.paxtools.model.level2.Model;
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
import org.jdom.Element;
import org.pathvisio.biopax.BiopaxManager;

public class BiopaxDialog extends Dialog {
	BiopaxManager biopax;
	TableViewer tableViewer;
	
	protected BiopaxDialog(Shell shell) {
		super(shell);
	}

	protected void setBiopax(List<Element> bp) {
		setBiopax(new BiopaxManager(bp));
	}
	
	protected void setBiopax(BiopaxManager bp) {
		biopax = bp;
		update();
	}
	
	private void update() {
		if(tableViewer != null) {
			tableViewer.setInput(biopax);
		}
	}
	
	protected Control createDialogArea(Composite parent) {
		 Composite comp = (Composite) super.createDialogArea(parent);
		 comp.setLayout(new FillLayout());
		 
		 tableViewer = new TableViewer(comp);
		 Table t = tableViewer.getTable();
		 TableColumn tcElm = new TableColumn(t, SWT.LEFT);
		 tcElm.setText("Element");
		 
		 tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object input) {
				Model m = (Model)input;
				if(m == null) return new Object[] {};
				else return m.getObjects().toArray();
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
			public void removeListener(ILabelProviderListener arg0) { }
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
