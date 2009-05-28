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
package org.pathvisio.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.pathvisio.debug.Logger;

/**
 * Exports to pathway format understood by the EuGene
 * pathway statistics program.
 * This format is basically a list of genes in a flat text file
 * preceded by 3 header lines.
 * 
 * EuGene supports several id systems but has its own naming
 * for them, this exporter also handles the translation.
 */
public class EUGeneExporter implements PathwayExporter 
{
	public String[] getExtensions() {
		return new String[] { "pwf" };
	}

	public String getName() {
		return "Eu.Gene pathway";
	}

	public void doExport(File file, Pathway pathway) throws ConverterException {
		EUGenePathway eugPathway = new EUGenePathway(pathway);
		try {
			eugPathway.writeToEUGene(file);
		} catch(Exception e) {
			throw new ConverterException(e);
		}
	}

	private static class EUGenePathway {
		Logger log = Logger.log;
		Pathway pathway;

		DataSource system; //The annotation system

		List<Xref> refs;

		public EUGenePathway(Pathway p)  {
			pathway = p;
			read();
		}

		void writeToEUGene(File file) throws FileNotFoundException {
			String euGeneSystem = null;
			StringBuilder geneString = new StringBuilder();
			StringBuilder missedGenes = new StringBuilder();
			euGeneSystem = getEUGeneSystem();

			for(Xref ref : refs)
			{
				DataSource ds = ref.getDataSource();
				String id = ref.getId();
				if(ds == system)
				{ //Check if gene is of most occuring system
					geneString.append(id + "\n");
				}
				else
				{
					missedGenes.append(id + "|" + ds.getSystemCode() + "; ");
					log.error("id '" + id + "' differs from pathway annotation system");
				}
			}

			//Write the file
			PrintStream out = null;
			out = new PrintStream(file);

			//Print the data
			out.println("//PATHWAY_NAME = " + pathway.getMappInfo().getMapInfoName());
			out.println("//PATHWAY_SOURCE = GenMAPP");
			out.println("//PATHWAY_MARKER = " + euGeneSystem);
			if(missedGenes.length() > 0) out.println("//LOST_DURING_CONVERSION: " + missedGenes );
			out.print(geneString);

			out.close();
		}

		void read() { 
			refs = new ArrayList<Xref>();
			Map<DataSource, Integer> codeCount = new HashMap<DataSource, Integer>();

			for(PathwayElement elm : pathway.getDataObjects()) {
				if(elm.getObjectType() != ObjectType.DATANODE) {
					continue; //Skip non-datanodes
				}
				Xref ref = elm.getXref();
				DataSource ds = ref.getDataSource();
				if(ref == null || ref.getId().equals("") || ref.getDataSource() == null) 
				{ 
					continue; //Skip datanodes with incomplete annotation
				}
				refs.add (ref);

				//Increase code count for this code
				if(codeCount.containsKey(ref.getDataSource())) 
					codeCount.put(ds, codeCount.get(ds) + 1);
				else codeCount.put(ds, 1);
			}

			//Get most occuring systemcode
			DataSource maxCode = null;
			for(DataSource ds : codeCount.keySet()) 
			{
				if(maxCode == null || codeCount.get(ds) > codeCount.get(maxCode)) 
				{
					maxCode = ds;
				}
			}
			system = maxCode;

			if(system == null) { //May occur when no identifiers available
				system = BioDataSource.ENSEMBL;
			}
			
			if(codeCount.keySet().size() > 1) {
				log.warn("\tThis pathway contains genes with different SystemCodes; '" +
						maxCode + "' has the highest occurence and is therefore chosen as PATHWAY_MARKER" +
						" for the EUGene file\n\t Other SystemCodes found and their occurences: "
						+ codeCount);
			}

		}

		String getEUGeneSystem() {
			if(systemMappings.containsKey(system)) 
			{
				return systemMappings.get(system);
			} 
			else 
			{
				return system.getFullName();
			}
		}
	}

	private static Map<DataSource, String> systemMappings;
	private static final String[] EU_GENE_SYSTEMS = new String[]
	                                           {
		"ENSEMBL_GENE_ID",
		"UNIPROT", 
		"ENTREZ", 
		"UNIGENE", 
		"AFFYMETRIX", 
		"AGILENT",
		"HGNC",
		"PDB_ID", 
		"SGD_ID" 
	                                           };
	private static final DataSource[] GENMAPP_SYSTEMS = new DataSource[]
	                                            {
		BioDataSource.ENSEMBL,
		BioDataSource.UNIPROT,
		BioDataSource.ENTREZ_GENE,
		BioDataSource.UNIGENE,
		BioDataSource.AFFY,
		BioDataSource.AGILENT,
		BioDataSource.HUGO,
		BioDataSource.PDB,
		BioDataSource.SGD
	                                            };
	
	static
	{
		systemMappings = new HashMap<DataSource, String>();
		for(int i = 0; i < EU_GENE_SYSTEMS.length; i++) 
		{
			systemMappings.put(GENMAPP_SYSTEMS[i], EU_GENE_SYSTEMS[i]);
		}
	}
}