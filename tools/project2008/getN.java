import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

public class getN{
	
	public static void main(String[] args) throws DataException, ConverterException{
	
	
	}
	
	public static Set<String> getSetGenIdsInPways(String dbDir, String pwDirString) throws DataException, ConverterException{
		File pwDir = null;


			pwDir = new File(pwDirString);
		
		
		Set<Xref> xrefs = getEnsembleZooi(dbDir,pwDir);
		Set<String> setWithGenIdsInPathways = new HashSet<String>();
		for(Xref xref: xrefs){
			setWithGenIdsInPathways.add(xref.getId());
		}
		
		return setWithGenIdsInPathways;
	}
	
	public static Set<Xref> getEnsembleZooi(String dbDir,File pwDir) throws DataException, ConverterException{

		
		// Here the method "getFileListing" is executed. In this method all files that are stored in the  list of files is created, so that each file can easily be loaded.
		String pwExtension = ".gpml";
		List<File> filenames = FileUtils.getFileListing(pwDir, pwExtension);
		
		// Make a set to store the genes used in WikiPathways.
		Set<Xref> totalS=new HashSet<Xref>();
		
		// A SimpleGdb Database is used to be able to load the Database downloaded from internet. 
		SimpleGdb db=new SimpleGdb(dbDir,new DataDerby(),0);

		
		//refPWarray is a list that contains all genes of all pathways. With this list, the overlap between different pathway can easily be determined. 
		List< Set<Xref> > refPWarray = new ArrayList< Set<Xref> >();
		
		// In the following for-loop the information from all different pathways must be loaded. 
		for (int i=0;i<filenames.size();i++){
		
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
		
	
		
		return totalS;
	}
	
	
	//In this method a set is created that contains all the references in a Pathway.
	public static Set<Xref> getRefPW(File filename,SimpleGdb db) throws ConverterException{
		
		//A new set is created where the Xrefs can be stored. 
		Set<Xref> s=new HashSet<Xref>();
		//A new pathway is created.
		Pathway p = new Pathway();
		//For this pathway the information is loaded. 
		p.readFromXml(filename, false);
				
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
	
	
	
}