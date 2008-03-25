import java.io.File;
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
	 */
	public static void main(String[] args) throws ConverterException {
		// TODO Auto-generated method stub
		
		String namePathway = "Rn_Apoptosis.gpml";
		File f = new File("D:\\My Documents\\school\\jaar 3\\Semester 2\\project blok 3E\\wikipathways_1206450480\\"+namePathway);
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
				System.out.println(name);
				
				
			
			}
		
		//List<PathwayElement> types = p.getObjectType();
		//for (PathwayElement v:types){
			//PathwayElement.getObjectType();
		
		
		
		//List<PathwayElement> pelts=List p.getDataObjects 
		
	}

}
}
