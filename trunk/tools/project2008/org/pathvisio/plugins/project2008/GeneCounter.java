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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.SimpleGdbFactory;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.model.XrefWithSymbol;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.PathwayParser;
import org.pathvisio.util.PathwayParser.ParseException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * In the Gene counter the percentage of genes that are known in the Ensembl database that
 * also exist in the pathways at wikipathways.org is returned.
 * Also a matrix is returned that contains the overlap between all possible pairs off two
 * pathways.
 */

public class GeneCounter {

	public static void main(String[] args) throws DataException, ConverterException{
		/**
		* in the String[] args, 2 arguments are given:
		* in example:
		* "C:\\databases\\"
		* "C:\pathways"
		*
		* The first one is the directory that contains the databases.
		* The second one is the directory that contains the pathway cache.
		*
		* Check if the String[] args is given, and make Files containing the directories to
		* the pathways and databases.
		*/
		String dbDir = null;
		File pwDir = null;
		try {
			dbDir = new String(args[0]+"Rn_39_34i.pgdb");
			pwDir = new File(args[1]+"\\Rattus_norvegicus");

		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("String[] args not given!");
			System.exit(0);
		}

		List<Set<Xref>> refPWarray=getSets(dbDir,pwDir);

		/**
		 * Here the percentage is calculated of genes that are known in the Ensembl database
		 * and also exist in the pathways at wikipathways.org.
		 */
		int numberOfGenesEN=getNumberOFGenesEN();
		int usedgenes=refPWarray.get(refPWarray.size()-1).size();
		double percentageUsedGenes=(double)usedgenes/(double)numberOfGenesEN*100.0;
		percentageUsedGenes=(long)Math.round(percentageUsedGenes*1000.0)/1000.0;
		System.out.println("Percentage of used genes at http://www.wikipathways.org = "+percentageUsedGenes+"%");

		/**
		 * Here the matrix is calculated with the overlap between the pathways.
		 */
		refPWarray.remove(refPWarray.size()-1);
		Double[][] overlap=getPercentage(refPWarray);
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
	public static List<Set<Xref>> getSets(String dbDir,File pwDir) throws DataException, ConverterException{
		List<File> filenames = FileUtils.getFiles(pwDir, "gpml", true);
		Set<Xref> totalS=new HashSet<Xref>();
		Gdb db= SimpleGdbFactory.createInstance(dbDir,new DataDerby(),0);
		List<Set<Xref>> refPWarray = new ArrayList<Set<Xref>>();
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
	 * In the method getUsedGenes the percentage is returned of the total genes known in the
	 * Ensembl database that are used in wikipathways.
	 * In the set 'refPWarray' all the Xref's are stored that are used in the pathways. So the
	 * size of this set represents the number of genes used in the pathways.
	 * Now the percentage can be calculated.
	 */
	public static double getUsedGenes(String dbDir,File pwDir) throws DataException, ConverterException{
		// Total amount of known genes in the Ensembl Database.
		int numberOfGenesEN = getNumberOFGenesEN();
		List<Set<Xref>> refPWarray=getSets(dbDir,pwDir);
		int usedgenes=refPWarray.get(refPWarray.size()-1).size();
		double percentageUsedGenes=(double)usedgenes/(double)numberOfGenesEN*100.0;
		percentageUsedGenes=(long)Math.round(percentageUsedGenes*1000.0)/1000.0;
		System.out.println("Percentage of used genes at http://www.wikipathways.org = "+percentageUsedGenes+"%");
		return percentageUsedGenes;
	}

	/**
	 * In the method 'getOverlap' the overlap of genes between all pathways is calculated.
	 * A two dimensional array is returned with the overlap between the pathways.
	 */
	public static Double[][] getOverlap(String dbDir,File pwDir) throws DataException, ConverterException{
		List<Set<Xref>> refPWarray=getSets(dbDir,pwDir);
		refPWarray.remove(refPWarray.size()-1);
		Double[][] overlap=getPercentage(refPWarray);
		return overlap;
	}

	/**
	 * In this method a set is created that contains all the references in a Pathway,
	 * normalized to Ensembl.
	 *
	 * First, for a pathway, p, the information is loaded.
	 * Then a list is formed that contains the elements stored in the pathway.
	 * In the for-loop each element of the pathway that represents a Xref is stored in a set.
	 * Only is the objectType is DATANODE, the element is a Xref.
	 * Than each Xref is translated to a reference as stored in the Enseml databank.
	 * At last all references are added to a set. So a set remains with all Xref't that exist
	 * in the pathways. This set is returned.
	 */
	public static Set<Xref> getRefPW(File filename, Gdb db)
	{
		Set<Xref> s = new HashSet<Xref>();

		try
		{
			Logger.log.info ("Reading pathway " + filename);
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			PathwayParser p = new PathwayParser(filename, xmlReader);
			for (XrefWithSymbol gene : p.getGenes())
			{
				List<Xref> cRef = db.getCrossRefs(gene.asXref(),DataSource.ENSEMBL);
				s.addAll(cRef);
			}
		}
		catch (ParseException e)
		{
			Logger.log.error ("Ignoring Pathway");
		}
		catch (DataException e)
		{
			Logger.log.error ("Ignoring Pathway");
		}
		catch (SAXException e)
		{
			Logger.log.error ("Couldn't create XML reader");
		}
		return s;
	}

	/**
	 * In the method 'getPercentage' the overlap between all the pathways is calculated in
	 * percentages. For each possible pair of two pathways, it is calculated what percentage
	 * of genes that exist in the first pathway also exists in the second pathway. A matrix
	 * is returned with these percentages.
	 */
	public static Double[][] getPercentage(List<Set<Xref>> refPWarray){
		int numberOfPathways=refPWarray.size();
		Double[][] overlap=new Double[numberOfPathways][];
		int[][] a=getOverlapMatrix(refPWarray);
		int[] numberOfGenes=getSizeVector(refPWarray);
		for(int j=0;j<numberOfPathways;j++){
			overlap[j]=new Double[numberOfPathways];
			for(int k=0;k<numberOfPathways;k++){
				overlap[j][k]=(double)a[j][k]/(double)numberOfGenes[j]*100.0;
				overlap[j][k]=(long)Math.round(overlap[j][k]*1000.0)/1000.0;
			}
		}
		return overlap;
	}

	/**
	 * In the method 'getOverlapMatrix' the overlap between all the pathways is calculated in
	 * numbers. These numbers are calculated into percentages in another method.
	 * In two for-loops for all pathways it is checked how many genes in that pathway also exist
	 * in another pathway. These numbers are returned.
	 */
	public static int[][] getOverlapMatrix(List<Set<Xref>> refPWarray){

		int numberOfPathways=refPWarray.size();
		int[][] a=new int[numberOfPathways][];
		boolean m;
		for(int j=0;j<numberOfPathways;j++){
			a[j]=new int[numberOfPathways];
			for(int k=0;k<j+1;k++){
				Set<Xref> refSet=refPWarray.get(j);
				int overeenkomsten=0;
				for(Xref l:refSet){
					m=refPWarray.get(k).contains(l);
					if(m==true){
						overeenkomsten++;
					}
				}
				a[j][k]=overeenkomsten;
				a[k][j]=overeenkomsten;
			}
		}
		return a;
	}

	/**
	 * In the method 'getSizeVector' the number of genes are returned that are stored in the
	 * List<Set<Xref>> refPWarray.
	 */
	public static int[] getSizeVector(List<Set<Xref>> refPWarray){
		int numberOfPathways=refPWarray.size();
		int[] numberOfGenes=new int[numberOfPathways];
		for(int j=0;j<numberOfPathways;j++){
			numberOfGenes[j]=refPWarray.get(j).size();
		}
		return numberOfGenes;
	}

}


