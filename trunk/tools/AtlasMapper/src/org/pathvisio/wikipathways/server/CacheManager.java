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
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;

import org.bridgedb.IDMapperException;
import org.bridgedb.rdb.GdbProvider;
import org.pathvisio.debug.Logger;
import org.pathvisio.wikipathways.WikiPathwaysClient;

public class CacheManager {
	static final String PROPS_FILE = "cache.props";
	static final String PROP_RETENTION_TIME = "retention-time";
	static final String PROP_CACHE_DIR = "base-directory";

	private PathwayCache pathwayCache;
	private GdbProvider gdbs;
	private AtlasCache atlasCache;
	private WikiPathwaysClient client;
	private ImageCache imageCache;
	private File propsPath;

	private Properties props;
	private long retention_time; //in milliseconds

	public CacheManager(File propsPath, WikiPathwaysClient client) {
		this.client = client;
		this.propsPath = propsPath;

		props = new Properties();

		//Read properties
		try {
			File propFile = new File(propsPath, PROPS_FILE);
			props.load(new FileInputStream(
					propFile)
			);
			Logger.log.info("Read properties from " + propFile);
		} catch (Exception e) {
			Logger.log.error("Unable to read properties, using defaults", e);
		}
		retention_time = Long.parseLong(props.getProperty(PROP_RETENTION_TIME, "-1"));
		if(retention_time > 0) {
			retention_time = retention_time * 1000L * 60L * 60L * 24L; //days to milliseconds
		}
	}

	public CacheManager(ServletContext servlet, WikiPathwaysClient client) {
		this(new File(servlet.getRealPath("")), client);
	}

	private WikiPathwaysClient getClient() {
		return client;
	}

	public File getCacheDir() {
		return new File(props.getProperty(PROP_CACHE_DIR, propsPath.toString()));
	}

	public long getRetentionTime() {
		return retention_time;
	}

	public ImageCache getImageCache() throws ServiceException, IOException, IDMapperException, ClassNotFoundException {
		if(imageCache == null) {
			imageCache = new ImageCache(
				getCacheDir().getAbsolutePath(),
				getPathwayCache(),
				getAtlasCache(),
				getGdbProvider()
			);
			imageCache.setRetentionTime(retention_time);
		}
		return imageCache;
	}

	public AtlasCache getAtlasCache() throws ServiceException, IOException, IDMapperException, ClassNotFoundException {
		if(atlasCache == null) {
			atlasCache = new AtlasCache(
					getCacheDir().getAbsolutePath(),
				getPathwayCache(),
				getGdbProvider()
			);
			atlasCache.setRetentionTime(retention_time);
		}
		return atlasCache;
	}

	public PathwayCache getPathwayCache() throws ServiceException {
		if(pathwayCache == null) {
			pathwayCache = new PathwayCache(
					getCacheDir().getAbsolutePath(),
				getClient()
			);
		}
		return pathwayCache;
	}

	public GdbProvider getGdbProvider() throws IOException, IDMapperException, ClassNotFoundException {
		if(gdbs == null) {
			gdbs = GdbProvider.fromConfigFile(
					new File(propsPath, "gdb.config")
			);
		}
		return gdbs;
	}

	/**
	 * Check if this cache file is out of date, and delete it if so.
	 * @return true if the cache file is still valid, false if not
	 */
	protected static boolean checkCacheAge(File cacheFile, long retention_time) {
		if(retention_time < 0) {
			return true;
		} else {
			long msMod = cacheFile.lastModified();
			Calendar c = GregorianCalendar.getInstance();
			long msNow = c.getTimeInMillis();
			if(msNow - msMod > retention_time) {
				cacheFile.delete();
				return false;
			}
			return true;
		}
	}
}
