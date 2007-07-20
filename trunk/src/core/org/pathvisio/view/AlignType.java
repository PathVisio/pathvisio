package org.pathvisio.view;

public enum AlignType {
	CENTERX("Center X", "Align horizontal centers", "icons/aligncenterx.gif"),
	CENTERY("Center Y", "Align vertical centers", "icons/aligncentery.gif"),
	LEFT("Left", "Align left edges", "icons/alignleft.gif"),
	RIGHT("Right", "Align right edges", "icons/alignright.gif"),
	TOP("Top", "Align top edges", "icons/aligntop.gif"),
	BOTTOM("Bottom", "Align bottom edges", "icons/alignbottom.gif"),
	WIDTH("Width", "Set common width", "icons/sizeheight.gif"),
	HEIGHT("Height", "Set common height", "icons/sizeheight.gif"),
	;
	
	String label;
	String description;
	String icon;
	
	AlignType(String label, String tooltip, String icon) {
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
