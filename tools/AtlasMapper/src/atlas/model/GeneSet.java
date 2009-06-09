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
package atlas.model;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.rpc.ServiceException;

import org.ebi.arrayexpress.AnyType2AnyTypeMapEntry;
import org.ebi.arrayexpress.AtlasWebServiceImplLocator;
import org.ebi.arrayexpress.AtlasWebServiceImplPortType;

public class GeneSet implements Serializable {
	private static final long serialVersionUID = 1897570987861021240L;
	
	Map<String, Gene> genes = new HashMap<String, Gene>();
	
	public GeneSet(String[] geneIds, String organism) throws RemoteException, ServiceException {
		AtlasWebServiceImplPortType port = new AtlasWebServiceImplLocator().getAtlasWebServiceImplPort();

		if(geneIds.length == 0) return; //No Ensembl genes on pathway;
		
		AnyType2AnyTypeMapEntry[][] results = port.batchQuery(
				geneIds, new String[0], organism, ""
		);

		Map<String, Gene> geneMap = new HashMap<String, Gene>();
		
		Map map = new HashMap();
		for(AnyType2AnyTypeMapEntry[] level1 : results) {
			//Build a map to easily access the properties
			map.clear();
			for(AnyType2AnyTypeMapEntry entry : level1) {
				map.put(entry.getKey(), entry.getValue());
			}
			
			String geneId = (String)map.get(Keys.KEY_GENE);
			String factorName = (String)map.get(Keys.KEY_FACTOR_NAME);
			String factorValue = (String)map.get(Keys.KEY_FACTOR_VALUE);
			String exp = (String)map.get(Keys.KEY_EXPERIMENT);
			int sign = (Integer)map.get(Keys.KEY_SIGN);
			double pvalue = (Double)map.get(Keys.KEY_PVALUE);
			
			assert(geneId != null);
			assert(factorName != null);
			assert(factorValue != null);
			assert(exp != null);
			
			Gene gene = geneMap.get(geneId);
			if(gene == null) {
				geneMap.put(geneId, gene = new Gene(geneId));
			}
			
			Factor factor = new Factor(factorName, factorValue);
			FactorData factorData = new FactorData(factor, pvalue, sign, exp);
			gene.addFactorData(factorData);
		}
		
		for(Gene gene : geneMap.values()) {
			genes.put(gene.getId(), gene);
		}
	}
	
	public Collection<Gene> getGenes() {
		return genes.values();
	}
	
	public Set<Factor> getFactors() {
		Set<Factor> factors = new TreeSet<Factor>();
		for(Gene g : genes.values()) {
			factors.addAll(g.getFactors());
		}
		return factors;
	}
	
	public Gene getGene(String id) {
		return genes.get(id);
	}
	
	public void insert(GeneSet geneSet) {
		genes.putAll(geneSet.genes);
	}
}
