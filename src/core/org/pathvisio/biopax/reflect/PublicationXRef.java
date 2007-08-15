package org.pathvisio.biopax.reflect;

import java.util.ArrayList;
import java.util.Collection;
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
	
	private String getPropertyValue(PropertyType pt) {
		BiopaxProperty p = getProperty(pt.name());
		if(p != null) {
			return p.getText();
		} else {
			return null;
		}
	}
	
	private void setPropertyValue(PropertyType pt, String value) {
		addProperty(new BiopaxProperty(pt, value));	
	}
	
	public String getTitle() { 
		return getPropertyValue(PropertyType.TITLE);
	}
	
	public void setTitle(String title) {
		setPropertyValue(PropertyType.TITLE, title);
	}
	
	public String getSource() {
		return getPropertyValue(PropertyType.SOURCE);
	}
	
	public void setSource(String source) {
		setPropertyValue(PropertyType.SOURCE, source);
	}
	
	public String getYear() {
		return getPropertyValue(PropertyType.YEAR);
	}
	
	public void setYear(String year) {
		setPropertyValue(PropertyType.YEAR, year);
	}
	
	public String getPubmedId() {
		return getPropertyValue(PropertyType.ID);
	}
	
	public void setPubmedId(String id) {
		setPropertyValue(PropertyType.ID, id);
		setPropertyValue(PropertyType.DB, "PubMed");
	}
	
	public List<String> getAuthors() {
		List<String> authors = new ArrayList<String>();
		for(BiopaxProperty p : getProperties(PropertyType.AUTHORS.name())) {
			authors.add(p.getValue());
		}
		return authors;
	}
	
	public void addAuthor(String author) {
		setPropertyValue(PropertyType.AUTHORS, author);
	}
	
	public void removeAuthor(String author) {
		for(BiopaxProperty p : getProperties(PropertyType.AUTHORS.name())) {
			if(author.equals(p.getValue())) {
				removeProperty(p);
			}
		}
	}
	
	private void clearAuthors() {
		for(BiopaxProperty p : getProperties(PropertyType.AUTHORS.name())) {
			removeProperty(p);
		}
	}
	
	public void setAuthors(String authorString) {
		clearAuthors();
		String[] authors = parseAuthorString(authorString);
		for(String a : authors) {
			addAuthor(a);
		}
	}
		
	public static final String AUTHOR_SEP = ";";
	
	public String[] parseAuthorString(String s) {
		String[] splitted = s.split(AUTHOR_SEP);
		String[] result = new String[splitted.length];
		for(int i = 0; i < result.length; i++) {
			result[i] = splitted[i].trim();
		}
		return result;
	}
	
	public String getAuthorString() {
		return createAuthorString(getAuthors());
	}
	
	public static String createAuthorString(List<String> authors) {
		String as = "";
		for(String a : authors) {
			as += a + AUTHOR_SEP + " ";
		}
		if(as.length() > 0) {
			as = as.substring(0, as.length() - AUTHOR_SEP.length() - 1);
		}
		return as;
	}
	
	public String toString() {
		String title = getTitle();
		String pmid = getPubmedId();
		String authors = getAuthorString();
		return 	(title != null && title.length() > 0 ? title + "; " : "") + 
				(pmid != null && pmid.length() > 0 ? authors + "; ": "") + 
				(authors != null && authors.length() > 0 ? " pmid=" + pmid : "");
	}
}
