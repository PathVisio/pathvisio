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

import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AtlasMapperServiceAsync {
	public void getOrganisms(AsyncCallback<String[]> callback);
	public void getPathways(AsyncCallback<PathwayInfo[]> callback);
	public void getFactors(String pathway, AsyncCallback<FactorInfo[]> callback);
	public void getImageUrl(String pathway, String factorType, String[] factorValues, AsyncCallback<String> callback);
	public void getPathwayInfo(String pathway, AsyncCallback<PathwayInfo> callback);
	public void getGeneInfo(String pathway, String factorType, Set<String> factorValues, AsyncCallback<GeneInfo[]> callback);
}
