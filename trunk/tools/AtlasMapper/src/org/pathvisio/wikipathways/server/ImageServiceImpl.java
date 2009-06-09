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
package org.pathvisio.wikipathways.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.ServiceException;

import org.bridgedb.IDMapperException;
import org.pathvisio.wikipathways.WikiPathwaysClient;

/**
 * A servlet that provides image data for the preview
 * images.
 * @author thomas
 */
public class ImageServiceImpl extends HttpServlet {
	private CacheManager cacheMgr;
	private ClientManager clientMgr;
	
	private WikiPathwaysClient getClient() throws ServiceException {
		if(clientMgr == null) {
			clientMgr = new ClientManager(getServletContext());
		}
		return clientMgr.getClient();
	}
	
	private CacheManager getCacheManager() throws ServiceException {
		if(cacheMgr == null) {
			cacheMgr = new CacheManager(
				getServletContext(),
				getClient()
			);
		}
		return cacheMgr;
	}
	
	private ImageCache getImageCache() throws ServiceException, IOException, IDMapperException {
		return getCacheManager().getImageCache();
	}
	
	/**
	 * Process a request. The url parameter should be either:
	 * - id, to get the image data for a given image id
	 * - updateCache, to update the cache for all images
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			//Update the cache
			if(req.getParameter(PAR_UPDATE_CACHE) != null) {
				getCacheManager().getAtlasCache().updateAllCache();
				return;
			}
			
			String id = req.getParameter(ImageCache.GET_ID);
			
			byte[] data = getImageCache().getImageData(id);
		    resp.setContentType("image/png");
		    resp.getOutputStream().write(data); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static final String PAR_UPDATE_CACHE = "updateCache";
}
