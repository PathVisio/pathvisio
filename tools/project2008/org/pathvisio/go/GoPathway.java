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

import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class GoPathway 
{
	private GoMap map;
	private GoReader reader;
	
	private void run (String[] args)
	{
		File obo = new File (args[0]);
		File mart = new File (args[1]);
		String goid = args[2];
		
		reader = new GoReader(obo);
		map = new GoMap(mart);

		Logger.log.info ("Go terms read: " + reader.getTerms().size());
		
		GoTerm term = reader.findTerm(goid);
		Pathway p = makeGoPathway (reader, map, term);
		
		try
		{
			p.writeToXml(new File ("/home/martijn/Desktop/gotest.gpml"), true);
		}
		catch (ConverterException e)
		{
			Logger.log.error ("",e);
		}
	}
	
	Pathway makeGoPathway (GoReader reader, GoMap map, GoTerm base)
	{
		Pathway result = new Pathway();

		double top = 1000.0;
		double left = 1000.0;

		PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.LABEL);
		
		pelt.setMCenterX(left);
		pelt.setMCenterY(top - DATANODEHEIGHT);
		pelt.setMWidth(DATANODEWIDTH * 2);
		pelt.setMHeight(DATANODEHEIGHT);		
		pelt.setTextLabel(base.getId() + " " + base.getName());
	
		result.add(pelt);

		Set<String> ensIds = map.getRefsRecursive(base);

		addIds (result, ensIds, left, top);
		
		return result;
	}
	
	static final int MAXCOLNUM = 10;
	static final double DATANODEWIDTH = 1600;
	static final double DATANODEHEIGHT = 300;
	static final double LABELWIDTH = 6000;
	static final double LABELHEIGHT = 250;
	static final double MARGIN = 100;
	static final double COLWIDTH = MAXCOLNUM * (DATANODEWIDTH + MARGIN);

	double addIds (Pathway p, Set<String> ensIds, double left, double top)
	{
		double xco = 0;
		double yco = 0;
		
		for (String ensId : ensIds)
		{
			PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			
			pelt.setMCenterX(left + xco);
			pelt.setMCenterY(top + yco);
			pelt.setMWidth(DATANODEWIDTH);
			pelt.setMHeight(DATANODEHEIGHT);
			
			pelt.setDataSource(DataSource.ENSEMBL);
			pelt.setGeneID(ensId);
			pelt.setDataNodeType(DataNodeType.GENEPRODUCT);
			pelt.setTextLabel(ensId);
		
			p.add(pelt);
			
			xco += DATANODEWIDTH + MARGIN;
			if (xco >= COLWIDTH)
			{
				xco = 0;
				yco += DATANODEHEIGHT;
			}
		}
		return yco + top;
	}
	
	
	public static void main(String [] args)
	{
		GoPathway pathway = new GoPathway();
		pathway.run (args);
		Logger.log.info ("DONE");
	}
}
