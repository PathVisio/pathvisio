package org.pathvisio.wikipathways.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pathvisio.wikipathways.client.ResultsTable;

public class ImageServiceImpl extends HttpServlet {
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
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
