package org.pathvisio.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.pathvisio.data.DBConnDerby;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Organism;
import org.pathvisio.util.FileUtils;
import org.pathvisio.wikipathways.WikiPathwaysCache;
import org.pathvisio.wikipathways.WikiPathwaysClient;

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
	private Logger log = Logger.log;
	
	volatile boolean interrupt;
	
	/**
	 * Constructor for this class. Does not create the index yet, use
	 * update to create or update the existing index.
	 * @param indexDir	The path to the index
	 * @param cacheDir	The path to the directory where the GPML files will be cached
	 * @param rpcUrl	The url to the WikiPathways RPC service
	 * @param gdbs		The GdbProvider that provides the necessary gene databases to lookup
	 * the cross references for datanodes
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws ConverterException
	 */
	public WikiPathwaysIndexer(File indexPath, File cachePath, URL rpcUrl, GdbProvider gdbs) throws CorruptIndexException, LockObtainFailedException, IOException, ConverterException {
		this.indexPath = indexPath;
		this.cachePath = cachePath;
		this.rpcUrl = rpcUrl;
		this.gdbs = gdbs;
		init();
	}
	
	public WikiPathwaysIndexer(File indexConfig, File gdbConfig) throws CorruptIndexException, LockObtainFailedException, IOException, ConverterException {
		log.setLogLevel(true, true, true, true, true, true);
		parseIndexConfig(indexConfig);
		parseGdbConfig(gdbConfig);
		init();
	}
	
	private void init() throws CorruptIndexException, LockObtainFailedException, IOException, ConverterException {
		wikiCache = new WikiPathwaysCache(
				new WikiPathwaysClient(rpcUrl),
				cachePath
		);
		List<File> files = FileUtils.getFiles(cachePath, "gpml", true);
		Set<File> fileSet = new HashSet<File>();
		fileSet.addAll(files);
		
		writer = new IndexWriter(indexPath, new StandardAnalyzer());
		indexer = new GpmlIndexer(writer, fileSet, gdbs);
	}
	
	private void createWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		if(writer != null) writer.close();
		writer = new IndexWriter(indexPath, new StandardAnalyzer());
		indexer.setWriter(writer);
	}
	
	private void closeWriter() throws CorruptIndexException, IOException {
		if(writer != null) writer.close();
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
		List<File> updated = wikiCache.update();
		if(updated.size() > 0) {
			createWriter();
			for(File f : updated) {
				Logger.log.trace("Updating index for " + f);
				indexer.update(f);
			}
			closeWriter();
		} else {
			Logger.log.trace("Nothing to update!");
		}
	}
	
	void start() {
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
			WikiPathwaysIndexer indexer = new WikiPathwaysIndexer(
					new File(args[0]),
					new File(args[1])
			);
			indexer.start();
		} catch(Exception e) {
			Logger.log.error("Unable to start indexer", e);
			printUsage();
		}
	}
	
	
	void parseGdbConfig(File f) {
		Logger.log.info("Parsing gene database configuration: " + f);
		gdbs = new GdbProvider();
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line = in.readLine();
			while(line != null) {
				String[] kv = line.split("\t");
				if(kv.length == 2) {
					String key = kv[0];
					String value = kv[1];
					Organism org = Organism.fromLatinName(key);
					if(org != null) {
						DBConnector dbConn = new DBConnDerby();
						SimpleGdb gdb = new SimpleGdb(value, dbConn, DBConnector.PROP_NONE);
						gdbs.addOrganismGdb(org, gdb);
					} else if(DB_GLOBAL.equalsIgnoreCase(key)) {
						DBConnector dbConn = new DBConnDerby();
						SimpleGdb gdb = new SimpleGdb(value, dbConn, DBConnector.PROP_NONE);
						gdbs.addGlobalGdb(gdb);
					} else {
						Logger.log.warn("Unable to parse organism: " + key);
					}
				} else {
					configError("Invalid key/value pair in gene database configuration: " + line, null);
				}
				line = in.readLine();
			}
			in.close();
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
			"org.pathvisio.indexer <index_config> <gdb_config>\n" +
			"- index_config: the index configuration file, see index.config for an example\n" +
			"- gdb_config: the gene database configuration file, see gdb.config for an example\n"
		);
	}
	
	static final String SEPERATOR = "=";
	static final String KEY_RPCURL = "rpc_url";
	static final String KEY_UPDATEINTERVAL = "update_interval";
	static final String KEY_INDEXPATH = "index_path";
	static final String KEY_CACHEPATH = "cache_path";
	static final String KEY_ERRORLOG = "error_log";
	static final String DB_GLOBAL = "*";
}
