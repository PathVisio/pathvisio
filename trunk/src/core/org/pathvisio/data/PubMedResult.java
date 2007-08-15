package org.pathvisio.data;

import java.util.ArrayList;
import java.util.List;

public class PubMedResult {
	private String id;
	private String title;
	private String source;
	private String year;
	private List<String> authors = new ArrayList<String>();
	
	public String getId() {
		return id;
	}
	
	void setId(String id) {
		this.id = id;
	}
		
	public String getSource() {
		return source;
	}
	
	void setSource(String source) {
		this.source = source;
	}
	
	public String getTitle() {
		return title;
	}
	
	void setTitle(String title) {
		this.title = title;
	}
	
	public String getYear() {
		return year;
	}
	
	void setYear(String year) {
		this.year = year;
	}
	
	public List<String> getAuthors() {
		return authors;
	}
	
	void addAuthor(String author) {
		authors.add(author);
	}
	
}
