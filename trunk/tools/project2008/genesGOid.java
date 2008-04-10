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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class genesGOid {

	// args[0] should refer to 2D mart_export.txt
	public static void main(String[] args)
	{		
		// create array to store output from filereader
		List<String[]> arrayGOgenes = readDatabase(args[0]);
		
		/**In this method the map "goByGene" is created. 
		 * In this map, the gene-Id's are the keys and the GO-Id's are the values.*/
		Map<String,Set<String>> goByGene=goByGene(arrayGOgenes);
		
		/**In this method the map "geneByGO" is created. 
		 * In this map, the GO-Id's are the keys and the gene-Id's are the values.*/	
		Map<String,Set<String>> geneByGO=geneByGO(goByGene);
		
		// Example GO-Id
		String goId="GO:0008020";
		//In this method all Gene-Id's for the sample GO-ID are returned in a List*/
		Set<String> ensIdsforGOId = ensIdsforGOId(goId,geneByGO);
		System.out.println("Ensembl ID's for " + goId + " are: " + ensIdsforGOId);
	}
	
	public static List<String[]> readDatabase(String path){
		// line string
		String line; 
		// the array to return
		List<String[]> arrayGOgenes = new ArrayList<String[]>();
		// start FileReader
		try {
			 FileReader fr = new FileReader(path);
		     BufferedReader br = new BufferedReader(fr);
		     // continue readering lines until EOF is reached
		     while((line = br.readLine()) != null){
		    	 // TSV so split lines using \t
		    	 arrayGOgenes.add(line.split("\t"));
		      }
		      fr.close();
		    }
		    catch(Exception e) {
		      System.out.println("Exception: " + e);
			}
		return arrayGOgenes;
	}
	
	
	
    /** In this method the map "goByGene" is created. In this map, the gene-Id's are the keys and the GO-Id's are the values.*/	
	public static Map<String,Set<String>> goByGene(List<String[]> arrayGOgenes)
	{
		/** In the array "arrayGogenes" each gene-Id is compared to the gene-Id above the current gene. 
		 * If the gene-Id does not equal the gene-Id from the previous gene,
		 * The GO-Id that matches this gene-ID is put in a list. The for loop goes on and as long as
		 * the current gene is equal to the previous gene, the GO-Id is added to the list.
		 * When the next current gene is no longer equal to the previous gene, the GO-Id list is added
		 * to the map. The key is than the previous gene and the value is the GO-Id list.   
		 */
		Map<String,Set<String>> goByGenemap = new HashMap<String,Set<String>>();
		Set<String> gOIds = new HashSet<String>();
		for (int i = 1;i<arrayGOgenes.size();i++)
		{
			if (arrayGOgenes.get(i).length > 1)
			{
				String currentGene = arrayGOgenes.get(i)[0];
				String currentGOId = arrayGOgenes.get(i)[1];
				String previousGene = arrayGOgenes.get(i-1)[0];
				if (!currentGene.equals(previousGene))
				{
					goByGenemap.put(previousGene,gOIds);
					gOIds = new HashSet<String>();
					gOIds.add(currentGOId);
				}
				else {gOIds.add(currentGOId);
				}
			}
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


	
