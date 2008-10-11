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
