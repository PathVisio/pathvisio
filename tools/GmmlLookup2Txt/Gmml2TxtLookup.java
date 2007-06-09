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
import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import util.FileUtils;
import util.XmlUtils.PathwayParser;
import util.XmlUtils.PathwayParser.Gene;
import data.GmmlGdb;
import data.GmmlGdb.IdCodePair;

/**
 * Creates a txt file from a Gpml pathway and looks up cross references in the gene database
 */
public class Gmml2TxtLookup {
	//The codes of the cross-reference types you want to lookup
	static final String[] lookupCodes = { "X" };
	
	//The location of the gene database
	static final String gdbLocation = "/home/thomas/study/afstuderen/visio_data/gene database/Hs_ensembl/ensembl_homo_sapiens_38_36.properties";

	//The directory of pathways for which you want to lookup the cross-references
	static final String pathwayDir = "/home/thomas/study/afstuderen/visio_data/pathways/small";
	
	
	static String headers = "Name\tID\tCode\t";
	static 
	{
		for(String code : lookupCodes) headers += code + "\t";
		headers += "\n";
	}
	
	public static void main(String[] args) {	
		try {
			doConversion();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void doConversion() throws Exception {
		List<File> pwFiles = FileUtils.getFiles(new File(pathwayDir), "xml", true);

		//Connect to the gene database
		GmmlGdb.connect(new File(gdbLocation));
		
		HashMap<String, StringBuilder> refResult = new HashMap<String, StringBuilder>();
		
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		for(File f : pwFiles) {
			System.out.println("parsing Gpml file " + f.getName());
			
			//Parse the Gpml file
			PathwayParser pwp = new PathwayParser(xmlReader);
			try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { 
				GmmlVision.log.error("Couldn't read " + f, e); 
				continue; 
			}
			
			FileWriter out = new FileWriter(new File(f.getName() + ".txt"));
			out.append(headers);
			
			System.out.println("Looking up cross references");
			for(Gene g : pwp.getGenes()) {
				System.out.println("\tFor gene " + g.getId());
				// Clear previous cross ref results
				refResult.clear();
				for(String code : lookupCodes) refResult.put(code, new StringBuilder());
				
				// Get cross refs for every gene
				List<IdCodePair> refs = GmmlGdb.getCrossRefs(new IdCodePair(g.getId(), g.getCode()));
				for(IdCodePair ref : refs) {
					StringBuilder s = refResult.get(ref.getCode());
					if(s != null) s.append(ref.getId() + ", ");
				}
				//Print to file
				out.append(g.getSymbol() + "\t" + g.getId() + "\t" + g.getCode() + "\t");
				for(String code : lookupCodes) {
					StringBuilder s = refResult.get(code);
					int comma = s.lastIndexOf(",");
					out.append(comma > -1 ? s.substring(0, comma) : s + "\t");
				}
				out.append("\n");
			}
			out.close();
		}
		
		System.out.println("Finished!");
	}
}
