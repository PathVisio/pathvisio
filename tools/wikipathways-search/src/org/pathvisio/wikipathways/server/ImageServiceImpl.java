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
package org.pathvisio.wikipathways.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.client.ResultsTable;

/**
 * A servlet that provides image data for the preview
 * images.
 * @author thomas
 */
public class ImageServiceImpl extends HttpServlet {
	/**
	 * Process a request. The url parameter should be either:
	 * - id, to get the image data for a given image id
	 * - updateCache, to update the cache for all images
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			if(!ImageManager.isInit()) {
				WikiPathwaysClient client = new WikiPathwaysClient(
						SearchServiceImpl.getClientUrl(getServletContext())
				);
				ImageManager.init(getServletContext().getRealPath(""), client);
			}
			ImageManager imgManager = ImageManager.getInstance();

			//Update the cache
			if(req.getParameter(ImageManager.PAR_UPDATE_CACHE) != null) {
				imgManager.updateAllCache();
				return;
			}

			//Else get the image
			String id = req.getParameter(ResultsTable.GET_ID);

		    byte[] data = imgManager.getImageData(id);
		    resp.setContentType("image/png");
		    resp.getOutputStream().write(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
