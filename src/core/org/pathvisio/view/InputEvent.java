package org.pathvisio.view;

import java.util.EventObject;

public class InputEvent extends EventObject {
	public static final int M_SHIFT = java.awt.event.InputEvent.SHIFT_MASK;
	public static final int M_CTRL = java.awt.event.InputEvent.CTRL_MASK;
	public static final int M_ALT = java.awt.event.InputEvent.ALT_MASK;
	public static final int M_META = java.awt.event.InputEvent.META_MASK;
	
	private int modifier;
	
	public InputEvent(Object source, int modifier) {
		super(source);
		this.modifier = modifier;
	}
	
	public boolean isKeyDown(int key) {
		return (modifier & key) != 0;
	}
}
