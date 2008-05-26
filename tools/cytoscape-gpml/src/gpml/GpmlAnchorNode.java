package gpml;

import giny.view.GraphView;
import giny.view.NodeView;

import java.awt.geom.Point2D;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.MLine;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MAnchor;

import cytoscape.CyNode;

/**
 * Class that holds a Cytoscape edge that has a GPML representation, which is stored
 * as edge attributes
 * @author thomas
 *
 */
public class GpmlAnchorNode extends GpmlNode {
	/**
	 * Constructor for this class. Creates a new GpmlNode, based on the given
	 * node and line
	 */
	public GpmlAnchorNode(CyNode parent, PathwayElement e, AttributeMapper attributeMapper) {
		super(parent, e, attributeMapper);
	}
	
	private MLine getMLine() {
		return (MLine)getPathwayElement();
	}
	
	protected void resetPosition(GraphView view) {
		NodeView nv = view.getNodeView(parent);
		if(nv == null) {
			Logger.log.trace("Null node view found in GpmlNode.resetPosition, a group?");
			return;
		}
		MAnchor anchor = getPathwayElement().getMAnchors().get(0);
		Point2D p = getMLine().getConnectorShape().fromLineCoordinate(anchor.getPosition());
		Logger.log.trace("Setting position of anchor node to: " + p);
		nv.setXPosition(GpmlPlugin.mToV(p.getX()), false);
		nv.setYPosition(GpmlPlugin.mToV(p.getY()), false);
	}
	
	public void addAnnotation(GraphView view) {
		//Do nothing
	}
}
