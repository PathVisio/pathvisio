import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class GoReader {
	
	public static void main(String[] args) {
		List<GoTerm> terms = new ArrayList<GoTerm>();
		List<GoTerm> roots = new ArrayList<GoTerm>();
		terms=readGoDatabase(args[0]);
		roots=getRoots(terms);
		getChildren(terms,"GO:0052182");
		//for (GoTerm root : roots){
		//	System.out.println(root.getId());
		//	System.out.println(root.getName());
		//	System.out.println(root.getNamespace());
		//	System.out.println(root.getParents());
		//	System.out.println();
		//}
	}
	
	public static List<GoTerm> readGoDatabase(String path){
		String line; 
		List<GoTerm> terms = new ArrayList<GoTerm>();			
		
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);

			while((line=br.readLine()) != null){
				if(line.startsWith("[Term]")){					
					String id = br.readLine().substring(4);
					String name = br.readLine().substring(6);
					String namespace = br.readLine().substring(11);
					List<String> isa = new ArrayList<String>();
					// continue reading lines until the end of the term is reached
					do {
						line=br.readLine();
						if(line.startsWith("is_a:")){
							isa.add(line.substring(6,16));							
						}
					}
					while(! line.equals(""));
					terms.add(new GoTerm(id,name,namespace,isa));
				}
		    }		
		    fr.close();
		    }		
		    catch(Exception e) {
		    System.out.println("Exception: " + e);
		}
		return terms;
	}
	
	public static List<GoTerm> getRoots(List<GoTerm> terms){
		List<GoTerm> roots = new ArrayList<GoTerm>();
		for (GoTerm term : terms){
			if(term.getParents().isEmpty()){
				roots.add(term);
			}				
		}		
	return roots;
	}
	
	public static List<GoTerm> getChildren(List<GoTerm> terms, String id){
		List<GoTerm> children = new ArrayList<GoTerm>();
		for (GoTerm term : terms){
			if(term.getParents().equals(id)){
				System.out.println("Child found!");
			}
 		}
		
		
	return children;
	}
	
	

}
