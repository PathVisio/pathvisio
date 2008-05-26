package gpml;

import giny.view.EdgeView;
import giny.view.GraphView;
import giny.view.NodeView;
import gpml.actions.CopyAction;
import gpml.actions.ExportAction;
import gpml.actions.PasteAction;
import gpml.actions.ToggleAnnotationAction;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.swing.PathwayTransferable;

import phoebe.PhoebeCanvasDropEvent;
import phoebe.PhoebeCanvasDropListener;
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.ImportHandler;
import cytoscape.data.readers.GraphReader;
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
		menu.addCytoscapeAction(new ToggleAnnotationAction(gpmlHandler));
		menu.addCytoscapeAction(new ExportAction(this));
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
	
	public void drop(Transferable transfer) {
			try {
				Pathway p = PathwayTransferable.pathwayFromTransferable(transfer);
				
				if(p == null) return; //No pathway in transferable
				
				GpmlConverter converter = new GpmlConverter(gpmlHandler, p);

				//Get the nodes/edges indexes
				int[] nodes = converter.getNodeIndicesArray();
				int[] edges = converter.getEdgeIndicesArray();
				
				//Get the current network, or create a new one, if none is available
				CyNetwork network = Cytoscape.getCurrentNetwork();
				if(network == Cytoscape.getNullNetwork()) {
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
			} catch(Exception ex) {
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
			gpmlElements.add(gn.getPathwayElement(nview, gpmlHandler.getAttributeMapper()));
		}
		for(CyEdge edge : selEdges) {
			GpmlEdge ge = gpmlHandler.createEdge(nview.getEdgeView(edge));
			gpmlElements.add(ge.getPathwayElement(nview, gpmlHandler.getAttributeMapper()));
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
		
		PathwayTransferable content = new PathwayTransferable(gpmlElements);
		clipboard.setContents(content, new ClipboardOwner() {
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
				
			}
		});
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
