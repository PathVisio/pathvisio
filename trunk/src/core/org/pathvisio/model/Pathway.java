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
package org.pathvisio.model;

import java.io.File;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.GraphLink.GraphRefContainer;

/**
* This class is the model for pathway data. It is responsible for
* storing all information necessary for maintaining, loading and saving
* pathway data.
* 
* Pathway contains multiple PathwayElements. Pathway is guaranteed
* to always have exactly one object of the type MAPPINFO and exactly
* one object of the type INFOBOX.
*/
public class Pathway implements PathwayListener
{		
	/**
	   "changed" tracks if the Pathway has been changed since the file
	   was opened or last saved. New pathways start changed.
	 */
	private boolean changed = true;
	public boolean hasChanged() { return changed; }
	/**
	   clearChangedFlag should be called after when the current
	   pathway is known to be the same as the one on disk. This
	   happens when you just opened it, or when you just saved it.
	*/
	private void clearChangedFlag() { changed = false; }
	/**
	   To be called after each edit operation
	*/
	private void markChanged()
	{
		changed = true;
	}
	
	/**
	 * factor to convert screen cordinates used in GenMAPP to pixel cordinates
	 * NOTE: maybe it is better to adapt gpml to store cordinates as pixels and
	 * divide the GenMAPP cordinates by this factor on conversion
	 * 
	 * @deprecated
	 */
	final public static int OLD_GMMLZOOM = 15;
		
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
	
	private PathwayElement mappInfo = null;
	private PathwayElement infoBox = null;
	private PathwayElement biopax = null;
	
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
		biopax = new PathwayElement(ObjectType.BIOPAX);
		this.add(biopax);
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
		if (o.getObjectType() == ObjectType.MAPPINFO && o != mappInfo && mappInfo != null)
		{
			replaceUnique (mappInfo, o);
			mappInfo = o;
			return;
		}
		// There can be only one InfoBox object, so if we're trying to add it, remove the old one.
		if (o.getObjectType() == ObjectType.INFOBOX && o != infoBox && infoBox != null)
		{
			replaceUnique (infoBox, o);
			infoBox = o;
			return;
		}
		// There can be zero or one Biopax object, so if we're trying to add it, remove the old one.
		if(o.getObjectType() == ObjectType.BIOPAX && o != biopax)
		{
			if(biopax != null) {
				replaceUnique (biopax, o);
			}
			biopax = o;
			return;
		}
		if (o.getParent() == this) return; // trying to re-add the same object
		if (o.getParent() != null) { o.getParent().remove(o); }
		dataObjects.add(o);
		o.addListener(this);
		o.setParent(this);
		fireObjectModifiedEvent(new PathwayEvent(o, PathwayEvent.ADDED));
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
		fireObjectModifiedEvent (new PathwayEvent (oldElt, PathwayEvent.DELETED));
		oldElt.removeListener (this);
		dataObjects.remove(oldElt);
		oldElt.setParent (null);
		newElt.addListener (this);
		newElt.setParent (this);
		dataObjects.add(newElt);
		fireObjectModifiedEvent(new PathwayEvent(newElt, PathwayEvent.ADDED));		
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
		o.removeListener(this);
		dataObjects.remove(o);
		List<GraphRefContainer> references = getReferringObjects(o.getGraphId());
		for(GraphRefContainer refc : references) {
			refc.setGraphRef(null);
		}
		fireObjectModifiedEvent(new PathwayEvent(o, PathwayEvent.DELETED));
		o.setParent(null);
	}

	/**
	 * Stores references of line endpoints to other objects
	 */
	private HashMap<String, List<GraphRefContainer>> graphRefs = new HashMap<String, List<GraphRefContainer>>();
	private Set<String> ids = new HashSet<String>();
	
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
	
	private HashMap<String, Set<PathwayElement>> groupRefs = new HashMap<String, Set<PathwayElement>>();
	
	public void addRef (String ref, PathwayElement child)
	{
		if (groupRefs.containsKey(ref))
		{
			Set<PathwayElement> s = groupRefs.get(ref);
			s.add(child);
		}
		else
		{
			Set<PathwayElement> s = new HashSet<PathwayElement>();
			s.add(child);		
			groupRefs.put(ref, s);
			
		}
	}
	
	public void removeRef (String id, PathwayElement child)
	{
		if (!groupRefs.containsKey(id)) throw new IllegalArgumentException();
		
		groupRefs.get(id).remove(child);
		if (groupRefs.get(id).size() == 0)
			groupRefs.remove(id);
	}
	
	
	/**
	 * Remove a reference to another Id. 
	 * @param id
	 * @param target
	 */
	public void removeGraphRef (String id, GraphRefContainer target)
	{
		if (!graphRefs.containsKey(id)) throw new IllegalArgumentException();
		
		graphRefs.get(id).remove(target);
		if (graphRefs.get(id).size() == 0)
			graphRefs.remove(id);
	}
	
	private HashMap<String, PathwayElement> groups = new HashMap<String, PathwayElement>();
	
	/**
	 * Registers an id that can subsequently be used for
	 * referrral. It is tested for uniqueness.
	 * @param id
	 */
	public void addId (String id)
	{
		if (id == null)
		{
			throw new IllegalArgumentException ("unique id can't be null");
		}
		if (ids.contains(id))
		{
			throw new IllegalArgumentException ("id '" + id + "' is not unique");
		}
		ids.add (id);
	
	}
	
	public void addGroupId(String id, PathwayElement group) {
		addId(id);
		groups.put(id, group);
	}
	
	public PathwayElement getGroupById(String id) {
		return groups.get(id);
	}
	
	public void removeId (String id)
	{
		ids.remove(id);
	}
	
	/*AP20070508*/	
	/**
	 * Generate random ids, based on strings of hex digits (0..9 or a..f)
	 * Ids are unique across both graphIds and groupIds per pathway
	 * @return an Id unique for this pathway
	 */
	public String getUniqueId ()
	{
		String result;
		Random rn = new Random();
		int mod = 0x600; // 3 hex letters
		int min = 0xa00; // has to start with a letter
		// in case this map is getting big, do more hex letters
		if ((ids.size()) > 1000) 
		{
			mod = 0x60000;
			min = 0xa0000;
		}
				
		do
		{
			result = Integer.toHexString(Math.abs(rn.nextInt()) % mod + min);
		}
		while (ids.contains(result));
		
		return result;
	}
	
	/**
	 * Returns all lines that refer to an object with a particular graphId.
	 */
	public List<GraphRefContainer> getReferringObjects (String id)
	{
		List<GraphRefContainer> refs = graphRefs.get(id);
		if(refs != null) {
			refs = new ArrayList<GraphRefContainer>(refs);
		} else {
			refs = new ArrayList<GraphRefContainer>();
		}
		return refs;
	}
	
	private File sourceFile = null;
	
	/**
	 * Gets the xml file containing the Gpml/mapp pathway currently displayed
	 * @return current xml file
	 */
	public File getSourceFile () { return sourceFile; }
	private void setSourceFile (File file) { sourceFile = file; }

	/**
	 * Contructor for this class, creates a new gpml document
	 */
	public Pathway() 
	{
		mappInfo = new PathwayElement(ObjectType.MAPPINFO);
		this.add (mappInfo);
		infoBox = new PathwayElement(ObjectType.INFOBOX);
		this.add (infoBox);
	}
	
	static final double M_INITIAL_BOARD_WIDTH = 18000;
	static final double M_INITIAL_BOARD_HEIGHT = 12000;
	
	/*
	 * Call when making a new mapp.
	 */	
	public void initMappInfo()
	{
		mappInfo.setMBoardWidth(M_INITIAL_BOARD_WIDTH);
		mappInfo.setMBoardHeight(M_INITIAL_BOARD_HEIGHT);
		mappInfo.setWindowWidth(M_INITIAL_BOARD_WIDTH);
		mappInfo.setWindowHeight(M_INITIAL_BOARD_HEIGHT);
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

	}

	public void readFromXml(Reader in, boolean validate) throws ConverterException
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
		
	public void readFromMapp (File file) throws ConverterException
	{
        String inputString = file.getAbsolutePath();

        MappFormat.readFromMapp (inputString, this);
        
        setSourceFile (file);
        clearChangedFlag();
	}
	
	public void writeToMapp (File file) throws ConverterException
	{
		String[] mappInfo = MappFormat.uncopyMappInfo (this);
		List<String[]> mappObjects = MappFormat.uncopyMappObjects (this);
		
		MappFormat.exportMapp (file.getAbsolutePath(), mappInfo, mappObjects);
		setSourceFile (file);
	}

	public void writeToSvg (File file) throws ConverterException
	{
		SvgFormat.writeToSvg (this, file);
	}
	
	private List<PathwayListener> listeners = new ArrayList<PathwayListener>();
	public void addListener(PathwayListener v) { listeners.add(v); }
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
			g.gmmlObjectModified(e);
		}
	}
	
	/**
	 * Get the systemcodes of all genes in this pathway
	 * @return	a list of systemcodes for every gene on the mapp
	 */
	public ArrayList<String> getSystemCodes()
	{
		ArrayList<String> systemCodes = new ArrayList<String>();
		for(PathwayElement o : dataObjects)
		{
			if(o.getObjectType() == ObjectType.DATANODE)
			{
				systemCodes.add(o.getSystemCode());
			}
		}
		return systemCodes;
	}
	
	/**
	 * register undo actions,
	 * disabled for the moment.
	 */
	public void gmmlObjectModified(PathwayEvent e) 
	{
		markChanged();
	}

	public Pathway clone()
	{
		Pathway result = new Pathway();
		int i = 0;
		for (PathwayElement pe : dataObjects)
		{
			result.add (pe.copy());
			i++;
		}
		System.out.println (i + " objects copied");
		return result;
	}

}
