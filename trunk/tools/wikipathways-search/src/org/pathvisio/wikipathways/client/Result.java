package org.pathvisio.wikipathways.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A search result for the WikiPathways search.
 * @author thomas
 */
public class Result implements IsSerializable {
	private String pathwayId;
	private String imageId;
	private String title;
	private String description;
	private String url;
	private int order;
	
	private Map<String, String[]> properties;
	
	public Result(String pathwayId, String title, String description, String url) {
		properties = new HashMap<String, String[]>();
		this.pathwayId = pathwayId;
		this.title = title;
		this.description = description;
		this.url = url;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	
	public void setProperty(String prop, String[] values) {
		properties.put(prop, values);
	}
	
	public void setPathwayId(String pathwayId) {
		this.pathwayId = pathwayId;
	}
	
	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getUrl() {
		return url;
	}

	public String getImageId() {
		return imageId;
	}
	
	public int getOrder() {
		return order;
	}
	
	public String getPathwayId() {
		return pathwayId;
	}
	
	public Map<String, String[]> getProperties() {
		return properties;
	}
	
	public Result() {
	}
}