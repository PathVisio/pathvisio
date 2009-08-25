package atlas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.xml.rpc.ServiceException;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.GdbProvider;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdb.IDMapperRdb;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.util.Utils;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.server.AtlasMapperServiceImpl;
import org.pathvisio.wikipathways.server.PathwayCache;
import org.pathvisio.wikipathways.server.WPPathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

import atlas.model.Factor;
import atlas.model.FactorData;
import atlas.model.Gene;
import atlas.model.GeneSet;



/**
 * Generates statistics on the AtlasMapper, by counting mappings on
 * current gene content on WikiPathways and experiments
 * on Atlas.
 * @author thomas
 */
public class AtlasStatistics {
	File cachePath;
	GdbProvider gdbProv;
	WikiPathwaysClient wpClient;

	PathwayCache pwCache;

	public AtlasStatistics(File cachePath, File gdbConfig) throws IDMapperException, IOException, ServiceException {
		this.cachePath = cachePath;
		gdbProv = GdbProvider.fromConfigFile(gdbConfig);
		wpClient = new WikiPathwaysClient();
		pwCache = new PathwayCache(cachePath.toString(), wpClient);
	}

	void printStatistics(Collection<WSPathwayInfo> pathways, Organism organism) throws ServiceException, IDMapperException, ConverterException, IOException, ClassNotFoundException {
		//A set of all unique Ensembl genes in the pathways
		DataSource orgEns = AtlasMapperServiceImpl.getEnsemblDataSource(organism);
		Set<DataSource> orgEnsSet = Utils.setOf(orgEns);
		File idsCache = new File(cachePath, organism + ".ids");
		Set<String> ensIds = new HashSet<String>();
		if(idsCache.exists()) {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(idsCache));
			ensIds = (Set<String>)in.readObject();
		} else {
			for(WSPathwayInfo wpi : pathways) {
				if(wpi.getSpecies().equals(organism.latinName())) {
					WPPathway wp = pwCache.getPathway(wpi.getId());
					Pathway p = wp.getPathway();
					for(Xref x : p.getDataNodeXrefs()) {
						if(x.getId() == null || x.getDataSource() == null) continue;
						for(IDMapperRdb gdb : gdbProv.getGdbs(Organism.fromLatinName(wpi.getSpecies()))) {
							for(Xref ens : gdb.mapID(x, orgEnsSet)) {
								ensIds.add(ens.getId());
							}
						}
					}
				}
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(idsCache));
			out.writeObject(ensIds);
			out.close();
		}
		System.out.println(organism + ": " + ensIds.size() + " genes in pathways");
		if(ensIds.size() == 0) return;

		GeneSet atlasGenes = null;
		File atlasCache = new File(cachePath, organism + ".atlas");
		if(atlasCache.exists()) {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(atlasCache));
			atlasGenes = (GeneSet)in.readObject();
		} else {
			Stack<String> idStack = new Stack<String>();
			idStack.addAll(ensIds);
			int j = 0;
			while(!idStack.empty()) {
				List<String> tmpIds = new ArrayList<String>(500);
				for(int i = 0; i < 500; i++) {
					if(idStack.empty()) break;
					tmpIds.add(idStack.pop());
				}
				Logger.log.info("Fetching atlas info for genes " + j * 500 + " to " + (j * 500 + tmpIds.size()) + " out of " + ensIds.size());
				if(atlasGenes == null) {
					atlasGenes = new GeneSet(tmpIds.toArray(new String[0]), organism.latinName());
				} else {
					atlasGenes.insert(new GeneSet(tmpIds.toArray(new String[0]), organism.latinName()));
				}
				j++;
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(atlasCache));
			out.writeObject(atlasGenes);
			out.close();
		}
		
		System.out.println(organism.latinName() + ": " + atlasGenes.getGenes().size() + " genes");
		System.out.println(organism.latinName() + ": " + atlasGenes.getFactors().size() + " factors");
		Set<String> experiments = new HashSet<String>();

		for(Gene g : atlasGenes.getGenes()) {
			for(Factor f : g.getFactors()) {
				for(FactorData fd : g.getFactorData(f)) {
					experiments.add(fd.getExperiment());
				}
			}
		}

		System.out.println(organism.latinName() + ": " + experiments.size() + " experiments");
	}

	public static void main(String[] args) {
		Logger.log.setLogLevel(true, true, true, true, true, true);
		try {
			File cachePath = new File(args[0]);
			File gdbConfig = new File(args[1]);
			cachePath.mkdirs();

			AtlasStatistics stats = new AtlasStatistics(cachePath, gdbConfig);

			WSPathwayInfo[] pws = stats.wpClient.listPathways();

			for(String orgName : stats.wpClient.listOrganisms()) {
				Organism org = Organism.fromLatinName(orgName);
				Set<WSPathwayInfo> orgPws = new HashSet<WSPathwayInfo>();
				//int i = 0;
				for(WSPathwayInfo wpi : pws) {
					if(wpi.getSpecies().equals(orgName)) orgPws.add(wpi);
					//if(i++ > 15) break; //Testing, only take first x
				}
				stats.printStatistics(orgPws, org);
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
