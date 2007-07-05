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
package org.rosuda.JRI;

// JRclient library - client interface to Rserve, see http://www.rosuda.org/Rserve/
// Copyright (C) 2004 Simon Urbanek
// --- for licensing information see LICENSE file in the original JRclient distribution ---

import java.util.*;

/** representation of a factor variable. In R there is no actual xpression
    type called "factor", instead it is coded as an int vector with a list
    attribute. The parser code of REXP converts such constructs directly into
    the RFactor objects and defines an own XT_FACTOR type 
    
    @version $Id: RFactor.java 2720 2007-03-15 17:35:42Z urbanek $
*/    
public class RFactor extends Object {
    /** IDs (content: Integer) each entry corresponds to a case, ID specifies the category */
    Vector id;
    /** values (content: String), ergo category names */
    Vector val;

    /** create a new, empty factor var */
    public RFactor() { id=new Vector(); val=new Vector(); }
    
    /** create a new factor variable, based on the supplied arrays.
		@param i array of IDs (0..v.length-1)
		@param v values - category names */		
    public RFactor(int[] i, String[] v) {
		this(i, v, 0);
	}

    /** create a new factor variable, based on the supplied arrays.
		@param i array of IDs (base .. v.length-1+base)
		@param v values - cotegory names
		@param base of the indexing
		*/
    RFactor(int[] i, String[] v, int base) {
		id=new Vector(); val=new Vector();
		int j;
		if (i!=null && i.length>0)
			for(j=0;j<i.length;j++)
				id.addElement(new Integer(i[j]-base));
		if (v!=null && v.length>0)
			for(j=0;j<v.length;j++)
				val.addElement(v[j]);
    }
   
    /** add a new element (by name)
	@param v value */
    public void add(String v) {
	int i=val.indexOf(v);
	if (i<0) {
	    i=val.size();
	    val.addElement(v);
	};
	id.addElement(new Integer(i));
    }

    /** returns name for a specific ID 
	@param i ID
	@return name. may throw exception if out of range */
    public String at(int i) {
	if (i<0||i>=id.size()) return null;
	return (String)val.elementAt(((Integer)id.elementAt(i)).intValue());
    }

    /** returns the number of caes */
    public int size() { return id.size(); }

    /** displayable representation of the factor variable */
    public String toString() {
	//return "{"+((val==null)?"<null>;":("levels="+val.size()+";"))+((id==null)?"<null>":("cases="+id.size()))+"}";
	StringBuffer sb=new StringBuffer("{levels=(");
	if (val==null)
	    sb.append("null");
	else
	    for (int i=0;i<val.size();i++) {
		sb.append((i>0)?",\"":"\"");
		sb.append((String)val.elementAt(i));
		sb.append("\"");
	    };
	sb.append("),ids=(");
	if (id==null)
	    sb.append("null");
	else
	    for (int i=0;i<id.size();i++) {
		if (i>0) sb.append(",");
		sb.append((Integer)id.elementAt(i));
	    };
	sb.append(")}");
	return sb.toString();
    }
}

