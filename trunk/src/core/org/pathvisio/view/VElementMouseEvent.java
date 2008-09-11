package org.pathvisio.view;

import java.util.EventObject;

/**
 * An event for mouse events that apply to a single VPathwayElement 
 * @author thomas
 */
public class VElementMouseEvent extends EventObject {
	public static final int TYPE_MOUSEENTER = 0;
	public static final int TYPE_MOUSEEXIT = 1;
	
	public int type;
	public VPathwayElement element;
	public MouseEvent mouseEvent;
	
	public VElementMouseEvent(VPathway source, int type, VPathwayElement element, MouseEvent mouseEvent) {
		super(source);
		this.type = type;
		this.element = element;
		this.mouseEvent = mouseEvent;
	}
	
	public int getType() {
		return type;
	}
	
	public VPathwayElement getElement() {
		return element;
	}
	
	public MouseEvent getMouseEvent() {
		return mouseEvent;
	}
}
