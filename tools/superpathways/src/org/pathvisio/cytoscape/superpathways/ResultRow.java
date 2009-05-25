package org.pathvisio.cytoscape.superpathways;

import org.pathvisio.util.swing.RowWithProperties;
import org.pathvisio.wikipathways.webservice.WSSearchResult;

public class ResultRow implements RowWithProperties<ResultProperty> {
	WSSearchResult result;
	
	public ResultRow(WSSearchResult result) {
		this.result = result;
	}
	
	public WSSearchResult getResult() {
		return result;
	}
	
	public String getProperty(ResultProperty prop) {
		switch(prop) {
		case NAME: return result.getName();
		case ORGANISM: return result.getSpecies();
		case SCORE: return Double.toString(result.getScore());
		case URL: return result.getUrl();
		}
		return null;
	}
}