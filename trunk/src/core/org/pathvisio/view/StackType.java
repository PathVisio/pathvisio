package org.pathvisio.view;

public enum StackType {	
	CENTERX("Center X", "Stack vertical center", "icons/stackverticalcenter.gif"),
	CENTERY("Center Y", "Stack horizontal center", "icons/stackhorizontalcenter.gif"),
	LEFT("Left", "Stack vertical left", "icons/stackverticalleft.gif"),
	RIGHT("Right", "Stack veritcal right", "icons/stackverticalright.gif"),
	TOP("Top", "Stack horizontal top", "icons/stackhorizontaltop.gif"),
	BOTTOM("Bottom", "Stack horizontal bottom", "icons/stackhorizontalbottom.gif"),
	;
	
	String label;
	String description;
	String icon;
	
	StackType(String label, String tooltip, String icon) {
		this.label = label;
		this.description = tooltip;
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}
}
