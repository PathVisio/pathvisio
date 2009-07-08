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
package org.pathvisio.cytoscape;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.ImportHandler;
import cytoscape.data.attr.MultiHashMap;
import cytoscape.data.attr.MultiHashMapDefinition;
import cytoscape.data.readers.GraphReader;
import cytoscape.data.webservice.WebServiceClientManager;
import cytoscape.groups.CyGroup;
import cytoscape.groups.CyGroupManager;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CyFileFilter;
import cytoscape.view.CyMenus;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.visual.ArrowShape;
import cytoscape.visual.EdgeAppearance;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;

import ding.view.DGraphView;
import ding.view.InnerCanvas;

import giny.model.Node;
import giny.view.EdgeView;
import giny.view.GraphView;
import giny.view.NodeView;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.cytoscape.actions.AttributeMapperAction;
import org.pathvisio.cytoscape.actions.CopyAction;
import org.pathvisio.cytoscape.actions.ExportAction;
import org.pathvisio.cytoscape.actions.PasteAction;
import org.pathvisio.cytoscape.actions.ToggleAnnotationAction;
import org.pathvisio.cytoscape.visualmapping.GpmlVisualStyle;
import org.pathvisio.cytoscape.wikipathways.CyWikiPathwaysClient;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.LineType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;
import org.pathvisio.view.MIMShapes;
import org.pathvisio.view.swing.PathwayTransferable;

import phoebe.PhoebeCanvasDropEvent;
import phoebe.PhoebeCanvasDropListener;

/**
 * Main Plugin class.
 */
public class GpmlPlugin extends CytoscapePlugin implements PhoebeCanvasDropListener, PropertyChangeListener {
	GpmlHandler gpmlHandler;

	private static GpmlPlugin instance;
	
	/**
	 * Can be used by other plugins to get an instance of the GpmlPlugin.
	 * @return The instance of GpmlPlugin, or null if the plugin wasn't initialized
	 * yet by the PluginManager.
	 */
	public static GpmlPlugin getInstance() {
		return instance;
	}
	
	/**
	 * Initializes the GpmlPlugin. Should only be called by Cytoscape's plugin manager!
	 * 
	 * Only one instance of this class is allowed, but this constructor can't be made 
	 * private because it's need by the Cytoscape plugin mechanism.
	 */
	public GpmlPlugin() {
		if(instance != null) {
			throw new RuntimeException("GpmlPlugin is already instantiated! Use static" +
					" method getInstance instead!");
		}
		instance = this;
		
		Logger.log.setLogLevel(true, false, true, true, true, true);
		MIMShapes.registerShapes();

		gpmlHandler = new GpmlHandler();

		Cytoscape.getImportHandler().addFilter(new GpmlFilter());

		// Listen for Network View Creation
		Cytoscape.getDesktop().getSwingPropertyChangeSupport()
		.addPropertyChangeListener(
				CytoscapeDesktop.NETWORK_VIEW_CREATED, this);

		CytoscapeDesktop desktop = Cytoscape.getDesktop();
		CyMenus menu = desktop.getCyMenus();
		menu.addCytoscapeAction(new CopyAction(this));
		menu.addCytoscapeAction(new PasteAction(this));
		menu.addCytoscapeAction(new ExportAction(this));
		menu.addCytoscapeAction(new ToggleAnnotationAction(gpmlHandler));

		JMenu pluginMenu = menu.getOperationsMenu();
		JMenu gpmlMenu = new JMenu("Gpml plugin");
		gpmlMenu.add(new AttributeMapperAction(this));
		pluginMenu.add(gpmlMenu);

		WebServiceClientManager.registerClient(new CyWikiPathwaysClient(this));
	}

	public GpmlHandler getGpmlHandler() {
		return gpmlHandler;
	}

	/**
	 * File Filter for selecting *.gpml files
	 */
	class GpmlFilter extends CyFileFilter {
		public GpmlFilter() {
			super("gpml", "GPML file", ImportHandler.GRAPH_NATURE);
		}

		public GraphReader getReader(String fileName) {
			return new GpmlReader(fileName, gpmlHandler);
		}
		
		public boolean accept(URL url, String contentType) {
			return "application/xml".equals(contentType);
		}
		
		public GraphReader getReader(URL url, URLConnection conn) {
			return new GpmlReader(conn, url, gpmlHandler);
		}
	}

	public static double mToV(double m) {
		return m * 1.0/15; //Should be stored in the model somewhere (pathvisio)
	}

	public static double vToM(double v) {
		return v * 15.0;
	}
	public void itemDropped(PhoebeCanvasDropEvent e) {
		drop(e.getTransferable());
	}

	public CyNetwork load(Pathway p, boolean newNetwork) {
		try {
			GpmlConverter converter = new GpmlConverter(gpmlHandler, p);

			//Get the nodes/edges indexes
			int[] nodes = converter.getNodeIndicesArray();
			int[] edges = converter.getEdgeIndicesArray();

			//Get the current network, or create a new one, if none is available
			CyNetwork network = Cytoscape.getCurrentNetwork();
			if(newNetwork || network == Cytoscape.getNullNetwork()) {
				String title = converter.getPathway().getMappInfo().getMapInfoName();
				network = Cytoscape.createNetwork(title == null ? "new network" : title, false);
			}

			//Add all nodes and edges to the network
			for(int nd : nodes) {
				network.addNode(nd);
			}
			for(int ed : edges) network.addEdge(ed);

			CyNetworkView view = Cytoscape.getNetworkView(network.getIdentifier());
			if(view == Cytoscape.getNullNetworkView()) {
				view = Cytoscape.createNetworkView(network);
				Cytoscape.firePropertyChange(CytoscapeDesktop.NETWORK_VIEW_FOCUS,
						null, view.getIdentifier()); 
			} else {
				view = Cytoscape.getCurrentNetworkView();
			}
			converter.layout(view);
			view.redrawGraph(true, false);

			return network;
		} catch(Exception ex) {
			Logger.log.error("Error while importing GPML", ex);
			JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
					"Error while importing GPML: " + ex.getMessage(),
					"Error", 
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public void drop(Transferable transfer) {
		try {
			Pathway p = PathwayTransferable.pathwayFromTransferable(transfer);
			if(p != null) {
				load(p, false);
			}
		}catch(Exception ex) {
			Logger.log.error("Unable to process pasted data", ex);
			JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
					"Error while importing GPML: " + ex.getMessage(),
					"Error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Copied from XGMMLWriter.java, use to get complex attributes
	 */
	private Map getComplexAttributeStructure(MultiHashMap mmap, String id, String attributeName,
			Object[] keys, int keysIndex, int numKeyDimensions) {
		// are we done?
		if (keysIndex == numKeyDimensions)
			return null;

		// the hashmap to return
		Map keyHashMap = new HashMap();

		// create a new object array to store keys for this interation
		// copy all existing keys into it
		Object[] newKeys = new Object[keysIndex + 1];

		for (int lc = 0; lc < keysIndex; lc++) {
			newKeys[lc] = keys[lc];
		}

		// get the key span
		Iterator keyspan = mmap.getAttributeKeyspan(id, attributeName, keys);

		while (keyspan.hasNext()) {
			Object newKey = keyspan.next();
			newKeys[keysIndex] = newKey;

			Map nextLevelMap = getComplexAttributeStructure(mmap, id, attributeName, newKeys,
					keysIndex + 1, numKeyDimensions);
			Object objectToStore = (nextLevelMap == null)
			? mmap.getAttributeValue(id, attributeName, newKeys) : nextLevelMap;
			keyHashMap.put(newKey, objectToStore);
		}
		return keyHashMap;
	}

	private void addLitSearchRef(PathwayElement line, Map litMap) {
		for(Object key : litMap.keySet()) {
			String link = key.toString();
			//Parse pubmed id from
			//http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=18700251
			Pattern p = Pattern.compile("db=pubmed&dopt=Abstract&list_uids=(\\d+)");
			Matcher m = p.matcher(link);
			String pmid = null;
			if(m.find()) {
				pmid = m.group(1);
			}
									
			Map valueMap = (HashMap)litMap.get(key);
			Map litInfo = (HashMap)valueMap.get(1);
			String journal = litInfo.get(0).toString();
			String year = litInfo.get(4).toString();
			
			PublicationXRef xref = new PublicationXRef();
			if(pmid != null) xref.setPubmedId(pmid);
			xref.setSource(journal);
			xref.setYear(year);
			
			line.getBiopaxReferenceManager().addElementReference(xref);
		}
	}
	
	private void mapEdgeArrows(CyNetwork network, CyEdge edge, PathwayElement pwElm) {
		//Map vizmapper arrows (will be overridden by attribute mapper)
		VisualStyle vs = Cytoscape.getVisualMappingManager().getVisualStyle();
		EdgeAppearance app = vs.getEdgeAppearanceCalculator().calculateEdgeAppearance(edge, network);
		ArrowShape arrow = (ArrowShape)app.get(VisualPropertyType.EDGE_TGTARROW_SHAPE);
		LineType type = LineType.fromName(GpmlVisualStyle.getArrowToAttribute().get(arrow));
		if(type != null) {
			pwElm.setEndLineType(type);
		}
		arrow = (ArrowShape)app.get(VisualPropertyType.EDGE_SRCARROW_SHAPE);
		type = LineType.fromName(GpmlVisualStyle.getArrowToAttribute().get(arrow));
		if(type != null) {
			pwElm.setStartLineType(type);
		}
	}
	
	public void drag(Clipboard clipboard) {
		CyNetwork network = Cytoscape.getCurrentNetwork();
		CyNetworkView nview = Cytoscape.getCurrentNetworkView();

		Set<CyEdge> selEdges = network.getSelectedEdges();
		Set<CyNode> selNodes = network.getSelectedNodes();

		List<PathwayElement> gpmlElements = new ArrayList<PathwayElement>();
		Map<CyNode, PathwayElement> node2element = new HashMap<CyNode, PathwayElement>();

		//Keep reference to a pathway and biopax element for adding
		//biopax references
		Pathway pathway = new Pathway();
		
		//Process nodes
		for(CyNode node : selNodes) {
			GpmlNode gn = gpmlHandler.createNode(nview.getNodeView(node));
			PathwayElement pwe = gn.getPathwayElement(nview, gpmlHandler.getAttributeMapper()).copy();
			gpmlElements.add(pwe);
			node2element.put(node, pwe);
		}

		//Process edges
		for(CyEdge edge : selEdges) {
			//Don't copy edges that connect anchor nodes
			if(gpmlHandler.isAnchorEdge(edge)) {
				continue;
			}

			//Don't copy edges that connect to groups
			if(gpmlHandler.isGroupEdge(edge)) {
				continue;
			}
			
			//Don't copy edges that don't connect to selected nodes
			Node source = edge.getSource();
			Node target = edge.getTarget();
			if(!selNodes.contains(source) || !selNodes.contains(target)) {
				continue;
			}

			GpmlEdge ge = gpmlHandler.createEdge(nview.getEdgeView(edge));
			PathwayElement line = ge.getPathwayElement(nview, gpmlHandler.getAttributeMapper()).copy();
			gpmlElements.add(line);

			//Check for litsearch references
			String attributeName = "TextSourceInfo";
			CyAttributes attr = Cytoscape.getEdgeAttributes();
			byte attrType = attr.getType(attributeName);
			
			//Check if the attribute has the correct type
			if(attrType == CyAttributes.TYPE_COMPLEX) {
				MultiHashMap mh = attr.getMultiHashMap();
				MultiHashMapDefinition mhd = attr.getMultiHashMapDefinition();
				Map litMap = getComplexAttributeStructure(
						mh, edge.getIdentifier(), attributeName, 
						null, 0, mhd.getAttributeKeyspaceDimensionTypes(attributeName).length
				);
				if(litMap != null) {
					System.err.println("Adding litsearch references");
					pathway.add(line);
					addLitSearchRef(line, litMap);
				}
			}
			
			//Check for visual style arrows
			//Only apply visual style if no linestyle is defined by attribute mapping
			if(line.getStartLineType() != LineType.LINE && line.getEndLineType() != LineType.LINE) {
				mapEdgeArrows(network, edge, line);
			}
		}

		//Process BubbleRouter groups
		Set<CyNode> groupedNodes = new HashSet<CyNode>();
		Set<String> groupIds = new HashSet<String>();

		//Sort on group size. Add element to smallest group if it's in multiple
		//groups (GPML doesn't support an object to be added to multiple groups)
		List<CyGroup> groups = new ArrayList<CyGroup>(CyGroupManager.getGroupList());
		Collections.sort(groups, new Comparator<CyGroup>() {
			public int compare(CyGroup o1, CyGroup o2) {
				return o1.getNodes().size() - o2.getNodes().size();
			}
		});

		for(CyGroup g : groups) {
			//Only include BubbleRouter groups
			if(!"bubbleRouter".equals(g.getViewer())) continue;
			
			PathwayElement gpmlGroup = null;
			Set<PathwayElement> groupElements = new HashSet<PathwayElement>();

			for(CyNode n : selNodes) {
				if(g.contains(n) && !groupedNodes.contains(n)) {
					if(gpmlGroup == null) {
						gpmlGroup = PathwayElement.createPathwayElement(ObjectType.GROUP);
						gpmlGroup.setGroupId(pathway.getUniqueId(groupIds));
						gpmlGroup.setTextLabel(g.getGroupName());
					}
					PathwayElement pwe = node2element.get(n);
					pwe.setGroupRef(gpmlGroup.getGroupId());
					groupElements.add(pwe);
					groupedNodes.add(n);
				}
			}
			if(gpmlGroup != null && groupElements.size() > 0) {
				gpmlElements.add(gpmlGroup);

				Rectangle2D bounds = null;
				for(PathwayElement e : groupElements) {
					if(bounds == null) bounds = e.getMBounds();
					else bounds.add(e.getMBounds());
				}

				PathwayElement label = PathwayElement.createPathwayElement(ObjectType.LABEL);
				label.setTextLabel(g.getGroupName());
				label.setMWidth(bounds.getWidth());
				label.setMCenterX(bounds.getCenterX());
				label.setMTop(bounds.getY() - vToM(8));
				label.setMHeight(vToM(10));
				label.setGroupRef(gpmlGroup.getGroupId());
				gpmlElements.add(label);
			}
		}

		//Shift all coordinates, so that the NW corner is at 0,0 (instead of the center)
		Point2D vOrigin = getViewOrigin(nview, selNodes);

		int border = 50;
		vOrigin.setLocation(vOrigin.getX() - border, vOrigin.getY() - border);
		Point2D mOrigin = new Point2D.Double(vToM(vOrigin.getX()), vToM(vOrigin.getY()));
		for(PathwayElement pe : gpmlElements) {
			pe.setMCenterX(pe.getMCenterX() - mOrigin.getX());
			pe.setMCenterY(pe.getMCenterY() - mOrigin.getY());
			pe.setMStartX(pe.getMStartX() - mOrigin.getX());
			pe.setMStartY(pe.getMStartY() - mOrigin.getY());
			pe.setMEndX(pe.getMEndX() - mOrigin.getX());
			pe.setMEndY(pe.getMEndY() - mOrigin.getY());
		}

		if(pathway.getBiopax() != null) {
			gpmlElements.add(pathway.getBiopax());
		}
		
		//Ensure unique ids
		Map<String, List<MPoint>> graphRefs = new HashMap<String, List<MPoint>>();
		for(PathwayElement pe : gpmlElements) {
			for(MPoint p : pe.getMPoints()) {
				if(p.getGraphRef() != null) {
					List<MPoint> l = graphRefs.get(p.getGraphRef());
					if(l == null) {
						graphRefs.put(p.getGraphRef(), l = new ArrayList<MPoint>());
					}
					l.add(p);
				}
			}
		}

		Map<String, Integer> idCounts = new HashMap<String, Integer>();
		for(PathwayElement pe : gpmlElements) {
			int count = idCounts.containsKey(pe.getGraphId()) ? idCounts.get(pe.getGraphId()) : 0;
			idCounts.put(pe.getGraphId(), ++count);
			for(MAnchor ma : pe.getMAnchors()) {
				count = idCounts.containsKey(pe.getGraphId()) ? idCounts.get(pe.getGraphId()) : 0;
				idCounts.put(ma.getGraphId(), ++count);
			}
		}
		for(PathwayElement pe : gpmlElements) {
			fixGraphIds(pe, idCounts, graphRefs);
			for(MAnchor ma : pe.getMAnchors()) {
				fixGraphIds(ma, idCounts, graphRefs);
			}
		}

		PathwayTransferable content = new PathwayTransferable(gpmlElements);
		clipboard.setContents(content, new ClipboardOwner() {
			public void lostOwnership(Clipboard clipboard, Transferable contents) {

			}
		});
	}

	private void fixGraphIds(GraphIdContainer idc, Map<String, Integer> graphIds, Map<String, List<MPoint>> graphRefs) {
		Pathway dummyPathway = new Pathway(); //TODO: make getUniqueId static

		String gid = idc.getGraphId();
		int count = graphIds.containsKey(gid) ? graphIds.get(gid) : 0;
		if(count > 1) {
			String newId = dummyPathway.getUniqueId(graphIds.keySet());
			idc.setGraphId(newId);
			graphIds.put(gid, --count);
			List<MPoint> mpoints = graphRefs.get(gid);
			if(mpoints != null) {
				for(MPoint mp : mpoints) mp.setGraphRef(newId);
			}
		}
	}

	public void writeToFile(GraphView view, File file) throws ConverterException {
		Iterator<NodeView> itn = view.getNodeViewsIterator();
		while(itn.hasNext()) {
			gpmlHandler.createNode(itn.next());
		}
		Iterator<EdgeView> ite = view.getEdgeViewsIterator();
		while(ite.hasNext()) {
			gpmlHandler.createEdge(ite.next());
		}
		Pathway p = gpmlHandler.createPathway(view);
		p.writeToXml(file, true);
	}

	private Point2D getViewOrigin(GraphView view, Set<CyNode> nodes) {
		Point2D origin = new Point2D.Double(Double.MAX_VALUE, Double.MAX_VALUE);

		for(CyNode n : nodes) {
			NodeView nv = view.getNodeView(n);

			if(nv == null) continue;

			GpmlNode gn = gpmlHandler.createNode(nv);
			if(gn.isAnnotation(view)) continue; //Don't include annotations, these
			//are hidden nodes and return NaN as position
			double x = nv.getXPosition();
			double y = nv.getYPosition();
			if(!Double.isNaN(x))
				x = Math.min(x, origin.getX());
			else
				x = origin.getX();
			if(!Double.isNaN(y))
				y = Math.min(y, origin.getY());
			else
				y = origin.getY();

			origin.setLocation(
					x,
					y
			);
		}

		Logger.log.trace("Origin: " + origin);
		return origin;
	}

	public void propertyChange(PropertyChangeEvent e) {
		//Register droplistener to new canvas
		if (e.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_CREATED)) {
			DGraphView view = (DGraphView) e.getNewValue();
			((InnerCanvas)view.getCanvas()).addPhoebeCanvasDropListener(this);
		}
		super.propertyChange(e);
	}
}
