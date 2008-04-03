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
		//Inlezen van de file
		String s; 
		List<String[]> arrayGOgenes = new ArrayList<String[]>();
		try {
			 FileReader fr = new FileReader(args[2]);
		      BufferedReader br = new BufferedReader(fr);

		     while((s = br.readLine()) != null){
		    	 arrayGOgenes.add(s.split("\t"));
		      }
		        //System.out.println(s);
		
		
		      fr.close();
		    }
		    catch(Exception e) {
		      System.out.println("Exception: " + e);
				}
	
    //Methode aanroepen waarin de map goByGene wordt gemaakt	
	Map<String,List<String>> goByGene=goByGene(arrayGOgenes);
	//Uitprinten van de Map "goByGene"
	for (String key : goByGene.keySet()){
		//System.out.println (key);
		List<String> values = goByGene.get(key);
		
		for (String value : values)	{
			//System.out.println ("  " + value);
		}
	}
	
	
	String Ensid="ENSRNOG00000005016";
	//Methode aanroepen waar voor het ensid alle go id's worden gereturned. 
	List<String> gOIdforEnsId = goIdsforEnsId(Ensid,goByGene);
	
	
	
	//Methode aanroepen waarin de map geneByGo wordt gemaakt	
	Map<String,List<String>> geneByGO=geneByGO(goByGene);
	//Uitprinten van de Map "geneByGo"
	for (String key : geneByGO.keySet()){
		System.out.println (key);
		List<String> values = geneByGO.get(key);
		for (String value : values)	{
			System.out.println ("  " + value);
		}
	
	}
	
	
	String goId="GO:0008020";
	//Methode aanroepen waar voor het goid alle ensid's worden gereturned. 
	List<String> ensIdsforGOId = ensIdsforGOId(goId,geneByGO);
	//System.out.println(ensIdsforGOId.get(0));
	
	
	}
	
	//In deze methode wordt een map gemaakt waarin de de ensembleid's de keys zijn en de go id's de values bij de keys
	public static Map<String,List<String>> goByGene(List<String[]> arrayGOgenes){
		//i begint bij 1 en niet bij 0 omdat de string telkens wordt vergeleken met de string ervoor.
		List<String> ensemblGeneIds = new ArrayList<String>();
		for (int i = 1;i<(arrayGOgenes.size());i++){
			String currentGene = arrayGOgenes.get(i)[0];
			String previousGene = arrayGOgenes.get(i-1)[0];
			if (!currentGene.equals(previousGene)){
				ensemblGeneIds.add(currentGene);}
			}
				
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		List<String> gOIds = new ArrayList<String>();
				
				
		for (int i = 1;i<(arrayGOgenes.size());i++){
			if (arrayGOgenes.get(i).length > 2){
				String currentGene = arrayGOgenes.get(i)[0];
				String currentGOId = arrayGOgenes.get(i)[2];
				String previousGene = arrayGOgenes.get(i-1)[0];
				if (!currentGene.equals(previousGene)){
					map.put(previousGene,gOIds);
					gOIds = new ArrayList<String>();
					gOIds.add(currentGOId);}
				else {gOIds.add(currentGOId);}
			}
		}
		return map;
		}
    
	
	//In deze methode wordt een lijst gereturned waarin voor een gegeven ensId alle GoId's staan. 
	public static List goIdsforEnsId(String Ensid, Map<String, List<String>> goByGene){
		List<String> list = new ArrayList<String>();
		list.addAll(goByGene.get(Ensid));
		return list;
		}

	
	
	
	
	//In deze methode wordt een map gemaakt waarin de de ensembleid's de keys zijn en de go id's de values bij de keys
	  public static Map<String,List<String>> geneByGO(Map<String, List<String>> goByGene){
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
	
	
	//In deze methode wordt een lijst gereturned waarin voor een gegeven GO ID alle EnsId's staan. 
	public static List ensIdsforGOId(String goId, Map<String, List<String>> geneByGO){
		List<String> list = new ArrayList<String>();
		list.addAll(geneByGO.get(goId));
		return list;
		}
}


	
