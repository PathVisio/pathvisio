package search;

import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import search.SearchMethods.GeneParser.IdCodePair;
import search.SearchResults.Attribute;
import search.SearchResults.SearchResult;
import data.GmmlData;
import data.GmmlGdb;
import data.GmmlGex;

//TODO: progress bar
public class SearchMethods {
	private GmmlGex gmmlGex;
	private GmmlGdb gmmlGdb;
	
	public SearchMethods(GmmlGex gmmlGex, GmmlGdb gmmlGdb) {
		this.gmmlGex = gmmlGex;
		this.gmmlGdb = gmmlGdb;
	}
	
	/**
	 * Search for pathways containing the given gene and display result in given result table
	 * @param id
	 * @param code
	 * @param folder
	 * @param srt
	 */
	public int pathwaysContainingGene(String id, String code, File folder, SearchResultTable srt) {
		SearchResults srs = new SearchResults();
		srs.addAttribute("pathway", Attribute.TYPE_TEXT);
		srs.addAttribute("directory", Attribute.TYPE_TEXT);

		srt.setSearchResults(srs);
		//Get the ensembl ids for the gene to search for
		ArrayList<String> ensIds = gmmlGdb.ref2EnsIds(id, code);
		if(ensIds.size() == 0) return 0; //Gene not found
		
		//get all pathway files in the folder and subfolders
		ArrayList<File> pathways = getPathwayFiles(folder);
		
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			for(File f : pathways) {
				//Get all genes in the pathway
				GeneParser parser = new GeneParser();
				xmlReader.setContentHandler(parser);
				xmlReader.setErrorHandler(parser);
				try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { }
				ArrayList<IdCodePair> genes = parser.getGenes();
				GmmlVision.log.trace("searching pathway " + f.getName() + "...");
				long time = System.currentTimeMillis();
				//Get all ensembl ids of the genes in the pathway
				ArrayList<String> pwEnsIds = new ArrayList<String>();
				for(IdCodePair idcp : genes) {
					pwEnsIds.addAll(gmmlGdb.ref2EnsIds(idcp.id, idcp.code)); 
				}
				GmmlVision.log.trace("\t...getting ensembl ids took " + 
						(System.currentTimeMillis() - time) + " ms");
				//Check if one of the given gene's ensids is in the pathway
				for(String ensId : ensIds) {
					if(pwEnsIds.contains(ensId)) {//Gene found, add pathway to search result and break
						SearchResult sr = srs.new SearchResult();
						sr.setAttribute("pathway", f.getName());
						sr.setAttribute("directory", f.getPath());
						GmmlVision.log.trace("result found!: " + f.getName() + ", " + f.getPath());
						srt.getTableViewer().refresh(true);
						break;
					}
				}
			}
		} catch(Exception e) { GmmlVision.log.error("while searching", e); }
		GmmlVision.log.trace("search finished");
		return srs.getResults().size();
		
	}
	
	private ArrayList<File> getPathwayFiles(File folder) {
		ArrayList<File> pathways = new ArrayList<File>();
		
		//Get all pathways in this directory
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".xml") || dir.isDirectory()) ? true : false;
			}
		});
		//Recursively add the pathway files
		for(File f : files) {
			if(f.isDirectory()) pathways.addAll(getPathwayFiles(f));
			else pathways.add(f);
		}
		
		return pathways;
	}
	
	public class GeneParser extends DefaultHandler {
		private ArrayList<IdCodePair> genes;
		
		public GeneParser() {
			genes = new ArrayList<IdCodePair>();
		}
		
		public ArrayList<IdCodePair> getGenes() { return genes; }
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
			if(localName.equals("GeneProduct")) { //For every geneproduct, store gene/code
				String name = attributes.getValue("Name");
				String sysName = attributes.getValue("GeneProduct-Data-Source");
				String code = GmmlData.sysName2Code.get(sysName);
				if(name != null && !name.equals("") && code != null) {
					genes.add(new IdCodePair(name, code));
				}
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
		
		public class IdCodePair {
			String id;
			String code;
			public IdCodePair(String id, String code) { this.id = id; this.code = code; }
		}
	}
}
