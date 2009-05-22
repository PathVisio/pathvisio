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
package org.pathvisio.util.swing;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.bridgedb.DataSource;
import org.bridgedb.Organism;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

/** 
 * File and schema version compatibilities checks
 * This collects some methods that are perhaps too hacky to run in the wild. 
 */
public class Compat implements Engine.ApplicationEventListener
{
	private final SwingEngine swingEngine;
	
	private Map<Organism, DataSource> ensSpecies = new HashMap<Organism, DataSource>();
	
	public Compat (SwingEngine swingEngine)
	{
		this.swingEngine = swingEngine;
		
		ensSpecies.put (Organism.HomoSapiens, DataSource.ENSEMBL_HUMAN);
		ensSpecies.put (Organism.CaenorhabditisElegans, DataSource.ENSEMBL_CELEGANS);
		ensSpecies.put (Organism.DanioRerio, DataSource.ENSEMBL_ZEBRAFISH);
		ensSpecies.put (Organism.DrosophilaMelanogaster, DataSource.ENSEMBL_ZEBRAFISH);
		ensSpecies.put (Organism.MusMusculus, DataSource.ENSEMBL_MOUSE);
		ensSpecies.put (Organism.RattusNorvegicus, DataSource.ENSEMBL_RAT);
		ensSpecies.put (Organism.SaccharomycesCerevisiae, DataSource.ENSEMBL_SCEREVISIAE);
	}
	
	private boolean usesOldEnsembl(Pathway pwy)
	{
		Organism org = Organism.fromLatinName(pwy.getMappInfo().getOrganism());
		if (!ensSpecies.containsKey(org))
			return false; // this pwy is not one of the species to be converted

		for (PathwayElement elt : pwy.getDataObjects())
		{
			if (elt.getObjectType() == ObjectType.DATANODE &&
					elt.getDataSource() == DataSource.ENSEMBL)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Ensembl considers each species database as separate,
	 * and thus they should have separate system codes as well.
	 * This method will convert generic Ensembl datanodes
	 * to species specific datanodes if possible.
	 */
	private void convertEnsembl(Pathway pwy)
	{
		Organism org = Organism.fromLatinName(pwy.getMappInfo().getOrganism());
		if (!ensSpecies.containsKey(org))
			return; // this pwy is not one of the species to be converted
		
		for (PathwayElement elt : pwy.getDataObjects())
		{
			if (elt.getObjectType() == ObjectType.DATANODE &&
					elt.getDataSource() == DataSource.ENSEMBL)
			{
				elt.setDataSource (ensSpecies.get (org));
			}
		}
		
	}

	public void applicationEvent(ApplicationEvent e) 
	{
		switch (e.getType())
		{
		case ApplicationEvent.PATHWAY_OPENED:
			{
				Pathway pwy = swingEngine.getEngine().getActivePathway();
				if (usesOldEnsembl(pwy))
				{
					int result = JOptionPane.showConfirmDialog(
							swingEngine.getFrame(), 
							"This Pathway uses the old style references to Ensembl.\nDo you want" +
							"to update this pathway?\n\n" +
							"This update is required if you want to use the latest gene databases.", 
							"Update pathway?", JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION)
					{
						convertEnsembl(pwy);
					}
				}
			}
		}
		
	}
	
}
