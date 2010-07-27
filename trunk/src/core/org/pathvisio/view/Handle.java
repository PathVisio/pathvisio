// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.view.LinAlg.Point;

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
	enum Freedom {
		/** a FREE handle can move to any location on the canvas, it move diagonally when SHIFT is pressed */
		FREE,
		/** a FREER handle can move to any location on the canvas, it move diagonally (perpendicular to XY) when SHIFT is pressed */
		FREER,
		/** an X handle can only move horizontally */
		X,
		/** an Y handle can only move vertically */
		Y,
		/** a ROTATION handle can only move in a circle with a fixed radius */
		ROTATION,
		/** an XY handle can only move diagonally */
		XY,
		/** an NEGXY handle can only move diagonally, perpendicular to XY */
		NEGXY,
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

	// the appearance of the mouse cursor when
	// the mouse hovers over / drags this handle
	private int cursor = Cursor.DEFAULT_CURSOR;

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
	 * Set a hint for a cursor to use by the front-end while
	 * dragging or hovering over this handle.
	 * @param cursor One of the Swing cursor types
	 */
	public void setCursorHint(int cursor) {
		this.cursor = cursor;
	}

	public int getCursorHint() {
		return cursor;
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
		markDirty();
		
		if((freedom != Freedom.FREE || canvas.isSnapToAngle()) 
				&& (freedom != Freedom.FREER || canvas.isSnapToAngle()) 
				&& freedom != Freedom.ROTATION ) {
			Point v = new Point(0,0);
			Rectangle2D b = adjustable.getVBounds();
			Point base = new Point (b.getCenterX(), b.getCenterY());
			if (freedom == Freedom.X)
			{
				v = new Point (1, 0);
			}
			else if	(freedom == Freedom.Y)
			{
				v = new Point (0, 1);
			}
			else if (freedom == Freedom.XY)
			{
				v = new Point (b.getWidth(), b.getHeight());
			}
			else if (freedom == Freedom.NEGXY)
			{
				v = new Point (b.getHeight(), -b.getWidth());
			}
			if (freedom == Freedom.FREE)
			{
				v = new Point (adjustable.getVWidth(), adjustable.getVHeight());
			}
			else if (freedom == Freedom.FREER)
			{
				v = new Point (adjustable.getVWidth(), -adjustable.getVHeight());
			}
			Point yr = LinAlg.rotate(v, -rotation);
			Point prj = LinAlg.project(base, new Point(vnx, vny), yr);
			vnx = prj.x; vny = prj.y;
		}

		/*
		if ((freedom == Freedom.FREE || freedom == Freedom.FREER) && canvas.isSnapToAngle())
		{
			// powerpoint like handler
			double w = adjustable.getVWidth();
			double h = adjustable.getVHeight();
			Rectangle2D b = adjustable.getVBounds();
			Point base = new Point (b.getCenterX()-w/2, b.getCenterY()-h/2);
			double ratio = w/h; 
			System.out.println("["+base.x+" "+ base.y+"] ratio="+ratio);
			if((vnx - base.x) / (vny - base.y) < ratio) {
				vnx = (vny - base.y) * ratio + base.x;
			} else {
				vny = (vnx - base.x) / ratio + base.y; 
			}	
		}		
		*/
		adjustable.adjustToHandle(this, vnx, vny);
		markDirty();
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
