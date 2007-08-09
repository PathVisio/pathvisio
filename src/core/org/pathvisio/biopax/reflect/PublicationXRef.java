package org.pathvisio.biopax.reflect;

import java.util.ArrayList;
import java.util.List;

public class PublicationXRef extends BiopaxElement {
	public PublicationXRef() {
		super();
		setName("PublicationXRef");
		setValidProperties(new PropertyType[] {
				PropertyType.AUTHORS,
				PropertyType.DB,
				PropertyType.ID,
				PropertyType.SOURCE,
				PropertyType.TITLE,
				PropertyType.YEAR,
			});
	}
	
	public PublicationXRef(String id) {
		this();
		setId(id);
	}
	
	public String getTitle() { 
		BiopaxProperty p = getProperty(PropertyType.TITLE.name());
		if(p != null) {
			return p.getName();
		} else {
			return null;
		}
	}
	
	public List<String> getAuthors() {
		List<String> authors = new ArrayList<String>();
		for(BiopaxProperty p : getProperties(PropertyType.AUTHORS.name())) {
			authors.add(p.getValue());
		}
		return authors;
	}
	
	public String toString() {
		String title = getTitle();
		String authors = "";
		for(String s : getAuthors()) {
			authors += "," + s;
		}
		return title + "; " + authors;
	}
}
