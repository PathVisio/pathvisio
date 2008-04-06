import java.util.HashSet;
import java.util.Set;

public class GoTerm {

	private String id;
	private String name;
	private String namespace;
	private Set<GoTerm> parents = new HashSet<GoTerm>();
	private Set<GoTerm> children = new HashSet<GoTerm>();
	
	public GoTerm(String id, String name, String namespace){
		this.id = id;
		this.name = name;
		this.namespace = namespace;
	}
	
	public void addChild(GoTerm child){
		this.children.add(child);
	}
	
	public void addParent(GoTerm parent){
		this.parents.add(parent);
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
	
	public Set<GoTerm> getParents(){
		return parents;
	}
	
	public Set<GoTerm> getChildren(){
		return children;
	}

}
