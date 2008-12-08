// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

/** Implementation of tri-state logical data type in R.
    The three states are TRUE, FALSE and NA. To obtain truly boolean
    value, you'll need to use {@link #isTRUE} or {@link #isFALSE} since there is
    no canonical representation of RBool in boolean

    @version $Id: RBool.java 2720 2007-03-15 17:35:42Z urbanek $
*/
public class RBool extends Object {
    int val;

    public RBool(boolean b) {
	val=(b)?1:0;
    };
    public RBool(RBool r) {
	val=r.val;
    };
    public RBool(int i) { /* 0=FALSE, 2=NA, anything else = TRUE */
	val=(i==0||i==2)?i:1;
    };

    public boolean isNA() { return (val==2); };
    public boolean isTRUE() { return (val==1); };
    public boolean isFALSE() { return (val==0); };

    public String toString() { return (val==0)?"FALSE":((val==2)?"NA":"TRUE"); };
}
