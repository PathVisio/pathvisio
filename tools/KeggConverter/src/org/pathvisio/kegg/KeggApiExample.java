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
package org.pathvisio.kegg;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.LinkDBRelation;

/**
 * Example on how to use the KEGG API
 * @author thomas
 *
 */
public class KeggApiExample {

	/**
	 * Start with simple main method that lists all
	 * genes for a given enzyme-code/organism combination
	 */
	public static void main(String[] args) throws Exception {
		//Check the number of command line arguments
		if(args.length != 2) {
			System.err.println(
					"Invalid number of input arguments\n" +
					"Usage:\n" +
					"\tjava org.pathvisio.kegg.Converter [ec-code] [organism]\n\n" +
					"where [ec-code] is a valid enzyme code (e.g. 'ec:2.3.3.1')\n" +
					"and [organism] is a valid KEGG organism code (e.g. 'hsa')\n" +
					"For a complete list of organism codes, see:\n" +
					"http://www.genome.jp/kegg/catalog/org_list.html"
			);
			System.exit(1);
		}

		//Get the specified organism name
		String ec = args[0];
		String species = args[1];

		//Setup a connection to KEGG
		KEGGLocator  locator = new KEGGLocator();
		KEGGPortType serv;
		serv = locator.getKEGGPort();

		//Fetch the gene names
		String[] genes = serv.get_genes_by_enzyme(ec, species);

		//Print out the information to the screen
		if(genes.length == 0) {
			System.out.println("No genes found for " + ec + " (" + species + ")");
		} else {
			System.out.println("Enzyme " + ec + " (" + species + ") maps to the following genes: ");

			for(String gene : genes) {
				System.out.println("\t" + gene);
				LinkDBRelation[] links = serv.get_linkdb_by_entry(gene, "NCBI-GeneID", 1, 100);
				for(LinkDBRelation ldb : links) {
					System.out.println("\t\t" + ldb.getEntry_id1() + " links to " + ldb.getEntry_id2());
				}
				System.out.println("btit says: " + serv.btit(gene));
			}
		}
	}

}
