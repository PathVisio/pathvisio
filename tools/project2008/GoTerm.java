import java.util.List;

public class GoTerm {

	private String id;
	private String name;
	private List<String> isa;
	
	public GoTerm(String id, String name, List<String> isa){
		this.id = id;
		this.name = name;
		this.isa = isa;
	}
	
	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;	
	}
	
	public List<String> getParents(){
		return isa;
	}

	
	

}
