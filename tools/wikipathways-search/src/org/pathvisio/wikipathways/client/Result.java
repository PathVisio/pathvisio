// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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

	private Map<String, String[]> properties;

	/**
	 * Create a search result.
	 * @param pathwayId The identifier of the resulting pathway
	 * @param title The title that should be displayed for this result
	 * @param description The description that should be displayed for this result
	 * @param url The url of the resulting pathway
	 * @param organism The organism of the resulting pathway
	 */
	public Result(String pathwayId, String title, String description, String url, String organism) {
		properties = new HashMap<String, String[]>();
		this.pathwayId = pathwayId;
		this.title = title;
		this.description = description;
		this.url = url;
		this.organism = organism;
	}

	/**
	 * Set the image id, that is used to retrieve the preview
	 * image from the server.
	 */
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	/**
	 * Set a custom property
	 * @param prop The property name
	 * @param values The property values
	 */
	public void setProperty(String prop, String[] values) {
		properties.put(prop, values);
	}

	/**
	 * Set the pathway id of the resulting pathway.
	 */
	public void setPathwayId(String pathwayId) {
		this.pathwayId = pathwayId;
	}

	/**
	 * Set the organism of the resulting pathway;
	 */
	public void setOrganism(String organism) {
		this.organism = organism;
	}

	/**
	 * Get the title to display
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Get the description to display
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the url of the resulting pathway
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Get the id of the preview image
	 */
	public String getImageId() {
		return imageId;
	}

	/**
	 * Get the identifier of the resulting pathway
	 */
	public String getPathwayId() {
		return pathwayId;
	}

	/**
	 * Get the organism of the resulting pathway
	 */
	public String getOrganism() {
		return organism;
	}

	/**
	 * Get the custom properties
	 */
	public Map<String, String[]> getProperties() {
		return properties;
	}

	public Result() {
	}
}