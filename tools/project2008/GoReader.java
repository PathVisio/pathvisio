import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class GoReader {
	
	public static void main(String[] args) {
		readGoDatabase(args[0]);
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
		for (GoTerm term : terms){
				System.out.println(term.getId());
				System.out.println(term.getName());
				System.out.println(term.getNamespace());
				System.out.println(term.getParents());
				System.out.println();
		}
		return terms;
	}

}
