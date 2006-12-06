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
package search;

import gmmlVision.GmmlVision;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import search.PathwaySearchComposite.SearchRunnableWithProgress;
import util.FileUtils;
import util.XmlUtils.PathwayParser;
import util.tableviewer.PathwayTable;
import util.tableviewer.TableData;
import util.tableviewer.TableData.Column;
import util.tableviewer.TableData.Row;
import data.GmmlGdb;
import data.GmmlGdb.IdCodePair;

public abstract class SearchMethods {	
	public static final String MSG_NOT_IN_GDB = "Gene not found in selected gene database";
	public static final String MSG_NOTHING_FOUND = "Nothing found";
	public static final String MSG_CANCELLED = "cancelled";
	
	public static final double TOTAL_WORK = 1000.0;
		
	/**
	 * Search for pathways containing the given gene and display result in given result table
	 * @param id	Gene identifier to search for
	 * @param code	System code of the gene identifier
	 * @param folder	Directory to search (includes sub-directories)
	 * @param srt	{@link SearchResultTable} to display the results in
	 * @return string with message to display. if null, no message is displayed
	 */
	public static String pathwaysContainingGene(String id, String code, File folder, 
			SearchResultTable srt) throws SearchException {
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
	public static void pathwaysContainingGeneID(String id, String code, File folder, 
			SearchResultTable srt, SearchRunnableWithProgress runnable) 
			throws SearchException, SAXException {
		
		TableData srs = new TableData();
		srs.addColumn("pathway", Column.TYPE_TEXT);
		srs.addColumn("directory", Column.TYPE_TEXT);
		srs.addColumn(PathwayTable.COLNAME_FILE, Column.TYPE_TEXT, false);
		srs.addColumn(SearchResultTable.COLUMN_FOUND_IDS, Column.TYPE_ARRAYLIST, false);

		srt.setTableData(srs);
		//Get all cross references
		List<IdCodePair> refs = GmmlGdb.getCrossRefs(id, code);
		if(refs.size() == 0) throw new NoGdbException();
		
		SearchRunnableWithProgress.monitorWorked((int)(TOTAL_WORK * 0.2));
		
		//get all pathway files in the folder and subfolders
		ArrayList<File> pathways = FileUtils.getFiles(folder, GmmlVision.PATHWAY_FILE_EXTENSION, true);

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		for(File f : pathways) {
			if(SearchRunnableWithProgress.getMonitor().isCanceled()) return;
			//Get all genes in the pathway
			PathwayParser parser = new PathwayParser(xmlReader);
			try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { }
			ArrayList<PathwayParser.Gene> genes = parser.getGenes();
			//Check if one of the given ids is in the pathway
			for(PathwayParser.Gene gene : genes) {
				if(refs.contains(new IdCodePair(gene.getId(), gene.getCode()))) {//Gene found, add pathway to search result and break
					Row sr = srs.new Row();
					sr.setColumn("pathway", f.getName());
					sr.setColumn("directory", f.getParentFile().getName());
					sr.setColumn(PathwayTable.COLNAME_FILE, f.getAbsolutePath());
					ArrayList<String> idsFound = new ArrayList<String>();
					idsFound.add(gene.getId());
					sr.setColumn(SearchResultTable.COLUMN_FOUND_IDS, idsFound);
					srt.refreshTableViewer(true);
					break;
				}
			}
			SearchRunnableWithProgress.monitorWorked((int)Math.ceil(TOTAL_WORK / pathways.size()));
		}
		if(srs.getResults().size() == 0) throw new NothingFoundException();
	}

	public static void pathwaysContainingGeneSymbol(String regex, File folder, 
			SearchResultTable srt, SearchRunnableWithProgress runnable) 
			throws SearchException, SAXException {

		//Create regex
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		
		TableData srs = new TableData();
		srs.addColumn("pathway", Column.TYPE_TEXT);
		srs.addColumn("directory", Column.TYPE_TEXT);
		srs.addColumn(PathwayTable.COLNAME_FILE, Column.TYPE_TEXT, false);
		srs.addColumn("namesFound", Column.TYPE_ARRAYLIST);
		srs.addColumn(SearchResultTable.COLUMN_FOUND_IDS, Column.TYPE_ARRAYLIST, false);

		srt.setTableData(srs);
		
		//get all pathway files in the folder and subfolders
		ArrayList<File> pathways = FileUtils.getFiles(folder, GmmlVision.PATHWAY_FILE_EXTENSION, true);

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		for(File f : pathways) {
			if(SearchRunnableWithProgress.getMonitor().isCanceled()) return;
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
				Row sr = srs.new Row();
				sr.setColumn("pathway", f.getName());
				sr.setColumn("directory", f.getParentFile().getName());
				sr.setColumn(PathwayTable.COLNAME_FILE, f.getAbsolutePath());
				sr.setColumn(SearchResultTable.COLUMN_FOUND_IDS, idsFound);
				sr.setColumn("namesFound", namesFound);

				srt.refreshTableViewer(true);
			}
			SearchRunnableWithProgress.monitorWorked((int)Math.ceil(TOTAL_WORK / pathways.size()));
		}
		if(srs.getResults().size() == 0) throw new NothingFoundException();
	}
	
	static class SearchException extends Exception {
		private static final long serialVersionUID = 1L;

		SearchException(String msg) {
			super(msg);
		}
	}
	
	static class NothingFoundException extends SearchException {
		private static final long serialVersionUID = 1L;

		NothingFoundException() {
			super(MSG_NOTHING_FOUND);
		}
	}
	
	static class NoGdbException extends SearchException {
		private static final long serialVersionUID = 1L;

		NoGdbException() {
			super(MSG_NOT_IN_GDB);
		}
	}
}
