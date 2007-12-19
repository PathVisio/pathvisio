package org.pathvisio.model;

public enum OrderType {
	TOP("Bring to front", "Bring the element in front of all other elements of the same type"),
	BOTTOM("Send to back", "Send the element behind all other elements of the same type"),
	//UP("Move up", "Move up"),
	//DOWN("Move down", "Move down"),
	
	;
	
	private String description;
	private String name;
	
	private OrderType(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
}
