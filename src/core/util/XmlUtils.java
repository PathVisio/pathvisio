// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package util;

import gmmlVision.GmmlVision;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import data.MappFormat;
import data.GmmlGdb.IdCodePair;

public class XmlUtils {
	/**
	 * This sax handler can be used to quickly parse pathway information from
	 * a gpml file
	 */
	public static class PathwayParser extends DefaultHandler {
		String name;
		private ArrayList<Gene> genes;
		
		public PathwayParser() {
			name = "";
			genes = new ArrayList<Gene>();
		}
		
		public PathwayParser(XMLReader xmlReader) {
			this();
			xmlReader.setContentHandler(this);
			xmlReader.setEntityResolver(this);
		}
		
		public ArrayList<Gene> getGenes() { return genes; }
		
		public String getName() { return name; }
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if(localName.equals("GeneProduct")) { //For every geneproduct, store gene/code
				String name = attributes.getValue("Name");
				String sysName = attributes.getValue("GeneProduct-Data-Source");
				String code = MappFormat.sysName2Code.get(sysName);
				String symbol = attributes.getValue("GeneID");
				name = name == null ? "" : name;
				sysName = sysName == null ? "" : sysName;
				code = code == null ? "" : code;
				symbol = symbol == null ? "" : symbol;
				
				Gene gene = new Gene(name, code, symbol);
				if(!genes.contains(gene)) //Don't add duplicate genes
					genes.add(gene);
			}
			else if(localName.equals("Pathway")) {
				name = attributes.getValue("Name");
			}
		}
		
		public void error(SAXParseException e) { 
			GmmlVision.log.error("Error while parsing xml document", e);
		}
		
		public void fatalError(SAXParseException e) throws SAXParseException { 
			GmmlVision.log.error("Fatal error while parsing xml document", e);
			throw new SAXParseException("Fatal error, parsing of this document aborted", null);
		}
		
		public void warning(SAXParseException e) { 
			GmmlVision.log.error("Warning while parsing xml document", e);
		}
		
		public class Gene extends IdCodePair {
			String symbol;
			
			public Gene(String id, String code, String symbol) 
			{ super(id, code); this.symbol = symbol; }
			
			public String toString() { return getId(); }
			public String getSymbol() { return symbol; }
		}
	}
}
