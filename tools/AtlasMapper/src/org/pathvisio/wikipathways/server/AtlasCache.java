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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.GdbProvider;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdb.IDMapperRdb;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.Utils;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

import atlas.model.GeneSet;

public class AtlasCache {
	static final String CACHE_PATH = "cache_atlas/";
	static final String SEP_REV = "@";
	
	private PathwayCache pathwayCache;
	private String basePath;
	private GdbProvider gdbs;
	
	private long retention_time = -1;
	
	public AtlasCache(String basePath, PathwayCache pathwayCache, GdbProvider gdbs) {
		this.pathwayCache = pathwayCache;
		this.basePath = basePath;
		this.gdbs = gdbs;
		
		new File(basePath + "/" + CACHE_PATH).mkdirs();
	}
	
	public void setRetentionTime(long retention_time) {
		this.retention_time = retention_time;
	}
	
	public GeneSet getGeneSet(String id) throws FileNotFoundException, ServiceException, IOException, ConverterException, ClassNotFoundException, IDMapperException {
		WPPathway p = pathwayCache.getPathway(id);
		
		File cache = getCacheFile(p.getId(), p.getRevision());
		GeneSet genes = null;
		if(cache.exists() && CacheManager.checkCacheAge(cache, retention_time)) {
			Logger.log.trace("Reading atlas data from cache...");
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(cache));
			try {
				genes = (GeneSet)in.readObject();
			} catch(InvalidClassException e) { //Might happen if the class uid has changed
				cache.delete();
				genes = updateCache(p);
			}
			in.close();
		} else {
			Logger.log.trace("Downloading atlas data...");
			genes = updateCache(p);
		}
		return genes;
	}
	
	private GeneSet updateCache(WPPathway p) throws ServiceException, FileNotFoundException, IOException, IDMapperException {
		Pathway pathway = p.getPathway();
		Organism org = Organism.fromLatinName(
				pathway.getMappInfo().getOrganism()
		);
		DataSource ensDs = AtlasMapperServiceImpl.getEnsemblDataSource(org);
		
		if(org == null) {
			org = Organism.HomoSapiens;
			Logger.log.warn("No organism found in pahtway " + p.getId() + ", assuming human");
		}
		List<IDMapperRdb> gdbList = gdbs.getGdbs(org);
		
		//Get all ensembl genes on the pathway
		Set<String> ensIds = new HashSet<String>();
		for(Xref x : pathway.getDataNodeXrefs()) {
			if(x.getId() == null || x.getDataSource() == null) continue;
			for(IDMapperRdb gdb : gdbList) {
				for(Xref c : gdb.mapID(x, ensDs)) {
					ensIds.add(c.getId());
				}
			}
		}
		GeneSet atlasGenes = new GeneSet(
				ensIds.toArray(new String[ensIds.size()]), org.latinName()
		);
		if(atlasGenes.getGenes().size() == 0) {
			//Don't store cache for empty dataset
			return atlasGenes;
		}
		Logger.log.trace("Writing atlas cache...");
		File cache = getCacheFile(p.getId(), p.getRevision());
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cache));
		out.writeObject(atlasGenes);
		out.close();
		return atlasGenes;
	}
	
	private File getCacheFile(String id, String revision) {
		return new File(basePath + "/" + CACHE_PATH, id + SEP_REV + revision);
	}
	
	public void updateAllCache() {
		try {
			WSPathwayInfo[] pathways = pathwayCache.getClient().listPathways();
			int i = 0;
			for(WSPathwayInfo p : pathways) {
				Logger.log.trace(
					"MEM: "	+ (Runtime.getRuntime().totalMemory() -
					      Runtime.getRuntime().freeMemory()) / 1000000
				);
				Logger.log.info("Updating pathway " + i++ + "/" + pathways.length + "; " + p.getId() + 
						" (" + p.getName() + ", " + p.getSpecies() + ")");
				getGeneSet(p.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		PreferenceManager.init();
		BioDataSource.init();
		Logger.log.setLogLevel(true, true, true, true, true, true);
		if(args.length == 0) {
			System.err.println("Please specify directory containing AtlasMapper properties file.");
			System.exit(-1);
		}
		try {
			WikiPathwaysClient wpclient = new WikiPathwaysClient();
			File propFile = new File(args[0]);
			CacheManager cacheMgr = new CacheManager(propFile, wpclient);
			cacheMgr.getAtlasCache().updateAllCache();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
