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
package org.pathvisio.plugins;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.LinkDBRelation;

/**
 * Generates Putative Pathway Parts based on a 
 * HMDB metabolic network parsed and stored in MySQL by Andra.
 */
public class HelloKegg {

	public static void main(String[] args) throws Exception {
        KEGGLocator    locator  = new KEGGLocator();
        KEGGPortType   serv     = locator.getKEGGPort();

        String         query    = "ncbi-geneid:8854";
        String[] results  = null;
        String keggid = null;
        keggid = serv.bconv(query);
        System.out.println(serv.btit(keggid));
        String[] bconvoutput = keggid.split("\t");
        System.out.println(bconvoutput[1]);
        String eccode[] = serv.get_enzymes_by_gene(bconvoutput[1]);
        results = serv.get_compounds_by_enzyme(eccode[0]);
        for (int i = 0; i < results.length; i++) {
                System.out.println(serv.btit(results[i]));
                String btitTextLabel = serv.btit(results[i]);
    	    	String[] textLabel = btitTextLabel.split(" ");
    	    	LinkDBRelation[] chebiId = serv.get_linkdb_by_entry(textLabel[0], "chebi", 1, 100);
    	    	System.out.println(chebiId[0].getEntry_id1());
        }
	}
}
