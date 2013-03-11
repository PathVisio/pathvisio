package org.pathvisio.pluginmanager.impl.data;

import java.util.ArrayList;
import java.util.List;

public class Profile {

	private String name;
	private List<Category> categories;
	
	public Profile() {
		categories = new ArrayList<Category>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	
	
	
}
