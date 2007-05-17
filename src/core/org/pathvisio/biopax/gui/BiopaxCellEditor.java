package org.pathvisio.biopax.gui;

import java.util.List;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.jdom.Document;
import org.pathvisio.gui.ButtonCellEditor;

public class BiopaxCellEditor extends ButtonCellEditor {
	Document biopax;
	
	public BiopaxCellEditor(Composite parent, String label) {
		super(parent, label);
	}
	
	protected void setSelectionListeners(Button b) {
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				BiopaxDialog d = new BiopaxDialog(e.display.getActiveShell());
				d.setBiopax(biopax);
				d.open();
			}
		});
	}

	protected Object doGetValue() {
		return biopax;
	}

	protected void doSetValue(Object obj) {
		if(!(obj instanceof List) && obj != null) {
			throw new IllegalArgumentException("Can't set object of class " + obj.getClass());
		} else {
			biopax = (Document)obj;
		}
	}
}
