// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.biopax;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biopax.paxtools.io.jena.JenaIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MAnchor;

public class BiopaxFormat {
	static final String COMMENT_SRC = "biopax";

	Model bpModel;
	BioPAXFactory bpFactory;

	public BiopaxFormat(File biopaxFile) throws FileNotFoundException {
		JenaIOHandler ioh = new JenaIOHandler();
		bpModel = ioh.convertFromOWL(new BufferedInputStream(
				new FileInputStream(biopaxFile)));
		bpModel.getLevel().getDefaultFactory();
	}

	void noMapping(Object o) {
		Logger.log.warn("No mapping found for " + o);
	}

	public List<Pathway> convert() {
		Logger.log.info("Starting conversion of " + bpModel);

		List<Pathway> pathways = new ArrayList<Pathway>();
		for (BioPAXElement bpe : bpModel.getObjects(pathway.class)) {
			Logger.log.info("Found pathway: " + bpe);
			newPathway();

			pathway bpPathway = (pathway) bpe;

			Pathway gpmlPathway = new Pathway();
			pathways.add(gpmlPathway);

			// Map general pathway information
			String pathwayName = bpPathway.getSHORT_NAME() == null ? bpPathway.getNAME() : bpPathway.getSHORT_NAME();
			if (pathwayName != null) {
				if (pathwayName.length() > 50) {
					pathwayName = pathwayName.substring(0, 49);
				}
				gpmlPathway.getMappInfo().setMapInfoName(pathwayName);
			}
			File file = new File(gpmlPathway.getMappInfo()
					.getMapInfoName().replace(' ', '_')
					+ ".gpml"
			);
			if(file.canWrite()) {
				gpmlPathway.setSourceFile(file);
			}

			Organism organism = Organism.fromLatinName(bpPathway.getORGANISM()
					.getNAME());
			if (organism != null) {
				gpmlPathway.getMappInfo().setOrganism(organism.latinName());
			}

			// Map the pathway components
			for (pathwayComponent bpc : bpPathway.getPATHWAY_COMPONENTS()) {
				Logger.log.info("Pathway component: " + bpc);
				if (bpc instanceof pathwayStep) {
					mapPathwayStep(gpmlPathway, (pathwayStep) bpc);
				} else {
					noMapping(bpc);
				}
			}

			layoutPathway(gpmlPathway, bpPathway);
		}

		return pathways;
	}

	/**
	 * Initializes variables that need to be refreshed every time a new pathway
	 * is encountered.
	 */
	protected void newPathway() {
		pep2pwelm.clear();
	}

	protected void layoutPathway(Pathway gpmlPathway, pathway bpPathway) {
		// may be implemented by subclasses
	}

	void mapPathwayStep(Pathway gpmlPathway, pathwayStep pws) {
		Logger.log.info("Mapping pathwayStep: " + pws);
		for (process p : pws.getSTEP_INTERACTIONS()) {
			mapProcess(gpmlPathway, p);
		}
	}

	void mapProcess(Pathway gpmlPathway, process p) {
		Logger.log.info("Mapping process: " + p);
		if (p instanceof interaction) {
			mapInteraction(gpmlPathway, (interaction) p);
		} else {
			noMapping(p);
		}
	}

	void mapInteraction(Pathway gpmlPathway, interaction i) {
		Logger.log.info("Mapping interaction " + i);

		for (InteractionParticipant p : i.getPARTICIPANTS()) {
			mapInteractionParticipant(gpmlPathway, p);
		}
		if (i instanceof conversion) {
			mapConversion(gpmlPathway, (conversion) i);
		} else {
			noMapping(i);
		}
	}

	protected void mapConversion(Pathway gpmlPathway, conversion c) {
		physicalEntityParticipant pLeft = c.getLEFT().size() > 0 ? c.getLEFT().iterator().next() : null;
		physicalEntityParticipant pRight = c.getRIGHT().size() > 0 ? c.getRIGHT().iterator().next() : null;

		PathwayElement pweLeft = getPhysicalEntityElement(pLeft);
		PathwayElement pweRight = getPhysicalEntityElement(pRight);

		if (pweLeft != null && pweRight != null) {
			PathwayElement line = PathwayElement.createPathwayElement(ObjectType.LINE);
			gpmlPathway.add(line);
			line.getMStart().linkTo(pweLeft, 0, 0);
			line.getMEnd().linkTo(pweRight, 0, 0);

			MAnchor anchorLeft = line.addMAnchor(0.3);
			for(physicalEntityParticipant pep : c.getLEFT()) {
				PathwayElement pwe = getPhysicalEntityElement(pep);
				if(pep != pLeft && pwe != null) {
					PathwayElement l = PathwayElement.createPathwayElement(ObjectType.LINE);
					gpmlPathway.add(l);
					l.getMStart().linkTo(pwe);
					l.getMEnd().linkTo(anchorLeft);
				}
			}
			MAnchor anchorRight = line.addMAnchor(0.7);
			for(physicalEntityParticipant pep : c.getRIGHT()) {
				PathwayElement pwe = getPhysicalEntityElement(pep);
				if(pep != pRight && pwe != null) {
					PathwayElement l = PathwayElement.createPathwayElement(ObjectType.LINE);
					gpmlPathway.add(l);
					l.getMStart().linkTo(anchorRight);
					l.getMEnd().linkTo(pwe);
				}
			}
		}
	}

	PathwayElement mapInteractionParticipant(Pathway gpmlPathway,
			InteractionParticipant p) {
		Logger.log.info("Mapping interaction participant: " + p);
		PathwayElement pwElm = null;

		if (p instanceof physicalEntityParticipant) {
			pwElm = mapPhysicalEntityParticipant(gpmlPathway,
					(physicalEntityParticipant) p);
		} else {
			noMapping(p);
		}

		if (pwElm != null) {
			for (String cmt : p.getCOMMENT()) {
				pwElm.addComment(cmt, COMMENT_SRC);
			}
		}

		return pwElm;
	}

	PathwayElement mapPhysicalEntityParticipant(Pathway gpmlPathway,
			physicalEntityParticipant p) {
		
		PathwayElement pwElm = getPhysicalEntityElement(p);
			
		if(pwElm == null) {
			pwElm = mapPhysicalEntity(gpmlPathway, p.getPHYSICAL_ENTITY());
			if (pwElm != null) {
				pep2pwelm.put(p, pwElm);
				// TODO: mappings
			}
		}
		return pwElm;
	}

	Map<physicalEntityParticipant, PathwayElement> pep2pwelm = new HashMap<physicalEntityParticipant, PathwayElement>();

	protected final PathwayElement getPhysicalEntityElement(
			physicalEntityParticipant e) {
		return pep2pwelm.get(e);
	}

	PathwayElement mapPhysicalEntity(Pathway gpmlPathway, physicalEntity entity) {
		PathwayElement pwElm = null;
		if (entity instanceof complex) {
			Logger.log.trace("Mapping complex: " + entity);
			// TODO: map complex, return group
		} else {
			Logger.log.trace("Mapping non-complex: " + entity);
			pwElm = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			pwElm.setInitialSize();
			String name = entity.getSHORT_NAME();
			if (name == null) {
				name = entity.getSHORT_NAME();
			}
			if (name != null) {
				pwElm.setTextLabel(name);
			}
			gpmlPathway.add(pwElm);
		}
		return pwElm;
	}

}
