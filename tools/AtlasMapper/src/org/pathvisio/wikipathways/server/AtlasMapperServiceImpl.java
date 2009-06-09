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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.bridgedb.IDMapperRdb;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayWrapperBase;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.client.AtlasMapperService;
import org.pathvisio.wikipathways.client.ExpressionValue;
import org.pathvisio.wikipathways.client.FactorInfo;
import org.pathvisio.wikipathways.client.GeneInfo;
import org.pathvisio.wikipathways.client.PathwayInfo;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

import atlas.model.Factor;
import atlas.model.FactorData;
import atlas.model.Gene;
import atlas.model.GeneSet;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AtlasMapperServiceImpl extends RemoteServiceServlet implements AtlasMapperService {
	private ClientManager clientMgr;
	private CacheManager cacheMgr;
	
	private ClientManager getClientManager() {
		if(clientMgr == null) {
			clientMgr = new ClientManager(getServletContext());
		}
		return clientMgr;
	}
	
	private CacheManager getCacheManager() throws ServiceException {
		if(cacheMgr == null) {
			cacheMgr = new CacheManager(
				getServletContext(),
				getClient()
			);
		}
		return cacheMgr;
	}
	
	private WikiPathwaysClient getClient() throws ServiceException {
		return getClientManager().getClient();
	}
	
	public AtlasMapperServiceImpl() {
		PreferenceManager.init();
	}

	public String[] getOrganisms() {
		try {
			return getClient().listOrganisms();
		} catch (Exception e) {
			Logger.log.error("Unable to list organisms", e);
			throw new RuntimeException(e);
		}
	}
	
	public PathwayInfo[] getPathways() {
		try {
			WSPathwayInfo[] result = getClient().listPathways();
			PathwayInfo[] pathways = new PathwayInfo[result.length];
			for(int i = 0; i < pathways.length; i++) {
				pathways[i] = new PathwayInfo(
					result[i].getId(),
					result[i].getName(),
					result[i].getSpecies()
				);
			}
			return pathways;
		} catch (Exception e) {
			Logger.log.error("Unable to get pathways", e);
			throw new RuntimeException(e);
		}
	}
	
	public FactorInfo[] getFactors(String pathwayId) {
		try {
			GeneSet atlasGenes = getCacheManager().getAtlasCache().getGeneSet(pathwayId);
			
			Set<Factor> factors = atlasGenes.getFactors();
			Map<String, Set<String>> factorInfo = new HashMap<String, Set<String>>();
			for(Factor f : factors) {
				Set<String> values = factorInfo.get(f.getName());
				if(values == null) {
					factorInfo.put(f.getName(), values = new HashSet<String>());
				}
				values.add(f.getValue());
			}
			
			FactorInfo[] result = new FactorInfo[factorInfo.size()];
			int i = 0;
			for(String name : factorInfo.keySet()) {
				result[i++] = new FactorInfo(
						name, factorInfo.get(name).toArray(new String[0])
				);
			}
			return result;
		} catch(Exception e) {
			Logger.log.error("Unable to get factors", e);
			throw new RuntimeException(e);
		}
	}
	
	public String getImageUrl(String pathway, String factorType,
			String[] factorValues) {
		try {
			List<Factor> factors = new ArrayList<Factor>();
			for(String fv : factorValues) {
				factors.add(new Factor(factorType, fv));
			}
			return getCacheManager().getImageCache().getImageUrl(
				pathway, factors
			);
		} catch(Exception e) {
			Logger.log.error("Unable to get image url", e);
			throw new RuntimeException(e);
		}
	}
	
	public PathwayInfo getPathwayInfo(String pathway) {
		try {
			WSPathwayInfo wsi = getClient().getPathwayInfo(pathway);
			return new PathwayInfo(
				wsi.getId(),
				wsi.getName(),
				wsi.getSpecies()
			);
		} catch(Exception e) {
			Logger.log.error("Unable to get pathway info", e);
			throw new RuntimeException(e);
		}
	}
	
	public GeneInfo[] getGeneInfo(String pathwayId, String factorType, Set<String> factorValues) {
		try {
			GeneSet atlasGenes = getCacheManager().getAtlasCache().getGeneSet(pathwayId);
			Pathway pathway = getCacheManager().getPathwayCache().getPathway(pathwayId).getPathway();
			//To calculate pathway size
			VPathway vPathway = new VPathway(new VPathwayWrapperBase());
			vPathway.fromModel(pathway);
			Dimension vsize = vPathway.calculateVSize();
			
			double[] msize = new double[2];
			msize[0] = vPathway.mFromV(vsize.getWidth());
			msize[1] = vPathway.mFromV(vsize.getHeight());
			
			Organism org = Organism.fromLatinName(pathway.getMappInfo().getOrganism());
			List<IDMapperRdb> gdbs = getCacheManager().getGdbProvider().getGdbs(org);

			List<GeneInfo> genes = new ArrayList<GeneInfo>();

			for(PathwayElement pwe : pathway.getDataObjects()) {
				if(pwe.getObjectType() == ObjectType.DATANODE) {
					double[] bounds = new double[4];
					bounds[0] = pwe.getMLeft() / msize[0];
					bounds[1] = pwe.getMTop() / msize[1];
					bounds[2] = (pwe.getMLeft() + pwe.getMWidth()) / msize[0];
					bounds[3] = (pwe.getMTop() + pwe.getMHeight()) / msize[1];
					
					Set<Xref> xrefs = new HashSet<Xref>();
					for(IDMapperRdb gdb : gdbs) {
						xrefs.addAll(gdb.getCrossRefs(pwe.getXref(), BioDataSource.ENSEMBL));
					}
					
					for(Xref x : xrefs) {
						Map<String, Set<ExpressionValue>> data = new HashMap<String, Set<ExpressionValue>>();
						Gene atlasGene = atlasGenes.getGene(x.getId());
						if(atlasGene == null) continue; //No atlas data for this gene
						
						Set<String> experiments = new HashSet<String>();
						
						for(Factor f : atlasGene.getFactors()) {
							if(!factorValues.contains(f.getValue())) {
								continue; //Skip this data if not a selected factor
							}
							Set<FactorData> fds = atlasGene.getFactorData(f);
							for(FactorData fd : fds) {
								Set<ExpressionValue> values = data.get(f.getValue());
								if(values == null) {
									data.put(f.getValue(), values = new HashSet<ExpressionValue>());
								}
								values.add(new ExpressionValue(fd.getPvalue(), fd.getSign(), fd.getExperiment()));
								if(fd.getExperiment() != null) experiments.add(fd.getExperiment());
							}
						}
						
						GeneInfo gi = new GeneInfo(
							pwe.getTextLabel() + " (" + pwe.getXref() + ")",
							x.getId(),
							data,
							bounds
						);
						
						gi.setEnsemblLink("http://www.ensembl.org/" + 
								org.latinName().replace(' ', '_') + "/Gene/Summary?g=" + x.getId());
						
						String exp = "";
						for(String e : experiments) exp += e + ", ";
						
						gi.setWarehouseLink(
								"http://www.ebi.ac.uk/microarray-as/aew/DW?queryFor=gene&gene_query=" +
								x.getId() + "&" +
								"species=" + org.latinName() + 
								"&displayInsitu=on&exp_query=" + exp
						);
						genes.add(gi);
					}
				}
			}
			return genes.toArray(new GeneInfo[genes.size()]);
		} catch(Exception e) {
			Logger.log.error("Unable to get gene information", e);
			throw new RuntimeException(e);
		}
	}
}
