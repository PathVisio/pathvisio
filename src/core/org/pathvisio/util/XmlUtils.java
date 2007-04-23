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
package org.pathvisio.util;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.pathvisio.gmmlVision.GmmlVision;
import org.pathvisio.data.DataSources;
import org.pathvisio.data.GmmlGdb.IdCodePair;
import org.pathvisio.model.MappFormat;

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
		
		Gene currentGene = null;
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if(localName.equals("DataNode")) 
			{ 
				// the only way this can be not null
				// is when two consecutive DataNode opening tags don't have an Xref in between
				assert (currentGene != null);				
				currentGene = new Gene();

				String symbol = attributes.getValue("TextLabel");
				currentGene.setSymbol(symbol);		
			}
			else if(localName.equals("Pathway")) {
				name = attributes.getValue("Name");
			}
			else if(localName.equals("Xref"))
			{
				String sysName = attributes.getValue("Database");
				assert (sysName != null);
				String code = DataSources.sysName2Code.get(sysName);
				assert (code != null);
				String geneId = attributes.getValue("ID");
				assert (geneId != null);
				
				currentGene.setCode(code);
				currentGene.setId(geneId);
				
				if(!genes.contains(currentGene)) //Don't add duplicate genes
					genes.add(currentGene);
				currentGene = null;
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
			
			public Gene() { super (null, null); }			
			
			public String toString() { return getId(); }
			public String getSymbol() { return symbol; }
			public void setSymbol(String value) { symbol = value; }
		}
	}
}
