package search;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class holds a collection of {@link SearchResult}s sharing the same attribute types
 */
public class SearchResults {
	private ArrayList<String> attributeNames;
	private ArrayList<Integer> attributeTypes;
	
	private ArrayList<SearchResult> results;
	
	public SearchResults() {
		attributeNames = new ArrayList<String>();
		attributeTypes = new ArrayList<Integer>();
		results = new ArrayList<SearchResult>();
	}
	
	public ArrayList<SearchResult> getResults() { return results; }
	public ArrayList<String> getAttributeNames() { return attributeNames; }
	public void addAttribute(String name, int type) {
		attributeNames.add(name);
		attributeTypes.add(type);
	}
	
	public void addResult(SearchResult rs) { results.add(rs); }
	
	/**
	 * This class contains a single result from a search
	 */
	public class SearchResult {
		private HashMap<String, Attribute> attributes;
		
		public SearchResult() { 
			attributes = new HashMap<String, Attribute>();
			for(int i = 0; i < attributeNames.size(); i++) {
				attributes.put(attributeNames.get(i),
						new Attribute(attributeNames.get(i), attributeTypes.get(i)));
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
		
		public ArrayList<Attribute> getAttributes() {
			ArrayList<Attribute> attr = new ArrayList<Attribute>();
			for(String name : attributeNames) attr.add(attributes.get(name));
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
		
		public Attribute(String n, int t) { name = n; type = t; textValue = ""; }
		
		public String getName() { return name; }
		public String getText() { return type == TYPE_NUM ? Double.toString(numValue) : textValue; }
		public double getNumeric() { return numValue; }
		public void setText(String value) { textValue = value; }
		public void setNumeric(double value) { numValue = value; }
	}
}
