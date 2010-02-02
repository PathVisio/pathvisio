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
package org.pathvisio.gui.swing.propertypanel;

import java.util.Collection;
import java.util.HashSet;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Property;
import org.pathvisio.view.VPathway;

/**
 * PropertyView ties together functionality to view / edit a property
 * on one or more PathwayElements at the same time.
 */
public class PropertyView implements Comparable<PropertyView> {
	private VPathway vPathway;
	Collection<PathwayElement> elements;
	private Object value;
	private Object type;
	boolean different;
	private TableCellRenderer propertyLabelRenderer = new PropertyLabelRenderer();

	/**
	 * @param aType is either String for a dynamic property,
	 * or StaticProperty for a static property;
	 * @param aVPathway is used to register undo actions when setting a value
	 * to this property. May be null, in which case no undo actions are registered.
	 */
	public PropertyView(VPathway aVPathway, Object aType) {
		type = aType;
		if (!(type instanceof String || type instanceof Property))
		{
			throw new IllegalArgumentException();
		}
		vPathway = aVPathway;
		elements = new HashSet<PathwayElement>();
	}

	/**
	 * Add a PathwayElement to the set of elements that are viewed / edited together
	 */
	public void addElement(PathwayElement e) {
		elements.add(e);
		refreshValue();
	}

	/**
	 * Remove a PathwayElement to the set of elements that are viewed / edited together
	 */
	public void removeElement(PathwayElement e) {
		elements.remove(e);
		refreshValue();
	}

	/**
	 * Refresh the viewer / editor value by checking all PathwayElements
	 * This notifies the PropertyView that one of the PathwayElements has changed
	 * or that the PathwayElement list has been changed, and a new value should be cached.
	 */
	public void refreshValue() {
		boolean first = true;
		for(PathwayElement e : elements) {
			Object o = e.getPropertyEx(type);
			if(!first && (o == null || !o.equals(value))) {
				different = true;
				return;
			}
			value = o;
			first = false;
		}
	}

	/**
	 * Number of PathwayElement's being edited / viewed
	 */
	public int elementCount() { return elements.size(); }

	/**
	 * Get a name for the property being edited.
	 */
	public String getName()
	{
		if (type instanceof Property) {
			return ((Property)type).getName();
		}
		else
		{
			Property prop = PropertyDisplayManager.getDynamicProperty((String)type);
			if (prop != null) {
				return prop.getName();
			}
			return type.toString();
		}
	}

	/**
	 * Get a description for the property being edited.
	 */
	public String getDescription()
	{
		if (type instanceof Property) {
			return ((Property)type).getDescription();
		}
		else
		{
			Property prop = PropertyDisplayManager.getDynamicProperty((String)type);
			if (prop != null) {
				return prop.getDescription();
			}
			return null;
		}
	}

	/**
	 * Set a value for the property being edited.
	 * This will update all PathwayElements that are being edited at once.
	 */
	public void setValue(Object value) {
		this.value = value;
		if(value != null) {
			if (vPathway != null)
			{
				vPathway.getUndoManager().newAction (
					"Change " + type + " property");
			}
			for(PathwayElement e : elements) {
				e.setPropertyEx(type, value);
			}
		}
	}

	/**
	 * The value of the property being viewed / edited.
	 * This value is cached, call refreshValue() to update the cache.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * The type of the property being edited. This is a String
	 * if the property is dynamic, or a Property if the property
	 * is static. (See PathwayElement for an explanation of static / dynamic)
	 */
	public Object getType() {
		return type;
	}

	private TypeHandler getTypeHandler() {
		if (type instanceof Property) {
			return PropertyDisplayManager.getTypeHandler(((Property)type).getType());
		} else {
			Property prop = PropertyDisplayManager.getDynamicProperty((String)type);
			if (prop != null) {
				return PropertyDisplayManager.getTypeHandler(prop.getType());
			}
			return null;
		}
	}

	/**
	 * Returns true if the PathwayElement's being edited differ for this Property.
	 */
	public boolean hasDifferentValues() { return different; }


	/**
	 * Returns a TableCellRenderer suitable for rendering this property's label.
	 */
	public TableCellRenderer getLabelRenderer() {

		TypeHandler handler = getTypeHandler();
		TableCellRenderer renderer = null;
		if (handler != null) {
			renderer = handler.getLabelRenderer();
		}
		if (renderer == null &&
				(type instanceof Property || PropertyDisplayManager.getDynamicProperty((String)type) != null)) {
			return propertyLabelRenderer;
		}
		return renderer;
	}


	/**
	 * Returns a TableCellRenderer suitable for rendering this property's value.
	 */
	public TableCellRenderer getCellRenderer()
	{
		if(hasDifferentValues()) {
			return differentRenderer;
		}
		TypeHandler handler = getTypeHandler();
		if (handler != null) {
			return handler.getValueRenderer();
		}
		return null;
	}

	/**
	 * Returns a TableCellEditor suitable for editing this property's value.
	 *
	 * @param swingEngine: the comments editor requires a connection to swingEngine, so you need to pass it here.
	 */
	public TableCellEditor getCellEditor(SwingEngine swingEngine) {

		TypeHandler handler = getTypeHandler();
		if (handler != null) {
			if (handler instanceof ContextSensitiveEditor) {
				((ContextSensitiveEditor)handler).updateEditor(swingEngine, vPathway.getPathwayModel(), this);
			}
			return handler.getValueEditor();
		}
		return null;
	}


	private static DefaultTableCellRenderer differentRenderer = new DefaultTableCellRenderer() {

		protected void setValue(Object value) {
			value = "Different values";
			super.setValue(value);
		}
	};


	public int compareTo(PropertyView arg0)
	{
		if (arg0 == null) throw new NullPointerException();


		Object aType = type;
		int aOrder = -1;
		Object bType = arg0.getType();
		int bOrder = -1;

		if (aType instanceof String) {
			if (PropertyDisplayManager.getDynamicProperty((String)aType) != null) {
				aType = PropertyDisplayManager.getDynamicProperty((String)aType);
				aOrder = PropertyDisplayManager.getPropertyOrder((Property)aType);
			}
		} else {
			aOrder = PropertyDisplayManager.getPropertyOrder((Property)aType);
		}
		if (bType instanceof String) {
			if (PropertyDisplayManager.getDynamicProperty((String)bType) != null) {
				bType = PropertyDisplayManager.getDynamicProperty((String)bType);
				bOrder = PropertyDisplayManager.getPropertyOrder((Property)bType);
			}
		} else {
			bOrder = PropertyDisplayManager.getPropertyOrder((Property)bType);
		}

		if (aOrder < 0 && bOrder < 0) {
			return ((String)aType).compareTo((String)bType);
		}
		int rez = aOrder - bOrder;
		if (rez == 0) {
			// same order, sort by alpha
			rez = ((Property)aType).getName().compareTo(((Property)bType).getName());
		}
		return rez;
	}
}
