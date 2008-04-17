package org.pathvisio.view;

import java.awt.geom.Line2D;

public class StraightConnectorShape implements ConnectorShape {

	public java.awt.Shape getShape(ConnectorRestrictions restrictions) {
		Line2D line = new Line2D.Double(
				restrictions.getStartPoint(),
				restrictions.getEndPoint()
		);
		return line;
	}
	
	public Segment[] getSegments(ConnectorRestrictions restrictions) {
		return new Segment[] {
				new Segment(restrictions.getStartPoint(), restrictions.getEndPoint())
		};
	}

	public boolean isUsePreferredSegments(ConnectorRestrictions restrictions) {
		return false;
	}
}
