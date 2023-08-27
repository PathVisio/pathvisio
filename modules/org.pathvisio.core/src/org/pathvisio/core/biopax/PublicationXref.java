/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2019 BiGCaT Bioinformatics
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.biopax;

import org.pathvisio.core.util.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Object to store a publication Xref (citation), based on BioPAX
 * In PathVisio, all citations  are stored as a BioPAX element.
 * Can hold information about a.o. authors, title and year.
 */
public class PublicationXref extends BiopaxNode {
	static final String PUBMED_URL = "http://www.ncbi.nlm.nih.gov/pubmed/";
	static final String DOI_URL = "http://doi.org/";

	public PublicationXref() {
		super();
		setName("PublicationXref");
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

	public String getDb() {
		return getPropertyValue(PropertyType.DB);
	}

	public void setDb(String db) {
		setPropertyValue(PropertyType.DB, db.equals("") ? "NA" : db);
	}

	public String getPubmedId() {
		return getPropertyValue(PropertyType.ID);
	}

	public void setPubmedId(String id) {
		setPropertyValue(PropertyType.ID, id.equals("") ? "NA" : id);
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
		StringBuilder builder = new StringBuilder();
		String title = getTitle();
		if(!Utils.isEmpty(title)) {
			builder.append(title);
			if (!title.endsWith(".") && title.endsWith("!") && title.endsWith("?")) {
				builder.append(".");
			}
			builder.append(" ");
		}
		String authors = getAuthorString();
		if(!Utils.isEmpty(authors)) {
			builder.append("<B>")
					.append(authors)
					.append("</B>. ");
		}
		String source = getSource();
		if(!Utils.isEmpty(source)) {
			if(source.startsWith("http://")) {
				builder.append("<A href='")
						.append(source)
						.append("'>")
						.append(source)
						.append("</A>. ");
			} else {
				builder.append("<I>")
						.append(source)
						.append("</I>. ");
			}
		}
		String year = getYear();
		if(!Utils.isEmpty(year)) {
			builder.append(year)
					.append(". ");
		}
		String db = getDb();
		String pmid = getPubmedId();
		if(!Utils.isEmpty(pmid)) {
			if (db.equals("PubMed")) {
				builder.append("<A href='" + PUBMED_URL)
						.append(pmid)
						.append("'>PubMed ")
						.append(pmid)
						.append("</A>.");
			} else if (db.equals("DOI")) {
				builder.append("<A href='" + DOI_URL)
						.append(pmid)
						.append("'>doi:")
						.append(pmid)
						.append("</A>.");
			} else {
				builder.append(db)
						.append(" ")
						.append(pmid);
			}
		}
		return 	builder.toString();
	}
}
