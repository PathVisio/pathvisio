//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.pathvisio.kegg;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import keggapi.Definition;
import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.LinkDBRelation;

import org.bridgedb.bio.Organism;
import org.pathvisio.core.model.ConverterException;

public class KeggService {
	private static KEGGLocator keggLocator = new KEGGLocator();
	private static KEGGPortType keggPortType;

	private static KeggService instance;

	static KeggService getInstance() throws ServiceException {
		if(instance == null) {
			instance = new KeggService();
		}
		return instance;
	}

	private KeggService() throws ServiceException {
		keggPortType = keggLocator.getKEGGPort();
	}

	/**
	 * Fetches the organism specific NCBI gene identifiers for the enzyme code
	 * @throws ConverterException
	 * @throws RemoteException
	 */
	String[] getGenesForEc(String ec, Organism organism) throws RemoteException, ConverterException {
		Set<String> genes = new HashSet<String>();

		//Fetch the kegg gene IDs
		String[] keggGenes = keggPortType.get_genes_by_enzyme(ec, Util.getKeggOrganism(organism));
		if(keggGenes != null) {
			for(String kg : keggGenes) {
				//KEGG code --> NCBI code
				LinkDBRelation[] links = keggPortType.get_linkdb_by_entry(kg, "NCBI-GeneID", 1, 1000);
				for(LinkDBRelation ldb : links) {
					genes.add(ldb.getEntry_id2().substring(12));
				}
			}
		}
		return genes.toArray(new String[genes.size()]);
	}

	String[] getGenesForKo(String ko, Organism organism) throws RemoteException, ConverterException {
		Set<String> genes = new HashSet<String>();

		Definition[] keggGenes = keggPortType.get_genes_by_ko(ko, Util.getKeggOrganism(organism));
		if(keggGenes != null) {
			for(Definition def : keggGenes) {
				LinkDBRelation[] links = keggPortType.get_linkdb_by_entry(
						def.getEntry_id(), "NCBI-GeneID", 1, 1000
				);
				for(LinkDBRelation ldb : links) {
					genes.add(ldb.getEntry_id2().substring(12));
				}
			}
		}

		return genes.toArray(new String[genes.size()]);
	}

	String getKeggSymbol(String geneId) throws RemoteException, ConverterException {
		String result = keggPortType.btit(geneId);
		result = result.replaceAll(geneId + " ", ""); //Results starts with query + space, remove
		String[] data = result.split("; "); //Subsequent results are separated by '; '
		if(data.length > 1) {
			result = data[0].substring(0, data[0].length()); //Pick the first synonym
		} else {
			result = geneId;
		}
		return result;
	}
}