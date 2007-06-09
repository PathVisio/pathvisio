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
package org.pathvisio.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
   Handles all data sources recognized by PathVisio (e.g. Ensembl, Unigene, Entrez).
*/   
public class DataSources 
{
	/**
	   GenMAPP System Codes
	*/
    public final static String[] systemCodes = 
	{ 
	"D", "F", "G", "I", "L", "M",
	"Q", "R", "S", "T", "U",
	"W", "Z", "X", "En", "Em", 
	"H", "Om", "Pd", "Pf", 
	"Z", "Hs", "H", "C",
	"Ag", "Il", "Sn", "Ec",
	"Ca", "Ce", "Cp", "Nw",
	"Ck", "Ch",
	"O", ""
	};

	/**
	   Full names of data sources, corresponding to the System Codes in {@link systemCodes}
	*/
    public final static String[] dataSources = 
	{
	"SGD", "FlyBase", "GenBank", "InterPro" ,"Entrez Gene", "MGI",
	"RefSeq", "RGD", "SwissProt", "GeneOntology", "UniGene",
	"WormBase", "ZFIN", "Affy", "Ensembl", "EMBL", 
	"HUGO", "OMIM", "PDB", "Pfam",
	"ZFIN", "HsGene", "HUGO", "Cint",
	"Agilent", "Illumina", "SNP", "Ecoli",
	"CAS", "ChEBI", "PubChem", "NuGOwiki",
	"KEGG Compound", "HMDB",
	"Other", ""
	};
    
	/**
	 * {@link HashMap} containing mappings from system name (as used in Gpml) to system code
	 */
	public static final HashMap<String,String> sysName2Code = initSysName2Code();

	/**
	 * {@link HashMap} containing mappings from system code to system name (as used in Gpml)
	 */
	public static final HashMap<String,String> sysCode2Name = initSysCode2Name();

	/**
	 * Initializes the {@link HashMap} containing the mappings between system name (as used in gpml)
	 * and system code
	 */
	private static HashMap<String, String> initSysName2Code()
	{
		HashMap<String, String> sn2c = new HashMap<String,String>();
		for(int i = 0; i < dataSources.length; i++)
			sn2c.put(dataSources[i], systemCodes[i]);
		return sn2c;
	}
	
	/**
	 * Initializes the {@link HashMap} containing the mappings between system code and 
	 * system name (as used in Gpml)
	 */
	private static HashMap<String, String> initSysCode2Name()
	{
		HashMap<String, String> sn2c = new HashMap<String,String>();
		for(int i = 0; i < systemCodes.length; i++)
			sn2c.put(systemCodes[i], dataSources[i]);
		return sn2c;
	}

	/**
	   System names converted to arraylist for easy index lookup
	*/
	public final static List<String> lDataSources = Arrays.asList(dataSources);
}
