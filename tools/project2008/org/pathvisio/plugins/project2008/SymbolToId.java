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
package org.pathvisio.plugins.project2008;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;
import org.pathvisio.model.XrefWithSymbol;
import org.pathvisio.wikipathways.WikiPathwaysClient;

/**
 * This program was used to convert two pathways that used the entrez symbol as gene id
 * (i.e. systemcodel L with id "HDAC1" instead of id 3065)
 *
 * I'll keep this script around as an example for how to use the WikiPathwaysClient
 * and gene databases.
 */
public class SymbolToId
{
	static final String[] PWY_NAMES =
	{	//"Homo_sapiens:Notch_signaling_KEGG",
//		"Homo_sapiens:Focal_adhesion_KEGG",
//		"Rattus_norvegicus:Gluthation_Metabolism_KEGG",
//		"Rattus_norvegicus:Regulation_of_Actin_Cytoskeleton_KEGG",
//		"Mus_musculus:Statin_Pathway_PharmGKB",
		"Rattus_norvegicus:Statin_Pathway_PharmGKB",

	};

	static final File DB_DIR = new File("/home/martijn/PathVisio-Data/gene databases");

	/**
	 * args[0] -> wikipathways username
	 * args[1] -> wikipathways password
	 */
	public static void main (String [] args) throws ConverterException, IOException
	{
		WikiPathwaysClient wp = new WikiPathwaysClient();
		wp.login (args[0], args[1]);

		LocalGdbManager localGdbManager = new LocalGdbManager(DB_DIR);

		for (String pwyName : PWY_NAMES)
		{
			File tmp = File.createTempFile(pwyName, ".gpml");
			int revision = wp.downloadPathway(pwyName, tmp);
			System.out.println (pwyName + ", revision: " + revision);

			Pathway pwy = new Pathway();
			pwy.readFromXml(tmp, true);
			String organism = pwy.getMappInfo().getOrganism();
			System.out.println ("Organism is " + organism);
			SimpleGdb gdb = localGdbManager.getDatabaseForOrganism(Organism.fromLatinName(organism));

			if (gdb == null)
			{
				System.out.println ("No suitable Gene Database found");
				return;
			}

			for (PathwayElement elt : pwy.getDataObjects())
			{
				if (elt.getObjectType() == ObjectType.DATANODE)
				{
					Xref ref = elt.getXref();
					try {
						if (!gdb.xrefExists(ref))
						{
							List<XrefWithSymbol> searchResult = gdb.freeSearch(elt.getTextLabel(), 1);
							if (searchResult.size() > 0)
							{
								XrefWithSymbol newtry = null;
								for (XrefWithSymbol result : searchResult)
								{
									if (elt.getTextLabel().equalsIgnoreCase(result.getSymbol()) &&
										result.getDataSource() == DataSource.ENTREZ_GENE)
									{
										newtry = result;
									}
								}

								if (newtry != null)
								{
									System.out.println (ref + " should be " + newtry + " " + newtry.getSymbol());
									elt.setDataSource(newtry.getDataSource());
									elt.setGeneID(newtry.getId());
								}
								else
								{
									System.out.println ("No alternative found for " + ref);
								}
							}
						}
					} catch (DataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			pwy.writeToXml(tmp, true);

			wp.uploadPathway(pwyName, tmp, revision, "Converted symbols to entrez id's");
		}

	}
}
