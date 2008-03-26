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
import java.io.File;

import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

import java.util.*;

public class GeneCounter {

	/**
	 * @param args
	 * @throws ConverterException 
	 * @throws DataException 
	 */
	public static void main(String[] args) throws ConverterException, DataException {
		
		// Totale aantal bekende genen in Ensemble Database (Ensemble.org)
		int numberOfGenesEN = 100;
		// Set aanmaken voor totale aantal gebruikte genen in WikiPathways
		Set<String> totalS=new HashSet<String>();
				
		
		// namen van de Pathways maken. Dit moet uiteindelijk automatisch gedaan worden.
		List<String> namePathway = new ArrayList<String>();
		namePathway.add("Rn_ACE-Inhibitor_pathway_PharmGKB.gpml");
		namePathway.add("Rn_Acetylcholine_Synthesis.gpml");
		namePathway.add("Rn_Adipogenesis.gpml");
		namePathway.add("Rn_Alanine_and_aspartate_metabolism_KEGG.gpml");
		namePathway.add("Rn_Alpha6-Beta4-Integrin_NetPath_1.gpml");
		namePathway.add("Rn_Androgen-Receptor_NetPath_2.gpml");
		namePathway.add("Rn_Apoptosis.gpml");
		namePathway.add("Rn_B_Cell_Receptor_NetPath_12.gpml");
		namePathway.add("Rn_Biogenic_Amine_Synthesis.gpml");
		namePathway.add("Rn_Biosynthesis_of_Aldosterone_and_Cortisol.gpml");
		System.out.println(namePathway);
		
		/* Hier moet iets van een for-loop komen om de gegevens
		 * uit de verschillende Pathways te halen.
		 * */
		int i;
		for (i=0;i<namePathway.size();i++){
		
			
			String fileName=namePathway.get(i);
			System.out.println(fileName);
			
			//s berekenen
			Set<String> setOfRefPW=getRefPW(fileName);
		
			/*
			 * Hier moet de functie worden aangeroepen die de referenties omzet naar EN.
			 */
			SimpleGdb db=new SimpleGdb("D:\\My Documents\\Tue\\BIGCAT",new DataDerby(),0);
			//db=getCrossRefs(L:24903);
			
			
			//s in de totale set zetten
			totalS.addAll(setOfRefPW);
		}
		/* Einde for loop
		 */
		
		// Output: Grootte van de totale set
		System.out.println(totalS);
		System.out.println(totalS.size());
		double usedgenes=totalS.size();
		usedgenes=usedgenes/numberOfGenesEN;
		System.out.println("Percentage of used genes at http://www.wikipathways.org = "+usedgenes+"%");
		//
}
	
	/*
	 * Deze functie geeft een set van alle referenties van een Pathway.
	 */
	public static Set<String> getRefPW(String namePathway) throws ConverterException{
		
		Set<String> s=new HashSet<String>();
		
		File f = new File("D:\\My Documents\\Tue\\BIGCAT\\Rat\\"+namePathway);
		System.out.println("file = "+f);
		
		Pathway p = new Pathway();
		p.readFromXml(f, true);
				
		List<PathwayElement> pelts = p.getDataObjects();
		
		for (PathwayElement v:pelts){
			
			int type;
			type=v.getObjectType();
			
			if (type ==1){
				Xref reference;
				reference=v.getXref();
				String name=reference.getName();
				//System.out.println(name);
				s.add(name);
				
			
			}
		}
		
		s.remove("null:");
		System.out.println(s);
		System.out.println(s.size());
		
		return s;
				
		
	}
	
	
	
}
