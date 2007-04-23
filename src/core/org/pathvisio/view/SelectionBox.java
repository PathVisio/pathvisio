// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;

import org.pathvisio.model.GmmlDataObject;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.GmmlDataObject.MPoint;
import org.pathvisio.model.GraphLink.GraphRefContainer;

/**
 * This class implements a selectionbox 
 */ 
public class SelectionBox extends GraphicsShape
{
	private static final long serialVersionUID = 1L;
		
	private ArrayList<PathwayElement> selection;
	boolean isSelecting;
	boolean isVisible;
		
	/**
	 * Constructor for this class
	 * @param canvas - the Pathway this selectionbox will be part of
	 */
	public SelectionBox(Pathway canvas)
	{
		// TODO: selectionbox shouldn't need a dataobject...
		// note, not setting parent of GmmlDataObject here.
		super(canvas, new GmmlDataObject(ObjectType.SHAPE));
			
		selection = new ArrayList<PathwayElement>();
	}	
	
	public int getDrawingOrder() {
		return Pathway.DRAW_ORDER_SELECTIONBOX;
	}
	
	public ArrayList<PathwayElement> getSelection() {
		return selection;
	}
	
	/**
	 * Add an object to the selection
	 * @param o
	 */
	public void addToSelection(PathwayElement o) {
		if(o == this || selection.contains(o)) return; //Is selectionbox or already in selection
		if(o instanceof VPoint) {
			for(Line l : ((VPoint)o).getLines()) {
				l.select();
				doAdd(l);
			}
		} else {
			o.select();
			doAdd(o);
		}
		fireSelectionEvent(new SelectionEvent(this, SelectionEvent.OBJECT_ADDED, o));
		if(isSelecting) return; //All we have to do if user is dragging selectionbox
		if(hasMultipleSelection()) { 
			stopSelecting(); //show and fit to SelectionBox if performed after dragging
		}
		 
	}
	
	private void doAdd(PathwayElement o) {
		if(!selection.contains(o)) selection.add(o);
	}
	
	/**
	 * Remove an object from the selection
	 * @param o
	 */
	public void removeFromSelection(PathwayElement o) {
		if(o == this) return;
		selection.remove(o); 
		o.deselect();
		fireSelectionEvent(new SelectionEvent(this, SelectionEvent.OBJECT_REMOVED, o));
		if(!isSelecting) fitToSelection();
	}
	
	/**
	 * Get the child object at the given coordinates (relative to canvas)
	 * @param p
	 * @return the child object or null if none is present at the given location
	 */
	public PathwayElement getChild(Point2D p) {
		//First check selection
		for(PathwayElement o : selection) {
			if(o.vContains(p)) return o;
		}
		//Nothing in selection, check all other objects
		for(PathwayElement o : canvas.getDrawingObjects()) {
			if(o.vContains(p) && o != this)
				return o;
		}
		return null; //Nothing found
	}
	
	/**
	 * Removes or adds the object (if exists) at the given coordinates from the selection,
	 * depending on its selection-state
	 * @param p
	 */
	public void objectClicked(Point2D p) {
		PathwayElement clicked = getChild(p);
		if(clicked == null) return; //Nothing clicked
		if(clicked.isSelected()) 	//Object is selected, remove
		{
			removeFromSelection(clicked);
		} 
		else 						//Object is not selected, add
		{
			addToSelection(clicked);
		}
	}
	
	/**
	 * Returns true if the selectionbox has multiple objects in its selection, false otherwise
	 */
	public boolean hasMultipleSelection() { return selection.size() > 1 ? true : false; }
	
	/**
	 * Resets the selectionbox (unselect selected objects, clear selection, reset rectangle
	 * to upperleft corner
	 */
	public void reset() { 
		reset(0, 0, true);
	}
	
	
	/**
	 * Resets the selectionbox (unselect selected objects, clear selection, reset rectangle
	 * to upperleft corner
	 * @param clearSelection if true the selection is cleared
	 */
	public void reset(boolean clearSelection) { 
		reset(0, 0, clearSelection);
	}
	
	public void reset(double vStartX, double vStartY) {
		reset(vStartX, vStartY, true);
	}
	
	private void reset(double vStartX, double vStartY, boolean clearSelection) {
		for(PathwayElement o : selection) o.deselect();
		if(clearSelection) {
			selection.clear();
			fireSelectionEvent(
					new SelectionEvent(this, SelectionEvent.SELECTION_CLEARED));
		}
		
		gdata.setMLeft(mFromV(vStartX));
		gdata.setMTop(mFromV(vStartY));
		gdata.setMWidth(0);
		gdata.setMHeight(0);
	}

	/**
	 * Returns true if this selectionbox is in selecting state (selects containing objects when resized)
	 */
	public boolean isSelecting() { return isSelecting; }
	
	/**
	 * Start selecting
	 */
	public void startSelecting() {
		isSelecting = true;
		setHandleRestriction(false);
		show();
	}
	
	/**
	 * Stop selecting
	 */
	public void stopSelecting() {
		isSelecting = false;
		if(!hasMultipleSelection()) {
			if(selection.size() == 1) {
				PathwayElement passTo = selection.get(0);
				reset();
				passTo.select();
			} else {
				reset();
			}
		} else {
			select();
			fitToSelection();
			setHandleRestriction(true);
		}
	}
	
	/**
	 * Sets movement direction restriction for this object's handles
	 * @param restrict if true, handle movement is restricted in XY direction,
	 * else handles can move freely
	 */
	private void setHandleRestriction(boolean restrict) {
		if(restrict) {
			handleNE.setDirection(Handle.DIRECTION_MINXY);
			handleSW.setDirection(Handle.DIRECTION_MINXY);
			handleNW.setDirection(Handle.DIRECTION_XY);
			handleSE.setDirection(Handle.DIRECTION_XY);
		} else {
			for(Handle h : getHandles()) 
				h.setDirection(Handle.DIRECTION_FREE); 
		}
	}
	
	public void select() {
		super.select();
		for(PathwayElement o : selection) {
			o.select();
			for(Handle h : o.getHandles()) h.hide();
		}
	}
	
	/**
	 * Fit the size of this object to the selected objects
	 */
	public void fitToSelection() {
		if(selection.size() == 0) { //No objects in selection
			hide(); 
			return;
		}
		if(! hasMultipleSelection()) { //Only one object in selection, hide selectionbox
			PathwayElement passTo = selection.get(0);
			hide(false);
			passTo.select();
			return;
		}

		Rectangle vr = null;
		for(PathwayElement o : selection) {
			if(vr == null) vr = o.getVBounds();
			else vr.add(o.getVBounds());
			for(Handle h : o.getHandles()) h.hide();
		}

		gdata.setMWidth(mFromV(vr.width));
		gdata.setMHeight(mFromV(vr.height));
		gdata.setMLeft(mFromV(vr.x));
		gdata.setMTop(mFromV(vr.y));
		setHandleLocation();		
	}
			
	/**
	 * Show the selectionbox
	 */
	public void show() { 
		isVisible = true; 
		markDirty();
	}
	
	/**
	 * Hide the selectionbox
	 */
	public void hide() {
		hide(true);
	}
	
	public void hide(boolean reset) {
		for(Handle h : getHandles()) h.hide();
		isVisible = false;
		if(reset) reset();
	}
	
	/**
	 * Gets the corner handle (South east) for start dragging
	 */
	public Handle getCornerHandle() { return handleSE; }
	
	public void adjustToHandle(Handle h) {	
		//Store original size and location before adjusting to handle
		double vWidthOld = getVWidthDouble();
		double vHeightOld = getVHeightDouble();
		double vCenterXOld = getVCenterXDouble();
		double vCenterYOld = getVCenterYDouble();
		
		super.adjustToHandle(h);
		if(isSelecting) { //Selecting, so add containing objects to selection
			Rectangle vr = getVBounds();
			Rectangle2D.Double bounds = new Rectangle2D.Double(vr.x, vr.y, vr.width, vr.height);
			for(PathwayElement o : canvas.getDrawingObjects()) {
				if((o == this) || (o instanceof Handle)) continue;
				if(o.vIntersects(bounds)) { 
					addToSelection(o);
				} else if(o.isSelected()) removeFromSelection(o);
			}
		} else { //Resizing, so resize child objects too
			double widthRatio = getVWidthDouble() / vWidthOld;
			double heightRatio = getVHeightDouble() / vHeightOld;
			//Scale all selected objects in x and y direction, treat points seperately
			Set<VPoint> points = new HashSet<VPoint>();
			for(PathwayElement o : selection) { 
				if(o instanceof Line) {
					points.addAll(((Line)o).getPoints());
				} else { 
					Rectangle2D.Double vr = o.getVScaleRectangle();
					double newObjectWidth = vr.width * widthRatio;
					double newObjectHeight = vr.height * heightRatio;
					double objectFromCenterX = (vr.x - vCenterXOld) * widthRatio;
					double objectFromCenterY = (vr.y - vCenterYOld) * heightRatio;
					o.setVScaleRectangle(new Rectangle2D.Double(
							getVCenterXDouble() + objectFromCenterX, 
							getVCenterYDouble() + objectFromCenterY, 
							newObjectWidth, 
							newObjectHeight));
				}
			}
			for(VPoint p : points) {
				double dx = (p.getVX() - vCenterXOld) * widthRatio;
				double dy = (p.getVY() - vCenterYOld) * heightRatio;
				p.setVLocation(getVCenterXDouble() + dx, getVCenterYDouble() + dy);
			}
		}
	}
	
	public void vMoveBy(double vdx, double vdy) 
	{

		gdata.setMLeft(gdata.getMLeft() + mFromV(vdx)); 
		gdata.setMTop(gdata.getMTop() + mFromV(vdy));

		//Move selected object and their references
		Set<GraphRefContainer> not = new HashSet<GraphRefContainer>(); //Will be moved by linking object
		Set<VPoint> points = new HashSet<VPoint>(); //Will not be moved by linking object
		
		for(PathwayElement o : selection) 
		{
			if (o instanceof Graphics)
			{
				GmmlDataObject g = ((Graphics)o).getGmmlData();
				if(!(o instanceof Line)) {
					o.vMoveBy(vdx, vdy);
					not.addAll(g.getReferences());
				}
				if(g.getObjectType() == ObjectType.LINE) {
					points.addAll(((Line)o).getPoints());
				}
			}

		}
		
		for(GraphRefContainer ref : not) {
			if(ref instanceof MPoint) {
				points.remove(canvas.getPoint((MPoint)ref));
			}
		}
			
		for(VPoint p : points) {
			p.vMoveBy(vdx, vdy);
		}
	}
	
	public void draw(PaintEvent e, GC buffer)
	{
		if(isVisible) {
			buffer.setAntialias(SWT.OFF);
			buffer.setForeground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.setBackground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.setLineStyle (SWT.LINE_DOT);
			buffer.setLineWidth (1);
			buffer.drawRectangle (getVLeft(), getVTop(), getVWidth(), getVHeight());
			buffer.setAntialias(SWT.ON);
		}
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	public void adjustToZoom(double factor) { fitToSelection(); }
	
	static List<SelectionListener> listeners;

	/**
	 * Add a {@link SelectionListener}, that will be notified if a selection event occurs
	 * @param l The {@link SelectionListener} to add
	 */
	public static void addListener(SelectionListener l) {
		if(listeners == null)
			listeners = new ArrayList<SelectionListener>();
		listeners.add(l);
	}

	/**
	 * Fire a {@link SelectionEvent} to notify all {@link SelectionListener}s registered
	 * to this class
	 * @param e
	 */
	public static void fireSelectionEvent(SelectionEvent e) {
		for(SelectionListener l : listeners) {
			l.drawingEvent(e);
		}
	}

	public interface SelectionListener {
		public void drawingEvent(SelectionEvent e);
	}

	public static class SelectionEvent extends EventObject {
		private static final long serialVersionUID = 1L;
		public static final int OBJECT_ADDED = 0;
		public static final int OBJECT_REMOVED = 1;
		public static final int SELECTION_CLEARED = 2;

		public SelectionBox source;
		public PathwayElement affectedObject;
		public int type;
		public List<PathwayElement> selection;

		public SelectionEvent(SelectionBox source, int type, PathwayElement affectedObject) {
			super(source);
			this.source = source;
			this.type = type;
			this.selection = source.selection;
			this.affectedObject = affectedObject;
		}
		
		public SelectionEvent(SelectionBox source, int type) {
			this(source, type, null);
		}
	}	
	
}