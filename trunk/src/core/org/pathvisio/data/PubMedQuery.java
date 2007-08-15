package org.pathvisio.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class PubMedQuery extends DefaultHandler {
	static final String URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi";
	
	String id;
	PubMedResult result;
	
	public PubMedQuery(String id) {
		this.id = id;
	}
	
	public void execute() throws IOException, SAXException {
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
	
	public PubMedResult getResult() {
		return result;
	}
	
	String parsingId;
	String parsingName;
	String parsingElement;
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		parsingElement = localName;
		parsingName = attributes.getValue(NAME);
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {		
		String value = new String(ch, start, length).trim();
		if(value.length() == 0) return;
		
		if(parsingElement == ID) {
			parsingId = value;
		}
		if		(TITLE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing title: " + value);
			result.setTitle(value);
		}
		else if (PUBDATE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing pubdate: " + value);
			if(value.length() >= 4) value = value.substring(0, 4);
			result.setYear(value);
		}
		else if (SOURCE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing source: " + value);
			result.setSource(value);
		}
		else if (AUTHOR.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing author: " + value);
			result.addAuthor(value);
		}
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
