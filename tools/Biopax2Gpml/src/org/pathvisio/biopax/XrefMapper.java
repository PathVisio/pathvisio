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
package org.pathvisio.biopax;

import org.biopax.paxtools.model.level2.entity;
import org.pathvisio.model.PathwayElement;

/**
 * Implement this interface to provide mappings
 * for BioPAX xrefs to GPML xrefs.
 * @author thomas
 */
public interface XrefMapper {
	/**
	 * Map the xref property for the given BioPAX element
	 * to the GPML pathway element.
	 * @param e The BioPAX element
	 * @param pwElm The GPML element
	 */
	public void mapXref(entity e, PathwayElement pwElm);
}
