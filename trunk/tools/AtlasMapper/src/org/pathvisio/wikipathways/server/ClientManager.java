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
package org.pathvisio.wikipathways.server;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;

import org.pathvisio.debug.Logger;
import org.pathvisio.wikipathways.WikiPathwaysClient;

public class ClientManager {
	private WikiPathwaysClient client;
	private ServletContext servlet;

	public ClientManager(ServletContext servlet) {
		this.servlet = servlet;
	}

	public WikiPathwaysClient getClient() throws ServiceException {
		if(client == null) {
			client = new WikiPathwaysClient(getClientUrl(servlet));
		}
		return client;
	}

	private URL getClientUrl(ServletContext servlet) {
		URL url = null;
		try {
			url = new URL(
					"http://www.wikipathways.org/wpi/webservice/webservice.php"
			);
			Properties prop = new Properties();
			prop.load(new FileInputStream(
					new File(servlet.getRealPath(""), "wikipathways.props"))
			);
			String wsurl = (String)prop.get("webservice-url");
			if(wsurl != null) {
				url = new URL(wsurl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Logger.log.trace("Client url: " + url);
		return url;
	}
}
