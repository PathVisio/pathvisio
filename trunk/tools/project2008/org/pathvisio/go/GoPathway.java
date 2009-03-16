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
package org.pathvisio.go;

import java.io.File;
import java.util.Set;

import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.SimpleGdbFactory;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.GroupStyle;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

/**
 * Run GoPathway with the following command line arguments (5 or more):
 * 
 * Argument 1: file name of gene ontology obo definition file. The latest one can be downloaded from 
 * 	http://www.geneontology.org/GO.downloads.ontology.shtml
 * 
 * Argument 2: a mapping of ensembl to gene ontology, which can be downloaded from 
 * Ensembl BioMart (http://www.ensembl.org/biomart/martview/)
 * This should be a tab-delimited text file with 
 * exactly one header row, with the following columns:
 * 	Column 1: Ensembl gene ID
 *  Column 2: GO ID
 *  Any more columns are ignored if present.
 *  (NB: Make sure you don't have a column with Ensembl transcript ID's)
 *  
 * Argument 3: file name of a PathVisio pgdb database for the correct species.
 * 
 * Argument 4: output directory, This should be an existing directory
 * 
 * Argument 5 and on: GO ID to make pathways of
 * 
 * For example
 * 
		/home/martijn/db/go/gene_ontology.obo
		/home/martijn/Desktop/rest/hs_e50_goid_mart.txt
		"/home/martijn/PathVisio-Data/gene databases/Hs_Derby_20080102_ens_workaround.pgdb"
		/home/martijn/Desktop
		GO:0045086
		GO:0051260
		GO:0045404
		GO:0030154
 */
public class GoPathway 
{
	private GoMap map;
	private GoReader reader;
	
	private void run (String[] args) throws DataException
	{
		File obo = new File (args[0]);
		File mart = new File (args[1]);
		String gdbname = args[2]; 
		File destDir = new File (args[3]);

		if (!obo.exists() || !mart.exists() || !destDir.isDirectory())
		{
			throw new IllegalArgumentException();
		}

		Gdb gdb = SimpleGdbFactory.createInstance(gdbname, new DataDerby(), 0); 
		for (int i = 4; i < args.length; ++i)
		{
			String goid = args[i];
			
			
			reader = new GoReader(obo);
			map = new GoMap(mart);
	
			Logger.log.info ("Go terms read: " + reader.getTerms().size());
			
			GoTerm term = reader.findTerm(goid);
			Logger.log.info (goid);
			if (term == null) throw new NullPointerException();
			
			Pathway p = makeGoPathway (reader, map, term, gdb);
			p.getMappInfo().setAuthor("Martijn van Iersel");
			p.getMappInfo().setMapInfoDataSource("Gene Ontology");
			p.getMappInfo().setEmail("martijn.vaniersel@bigcat.unimaas.nl");
			
			String name = "Hs_GO_" + term.getName();
			if (name.length() >= 50) name = name.substring (0, 50);
			
			p.getMappInfo().setMapInfoName(name);
			
			try
			{
				p.writeToXml(new File (destDir, "Hs_GO_" + term.getName() + ".gpml"), true);
			}
			catch (ConverterException e)
			{
				Logger.log.error ("",e);
			}
		}
	}
	
	Pathway makeGoPathway (GoReader reader, GoMap map, GoTerm base, Gdb gdb)
	{
		Pathway result = new Pathway();

		double top = 1000.0;
		double left = 1000.0;

		addIds (result,  base, left, top, gdb, null);
		
		return result;
	}
	
	static final int MAXCOLNUM = 10;
	static final double DATANODEWIDTH = 1600;
	static final double DATANODEHEIGHT = 300;
	static final double LABELWIDTH = 6000;
	static final double LABELHEIGHT = 250;
	static final double MARGIN = 100;
	static final double COLWIDTH = MAXCOLNUM * (DATANODEWIDTH + MARGIN);

	double addIds (Pathway p, GoTerm term, double left, double top, Gdb gdb, String parentGroup)
	{
		// ignore if there are no genes corresponding to this tree.
		if (map.getRefsRecursive(term).size() == 0) return top; 
		
		Set<String> ensIds = map.getRefs(term);

		double xco = 0;
		double yco = 0;

		PathwayElement group = PathwayElement.createPathwayElement(ObjectType.GROUP);
		group.setGroupStyle(GroupStyle.COMPLEX);
		group.setTextLabel(term.getId());

		if (parentGroup != null)
		{
			group.setGroupRef(parentGroup);
		}
		
		p.add (group);
		String groupRef = group.createGroupId();
		
		PathwayElement label = PathwayElement.createPathwayElement(ObjectType.LABEL);
		
		label.setMCenterX(left + LABELWIDTH / 2);
		label.setMCenterY(top + LABELHEIGHT / 2);
		label.setMWidth(LABELWIDTH);
		label.setMHeight(LABELHEIGHT);		
		label.setTextLabel(term.getId() + " " + term.getName());
		label.setGroupRef(groupRef);
		
		top += LABELHEIGHT + MARGIN;
		
		p.add(label);
		
		for (String ensId : ensIds)
		{
			PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			
			Xref ref = new Xref (ensId, DataSource.ENSEMBL);
			String symbol = null;
			try
			{
				symbol = gdb.getGeneSymbol(ref);
			}
			catch (DataException ex)
			{
				Logger.log.warn ("Failed lookup of gene symbol", ex);
			}
			if (symbol == null) symbol = ensId;
			
			pelt.setMCenterX(left + xco + DATANODEWIDTH / 2);
			pelt.setMCenterY(top + yco + DATANODEHEIGHT / 2);
			pelt.setMWidth(DATANODEWIDTH);
			pelt.setMHeight(DATANODEHEIGHT);
			
			pelt.setDataSource(DataSource.ENSEMBL);
			pelt.setGeneID(ensId);
			pelt.setDataNodeType(DataNodeType.GENEPRODUCT);
			pelt.setTextLabel(symbol);
			pelt.setGroupRef (groupRef);
			
			p.add(pelt);
			
			xco += DATANODEWIDTH + MARGIN;
			
			if (xco >= COLWIDTH)
			{
				xco = 0;
				yco += DATANODEHEIGHT;
			}
		}
		
		double bottom = top + yco;
		
		if (ensIds.size() > 0)
		{
			bottom += DATANODEHEIGHT * 2;
		}
		
		for (GoTerm child : term.getChildren())
		{
			bottom = addIds (p, child, left + 500, bottom, gdb, groupRef);
		}
		
		return bottom + DATANODEHEIGHT;
	}
	
	
	public static void main(String [] args) throws DataException
	{
		GoPathway pathway = new GoPathway();
		pathway.run (args);
		Logger.log.info ("DONE");
	}
}
