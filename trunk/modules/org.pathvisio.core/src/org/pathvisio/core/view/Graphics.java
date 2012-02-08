// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.pathvisio.core.biopax.BiopaxEvent;
import org.pathvisio.core.biopax.BiopaxListener;
import org.pathvisio.core.biopax.reflect.PublicationXref;
import org.pathvisio.core.debug.DebugList;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElementEvent;
import org.pathvisio.core.model.PathwayElementListener;

/**
 * This class is a parent class for all graphics
 * that can be added to a VPathway.
 */
public abstract class Graphics extends VPathwayElement implements PathwayElementListener, BiopaxListener
{
	protected PathwayElement gdata = null;
	
	// children is everything that moves when this element is dragged.
	// includes Citation and State
	private List<VPathwayElement> children = new DebugList<VPathwayElement>();
	
	private Citation citation;

	public Graphics(VPathway canvas, PathwayElement o) {
		super(canvas);
		o.addListener(this);
		gdata = o;
		gdata.getBiopaxReferenceManager().addBiopaxListener(this);
		checkCitation();
	}

	protected Citation createCitation()
	{
		return new Citation(canvas, this, new Point2D.Double(1, -1));
	}

	public final void checkCitation()
	{
		List<PublicationXref> xrefs = gdata.getBiopaxReferenceManager().getPublicationXRefs();
		if (xrefs.size() > 0 && citation == null)
		{
			citation = createCitation();
			children.add(citation);
		}
		else if (xrefs.size() == 0 && citation != null)
		{
			citation.destroy();
			children.remove(citation);
			citation = null;
		}

		if (citation != null)
		{
			// already exists, no need to create / destroy
			// just redraw...
			citation.markDirty();
		}
	}

	public void markDirty() {
		super.markDirty();
		for (VPathwayElement child : children)
			child.markDirty();
	}

	protected Citation getCitation() {
		return citation;
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * @return
	 */
	public PathwayElement getPathwayElement() {
		return gdata;
	}

	boolean listen = true;
	public void gmmlObjectModified(PathwayElementEvent e) {
		if(listen)
		{
			markDirty(); // mark everything dirty
			checkCitation();
		}
	}

	public Area createVisualizationRegion() {
		return new Area(getVBounds());
	}

	/**
	 * Get the x-coordinate of the center point of this object
	 * adjusted to the current zoom factor

	 * @return the center x-coordinate
	 */
	public double getVCenterX() { return vFromM(gdata.getMCenterX()); }

	/**
	 * Get the y-coordinate of the center point of this object
	 * adjusted to the current zoom factor
	 *
	 * @return the center y-coordinate
	 */
	public double getVCenterY() { return vFromM(gdata.getMCenterY()); }

	/**
	 * Get the x-coordinate of the left side of this object
	 * adjusted to the current zoom factor, but not taking into
	 * account rotation
	 * @note if you want the left side of the rotated object's boundary,
	 * use {@link #getVShape(true)}.getX();
	 * @return
	 */
	public double getVLeft() { return vFromM(gdata.getMLeft()); }

	/**
	 * Get the width of this object
	 * adjusted to the current zoom factor, but not taking into
	 * account rotation
	 * @note if you want the width of the rotated object's boundary,
	 * use {@link #getVShape(true)}.getWidth();
	 * @return
	 */
	public double getVWidth() { return vFromM(gdata.getMWidth());  }

	/**
	 * Get the y-coordinate of the top side of this object
	 * adjusted to the current zoom factor, but not taking into
	 * account rotation
	 * @note if you want the top side of the rotated object's boundary,
	 * use {@link #getVShape(true)}.getY();
	 * @return
	 */
	public double getVTop() { return vFromM(gdata.getMTop()); }

	/**
	 * Get the height of this object
	 * adjusted to the current zoom factor, but not taking into
	 * account rotation
	 * @note if you want the height of the rotated object's boundary,
	 * use {@link #getVShape(true)}.getY();
	 * @return
	 */
	public double getVHeight() { return vFromM(gdata.getMHeight()); }

	/**
	 * Get the direct view to model translation of this shape
	 * @param rotate Whether to take into account rotation or not
	 * @return
	 */
	abstract protected Shape getVShape(boolean rotate);

	/**
	 * Get the rectangle that represents the bounds of the shape's
	 * direct translation from model to view, without taking into
	 * account rotation.
	 * Default implementation is equivalent to <code>getVShape(false).getBounds2D();</code>
	 */
	protected Rectangle2D getVScaleRectangle() {
		return getVShape(false).getBounds2D();
	}

	/**
	 * Scales the object to the given rectangle, by taking into account
	 * the rotation (given rectangle will be rotated back before scaling)
	 * @param r
	 */
	protected abstract void setVScaleRectangle(Rectangle2D r);

	/**
	 * Default implementation returns the rotated shape.
	 * Subclasses may override (e.g. to include the stroke)
	 * @see {@link VPathwayElement#calculateVOutline()}
	 */
	protected Shape calculateVOutline() {
		return getVShape(true);
	}

	/**
	 * Returns the fontstyle to create a java.awt.Font
	 * @return the fontstyle, or Font.PLAIN if no font is available
	 */
	public int getVFontStyle() {
		int style = Font.PLAIN;
		if(gdata.getFontName() != null) {
			if(gdata.isBold()) {
				style |= Font.BOLD;
			}
			if(gdata.isItalic()) {
				style |= Font.ITALIC;
			}
		}
		return style;
	}

	protected void destroy() {
		super.destroy();
		gdata.removeListener(this);
		for (VPathwayElement child : children)
		{
			child.destroy();
		}
		children.clear();
		citation = null;
		gdata.getBiopaxReferenceManager().removeBiopaxListener(this);

		//View should not remove its model
//		Pathway parent = gdata.getParent();
//		if(parent != null) parent.remove(gdata);
	}

	/**
	 * Returns the z-order from the model
	 */
	protected int getZOrder() {
		return gdata.getZOrder();
	}

	public void biopaxEvent(BiopaxEvent e)
	{
		checkCitation();
	}

	protected Color getLineColor()
	{
		Color linecolor = gdata.getColor();
		/*
		 * the selection is not colored red when in edit mode
		 * it is possible to see a color change immediately
		 */
		if(isSelected() && !canvas.isEditMode())
		{
			linecolor = selectColor;
		}
		return linecolor;
	}
	
	protected void setLineStyle(Graphics2D g)
	{
		int ls = gdata.getLineStyle();
		float lt = (float) vFromM(gdata.getLineThickness());
		if (ls == LineStyle.SOLID)
		{
			g.setStroke(new BasicStroke(lt));
		}
		else if (ls == LineStyle.DASHED)
		{
			g.setStroke	(new BasicStroke (
				  lt,
				  BasicStroke.CAP_SQUARE,
				  BasicStroke.JOIN_MITER,
				  10, new float[] {4, 4}, 0));
		} else if (ls == LineStyle.DOUBLE)
		{
			g.setStroke( new CompositeStroke( 
					new BasicStroke( lt * 3 ), new BasicStroke( lt ) ) );
		}
	}
	

	public void addChild(VPathwayElement elt)
	{
		children.add(elt);
	}

	public void removeChild(VPathwayElement elt)
	{
		children.remove(elt);
	}

}

/**
 * Generates double line stroke, e.g., for cellular compartment shapes.
 *
 */
final class CompositeStroke implements Stroke {
	private Stroke stroke1, stroke2;

	public CompositeStroke( Stroke stroke1, Stroke stroke2 ) {
		this.stroke1 = stroke1;
		this.stroke2 = stroke2;
	}

	public Shape createStrokedShape( Shape shape ) {
		return stroke2.createStrokedShape( stroke1.createStrokedShape( shape ) );
	}
}
