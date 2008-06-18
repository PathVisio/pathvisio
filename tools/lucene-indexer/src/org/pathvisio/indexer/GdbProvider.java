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
package org.pathvisio.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.data.Gdb;
import org.pathvisio.model.Organism;

/**
 * Utility class that maintains a list of organism, gene databases relations
 * @author thomas
 */
public class GdbProvider {
	Map<Organism, List<Gdb>> organism2gdb = new HashMap<Organism, List<Gdb>>();
	List<Gdb> globalGdbs = new ArrayList<Gdb>();
	
	public void addOrganismGdb(Organism organism, Gdb gdb) {
		List<Gdb> l = organism2gdb.get(organism);
		if(l == null) {
			organism2gdb.put(organism, l = new ArrayList<Gdb>());
		}
		if(!l.contains(gdb)) {
			l.add(gdb);
		}
	}
	
	public void removeOrganismGdb(Organism organism, Gdb gdb) {
		List<Gdb> l = organism2gdb.get(organism);
		if(l != null) {
			l.remove(gdb);
		}
	}
	
	public void addGlobalGdb(Gdb gdb) {
		if(!globalGdbs.contains(gdb)) globalGdbs.add(gdb);
	}
	
	public void removeGlobalGdb(Gdb gdb) {
		globalGdbs.remove(gdb);
	}
	
	public List<Gdb> getGdbs(Organism organism) {
		List<Gdb> gdbs = organism2gdb.get(organism);
		if(gdbs == null) {
			gdbs = new ArrayList<Gdb>();
		}
		gdbs.addAll(globalGdbs);
		return gdbs;
	}
}
