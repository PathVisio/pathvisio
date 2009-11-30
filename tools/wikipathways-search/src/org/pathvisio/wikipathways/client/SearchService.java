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


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The RPC service for the wikipathways search.
 * @author thomas
 */
@RemoteServiceRelativePath("wikipathwaysSearch")
public interface SearchService extends RemoteService {
	public static final int TIMEOUT = 60000;

	/**
	 * Perform a search.
	 * @param query The search query
	 * @return The results
	 */
	public Result[] search(Query query);

	/**
	 * Wait for a preview image to be converted on the server.
	 * This method will return after the image is generated, or after
	 * {@link #TIMEOUT} milliseconds have passed.
	 * @param id
	 */
	public void waitForImage(String id);

	/**
	 * Get the names for all supported database systems.
	 */
	public String[] getSystemNames();

	/**
	 * Get the names of all supported organisms.
	 */
	public String[] getOrganismNames();
}