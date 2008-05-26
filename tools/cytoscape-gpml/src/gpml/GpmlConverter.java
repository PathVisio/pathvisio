package gpml;

import giny.view.GraphView;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.GroupStyle;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.PathwayElement.MAnchor;

import cytoscape.CyEdge;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.groups.CyGroup;
import cytoscape.groups.CyGroupManager;
import cytoscape.view.CyNetworkView;

public class GpmlConverter {
	List<CyEdge> edges = new ArrayList<CyEdge>();
	
	HashMap<GraphIdContainer, CyNode> nodeMap = new HashMap<GraphIdContainer, CyNode>();
	HashMap<PathwayElement, String[]> edgeMap = new HashMap<PathwayElement, String[]>();

	GpmlHandler gpmlHandler;
	Pathway pathway;
		
	private GpmlConverter(GpmlHandler h) {
		gpmlHandler = h;
	}
	
	public GpmlConverter(GpmlHandler gpmlHandler, Pathway p) {
		this(gpmlHandler);
		pathway = p;
		convert();
	}
		
	public GpmlConverter(GpmlHandler gpmlHandler, String gpml) throws ConverterException {
		this(gpmlHandler);
		pathway = new Pathway();
		GpmlFormat.readFromXml(pathway, new StringReader(gpml), true);
		convert();
	}
	
	private void convert() {
		edgeMap.clear();
		edges.clear();
		nodeMap.clear();
		
		findNodes();
		findEdges();
	}
	
	public Pathway getPathway() {
		return pathway;
	}
	
	private void findNodes() {
		for(PathwayElement o : pathway.getDataObjects()) {
			int type = o.getObjectType();
			if(type == ObjectType.LEGEND || type == ObjectType.INFOBOX || type == ObjectType.MAPPINFO) {
				continue;
			}
			String id = o.getGraphId();
			//Get an id if it's not already there
			if(id == null) {
				id = pathway.getUniqueGraphId();
				o.setGraphId(id);
			}
			CyNode n = null;
			switch(type) {
			case ObjectType.GROUP:
				Logger.log.trace("Creating group: " + id);
				n = addGroup(o);
				if(n == null) {
					Logger.log.error("Group node is null");
				} else {
					Logger.log.trace("Created group node: " + n.getIdentifier());
				}
				break;
			case ObjectType.LINE:
				if(isEdge(o)) {
					continue; //Don't add an annotation node for an edge
				}
			default:
				//Create a node for every pathway element
				Logger.log.trace("Creating node: " + id + " for " + o.getGraphId() + "@" + o.getObjectType());
				n = Cytoscape.getCyNode(id, true);
			}
			
			gpmlHandler.addNode(n, o);
			nodeMap.put(o, n);
		}
		processGroups();
	}
	
	private boolean isEdge(PathwayElement e) {
		GraphIdContainer start = pathway.getGraphIdContainer(e.getMStart().getGraphRef());
		GraphIdContainer end = pathway.getGraphIdContainer(e.getMEnd().getGraphRef());
		Logger.log.trace("Checking if edge " + e.getGraphId() + ": " + 
				isNode(start) + ", " + isNode(end)
		);
		return isNode(start) && isNode(end);
	}
	
	private boolean isNode(GraphIdContainer idc) {
		if(idc instanceof MAnchor) {
			//only valid if the parent line is an edge
			return isEdge(((MAnchor)idc).getParent());
		} else if(idc instanceof PathwayElement) {
			int ot = ((PathwayElement)idc).getObjectType();
			return 
				ot == ObjectType.DATANODE ||
				ot == ObjectType.GROUP;
		} else {
			return false;
		}
	}
	
	private void findEdges() {
		Logger.log.trace("Start finding edges");
		
		//First find edges that contain anchors
		//Add an AnchorNode for that line
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.LINE) {
				if(pe.getMAnchors().size() > 0 && isEdge(pe)) {
					CyNode n = Cytoscape.getCyNode(pe.getGraphId(), true);
					gpmlHandler.addAnchorNode(n, pe);
					for(MAnchor a : pe.getMAnchors()) {
						nodeMap.put(a, n);
					}
				}
			}
		}
		//Create the cytoscape edges for each line for which
		//both the start and end points connect to a node
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.LINE) {
				if(isEdge(pe)) {
					//A line without anchors, convert to single edge
					if(pe.getMAnchors().size() == 0) {
						String source = nodeMap.get(
								pathway.getGraphIdContainer(pe.getMStart().getGraphRef())
						).getIdentifier();
						String target = nodeMap.get(
								pathway.getGraphIdContainer(pe.getMEnd().getGraphRef())
						).getIdentifier();
						
						Logger.log.trace("Line without anchors ( " + pe.getGraphId() + " ) to edge: " + 
								source + ", " + target
						);
						
						String type = pe.getStartLineType() + ", " + pe.getEndLineType();
						CyEdge e = Cytoscape.getCyEdge(
								source,
								pe.getGraphId(),
								target,
								type
						);
						edges.add(e);
						gpmlHandler.addEdge(e, pe);
					//A line with anchors, split into multiple edges
					} else {
						String sId = nodeMap.get(
								pathway.getGraphIdContainer(pe.getMStart().getGraphRef())
						).getIdentifier();
						String eId = nodeMap.get(
								pathway.getGraphIdContainer(pe.getMEnd().getGraphRef())
						).getIdentifier();
						
						Logger.log.trace("Line with anchors ( " + pe.getGraphId() + " ) to edges: " + 
								sId + ", " + eId
						);
						
						CyEdge es = Cytoscape.getCyEdge(
								sId,
								pe.getGraphId() + "_start",
								gpmlHandler.getNode(pe.getGraphId()).getParentIdentifier(),
								"start-anchor"
						);
						edges.add(es);
						gpmlHandler.addEdge(es, pe);
						CyEdge ee = Cytoscape.getCyEdge(
								gpmlHandler.getNode(pe.getGraphId()).getParentIdentifier(),
								pe.getGraphId() + "end",
								eId,
								"anchor-end"
						);
						edges.add(ee);
						gpmlHandler.addEdge(ee, pe);
					}
				}
			}
		}
	}
	
	public int[] getNodeIndicesArray() {
		int[] inodes = new int[nodeMap.size()];
		int i = 0;
		for(CyNode n : nodeMap.values()) {
				inodes[i++] = n.getRootGraphIndex();
		}
		return inodes;
	}
	
	public int[] getEdgeIndicesArray() {
		int[] iedges = new int[edges.size()];
		for(int i = 0; i< edges.size(); i++) iedges[i] = edges.get(i).getRootGraphIndex();
		return iedges;
	}
	
	//Add a group node
	private CyNode addGroup(PathwayElement group) {
		CyGroup cyGroup = CyGroupManager.findGroup(group.getGroupId());
		if(cyGroup == null) {
			cyGroup = CyGroupManager.createGroup(group.getGroupId(), null);
		}
		CyNode gn = cyGroup.getGroupNode();
		gn.setIdentifier(group.getGraphId());
		return gn;
	}
	
	//Add all nodes to the group
	private void processGroups() {
		for(PathwayElement pwElm : pathway.getDataObjects()) {
			if(pwElm.getObjectType() == ObjectType.GROUP) {
				
				GpmlNode gpmlNode = gpmlHandler.getNode(pwElm.getGraphId());
				CyGroup cyGroup = CyGroupManager.getCyGroup(gpmlNode.getParent());
				if(cyGroup == null) {
					Logger.log.warn("Couldn't create group: CyGroupManager returned null");
					return;
				}
				
				//The interaction name
				GroupStyle groupStyle = pwElm.getGroupStyle();
				String interaction = groupStyle.name();
				if(groupStyle == GroupStyle.NONE) {
					interaction = "group";
				}
				
				PathwayElement[] groupElements = pathway.getGroupElements(
						pwElm.getGroupId()
					).toArray(new PathwayElement[0]);

				//Create the cytoscape parts of the group
				for(int i = 0; i < groupElements.length; i++) {
					PathwayElement pe_i = groupElements[i];
					GpmlNetworkElement<?> ne_i = gpmlHandler.getNetworkElement(pe_i.getGraphId());
					//Only add links to nodes, not to annotations
					if(ne_i instanceof GpmlNode) {
						cyGroup.addNode(((GpmlNode)ne_i).getParent());
						edges.add(Cytoscape.getCyEdge(
								cyGroup.getGroupNode().getIdentifier(), 
								"inGroup: " + cyGroup.getGroupName(),
								ne_i.getParentIdentifier(), interaction)
						);
						
//						//Add links between all elements of the group
//						for(int j = i + 1; j < groupElements.length; j++) {
//							PathwayElement pe_j = groupElements[j];
//							GpmlNetworkElement<?> ne_j = gpmlHandler.getNetworkElement(pe_j.getGraphId());
//							if(ne_j instanceof GpmlNode) {
//								edges.add(Cytoscape.getCyEdge(
//										ne_i.getParentIdentifier(), 
//										"inGroup: " + cyGroup.getGroupName(),
//										ne_j.getParentIdentifier(), interaction)
//								);
//							}
//						}
					}
				}
			}
		}
	}
	
	private void setGroupViewer(CyNetworkView view, String groupViewer) {
		for(GpmlNode gn : gpmlHandler.getNodes()) {
			if(gn.getPathwayElement().getObjectType() == ObjectType.GROUP) {
				CyGroup group = CyGroupManager.getCyGroup(gn.getParent());
				CyGroupManager.setGroupViewer(group, groupViewer, view, true);
			}
		}
	}
	
	public void layout(GraphView view) {
//		String viewerName = "metaNode";
//		Logger.log.trace(CyGroupManager.getGroupViewers() + "");
//		if(CyGroupManager.getGroupViewer(viewerName) != null) {
//			setGroupViewer((CyNetworkView)view, viewerName);
//		}
		gpmlHandler.addAnnotations(view, nodeMap.values());
		gpmlHandler.applyGpmlLayout(view, nodeMap.values());
		gpmlHandler.applyGpmlVisualStyle();
		view.fitContent();
	}
}
