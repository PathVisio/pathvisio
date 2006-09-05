package search;

import gmmlVision.GmmlVision;

import java.io.File;
import java.util.*;
import java.util.regex.*;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import search.PathwaySearchComposite.SearchRunnableWithProgress;
import search.SearchResults.Attribute;
import search.SearchResults.SearchResult;
import util.FileUtils;
import util.XmlUtils.PathwayParser;
import data.GmmlGdb;

public abstract class SearchMethods {	
	public static final String MSG_NOT_IN_GDB = "Gene not found in selected gene database";
	public static final String MSG_NOTHING_FOUND = "Nothing found";
	public static final String MSG_CANCELLED = "cancelled";
		
	/**
	 * Search for pathways containing the given gene and display result in given result table
	 * @param id	Gene identifier to search for
	 * @param code	System code of the gene identifier
	 * @param folder	Directory to search (includes sub-directories)
	 * @param srt	{@link SearchResultTable} to display the results in
	 * @return string with message to display. if null, no message is displayed
	 */
	public static String pathwaysContainingGene(String id, String code, File folder, 
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
	 * @return string with message to display. if null, no message is displayed
	 */
	public static String pathwaysContainingGene(String id, String code, File folder, 
			SearchResultTable srt, SearchRunnableWithProgress runnable) {
		
		SearchResults srs = new SearchResults();
		srs.addAttribute("pathway", Attribute.TYPE_TEXT);
		srs.addAttribute("directory", Attribute.TYPE_TEXT);
		srs.addAttribute("file", Attribute.TYPE_TEXT, false);
		srs.addAttribute("idsFound", Attribute.TYPE_ARRAYLIST, false);

		srt.setSearchResults(srs);
		//Get all cross references
		List<String> refs = GmmlGdb.getCrossRefs(id, code);
		if(refs.size() == 0) return MSG_NOT_IN_GDB;
		
		runnable.updateMonitor(200);
		
		//get all pathway files in the folder and subfolders
		ArrayList<File> pathways = FileUtils.getFiles(folder, "xml", true);
		
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			for(File f : pathways) {
				if(runnable.monitor.isCanceled()) return MSG_CANCELLED;
				//Get all genes in the pathway
				PathwayParser parser = new PathwayParser(xmlReader);
				try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { }
				ArrayList<PathwayParser.Gene> genes = parser.getGenes();
				//Check if one of the given ids is in the pathway
				for(PathwayParser.Gene gene : genes) {
					if(refs.contains(gene.getId())) {//Gene found, add pathway to search result and break
						SearchResult sr = srs.new SearchResult();
						sr.setAttribute("pathway", f.getName());
						sr.setAttribute("directory", f.getParentFile().getName());
						sr.setAttribute("file", f.getAbsolutePath());
						ArrayList<String> idsFound = new ArrayList<String>();
						idsFound.add(gene.getId());
						sr.setAttribute("idsFound", idsFound);
						srt.refreshTableViewer(true);
						break;
					}
				}
				runnable.updateMonitor((int)Math.ceil(800.0 / pathways.size()));
			}
		} catch(Exception e) { GmmlVision.log.error("while searching", e); }
		GmmlVision.log.trace("search finished");
		return srs.getResults().size() == 0 ? MSG_NOTHING_FOUND : null;
	}
	
	public static String pathwaysContainingGeneSymbol(String regex, File folder, 
			SearchResultTable srt, SearchRunnableWithProgress runnable) {
		
		//Create regex
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		
		SearchResults srs = new SearchResults();
		srs.addAttribute("pathway", Attribute.TYPE_TEXT);
		srs.addAttribute("directory", Attribute.TYPE_TEXT);
		srs.addAttribute("file", Attribute.TYPE_TEXT, false);
		srs.addAttribute("namesFound", Attribute.TYPE_ARRAYLIST);
		srs.addAttribute("idsFound", Attribute.TYPE_ARRAYLIST, false);

		srt.setSearchResults(srs);
		
		//get all pathway files in the folder and subfolders
		ArrayList<File> pathways = FileUtils.getFiles(folder, "xml", true);
		
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			for(File f : pathways) {
				if(runnable.monitor.isCanceled()) return MSG_CANCELLED;
				//Get all genes in the pathway
				PathwayParser parser = new PathwayParser(xmlReader);
				try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { }
				ArrayList<PathwayParser.Gene> genes = parser.getGenes();
				//Find what symbols match
				ArrayList<PathwayParser.Gene> matched = new ArrayList<PathwayParser.Gene>();
				ArrayList<String> idsFound = new ArrayList<String>();
				ArrayList<String> namesFound = new ArrayList<String>();
				for(PathwayParser.Gene gene : genes) {
					Matcher m = pattern.matcher(gene.getSymbol());
					if(m.find()) {
						matched.add(gene);
						idsFound.add(gene.getId());
						namesFound.add(gene.getSymbol());
					}
				}
				if(matched.size() > 0) {
					SearchResult sr = srs.new SearchResult();
					sr.setAttribute("pathway", f.getName());
					sr.setAttribute("directory", f.getParentFile().getName());
					sr.setAttribute("file", f.getAbsolutePath());
					sr.setAttribute("idsFound", idsFound);
					sr.setAttribute("namesFound", namesFound);
					
					srt.refreshTableViewer(true);
				}
				runnable.updateMonitor((int)Math.ceil(1000.0 / pathways.size()));
			}
		} catch(Exception e) { GmmlVision.log.error("while searching", e); }
		GmmlVision.log.trace("search finished");
		return srs.getResults().size() == 0 ? MSG_NOTHING_FOUND : null;
	}
}
