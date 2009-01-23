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
package org.pathvisio.plugins.project2008;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * In this class the file is read that contains information about genId's and GOId's. There are 
 * two sets created:
 * - In the first set, the geneId's are the keys and the GOId's are the values that belong to 
 *   the keys.
 * - In the second set, the GOId's are the keys and the GeneId's are the values that belong to 
 *   the keys.
 *   It is also possible to return a list of GOId's for a given GeneId, or a list of GeneId's
 *   for a given GOId. 
 */

public class GenesGOid {

	// args[0] should refer to 2D mart_export.txt
	public static void main(String[] args)
	{		
		// create array to store output from filereader
		Map<String,Set<String>> goByGene = goByGene(new File (args[0]));
		
		// In this method the map "goByGene" is created. In this map, the gene-Id's are the keys 
		// and the GO-Id's are the values.
		
		// In this method the map "geneByGO" is created. In this map, the GO-Id's are the keys 
		// and the gene-Id's are the values.	
		Map<String,Set<String>> geneByGO=geneByGO(goByGene);
		
		// Example GO-Id
		String goId="GO:0008020";
		//In this method all Gene-Id's for the sample GO-ID are returned in a List*/
		Set<String> ensIdsforGOId = ensIdsforGOId(goId,geneByGO);
		System.out.println("Ensembl ID's for " + goId + " are: " + ensIdsforGOId);
	}
	
	public static Map<String,Set<String>> goByGene (File path)
	{
		// line string
		String line; 
		// the array to return
		String[] currentRow;
		// start FileReader
		Map<String,Set<String>> goByGenemap = new HashMap<String,Set<String>>();
		try {
			 FileReader fr = new FileReader(path);
		     BufferedReader br = new BufferedReader(fr);
		     // continue reading lines until EOF is reached
		     while((line = br.readLine()) != null){
		    	 // TSV so split lines using \t
		    	currentRow=line.split("\t");
		    	if(currentRow.length > 1){			 		
		    		if(goByGenemap.containsKey(currentRow[0])){
		    			Set<String> gOIds;
		    			gOIds=goByGenemap.get(currentRow[0]);
		    			gOIds.add(currentRow[1]);
		    			goByGenemap.put(currentRow[0],gOIds);
		    		 }
		    		else
		    		{
		    			Set<String> gOIds = new HashSet<String>();
		    			gOIds.add(currentRow[1]);
		    			goByGenemap.put(currentRow[0],gOIds);
		    		}
		    	}
	     
		    }		
		    fr.close();
		}		     
		catch(Exception e) {
			System.out.println("Exception: " + e);
		}
		return goByGenemap;
	}
	
	
	
 	/**In this method all GO-Id's for the given gene-ID are returned in a List*/ 
	public static Set <String> goIdsforEnsId(String geneId, Map<String, Set<String>> goByGene)
	{
		Set<String> goIdsforEnsIdlist = new HashSet<String>();
		if(goByGene.get(geneId) != null)
		{
			goIdsforEnsIdlist.addAll(goByGene.get(geneId));
		}
		return goIdsforEnsIdlist;
	}
	
    /**In this method the map "geneByGO" is created. In this map, the GO-Id's are the keys and the gene-Id's are the values.*/	
	  public static Map<String,Set<String>> geneByGO(Map<String, Set<String>> goByGene)
	  {
		  /** Because there already is a map "goByGenes", this map can be used to create the map 
		   * "genesByGO". In the first for-loop, for each gene-ID a list with all GO-Id's for that 
		   * gene-ID is asked. In the second for-loop, each GO-Id is called a key and each gene-ID 
		   * is called a value. 
		   * If this GO-Id is not already a key in the new map, a new list is
		   * created with the value (gene-Id) for this key (GO-Id). This key-value combination is
		   * put into the map. 
		   * If this GO-Id is already a key in the new map, the value (gene-ID) that matches with 
		   * this key (GO-Id) is added to the ohter values that are added to the map.
		   */
		  Map<String,Set<String>> geneByGo = new HashMap<String,Set<String>>();
		  for (String gene : goByGene.keySet())
		  {
			  Set<String> goList = goIdsforEnsId(gene,goByGene);
			  for (String go : goList)
			  {
				  String key = go;
				  String value = gene;
				  if (!geneByGo.containsKey(key))
				  {
					  Set<String> valueList = new HashSet<String>();
					  valueList.add(value);
					  geneByGo.put(key,valueList);
				  }
				  else
				  {
					  Set<String> valueList = geneByGo.get(key);
					  valueList.add(value);
				  }
			  }
		  }
		  return geneByGo;
	  }
	 
	/**In this method all Gene-Id's for the given GO-ID are returned in a List*/ 
	public static Set<String> ensIdsforGOId(String goId, Map<String, Set<String>> geneByGO)
	{
		Set<String> list = new HashSet<String>();
		if(geneByGO.get(goId) != null)
		{
			list.addAll(geneByGO.get(goId));
		}
		return list;
	}
}


	
