// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.pathvisio.biopax.reflect;

import java.util.ArrayList;
import java.util.List;

/**
 * Object to store a publication Xref (citation), based on BioPAX
 * In PathVisio, all citations  are stored as a BioPAX element.
 * Can hold information about a.o. authors, title and year.
 */
public class PublicationXRef extends BiopaxElement {
	static final String PUBMED_URL = "http://www.ncbi.nlm.nih.gov/pubmed/";

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
			if(a != null && a.length() > 0) addAuthor(a.trim());
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

	public String toHTML() {
		return toString();
	}

	public String toString() {
		String title = getTitle();
		String pmid = getPubmedId();
		String authors = getAuthorString();
		String source = getSource();
		String year = getYear();
		if(title != null && title.length() > 0) {
			title += "; ";
		} else {
			title = "";
		}
		if(authors != null && authors.length() > 0) {
			authors = "<B>" + authors + "</B>; ";
		} else {
			authors = "";
		}
		if(source != null && source.length() > 0) {
			if(source.startsWith("http://")) {
				source = "<A href='" + source + "'>" + source + "</A>; ";
			} else {
				source = "<I>" + source + "</I>; ";
			}
		}
		if(year != null && year.length() > 0) {
			year += "; ";
		}
		if(pmid != null && pmid.length() > 0) {
			pmid = "<A href='" + PUBMED_URL + pmid + "'>" +
				"PubMed" + "</A>";
		} else {
			pmid = "";
		}
		return 	title + authors + source + year + pmid;
	}
}
