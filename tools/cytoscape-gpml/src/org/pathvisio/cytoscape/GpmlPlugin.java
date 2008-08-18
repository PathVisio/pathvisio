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
package org.pathvisio.cytoscape;

import giny.view.EdgeView;
import giny.view.GraphView;
import giny.view.NodeView;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import org.pathvisio.cytoscape.actions.AttributeMapperAction;
import org.pathvisio.cytoscape.actions.CopyAction;
import org.pathvisio.cytoscape.actions.ExportAction;
import org.pathvisio.cytoscape.actions.PasteAction;
import org.pathvisio.cytoscape.actions.ToggleAnnotationAction;
import org.pathvisio.cytoscape.wikipathways.WikiPathwaysClient;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;
import org.pathvisio.view.MIMShapes;
import org.pathvisio.view.swing.PathwayTransferable;

import phoebe.PhoebeCanvasDropEvent;
import phoebe.PhoebeCanvasDropListener;
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.ImportHandler;
import cytoscape.data.readers.GraphReader;
import cytoscape.data.webservice.WebServiceClientManager;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CyFileFilter;
import cytoscape.view.CyMenus;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import ding.view.DGraphView;
import ding.view.InnerCanvas;

public class GpmlPlugin extends CytoscapePlugin implements PhoebeCanvasDropListener, PropertyChangeListener {
	GpmlHandler gpmlHandler;

	public GpmlPlugin() {
		Logger.log.setLogLevel(true, true, true, true, true, true);
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
		
		JMenu pluginMenu = menu.getOperationsMenu();
		JMenu gpmlMenu = new JMenu("Gpml plugin");
		gpmlMenu.add(new ToggleAnnotationAction(gpmlHandler));
		gpmlMenu.add(new AttributeMapperAction(this));
		pluginMenu.add(gpmlMenu);
		
		WebServiceClientManager.registerClient(new WikiPathwaysClient(this));
	}

	public GpmlHandler getGpmlHandler() {
		return gpmlHandler;
	}

	class GpmlFilter extends CyFileFilter {
		public GpmlFilter() {
			super("gpml", "GPML file", ImportHandler.GRAPH_NATURE);
		}

		public GraphReader getReader(String fileName) {
			return new GpmlReader(fileName, gpmlHandler);
		}
	}

	static double mToV(double m) {
		return m * 1.0/15; //Should be stored in the model somewhere (pathvisio)
	}

	static double vToM(double v) {
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

	public void drag(Clipboard clipboard) {
		CyNetwork network = Cytoscape.getCurrentNetwork();
		CyNetworkView nview = Cytoscape.getCurrentNetworkView();

		Set<CyEdge> selEdges = network.getSelectedEdges();
		Set<CyNode> selNodes = network.getSelectedNodes();

		List<PathwayElement> gpmlElements = new ArrayList<PathwayElement>();

		for(CyNode node : selNodes) {
			GpmlNode gn = gpmlHandler.createNode(nview.getNodeView(node));
			gpmlElements.add(gn.getPathwayElement(nview, gpmlHandler.getAttributeMapper()).copy());
		}
		
		for(CyEdge edge : selEdges) {
			//Don't copy edges that connect anchor nodes
			if(gpmlHandler.isAnchorEdge(edge)) {
				continue;
			}
			
			GpmlEdge ge = gpmlHandler.createEdge(nview.getEdgeView(edge));
			gpmlElements.add(ge.getPathwayElement(nview, gpmlHandler.getAttributeMapper()).copy());
		}

		//Shift all coordinates, so that the NW corner is at 0,0 (instead of the center)
		Point2D vOrigin = getViewOrigin(nview);
		int border = 20;
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

		//Ensure unique ids
		HashMap<String, List<MPoint>> graphRefs = new HashMap<String, List<MPoint>>();
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

	private void fixGraphIds(GraphIdContainer idc, Map<String, Integer> graphIds, HashMap<String, List<MPoint>> graphRefs) {
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

	private Point2D getViewOrigin(GraphView view) {
		Point2D origin = new Point2D.Double(0, 0);
		Iterator<NodeView> it = view.getNodeViewsIterator();
		while(it.hasNext()) {
			NodeView nv = it.next();

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
