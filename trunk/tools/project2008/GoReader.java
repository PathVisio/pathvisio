import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class GoReader {
	
	public static void main(String[] args) {
		String s; 
		System.out.println(args[0]);		
		List<GoTerm> terms = new ArrayList<GoTerm>();	
		List<String> isa = new ArrayList<String>();
		
		try {
			FileReader fr = new FileReader(args[0]);
			BufferedReader br = new BufferedReader(fr);

			while((s = br.readLine()) != null){
				if(s.startsWith("[Term]")){
					String id = br.readLine().substring(4);
					String name = br.readLine().substring(6);
					while ((s = br.readLine()) != "" && s != null ){
						if(s.startsWith("is_a:")){
							System.out.println(s);
							isa.add(s.substring(6,16));							
						}
					}
					terms.add(new GoTerm(id,name,isa));
					isa.clear();
				}
		    }		
		    fr.close();
		    }		
		    catch(Exception e) {
		      System.out.println("Exception: " + e);
		}
		for (GoTerm term : terms){
				//System.out.println(term.getId());
				//System.out.println(term.getName());
				//System.out.println(term.getParents());
				//System.out.println();
		}
	}
}
