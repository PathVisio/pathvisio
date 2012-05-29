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
package org.pathvisio.core.model;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bridgedb.Xref;
import org.jdom.Document;
import org.jdom.Element;
import org.pathvisio.core.biopax.BiopaxElementManager;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.GraphLink.GraphIdContainer;
import org.pathvisio.core.model.GraphLink.GraphRefContainer;
import org.pathvisio.core.model.PathwayElement.MAnchor;
import org.pathvisio.core.model.PathwayElement.MPoint;
import org.pathvisio.core.util.Utils;

/**
* This class is the model for pathway data. It is responsible for
* storing all information necessary for maintaining, loading and saving
* pathway data.
*
* Pathway contains multiple PathwayElements. Pathway is guaranteed
* to always have exactly one object of the type MAPPINFO and exactly
* one object of the type INFOBOX.
*/
public class Pathway
{
	private boolean changed = true;
	/**
	   The "changed" flag tracks if the Pathway has been changed since
	   the file was opened or last saved. New pathways start changed.
	 */
	public boolean hasChanged() { return changed; }

	/**
	   clearChangedFlag should be called after when the current
	   pathway is known to be the same as the one on disk. This
	   happens when you just opened it, or when you just saved it.
	*/
	public void clearChangedFlag()
	{
		if (changed)
		{
			changed = false;
			fireStatusFlagEvent (new StatusFlagEvent (changed));
			//System.out.println ("Changed flag is cleared");
		}
	}

	/**
	   To be called after each edit operation
	*/
	private void markChanged()
	{
		if (!changed)
		{
			changed = true;
			fireStatusFlagEvent (new StatusFlagEvent (changed));
			//System.out.println ("Changed flag is set");
		}
	}

	/**
	 * List of contained dataObjects
	 */
	private List<PathwayElement> dataObjects = new ArrayList<PathwayElement>();

	/**
	 * Getter for dataobjects contained. There is no setter, you
	 * have to add dataobjects individually
	 * @return List of dataObjects contained in this pathway
	 */
	public List<PathwayElement> getDataObjects()
	{
		return dataObjects;
	}

	/**
	 * Get a pathway element by it's GraphId
	 * @param graphId The graphId of the element
	 * @return The pathway element with the given id, or null when no element was found
	 */
	public PathwayElement getElementById(String graphId) {
		//TODO: dataobject should be stored in a hashmap, with the graphId as key!
		if(graphId != null) {
			for(PathwayElement e : dataObjects) {
				if(graphId.equals(e.getGraphId())) {
					return e;
				}
			}
		}
		return null;
	}

	/**
	 * Takes the Xref of all DataNodes in this pathway
	 * and returns them as a List.
	 *
	 * returns an empty arraylist if there are no datanodes in
	 * this pathway.
	 */
	public List<Xref> getDataNodeXrefs()
	{
		List<Xref> result = new ArrayList<Xref>();
		for (PathwayElement e : dataObjects)
		{
			if (e.getObjectType() == ObjectType.DATANODE)
			{
				result.add(e.getXref());
			}
		}
		return result;
	}

	private PathwayElement mappInfo = null;
	private PathwayElement infoBox = null;
	private PathwayElement biopax = null;
	private PathwayElement legend = null;

	/**
	 * get the one and only MappInfo object.
	 *
	 * @return a PathwayElement with ObjectType set to mappinfo.
	 */
	public PathwayElement getMappInfo()
	{
		return mappInfo;
	}

	/**
	 * get the one and only InfoBox object.
	 *
	 * @return a PathwayElement with ObjectType set to mappinfo.
	 */
	public PathwayElement getInfoBox()
	{
		return infoBox;
	}

	/**
	   note: may return null.
	 */
	public PathwayElement getBiopax()
	{
		return biopax;
	}

	public void createBiopax()
	{
		PathwayElement biopax = PathwayElement.createPathwayElement(ObjectType.BIOPAX);
		this.add(biopax);
	}

	private BiopaxElementManager bpElmMgr;

	public BiopaxElementManager getBiopaxElementManager() {
		if(bpElmMgr == null) {
			bpElmMgr = new BiopaxElementManager(this);
		}
		bpElmMgr.refresh();
		return bpElmMgr;
	}

	/**
	 * Add a PathwayElement to this Pathway.
	 * takes care of setting parent and removing from possible previous
	 * parent.
	 *
	 * fires PathwayEvent.ADDED event <i>after</i> addition of the object
	 *
	 * @param o The object to add
	 */
	public void add (PathwayElement o)
	{
		assert (o != null);
		// There can be only one mappInfo object, so if we're trying to add it, remove the old one.
		if (o.getObjectType() == ObjectType.MAPPINFO && o != mappInfo)
		{
			if(mappInfo != null) {
				replaceUnique (mappInfo, o);
				mappInfo = o;
				return;
			}
			mappInfo = o;
		}
		// There can be only one InfoBox object, so if we're trying to add it, remove the old one.
		if (o.getObjectType() == ObjectType.INFOBOX && o != infoBox)
		{
			if(infoBox != null) {
				replaceUnique (infoBox, o);
				infoBox = o;
				return;
			}
			infoBox = o;
		}
		// There can be zero or one Biopax object, so if we're trying to add it, remove the old one.
		if(o.getObjectType() == ObjectType.BIOPAX && o != biopax)
		{
			if(biopax != null) {
				replaceUnique (biopax, o);
				biopax = o;
				return;
			}
			biopax = o;
		}
		// There can be only one Legend object, so if we're trying to add it, remove the old one.
		if (o.getObjectType() == ObjectType.LEGEND && o != legend)
		{
			if(legend != null) {
				replaceUnique (legend, o);
				legend = o;
				return;
			}
			legend = o;
		}
		if (o.getParent() == this) return; // trying to re-add the same object
		forceAddObject(o);
	}

	private void forceAddObject(PathwayElement o) {
		if (o.getParent() != null) { o.getParent().remove(o); }
		dataObjects.add(o);
		o.setParent(this);
		for (MPoint p : o.getMPoints())
		{
			if (p.getGraphRef() != null)
			{
				addGraphRef(p.getGraphRef(), p);
			}
		}
		if(o.getGroupRef() != null)
		{
			addGroupRef(o.getGroupRef(), o);
		}
		for (MAnchor a : o.getMAnchors())
		{
			if(a.getGraphId() != null)
			{
				addGraphId(a.getGraphId(), a);
			}
		}
		if (o.getGraphId() != null)
		{
			addGraphId(o.getGraphId(), o);
		}
		if (o.getGroupId() != null)
		{
			addGroupId(o.getGroupId(), o);
		}
		if (o.getGraphRef() != null)
		{
			addGraphRef(o.getGraphRef(), (GraphRefContainer)o);
		}
		fireObjectModifiedEvent(new PathwayEvent(o, PathwayEvent.ADDED));
		checkMBoardSize(o);
	}

	/**
	 * get the highest z-order of all objects
	 */
	public int getMaxZOrder()
	{
		if (dataObjects.size() == 0) return 0;

		int zmax = dataObjects.get(0).getZOrder();
		for(PathwayElement e : dataObjects)
		{
			if(e.getZOrder() > zmax) zmax = e.getZOrder();
		}
		return zmax;
	}

	/**
	 * get the lowest z-order of all objects
	 */
	public int getMinZOrder()
	{
		if (dataObjects.size() == 0) return 0;

		int zmin = dataObjects.get(0).getZOrder();
		for(PathwayElement e : dataObjects)
		{
			if(e.getZOrder() < zmin) zmin = e.getZOrder();
		}
		return zmin;
	}

	/** only used by children of this Pathway to
	 * notify the parent of modifications */
	void childModified (PathwayElementEvent e)
	{
		markChanged();
		// a coordinate change could trigger dependent objects such as states, 
		// groups and connectors to be updated as well.
		if (e.isCoordinateChange())
		{

			PathwayElement elt = e.getModifiedPathwayElement();
			List<GraphRefContainer> references = getReferringObjects(elt.getGraphId());
			for(GraphRefContainer refc : references) 
			{
				refc.refeeChanged();
			}
			
			String ref = elt.getGroupRef();
			if (ref != null && getGroupById(ref) != null)
			{
				//identify group object and notify model change to trigger view update
				PathwayElement group = getGroupById(ref);
				group.fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(group));
			}

			checkMBoardSize(e.getModifiedPathwayElement());
		}
	}

	/**
	   called for biopax, infobox and mappInfo upon addition.
	 */
	private void replaceUnique (PathwayElement oldElt, PathwayElement newElt)
	{
		assert (oldElt.getParent() == this);
		assert (oldElt.getObjectType() == newElt.getObjectType());
		assert (newElt.getParent() == null);
		assert (oldElt != newElt);
		forceRemove(oldElt);
		forceAddObject(newElt);
	}

	/**
	 * removes object
	 * sets parent of object to null
	 * fires PathwayEvent.DELETED event <i>before</i> removal of the object
	 *
	 * @param o the object to remove
	 */
	public void remove (PathwayElement o)
	{
		assert (o.getParent() == this); // can only remove direct child objects
		if (o.getObjectType() == ObjectType.MAPPINFO)
			throw new IllegalArgumentException("Can't remove mappinfo object!");
		if (o.getObjectType() == ObjectType.INFOBOX)
			throw new IllegalArgumentException("Can't remove infobox object!");
		forceRemove(o);
	}

	/**
	 * removes object, regardless whether the object may be removed or not
	 * sets parent of object to null
	 * fires PathwayEvent.DELETED event <i>before</i> removal of the object
	 *
	 * @param o the object to remove
	 */
	private void forceRemove(PathwayElement o) {
		dataObjects.remove(o);
		List<GraphRefContainer> references = getReferringObjects(o.getGraphId());
		for(GraphRefContainer refc : references) {
			refc.unlink();
		}
		String groupRef = o.getGroupRef();
		if(groupRef != null)
		{
			removeGroupRef(groupRef, o);			
		}
		for (MAnchor a : o.getMAnchors()) {
			if (a.getGraphId() != null)
			{
				removeGraphId(a.getGraphId());
			}
		}
		if (o.getGraphId() != null)
		{
			removeGraphId(o.getGraphId());
		}
		if (o.getGroupId() != null)
		{
			removeGroupId(o.getGroupId());
		}
		if (o.getGraphRef() != null)
		{
			removeGraphRef(o.getGraphRef(), (GraphRefContainer)o);
		}
		fireObjectModifiedEvent(new PathwayEvent(o, PathwayEvent.DELETED));
		o.setParent(null);
	}

	public void mergeBiopax(PathwayElement bpnew) {
		if(bpnew == null) return;

		Document dNew = bpnew.getBiopax();
		Document dOld = biopax == null ? null : biopax.getBiopax();

		if(dNew == null) {
			return; //Nothing to merge
		}

		if(dOld == null) {
			createBiopax();
			biopax.setBiopax(dNew);
			return;
		}

		//Create a map of existing biopax elements with an id
		Map<String, Element> bpelements = new HashMap<String, Element>();
		for(Object o : dOld.getRootElement().getContent()) {
			if(o instanceof Element) {
				Element e = (Element)o;
				String id = e.getAttributeValue("id", GpmlFormat.RDF);
				if(id != null) bpelements.put(id, e);
			}
		}

		//Replace existing elements with the new one, or add if none exist yet
		for(Object o : dNew.getRootElement().getContent()) {
			if(o instanceof Element) {
				Element eNew = (Element)o;
				String id = eNew.getAttributeValue("id", GpmlFormat.RDF);
				Element eOld = bpelements.get(id);
				if(eOld != null) { //If an elements with the same id exist, remove it
					dOld.getRootElement().removeContent(eOld);
				}
				dOld.getRootElement().addContent((Element)eNew.clone());
			}
		}
	}

	/**
	 * Stores references of graph ids to other GraphRefContainers
	 */
	private Map<String, List<GraphRefContainer>> graphRefs = new HashMap<String, List<GraphRefContainer>>();
	private Map<String, GraphIdContainer> graphIds = new HashMap<String, GraphIdContainer>();

	public Set<String> getGraphIds() {
		return graphIds.keySet();
	}

	public GraphIdContainer getGraphIdContainer(String id) {
		return graphIds.get(id);
	}

	/**
	 * Returns all GraphRefContainers that refer to an object with a
	 * particular graphId.
	 */
	public List<GraphRefContainer> getReferringObjects (String id)
	{
		List<GraphRefContainer> refs = graphRefs.get(id);
		if(refs != null) {
			// create defensive copy to prevent problems with ConcurrentModification.
			refs = new ArrayList<GraphRefContainer>(refs);
		} else {
			refs = new ArrayList<GraphRefContainer>();
		}
		return refs;
	}

	/**
	 * Register a link from a graph id to a graph ref
	 * @param id The graph id
	 * @param target The target GraphRefContainer
	 */
	public void addGraphRef (String id, GraphRefContainer target)
	{
		if (graphRefs.containsKey(id))
		{
			List<GraphRefContainer> l = graphRefs.get(id);
			l.add(target);
		}
		else
		{
			List<GraphRefContainer> l = new ArrayList<GraphRefContainer>();
			l.add(target);
			graphRefs.put(id, l);
		}
	}

	/**
	 * Remove a reference to another Id.
	 * @param id
	 * @param target
	 */
	void removeGraphRef (String id, GraphRefContainer target)
	{
		if (!graphRefs.containsKey(id)) throw new IllegalArgumentException();

		graphRefs.get(id).remove(target);
		if (graphRefs.get(id).size() == 0)
			graphRefs.remove(id);
	}

	/**
	 * Registers an id that can subsequently be used for
	 * referral. It is tested for uniqueness.
	 * @param id
	 */
	public void addGraphId (String id, GraphIdContainer idc)
	{
		if (idc == null || id == null)
		{
			throw new IllegalArgumentException ("unique id can't be null");
		}
		if (graphIds.containsKey(id))
		{
			throw new IllegalArgumentException ("id '" + id + "' is not unique");
		}
		graphIds.put(id, idc);
	}

	void removeGraphId (String id)
	{
		graphIds.remove(id);
	}

	private Map<String, PathwayElement> groupIds = new HashMap<String, PathwayElement>();
	private Map<String, Set<PathwayElement>> groupRefs = new HashMap<String, Set<PathwayElement>>();

	public Set<String> getGroupIds() {
		return groupIds.keySet();
	}

	void addGroupId(String id, PathwayElement group) {
		if (id == null)
		{
			throw new IllegalArgumentException ("unique id can't be null");
		}
		if (groupIds.containsKey(id))
		{
			throw new IllegalArgumentException ("id '" + id + "' is not unique");
		}
		groupIds.put(id, group);
	}

	void removeGroupId(String id) 
	{
		groupIds.remove(id);
		Set<PathwayElement> elts = groupRefs.get(id);
		if (elts != null) 
			for (PathwayElement elt : elts)
			{
				elt.groupRef = null;
				elt.fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(elt, StaticProperty.GROUPREF));
			}
		groupRefs.remove(id);
	}

	public PathwayElement getGroupById(String id) {
		return groupIds.get(id);
	}

	void addGroupRef (String ref, PathwayElement child)
	{
		Utils.multimapPut(groupRefs, ref, child);
	}

	void removeGroupRef (String id, PathwayElement child)
	{
		if (!groupRefs.containsKey(id)) throw new IllegalArgumentException();

		groupRefs.get(id).remove(child);
		
		//Find out if this element is the last one in a group
		//If so, remove the group as well			
		if (groupRefs.get(id).size() == 0)
		{
			groupRefs.remove(id);
			PathwayElement group = getGroupById(id);
			if (group != null) forceRemove(group);
		} 
		else 
		{
			// redraw group outline
			if (getGroupById(id) != null ){
				MGroup group = (MGroup)getGroupById(id);
				group.fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(group));
			}
		}
	}

	/**
	 * Get the pathway elements that are part of the given group
	 * @param id The id of the group
	 * @return The set of pathway elements part of the group
	 */
	public Set<PathwayElement> getGroupElements(String id) {
		Set<PathwayElement> result = groupRefs.get(id);
		//Return an empty set if the group is empty
		return result == null ? new HashSet<PathwayElement>() : result;
	}

	public String getUniqueGraphId() {
		return getUniqueId(graphIds.keySet());
	}

	public String getUniqueGroupId() {
		return getUniqueId(groupIds.keySet());
	}

	/**
	 * Generate random ids, based on strings of hex digits (0..9 or a..f)
	 * Ids are unique across both graphIds and groupIds per pathway
	 * @param ids The collection of already existing ids
	 * @return an Id unique for this pathway
	 */
	public String getUniqueId (Set<String> ids)
	{
		String result;
		Random rn = new Random();
		int mod = 0x60000; // 3 hex letters
		int min = 0xa0000; // has to start with a letter
		// in case this map is getting big, do more hex letters
		if ((ids.size()) > 0x10000)
		{
			mod = 0x60000000;
			min = 0xa0000000;
		}

		do
		{
			result = Integer.toHexString(Math.abs(rn.nextInt()) % mod + min);
		}
		while (ids.contains(result));

		return result;
	}

	double mBoardWidth = 0;
	double mBoardHeight = 0;

	private static final int BORDER_SIZE = 30;

	/**
	 * Checks whether the board size is still large enough for the given {@link PathwayElement}
	 * and increases the size if not
	 * @param elm The element to check the board size for
	 */
	private void checkMBoardSize(PathwayElement e)
	{
		double mw = mBoardWidth;
		double mh = mBoardHeight;

		switch(e.getObjectType()) {
		case LINE:
			mw = Math.max(mw, BORDER_SIZE + Math.max(e.getMStartX(), e.getMEndX()));
			mh = Math.max(mh, BORDER_SIZE + Math.max(e.getMStartY(), e.getMEndY()));
			break;
		default:
			mw = Math.max(mw, BORDER_SIZE + e.getMLeft() + e.getMWidth());
			mh = Math.max(mh, BORDER_SIZE + e.getMTop() + e.getMHeight());
			break;
		}

		if (Math.abs (mBoardWidth - mw) + Math.abs (mBoardHeight - mh) > 0.01)
		{
			mBoardWidth = mw;
			mBoardHeight = mh;
			fireObjectModifiedEvent(new PathwayEvent(mappInfo, PathwayEvent.RESIZED));
		}
	}

	public double[] getMBoardSize()
	{
		return new double[] {mBoardWidth, mBoardHeight};
	}
	
	private File sourceFile = null;

	/**
	 * Gets the xml file containing the Gpml/mapp pathway currently displayed
	 * @return current xml file
	 */
	public File getSourceFile () { return sourceFile; }
	public void setSourceFile (File file) { sourceFile = file; }

	/**
	 * Contructor for this class, creates a new gpml document
	 */
	public Pathway()
	{
		mappInfo = PathwayElement.createPathwayElement(ObjectType.MAPPINFO);
		this.add (mappInfo);
		infoBox = PathwayElement.createPathwayElement(ObjectType.INFOBOX);
		this.add (infoBox);
	}

	/*
	 * Call when making a new mapp.
	 */
	public void initMappInfo()
	{
		String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
		mappInfo.setVersion(dateString);
		mappInfo.setMapInfoName("New Pathway");
	}

	/**
	 * Writes the JDOM document to the file specified
	 * @param file	the file to which the JDOM document should be saved
	 * @param validate if true, validate the dom structure before writing to file. If there is a validation error,
	 * 		or the xsd is not in the classpath, an exception will be thrown.
	 */
	public void writeToXml(File file, boolean validate) throws ConverterException
	{
		GpmlFormat.writeToXml (this, file, validate);
		setSourceFile (file);
		clearChangedFlag();

	}

	public void readFromXml(Reader in, boolean validate) throws ConverterException
	{
		GpmlFormat.readFromXml (this, in, validate);
		setSourceFile (null);
		clearChangedFlag();
	}

	public void readFromXml(InputStream in, boolean validate) throws ConverterException
	{
		GpmlFormat.readFromXml (this, in, validate);
		setSourceFile (null);
		clearChangedFlag();
	}

	public void readFromXml(File file, boolean validate) throws ConverterException
	{
		Logger.log.info("Start reading the XML file: " + file);
		GpmlFormat.readFromXml (this, file, validate);
		setSourceFile (file);
		clearChangedFlag();
	}

	public void writeToMapp (File file) throws ConverterException
	{
		new MappFormat().doExport(file, this);
	}

	public void writeToSvg (File file) throws ConverterException
	{
		//Use Batik instead of SvgFormat
		//SvgFormat.writeToSvg (this, file);
		new BatikImageExporter(ImageExporter.TYPE_SVG).doExport(file, this);
	}

	/**
	 * Implement this interface if you want to be notified when the "changed" status changes.
	 * This happens e.g. when the user makes a change to an unchanged pathway,
	 * or when a changed pathway is saved.
	 */
	public interface StatusFlagListener extends EventListener
	{
		public void statusFlagChanged (StatusFlagEvent e);
	}

	/**
	 * Event for a change in the "changed" status of this Pathway
	 */
	public static class StatusFlagEvent
	{
		private boolean newStatus;
		public StatusFlagEvent (boolean newStatus) { this.newStatus = newStatus; }
		public boolean getNewStatus() {
			return newStatus;
		}
	}

	private List<StatusFlagListener> statusFlagListeners = new ArrayList<StatusFlagListener>();

	/**
	 * Register a status flag listener
	 */
	public void addStatusFlagListener (StatusFlagListener v)
	{
		if (!statusFlagListeners.contains(v)) statusFlagListeners.add(v);
	}

	/**
	 * Remove a status flag listener
	 */
	public void removeStatusFlagListener (StatusFlagListener v)
	{
		statusFlagListeners.remove(v);
	}

	//TODO: make private
	public void fireStatusFlagEvent(StatusFlagEvent e)
	{
		for (StatusFlagListener g : statusFlagListeners)
		{
			g.statusFlagChanged (e);
		}
	}

	private List<PathwayListener> listeners = new ArrayList<PathwayListener>();

	public void addListener(PathwayListener v)
	{
		if(!listeners.contains(v)) listeners.add(v);
	}

	public void removeListener(PathwayListener v) { listeners.remove(v); }

    /**
	   Firing the ObjectModifiedEvent has the side effect of
	   marking the Pathway as changed.
	 */
	public void fireObjectModifiedEvent(PathwayEvent e)
	{
		markChanged();
		for (PathwayListener g : listeners)
		{
			g.pathwayModified(e);
		}
	}

	public Pathway clone()
	{
		Pathway result = new Pathway();
		for (PathwayElement pe : dataObjects)
		{
			result.add (pe.copy());
		}
		result.changed = changed;
		if(sourceFile != null) {
			result.sourceFile = new File(sourceFile.getAbsolutePath());
		}
		// do not copy status flag listeners
//		for(StatusFlagListener l : statusFlagListeners) {
//			result.addStatusFlagListener(l);
//		}
		return result;
	}

	public String summary()
	{
		String result = "    " + toString() + "\n    with Objects:";
		for (PathwayElement pe : dataObjects)
		{
			String code = pe.toString();
			code = code.substring (code.lastIndexOf ('@'), code.length() - 1);
			result += "\n      " + code + " " +
				pe.getObjectType().getTag() + " " + pe.getParent();
		}
		return result;
	}

	/**
	 * Check for any dangling references, and fix them if found
	 * This is called just before writing out a pathway.
	 *
	 * This is a fallback solution for problems elsewhere in the
	 * reference handling code. Theoretically, if the rest of
	 * the code is bug free, this should always return 0.
	 *
	 * @return number of references fixed. Should be 0 under normal
	 * circumstances.
	 */
	public int fixReferences()
	{
		int result = 0;
		Set <String> graphIds = new HashSet <String>();
		for (PathwayElement pe : dataObjects)
		{
			String id = pe.getGraphId();
			if (id != null)
			{
				graphIds.add (id);
			}
			for (PathwayElement.MAnchor pp : pe.getMAnchors())
			{
				String pid = pp.getGraphId();
				if (pid != null)
				{
					graphIds.add (pid);
				}
			}
		}
		for (PathwayElement pe : dataObjects)
		{
			if (pe.getObjectType() == ObjectType.LINE)
			{
				String ref = pe.getStartGraphRef();
				if (ref != null && !graphIds.contains(ref))
				{
					pe.setStartGraphRef(null);
					result++;
				}

				ref = pe.getEndGraphRef();
				if (ref != null && !graphIds.contains(ref))
				{
					pe.setEndGraphRef(null);
					result++;
				}
			}
		}
		if (result > 0)
		{
			Logger.log.warn("Pathway.fixReferences fixed " + result + " reference(s)");
		}
		return result;
	}

	/**
	 * Transfer statusflag listeners from one pathway to another.
	 * This is used needed when copies of the pathway are created / returned
	 * by UndoManager. The status flag listeners are only interested in status flag
	 * events of the active copy.
	 */
	public void transferStatusFlagListeners(Pathway dest)
	{
		for (Iterator<StatusFlagListener> i = statusFlagListeners.iterator(); i.hasNext(); )
		{
			StatusFlagListener l = i.next();
			dest.addStatusFlagListener(l);
			i.remove();
		}
	}

	public void printRefsDebugInfo()
	{
		for (PathwayElement elt : dataObjects)
		{
			elt.printRefsDebugInfo();
		}
	}
	

}
