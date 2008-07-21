package org.pathvisio.reactome;

public class PhysicalEntity {
	int id;
	int count;
	String name;
	
	public PhysicalEntity(int id) {
		this.id = id;
	}
	
	void setName(String name) {
		this.name = name;
	}
	
	String getName() {
		return name;
	}
	
	int getId() {
		return id;
	}
	
	int getCount() {
		return count;
	}
	
	void setCount(int count) {
		this.count = count;
	}
}
