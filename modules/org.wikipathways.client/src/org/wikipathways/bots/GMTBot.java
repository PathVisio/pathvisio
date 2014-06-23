package org.wikipathways.bots;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Properties;

import org.bridgedb.rdb.GdbProvider;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.PreferenceManager;
import org.wikipathways.bots.utils.GenerateGMT;

/**
 * Bot creates a GMT file 
 * Gene Set file format
 * @author martina
 *
 */
public class GMTBot extends Bot {

	private static final String PROP_GDBS = "gdb-config";
	GdbProvider gdbs;
	
	public GMTBot(Properties props) throws BotException {
		super(props);
		
		File gdbFile = new File(props.getProperty(PROP_GDBS));
		try {
			gdbs = GdbProvider.fromConfigFile(gdbFile);
		} catch (Exception e) {
			throw new BotException(e);
		}
	}
	
	@Override
	public BotReport createReport(Collection<Result> result) {
		BotReport report = new BotReport(
				new String[] {
					"Entrez Gene ids"
				}
			);
		
		report.setTitle("GMT file generation report");
		report.setDescription("GMT bot creates GMT file");
		return null;
	}

	@Override
	public String getTagName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Result scanPathway(File pathwayFile) throws BotException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting GMTBot");
			PreferenceManager.init();
			Properties props = new Properties();
			props.load(new FileInputStream(new File(args[0])));
			GMTBot bot =  new GMTBot(props);

			File output = new File(args[1]);
			
			GenerateGMT gmt = new GenerateGMT(bot.gdbs, bot.getClient());
			
			String result = gmt.createGMTFile(bot.getCache().getFiles());
			FileWriter writer = new FileWriter(output);
			writer.write(result);
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
			printUsage();
		}
	}

	static private void printUsage() {
		System.out.println(
			"Usage:\n" +
			"java org.pathvisio.wikipathways.bots.GMTBot propsfile reportfilename\n" +
			"Where:\n" +
			"-propsfile: a properties file containing the bot properties\n" +
			"-reportfilename: the base name of the file that will be used to write reports to " +
			"(extension will be added automatically)\n"
		);
	}
}
