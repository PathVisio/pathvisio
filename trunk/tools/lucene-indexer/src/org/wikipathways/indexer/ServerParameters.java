package org.wikipathways.indexer;

import java.io.File;

import org.kohsuke.args4j.Option;

public class ServerParameters {
	@Option(name = "-i", required = true, usage = "The index configuration file.")
	private File indexConfig;

	@Option(name = "-b", required = true, usage = "The bridgedb configuration file.")
	private File bridgeConfig;

	@Option(name = "-p", required = true, usage = "The port to start the server on.")
	private int port;

	@Option(name = "-rebuild", required = false, usage = "Force a fresh rebuild of the index.")
	private boolean rebuild;

	@Option(name = "-noupdate", required = false, usage = "Don't update the index, only start the rest service.")
	private boolean noUpdate;

	public File getIndexConfig() {
		return indexConfig;
	}

	public File getBridgeConfig() {
		return bridgeConfig;
	}

	public boolean isRebuild() {
		return rebuild;
	}

	public int getPort() {
		return port;
	}

	public boolean isNoUpdate() {
		return noUpdate;
	}
}
