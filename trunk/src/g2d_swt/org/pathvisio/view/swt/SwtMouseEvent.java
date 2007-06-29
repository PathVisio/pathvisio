package org.pathvisio.view.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

public class SwtMouseEvent extends org.pathvisio.view.MouseEvent {

	private MouseEvent swtEvent;
	
	public SwtMouseEvent(MouseEvent swtEvent, int type, int clickCount) {
		super(swtEvent.getSource(), type, convertButton(swtEvent), swtEvent.x, swtEvent.y, 
				clickCount, VPathwaySWT.convertStateMask(swtEvent.stateMask));
		
		this.swtEvent = swtEvent;
	}

	protected static int convertButton(MouseEvent se) {
		switch(se.button) {
		case SWT.BUTTON1:
			return org.pathvisio.view.MouseEvent.BUTTON1;
		case SWT.BUTTON2:
			return org.pathvisio.view.MouseEvent.BUTTON2;
		case SWT.BUTTON3:
			return org.pathvisio.view.MouseEvent.BUTTON3;
		}
		return org.pathvisio.view.MouseEvent.BUTTON_NONE;
	}
		
}
