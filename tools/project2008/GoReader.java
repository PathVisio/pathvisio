import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class GoReader {
	
	public static void main(String[] args) {
		List<GoTerm> terms = new ArrayList<GoTerm>();
		List<GoTerm> roots = new ArrayList<GoTerm>();
		List<GoTerm> children = new ArrayList<GoTerm>();
		List<String> id = new ArrayList<String>();
		terms=readGoDatabase(args[0]);
		roots=getRoots(terms);
		id.add("GO:0006854");
		children=getChildren(terms,id);
		for (GoTerm child : children){
			System.out.println(child.getId());
			System.out.println(child.getName());
			System.out.println(child.getNamespace());
			System.out.println(child.getParents());
			System.out.println();
		}
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
	
	public static List<GoTerm> getChildren(List<GoTerm> terms, List<String> id){
		List<GoTerm> children = new ArrayList<GoTerm>();
		for (GoTerm term : terms){
			if(term.getParents().contains(id)){
				children.add(term);
			}
 		}
		if(children.isEmpty()){
			System.out.println("No children");
		}
		return children;
	}
}
