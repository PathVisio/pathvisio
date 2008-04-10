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
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

import java.util.*;

/**
 * In the Gene counter....
 */

public class GeneCounter {


	/**
	 * @param args
	 * @throws ConverterException 
	 * @throws DataException 
	 * @throws ConverterException 
	 * @throws DataException 
	 */
	public static void main(String[] args) throws DataException, ConverterException{
		/** 
		 * Check if the String[] args is given, and make Files containing the directories to
		 * the pathways and databases. 
		 */ 

		String dbDir = null;
		File pwDir = null;
		
		try {
			dbDir = new String(args[0]);
			pwDir = new File(args[1]);
			
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("String[] args not given!");
			System.exit(0);
		}
		
		/**
		 * The method 'getSets' returns a list...... 
		 */
		List<Set> refPWarray=getSets(dbDir,pwDir);
		
		
		
		int numberOfGenesEN=getNumberOFGenesEN();
		System.out.println(refPWarray.get(refPWarray.size()-1).size());
		int usedgenes=refPWarray.get(refPWarray.size()-1).size();
		double percentageUsedgenes=(double)usedgenes/(double)numberOfGenesEN*100.0;
		
		
		refPWarray.remove(refPWarray.size()-1);
		Double[][] overlap=getPercentage(refPWarray);
		
		System.out.println("Percentage of used genes at http://www.wikipathways.org = "+percentageUsedgenes+"%");

		
		
		
}
	/**
	 * In the method 'getSets' a List is created that contains a set with all the Xref's.
	 * The properties you have to enter are:
	 * 'dbDir' (the direction that contains the databases) and 
	 * 'pwDir' (the direction to the file that contains the pathways).
	 * 
	 * First the method "getFileListing" is executed. In this method all files that are 
	 * stored in the list of files is created, so that each file can easily be loaded.
	 * In the for-loop the information from all different pathways is loaded. 
	 * In the set 'totalS', all different sets are added. So one big set is formed with
	 * all Xref's. This set is then added to an array, so that an array can be returned 
	 * that contains all different Xref's. With this array the overlap can easily be 
	 * determined.
	 */ 

	public static List<Set> getSets(String dbDir,File pwDir) throws DataException, ConverterException{
		List<File> filenames = FileUtils.getFileListing(pwDir, ".gpml");
		Set<Xref> totalS=new HashSet<Xref>();
		SimpleGdb db=new SimpleGdb(dbDir,new DataDerby(),0);
		List<Set> refPWarray = new ArrayList<Set>();
		for (int i=0;i<filenames.size();i++){
			File fileName=filenames.get(i);
			Set<Xref> setOfRefPW=getRefPW(fileName,db);
			refPWarray.add(setOfRefPW);
			totalS.addAll(setOfRefPW);
		}
		refPWarray.add(totalS);
		return refPWarray;
	}
	
		
	/**
	 * In this method the total amount of genes that is known so far can be set. 
	 * This number is returned.
	 */
	public static int getNumberOFGenesEN(){
		int numberOfGenesEN = 17738;
		return numberOfGenesEN;
	}

	/**
	 * 
	 * 
	 */
public static double getUsedGenes(String dbDir,File pwDir) throws DataException, ConverterException{
		
		// Total amount of known genes in the Ensembl Database (http://www.ensembl.org).
		int numberOfGenesEN = getNumberOFGenesEN();
		
		List<Set> refPWarray=getSets(dbDir,pwDir);
		int usedgenes=refPWarray.get(refPWarray.size()-1).size();
		double percentageUsedgenes=(double)usedgenes/(double)numberOfGenesEN*100.0;
		//usedgenes=usedgenes/numberOfGenesEN*100;
		System.out.println("Percentage of used genes at http://www.wikipathways.org = "+percentageUsedgenes+"%");
		return percentageUsedgenes;
	}
	
	
	public static Double[][] getOverlap(String dbDir,File pwDir) throws DataException, ConverterException{
			
		List<Set> refPWarray=getSets(dbDir,pwDir);
		
		Double[][] overlap=getPercentage(refPWarray);
		
		
		return overlap;
	}

	
	//In this method a set is created that contains all the references in a Pathway.
	public static Set<Xref> getRefPW(File filename,SimpleGdb db) throws ConverterException{
		
		//A new set is created where the Xrefs can be stored. 
		Set<Xref> s=new HashSet<Xref>();
		//A new pathway is created.
		Pathway p = new Pathway();
		//For this pathway the information is loaded. 
		p.readFromXml(filename, true);
				
		//A list is formed that contains the elements stored in the pathway.
		List<PathwayElement> pelts = p.getDataObjects();
		
		//In this for-loop each element of the pathway that represents a Xref is stored in a set. 
		for (PathwayElement v:pelts){
			
			int type;
			//For each element in the patyway, the object type is returned.
			type=v.getObjectType();
			
			//Only if the object is a DATANODE, the reference must be stored in the list. 
			if (type ==1){
				Xref reference;
				reference=v.getXref();
				//Each reference is translated to a reference in the same databank, here: ENSEMBLE
				List<Xref> cRef=db.getCrossRefs(reference,DataSource.ENSEMBL);
				//All references are added to a set.				
				s.addAll(cRef);
			}
		}
		
		//'s' contains all the references as in the databank ENSEMBLE for one pathway. 
		return s;
	
	}
	
	public static Double[][] getPercentage(List<Set> refPWarray){
		
		int numberOfPathways=refPWarray.size();
		Double[][] overlap=new Double[numberOfPathways][];
		int[][] a=getOverlapMatrix(refPWarray);
		int[] numberOfGenes=getSizeVector(refPWarray);
		
		for(int j=0;j<numberOfPathways;j++){
			overlap[j]=new Double[numberOfPathways];
			for(int k=0;k<numberOfPathways;k++){
				overlap[j][k]=(double)a[j][k]/(double)numberOfGenes[j]*100.0;
				
			}
			
			
		}
		
		return overlap;
		
	}
	
	public static int[][] getOverlapMatrix(List<Set> refPWarray){
		
		int numberOfPathways=refPWarray.size();
		int[][] a=new int[numberOfPathways][];
		boolean m;
		
		for(int j=0;j<numberOfPathways;j++){
			a[j]=new int[numberOfPathways];
			
			
			for(int k=0;k<j+1;k++){
				
				System.out.println("("+j+","+k+")");
												
				Set<Xref> refSet=refPWarray.get(j);
				int overeenkomsten=0;
				for(Xref l:refSet){
					m=refPWarray.get(k).contains(l);
					
					if(m==true){
						overeenkomsten++;
					}
					
				}
				System.out.println(overeenkomsten);
				a[j][k]=overeenkomsten;
				a[k][j]=overeenkomsten;
			}
		}
			
		return a;
	}
	
	public static int[] getSizeVector(List<Set> refPWarray){
		
		int numberOfPathways=refPWarray.size();
		int[] numberOfGenes=new int[numberOfPathways];
		
		for(int j=0;j<numberOfPathways;j++){
			numberOfGenes[j]=refPWarray.get(j).size();
		}
		
		return numberOfGenes;
	}

	
	
	
}


