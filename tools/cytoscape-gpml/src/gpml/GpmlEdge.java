package gpml;

import giny.view.EdgeView;
import giny.view.GraphView;

import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.LinAlg;
import org.pathvisio.view.LinAlg.Point;

import cytoscape.CyEdge;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

/**
 * Class that holds a Cytoscape edge that has a GPML representation, which is stored
 * as edge attributes
 * @author thomas
 *
 */
public class GpmlEdge extends GpmlNetworkElement<CyEdge> {
	GpmlNode source;
	GpmlNode target;
	
	/**
	 * Constructor for this class. Creates a new GpmlEdge, based on the given
	 * edge and PathwayElement
	 * @param parent
	 * @param pwElm
	 */
	public GpmlEdge(CyEdge parent, PathwayElement pwElm, GpmlNode source, 
			GpmlNode target, AttributeMapper attributeMapper) {
		super(parent, pwElm);
		this.source = source;
		this.target = target;
		resetToGpml(attributeMapper);
	}
		
	/**
	 * Creates a new GpmlEdge based on the given node view. A GPML representation
	 * (PathwayElement of type Line) will automatically created based on the edge view.
	 * @param parent
	 */
	public GpmlEdge(EdgeView eview, GpmlNode source, GpmlNode target, AttributeMapper attributeMapper) {
		this((CyEdge)eview.getEdge(), PathwayElement.createPathwayElement(ObjectType.LINE), source, target, attributeMapper);
		PathwayElement psource = source.getPathwayElement();
		PathwayElement ptarget = target.getPathwayElement();

		pwElmOrig.setStartGraphRef(psource.getGraphId());
		pwElmOrig.setEndGraphRef(ptarget.getGraphId());
	
		pwElmOrig.setMStartX(psource.getMCenterX());
		pwElmOrig.setMStartY(psource.getMCenterY());
		pwElmOrig.setMEndX(ptarget.getMCenterX());
		pwElmOrig.setMEndY(ptarget.getMCenterY());
		
		resetToGpml(attributeMapper);	
		//TODO: map line style and arrowheads
	}

	public CyAttributes getCyAttributes() {
		return Cytoscape.getEdgeAttributes();
	}

	public String getParentIdentifier() {
		return getParent().getIdentifier();
	}

	public void updateFromCytoscape(GraphView view, AttributeMapper attributeMapper) {
		super.updateFromCytoscape(view, attributeMapper);
		
		PathwayElement psource = source.getPathwayElement(view, attributeMapper);
		PathwayElement ptarget = target.getPathwayElement(view, attributeMapper);

		getPwElmCy().setStartGraphRef(psource.getGraphId());
		getPwElmCy().setEndGraphRef(ptarget.getGraphId());
				
		fixCoordinates(getPwElmCy(), psource, ptarget);
	}
	
	private Point findBorder(PathwayElement pwElm, double angle) {
		Point bp = new Point(pwElm.getMLeft(), pwElm.getMTop());
		
		double diagAngle = Math.atan(pwElm.getMHeight() / pwElm.getMWidth());
		double angleA = Math.abs(angle);
		   /*    da < |a| < da + pi/2
		       \   /
		        \ /
|a| > da + pi/2	 \  |a| < da 
		        / \
		       /   \
		         da < |a| < da + pi/2
		*/
		
		if(angleA > diagAngle && angleA <= diagAngle + Math.PI/2) {
			bp.x = pwElm.getMCenterX();
			if(angle < 0) {
				bp.y += pwElm.getMHeight();
			}
		}
		if(angleA <= diagAngle || angleA > diagAngle + Math.PI/2) {
			bp.y = pwElm.getMCenterY();
			if(angle < Math.PI / 2 && angle > -Math.PI / 2) {
				bp.x += pwElm.getMWidth();
			}
		}
		
		return bp;
	}
	
	private void fixCoordinates(PathwayElement toSet, PathwayElement source, PathwayElement target) {
		Point psource = new Point(source.getMCenterX(), source.getMCenterY());
		Point ptarget = new Point(target.getMCenterX(), target.getMCenterY());
		
		double angle = LinAlg.angle(ptarget.subtract(psource), new Point(1, 0));
		double astart = angle;
		double aend = angle;
		if(angle < 0) 	aend += Math.PI;
		else 			aend -= Math.PI;
		Point start = findBorder(source, astart);
		Point end = findBorder(target, aend);
		toSet.setMStartX(start.x);
		toSet.setMStartY(start.y);
		toSet.setMEndX(end.x);
		toSet.setMEndY(end.y);
	}
}
