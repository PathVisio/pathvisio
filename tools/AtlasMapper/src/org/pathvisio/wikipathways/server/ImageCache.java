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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.bridgedb.IDMapperException;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdb.GdbProvider;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ImageExporter;

import atlas.model.Factor;
import atlas.model.GeneSet;

public class ImageCache {
	static final String CACHE_PATH = "cache_images/";
	static final String SEP_REV = "@";
	public static final String GET_ID = "id";

	private PathwayCache pathwayCache;
	private AtlasCache atlasCache;
	private String basePath;
	private GdbProvider gdbs;

	private long retention_time = -1;

	public ImageCache(String basePath, PathwayCache pathwayCache, AtlasCache atlasCache, GdbProvider gdbs) {
		this.pathwayCache = pathwayCache;
		this.atlasCache = atlasCache;
		this.basePath = basePath;
		this.gdbs = gdbs;

		new File(basePath + "/" + CACHE_PATH).mkdirs();
	}

	public void setRetentionTime(long retention_time) {
		this.retention_time = retention_time;
	}

	public String getImageUrl(String pathwayId, List<Factor> factors) throws ConverterException, FileNotFoundException, ServiceException, IOException, ClassNotFoundException, IDMapperException {
		WPPathway pathway = pathwayCache.getPathway(pathwayId);
		GeneSet atlasGenes = atlasCache.getGeneSet(pathwayId);

		File cache = getCacheFile(pathway.getId(), pathway.getRevision(), factors);
		if(!cache.exists() || !CacheManager.checkCacheAge(cache, retention_time)) {
			Organism org = Organism.fromLatinName(pathway.getPathway().getMappInfo().getOrganism());
			AtlasVisualizer visualizer = new AtlasVisualizer(
				pathway.getPathway(),
				atlasGenes,
				factors,
				gdbs.getGdbs(org)
			);
			visualizer.export(new BatikImageExporter(ImageExporter.TYPE_PNG), cache);
		}
		return "getImage?" + GET_ID + "=" + cache.getName();
	}

	public byte[] getImageData(String id) throws IOException {
		return getBytesFromFile(getCacheFile(id));
	}

	/**
	 * Read the given file into a byte array.
	 */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        byte[] bytes = new byte[(int)length];
        // Read in the data
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        is.close();
        return bytes;
    }

	private File getCacheFile(String imageId) {
		return new File(basePath + "/" + CACHE_PATH, imageId);
	}

	private File getCacheFile(String id, String revision, List<Factor> factors) {
		String factorId = "";
		for(Factor f : factors) factorId += f.hashCode() + SEP_REV;
		return new File(basePath + "/" + CACHE_PATH, id + SEP_REV + revision + SEP_REV + factorId.hashCode());
	}
}
