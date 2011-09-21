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
package org.wikipathways.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.rpc.ServiceException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.rdb.GdbProvider;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.indexer.GpmlIndexer;
import org.pathvisio.indexer.SourceProvider;
import org.wikipathways.client.WikiPathwaysCache;
import org.wikipathways.client.WikiPathwaysClient;

/**
 * Creates an index of all WikiPathways pathways, after
 * downloading them into a cache directory. Use the {@link #start()} method
 * to keep updating at a given interval.
 * @author thomas
 */
public class WikiPathwaysIndexer extends Timer{
	private GpmlIndexer indexer;
	private WikiPathwaysCache wikiCache;
	private IndexWriter writer;
	private GdbProvider gdbs;

	private URL rpcUrl;
	private File indexPath;
	private File cachePath;
	private int updateInterval = 30000; //30s by default
	private int optimizeInterval = 10; //Optimize every 10th update
	private Logger log = Logger.log;
	private boolean rebuild;

	private int optimizeCounter = 0;

	volatile boolean interrupt;

	/**
	 * Constructor for this class. Does not create the index yet, use
	 * update to create or update the existing index.
	 * @param indexDir	The path to the index
	 * @param cacheDir	The path to the directory where the GPML files will be cached
	 * @param rpcUrl	The url to the WikiPathways RPC service
	 * @param gdbs		The GdbProvider that provides the necessary gene databases to lookup
	 * the cross references for datanodes
	 * @throws ServiceException
	 * @throws ConverterException
	 */
	public WikiPathwaysIndexer(File indexPath, File cachePath, URL rpcUrl, GdbProvider gdbs) throws ServiceException, ConverterException {
		this.indexPath = indexPath;
		this.cachePath = cachePath;
		this.rpcUrl = rpcUrl;
		this.gdbs = gdbs;
		init();
	}

	public WikiPathwaysIndexer(File indexConfig, File gdbConfig) throws ServiceException, ConverterException {
		log.setLogLevel(true, true, true, true, true, true);
		parseIndexConfig(indexConfig);
		parseGdbConfig(gdbConfig);
		init();
	}

	public File getIndexPath() {
		return indexPath;
	}

	private void init() throws ServiceException, ConverterException {
		wikiCache = new WikiPathwaysCache(
				new WikiPathwaysClient(rpcUrl),
				cachePath
		);
		List<File> files = FileUtils.getFiles(cachePath, "gpml", true);
		Set<File> fileSet = new HashSet<File>();
		fileSet.addAll(files);

		indexer = new GpmlIndexer(null, fileSet, gdbs);
		indexer.setSourceProvider(new SourceProvider() {
			public String getSource(File gpmlFile) {
				String source = gpmlFile.getAbsolutePath();
				try {
					source = wikiCache.getPathwayInfo(gpmlFile).getUrl();
				} catch (Exception e) {
					log.error("Unable to get pathway url", e);
				}
				return source;
			}
		});
	}

	private void createWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		if(writer != null) {
			closeWriter();
		}
		writer = new IndexWriter(FSDirectory.getDirectory(indexPath), false, new StandardAnalyzer());

		indexer.setWriter(writer);
	}

	private boolean shouldOptimize() {
		return optimizeCounter >= optimizeInterval;
	}

	private void closeWriter() throws CorruptIndexException, IOException {
		if(writer != null) {
			if(shouldOptimize()) {
				log.info(optimizeCounter + " updates since last optimization, start optimizing index");
				optimizeCounter = 0;
				writer.optimize();
			}
			writer.close();
			//Set read/write permissions for index files
			for(File f : indexPath.listFiles()) {
				String cmd = "chmod 777 " + f.getAbsolutePath();
				Logger.log.info("Setting file permissions: " + cmd);
				Runtime.getRuntime().exec(cmd);
			}
		}
		writer = null;
	}

	/**
	 * Update the index. This method updates the WikiPathways cache and then updates
	 * pathways changed since the last index build. A new index will be automatically
	 * created if none exists yet.
	 * @throws CorruptIndexException
	 * @throws ConverterException
	 * @throws IOException
	 */
	public void update() throws CorruptIndexException, ConverterException, IOException {
		update(wikiCache.update());
	}

	void update(Collection<File> files) throws CorruptIndexException, LockObtainFailedException, IOException, ConverterException {
		if(files.size() > 0) {
			createWriter();
			int i = 1;
			for(File f : files) {
				log.info("Updating index for file " + i++ + " out of " + files.size() + "; " + f);
				indexer.update(f);
			}
			optimizeCounter++;
			closeWriter();
			log.info("Done updating");
		} else {
			log.trace("Nothing to update!");
		}
	}

	/**
	 * Rebuild the index. This method updates the index for all cached files.
	 * @throws ConverterException
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 */
	public void rebuild() throws CorruptIndexException, LockObtainFailedException, IOException, ConverterException {
		log.info("Rebuilding index");
		if(rebuild) {
			//Remove old index
			for(File f : indexPath.listFiles()) {
				f.delete();
			}
		}

		update(FileUtils.getFiles(cachePath, "gpml", true));
	}

	void start() {
		//Always close the index when java shuts down
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Logger.log.info("Shutdown signal received, closing index");
                try {
                	closeWriter();
                } catch(Exception e) {
                	Logger.log.error("Unable to close index writer", e);
                }
            }
        });

		if(rebuild) { //Start by rebuilding the complete index if needed
			try {
				rebuild();
			} catch(Exception e) {
				log.error("Unable to rebuild index", e);
			}
		}
		schedule(
				new TimerTask() {
					public void run() {
						try {
							update();
						} catch (Exception e) {
							log.error("Unable to update index", e);
						}
					}
				},
				0,
				updateInterval
		);
	}

	public static void main(String[] args) {
		if(args.length < 1) {
			printUsage();
			System.exit(-1);
		}

		try {
			BioDataSource.init();
			WikiPathwaysIndexer indexer = new WikiPathwaysIndexer(
					new File(args[0]),
					new File(args[1])
			);
			//Parse options
			for(int i = 2; i < args.length; i++) {
				if(args[i].equals("--rebuild")) {
					indexer.rebuild = true;
				}
			}

			indexer.start();
		} catch(Exception e) {
			Logger.log.error("Unable to start indexer", e);
			printUsage();
		}
	}

	void parseGdbConfig(File f) {
		try {
			gdbs = GdbProvider.fromConfigFile(f);
		} catch(Exception e) {
			configError("", e);
		}
	}

	void parseIndexConfig(File f) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line = in.readLine();
			while(line != null) {
				String[] kv = line.split(SEPERATOR);
				if(kv.length == 2) {
					String key = kv[0];
					String value = kv[1];
					if(KEY_RPCURL.equals(key)) {
						try {
							rpcUrl = new URL(value);
						} catch(MalformedURLException e) {
							configError(line, e);
						}
					} else if (KEY_CACHEPATH.equals(key)) {
						cachePath = new File(value);
					} else if (KEY_INDEXPATH.equals(key)) {
						indexPath = new File(value);
					} else if (KEY_UPDATEINTERVAL.equals(key)) {
						try {
							updateInterval = Integer.parseInt(value);
						} catch(NumberFormatException e) {
							configError(line, e);
						}
					} else if (KEY_ERRORLOG.equals(key)) {
						try {
							log.setStream(new PrintStream(value));
						} catch(Exception e) {
							configError("Unable to set error log: " + line, e);
							log.setStream(System.err);
						}
					} else if (KEY_OPTIMIZEINTERVAL.equals(key)) {
						try {
							optimizeInterval = Integer.parseInt(value);
						} catch(NumberFormatException e) {
							configError(line, e);
						}
					}
				} else {
					configError("Invalid key/value pair in index configuration: " + line, null);
				}
				line = in.readLine();
			}
			in.close();
		} catch(Exception e) {
			configError("", e);
		}
	}

	static void configError(String msg, Throwable e) {
		Logger.log.error("Error while parsing configuration file: " + msg, e);
	}

	static void printUsage() {
		System.out.println(
			"org.pathvisio.indexer <index_config> <gdb_config> [options]\n" +
			"- index_config: the index configuration file, see index.config for an example\n" +
			"- gdb_config: the gene database configuration file, see gdb.config for an example\n" +
			"Valid options are:\n" +
			"--rebuild:	Forces reindexing of all cached files\n"
		);
	}

	static final String SEPERATOR = "=";
	static final String KEY_RPCURL = "rpc_url";
	static final String KEY_UPDATEINTERVAL = "update_interval";
	static final String KEY_OPTIMIZEINTERVAL = "optimize_interval";
	static final String KEY_INDEXPATH = "index_path";
	static final String KEY_CACHEPATH = "cache_path";
	static final String KEY_ERRORLOG = "error_log";
	static final String DB_GLOBAL = "*";
}
