import java.util.Set;

public class GoTerm {

	private String id;
	private String name;
	private String namespace;
	private Set<String> isa;
	private Set<String> children;
	
	public GoTerm(String id, String name, String namespace, Set<String> isa, Set<String> children){
		this.id = id;
		this.name = name;
		this.namespace = namespace;
		this.isa = isa;
		this.children = children;
	}
	
	public void setChildren(Set<String> children){
		this.children=children;
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
	
	public Set<String> getChildren(){
		return children;
	}

}
