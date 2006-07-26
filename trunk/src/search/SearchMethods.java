package search;

import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import search.PathwaySearchComposite.SearchRunnableWithProgress;
import search.SearchMethods.GeneParser.Gene;
import search.SearchResults.Attribute;
import search.SearchResults.SearchResult;
import data.GmmlData;
import data.GmmlGdb;

public class SearchMethods {
	private GmmlGdb gmmlGdb;
	
	public SearchMethods(GmmlGdb gmmlGdb) {
		this.gmmlGdb = gmmlGdb;
	}
	
	/**
	 * Search for pathways containing the given gene and display result in given result table
	 * @param id	Gene identifier to search for
	 * @param code	System code of the gene identifier
	 * @param folder	Directory to search (includes sub-directories)
	 * @param srt	{@link SearchResultTable} to display the results in
	 * @return the number of results found, -1 if the process is cancelled
	 */
	public int pathwaysContainingGene(String id, String code, File folder, 
			SearchResultTable srt) {
		return pathwaysContainingGene(id, code, folder, srt);
	}
	
	/**
	 * Search for pathways containing the given gene and display result in given result table
	 * @param id	Gene identifier to search for
	 * @param code	System code of the gene identifier
	 * @param folder	Directory to search (includes sub-directories)
	 * @param srt	{@link SearchResultTable} to display the results in
	 * @param runnable	{@link SearchRunnableWithProgress} containing the monitor responsible for
	 * displaying the progress
	 * @return the number of results found, -1 if the process is cancelled
	 */
	public int pathwaysContainingGene(String id, String code, File folder, 
			SearchResultTable srt, SearchRunnableWithProgress runnable) {
		
		SearchResults srs = new SearchResults();
		srs.addAttribute("pathway", Attribute.TYPE_TEXT);
		srs.addAttribute("directory", Attribute.TYPE_TEXT);
		srs.addAttribute("file", Attribute.TYPE_TEXT, false);

		srt.setSearchResults(srs);
		//Get all cross references
		ArrayList<String> refs = gmmlGdb.getCrossRefs(id, code);
		if(refs.size() == 0) return 0; //Gene not found
		
		runnable.updateMonitor(200);
		
		//get all pathway files in the folder and subfolders
		ArrayList<File> pathways = getPathwayFiles(folder);
		
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			for(File f : pathways) {
				if(runnable.monitor.isCanceled()) return -1;
				//Get all genes in the pathway
				GeneParser parser = new GeneParser();
				xmlReader.setContentHandler(parser);
				xmlReader.setErrorHandler(parser);
				try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { }
				ArrayList<Gene> genes = parser.getGenes();
				//Check if one of the given ids is in the pathway
				for(Gene gene : genes) {
					if(refs.contains(gene.id)) {//Gene found, add pathway to search result and break
						SearchResult sr = srs.new SearchResult();
						sr.setAttribute("pathway", f.getName());
						sr.setAttribute("directory", f.getParentFile().getName());
						sr.setAttribute("file", f.getAbsolutePath());
						srt.refreshTableViewer(true);
						break;
					}
				}
				runnable.updateMonitor((int)Math.ceil(800.0 / pathways.size()));
			}
		} catch(Exception e) { GmmlVision.log.error("while searching", e); }
		GmmlVision.log.trace("search finished");
		return srs.getResults().size();
	}
	
	public int pathwaysContainingGeneSymbol(String regex, File folder, 
			SearchResultTable srt, SearchRunnableWithProgress runnable) {
		
		//Create regex
		Pattern pattern = Pattern.compile(regex);
		
		SearchResults srs = new SearchResults();
		srs.addAttribute("pathway", Attribute.TYPE_TEXT);
		srs.addAttribute("directory", Attribute.TYPE_TEXT);
		srs.addAttribute("file", Attribute.TYPE_TEXT, false);
		srs.addAttribute("matches", Attribute.TYPE_TEXT);

		srt.setSearchResults(srs);
		
		//get all pathway files in the folder and subfolders
		ArrayList<File> pathways = getPathwayFiles(folder);
		
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			for(File f : pathways) {
				if(runnable.monitor.isCanceled()) return -1;
				//Get all genes in the pathway
				GeneParser parser = new GeneParser();
				xmlReader.setContentHandler(parser);
				xmlReader.setErrorHandler(parser);
				try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { }
				ArrayList<Gene> genes = parser.getGenes();
				//Find what symbols match
				ArrayList<Gene> matched = new ArrayList<Gene>();
				for(Gene gene : genes) {
					Matcher m = pattern.matcher(gene.symbol);
					if(m.find()) matched.add(gene);
				}
				if(matched.size() > 0) {
					SearchResult sr = srs.new SearchResult();
					sr.setAttribute("pathway", f.getName());
					sr.setAttribute("directory", f.getParentFile().getName());
					sr.setAttribute("file", f.getAbsolutePath());
					sr.setAttribute("matches", matched.toString());
					srt.refreshTableViewer(true);
				}
				runnable.updateMonitor((int)Math.ceil(1000.0 / pathways.size()));
			}
		} catch(Exception e) { GmmlVision.log.error("while searching", e); }
		GmmlVision.log.trace("search finished");
		return srs.getResults().size();
	}
	
	/**
	 * Get all pathway (.xml) file in a directory (including subdirectories)
	 * @param folder
	 * @return
	 */
	private ArrayList<File> getPathwayFiles(File folder) {
		ArrayList<File> pathways = new ArrayList<File>();
		
		//Get all pathways in this directory
		File[] files = folder.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return (f.isDirectory() || f.getName().endsWith(".xml")) ? true : false;
			}
		});
		//Recursively add the pathway files
		for(File f : files) {
			if(f.isDirectory()) pathways.addAll(getPathwayFiles(f));
			else pathways.add(f);
		}
		
		return pathways;
	}
	
	/**
	 * This sax handler can be used to quickly parse gene information (id, systemcode) from
	 * a gmml file
	 */
	public class GeneParser extends DefaultHandler {
		private ArrayList<Gene> genes;
		
		public GeneParser() {
			genes = new ArrayList<Gene>();
		}
		
		public ArrayList<Gene> getGenes() { return genes; }
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
			if(localName.equals("GeneProduct")) { //For every geneproduct, store gene/code
				String name = attributes.getValue("Name");
				String sysName = attributes.getValue("GeneProduct-Data-Source");
				String code = GmmlData.sysName2Code.get(sysName);
				String symbol = attributes.getValue("GeneID");
				name = name == null ? "" : name;
				sysName = sysName == null ? "" : sysName;
				code = code == null ? "" : code;
				symbol = symbol == null ? "" : symbol;
				genes.add(new Gene(name, code, symbol));
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
		
		public class Gene {
			String id;
			String code;
			String symbol;
			public Gene(String id, String code, String symbol) 
			{ this.id = id; this.code = code; this.symbol = symbol; }
			
			public String toString() { return symbol; }
		}
	}
}
