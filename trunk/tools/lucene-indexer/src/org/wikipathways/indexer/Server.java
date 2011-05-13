package org.wikipathways.indexer;

import java.io.File;
import java.io.IOException;

import javax.xml.rpc.ServiceException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.LockObtainFailedException;
import org.bridgedb.bio.BioDataSource;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.pathvisio.core.model.ConverterException;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Server that maintains a wikipathways index and publishes it through a REST service.
 * @author thomas
 */
public class Server {
	WikiPathwaysIndexer indexer;
	IndexService service;
	Component component;

	public Server(File indexConfig, File gdbConfig) throws ServiceException, ConverterException, CorruptIndexException, IOException {
		indexer = new WikiPathwaysIndexer(indexConfig, gdbConfig);
	}

	void startIndexer(boolean rebuild) throws CorruptIndexException, LockObtainFailedException, IOException, ConverterException {
		if(rebuild) {
			indexer.rebuild();
		} else {
			indexer.update();
		}
		indexer.start();
	}

	void startService(int port) throws Exception {
		component = new Component();
		component.getServers().add(Protocol.HTTP, port);
		IndexReader reader = IndexReader.open(indexer.getIndexPath().getAbsolutePath());
		component.getDefaultHost().attach(
				new IndexService(new WikiPathwaysSearcher(reader))
		);
		component.start();
	}

	public static void main(String[] args) {
		ServerParameters par = new ServerParameters();
		CmdLineParser parser = new CmdLineParser(par);
		try {
			parser.parseArgument(args);
		} catch(CmdLineException e) {
			e.printStackTrace();
			parser.printUsage(System.err);
			System.exit(-1);
		}

		BioDataSource.init();

		try {
			Server server = new Server(par.getIndexConfig(), par.getBridgeConfig());
			if(!par.isNoUpdate()) server.startIndexer(par.isRebuild());
			server.startService(par.getPort());
		} catch(Exception e) {
			e.printStackTrace();
			parser.printUsage(System.err);
		}
	}
}
