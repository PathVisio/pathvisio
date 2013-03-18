// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2013 BiGCaT Bioinformatics
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
package org.pathvisio.pluginmanager.impl.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.pathvisio.pluginmanager.impl.StatusMessage;

public class PVBundle implements Comparable<PVBundle> {

	private String name;
	private String symbolicName;
	private String shortDescription;
	private String description;
	private String source;
	private String website;
	private String faq;
	private String type; 

	private Boolean installed = false;
	private StatusMessage status;
	
	private List<Category> categories;
	
	public PVBundle() {
		categories = new ArrayList<Category>();
		status = new StatusMessage();
	}

	@XmlTransient
	public StatusMessage getStatus() {
		return status;
	}

	public void setStatus(StatusMessage status) {
		this.status = status;
	}

	public String getSymbolicName() {
		return symbolicName;
	}
	
	public String getName() {
		return name;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlTransient
	public Boolean isInstalled() {
		return installed;
	}

	public void setInstalled(Boolean installed) {
		this.installed = installed;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	
	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getFaq() {
		return faq;
	}

	public void setFaq(String faq) {
		this.faq = faq;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public int compareTo(PVBundle plugin) {
		return plugin.getName().compareTo(name);
	}
}
