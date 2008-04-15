package org.pathvisio.plugins.project2008;
//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics
//
//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 
//
//http://www.apache.org/licenses/LICENSE-2.0 
//
//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.
//
import java.util.HashSet;
import java.util.Set;

/**
 * Class to create the object "GoTerm";
 * a GoTerm is a Gene Ontology Term, which has compulsory name, id and namespace;
 * and optional parents, children and genes. 
 */
public class GoTerm {

	private String id;
	private String name;
	private String namespace;
	private Set<GoTerm> parents = new HashSet<GoTerm>();
	private Set<GoTerm> children = new HashSet<GoTerm>();
	private Set<String> genes = new HashSet<String>();
	
	/**
	 * Constructor. Create a new GoTerm, giving the id of the term,
	 * the name and the namespace.
	 * 
	 * i.e. GoTerm newTerm = new GoTerm(id,name,namespace);
	 * ---> GoTerm newTerm = new GoTerm("GO:0048745","smooth muscle development","biological_process");
	 */
	public GoTerm(String id, String name, String namespace){
		this.id = id;
		this.name = name;
		this.namespace = namespace;
	}
	
	/**
	 * add a (Ensemble) gene to the GoTerm.
	 * i.e. newTerm.addGene(gen);
	 * ---> newTerm.addGene("ENSRNOG00000028412");
	 */
	public void addGene(String gen){
		this.genes.add(gen);
	}
	
	/**
	 * return a set of strings containing all the (Ensemble) genes
	 * added to the GoTerm
	 */
	public Set<String> getGenes(){
		return this.genes;
	}
	
	/**
	 * return the number of items the set of (Ensemble) genes contains
	 */
	public int getNumberOfGenes(){
		return this.genes.size();
	}
	
	/**
	 * return the number of overlapping items this GoTerm's set of (Ensemble) genes
	 * has comparing with a given set of genes (Set<String> otherSet)
	 */
	public int getOverlapGenes(Set<String> otherSet){
		int number = 0;
		
		for (String Gene: this.genes){
			if (otherSet.contains(Gene)){
				number = number + 1;
			}
		}
		return number;
	}
	
	/**
	 * add a (GoTerm) child to the GoTerm
	 */
	public void addChild(GoTerm child){
		this.children.add(child);
	}
	
	/**
	 * add a (GoTerm) parent to the GoTerm
	 */
	public void addParent(GoTerm parent){
		this.parents.add(parent);
	}
	
	/**
	 * return the id of the GoTerm
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * return the name of the GoTerm
	 */
	public String getName(){
		return name;	
	}
	
	/**
	 * return the namespace of the GoTerm
	 */
	public String getNamespace(){
		return namespace;	
	}
	
	/**
	 * return a set of GoTerms containing all the parents of the GoTerm
	 */
	public Set<GoTerm> getParents(){
		return parents;
	}
	
	/**
	 * return a set of GoTerms containing all the children of the GoTerm
	 */
	public Set<GoTerm> getChildren(){
		return children;
	}

	/**
	 * check if the GoTerm has Parents
	 * if so, return 'true', else return 'false'
	 */
	public boolean hasParents(){
		return !parents.isEmpty();
	}
	
	/**
	 * check if the GoTerm has Children
	 * if so, return 'true', else return 'false'
	 */
	public boolean hasChildren(){
		return !children.isEmpty();
	}
	
}
