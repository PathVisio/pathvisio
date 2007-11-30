package org.pathvisio.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.pathvisio.data.DataSource;
import org.pathvisio.debug.Logger;


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

	class EUGenePathway {
		Logger log = Logger.log;
		Pathway pathway;

		String system; //The annotation system

		ArrayList<String> ids;
		ArrayList<String> codes; 

		public EUGenePathway(Pathway p)  {
			pathway = p;
			read();
		}

		void writeToEUGene(File file) throws FileNotFoundException {
			String euGeneSystem = null;
			StringBuilder geneString = new StringBuilder();
			StringBuilder missedGenes = new StringBuilder();
			euGeneSystem = getEUGeneSystem();

			for(int i = 0; i < ids.size(); i++) {
				String code = codes.get(i);
				String id = ids.get(i);
				if(code.equals(system)) { //Check if gene is of most occuring system
					geneString.append(id + "\n");
				} else {
					missedGenes.append(id + "|" + code + "; ");
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
			ids = new ArrayList<String>();
			codes = new ArrayList<String>();
			HashMap<String, Integer> codeCount = new HashMap<String, Integer>();

			for(PathwayElement elm : pathway.getDataObjects()) {
				if(elm.getObjectType() != ObjectType.DATANODE) {
					continue; //Skip non-datanodes
				}
				String id = elm.getGeneID();
				String code = elm.getSystemCode();
				if(id == null || code == null || id.equals("") || code.equals("")) { 
					continue; //Skip datanodes with incomplete annotation
				}
				ids.add(id); 
				codes.add(code);

				//Increase code count for this code
				if(codeCount.containsKey(code)) codeCount.put(code, codeCount.get(code) + 1);
				else codeCount.put(code, 1);
			}

			//Get most occuring systemcode
			String maxCode = null;
			for(String code : codeCount.keySet()) {
				if(maxCode == null || codeCount.get(code) > codeCount.get(maxCode)) {
					maxCode = code;
				}
			}
			system = maxCode;

			if(codeCount.keySet().size() > 1) {
				log.warn("\tThis pathway contains genes with different SystemCodes; '" +
						maxCode + "' has the highest occurence and is therefore chosen as PATHWAY_MARKER" +
						" for the EUGene file\n\t Other SystemCodes found and their occurences: "
						+ codeCount);
			}

		}

		String getEUGeneSystem() {
			if(systemMappings.containsKey(system)) {
				return systemMappings.get(system);
			} else {
				return DataSource.getBySystemCode(system).getFullName();
			}
		}
	}

	static Map<DataSource, String> systemMappings;
	static final String[] euGeneSystems = new String[]
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
	static final DataSource[] genMappSystems = new DataSource[]
	                                            {
		DataSource.ENSEMBL,
		DataSource.SWISSPROT,
		DataSource.ENTREZ_GENE,
		DataSource.UNIGENE,
		DataSource.AFFY,
		DataSource.AGILENT,
		DataSource.HUGO,
		DataSource.PDB,
		DataSource.SGD
	                                            };
	
	static
	{
		systemMappings = new HashMap<DataSource, String>();
		for(int i = 0; i < euGeneSystems.length; i++) 
		{
			systemMappings.put(genMappSystems[i], euGeneSystems[i]);
		}
	}
}