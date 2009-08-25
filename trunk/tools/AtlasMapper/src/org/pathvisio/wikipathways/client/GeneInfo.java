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
package org.pathvisio.wikipathways.client;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GeneInfo implements IsSerializable {
	private String label;
	private String id;
	private String geneLink;
	private String geneLinkName;
	private String warehouseLink;
	
	private double[] bounds; //Relative bounds
	Map<String, Set<ExpressionValue>> data;
	
	public GeneInfo(String label, String id, Map<String, Set<ExpressionValue>> data, double[] bounds) {
		this.label = label;
		this.data = data;
		this.bounds = bounds;
		this.id = id;
	}
	
	public void setGeneLink(String geneLink) {
		this.geneLink = geneLink;
	}
	
	public void setWarehouseLink(String warehouseLink) {
		this.warehouseLink = warehouseLink;
	}
	
	public String getGeneLink() {
		return geneLink;
	}
	
	public String getGeneLinkName() {
		return geneLinkName;
	}
	
	public void setGeneLinkName(String geneLinkName) {
		this.geneLinkName = geneLinkName;
	}
	
	public String getWarehouseLink() {
		return warehouseLink;
	}
	
	public Map<String, Set<ExpressionValue>> getData() {
		return data;
	}
	
	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public double getLeft() {
		return bounds[0];
	}
	
	public double getRight() {
		return bounds[2];
	}
	
	public double getTop() {
		return bounds[1];
	}
	
	public double getBottom() {
		return bounds[3];
	}
	
	public GeneInfo() {
	}
}
