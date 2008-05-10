// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.plugins.project2008;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.io.File;

/**
 * The class GoReader contains two methods to read the data of the GO terms. 
 */
public class GoReader 
{
	private Set<GoTerm> terms;
	public Set<GoTerm> getTerms()
	{
		return terms;
	}
	
	public GoReader (File obo)
	{		
		/**
		 * Each line is readed as a string.
		 * A map with a GoTerm and the parents (isa's) of this term stored as strings.
		 * A map with a string (containing the goTerm's id) and the goterm.
		 */
		String line; 
		terms = new HashSet<GoTerm>();
		Map<GoTerm, Set<String>> goTerm_Parents = new HashMap<GoTerm,Set<String>>();
		Map<String, GoTerm> id_goTerm = new HashMap<String,GoTerm>();
		
		/**
		 * Start reading the file (buffered).
		 */
		try {
			FileReader fr = new FileReader(obo);
			BufferedReader br = new BufferedReader(fr);
			// Read line-by-line until the end is reached
			while((line=br.readLine()) != null){
				// If the line starts with a term, process it
				if(line.startsWith("[Term]")){
					// extract the GO:ID
					String id = br.readLine().substring(4);
					// extract the name
					String name = br.readLine().substring(6);
					// extract the namespace
					String namespace = br.readLine().substring(11);
					// create a list for the partens
					Set<String> isa = new HashSet<String>();
					// track if the entry is marked as obsolete
					boolean obsolete = false;
					// continue reading lines until the end of the term or file
					do {
						line=br.readLine();
						// if the line starts with an is_a, add this to the list
						if(line.startsWith("is_a:")){
							isa.add(line.substring(6,16));							
						}
						else{
							// if the line starts with is_obsolete, obsolete = true
							if (line.startsWith("is_obsolete:"))
							{
								obsolete = true;
							}
						}
					}
					while(! line.equals("") && line != null);
					// only add the term if it isn't obsolete
					if (!obsolete){
						// if the term isn't obsolete, create the GoTerm
						GoTerm new_GoTerm = new GoTerm(id,name,namespace);
						// and add this term to the 'terms' set
						terms.add(new_GoTerm);
						// and at this term and it's list of parents to the first map
						goTerm_Parents.put(new_GoTerm, isa);
						// and add this term's id and the term itself to the second map
						id_goTerm.put(id, new_GoTerm);
					}
				}				
		    }		
			fr.close();			
		}		
		catch(Exception e) 
		{
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}
		
		/**
		 * In a for-loop, first for all GoTerms the parents are returned.
		 * If these parents exist, the second for-loop walks through all these parents. The GoTerm
		 * of this parent is added (as a value) to a map with current GoTerm (as a key).
		 * Also, in a map the The GoTerm of this parent is added (as a key) to a map with current 
		 * GoTerm (as a value).
		 * So two maps are created: 
		 * One with the children as a key and the parents as a value.
		 * And one with the parents as a key and the children as a value.   
		 */
		// now loop through all GoTerms
		for (GoTerm thisTerm : terms){
			// get the parents (strings) of this goTerm
			Set<String> parents = goTerm_Parents.get(thisTerm);
			if (!parents.isEmpty()){
				// loop through all these parent strings
				for(String parent : parents){
					// get the goTerm beloning to the parent string (the parent string
					// contains the id of the parent, the second map contains the id's
					// with the goterms belonging to this id)
					GoTerm ouder = id_goTerm.get(parent);
					// add the found parent GoTerm as a parent for the read GoTerm
					thisTerm.addParent(ouder);
					// the read GoTerm is a child of it's parent; so add a child to
					// the parent GoTerm
					ouder.addChild(thisTerm);
				}
			}
		}
		
		// show a message that everything is read; and return the terms
		System.out.println("DB read");
	}
	
	/**
	 * In the method 'getRoots' for a set of GoTerms the roots are returned in a set.
	 */
	
	public List<GoTerm> getRoots()
	{
		// create a list for the roots
		List<GoTerm> roots = new ArrayList<GoTerm>();
		// walk through the terms to find the roots
		for (GoTerm term : terms)
		{
			// if a term has no parents, it's a root
			if(!term.hasParents()){
				// add the term as root
				roots.add(term);
			}				
		}
		// return the root list
		return roots;
	}
}
	

