package org.pathvisio.model;

/**
 * Line specific implementation of methods that calculate derived
 * coordinates that are not stored in GPML directly
 * @author thomas
 */
public class MLine extends PathwayElement {
	public MLine() {
		super(ObjectType.LINE);
	}
	
	public double getMCenterX()
	{
		double start = getMStart().getX();
		double end = getMEnd().getX();
		return start + (end - start) / 2;
	}
	
	public double getMCenterY()
	{
		double start = getMStart().getY();
		double end = getMEnd().getY();
		return start + (end - start) / 2;
	}
	
	public double getMLeft()
	{
		double start = getMStart().getX();
		double end = getMEnd().getX();
		return Math.min(start, end);
	}
	
	public double getMWidth()
	{
		double start = getMStart().getX();
		double end = getMEnd().getX();
		return Math.abs(start-end);
	}
	
	public double getMHeight()
	{
		double start = getMStart().getY();
		double end = getMEnd().getY();
		return Math.abs(start-end);
	}	
	
	public double getMTop()
	{
		double start = getMStart().getY();
		double end = getMEnd().getY();
		return Math.min(start, end);
	}
	
	/**
	 * Sets the position of the top side
	 * of the rectangular bounds of the line
	 */
	public void setMTop(double v) {
		if(getDirectionY() > 0) {
			setMStartY(v);
		} else {
			setMEndY(v);
		}
	}
	
	/**
	 * Sets the position of the left side
	 * of the rectangular bounds of the line
	 */
	public void setMLeft(double v) {
		if(getDirectionX() > 0) {
			setMStartX(v);
		} else {
			setMEndX(v);
		}
	}
	
	/**
	 * Sets the x position of the center of the line
	 */
	public void setMCenterX(double v) {
		double dx = v - getMCenterX();
		setMStartX(getMStartX() + dx);
		setMEndX(getMEndX() + dx);
	}
	
	/**
	 * Sets the y position of the center of the line
	 */
	public void setMCenterY(double v) {
		double dy = v - getMCenterY();
		setMStartY(getMStartY() + dy);
		setMEndY(getMEndY() + dy);
	}
	
	
	private int getDirectionX() {
		return (int)Math.signum(getMEndX() - getMStartX());
	}
	
	private int getDirectionY() {
		return (int)Math.signum(getMEndY() - getMStartY());
	}
}
