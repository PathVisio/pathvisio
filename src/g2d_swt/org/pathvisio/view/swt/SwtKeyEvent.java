package org.pathvisio.view.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

public class SwtKeyEvent extends org.pathvisio.view.KeyEvent {
	KeyEvent swtEvent;
	
	public SwtKeyEvent(KeyEvent swtEvent, int type) {
		super(swtEvent.getSource(), convertKeyCode(swtEvent), type, VPathwaySWT.convertStateMask(swtEvent.stateMask));
		this.swtEvent = swtEvent;
	}

	protected static int convertKeyCode(KeyEvent swtEvent) {
		switch(swtEvent.keyCode) {
		case SWT.CTRL:
			return org.pathvisio.view.KeyEvent.CTRL;
		case SWT.ALT:
			return org.pathvisio.view.KeyEvent.ALT;
		case SWT.DEL:
			return org.pathvisio.view.KeyEvent.DEL;
		case SWT.INSERT:
			return org.pathvisio.view.KeyEvent.INSERT;
		case SWT.SHIFT:
			return org.pathvisio.view.KeyEvent.SHIFT;
		default:
			return swtEvent.keyCode;
		}
	}
}
