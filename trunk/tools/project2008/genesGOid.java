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
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class genesGOid {

	public static void main(String[] args){
		//Load the file
		String s; 
		List<String[]> arrayGOgenes = new ArrayList<String[]>();
		try {
			 FileReader fr = new FileReader(args[2]);
		      BufferedReader br = new BufferedReader(fr);
		     while((s = br.readLine()) != null){
		    	 arrayGOgenes.add(s.split("\t"));
		      }
		      fr.close();
		    }
		    catch(Exception e) {
		      System.out.println("Exception: " + e);
			}
	
    /*In this method the map "goByGene" is created. In this map, the gene-Id's are the keys and the GO-Id's are the values.*/	
	Map<String,List<String>> goByGene=goByGene(arrayGOgenes);
	//The map "goByGene" can be printed to the screen
	for (String key : goByGene.keySet()){
		System.out.println (key);
		List<String> values = goByGene.get(key);
		
		for (String value : values)	{
			System.out.println ("  " + value);
		}
	}
	
	//Example gene-Id
	String geneId="ENSRNOG00000005016";
	/*In this method all GO-Id's for the given gene-ID are returned in a List*/ 
	List<String> gOIdforEnsId = goIdsforEnsId(geneId,goByGene);
	
    /*In this method the map "geneByGO" is created. In this map, the GO-Id's are the keys and the gene-Id's are the values.*/	
	Map<String,List<String>> geneByGO=geneByGO(goByGene);
	//The map "goByGene" can also be printed to the screen
	for (String key : geneByGO.keySet()){
		//System.out.println (key);
		List<String> values = geneByGO.get(key);
		for (String value : values)	{
		//	System.out.println ("  " + value);
		}
	}
	//Example GO-Id
	String goId="GO:0008020";
	/*In this method all Gene-Id's for the given GO-ID are returned in a List*/ 
	List<String> ensIdsforGOId = ensIdsforGOId(goId,geneByGO);
	
	}
	
	
    /*In this method the map "goByGene" is created. In this map, the gene-Id's are the keys and the GO-Id's are the values.*/	
	public static Map<String,List<String>> goByGene(List<String[]> arrayGOgenes){
		/* In the array "arrayGogenes" each gene-Id is compared to the gene-Id above the current gene. 
		 * If the gene-Id does not equal the gene-Id from the previous gene,
		 * The GO-Id that matches this gene-ID is put in a list. The for loop goes on and as long as
		 * the current gene is equal to the previous gene, the GO-Id is added to the list.
		 * When the next current gene is no longer equal to the previous gene, the GO-Id list is added
		 * to the map. The key is than the previous gene and the value is the GO-Id list.   
		 */
		Map<String,List<String>> goByGenemap = new HashMap<String,List<String>>();
		List<String> gOIds = new ArrayList<String>();
		for (int i = 1;i<(arrayGOgenes.size());i++){
			if (arrayGOgenes.get(i).length > 2){
				String currentGene = arrayGOgenes.get(i)[0];
				String currentGOId = arrayGOgenes.get(i)[2];
				String previousGene = arrayGOgenes.get(i-1)[0];
				if (!currentGene.equals(previousGene)){
					goByGenemap.put(previousGene,gOIds);
					gOIds = new ArrayList<String>();
					gOIds.add(currentGOId);
				}
				else {gOIds.add(currentGOId);
				}
			}
		}
		return goByGenemap;
	}
    
	
	/*In this method all GO-Id's for the given gene-ID are returned in a List*/ 
	public static List goIdsforEnsId(String geneId, Map<String, List<String>> goByGene){
		List<String> goIdsforEnsIdlist = new ArrayList<String>();
		goIdsforEnsIdlist.addAll(goByGene.get(geneId));
		return goIdsforEnsIdlist;
		}
	
    /*In this method the map "geneByGO" is created. In this map, the GO-Id's are the keys and the gene-Id's are the values.*/	
	  public static Map<String,List<String>> geneByGO(Map<String, List<String>> goByGene){
		  /* Because there already is a map "goByGenes", this map can be used to create the map 
		   * "genesByGO". In the first for-loop, for each gene-ID a list with all GO-Id's for that 
		   * gene-ID is asked. In the second for-loop, each GO-Id is called a key and each gene-ID 
		   * is called a value. 
		   * If this GO-Id is not already a key in the new map, a new list is
		   * created with the value (gene-Id) for this key (GO-Id). This key-value combination is
		   * put into the map. 
		   * If this GO-Id is already a key in the new map, the value (gene-ID) that matches with 
		   * this key (GO-Id) is added to the ohter values that are added to the map.
		   */
		  Map<String,List<String>> geneByGo = new HashMap<String,List<String>>();
		  for (String gene : goByGene.keySet()){
			  List<String> goList = goIdsforEnsId(gene,goByGene);
			  for (String go : goList){
				  String key = go;
				  String value = gene;
				  if (!geneByGo.containsKey(key)){
					  List<String> valueList = new ArrayList();
					  valueList.add(value);
					  geneByGo.put(key,valueList);
				  }
				  else{
					  List<String> valueList = geneByGo.get(key);
					  valueList.add(value);
				  }
			  }
		  }
		  return geneByGo;
	  }
	
	/*In this method all Gene-Id's for the given GO-ID are returned in a List*/ 
	public static List ensIdsforGOId(String goId, Map<String, List<String>> geneByGO){
		List<String> list = new ArrayList<String>();
		list.addAll(geneByGO.get(goId));
		return list;
	}
}


	
