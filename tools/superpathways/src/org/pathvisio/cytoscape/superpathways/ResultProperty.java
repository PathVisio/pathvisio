package org.pathvisio.cytoscape.superpathways;


import org.pathvisio.util.swing.PropertyColumn;

public enum ResultProperty implements PropertyColumn {
		NAME("Name"),
		ORGANISM("Organism"),
		SCORE("Score"),
		URL("Url");
		
		String title;
		
		private ResultProperty(String title) {
			this.title = title;
		}
		
		public String getTitle() {
			return title;
		}
	}