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
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConnectorType;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.GpmlFormatAbstract;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.MLine;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.ShapeType;
import org.pathvisio.model.ConnectorShape.Segment;
import org.pathvisio.model.ConnectorShape.WayPoint;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;
import org.pathvisio.view.LinAlg;
import org.pathvisio.view.MIMShapes;
import org.pathvisio.view.LinAlg.Point;

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

public class KeggFormat {
	static {
		MIMShapes.registerShapes();
	}

	private static final String COMMENT_SOURCE = "KeggConverter";
	private static final String KEGG_ID = "KeggId";
	private static final String CONVERSION_DATE = "ConversionDate";

	private static final SimpleDateFormat CONVERSION_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

	private double spacing = 2;

	private KeggService keggService;
	private Organism organism;
	private Pathway pathway; //Main pathway
	private Pathway map; //Used only to improve species specific pathway

	private org.pathvisio.model.Pathway gpmlPathway;

	private Map<String, PathwayElement> id2gpml = new HashMap<String, PathwayElement>();
	private Set<String> addedMapLinks = new HashSet<String>();
	private SetMultimap<String, String> reaction2entry = new HashMultimap<String, String>();

	//Contains extra information from relation elements, on which compound
	//is linked to which reaction (in case there are multiple compounds with the same
	//name on the map)
	private SetMultimap<String, String> ecrelEnzyme2Compound = new HashMultimap<String, String>();

	private SetMultimap<String, String> entryName2Ids  = new HashMultimap<String, String>();

	private Map<String, Entry> entriesById = new HashMap<String, Entry>();

	public KeggFormat(Pathway pathway, Organism organism) {
		this.pathway = pathway;
		this.organism = organism;
	}

	public KeggFormat(Pathway map, Pathway ko, Organism organism) {
		this(ko, organism);
		this.map = map;
	}

	public void setSpacing(double spacing) {
		this.spacing = spacing;
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

	public org.pathvisio.model.Pathway convert() throws RemoteException, ConverterException {
		id2gpml.clear();
		addedMapLinks.clear();
		gpmlPathway = new org.pathvisio.model.Pathway();
		reaction2entry.clear();
		ecrelEnzyme2Compound.clear();
		entriesById.clear();
		entryName2Ids.clear();

		//Convert pathway attributes
		PathwayElement mappInfo = gpmlPathway.getMappInfo();
		String title = pathway.getTitle();
		if(title != null) {
			if(title.length() > 50) {
				mappInfo.addComment(mappInfo.new Comment("Name truncated from: " + title, COMMENT_SOURCE));
				title = title.substring(0, 50);
			}
			mappInfo.setMapInfoName(title);
		}
		mappInfo.setMapInfoDataSource(pathway.getLink());

		//Some information to track which kegg id and version was used
		mappInfo.setDynamicProperty(KEGG_ID, pathway.getName());
		mappInfo.setDynamicProperty(
				CONVERSION_DATE, CONVERSION_DATE_FORMAT.format(Calendar.getInstance().getTime())
		);

		//Convert entries from organism specific pathway
		for(Entry entry : pathway.getEntry()) {
			Logger.log.trace("Processing entry (" + entry.getType() + "): " + entry.getName());
			convertEntry(entry);
		}

		//Convert entries that are only in the reference pathway
		//And add additional reaction information
		if(map != null) {
			for(Entry entry : map.getEntry()) {
				Logger.log.trace("Processing reference entry (" + entry.getType() + "): " + entry.getName() + ", " + entry.getId());
				if(id2gpml.get(entry.getId()) == null) {
					Logger.log.trace("Converting, was not present in organism specific map");
					convertEntry(entry);
				} else {
					Logger.log.trace("Skipping, already converted");
					mapToReaction(entry); //Still map reactions, map may contain additional values
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
				Logger.log.trace("Processing reference reaction:" + reaction.getType());
				if(!id2gpml.keySet().contains(reaction.getName())) {
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
				Point2D epoint = new Point2D.Double(
						Double.parseDouble(entry.getGraphics().getX()),
						Double.parseDouble(entry.getGraphics().getY())
				);
				for(String ceid : entryName2Ids.get(compoundName)) {
					Entry centry = entriesById.get(ceid);
					Point2D cpoint = new Point2D.Double(
						Double.parseDouble(centry.getGraphics().getX()),
						Double.parseDouble(centry.getGraphics().getY())
					);
					double d = cpoint.distance(epoint);
					if(d < shortestD) {
						shortestD = d;
						shortestCid = ceid;
					}
				}
			}
			compoundId = shortestCid;
		}
		return id2gpml.get(compoundId);
	}

	private void convertReaction(Reaction reaction) {
		// Create a list of elements in relations with reaction
		Set<String> enzymeIds = reaction2entry.get(reaction.getName());

		List<PathwayElement> substrates = new ArrayList<PathwayElement>();
		for(Substrate s : reaction.getSubstrate()) {
			PathwayElement pwe = getReactionCompounds(s.getName(), reaction);
				if(pwe != null) substrates.add(pwe);
		}
		List<PathwayElement> products = new ArrayList<PathwayElement>();
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

			//Create the lines for the remaining substrates / products
			MAnchor anchorIn = null;
			for(int i = 1; i < substrates.size(); i++) {
				if(anchorIn == null) anchorIn = line.addMAnchor(0.2);
				MLine l = createPathwayLine(substrates.get(i), anchorIn);
				l.setConnectorType(ConnectorType.CURVED);
				gpmlPathway.add(l);
			}
			MAnchor anchorOut = null;
			for(int i = 1; i < products.size(); i++) {
				if(anchorOut == null) anchorOut = line.addMAnchor(0.8);
				MLine l = createPathwayLine(anchorOut, products.get(i));
				l.setConnectorType(ConnectorType.CURVED);
				gpmlPathway.add(l);
			}

			line.setZOrder(gpmlPathway.getMinZOrder());
			mapConvertedId(reaction.getName(), line);

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
			String cid = subtype.getValue();
			ecrelEnzyme2Compound.put(relation.getEntry1(), cid);
			ecrelEnzyme2Compound.put(relation.getEntry2(), cid);

//			//Test: Add lines for ECrel
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
				if(e2.getObjectType() == ObjectType.LABEL) { //entry2 is map
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
		} else {
			Logger.log.warn("Invalid relation, missing connecting element for: " + relation);
		}
	}

	private void convertEntry(Entry entry) throws RemoteException, ConverterException {
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

	private void convertCompound(Entry compound) throws RemoteException, ConverterException {
		Graphics graphics = compound.getGraphics();

		String name = compound.getName();
		String label = graphics.getName();
		if(isUseWebservice()) { //fetch the real name from the webservice
			label = keggService.getKeggSymbol(name);
		}

		PathwayElement pwElm = createDataNode(
				graphics,
				DataNodeType.METABOLITE,
				label,
				name.replace("cpd:", ""),
				BioDataSource.KEGG_COMPOUND
		);

		gpmlPathway.add(pwElm);
		mapConvertedId(compound.getId(), pwElm);
	}

	private void convertMap(Entry map) {
		String label = map.getName();
		PathwayElement link = PathwayElement.createPathwayElement(ObjectType.LABEL);
		link.setShapeType(ShapeType.ROUNDED_RECTANGLE);
		link.addComment(link.new Comment(map.getLink(), COMMENT_SOURCE));
		Graphics graphics = map.getGraphics();
		if(graphics != null) {
			String glabel = graphics.getName();
			if(glabel == null) glabel = label;
			else label = glabel;
			if(label.startsWith("TITLE:")) {
				return; //This is the title of this map, skip it
			}
			convertGraphics(link, graphics);
		}

		link.setTextLabel(label);
		mapConvertedId(map.getId(), link);
		gpmlPathway.add(link);
	}

	private void convertDataNode(Entry entry) throws RemoteException, ConverterException {
		Graphics graphics = entry.getGraphics();
		if(graphics == null) {
			Logger.log.warn("Skipping entry without graphics: " + entry.getId() + ", " + entry.getName());
			return;
		}
		String label = graphics.getName();

		String name = entry.getName();
		String[] ids = name.split(" ");

		//Add all ids as a stack
		Set<PathwayElement> pwElms = new HashSet<PathwayElement>();

		for(int i = 0; i < ids.length; i++) {
			String id = ids[i];
			String[] genes = getGenes(id, organism, Type.fromString(entry.getType()));

			for(String gene : genes) {
				String geneName = graphics.getName();
				if(isUseWebservice()) { //fetch the real name from the webservice
					geneName = keggService.getKeggSymbol(
							Util.getKeggOrganism(organism) + ":" + gene
					);
				}

				//Create gpml element
				PathwayElement pwElm = createDataNode(
						graphics,
						DataNodeType.GENEPRODUCT,
						geneName == null ? "" : geneName,
						gene == null ? "" : gene,
						BioDataSource.ENTREZ_GENE
				);

				//Add comments regarding the source on KEGG
				String e_id = entry.getId();
				String e_type = entry.getType();
				String e_name = entry.getName();
				pwElm.addComment(pwElm.new Comment(
						"Original kegg element: " + e_type + ";" + e_id + ";" + e_name,
						COMMENT_SOURCE
				));
				gpmlPathway.add(pwElm);
				pwElms.add(pwElm);
			}

			if(genes.length == 0) { //Plain conversion if no gene mappings could be found
				PathwayElement pwElm = createDataNode(
						graphics,
						DataNodeType.GENEPRODUCT,
						name == null ? "" : name,
						label == null ? "" : label,
						DataSource.getByFullName("Kegg " + entry.getType())
				);

				gpmlPathway.add(pwElm);
				pwElms.add(pwElm);
			}
		}

		if(pwElms.size() > 1) {
			PathwayElement group = createGroup(name, pwElms);
			Util.stackElements(pwElms);
			mapConvertedId(entry.getId(), group);
		} else {
			PathwayElement pwElm = pwElms.iterator().next();
			mapConvertedId(entry.getId(), pwElm);
		}
		mapToReaction(entry);
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
		String s_cx = graphics.getX();
		String s_cy = graphics.getY();
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

	private String[] getGenes(String keggId, Organism organism, Type type) throws RemoteException, ConverterException {
		if(isUseWebservice() && type != Type.GENE) {
			if(type == Type.ORTHOLOG) {
				return keggService.getGenesForKo(keggId, organism);
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
