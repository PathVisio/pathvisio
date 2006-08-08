package graphics;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;

import util.LinAlg;
import util.LinAlg.Point;
import data.GmmlData;

/**
 * This is an {@link GmmlGraphics} class representing shapelike forms,
 * which all have the same type of handles (8 handles placed in a rectangle
 * arount the shape)
 */
public abstract class GmmlGraphicsShape extends GmmlGraphics {
	
	//Internal coordinates
	double startX;
	double startY;
	double width;
	double height;
	
	double rotation; //in radians
	
	//Side handles
	GmmlHandle handleN;
	GmmlHandle handleE;
	GmmlHandle handleS;
	GmmlHandle handleW;
	//Corner handles
	GmmlHandle handleNE;
	GmmlHandle handleSE;
	GmmlHandle handleSW;
	GmmlHandle handleNW;
	//Rotation handle
	GmmlHandle handleR;
	
	final GmmlHandle[][] handleMatrix;
	
	public GmmlGraphicsShape(GmmlDrawing canvas) {
		super(canvas);
		
		handleN	= new GmmlHandle(GmmlHandle.DIRECTION_Y, this, canvas);
		handleE	= new GmmlHandle(GmmlHandle.DIRECTION_X, this, canvas);
		handleS	= new GmmlHandle(GmmlHandle.DIRECTION_Y, this, canvas);
		handleW	= new GmmlHandle(GmmlHandle.DIRECTION_X, this, canvas);
		
		handleNE	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
		handleSE	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
		handleSW	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
		handleNW	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
		
		handleR = new GmmlHandle(GmmlHandle.DIRECTION_ROT, this, canvas);
		
		handleMatrix = new GmmlHandle[][] {
				{ handleNW, 	handleNE },
				{ handleSW, 	handleSE }};
	}
		
	public int getCenterX() { return (int)(startX + width/2); }
	public int getCenterY() { return (int)(startY + height/2); }
	
	public void moveBy(double dx, double dy)
	{
		markDirty();
		startX += dx; 
		startY += dy;
		markDirty();
		setHandleLocation();		
	}
				
	public GmmlHandle[] getHandles()
	{
		return new GmmlHandle[] {
				handleN, handleNE, handleE, handleSE,
				handleS, handleSW, handleW,	handleNW,
				handleR
		};
	}
	
	//Translate to internal coordinate system
	private Point toInternal(Point p) {
		Point pt = relativeToCenter(p);
		Point pr = LinAlg.rotate(pt, -rotation);
		return pr;
	}
	
	//Translate to external coordinate system
	private Point toExternal(Point p) {
		Point pr = LinAlg.rotate(p, rotation);
		Point pt = relativeToCanvas(pr);
		return pt;
	}
	
	private Point toExternal(double x, double y) {
		return toExternal(new Point(x, y));
	}
				
	private Point relativeToCenter(Point p) {
		return p.subtract(getCenter());
	}
	
	private Point relativeToCanvas(Point p) {
		return p.add(getCenter());
	}
	
	public Point getCenter() {
		return new Point(startX + width/2, startY + height/2);
	}
	
	public void setCenter(Point cn) {
		startX = cn.x - width/2;
		startY = cn.y - height/2;
	}
	
	public Point calcNewCenter(double newWidth, double newHeight) {
		Point cn = new Point((newWidth - width)/2, (newHeight - height)/2);
		Point cr = LinAlg.rotate(cn, rotation);
		return relativeToCanvas(cr);
	}
	
	public void setRotation(double angle) {
		rotation = angle;
		if(angle < 0) rotation += Math.PI*2;
		if(angle > Math.PI*2) rotation -= Math.PI*2;
	}
	
	public void rotateGC(GC gc, Transform tr) {
		tr.translate(getCenterX(), getCenterY());
		tr.rotate((float)Math.toDegrees(-rotation));	
		tr.translate(-getCenterX(), -getCenterY());
		gc.setTransform(tr);
	}
	
	public void adjustToHandle(GmmlHandle h) {
		
		//Rotation
		if 	(h == handleR) {
			Point def = relativeToCenter(getHandleLocation(h));
			Point cur = relativeToCenter(new Point(h.centerx, h.centery));
			
			setRotation(rotation - LinAlg.angle(def, cur));
		
			setHandleLocation(h);
			return;
		}
					
		// Transformation
		Point hi = toInternal(new Point(h.centerx, h.centery));
		
		double dx = 0;
		double dy = 0;
		double dw = 0;
		double dh = 0;
			
		if	(h == handleN || h == handleNE || h == handleNW) {
			dy = -(hi.y + height/2);
			dh = -dy;
		}
		if	(h == handleS || h == handleSE || h == handleSW ) {
			dy = hi.y - height/2;
			dh = dy;
		}
		if	(h == handleE || h == handleNE || h == handleSE) {
			dx = hi.x - width/2;
			dw = dx;
		}
		if	(h == handleW || h == handleNW || h== handleSW) {
			dx = -(hi.x + width/2);
			dw = -dx;
		};
		
		Point nc = calcNewCenter(width + dw, height + dh);
		height += dy;
		width += dx;
		setCenter(nc);		
	
		//In case object had zero width, switch handles
		if(width < 0) {
			negativeWidth(h);
		}
		if(height < 0) {
			negativeHeight(h);
		}		
		
		setHandleLocation(h);
	}
	
	public void negativeWidth(GmmlHandle h) {
		h = getOppositeHandle(h, GmmlHandle.DIRECTION_X);
		width = -width;
		startX -= width;
		canvas.setPressedObject(h);
	}
	
	public void negativeHeight(GmmlHandle h) {
		h = getOppositeHandle(h, GmmlHandle.DIRECTION_Y);
		height = -height;
		startY -= height;
		canvas.setPressedObject(h);
	}
	
	/**
	 * Sets the handles at the correct location;
	 * @param ignore the position of this handle will not be adjusted
	 */
	private void setHandleLocation(GmmlHandle ignore)
	{
		Point p;
		p = getHandleLocation(handleN);
		if(ignore != handleN) handleN.setLocation(p.x, p.y);
		p = getHandleLocation(handleE);
		if(ignore != handleE) handleE.setLocation(p.x, p.y);
		p = getHandleLocation(handleS);
		if(ignore != handleS) handleS.setLocation(p.x, p.y);
		p = getHandleLocation(handleW);
		if(ignore != handleW) handleW.setLocation(p.x, p.y);
		
		p = getHandleLocation(handleNE);
		if(ignore != handleNE) handleNE.setLocation(p.x, p.y);
		p = getHandleLocation(handleSE);
		if(ignore != handleSE) handleSE.setLocation(p.x, p.y);
		p = getHandleLocation(handleSW);
		if(ignore != handleSW) handleSW.setLocation(p.x, p.y);
		p = getHandleLocation(handleNW);
		if(ignore != handleNW) handleNW.setLocation(p.x, p.y);

		p = getHandleLocation(handleR);
		if(ignore != handleR) handleR.setLocation(p.x, p.y);
		
		for(GmmlHandle h : getHandles()) h.rotation = rotation;
	}
	
	private Point getHandleLocation(GmmlHandle h) {
		if(h == handleN) return toExternal(0, -height/2);
		if(h == handleE) return toExternal(width/2, 0);
		if(h == handleS) return toExternal(0,  height/2);
		if(h == handleW) return toExternal(-width/2, 0);
		
		if(h == handleNE) return toExternal(width/2, -height/2);
		if(h == handleSE) return toExternal(width/2, height/2);
		if(h == handleSW) return toExternal(-width/2, height/2);
		if(h == handleNW) return toExternal(-width/2, -height/2);

		if(h == handleR) return toExternal(width/2 + (30*getDrawing().getZoomFactor()), 0);
		return null;
	}

	/**
	 * Sets the handles at the correct location;
	 * left border.
	 */
	public void setHandleLocation()
	{
		setHandleLocation(null);
	}
	
	/**
	 * Gets the handle opposite to the given handle.
	 * For directions N, E, S and W this is always their complement,
	 * for directions NE, NW, SE, SW, you can constraint the direction, e.g.:
	 * if direction is X, the opposite of NE will be NW instead of SW
	 * @param h	The handle to find the opposite for
	 * @param direction	Constraints on the direction, one of {@link GmmlHandle}#DIRECTION_*.
	 * Will be ignored for N, E, S and W handles
	 * @return	The opposite handle
	 */
	GmmlHandle getOppositeHandle(GmmlHandle h, int direction) {
		//Ignore direction for N, E, S and W
		if(h == handleN) return handleS;
		if(h == handleE) return handleW;
		if(h == handleS) return handleN;
		if(h == handleW) return handleE;
				
		int[] pos = handleFromMatrix(h);
		switch(direction) {
		case GmmlHandle.DIRECTION_XY:
			return handleMatrix[ Math.abs(pos[0] - 1)][ Math.abs(pos[1] - 1)];
		case GmmlHandle.DIRECTION_Y:
			return handleMatrix[ Math.abs(pos[0] - 1)][pos[1]];
		case GmmlHandle.DIRECTION_X:
			return handleMatrix[ pos[0]][ Math.abs(pos[1] - 1)];
		default:
			return null;
		}
	}
	
	int[] handleFromMatrix(GmmlHandle h) {
		for(int x = 0; x < 2; x++) {
			for(int y = 0; y < 2; y++) {
				if(handleMatrix[x][y] == h) return new int[] {x,y};
			}
		}
		return null;
	}
	
	/**
	 * Creates a polygon containing the GmmlShape
	 */
	public Shape getOutline()
	{		
		int[] x = new int[4];
		int[] y = new int[4];
		
		int[] p = getHandleLocation(handleNE).asIntArray();
		x[0] = p[0]; y[0] = p[1];
		p = getHandleLocation(handleSE).asIntArray();
		x[1] = p[0]; y[1] = p[1];
		p = getHandleLocation(handleSW).asIntArray();
		x[2] = p[0]; y[2] = p[1];
		p = getHandleLocation(handleNW).asIntArray();
		x[3] = p[0]; y[3] = p[1];
		
		Polygon pol = new Polygon(x, y, 4);
		return pol;
	}
	
	protected boolean isContain(Point2D point)
	{
		return getOutline().contains(point);
	}	

	protected boolean intersects(Rectangle2D.Double r)
	{
		return getOutline().intersects(r);
	}
	
	protected Rectangle getBounds()
	{
		return getOutline().getBounds();
	}
	
	/*
	Methods for compatibility with GMML elements that use centerx,centery instead of startx, starty
	Will be removed when GMML is updated
	*/
	double centerX;
	double centerY;
	/**
	 * Method for compatibility with GMML element, which uses centerx,centery instead of startx,starty.
	 * Call this method after setting width, height and centerx, centery.
	 * Will be removed when GMML is updated and all shape-like elements have startx,starty
	 */
	public void calcStart() {
		startX = centerX - width/2;
		startY = centerY - height/2;
	}
	
	/**
	 * Method for compatibility with GMML element, which uses centerx,centery instead of startx,starty.
	 * Call this method after setting width, height.
	 * Will be removed when GMML is updated and all shape-like elements have startx,starty
	 * @param cx x-coördinate of the center
	 * @param cy y-coördinate of the center
	 */
	public void calcStart(double cx, double cy) {
		centerX = cx;
		centerY = cy;
		calcStart();
	}
	
	/**
	 * Sets the width of the graphical representation.
	 * This differs from the GMML representation:
	 * in GMML height and width are radius, here for all shapes the width is diameter
	 * TODO: change to diameter in gmml
	 * @param gmmlWidth the width as specified in the GMML representation
	 */
	public void setGmmlWidth(double gmmlWidth) {
		this.width = gmmlWidth*2;
	}
	
	/**
	 * Get the width as stored in GMML
	 * @return
	 */
	public int getGmmlWidth() {
		return (int)((this.width * GmmlData.GMMLZOOM)/2);
	}
	
	/**
	 * Sets the height of the graphical representation.
	 * This differs from the GMML representation:
	 * in GMML height and width are radius, here for all shapes the width is diameter
	 * TODO: change to diameter in gmml
	 *  @param gmmlHeight the height as specified in the GMML representation
	 */
	public void setGmmlHeight(double gmmlHeight) {
		this.height = gmmlHeight*2;
	}
	
	/**
	 * Get the height as stored in GMML
	 * @return
	 */
	public int getGmmlHeight() {
		return (int)((this.height * GmmlData.GMMLZOOM)/2);
	}
}
