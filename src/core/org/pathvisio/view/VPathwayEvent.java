package org.pathvisio.view;

import java.util.EventObject;

public class VPathwayEvent extends EventObject {
	public static final int NEW_ELEMENT_ADDED = 0;
	public static final int EDIT_MODE_ON = 1;
	public static final int EDIT_MODE_OFF = 2;
	
	int type;
	VPathwayElement affectedElement;
	
	public VPathwayEvent(VPathway source, int type) {
		super(source);
		this.type = type;
	}
	
	public VPathwayEvent(VPathway source, VPathwayElement affectedElement, int type) {
		this(source, type);
		this.affectedElement = affectedElement;
	}
	
	public VPathwayElement getAffectedElement() {
		return affectedElement;
	}
	
	public int getType() {
		return type;
	}
	
	public VPathway getVPathway() {
		return (VPathway)getSource();
	}
}
