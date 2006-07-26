package search;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class holds a collection of {@link SearchResult}s sharing the same attribute types
 */
public class SearchResults {
	private ArrayList<Attribute> attributeTemp;
	
	private ArrayList<SearchResult> results;
	
	public SearchResults() {
		attributeTemp = new ArrayList<Attribute>();
		
		results = new ArrayList<SearchResult>();
	}
	
	public ArrayList<SearchResult> getResults() { return results; }
	public ArrayList<String> getAttributeNames() { 
		return getAttributeNames(false); 
	}
	
	public ArrayList<String> getAttributeNames(boolean showHidden) {
		ArrayList<String> attrNames = new ArrayList<String>();
		for(Attribute at : attributeTemp) {
			if(!showHidden) { if(at.isVisible()) attrNames.add(at.name); }
			else attrNames.add(at.name);
		}
		return attrNames;
	}
	
	public void addAttribute(String name, int type) {
		attributeTemp.add(new Attribute(name, type, true));
	}
	public void addAttribute(String name, int type, boolean visible) {
		attributeTemp.add(new Attribute(name, type, visible));
	}
	
	public void addResult(SearchResult rs) { results.add(rs); }
	
	/**
	 * This class contains a single result from a search
	 */
	public class SearchResult {
		private HashMap<String, Attribute> attributes;
		
		public SearchResult() { 
			attributes = new HashMap<String, Attribute>();
			for(Attribute at : attributeTemp) {
				attributes.put(at.getName(),
						new Attribute(at.getName(), at.type, at.isVisible()));
			}
			results.add(this);
		}
		
		public void setAttribute(String name, String value) {
			if(attributes.containsKey(name)) attributes.get(name).setText(value);
		}
		
		public void setAttribute(String name, double value) {
			if(attributes.containsKey(name)) attributes.get(name).setNumeric(value);
		}
		
		public Attribute getAttribute(String name) throws Exception {
			if(attributes.containsKey(name)) return attributes.get(name);
			throw new Exception("Attribute " + name + " does not exist");
		}
		
		public ArrayList<Attribute> getAttributes(boolean onlyVisible) {
			ArrayList<Attribute> attr = new ArrayList<Attribute>();
			for(Attribute at : attributeTemp) {
				Attribute atr = attributes.get(at.getName());
				if(atr.isVisible()) attr.add(atr);
			}
			return attr;
		}
	}
	
	/**
	 * This class represents a search attribute, which can either
	 * have a numeric or text value
	 */
	public class Attribute {
		public static final int TYPE_TEXT = 0;
		public static final int TYPE_NUM  = 1;
		
		private String name;
		private String textValue;
		private double numValue;
		private int type;
		private boolean visible;
		
		public Attribute(String n, int t) { name = n; type = t; textValue = ""; this.visible = true; }
		public Attribute(String n, int t, boolean visible) { this(n, t); this.visible = visible; }
		
		public String getName() { return name; }
		public String getText() { return type == TYPE_NUM ? Double.toString(numValue) : textValue; }
		public double getNumeric() { return numValue; }
		public void setText(String value) { textValue = value; }
		public void setNumeric(double value) { numValue = value; }
		public boolean isVisible() { return visible; }
	}
}
