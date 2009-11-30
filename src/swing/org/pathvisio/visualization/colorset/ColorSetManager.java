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
package org.pathvisio.visualization.colorset;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.pathvisio.debug.Logger;

/**
 * Manages colorSets (a ColorSet is a combination of rules and / or gradients)
 * Can send events when a colorSet is added or removed.
 */
public class ColorSetManager {
	public final static String XML_ELEMENT = "color-sets";

	private Map<String, ColorSet> colorSets = new HashMap<String, ColorSet>();

	/**
	 * Gets the {@link ColorSet}s used for the currently loaded Expression data
	 */
	public Collection<ColorSet> getColorSets() { return colorSets.values(); }

	public boolean nameExists(String name) {
		return colorSets.containsKey(name);
	}

	public String getNewName() {
		String prefix = "color set";
		int i = 1;
		String name = prefix;
		while(nameExists(name)) name = prefix + "-" + i++;
		return name;
	}

	public void newColorSet(String name) {
		if(name == null) name = getNewName();
		addColorSet(new ColorSet(this));
	}

	public void addColorSet(ColorSet cs)
	{
		colorSets.put(cs.getName(), cs);
		fireColorSetEvent (
			new ColorSetEvent (
				ColorSetManager.class,
				ColorSetEvent.COLORSET_ADDED));
	}

	/**
	 * Removes this {@link ColorSet}
	 * @param cs Colorset to remove
	 */
	public void removeColorSet(ColorSet cs) {
		if(colorSets.containsKey(cs.getName()))
		{
			colorSets.remove(cs.getName());
			fireColorSetEvent(
				new ColorSetEvent(ColorSetManager.class, ColorSetEvent.COLORSET_REMOVED));
		}
	}

	/**
	 * Clears all color-set information
	 */
	public void clearColorSets() {
		colorSets.clear();
		fireColorSetEvent(
				new ColorSetEvent (ColorSetManager.class, ColorSetEvent.COLORSET_REMOVED));
	}

	public ColorSet getColorSet(String name) {
		return colorSets.get(name);
	}

	public Element getXML() {
		Element cse = new Element(XML_ELEMENT);

		for(ColorSet cs : colorSets.values()) cse.addContent(cs.toXML());

		return cse;
	}

	public void fromXML(Element xml) {
		clearColorSets();

		if(xml == null) return;

		for(Object o : xml.getChildren(ColorSet.XML_ELEMENT)) {
			Logger.log.trace("Adding " + o);
			addColorSet(ColorSet.fromXML((Element) o, this));
		}
	}

	Document parseInput(InputStream in) throws JDOMException, IOException {
		SAXBuilder parser = new SAXBuilder();
		return parser.build(in);
	}

	/**
	 * Fire a {@link ColorSetEvent} to notify all {@link VisualizationListener}s registered
	 * to this class
	 * //TODO, should be private...
	 */
	void fireColorSetEvent(ColorSetEvent e)
	{
		for(ColorSetListener l : listeners)
		{
			l.colorSetEvent(e);
		}
	}

	/**
	   List of listeners
	 */
	private List<ColorSetListener> listeners = new ArrayList<ColorSetListener>();

	/**
	 * Add a {@link ColorSetListener}, that will be notified if an
	 * event related to visualizations occurs
	 */
	public void addListener(ColorSetListener l)
	{
		if(listeners == null)
			listeners = new ArrayList<ColorSetListener>();
		listeners.add(l);
	}

	public void removeListener (ColorSetListener l)
	{
		if (listeners == null)
			return;
		listeners.remove (l);
	}

	/**
	 * Interface for classes that want to receive a ColorSetEvent
	 */
	public interface ColorSetListener extends EventListener
	{
		public void colorSetEvent (ColorSetEvent e);
	}
}
