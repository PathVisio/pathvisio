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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConnectorType;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.GpmlFormatAbstract;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.model.MLine;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.ShapeType;
import org.pathvisio.core.model.ConnectorShape.Segment;
import org.pathvisio.core.model.ConnectorShape.WayPoint;
import org.pathvisio.core.model.GraphLink.GraphIdContainer;
import org.pathvisio.core.model.PathwayElement.MAnchor;
import org.pathvisio.core.model.PathwayElement.MPoint;
import org.pathvisio.core.view.LinAlg;
import org.pathvisio.core.view.MIMShapes;
import org.pathvisio.core.view.LinAlg.Point;
import org.pathvisio.kegg.KeggService.SymbolInfo;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import dtd.kegg.Entry;
import dtd.kegg.Graphics;
import dtd.kegg.Pathway;
import dtd.kegg.Product;
import dtd.kegg.Reaction;
import dtd.kegg.Relation;
import dtd.kegg.Substrate;
import dtd.kegg.Subtype;

/**
 * File converter for the KGML, the kegg pathway format.
 */
public class KeggFormat {
	static {
		MIMShapes.registerShapes();
	}

	static final String COMMENT_SOURCE = "KeggConverter";
	private static final String KEGG_ID = "KeggId";
	private static final String CONVERSION_DATE = "ConversionDate";

	private static final SimpleDateFormat CONVERSION_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

	private double spacing = 2;

	private KeggService keggService;
	private String species;
	private String speciesCode;
	private Pathway pathway; //Main pathway
	private Pathway map; //Used only to improve species specific pathway

	private int prefSymbolIndex = 0;
	private boolean symbolByGene = true;
	
	private org.pathvisio.core.model.Pathway gpmlPathway;

	private Map<String, PathwayElement> id2gpml = new HashMap<String, PathwayElement>();
	private Map<String, PathwayElement> reaction2gpml = new HashMap<String, PathwayElement>();
	private Set<String> addedMapLinks = new HashSet<String>();
	private SetMultimap<String, String> reaction2entry = new HashMultimap<String, String>();

	
	//Contains extra information from relation elements, on which compound
	//is linked to which reaction (in case there are multiple compounds with the same
	//name on the map)
	private SetMultimap<String, String> ecrelEnzyme2Compound = new HashMultimap<String, String>();

	private SetMultimap<String, String> entryName2Ids  = new HashMultimap<String, String>();

	private Map<String, Entry> entriesById = new HashMap<String, Entry>();

	public KeggFormat(Pathway pathway, String organism) {
		this.pathway = pathway;
		this.species = organism;
	}

	public KeggFormat(Pathway map, Pathway ko, String organism) {
		this(ko, organism);
		this.map = map;
	}

	public void setSpacing(double spacing) {
		this.spacing = spacing;
	}

	public void setPrefSymbolIndex(int prefSymbolIndex) {
		this.prefSymbolIndex = prefSymbolIndex;
	}
	
	public void setSymbolByGene(boolean symbolByGene) {
		this.symbolByGene = symbolByGene;
	}
	
	public void setUseWebservice(boolean use) throws ServiceException {
		if(use) {
			keggService = KeggService.getInstance();
		} else {
			keggService = null;
		}
	}

	boolean isUseWebservice() {
		return keggService != null;
	}

	public org.pathvisio.core.model.Pathway convert() throws RemoteException, ConverterException, ClassNotFoundException, IDMapperException {
		id2gpml.clear();
		addedMapLinks.clear();
		gpmlPathway = new org.pathvisio.core.model.Pathway();
		reaction2entry.clear();
		ecrelEnzyme2Compound.clear();
		entriesById.clear();
		entryName2Ids.clear();
		reaction2gpml.clear();
		
		//Convert pathway attributes
		PathwayElement mappInfo = gpmlPathway.getMappInfo();
		String title = pathway.getTitle();
		if(title != null) {
			if(title.length() > 50) {
				mappInfo.addComment(mappInfo.new Comment("Name truncated from: " + title, COMMENT_SOURCE));
				title = title.substring(0, 50);
			}
			mappInfo.setMapInfoName(title);
			mappInfo.setOrganism(speciesCode);
		}
		mappInfo.setMapInfoDataSource(pathway.getLink()); //KH add url to kgml map

		//Some information to track which kegg id and version was used
		mappInfo.setDynamicProperty(KEGG_ID, pathway.getName());
		mappInfo.setDynamicProperty(
				CONVERSION_DATE, CONVERSION_DATE_FORMAT.format(Calendar.getInstance().getTime())
		);

		//Convert entries from organism specific pathway
		for(Entry entry : pathway.getEntry()) {
			List<Graphics> entrygraphics = entry.getGraphics();
			if (entrygraphics.size() == 1){
				String graphicstype = entrygraphics.get(0).getType();
				if (!graphicstype.equals("line")){
					Logger.log.trace("Processing entry (" + entry.getType() + "): " + entry.getName());
					convertEntry(entry);
				}
				else{
					Logger.log.trace("Skipping entry "+entry.getName()+ ". This entry defines a line.");
				}
			}	
			else
			{
				Logger.log.trace("Skipping entry " + entry.getName() + ". Entry has multiple graphics.");
			}
		}

		//Convert entries that are only in the reference pathway
		//And add additional reaction information
		if(map != null) {
			for(Entry entry : map.getEntry()) {
				List<Graphics> entrygraphics = entry.getGraphics();
				if (entrygraphics.size() == 1){
					String graphicstype = entrygraphics.get(0).getType();
				
				if (!graphicstype.equals("line")){
					Logger.log.trace("Processing reference entry (" + entry.getType() + "): " + entry.getName() + ", " + entry.getId());
					if(id2gpml.get(entry.getId()) == null) {
						Logger.log.trace("Converting, was not present in organism specific map");
						convertEntry(entry);
					} else {
						Logger.log.trace("Skipping, already converted");
						mapToReaction(entry); //Still map reactions, map may contain additional values
					}
				}
				else{
					Logger.log.trace("Skipping reference entry "+entry.getName()+ ". This entry defines a line.");				}
				}
				else{
					Logger.log.trace("Skipping reference entry " + entry.getName() + ". Entry has multiple graphics.");
				}
			}
		}

		//Convert relations from organism specific pathway
		for(Relation relation : pathway.getRelation()) {
			Logger.log.trace("Processing relation " + relation.getType());
			if(!id2gpml.keySet().contains(relationId(relation))) {
				Logger.log.trace("Converting, was not present in organism specific map");
				convertRelation(relation);
			} else {
				Logger.log.trace("Skipping, already converted");
			}
		}

		if(map != null) {
			//Convert relations that are only in the reference pathway
			for(Relation relation : map.getRelation()) {
				Logger.log.trace("Processing reference relation:" + relation.getType());
				if(!id2gpml.keySet().contains(relationId(relation))) {
					Logger.log.trace("Converting, was not present in organism specific map");
					convertRelation(relation);
				} else {
					Logger.log.trace("Skipping, already converted");
				}
			}
		}

		//Convert reactions from organism specific pathway
		for(Reaction reaction : pathway.getReaction()) {
			Logger.log.trace("Processing reaction " + reaction.getType());
			convertReaction(reaction);
		}

		if(map != null) {
			//Convert reactions that are only in the reference pathway
			for(Reaction reaction : map.getReaction()) {
				Logger.log.trace("Processing reference reaction:" + reaction.getId());
				if(!id2gpml.keySet().contains(reactionId(reaction))) { 
					Logger.log.trace("Converting, was not present in organism specific map");
					convertReaction(reaction);
				} else {
					Logger.log.trace("Skipping, already converted");
				}
			}
		}

		return gpmlPathway;
		
	} 

	private void mapConvertedId(String id, PathwayElement e) {
		if(id2gpml.containsKey(id)) {
			throw new IllegalArgumentException("Trying to map '" + id + "' to " + e +
					", but this id was already mapped to " + id2gpml.get(id));
		}
		id2gpml.put(id, e);
	}

	private PathwayElement getReactionCompounds(String compoundName, Reaction reaction) {
		//Get the entries that are linked to this reaction
		Set<String> reactionEnzymes = reaction2entry.get(reaction.getName());
		
		if(reactionEnzymes == null) {
			throw new RuntimeException("Unable to find reaction entries for " + compoundName);
		}
		
		//Try to find the correct compound id for these entries
		String compoundId = null;
		for(String entry : reactionEnzymes) {
			Set<String> ids = ecrelEnzyme2Compound.get(entry);
			for(String cid : ids) {
				Entry e = entriesById.get(cid);
				if(e != null && compoundName.equals(e)) {
					compoundId = cid;
					break; //TODO: In the case of two identical reactions, only this one
					//will be converted to a line. To correct this, we need to add two
					//reactions and somehow find the right substrate/mediator/product combinations.
				}
			}
		}
		
		if(compoundId == null) { //No ECrel for this compound, take closest to enzymes
			//Find the compound with correct name closest to reaction entry
			double shortestD = Double.MAX_VALUE;
			String shortestCid = null;
			for(String eid : reactionEnzymes) {
				Entry entry = entriesById.get(eid);
				Point2D epoint = null;
				Point2D cpoint = null;
				//getGraphics returns a list. Only deal with entries that have one graphic
				List<Graphics> egraphics = entry.getGraphics();
				if (egraphics.size() == 1)
				{
				Graphics eg = egraphics.get(0);
					if (!eg.getType().equals("line")){
						epoint = new Point2D.Double(
								Double.parseDouble(eg.getX()), 
								Double.parseDouble(eg.getY()));

						for(String ceid : entryName2Ids.get(compoundName)) {
							Entry centry = entriesById.get(ceid);
							List<Graphics> cgraphics = centry.getGraphics();
							if (cgraphics.size() == 1){
								Graphics cg = cgraphics.get(0);
								if (!cg.getType().equals("line")){
									cpoint = new Point2D.Double(
											Double.parseDouble(cg.getX()),
											Double.parseDouble(cg.getY()));
								}
							}
				
					double d = cpoint.distance(epoint);
					if(d < shortestD) {
						shortestD = d;
						shortestCid = ceid;
					}
				}
			}
		}
			}
		compoundId = shortestCid;
		}
		return id2gpml.get(compoundId);
	}

	private void convertReaction(Reaction reaction) {
		// Create a list of elements in relations with reaction
		String reactionName = reaction.getName();
		List<String> reactions = new ArrayList<String>();
		List<PathwayElement> substrates = new ArrayList<PathwayElement>();
		List<PathwayElement> products = new ArrayList<PathwayElement>();
		Set<String> enzymeIds = new HashSet<String>();
		
		if (reactionName.contains(" ")){
			String[] names = reactionName.split(" ");
			for(int i = 0; i < names.length; i++) {
				reactions.add(names[i]);
			}
		}
			
		enzymeIds = reaction2entry.get(reactionName);
		
		for(Substrate s : reaction.getSubstrate()) { 
			PathwayElement pwe = getReactionCompounds(s.getName(), reaction); 
				if(pwe != null) substrates.add(pwe);
		}
		Logger.log.trace("Substrates: " + substrates);
		
		for(Product p : reaction.getProduct()) {
			PathwayElement pwe = getReactionCompounds(p.getName(), reaction);
			if(pwe != null) products.add(pwe);
		}
		Logger.log.trace("Products: " + products);		
		
		//Add a reaction anchor + line to the datanodes
		if(substrates.size() > 0 && products.size() > 0) {
			PathwayElement substrate = substrates.get(0);
			PathwayElement product = products.get(0);

			//First create a base line between the first substrate / product
			MLine line = createPathwayLine(substrate, product);
			line.addComment(reaction.getName(), COMMENT_SOURCE); 
			gpmlPathway.add(line);
			line.setGeneratedGraphId();
			

			//Create the lines for the remaining substrates / products
			MAnchor anchorIn = null;
			for(int i = 1; i < substrates.size(); i++) {
				if(anchorIn == null) anchorIn = line.addMAnchor(0.2);
				MLine l = createPathwayLine(substrates.get(i), anchorIn);
				l.setConnectorType(ConnectorType.CURVED);
				gpmlPathway.add(l);
				l.setGeneratedGraphId();
			}
			MAnchor anchorOut = null;
			for(int i = 1; i < products.size(); i++) {
				if(anchorOut == null) anchorOut = line.addMAnchor(0.8);
				MLine l = createPathwayLine(anchorOut, products.get(i));
				l.setConnectorType(ConnectorType.CURVED);
				gpmlPathway.add(l);
				l.setGeneratedGraphId();
			}

			line.setZOrder(gpmlPathway.getMinZOrder());
			mapConvertedId(reactionId(reaction), line);

			if(enzymeIds != null) {
				//Add the mediators
				PathwayElement m = null;
				Iterator<String> it = enzymeIds.iterator();
				while(m == null && it.hasNext()) {
					m = id2gpml.get(it.next());
				}
				if(m == null) return; //No mediators to add

				Point sp = Util.findBorders(substrate, m)[0];
				Point ep = Util.findBorders(m, product)[1];
				line.getMStart().setRelativePosition(sp.x, sp.y);
				line.getMEnd().setRelativePosition(ep.x, ep.y);
				

				//Set the waypoints
				line.setConnectorType(ConnectorType.ELBOW);
				line.getConnectorShape().recalculateShape(line);

				WayPoint[] wps = line.getConnectorShape().getWayPoints();
				Segment[] sgs = line.getConnectorShape().getSegments();

				//Find the main axis of the substrate/product line
				int maxis = 0;
				double angle = LinAlg.angle(
						new Point(substrate.getMStart().toPoint2D()),
						new Point(product.getMStart().toPoint2D())
				);
				if(angle < -Math.PI/4 && angle > -3*Math.PI/4) {
					maxis = 1;
				}
				if(angle > Math.PI/4 && angle < 3*Math.PI/4) {
					maxis = 1;
				}

				//Find the first segment that equals the main axis,
				//its waypoint will be moved to the first mediator
				WayPoint wpm = null;
				for(int i = 1; i < sgs.length - 1; i++) {
					int axis = 0;
					if(sgs[i].getMStart().getX() == sgs[i].getMEnd().getX()) {
						axis = 1;
					}
					if(maxis == axis) {
						wpm = wps[i - 1];
						break;
					}
				}

				List<MPoint> mpoints = new ArrayList<MPoint>();
				mpoints.add(line.getMStart());
				for(WayPoint wp : wps) {
					if(wpm != null && wp == wpm) {
						mpoints.add(line.new MPoint(
								m.getMCenterX(),
								m.getMTop() + m.getMHeight() + 5 * 15
						));
					} else {
						mpoints.add(line.new MPoint(wp.getX(), wp.getY()));
					}
				}
				mpoints.add(line.getMEnd());
				line.setMPoints(mpoints);

				for(String entryId : enzymeIds) {
					addReactionMediator(line, id2gpml.get(entryId));
				}
			}
		//System.out.println("Reaction line: "+line);
		} else {
			Logger.log.error("No DataNodes to connect to for reaction " + reaction.getName());
		}
	}

	private void addReactionMediator(PathwayElement reactionLine, PathwayElement mediator) {
		//calculate anchor position
		Point lstart = new Point(reactionLine.getMStartX(), reactionLine.getMStartY());
		Point lend = new Point(reactionLine.getMEndX(), reactionLine.getMEndY());
		Point m = new Point(mediator.getMCenterX(), mediator.getMCenterY());
		double position = LinAlg.toLineCoordinates(lstart, lend, m);

		//create an anchor on the reaction line
		MAnchor anchor = reactionLine.addMAnchor(position);

		//draw a line from the bottom of the datanodes to the anchor
		PathwayElement aline = PathwayElement.createPathwayElement(ObjectType.LINE);
		gpmlPathway.add(aline);
		aline.setGeneratedGraphId();
		aline.setEndLineType(LineType.fromName("mim-catalysis"));
		aline.setStartGraphRef(Util.getGraphId(mediator));
		anchor.setGeneratedGraphId();
		aline.setEndGraphRef(anchor.getGraphId());
		aline.getMStart().setRelativePosition(0, -1);
		aline.getMEnd().setRelativePosition(0, 0);
		aline.addComment(reactionLine.getComments().get(0));
	}

	private void convertRelation(Relation relation) {
		
		PathwayElement e1 = id2gpml.get(relation.getEntry1());
		PathwayElement e2 = id2gpml.get(relation.getEntry2());
		
		
		Subtype subtype = null;
		if(relation.getSubtype().size() > 0) {
			subtype = relation.getSubtype().get(0);
			
		} else {
			Logger.log.warn("No subtype for " + relation);
		}
		Type type = Type.fromString(relation.getType());
		
		switch(type) {
		case ECREL:
			//ECrel is redundant with reaction,
			//but contains information about which compound element
			//to connect to, in case of duplicate elements mapping
			//to the same compound. Here we store this information for later use.
			
			if (relation.getSubtype().size() > 0){
			String cid = subtype.getValue();
			ecrelEnzyme2Compound.put(relation.getEntry1(), cid);
			ecrelEnzyme2Compound.put(relation.getEntry2(), cid);
			}

			//Test: Add lines for ECrel
//			PathwayElement ec = id2gpml.get(cid);
//			if(ec != null && e1 != null && e2 != null) {
//				PathwayElement l1 = createPathwayLine(e1, ec);
//				l1.setEndLineType(LineType.ARROW);
//				l1.setColor(Color.RED);
//				PathwayElement l2 = createPathwayLine(ec, e2);
//				l2.setEndLineType(LineType.ARROW);
//				l2.setColor(Color.RED);
//				gpmlPathway.add(l1);
//				gpmlPathway.add(l2);
//			}
			return; //ECrel is redundant with reaction
		case MAPLINK:
			if("compound".equals(subtype.getName())) {
				String maplinkid = null;
				
				//Find out which entry is the map
				//NOTE: the arrow direction sometimes doesn't match
				//that on the image on the kegg site. As far as I could
				//see, this is just incorrectly defined in the KGML.
				
				if(e2.getXref().toString().contains("path:")){ //entry 2 is map
					e1 = id2gpml.get(subtype.getValue());
					maplinkid = relation.getEntry2() + subtype.getValue();
				} else { //entry1 = map
					e2 = id2gpml.get(subtype.getValue());
					maplinkid = relation.getEntry1() + subtype.getValue();
				}
				if(!addedMapLinks.contains(maplinkid)) {
					addedMapLinks.add(maplinkid);
				} else {
					Logger.log.trace("Skipping maplink, already defined in previous relation");
					return; //Skip this link, already added
				}
			}
		}

		if(e1 != null && e2 != null) {
			PathwayElement line = createPathwayLine(e1, e2);
			line.setConnectorType(ConnectorType.STRAIGHT);
			line.addComment(line.new Comment(
					relation.getType() + ";" + relation.getEntry1() + ";" + relation.getEntry2(),
					COMMENT_SOURCE
			));
			if(subtype != null) {
				mapRelationType(subtype, line);
			}
			mapConvertedId(relationId(relation), line);
			gpmlPathway.add(line);
			line.setGeneratedGraphId();
		} else {
			Logger.log.warn("Invalid relation, missing connecting element for: " + relation);
		}
	}  

	private void convertEntry(Entry entry) throws RemoteException, ConverterException, ClassNotFoundException, IDMapperException {
		Type type = Type.fromString(entry.getType());
		
		
		switch(type) {
		case MAP:
			convertMap(entry);
			break;
		case COMPOUND:
			convertCompound(entry);
			break;
		case ENZYME:
		case ORTHOLOG:
		case GENE:
		case OTHER:
			convertDataNode(entry);
			break;
		case GROUP:
			Logger.log.warn("No mapping for this entry");
			break;
		}
		entriesById.put(entry.getId(), entry);
		entryName2Ids.put(entry.getName(), entry.getId());
	}

	private void convertCompound(Entry compound) throws RemoteException, ConverterException, ClassNotFoundException, IDMapperException {
		List<Graphics> graphics = compound.getGraphics();
		String name = compound.getName();
		SymbolInfo sinfo = null;
				
		if (graphics.size() == 1)
			{
			Graphics cg = graphics.get(0);
			
			if(isUseWebservice()) { //fetch the real name from the webservice
				sinfo = keggService.getKeggSymbol("cpd:"+cg.getName());
			}
			
			String compoundname = sinfo == null ? "cpd:"+cg.getName() : sinfo.getPreferred(prefSymbolIndex);
			
			//Create gpml element. Modified by KH to use compoundname instead of sinfo
			PathwayElement pwElm = createDataNode(
					cg,
					DataNodeType.METABOLITE,
					compoundname,
					name.replace("cpd:", ""),
					BioDataSource.KEGG_COMPOUND);
			
			//Add comments regarding the source on KEGG. Modified by KH to only add if working online (otherwise sinfo is null)
			if(!(sinfo == null)) {
				sinfo.addToComments(pwElm);
			}
			
			gpmlPathway.add(pwElm);
			pwElm.setGeneratedGraphId();
			mapConvertedId(compound.getId(), pwElm);
		}
		
		else {
			Logger.log.trace("Skipping compound "+compound.getName()+ " due to multiple graphics");
		}

	}

	// convert links to other pathways appearing in KEGG pathways as a label
	private void convertMap(Entry map) {
		List<Graphics> graphics = map.getGraphics();
		
		if(graphics.isEmpty()) {
			Logger.log.warn("Skipping entry without graphics: " + map.getId() + ", " + map.getName());
			return;
		}
		
		if (graphics.size() == 1)
		{
			Graphics dg = graphics.get(0);
			String label = null;
			String name = map.getName();
			
			if (dg.getName() == null) //Some map links have graphics with missing names
			{
				label = name;
			}
			else {
				label = dg.getName();
			}
			
			if(label.startsWith("TITLE:")) {
			return; //This is the title of this map, skip it
			}
	
			//Create gpml element
			PathwayElement pwElm = createDataNode(
				dg,
				DataNodeType.GENEPRODUCT,
				label == null ? "" : label,
				name == null ? "" : name,
				BioDataSource.KEGG_GENES
			);

			//Add comments regarding the source on KEGG and set shape type
			String e_id = map.getId();
			String e_type = map.getType();
			String e_name = map.getName();
			pwElm.addComment(pwElm.new Comment("Original kegg element: " + e_type + ";" + e_id + ";" + e_name, COMMENT_SOURCE));
			pwElm.setShapeType(ShapeType.ROUNDED_RECTANGLE);
		
			gpmlPathway.add(pwElm);
			pwElm.setGeneratedGraphId();
			mapConvertedId(map.getId(), pwElm);
			mapToReaction(map);
			}
		
		else {
			Logger.log.trace("Skipping datanode "+map.getName()+" due to mutliple graphics.");
		}
	}

	private String processLabel(String label) {
		if(label.contains(", ")) {
			label = label.split(", ")[0]; //Only use first synonym
		}
		if(label.endsWith("...")) label.substring(0, label.length() - 3);
		return label;
	}
	
	private void convertDataNode(Entry entry) throws RemoteException, ConverterException {
		List<Graphics> graphics = entry.getGraphics();
		
		if(graphics.isEmpty()) {
			Logger.log.warn("Skipping entry without graphics: " + entry.getId() + ", " + entry.getName());
			return;
		}
		
		
		if (graphics.size() == 1)
		{
		
			Graphics dg = graphics.get(0);
			String label = dg.getName();
			label = processLabel(label);
			String name = entry.getName();
			String[] ids = name.split(" ");

			//Add all ids as a stack
			List<PathwayElement> pwElms = new ArrayList<PathwayElement>();

			for(int i = 0; i < ids.length; i++) {
				String id = ids[i];
				String[] genes = getGenes(id, species, Type.fromString(entry.getType()));
				
				for(String gene : genes) {
					SymbolInfo sinfo = null;
					
					if(isUseWebservice()) { //fetch the real name from the webservice
						String query = symbolByGene ? gene : id;
						if(!query.startsWith(Util.getKeggOrganism(species) + ":")) {
							query = Util.getKeggOrganism(species) + ":" + query;
						}
						sinfo = keggService.getKeggSymbol(query);
						
					}
			
					String geneName = sinfo == null ? dg.getName() : sinfo.getPreferred(prefSymbolIndex);
					geneName = processLabel(geneName);

					//Create gpml element. Modified by KH to use geneName instead of sinfo.getPreferred
					PathwayElement pwElm = createDataNode(
							dg,
							DataNodeType.GENEPRODUCT,
							geneName,
							gene == null ? "" : gene,
							BioDataSource.ENTREZ_GENE
						);
					
					
					//Add comments regarding the source on KEGG. Modified by KH to only add if working online (otherwise sinfo is null)
					if(!(sinfo == null)) {
						sinfo.addToComments(pwElm);
					}
										
					
					String e_id = entry.getId();
					String e_type = entry.getType();
					String e_name = entry.getName();
					pwElm.addComment(pwElm.new Comment(
						"Original kegg element: " + e_type + ";" + e_id + ";" + e_name, COMMENT_SOURCE));
					
					gpmlPathway.add(pwElm);
					pwElm.setGeneratedGraphId();
					pwElms.add(pwElm);
				}

			if(genes.length == 0) { //Plain conversion if no gene mappings could be found
				PathwayElement pwElm = createDataNode(
						dg, //TODO: refactor variable names
						DataNodeType.GENEPRODUCT,
						name == null ? "" : id,
						label == null ? "" : label,
						DataSource.getByFullName("Kegg " + entry.getType())
				);
				
				gpmlPathway.add(pwElm);
				pwElm.setGeneratedGraphId();
				pwElms.add(pwElm);
			}
		} //end for loop
			
		if(pwElms.size() > 1) {
			
			PathwayElement group = createGroup(name, pwElms);  
			Util.stackElements(pwElms);
			mapConvertedId(entry.getId(), group);
			//mapConvertedId(entry.getId(), pwElms.get(0));   // this is for the case when you don't want to create groups
		} else {
			PathwayElement pwElm = pwElms.iterator().next();
			mapConvertedId(entry.getId(), pwElm);
		}
		mapToReaction(entry);
	}
		
		else {
			Logger.log.trace("Skipping datanode "+entry.getName()+" due to mutliple graphics.");
		}
	}

	private void mapToReaction(Entry entry) {
		String reactionString = entry.getReaction();
		if(reactionString != null) {
			String[] reactions = reactionString.split(" ");

			for(String reaction : reactions) {
				Logger.log.trace("Mapping reaction " + reaction + " to " + entry.getId());
				reaction2entry.put(reaction, entry.getId());
			}
		}
	}

	private String relationId(Relation r) {
		String id = r.getEntry1() + r.getEntry2() + r.getType();
		List<Subtype> st = r.getSubtype();
		if(st != null && st.size() > 0) {
			for(Subtype s : st) {
				id += s.getName() + s.getValue();
			}
		}
		return id;
	}
	
	private String reactionId(Reaction r) {
		String id = r.getId() + r.getName();
		return id;
	}

	private MLine createPathwayLine(GraphIdContainer start, GraphIdContainer end) {
		// Create new pathway line
		MLine line = (MLine)PathwayElement.createPathwayElement(ObjectType.LINE);

		line.setColor(Color.BLACK);

		String startId = Util.getGraphId(start);

		line.setStartGraphRef(startId);
		line.setEndLineType(LineType.ARROW);

		Point2D sc = start.toAbsoluteCoordinate(new Point2D.Double(0, 0));
		Point2D ec = end.toAbsoluteCoordinate(new Point2D.Double(0, 0));

		if(sc.getX() == ec.getX() ||
				sc.getY() == ec.getY()) {
			line.setConnectorType(ConnectorType.STRAIGHT);
		} else {
			line.setConnectorType(ConnectorType.ELBOW);
		}

		String endId = Util.getGraphId(end);
		line.setEndGraphRef(endId);

		Point[] pts = Util.findBorders(start, end);
		line.getMStart().setRelativePosition(pts[0].x, pts[0].y);
		line.getMEnd().setRelativePosition(pts[1].x, pts[1].y);
		
		return line;
	}

	private PathwayElement createGroup(String name, Collection<PathwayElement> elements) {
		
		PathwayElement group = PathwayElement.createPathwayElement(ObjectType.GROUP);
		group.setTextLabel(name);
		gpmlPathway.add(group);
		group.setGeneratedGraphId();
		String id = gpmlPathway.getUniqueGroupId();
		group.setGroupId(id);
		for(PathwayElement pe : elements) {
			pe.setGroupRef(id);
		}
		return group;
	}

	private void convertGraphics(PathwayElement pwElm, Graphics graphics) {
		// Convert a hexadecimal color into an awt.Color object
		// Remove the # before converting
		String colorStringGPML = graphics.getFgcolor();
		Color colorGPML;
		if (colorStringGPML != null) {
			colorGPML = GpmlFormatAbstract.gmmlString2Color(colorStringGPML.substring(1));
		} else {
			colorGPML = Color.BLACK;
		}
		pwElm.setColor(colorGPML);

		// Set x, y, width, height
		
		String s_cx = null;
		String s_cy = null;
		
		if (graphics.getX() == null){
		String coords = graphics.getCoords();
		String[] coordinates = coords.split(",");
		
		double x = 0.0;
		double y = 0.0;
		int numCoords = coordinates.length/2;

		for(int i = 0; i < coordinates.length; i++){
			if (i %2 == 0){
				//even = x
				x = x + Double.parseDouble(coordinates[i]);
			}
			else{
				//uneven = y
				y = y + Double.parseDouble(coordinates[i]);
			}
		}

		double cx = x/numCoords;
		double cy = y/numCoords;
		
		s_cx = Double.toString(cx);
		s_cy = Double.toString(cy);
		
		}
		
		else{
			s_cx = graphics.getX();
			s_cy = graphics.getY();
		}
		
		String s_w = graphics.getWidth();
		String s_h = graphics.getHeight();

		double height = Double.parseDouble(s_h);
		double width = Double.parseDouble(s_w);
		pwElm.setMWidth(coordinateToGpml(width));
		pwElm.setMHeight(coordinateToGpml(height));

		double centerY = Double.parseDouble(s_cy);
		double centerX = Double.parseDouble(s_cx);

		pwElm.setMCenterX(coordinateSpacing(coordinateToGpml(centerX)));
		pwElm.setMCenterY(coordinateSpacing(coordinateToGpml(centerY)));
		
	}

	private PathwayElement createDataNode(Graphics graphics, DataNodeType type, String label, String id, DataSource source) {
		
		PathwayElement dn = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		dn.setDataSource(source);
		if(id != null && id.length() < 50) dn.setGeneID(id);
		dn.setDataNodeType(type);
		dn.setTextLabel(label);

		convertGraphics(dn, graphics);
		dn.setInitialSize();

		if(type == DataNodeType.METABOLITE) {
			dn.setColor(Color.BLUE);
		}
		return dn;
	}

	private String[] getGenes(String keggId, String organism, Type type) throws RemoteException, ConverterException {
		boolean isValidGene = type == Type.GENE && keggId.matches("[a-z]{3}:[0-9]+$");
		
		if(isUseWebservice() && !isValidGene) {
			if(type == Type.ORTHOLOG) {
				return keggService.getGenesForKo(keggId, organism);
			} else if(type == Type.GENE) {
				return keggService.getGenes(keggId, organism);
			} else {
				return keggService.getGenesForEc(keggId, organism);
			}
		} else {
			//Assumes that if it's an annotated gene:
			//a gene is of the form hsa:1234, where 1234 is the Entrez Gene id and hsa is the organism code
			if(keggId.length() > 4) keggId = keggId.substring(4);
			return new String[] { keggId };
		}
	}

	private double coordinateToGpml(double c) {
		return c;
	}

	private double coordinateSpacing(double c) {
		return c * spacing; //Make pathway 2.5 times larger, for better spacing
	}

	private void mapRelationType(Subtype subtype, PathwayElement line) {
		String name = subtype.getName();
		String value = subtype.getValue();

		if			("--|".equals(value)) {
			line.setEndLineType(LineType.TBAR);
		} else if	("-->".equals(value)) {
			line.setEndLineType(LineType.ARROW);
			line.setLineStyle(LineStyle.DASHED);
		} else if	("..>".equals(value)) {
			line.setEndLineType(LineType.ARROW);
			line.setLineStyle(LineStyle.DASHED);
		} else if	("...".equals(value)) {
			line.setEndLineType(LineType.LINE);
			line.setLineStyle(LineStyle.DASHED);
		} else if	("---".equals(value)) {
			line.setEndLineType(LineType.LINE);
		} else if	("-+-".equals(value)) {
			line.setEndLineType(LineType.LINE);
		} else if	("+p".equals(value)) {
			line.setEndLineType(LineType.ARROW);
		} else if	("-p".equals(value)) {
			line.setEndLineType(LineType.ARROW);
		} else if	("+g".equals(value)) {
			line.setEndLineType(LineType.ARROW);
		} else if	("+u".equals(value)) {
			line.setEndLineType(LineType.ARROW);
		} else if	("+m".equals(value)) {
			line.setEndLineType(LineType.ARROW);
		}

		line.addComment(line.new Comment(name + "; " + value, COMMENT_SOURCE));
	}

	private static enum Type {
		MAP("map"), ENZYME("enzyme"), COMPOUND("compound"),
		ORTHOLOG("ortholog"), GENE("gene"), OTHER("other"),
		ECREL("ECrel"), PPREL("PPrel"), GEREL("GErel"), PCREL("PCrel"),
		MAPLINK("maplink"), GROUP("group"), UNDEFINED("undefined");

		private static final Map<String, Type> stringToEnum
			= new HashMap<String, Type>();
		static { for(Type e : values()) stringToEnum.put(e.toString(), e); }

		private final String name;

		private Type(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		static Type fromString(String s) {
			return stringToEnum.get(s);
		}
	}
}
