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
package org.pathvisio.wikipathways.bots;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

public class BotReport {
	private static final String SEPARATOR = "\t";
	private static final String COMMENT = "#";

	static final String FIELD_DATE = "Date";
	static final String FIELD_TITLE = "Title";
	static final String FIELD_DESCRIPTION = "Description";

	static final String COL_NAME = "Pathway name";
	static final String COL_SPECIES = "Pathway species";
	static final String COL_URL = "Url";

	private String title = "";
	private Date date = new Date();
	private String[] defaultColumns;
	private String[] columns;
	private HashMap<WSPathwayInfo, String[]> data;
	private HashMap<String, String> comments;

	public BotReport(String[] columns) {
		this.columns = columns;
		defaultColumns = new String[] {
				COL_NAME,
				COL_SPECIES,
				COL_URL
		};
		data = new HashMap<WSPathwayInfo, String[]>();
		comments = new HashMap<String, String>();
		setDescription("");
	}

	String[] getDefaultColumns() {
		return defaultColumns;
	}

	String[] getDefaultData(WSPathwayInfo p) {
		return new String[] {
				p.getName(),
				p.getSpecies(),
				p.getUrl()
		};
	}

	public void setRow(WSPathwayInfo p, String[] rowData) {
		data.put(p, rowData);
	}

	public void setDate(Date d) {
		date = d;
	}

	public void setTitle(String t) {
		title = t;
	}

	public void setDescription(String d) {
		comments.put(FIELD_DESCRIPTION, d);
	}

	public void setComment(String name, String value) {
		comments.put(name, value);
	}

	public void writeTextReport(File f) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		//Write comment headers
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
		out.append(COMMENT + FIELD_DATE + SEPARATOR + timestamp + "\n");
		out.append(COMMENT + FIELD_TITLE + SEPARATOR + title + "\n");

		List<String> cnames = new ArrayList<String>(comments.keySet());
		Collections.sort(cnames);

		for(String n : cnames) {
			out.append(COMMENT + n + SEPARATOR + comments.get(n) + "\n");
		}

		//Write column headers
		out.append(implode(getDefaultColumns(), SEPARATOR) + SEPARATOR);
		out.append(implode(columns, SEPARATOR) + "\n");

		//Write rows
		for(WSPathwayInfo p : data.keySet()) {
			String[] cols = data.get(p);
			String[] defCols = getDefaultData(p);
			out.append(implode(defCols, SEPARATOR) + SEPARATOR);
			out.append(implode(cols, SEPARATOR) + "\n");
		}
		out.close();
	}

	public void writeHtmlReport(File f) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f));

		String html = "<html><head><script src=\"sorttable.js\"></script>";
		html += " <link rel=\"stylesheet\" type=\"text/css\" href=\"botresult.css\">";
		html += "</head><body>";

		html += "<h1>" + title + "</h1>";
		DateFormat df = DateFormat.getDateInstance();
		html += "<BIG>" + df.format(date) + "</BIG>";

		html += "<dl class='botresult'>";
		List<String> cnames = new ArrayList<String>(comments.keySet());
		Collections.sort(cnames);
		for(String n : cnames) {
			html += "<dt>" + n + ":<dd>" + comments.get(n);
		}
		html += "</dl>";

		html += "<table class=\"sortable botresult\"><tbody>";
		html += "<th>Pathway<th>Organism<th>";
		html += implode(columns, "<th>");

		out.append(html);

		for(WSPathwayInfo p : data.keySet()) {
			html = "<tr>";
			String name = p.getName();
			if(name == null || "".equals(name)) name = p.getUrl();
			html += "<td><a href=\"" + p.getUrl() + "\">" +
				name + "</a></td>";
			html += "<td>" + p.getSpecies() + "</td>";

			String[] row = data.get(p);
			for(String s : row) {
				html += "<td>" + s + "</td>";
			}
			out.append(html);
		}

		out.append("</tbody></table></body></html>");
		out.close();
	}

	private String implode(String[] array, String sep) {
		StringBuilder sb = new StringBuilder();
		for(String s : array) {
			sb.append(s);
			sb.append(sep);
		}
		return sb.substring(0, sb.length() - sep.length());
	}
}
