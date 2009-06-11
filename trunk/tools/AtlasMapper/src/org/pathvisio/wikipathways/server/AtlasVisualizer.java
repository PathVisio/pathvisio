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

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperRdb;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ColorExporter;
import org.pathvisio.wikipathways.client.AtlasMapper;

import atlas.model.Factor;
import atlas.model.FactorData;
import atlas.model.Gene;
import atlas.model.GeneSet;

/**
 * Visualizes atlas data on a pathway
 * @author thomas
 */
public class AtlasVisualizer {
	Pathway pathway;
	GeneSet atlasGenes;
	List<Factor> factors;
	List<IDMapperRdb> gdbs;
	Organism organism;
	
	public AtlasVisualizer(Pathway pathway, GeneSet atlasGenes, List<Factor> factors, List<IDMapperRdb> gdbs) {
		this.pathway = pathway;
		this.atlasGenes = atlasGenes;
		this.factors = factors;
		this.gdbs = gdbs;
		this.organism = Organism.fromLatinName(pathway.getMappInfo().getOrganism());
		PreferenceManager.init();
	}
	
	public void export(BatikImageExporter exporter, File file) throws ConverterException, IDMapperException {
		Map<Xref, Color[]> xrefColors = createColorMap();
		Map<PathwayElement, Set<Xref>> ensMap = createEnsemblMap();
		
		Map<PathwayElement, List<Color>> colors = new HashMap<PathwayElement, List<Color>>();
		for(PathwayElement pwElm : ensMap.keySet()) {
			Set<Xref> xrefs = ensMap.get(pwElm);
			for(Xref xref : xrefs) {
				Color[] xc = xrefColors.get(xref);
				if(xc != null) {
					List<Color> elmColors = colors.get(pwElm);
					if(elmColors == null) {
						colors.put(pwElm, elmColors = Arrays.asList(xc));
					} else {
						//Average
						for(int i = 0; i < elmColors.size(); i++) {
							elmColors.set(i, averageColor(elmColors.get(i), xc[i]));
						}
					}
				}
			}
		}
		
		ColorExporter colorExporter = new ColorExporter(pathway, colors);
		colorExporter.export(exporter, file);
	}
	
	private Map<Xref, Color[]> createColorMap() {
		Map<Xref, Color[]> xrefColors = new HashMap<Xref, Color[]>();
		
		DataSource ens = AtlasMapper.getEnsemblDataSource(organism);
		
		Collection<Gene> genes = atlasGenes.getGenes();
		for(Gene gene : genes) {
			Xref xref = new Xref(gene.getId(), ens);
			Color[] colors = new Color[factors.size()];
			//Initial color is white
			for(int i = 0; i < colors.length; i++) colors[i] = Color.white;
			for(Factor f : gene.getFactors()) {
				int i = factors.indexOf(f);
				if(i >= 0) { //Use this factor
					Set<FactorData> fds = gene.getFactorData(f);
					//Try to get average p-value
					double avg_p = Double.NaN;
					int last_sign = 0;
					for(FactorData fd : fds) {
						//Trigger gray if signs disagree
						if(last_sign != 0 && fd.getSign() != last_sign) {
							avg_p = Double.NaN;
							break;
						}
						last_sign = fd.getSign();
						if(Double.isNaN(avg_p)) {
							avg_p = fd.getPvalue();
						} else {
							avg_p += fd.getPvalue();
						}
					}
					
					//If the signs disagree, use gray as color
					if(Double.isNaN(avg_p)) {
						colors[i] = Color.gray;
					} else {
						colors[i] = getColor(last_sign, avg_p);
					}
				}

			}
			xrefColors.put(xref, colors);
		}
		
		return xrefColors;
	}
	
	private Map<PathwayElement, Set<Xref>> createEnsemblMap() throws IDMapperException {
		DataSource ens = AtlasMapper.getEnsemblDataSource(organism);
		Map<PathwayElement, Set<Xref>> ensMap = new HashMap<PathwayElement, Set<Xref>>();
		for(PathwayElement pwElm : pathway.getDataObjects()) {
			if(pwElm.getObjectType() == ObjectType.DATANODE) {
				Xref xref = pwElm.getXref();
				Set<Xref> ensRefs = new HashSet<Xref>();
				
				for(int i = 0; i < gdbs.size(); i++) {
					ensRefs.addAll(gdbs.get(i).getCrossRefs(xref, ens));
				}
				
				ensMap.put(pwElm, ensRefs);
			}
		}
		return ensMap;
	}
	
	private Color getColor(int updn, double pvalue) {
		//Color from red (down, low p) to blue (up, low p)
		if(updn < 0) { //Down
			int rg = (int)(255 * pvalue);
			return new Color(rg, rg, 255);
		} else {
			int gb = (int)(255 * pvalue);
			return new Color(255, gb, gb);
		}
	}
	
	private Color averageColor(Color c1, Color c2) {
		return new Color(
				(c1.getRed() + c2.getRed()) / 2,
				(c1.getGreen() + c2.getGreen()) / 2,
				(c1.getBlue() + c2.getBlue()) / 2
		);
	}
}
