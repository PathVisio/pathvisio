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
import java.rmi.RemoteException;

import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

public class PathwayCache {
	static final String CACHE_PATH = "cache_gpml/";
	static final String SEP_REV = "@";

	WikiPathwaysClient client;
	private String basePath;

	public PathwayCache(String basePath, WikiPathwaysClient client) {
		this.client = client;
		this.basePath = basePath;
	}

	public WPPathway getPathway(String id) throws ConverterException, RemoteException {
		WSPathwayInfo info = client.getPathwayInfo(id);
		File cache = getCacheFile(id, info.getRevision());
		Pathway p = null;
		if(!cache.exists()) {
			p = updateCache(id, info.getRevision());
		}
		if(p == null) { //Load from cache
			p = new Pathway();
			p.readFromXml(cache, true);
		}
		return new WPPathway(id, info.getRevision(), p);
	}

	private Pathway updateCache(String id, String revision) throws RemoteException, ConverterException {
		Pathway p = WikiPathwaysClient.toPathway(client.getPathway(id));
		File f = getCacheFile(id, revision);
		f.getParentFile().mkdirs();
		p.writeToXml(f, true);
		return p;
	}

	private File getCacheFile(String id, String revision) {
		return new File(basePath + "/" + CACHE_PATH, id + SEP_REV + revision);
	}

	WikiPathwaysClient getClient() {
		return client;
	}
}
