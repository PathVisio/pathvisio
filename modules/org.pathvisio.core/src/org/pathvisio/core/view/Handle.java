/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * A Handle is a little marker (like a little
 * yellow square) that the user can grab with the mouse
 * and drag around, and in that way adjust some property
 * of an object (such as its width, rotation, etc.)
 */
public class Handle extends VPathwayElement
{
	//The direction this handle is allowed to move in
	final private Freedom freedom;

	/**
	 * Freedom determines the freedom of movement of
	 * a Handle. The freedom of a Handle is
	 * specified at creation time and does not change during
	 * the life of a Handle.
	 */
	public enum Freedom {
		/** a FREE handle can move to any location on the canvas. When shift is pressed, it maintains aspect ratio of the shape */
		FREE,
		/** a FREER handle can move to any location on the canvas, it move diagonally (perpendicular to XY) when SHIFT is pressed */
		NEGFREE,
		/** an X handle can only move horizontally */
		X,
		/** an Y handle can only move vertically */
		Y,
		/** a ROTATION handle can only move in a circle with a fixed radius */
		ROTATION,
	};

	// Typical size of handle, in pixels
	private static final int WIDTH 	= 8;
	private static final int HEIGHT	= 8;

	/**
	 * Style determines the visual appearance of a Handle
	 */
	enum Style
	{
		/**
		 * The default appearance: a yellow square,
		 * suitable for adjusting width / height / scale of an object
		 */
		DEFAULT,
		/** Appearance for handles on line segments, a blue diamond */
		SEGMENT,
		/** Appearance for rotation handles, a green circle */
		ROTATE,
		/** Invisible handles are on the corners of a selectionbox */
		INVISIBLE,
	};

	private Style style = Style.DEFAULT;

	final private Adjustable adjustable;
	final private VPathwayElement parent;

	// location of this handle
	private double mCenterx;
	private double mCentery;

	// used for calculations related to rotating parent objects
	double rotation;
	
	// the start angle of the handle without rotation 
	// (degrees of the angle in the bounding box of the element)
	private int angle = 1;

	/**
	 * Constructor for this class, creates a handle given the parent, direction and canvas
	 * @param aFreedom	 Direction this handle can be moved in (one of the Freedom enum)
	 * @param parent	 The {@link VPathwayElement} this handle belongs to, and which
	 *                   will be selected when this handle is clicked
	 * @param adjustable The object that is being adjusted by this handle. This is usually,
	 *                   but not always, the same as parent. For example, a Handle on a {@link VPoint}
	 *                   has a {@link Line} as parent but the {@link VPoint} as adjustable
	 */
	public Handle(Freedom aFreedom, VPathwayElement parent, Adjustable adjustable)
	{
		super(parent.canvas);
		freedom = aFreedom;
		this.adjustable = adjustable;
		this.parent = parent;
		if(freedom == Freedom.ROTATION) {
			setStyle(Style.ROTATE);
		}
	}

	/**
	 * Set the appearance style of the handle.
	 * @param style One of the styles defined in {@link Style}
	 */
	public void setStyle(Style style) {
		this.style = style;
	}
	
	/**
	 * set the angle of the handle
	 * is used to calculate the corresponding cursor type
	 * @param angle without rotation in degrees
	 */
	public void setAngle(int angle) {
		this.angle = angle;
	}
	
	public int getAngle() {
		return angle;
	}

	

	/**
	 * The object being adjusted by this Handle. Usually, but
	 * not always, the same as the Parent
	 */
	public Adjustable getAdjustable() {
		return adjustable;
	}

	/**
	 * The parent of this Handle, this is the object that this Handle is
	 * near, and will be selected when clicking this Handle.
	 */
	public VPathwayElement getParent() {
		return parent;
	}

	/**
	 * Get the type of freedom of movement that this handle has
	 * @return one of {@link Freedom}
	 */
	public Freedom getFreedom() { return freedom; }

	/** Set the handle location in view coordinates */
	public void setVLocation(double vx, double vy)
	{
		markDirty();
		mCenterx = mFromV(vx);
		mCentery = mFromV(vy);
		markDirty();
	}

	/** Set the handle location in model coordinates */
	public void setMLocation(double mx, double my)
	{
		markDirty();
		mCenterx = mx;
		mCentery = my;
		markDirty();
	}

	/** get the center x in view coordinates */
	public double getVCenterX() {
		return vFromM(mCenterx);
	}

	/** get the center y in view coordinates */
	public double getVCenterY() {
		return vFromM(mCentery);
	}

	/**
	 * Draws itself, the look depends on style. If
	 * the style is Style.INVISIBLE, nothing is drawn at all
	 */
	public void doDraw(Graphics2D g)
	{
		if(style == Style.INVISIBLE) return; // nothing to draw

		Shape fillShape = getFillShape();

		switch(style) {
		case ROTATE:
			g.setColor(Color.GREEN);
			break;
		case SEGMENT:
			g.setColor(new Color(0, 128, 255));
			break;
		default:
			g.setColor(Color.YELLOW);
			break;
		}

		g.fill(fillShape);

		g.setColor(Color.BLACK);

		g.draw(fillShape);
	}

	/**
	   Note: don't use Handle.vMoveBy, use vMoveTo instead.
	   it's impossible to handle snap-to-grid correctly if you only have the delta information.
	 */
	public void vMoveBy(double vdx, double vdy)
	{
		assert (false);
		// You shouldn't call vMoveBy on a handle! use vMoveTo instead
	}

	/**
	   Called when a mouse event forces the handle to move.
	   Note: this doesn't cause the handle itself to move,
	   rather, it passes the information to the underlying {@link Adjustable}
	   It is the responsibility of the {@link Adjustable} to
	   update the position of this Handle again.
	 */
	public void vMoveTo(double vnx, double vny)
	{
		adjustable.adjustToHandle(this, vnx, vny);
	}

	public Shape calculateVOutline() {
		return getFillShape((int)Math.ceil(DEFAULT_STROKE.getLineWidth())).getBounds();
	}

	private Shape getFillShape() {
		return getFillShape(0);
	}

	/** get the FillShape
	 * @param sw the stroke width
	 */
	private Shape getFillShape(int sw) {
		Shape s = null;
		switch(style) {
		case ROTATE:
			s = new Ellipse2D.Double(getVCenterX() - WIDTH/2, getVCenterY() - HEIGHT/2,
					WIDTH + sw, HEIGHT + sw);
			break;
		case SEGMENT:
			s = new Rectangle2D.Double(getVCenterX() - WIDTH/2, getVCenterY() - HEIGHT/2,
					WIDTH + sw, HEIGHT + sw);

			s = AffineTransform.getRotateInstance(
					Math.PI / 4, getVCenterX(), getVCenterY()
			).createTransformedShape(s);
			break;
		default:
			s = new Rectangle2D.Double(getVCenterX() - WIDTH/2, getVCenterY() - HEIGHT/2,
					WIDTH + sw, HEIGHT + sw);
			break;
		}
		return s;
	}

	/** prints some extra debug info */
	public String toString() {
		return 	"Handle with parent: " + adjustable.toString() +
		" and direction " + freedom;
	}

	protected int getZOrder() {
		return VPathway.ZORDER_HANDLE;
	}

}
