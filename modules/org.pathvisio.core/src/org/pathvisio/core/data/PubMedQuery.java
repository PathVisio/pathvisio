// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class can handle a query for a pubmed record.
 * Just instantiate this class with a given pubmed id (pmid),
 * and run execute() (this method may block, so don't call it from the UI thread)
 * The result can then be obtained with getResult()
 * TODO: move DefaultHandler methods to private subclass, they don't need to be exposed.
 */
public class PubMedQuery extends DefaultHandler {
	static final String URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi";

	String id;
	PubMedResult result;

	/**
	 * Prepares a new pubmed query for the given pmid, e.g. "17588266".
	 */
	public PubMedQuery(String id) {
		this.id = id;
	}

	/**
	 * Execute a query. Don't call this from the UI thread, because
	 * this method blocks.
	 */
	public void execute() throws IOException, SAXException {
		//TODO: assert not being in UI thread
		String urlString = URL_BASE;
		urlString += "?db=pubmed&id=" + id;

		URL url = new URL(urlString);
		InputStream is = url.openStream();

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setEntityResolver(this);

		result = new PubMedResult();
		result.setId(id);
		xmlReader.parse(new InputSource(is));

		is.close();
	}

	/**
	 * get the result, after execute() has finished.
	 */
	public PubMedResult getResult() {
		return result;
	}

	String parsingId;
	String parsingName;
	String parsingElement;
	String parsingValue;
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//		System.out.println("New element: " + localName + ", " + attributes.getValue(NAME));
		parsingElement = localName;
		parsingName = attributes.getValue(NAME);
		parsingValue = "";
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		parsingValue += new String(ch, start, length).trim();
//		System.out.println("characters: " + new String(ch, start, length).trim());
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
//		System.out.println("End element: " + localName);
		if(parsingElement == ID) {
			parsingId = parsingValue;
		}
		if		(TITLE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing title: " + value);
			result.setTitle(parsingValue);
		}
		else if (PUBDATE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing pubdate: " + value);
			if(parsingValue.length() >= 4) parsingValue = parsingValue.substring(0, 4);
			result.setYear(parsingValue);
		}
		else if (SOURCE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing source: " + value);
			result.setSource(parsingValue);
		}
		else if (AUTHOR.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsed author: " + parsingValue);
			result.addAuthor(parsingValue);
		}
		parsingElement = "";
		parsingName = "";
	}

	static final String ITEM = "Item";
	static final String ID = "Id";
	static final String NAME = "Name";
	static final String TITLE = "Title";
	static final String PUBDATE = "PubDate";
	static final String SOURCE = "Source";
	static final String AUTHOR_LIST = "AuthorList";
	static final String AUTHOR = "Author";

	/*
<DocSum>
	<Id>17588266</Id>
	<Item Name="PubDate" Type="Date">2007 Jun 24</Item>
	<Item Name="EPubDate" Type="Date">2007 Jun 24</Item>
	<Item Name="Source" Type="String">BMC Bioinformatics</Item>
	<Item Name="AuthorList" Type="List">

		<Item Name="Author" Type="String">Salomonis N</Item>
		<Item Name="Author" Type="String">Hanspers K</Item>
		<Item Name="Author" Type="String">Zambon AC</Item>
		<Item Name="Author" Type="String">Vranizan K</Item>
		<Item Name="Author" Type="String">Lawlor SC</Item>
		<Item Name="Author" Type="String">Dahlquist KD</Item>

		<Item Name="Author" Type="String">Doniger SW</Item>
		<Item Name="Author" Type="String">Stuart J</Item>
		<Item Name="Author" Type="String">Conklin BR</Item>
		<Item Name="Author" Type="String">Pico AR</Item>
	</Item>
	<Item Name="LastAuthor" Type="String">Pico AR</Item>

	<Item Name="Title" Type="String">GenMAPP 2: new features and resources for pathway analysis.</Item>
	<Item Name="Volume" Type="String">8</Item>
	<Item Name="Issue" Type="String"></Item>
	<Item Name="Pages" Type="String">217</Item>
	<Item Name="LangList" Type="List">
		<Item Name="Lang" Type="String">English</Item>
	</Item>

	<Item Name="NlmUniqueID" Type="String">100965194</Item>
	<Item Name="ISSN" Type="String"></Item>
	<Item Name="ESSN" Type="String">1471-2105</Item>
	<Item Name="PubTypeList" Type="List">
		<Item Name="PubType" Type="String">Journal Article</Item>
	</Item>
	<Item Name="RecordStatus" Type="String">PubMed - in process</Item>

	<Item Name="PubStatus" Type="String">epublish</Item>
	<Item Name="ArticleIds" Type="List">
		<Item Name="pii" Type="String">1471-2105-8-217</Item>
		<Item Name="doi" Type="String">10.1186/1471-2105-8-217</Item>
		<Item Name="pubmed" Type="String">17588266</Item>
	</Item>
	<Item Name="DOI" Type="String">10.1186/1471-2105-8-217</Item>

	<Item Name="History" Type="List">
		<Item Name="received" Type="Date">2006/11/16 00:00</Item>
		<Item Name="accepted" Type="Date">2007/06/24 00:00</Item>
		<Item Name="aheadofprint" Type="Date">2007/06/24 00:00</Item>
		<Item Name="pubmed" Type="Date">2007/06/26 09:00</Item>
		<Item Name="medline" Type="Date">2007/06/26 09:00</Item>

	</Item>
	<Item Name="References" Type="List"></Item>
	<Item Name="HasAbstract" Type="Integer">1</Item>
	<Item Name="PmcRefCount" Type="Integer">0</Item>
	<Item Name="FullJournalName" Type="String">BMC bioinformatics</Item>
	<Item Name="SO" Type="String">2007 Jun 24;8:217</Item>
</DocSum>

*/
}
