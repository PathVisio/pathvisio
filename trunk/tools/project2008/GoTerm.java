import java.util.List;
import java.util.Set;

public class GoTerm {

	private String id;
	private String name;
	private String namespace;
	private Set<String> isa;
	
	public GoTerm(String id, String name, String namespace, Set<String> isa){
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
	
	public Set<String> getParents(){
		return isa;
	}	

}
