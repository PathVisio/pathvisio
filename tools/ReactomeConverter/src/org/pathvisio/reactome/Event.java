package org.pathvisio.reactome;

public class Event {
	int id;
	String name;
	
	public Event(int id) {
		this.id = id;
	}
	
	int getId() {
		return id;
	}
	
	void setName(String name) {
		this.name = name;
	}
	
	String getName() {
		return name;
	}
}
