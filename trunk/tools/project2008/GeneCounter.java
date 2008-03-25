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
		
		Set<String> totalS=new HashSet<String>();
		
		/* Hier moet iets van een for-loop komen om de gegevens
		 * uit de verschillende Pathways te halen.
		 * */
		
		// namen van de Pathways maken/aanroepen
		String namePathway = "Rn_ACE-Inhibitor_pathway_PharmGKB.gpml";
		
		//s berekenen
		Set<String> setOfRefPW=getRefPW(namePathway);
		
		/*
		 * Hier moet de functie worden aangeroepen die de referenties omzet naar EN.
		 */
		
		//s in de totale set zetten
		totalS.addAll(setOfRefPW);
		
		/* Einde for loop
		 */
		
		// Output: Grootte van de totale set
		System.out.println(totalS.size());
		
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
