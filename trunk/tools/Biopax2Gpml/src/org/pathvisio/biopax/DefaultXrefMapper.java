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
import org.biopax.paxtools.model.level2.xref;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

public class DefaultXrefMapper implements XrefMapper {
	public void mapXref(entity e, PathwayElement pwElm) {
		for(xref xref : e.getXREF()) {
			Xref gpmlXref = getDataNodeXref(xref);
			if(gpmlXref != null) {
				pwElm.setGeneID(gpmlXref.getId());
				pwElm.setDataSource(gpmlXref.getDataSource());
				break; //Stop after first valid xref
			}
		}
	}
	
	Xref getDataNodeXref(xref x) {
		String db = x.getDB();
		DataSource ds = DataSource.getByFullName(db);
		String id = x.getID();
		return new Xref(id, ds);
	}
}
