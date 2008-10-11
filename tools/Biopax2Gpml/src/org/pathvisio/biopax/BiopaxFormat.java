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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biopax.paxtools.io.jena.JenaIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.GroupStyle;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.view.MIMShapes;

import edu.emory.mathcs.backport.java.util.Collections;

/*
 * TODO: Implement a layout.
 * TODO: Add BioPAX information that can't be mapped to GPML to the <Biopax> element in the
 * GPML file.
 */
/**
 * BioPAX to GPML importer. This class provides the basic conversion from BioPAX
 * to GPML and can be subclassed to add additional conversions 
 * (e.g. to include Reactome specific comment properties).
 * @author thomas
 */
public class BiopaxFormat {
	static final String COMMENT_SRC = "biopax";

	Model bpModel;
	BioPAXFactory bpFactory;
	Map<String, Element> rdfid2element = new HashMap<String, Element>();

	/**
	 * Remembers all BioPAX elements that are converted to
	 * a GPML element.
	 */
	Map<BioPAXElement, PathwayElement> converted = new HashMap<BioPAXElement, PathwayElement>();
	
	/**
	 * Initialize the BioPAX importer for the given BioPAX file.
	 * Use {@link #convert()} to convert all pathways in the BioPAX file
	 * to GPML pathways.
	 */
	public BiopaxFormat(File biopaxFile) throws JDOMException, IOException {
		MIMShapes.registerShapes();
		
		//Read in JDOM to give access to the raw xml
		SAXBuilder builder = new SAXBuilder(false);
		Document bpDoc = builder.build(biopaxFile);
		Logger.log.info("Building RDF:ID map");
		mapRdfIds(bpDoc.getRootElement());
		Logger.log.info("Finished RDF:ID map");

		//Read in paxtools
		JenaIOHandler ioh = new JenaIOHandler();
		bpModel = ioh.convertFromOWL(new BufferedInputStream(
				new FileInputStream(biopaxFile)));
		bpModel.getLevel().getDefaultFactory();
	}

	XrefMapper xrefMapper;
	
	/**
	 * Set the xref mappings
	 * @see XrefMapper
	 */
	public void setXrefMapper(XrefMapper xrefMapper) {
		this.xrefMapper = xrefMapper;
	}
	
	protected XrefMapper getXrefMapper() {
		if(xrefMapper == null) xrefMapper = new DefaultXrefMapper();
		return xrefMapper;
	}
	
	StyleMapper styleMapper;
	
	/**
	 * Set the style mappings
	 * @see StyleMapper
	 */
	public void setStyleMapper(StyleMapper styleMapper) {
		this.styleMapper = styleMapper;
	}
	
	protected StyleMapper getStyleMapper() {
		if(styleMapper == null) styleMapper = new DefaultStyleMapper();
		return styleMapper;
	}
	
	/**
	 * Creates a mapping between all RDF:ID properties and their corresponding
	 * JDom Element. This method will recursively create mappings for all children
	 * of the given element.
	 */
	private void mapRdfIds(Element e) {
		Attribute a = e.getAttribute("ID", GpmlFormat.RDF);
		if(a != null) {
			String base = 
				e.getDocument().getRootElement().getAttributeValue("base", Namespace.XML_NAMESPACE);
			String rdfId = a.getValue();

			if(base != null) rdfId = base + "#" + rdfId;
			rdfid2element.put(rdfId, e);
		}
		
		for(Object o : e.getChildren()) {
			if(o instanceof Element) {
				mapRdfIds((Element)o);
			}
		}
	}
	
	/**
	 * Get the GPML element that maps to the given BioPAX element.
	 * @return The GPML element, or null if the BioPAX element has not been
	 * converted, or the element doesn't have a direct mapping.
	 * @see BiopaxFormat#isConverted(BioPAXElement)
	 */
	protected PathwayElement getConverted(BioPAXElement e) {
		return converted.get(e);
	}
	
	/**
	 * Mark the BioPAX element as converted. This will add a BiopaxRef
	 * to the given GPML element. After this method is called, {@link #isConverted(BioPAXElement)}
	 * will return true for the given BioPAX element.
	 * @param e The BioPAX element that will be marked as converted.
	 * @param p The pathway element that maps to the BioPAX element.
	 */
	protected void markConverted(BioPAXElement e, PathwayElement p) {
		if(p != null) {
			p.addBiopaxRef(e.getRDFId().substring(e.getRDFId().lastIndexOf('#') + 1));
		}
		converted.put(e, p);
	}
	
	/**
	 * Find out if a BioPAX element has already been converted to
	 * a GPML element.
	 */
	protected boolean isConverted(BioPAXElement e) {
		return converted.containsKey(e);
	}
	
	/**
	 * This method will be called for each element that doesn't have a
	 * mapping
	 * @param gpmlPathway
	 * @param o
	 */
	void noMapping(Pathway gpmlPathway, BioPAXElement o) {
		Logger.log.warn("No mapping found for " + o);
		String rdfId = o.getRDFId();
		markConverted(o, null);
		
		Element e = rdfid2element.get(rdfId);
		if(e != null) {
			try {
				gpmlPathway.getBiopaxElementManager().addPassiveElement(e);
			} catch (Exception ex) {
				Logger.log.error("Unable to create BiopaxElement", ex);
			}
		} else {
			Logger.log.warn("No element found for RDF:ID " + rdfId);
		}
	}

	/**
	 * Get a properly formatted text label to put on the
	 * GPML element. This method prefers the SHORT_NAME
	 * property. If that doesn't exist, it takes the NAME
	 * attribute. Finally, if the length of the resulting String
	 * is > 20, it tries to find a shorter label in the
	 * SYNONYM properties.
	 * @return The text label, or null if no NAME, SHORT_NAME or
	 * SYNONYM is available
	 */
	protected String getTextLabel(entity e) {
		String label = e.getNAME();
		//Prefer short name
		if(e.getSHORT_NAME() != null) {
			label = e.getSHORT_NAME();
		}
		//Try to find a shorter synonym if
		//the label is long
		if(label == null || label.length() > 20) {
			for(String s : e.getSYNONYMS()) {
				if(label == null || s.length() < label.length()) {
					label = s;
				}
			}
		}
		return label;
	}
	
	/**
	 * Convert the BioPAX model to a set of GPML Pathways.
	 * This methods creates a GPML pathway for each BioPAX pathway entity,
	 * by iterating over the pathwayStep properties and converting
	 * all underlying interactions and physicalEntities to GPML
	 * elements.
	 * @return A list of converted GPML pathways.
	 */
	public List<Pathway> convert() {
		Logger.log.info("Starting conversion of " + bpModel);

		List<Pathway> pathways = new ArrayList<Pathway>();

		for (BioPAXElement bpe : bpModel.getObjects(pathway.class)) {
			Logger.log.info("Found pathway: " + bpe);

			newPathway();
			Pathway gpmlPathway = new Pathway();
			pathways.add(gpmlPathway);
			
			pathway bpPathway = (pathway) bpe;

			// Map general pathway information
			String pathwayName = bpPathway.getRDFId().substring(bpPathway.getRDFId().lastIndexOf('#') + 1);
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
			gpmlPathway.setSourceFile(file);

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
					noMapping(gpmlPathway, bpc);
				}
			}

			layoutPathway(gpmlPathway, bpPathway);
		}

		return pathways;
	}

	/**
	 * Initializes variables that need to be refreshed every time a new pathway
	 * is encountered. This method is called from {@link #convert()} every time a
	 * new BioPAX pathway entity is found.
	 */
	protected void newPathway() {
		converted.clear();
	}

	/**
	 * Maps a BioPAX pathwayStep to GPML element(s) and marks
	 * it as converted.
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 */
	void mapPathwayStep(Pathway gpmlPathway, pathwayStep pws) {
		if(isConverted(pws)) return;
		Logger.log.info("Mapping pathwayStep: " + pws.getRDFId());
		for (process p : pws.getSTEP_INTERACTIONS()) {
			mapProcess(gpmlPathway, p);
		}
		markConverted(pws, null);
	}

	/**
	 * Maps a BioPAX process to GPML element(s) and marks it as
	 * converted.
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 */
	void mapProcess(Pathway gpmlPathway, process p) {
		if(isConverted(p)) return;
		Logger.log.info("Mapping process: " + p.getRDFId());
		if (p instanceof interaction) {
			mapInteraction(gpmlPathway, (interaction) p);
		} else if (p instanceof pathway){
			mapPathway(gpmlPathway, (pathway) p);
		}
	}

	/**
	 * Maps a BioPAX pathway entity to GPML element(s) and marks
	 * it as converted. A pathway entity will be converted to 
	 * a label in this method (this should become a Link in the
	 * future).
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 */
	void mapPathway(Pathway gpmlPathway, pathway p) {
		if(isConverted(p)) return;
		Logger.log.info("Mapping pathway " + p.getRDFId());
		
		PathwayElement link = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		link.setInitialSize();
		String name = getTextLabel(p);
		link.setTextLabel(name);
		gpmlPathway.add(link);
		markConverted(p, link);
	}
	
	/**
	 * Maps a BioPAX interaction to GPML element(s) and marks
	 * it as converted.
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 */
	void mapInteraction(Pathway gpmlPathway, interaction i) {
		if(isConverted(i)) return;
		Logger.log.info("Mapping interaction " + i.getRDFId());
		
		if (i instanceof conversion) {
			mapConversion(gpmlPathway, (conversion) i);
		} else if (i instanceof control){
			mapControl(gpmlPathway, (control) i);
		}
	}

	/**
	 * Maps a BioPAX control to GPML element(s) and marks
	 * it as converted. Depending on the number of
	 * CONTROLLER and CONTROLLED objects, a control may result in multiple GPML
	 * line objects, e.g.:
	 * 
	 * CONTROLLER = a,b,c    CONTROLLED = x,y
	 * 
	 * a ------------------------|
	 *                           v
	 * b ----------------------> x
	 *                           ^
	 * c ------------------------|
	 * 
	 * a ------------------------|
	 *                           v
	 * b ----------------------> y
	 *                           ^
	 * c ------------------------|
	 * 
	 * results in a total of 6 GPML lines.
	 * 
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 */
	protected void mapControl(Pathway gpmlPathway, control c) {
		/*
		 * TODO: the CONTROLLER -> CONTROLLED lines should use an anchored line
		 * to connect to the CONTROLLED instance:
		 * a --------------
		 *                 \
		 * b ---------------o------> x
		 *                 /
		 * c --------------
		 */
		if(isConverted(c)) return;
		Logger.log.info("Mapping control " + c.getRDFId());
		Set<physicalEntityParticipant> controller = c.getCONTROLLER();
		Set<process> controlled = c.getCONTROLLED();

		for(physicalEntityParticipant pe : controller) {
			PathwayElement pweController = mapPhysicalEntityParticipant(gpmlPathway, pe, false);
			for(process pr : controlled) {
				PathwayElement line = PathwayElement.createPathwayElement(ObjectType.LINE);
				
				getStyleMapper().mapControl(c, line);
				
				gpmlPathway.add(line);
				line.getMStart().linkTo(pweController, 0, 0);
				
				mapProcess(gpmlPathway, pr);
				PathwayElement prPwe = getConverted(pr);
				if(prPwe.getObjectType() == ObjectType.LINE) {
					MAnchor ma = prPwe.addMAnchor(0.4);
					line.getMEnd().linkTo(ma, 0, 0);
				} else {
					line.getMEnd().linkTo(prPwe, 0, 0);
				}
				markConverted(c, line);
			}
		}
	}
	
	/**
	 * Maps a BioPAX conversion to GPML element(s) and marks
	 * it as converted. The conversion may result in multiple
	 * GPML objects, e.g.:
	 * 
	 * LEFT = a, b	RIGHT = x, y
	 * 
	 * a -----o-------o-----> x
	 *       /         \
	 * b ----           ----> y
	 * 
	 * This will result in a main conversion line, linking the first
	 * LEFT with the first RIGHT property. Additional properties will be
	 * linked to the main line using anchors.
	 * 
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 */
	protected void mapConversion(Pathway gpmlPathway, conversion c) {
		if(isConverted(c)) return;
		Logger.log.info("Mapping conversion " + c.getRDFId());
		Iterator<physicalEntityParticipant> itLeft = c.getLEFT().iterator();
		Iterator<physicalEntityParticipant> itRight = c.getRIGHT().iterator();
		
		physicalEntityParticipant pLeft = c.getLEFT().size() > 0 ? itLeft.next() : null;
		physicalEntityParticipant pRight = c.getRIGHT().size() > 0 ? itRight.next() : null;
		
		PathwayElement pweLeft = null;
		PathwayElement pweRight = null;
		if(pLeft == null) {
			pweLeft = getStyleMapper().createUnknownParticipant();
			gpmlPathway.add(pweLeft);
		} else {
			pweLeft = mapPhysicalEntityParticipant(gpmlPathway, pLeft, false);
		}
		if(pRight == null) {
			pweRight = getStyleMapper().createUnknownParticipant();
			gpmlPathway.add(pweRight);
		} else {
			pweRight = mapPhysicalEntityParticipant(gpmlPathway, pRight, false);
		}
		//Create a line between the first input/output
		PathwayElement line = PathwayElement.createPathwayElement(ObjectType.LINE);
		getStyleMapper().mapConversionLeft(c, line);
		gpmlPathway.add(line);

		//Add additional input/output to anchors of the first line
		if (pweLeft != null && pweRight != null) {
			line.getMStart().linkTo(pweLeft, 0, 0);
			line.getMEnd().linkTo(pweRight, 0, 0);

			MAnchor anchorLeft = line.addMAnchor(0.3);
			while(itLeft.hasNext()) {
				physicalEntityParticipant pep = itLeft.next();
				PathwayElement pwe = mapPhysicalEntityParticipant(gpmlPathway, pep, false);
				if(pep != pLeft && pwe != null) {
					PathwayElement l = PathwayElement.createPathwayElement(ObjectType.LINE);
					getStyleMapper().mapConversionLeft(c, l);
					gpmlPathway.add(l);
					l.getMStart().linkTo(pwe, 0, 0);
					l.getMEnd().linkTo(anchorLeft, 0, 0);
				}
			}
			MAnchor anchorRight = line.addMAnchor(0.7);
			while(itRight.hasNext()) {
				physicalEntityParticipant pep = itRight.next();
				PathwayElement pwe = mapPhysicalEntityParticipant(gpmlPathway, pep, false);
				if(pep != pRight && pwe != null) {
					PathwayElement l = PathwayElement.createPathwayElement(ObjectType.LINE);
					getStyleMapper().mapConversionRight(c, l);
					gpmlPathway.add(l);
					l.getMStart().linkTo(anchorRight, 0, 0);
					l.getMEnd().linkTo(pwe, 0, 0);
				}
			}
		}
		
		markConverted(c, line);
	}

	/**
	 * Maps a BioPAX interactionParticipan to GPML element(s) and marks
	 * it as converted.
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 */
	PathwayElement mapInteractionParticipant(Pathway gpmlPathway,
			InteractionParticipant p) {
		if(isConverted(p)) return converted.get(p);
		Logger.log.info("Mapping interaction participant: " + p.getRDFId());
		PathwayElement pwElm = null;

		if (p instanceof physicalEntityParticipant) {
			pwElm = mapPhysicalEntityParticipant(gpmlPathway,
					(physicalEntityParticipant) p, false);
		} else {
			noMapping(gpmlPathway, p);
		}

		if (pwElm != null) {
			for (String cmt : p.getCOMMENT()) {
				pwElm.addComment(cmt, COMMENT_SRC);
			}
		}

		markConverted(p, pwElm);
		return pwElm;
	}

	/**
	 * Maps a BioPAX physicalEntityParticipant to GPML element(s) and marks
	 * it as converted.
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 * @param forceCreate If true, this method will always create a new GPML element instead
	 * of reusing the converted element, even if the BioPAX entity has been converted to a GPML 
	 * element before.
	 */
	PathwayElement mapPhysicalEntityParticipant(Pathway gpmlPathway,
			physicalEntityParticipant p, boolean forceCreate) {
		if(isConverted(p) && !forceCreate) return getConverted(p);
		Logger.log.info("Mapping physical entity participant: " + p.getRDFId());
		PathwayElement pe = mapPhysicalEntity(gpmlPathway, p.getPHYSICAL_ENTITY(), forceCreate || shouldReplicate(p.getPHYSICAL_ENTITY()));
		markConverted(p, pe);
		return pe;
	}

	/**
	 * Should this physicalEntity always create a replicate GPML element, even
	 * if it's already converted. If this method returns true, it will override the forceCreate
	 * parameter of {@link #mapPhysicalEntityParticipant(Pathway, physicalEntityParticipant, boolean)}.
	 * This can be used to specify that certain small molecules should be converted into a replicate GPML
	 * element for each reaction (e.g. ATP or ADP).
	 */
	protected boolean shouldReplicate(physicalEntity p) {
//		if("ATP".equals(p.getSHORT_NAME())) return true;
//		if("ADP".equals(p.getSHORT_NAME())) return true;
		return false;
	}
	
	/**
	 * Maps a BioPAX physicalEntity to GPML element(s) and marks
	 * it as converted.
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 * @param forceCreate If true, this method will always create a new GPML element instead
	 * of reusing the converted element, even if the BioPAX entity has been converted to a GPML 
	 * element before.
	 */
	PathwayElement mapPhysicalEntity(Pathway gpmlPathway, physicalEntity entity, boolean forceCreate) {
		if(isConverted(entity) && !forceCreate) return getConverted(entity);
		Logger.log.info("Mapping physical entity: " + entity.getRDFId());
		
		PathwayElement pwElm = null;
		if (entity instanceof complex) {
			Logger.log.trace("\tMapping complex: " + entity.getRDFId());
			pwElm = mapComplex(gpmlPathway, (complex) entity, true);
		} else {
			Logger.log.trace("\tMapping non-complex: " + entity.getRDFId());
			pwElm = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			pwElm.setInitialSize();
			String name = getTextLabel(entity);
			if (name != null) {
				pwElm.setTextLabel(name);
			}
			getStyleMapper().mapPhysicalEntity(entity, pwElm);
			getXrefMapper().mapXref(entity, pwElm);
			gpmlPathway.add(pwElm);
		}
		markConverted(entity, pwElm);
		return pwElm;
	}

	/**
	 * Maps a BioPAX complex to GPML element(s) and marks
	 * it as converted.
	 * @param gpmlPathway The GPML pathway to add the elements to
	 * @param pws The BioPAX entity
	 * @param forceCreate If true, this method will always create a new GPML element instead
	 * of reusing the converted element, even if the BioPAX entity has been converted to a GPML 
	 * element before.
	 */
	PathwayElement mapComplex(Pathway gpmlPathway, complex c, boolean forceCreate) {
		if(isConverted(c) && !forceCreate) return converted.get(c);
		Logger.log.info("Mapping complex: " + c.getRDFId());
		PathwayElement group = PathwayElement.createPathwayElement(ObjectType.GROUP);
		gpmlPathway.add(group);
		
		String name = getTextLabel(c);
		if (name != null) {
			group.setTextLabel(name);
		}
		group.setGroupStyle(GroupStyle.COMPLEX);
		String groupId = group.createGroupId();

		for(physicalEntityParticipant ep : c.getCOMPONENTS()) {
			PathwayElement groupElm = mapPhysicalEntityParticipant(gpmlPathway, ep, true);
			String currRef = groupElm.getGroupRef();
			if(currRef != null) {
				Logger.log.warn("Object already in group " + currRef + ", replacing with " + groupId);
			}
			groupElm.setGroupRef(groupId);
		}
		stackGroup(group);
		markConverted(c, group);
		return group;
	}
	
	/**
	 * Stacks all elements in a GPML group.
	 */
	protected void stackGroup(PathwayElement group) {
		Pathway p = group.getParent();
		if(p != null) {
			Set<PathwayElement> groupElements = p.getGroupElements(group.getGroupId());
			List<PathwayElement> sorted = new ArrayList<PathwayElement>(groupElements);
			Collections.sort(
					sorted,
					new Comparator<PathwayElement>() {
						public int compare(PathwayElement o1, PathwayElement o2) {
							int ot1 = o1.getObjectType();
							int ot2 = o2.getObjectType();
							if(ot1 == ot2 || (ot1 != ObjectType.GROUP && ot2 != ObjectType.GROUP)) {
								return o1.compareTo(o2);
							} else {
								return ot1 == ObjectType.GROUP ? 1 : -1;
							}
						}
					}
			);
			double mtop = group.getMTop();
			double cx = group.getMCenterX();
			
			for(PathwayElement ge : sorted) {
				ge.setMTop(mtop);
				ge.setMCenterX(cx);
				mtop += ge.getMHeight();
			}
		}
	}
	
	/**
	 * Subclasses may implement this method to provide additional layout for the 
	 * converted pathway.
	 * @param gpmlPathway
	 * @param bpPathway
	 */
	protected void layoutPathway(Pathway gpmlPathway, pathway bpPathway) {
		// layout may be implemented by subclasses
	}
}
