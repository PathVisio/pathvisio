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
	private String organism;
	private int order;
	
	private Map<String, String[]> properties;
	
	public Result(String pathwayId, String title, String description, String url, String organism) {
		properties = new HashMap<String, String[]>();
		this.pathwayId = pathwayId;
		this.title = title;
		this.description = description;
		this.url = url;
		this.organism = organism;
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
	
	public void setOrganism(String organism) {
		this.organism = organism;
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
	
	public String getOrganism() {
		return organism;
	}
	
	public Map<String, String[]> getProperties() {
		return properties;
	}
	
	public Result() {
	}
}