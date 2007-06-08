// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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
package org.rosuda.JRI;

import java.util.Vector;
import java.util.Enumeration;

/** class encapsulating named generic vectors in R - do NOT use add/remove directly as names are not synchronized with the contents. The reason for this implementation is for historical compatibility and it may change in the future.
<p>
It is now used in <code>REXP</code> where <code>Vector</code> type was used previously for contents storage.
@since JRI 0.3
*/
public class RVector extends java.util.Vector {
	Vector names = null;
	public RVector() { super(); }
	
	/** replace the names vector - do NOT use directly!
		@param list of names */
	public void setNames(String[] nam) {
		names=new Vector(nam.length);
		int i=0;
		while (i<nam.length)
			names.addElement(nam[i++]);
	}
	
	/** return the vector containg all names
		@return */
	public Vector getNames() {
		return names;
	}

	/** return contents by name or <code>null</code> if not found
		@param name
		@return contents or <code>null</code> if not found
		*/
	public REXP at(String name) {
		if (names==null) return null;
		int i=0;
		for (Enumeration e = names.elements() ; e.hasMoreElements() ;) {
			String n = (String)e.nextElement();
			if (n.equals(name)) return (REXP) elementAt(i);
			i++;
		}
		return null;
	}
	
	public REXP at(int i) {
		return (REXP)elementAt(i);
	}
}
