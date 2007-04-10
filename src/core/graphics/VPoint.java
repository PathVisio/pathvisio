package graphics;

import java.awt.Shape;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;

import data.gpml.GmmlDataObject.MPoint;

public class VPoint extends GmmlDrawingObject {
	GmmlHandle handle;
	Set<GmmlLine> lines;
	Set<MPoint> mPoints;
	
	VPoint(GmmlDrawing canvas) {
		super(canvas);
		mPoints = new HashSet<MPoint>();
		lines = new HashSet<GmmlLine>();
		handle = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
	}
	
	public int getDrawingOrder() {
		return GmmlDrawing.DRAW_ORDER_LINE;
	}
	
	protected void addMPoint(MPoint p) {
		mPoints.add(p);
	}
	
	protected void removeMPoint(MPoint p) {
		mPoints.remove(p);
	}
	
	protected void addLine(GmmlLine l) {
		lines.add(l);
	}
	
	protected void removeLine(GmmlLine l) {
		lines.remove(l);
		//Remove this VPoint when it links to no lines no more
		if(lines.size() == 0) {
			destroy();
		}
	}
	
	protected Set<GmmlLine> getLines() { return lines; }
	
	protected void link(GmmlGraphics g) {
		if(lines.contains(g)) return; //Prevent linking to self
		String id = g.getGmmlData().getGraphId();
		if(id == null) id = g.getGmmlData().setGeneratedGraphId();
		for(MPoint p : mPoints) p.setGraphRef(id);
	}
	
	protected void link(VPoint p) {
		if(p == this) return; //Already linked
		for(MPoint mp : p.mPoints) {
			mPoints.add(mp);
		}
		for(GmmlLine l : p.lines) {
			l.swapPoint(p, this);
			addLine(l);
		}
		p.lines.clear();
		p.destroy();
	}
	
	protected double getVX() { return vFromM(getMPoint().getX()); }
	protected double getVY() { return vFromM(getMPoint().getY()); }
	
	protected void setVLocation(double vx, double vy) {
		for(MPoint p : mPoints) {
			p.setX(mFromV(vx));
			p.setY(mFromV(vy));
		}
	}
	
	protected void vMoveBy(double dx, double dy) {
		for(MPoint p : mPoints) {
			p.moveBy(mFromV(dx), mFromV(dy));
		}
	}
	
	protected void setHandleLocation() {
		MPoint mp = getMPoint();
		handle.setMLocation(mp.getX(), mp.getY());
	}
	
	private MPoint getMPoint() {
		for(MPoint p : mPoints) return p;
		return null;
	}
	
	protected void adjustToHandle(GmmlHandle h) {
		double mcx = h.mCenterx;
		double mcy = h.mCentery;
		for(MPoint p : mPoints) {
			p.setX(mcx);
			p.setY(mcy);
		}
	}
	
	protected GmmlHandle getHandle() {
		return handle;
	}
	
	protected GmmlHandle[] getHandles() {
		return new GmmlHandle[] { handle };
	}
	
	protected void draw(PaintEvent e) {
		// TODO Auto-generated method stub
	}

	public void draw(PaintEvent e, GC buffer) {
		// TODO Auto-generated method stub
	}

	protected Shape getVOutline() {
		return handle.getVOutline();
	}

	protected void destroy() {
		//Check if we can legally destroy this point
		if(lines.size() > 0) 
			throw new RuntimeException("VPoint cannot be destroyed: still linked to " + lines);

		super.destroy();
	}
}
