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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pathvisio.model.PathwayElement;

/**
 * This class implements a selectionbox
 *
 * A selectionbox has two states: 1: while dragging (being created) and 2: when
 * surrounding a selected area in Case 1, the handles can move freely, in case
 * 2, they move in such a way that the aspect ratio is always maintained.
 *
 * It only stores the view coordinates, not the model coordinates. Therefore it
 * is important to call adjustToZoom when the zoom pct has changed, so the view
 * coordinates can be recalculated.
 */
public class SelectionBox extends VPathwayElement implements Adjustable
{
	// Corner handles
	// These handles are not actually visible, they are just dummy objects to
	// mark which corner is being dragged.
	Handle handleNE;

	Handle handleSE;

	Handle handleSW;

	Handle handleNW;

	// double vTop, vLeft, vWidth, vHeight;
	double mTop, mLeft, mWidth, mHeight;


	private Set<VPathwayElement> selection;

	boolean isSelecting;

	boolean isVisible;

	public SelectionBox(VPathway canvas)
	{
		super(canvas);

		selection = new HashSet<VPathwayElement>();

		handleNE = new Handle(Handle.Freedom.FREE, this, this);
		handleSE = new Handle(Handle.Freedom.FREE, this, this);
		handleSW = new Handle(Handle.Freedom.FREE, this, this);
		handleNW = new Handle(Handle.Freedom.FREE, this, this);

		// handles of selectionbox are invisible
		handleNE.setStyle(Handle.Style.INVISIBLE);
		handleSE.setStyle(Handle.Style.INVISIBLE);
		handleSW.setStyle(Handle.Style.INVISIBLE);
		handleNW.setStyle(Handle.Style.INVISIBLE);
	}

	public Set<VPathwayElement> getSelection()
	{
		return selection;
	}

	public Rectangle2D calculateVOutline()
	{
		return new Rectangle2D.Double(vFromM(mLeft), vFromM(mTop),
				vFromM(mWidth), vFromM(mHeight));
	}

	/**
	 * Add an object to the selection
	 *
	 * @param o
	 */
	public void addToSelection(VPathwayElement o)
	{
		if (o == this || selection.contains(o))
			return; // Is selectionbox or already in selection
		else
		{
			o.select();
			doAdd(o);
		}
		if (!isSelecting)
			fitToSelection();

		fireSelectionEvent(new SelectionEvent(this,
				SelectionEvent.OBJECT_ADDED, o));
	}

	private void doAdd(VPathwayElement o)
	{
		if (!selection.contains(o))
			selection.add(o);
	}

	/**
	 * Remove an object from the selection
	 *
	 * @param o
	 */
	public void removeFromSelection(VPathwayElement o)
	{
		if (o == this)
			return;
		selection.remove(o);
		o.deselect();
		if (!isSelecting)
			fitToSelection();

		fireSelectionEvent(new SelectionEvent(this,
				SelectionEvent.OBJECT_REMOVED, o));
	}

	/**
	 * Get the child object at the given coordinates (relative to canvas)
	 *
	 * @param p
	 * @return the child object or null if none is present at the given location
	 */
	public VPathwayElement getChild(Point2D p)
	{
		// First check selection
		for (VPathwayElement o : selection)
		{
			if (o.vContains(p))
				return o;
		}
		// Nothing in selection, check all other objects
		for (VPathwayElement o : canvas.getDrawingObjects())
		{
			if (o.vContains(p) && o != this)
				return o;
		}
		return null; // Nothing found
	}

	/**
	 * Removes or adds the object (if exists) at the given coordinates from the
	 * selection, depending on its selection-state
	 *
	 * @param p
	 */
	public void objectClicked(Point2D p)
	{
		VPathwayElement clicked = getChild(p);
		if (clicked == null)
			return; // Nothing clicked
		if (clicked.isSelected()) // Object is selected, remove
		{
			removeFromSelection(clicked);
		} else
		// Object is not selected, add
		{
			addToSelection(clicked);
		}
	}

	/**
	 * Returns true if the selectionbox has multiple objects in its selection,
	 * false otherwise
	 */
	public boolean hasMultipleSelection()
	{
		return selection.size() > 1 ? true : false;
	}

	/**
	 * Resets the selectionbox (unselect selected objects, clear selection,
	 * reset rectangle to upperleft corner
	 */
	public void reset()
	{
		reset(0, 0, true);
	}

	/**
	 * Resets the selectionbox (unselect selected objects, clear selection,
	 * reset rectangle to upperleft corner
	 *
	 * @param clearSelection
	 *            if true the selection is cleared
	 */
	public void reset(boolean clearSelection)
	{
		reset(0, 0, clearSelection);
	}

	public void reset(double vStartX, double vStartY)
	{
		reset(vStartX, vStartY, true);
	}

	private void reset(double vStartX, double vStartY, boolean clearSelection)
	{
		if (clearSelection)
		{
			for (VPathwayElement o : selection)
				o.deselect();
			boolean hadObjects = selection.size() > 0;
			selection.clear();
			if (hadObjects)
			{
				fireSelectionEvent(new SelectionEvent(this,
						SelectionEvent.SELECTION_CLEARED));
			}
		}

		mLeft = mFromV(vStartX);
		mTop = mFromV(vStartY);
		mWidth = 0;
		mHeight = 0;
	}

	/**
	 * Returns true if this selectionbox is in selecting state (selects
	 * containing objects when resized)
	 */
	public boolean isSelecting()
	{
		return isSelecting;
	}

	/**
	 * Start selecting
	 */
	public void startSelecting()
	{
		isSelecting = true;
		show();
	}

	/**
	 * Stop selecting
	 */
	public void stopSelecting()
	{
		isSelecting = false;
		fitToSelection();
//		deselect();
		hide();
	}

	public void select()
	{
		// Do nothing
	}

	public void deselect()
	{
		// Do nothing
	}

	/**
	 * Fit the size of this object to the selected objects
	 */
	public void fitToSelection()
	{
		if (selection.size() == 0)
		{ // No objects in selection
			hide();
			reset();
			return;
		}
		if (!hasMultipleSelection())
		{ // Only one object in selection, hide selectionbox
			VPathwayElement passTo = selection.iterator().next();
			passTo.select();
			return;
		}

		Rectangle2D vr = null;
		for (VPathwayElement o : selection)
		{
			if (vr == null)
				vr = o.getVBounds();
			else
				vr.add(o.getVBounds());
		}

		mWidth = mFromV(vr.getWidth());
		mHeight = mFromV(vr.getHeight());
		mLeft = mFromV(vr.getX());
		mTop = mFromV(vr.getY());
		markDirty();
	}

	/**
	 * Sets the handles at the correct location;
	 */
	protected void setHandleLocation()
	{
		handleNE.setMLocation(mLeft + mWidth, mTop);
		handleSE.setMLocation(mLeft + mWidth, mTop + mHeight);
		handleSW.setMLocation(mLeft, mTop + mHeight);
		handleNW.setMLocation(mLeft, mTop);
	}

	/**
	 * Show the selectionbox
	 */
	public void show()
	{
		isVisible = true;
		markDirty();
	}

	public void hide()
	{
		markDirty();
		isVisible = false;
	}

	/**
	 * Gets the corner handle (South east) for start dragging
	 */
	public Handle getCornerHandle()
	{
		return handleSE;
	}

	public void adjustToHandle(Handle h, double vnewx, double vnewy)
	{
		double mnewx = mFromV(vnewx);
		double mnewy = mFromV(vnewy);
		double mdx = 0;
		double mdy = 0;
		double mdw = 0;
		double mdh = 0;

		if (h == handleNE || h == handleNW)
		{
			mdy = mnewy - mTop;
			mdh = -mdy;
		}
		if (h == handleSE || h == handleSW)
		{
			mdy = 0;
			mdh = mnewy - (mTop + mHeight);
		}
		if (h == handleSE || h == handleNE)
		{
			mdx = 0;
			mdw = mnewx - (mLeft + mWidth);
		}
		if (h == handleSW || h == handleNW)
		{
			mdx = mnewx - mLeft;
			mdw = -mdx;
		}

		mWidth += mdw;
		mHeight += mdh;
		mLeft += mdx;
		mTop += mdy;

		Handle opposite = h;
		if (mWidth < 0)
		{
			opposite = getHorizontalOpposite(opposite);
			negativeWidth();
		}
		if (mHeight < 0)
		{
			opposite = getVerticalOpposite(opposite);
			negativeHeight();
		}
		if (opposite != h)
			canvas.setPressedObject(opposite);

		markDirty();
		setHandleLocation();

		//Keep track of objects that were selected via a group
		//Don't unselect them if they're out of the selection bounds
		Set<Graphics> groupObjects = new HashSet<Graphics>();

		if (isSelecting)
		{ // Selecting, so add containing objects to selection
			Rectangle2D bounds = getVBounds();
			for (VPathwayElement o : canvas.getDrawingObjects())
			{
				if ((o == this) || (o instanceof Handle)) {
					continue;
				}

				if(o.vIntersects(bounds) ) { //&& !(o instanceof Group)
					//exclude objects in a group to avoid double selection
					if(o instanceof Graphics){
						PathwayElement pe = ((Graphics) o).getPathwayElement();
						String ref = pe.getGroupRef();
						if (ref != null) {
							continue;
						}
						if(o instanceof Group) {
							groupObjects.addAll(((Group) o).getGroupGraphics());
						}
					}
					addToSelection(o);

				} else if (o.isSelected() && !groupObjects.contains(o)) {
					removeFromSelection(o);
				}
			}
		}
	}

	private Handle getHorizontalOpposite(Handle h)
	{
		Handle opposite = null;
		if (h == handleNE)
			opposite = handleNW;
		else if (h == handleSE)
			opposite = handleSW;
		else if (h == handleSW)
			opposite = handleSE;
		else if (h == handleNW)
			opposite = handleNE;
		assert (opposite != null);
		return opposite;
	}

	private Handle getVerticalOpposite(Handle h)
	{
		Handle opposite = null;
		if (h == handleNE)
			opposite = handleSE;
		else if (h == handleSE)
			opposite = handleNE;
		else if (h == handleSW)
			opposite = handleNW;
		else if (h == handleNW)
			opposite = handleSW;
		assert (opposite != null);
		return opposite;
	}

	/**
	 * This method implements actions performed when the width of the object
	 * becomes negative after adjusting to a handle
	 *
	 * @param h
	 *            The handle this object adjusted to
	 */
	public void negativeWidth()
	{
		double mw = -mWidth;
		double msx = mLeft - mw;
		mWidth = mw;
		mLeft = msx;
	}

	/**
	 * This method implements actions performed when the height of the object
	 * becomes negative after adjusting to a handle
	 *
	 * @param h
	 *            The handle this object adjusted to
	 */
	public void negativeHeight()
	{
		double ht = -mHeight;
		double sy = mTop - ht;
		mHeight = ht;
		mTop = sy;
	}

	public void vMoveBy(double vdx, double vdy)
	{
		mLeft += mFromV(vdx);
		mTop += mFromV(vdy);
		setHandleLocation();
		markDirty();

		for (VPathwayElement o : selection) {
			if(o instanceof Graphics) {
				o.vMoveBy(vdx, vdy);
				// notify parents of any moving children!
				((Graphics) o).getPathwayElement().notifyParentGroup();
			}
		}
//		// Move selected object and their references
//		Set<GraphRefContainer> not = new HashSet<GraphRefContainer>(); // Will
//		// be moved by linking object
//		Set<VPoint> points = new HashSet<VPoint>(); // Will not be moved by
//		// linking object
//
//		for (VPathwayElement o : selection)
//		{
//			if (o instanceof Graphics)
//			{
//				PathwayElement g = ((Graphics) o).getPathwayElement();
//				if (!(o instanceof Line))
//				{
//					o.vMoveBy(vdx, vdy);
//					not.addAll(g.getReferences());
//				}
//				if (g.getObjectType() == ObjectType.LINE)
//				{
//					points.addAll(((Line) o).getPoints());
//				}
//			} else if (o instanceof VAnchor) {
//				MAnchor m = ((VAnchor)o).getMAnchor();
//				not.addAll(m.getReferences());
//			}
//
//		}

//		for (GraphRefContainer ref : not)
//		{
//			if (ref instanceof MPoint)
//			{
//				points.remove(canvas.getPoint((MPoint) ref));
//			}
//		}
//
//		for (VPoint p : points)
//		{
//			p.vMoveBy(vdx, vdy);
//		}
	}

	public void doDraw(Graphics2D g)
	{
		if (isVisible)
		{
			int sw = 1;
			g.setStroke(new BasicStroke(sw, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_MITER, 1, new float[] { 1, 2 }, 0));
			Rectangle2D rect = getVBounds();
			g.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect
					.getWidth()
					- sw, (int) rect.getHeight() - sw);
		}
	}

	private List<SelectionListener> listeners = new ArrayList<SelectionListener>();

	public void addListener(SelectionListener l)
	{
		if (!listeners.contains(l))
		{
			listeners.add(l);
		}
	}

	public void removeListener(SelectionListener l)
	{
		listeners.remove(l);
	}

	public List<SelectionListener> getListeners() {
		return listeners;
	}

	/**
	 * Fire a {@link SelectionEvent} to notify all {@link SelectionListener}s
	 * registered to this class
	 *
	 * @param e
	 */
	public void fireSelectionEvent(SelectionEvent e)
	{
		for (SelectionListener l : listeners)
		{
			l.selectionEvent(e);
		}
	}

	/** implement this if you want to be notified
	 * when the set of selected objects changes */
	public interface SelectionListener
	{
		public void selectionEvent(SelectionEvent e);
	}

	/**
	 * signals change to the set of currently selected objects.
	 */
	public static class SelectionEvent extends EventObject
	{

		public static final int OBJECT_ADDED = 0;

		public static final int OBJECT_REMOVED = 1;

		public static final int SELECTION_CLEARED = 2;

		public SelectionBox source;

		public VPathwayElement affectedObject;

		public int type;

		public Set<VPathwayElement> selection;

		public SelectionEvent(SelectionBox source, int type,
				VPathwayElement affectedObject)
		{
			super(source);
			this.source = source;
			this.type = type;
			this.selection = source.selection;
			this.affectedObject = affectedObject;
		}

		public SelectionEvent(SelectionBox source, int type)
		{
			this(source, type, null);
		}
	}

	protected int getZOrder() {
		return VPathway.ZORDER_SELECTIONBOX;
	}
}