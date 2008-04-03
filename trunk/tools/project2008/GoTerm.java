import java.util.List;

public class GoTerm {

	private String id;
	private String name;
	private String namespace;
	private List<String> isa;
	
	public GoTerm(String id, String name, String namespace, List<String> isa){
		this.id = id;
		this.name = name;
		this.namespace = namespace;
		this.isa = isa;
	}
	
	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;	
	}
	
	public String getNamespace(){
		return namespace;	
	}
	
	public List<String> getParents(){
		return isa;
	}	

}
