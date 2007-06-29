package org.pathvisio.view.swing;

import java.awt.Point;
import java.awt.event.MouseEvent;

public class SwingMouseEvent extends org.pathvisio.view.MouseEvent {
	MouseEvent awtEvent;
	
	public SwingMouseEvent(MouseEvent e) {
		super(e.getSource(), convertType(e), e.getButton(), 
				e.getX(), e.getY(), e.getClickCount(), e.getModifiers());
		awtEvent = e;
	}

	protected static int convertType(MouseEvent e) {
		if(e.isPopupTrigger()) return MOUSE_HOVER;

		switch(e.getID()) {
		case MouseEvent.MOUSE_ENTERED:
			return org.pathvisio.view.MouseEvent.MOUSE_ENTER;
		case MouseEvent.MOUSE_EXITED:
			return org.pathvisio.view.MouseEvent.MOUSE_EXIT;
		case MouseEvent.MOUSE_MOVED:
		case MouseEvent.MOUSE_DRAGGED:
			return org.pathvisio.view.MouseEvent.MOUSE_MOVE;
		case MouseEvent.MOUSE_PRESSED:
			return org.pathvisio.view.MouseEvent.MOUSE_DOWN;
		case MouseEvent.MOUSE_RELEASED:
			return org.pathvisio.view.MouseEvent.MOUSE_UP;
		case MouseEvent.MOUSE_CLICKED:
			return org.pathvisio.view.MouseEvent.MOUSE_CLICK;
		default:
			throw new IllegalArgumentException("Mouse event type not supported");
		}
	}
}
