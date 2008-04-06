import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


public class GoReader {
	
	public static void main(String[] args){
		// new list with GoTerms
		Set<GoTerm> terms = new HashSet<GoTerm>();
		// new list with roots 
		Set<GoTerm> roots = new HashSet<GoTerm>();
		// start reading the GoDatabase
		// make sure args[0] refers to the database file
		terms=readGoDatabase(args[0]);
		// get the roots of the database
		
		//roots=getRoots(terms);
		for (GoTerm term : roots){
			System.out.println(term.getId());
			System.out.println(term.getName());
			System.out.println(term.getNamespace());
			System.out.println(term.getParents());
			System.out.println(term.getChildren());
			System.out.println();
		}
	}
	
	public static Set<GoTerm> readGoDatabase(String path){
		// each line is read as a string
		String line; 
		// the list with GoTerms
		Set<GoTerm> terms = new HashSet<GoTerm>();
		Map<GoTerm, Set<String>> goTerm_Parents = new HashMap<GoTerm,Set<String>>();
		Map<String, GoTerm> id_goTerm = new HashMap<String,GoTerm>();
		// start reading the file (buffered)
		try 
		{
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			// read line-by-line until the end is reached
			while((line=br.readLine()) != null)
			{
				// if the line starts with a term, process it
				if(line.startsWith("[Term]"))
				{
					// extract the GO:ID
					String id = br.readLine().substring(4);
					// extract the name
					String name = br.readLine().substring(6);
					// extract the namespace
					String namespace = br.readLine().substring(11);
					// create a list for the partens
					Set<String> isa = new HashSet<String>();
					// track if the entry is marked as obsolete
					boolean obsolete = false;
					// continue reading lines until the end of the term or file
					do 
					{
						line=br.readLine();
						// if the line starts with an is_a, add this to the list
						if(line.startsWith("is_a:")){
							isa.add(line.substring(6,16));							
						}
						else{
							// if the line starts with is_obsolete, obsolete = true
							if (line.startsWith("is_obsolete:"))
							{
								obsolete = true;
							}
						}
					}
					while(! line.equals("") && line != null);
					// only add the term if it isn't obsolete
					if (!obsolete)
					{
						GoTerm new_GoTerm = new GoTerm(id,name,namespace);
						terms.add(new_GoTerm);
						goTerm_Parents.put(new_GoTerm, isa);
						id_goTerm.put(id, new_GoTerm);
					}
				}				
		    }		
			fr.close();			
		}		
		catch(Exception e) 
		{
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}
		
		for (GoTerm thisTerm : terms){
			Set<String> parents = goTerm_Parents.get(thisTerm);
			
			if (!parents.isEmpty()){
				for(String parent : parents){
				
					GoTerm ouder = id_goTerm.get(parent);
				
					thisTerm.addParent(ouder);
					ouder.addChild(thisTerm);
				
				}
			}
		}
		
		
		System.out.println("alles ingelezen");
		//for (GoTerm thisTerm : terms){
		//	System.out.println(thisTerm.getName());
		//	Set<GoTerm> kids = thisTerm.getChildren();
		//	for (GoTerm deze : kids){
		//		System.out.println("|-"+deze.getName());
		//	}
		//}
		//terms=findChildren(terms);
		return terms;
	}
	
	public static Set<GoTerm> getRoots(Set<GoTerm> terms)
	{
		// create a list for the roots
		Set<GoTerm> roots = new HashSet<GoTerm>();
		// walk through the terms to find the roots
		for (GoTerm term : terms)
		{
			// if a term has no parents, it's a root
			if(term.getParents().isEmpty())
			{
				// add the term as root
				roots.add(term);
			}				
		}
		// return the root list
		return roots;
	}
}
	

