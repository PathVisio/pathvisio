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
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;

public class GeneCounter {

	/**
	 * @param args
	 * @throws ConverterException 
	 * @throws DataException 
	 * @throws ConverterException 
	 * @throws DataException 
	 */
	public static void main(String[] args) throws DataException, ConverterException{
		
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
		
		Double[][] overlap=getOverlap(dbDir,pwDir);

		
		
		
}
	
	public static Double[][] getOverlap(String dbDir,File pwDir) throws DataException, ConverterException{
		
		

		// Total amount of known genes in the Ensembl Database (http://www.ensembl.org).
		int numberOfGenesEN = 17738;
		
		// Here the method "getFileListing" is executed. In this method all files that are stored in the  list of files is created, so that each file can easily be loaded. 
		List<File> filenames = FileUtils.getFileListing(pwDir, ".gpml");
		
		// Make a set to store the genes used in WikiPathways.
		Set<Xref> totalS=new HashSet<Xref>();
		
		// A SimpleGdb Database is used to be able to load the Database downloaded from internet. 
		SimpleGdb db=new SimpleGdb(dbDir,new DataDerby(),0);

		
		//refPWarray is a list that contains all genes of all pathways. With this list, the overlap between different pathway can easily be determined. 
		List<Set> refPWarray = new ArrayList<Set>();
		
		// In the following for-loop the information from all different pathways must be loaded. 
		for (int i=0;i<filenames.size();i++){
		//for (int i=0;i<5;i++){
		
			//
			File fileName=filenames.get(i);
			//System.out.println(fileName);
			
			//The ouput of the method 'getRefPW' is named 'setOfRefPW'
			Set<Xref> setOfRefPW=getRefPW(fileName,db);
			
			//The output of 'setOfRefPW' is added to 'refPWarray'
			refPWarray.add(setOfRefPW);
						
			//In the set 'totalS', all different sets are added. So one big set is formed with all Xrefs.
			totalS.addAll(setOfRefPW);
		}
		// End for-loop
		
	
		//The percentage of used genes that are used at http://www.wikipathways.org are calculated and given as output. 
		double usedgenes=totalS.size();
		usedgenes=usedgenes/numberOfGenesEN*100;
		System.out.println("Percentage of used genes at http://www.wikipathways.org = "+usedgenes+"%");
		
		Double[][] overlap=getPercentage(refPWarray);
		
		/*
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(overlap);
            }
        });
		*/
		
		
		
		
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


