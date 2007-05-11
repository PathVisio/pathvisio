package org.pathvisio.gui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class ButtonCellEditor extends CellEditor {
	public static int buttonStyle = SWT.PUSH;
	private Button button;
		
	public ButtonCellEditor(Composite parent, String label) {
		super(parent);
		button.setText(label);
	}
		
	protected Control createControl(Composite parent) {
		//Composite comp = new Composite(parent, SWT.NULL);
		//comp.setLayout(new FillLayout());
		button = new Button(parent, buttonStyle);
		setSelectionListeners(button);
		return button;
	}
	
	protected Button getButton() {
		return button;
	}
	
	protected abstract void setSelectionListeners(Button b);
	
	protected void doSetFocus() {
		if(button != null && !button.isDisposed()) 
			button.setFocus();
	}
}
