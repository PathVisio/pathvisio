package graphics;

import java.awt.geom.Line2D;

/**
 * This is an {@link GmmlGraphics} class representing line forms,
 * which all have a start and end coordinates + a start and end handle
 */
public abstract class GmmlGraphicsLine extends GmmlGraphics {
	GmmlHandle handleStart;
	GmmlHandle handleEnd;
	
	double startx;
	double starty;
	double endx;
	double endy;
		
	public GmmlGraphicsLine(GmmlDrawing canvas) {
		super(canvas);
		
		handleStart	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
		handleEnd	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
	}
	
	/**
	 * Constructs the line for the coordinates stored in this class
	 */
	public Line2D getLine()
	{
		return new Line2D.Double(startx, starty, endx, endy);
	}
	
	/**
	 * Sets the line start and end to the coordinates specified
	 * <DL><B>Parameters</B>
	 * <DD>Double x1	- new startx 
	 * <DD>Double y1	- new starty
	 * <DD>Double x2	- new endx
	 * <DD>Double y2	- new endy
	 */
	public void setLine(double x1, double y1, double x2, double y2)
	{
		startx = x1;
		starty = y1;
		endx   = x2;
		endy   = y2;
		
		setHandleLocation();		
	}
	
	/**
	 * Sets this class handles at the correct position 
	 */
	protected void setHandleLocation()
	{
		handleStart.setLocation(startx, starty);
		handleEnd.setLocation(endx, endy);
	}
	
	public GmmlHandle[] getHandles()
	{
		return new GmmlHandle[] { handleStart, handleEnd };
	}
	
	protected void adjustToHandle(GmmlHandle h) {
		markDirty();
		if		(h == handleStart) {
			startx = h.centerx; 
			starty = h.centery;
		}
		else if	(h == handleEnd) {
			endx = h.centerx;
			endy = h.centery;
		}
		markDirty();
	}
	
	protected void moveBy(double dx, double dy)
	{
		markDirty();
		setLine(startx + dx, starty + dy, endx + dx, endy + dy);
		markDirty();		
		setHandleLocation();
	}
	
//	protected void moveLineStart(double dx, double dy)
//	{
//		markDirty();
//		startx += dx;
//		starty += dy;
//		constructLine();
//		markDirty();
//		setHandleLocation();
//	}
//	
//	protected void moveLineEnd(double dx, double dy)
//	{
//		markDirty();
//		endx += dx;
//		endy += dy;
//		constructLine();
//		markDirty();
//		setHandleLocation();
//	}
	
	public void resizeX(double dx) {
		//TODO: implement
	}
	
	public void resizeY(double dy) {
		//TODO: implement
	}
	
	protected void adjustToZoom(double factor)
	{
		startx	*= factor;
		starty	*= factor;
		endx 	*= factor;
		endy	*= factor;
		
		setHandleLocation();
	}
}
