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
package org.pathvisio.cytoscape;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bridgedb.bio.BioDataSource;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.StaticProperty;
import org.pathvisio.model.StaticPropertyType;

import cytoscape.data.CyAttributes;

/**
 * Default for mapping cytoscape attributes to GPML properties.
 */
public class DefaultAttributeMapper implements AttributeMapper {
	public static final String CY_COMMENT_SOURCE = "cytoscape-attribute: ";
	private Map<StaticProperty, Object> defaultValues;
	private Map<StaticProperty, String> prop2attr;
	private Map<String, StaticProperty> attr2prop;

	private Set<StaticProperty> protectedProps;
	private Set<StaticProperty> hiddenAttrs;

	public DefaultAttributeMapper() {
		prop2attr = new HashMap<StaticProperty, String>();
		attr2prop = new HashMap<String, StaticProperty>();
		defaultValues = new HashMap<StaticProperty, Object>();

		setInitialMappings();
	}

	public String getMapping(StaticProperty prop) {
		//First check if a mapping is explicitly set
		String name = prop2attr.get(prop);
		if(name == null && prop != null) { //If not, use the property tag name
			name = prop.tag();
		}
		return name;
	}

	public StaticProperty getMapping(String attr) {
		StaticProperty prop = attr2prop.get(attr);
		if(prop == null) { //If not, find out if it's a GPML attribute
			prop = StaticProperty.getByTag(attr);
		}
		return prop;
	}

	public void setMapping(String attr, StaticProperty prop) {
		setAttributeToPropertyMapping(attr, prop);
		setPropertyToAttributeMapping(prop, attr);
	}

	public void setDefaultValue(StaticProperty prop, Object value) {
		defaultValues.put(prop, value);
	}

	public Object getDefaultValue(StaticProperty prop) {
		return defaultValues.get(prop);
	}

	/**
	 * Set a mapping from attribute to property
	 * @param attr
	 * @param prop
	 */
	public void setAttributeToPropertyMapping(String attr, StaticProperty prop) {
		attr2prop.put(attr, prop);
	}

	/**
	 * Set a mapping from property to attribute
	 * @param prop
	 * @param attr
	 */
	public void setPropertyToAttributeMapping(StaticProperty prop, String attr) {
		prop2attr.put(prop, attr);
	}

	protected Set<StaticProperty> getProtectedProps() {
		if(protectedProps == null) {
			protectedProps = new HashSet<StaticProperty>();
			protectedProps.add(StaticProperty.CENTERX);
			protectedProps.add(StaticProperty.CENTERY);
			protectedProps.add(StaticProperty.STARTX);
			protectedProps.add(StaticProperty.STARTY);
			protectedProps.add(StaticProperty.ENDX);
			protectedProps.add(StaticProperty.ENDY);
			protectedProps.add(StaticProperty.COMMENTS);
			protectedProps.add(StaticProperty.GRAPHID);
		}
		return protectedProps;
	}

	protected Set<StaticProperty> getHiddenAttrs() {
		if(hiddenAttrs == null) {
			hiddenAttrs = new HashSet<StaticProperty>();
			hiddenAttrs.add(StaticProperty.CENTERX);
			hiddenAttrs.add(StaticProperty.CENTERY);
			hiddenAttrs.add(StaticProperty.STARTX);
			hiddenAttrs.add(StaticProperty.STARTY);
			hiddenAttrs.add(StaticProperty.ENDX);
			hiddenAttrs.add(StaticProperty.ENDY);
			hiddenAttrs.add(StaticProperty.GRAPHID);
			hiddenAttrs.add(StaticProperty.ALIGN);
			hiddenAttrs.add(StaticProperty.BIOPAXREF);
			hiddenAttrs.add(StaticProperty.ENDGRAPHREF);
			hiddenAttrs.add(StaticProperty.FONTNAME);
			hiddenAttrs.add(StaticProperty.FONTSIZE);
			hiddenAttrs.add(StaticProperty.FONTSTYLE);
			hiddenAttrs.add(StaticProperty.FONTWEIGHT);
			hiddenAttrs.add(StaticProperty.LINETHICKNESS);
			hiddenAttrs.add(StaticProperty.HREF);
			hiddenAttrs.add(StaticProperty.ROTATION);
			hiddenAttrs.add(StaticProperty.SHAPETYPE);
			hiddenAttrs.add(StaticProperty.STARTGRAPHREF);
			hiddenAttrs.add(StaticProperty.TRANSPARENT);
			hiddenAttrs.add(StaticProperty.VALIGN);
			hiddenAttrs.add(StaticProperty.ZORDER);
		}
		return hiddenAttrs;
	}

	protected void setInitialMappings() {
		setMapping("canonicalName", StaticProperty.TEXTLABEL);
		setDefaultValue(StaticProperty.DATASOURCE, BioDataSource.UNIPROT);
	}

	public boolean isProtected(StaticProperty prop) {
		return getProtectedProps().contains(prop);
	}

	public void protect(StaticProperty prop) {
		getProtectedProps().add(prop);
	}

	public void unprotect(StaticProperty prop) {
		getProtectedProps().remove(prop);
	}

	public boolean isHidden(StaticProperty prop) {
		return getHiddenAttrs().contains(prop);
	}

	public void attributesToProperties(String id, PathwayElement elm, CyAttributes attr) {
		//Process defaults
		for(StaticProperty prop : defaultValues.keySet()) {
			if(elm.getStaticPropertyKeys().contains(prop)) {
				elm.setStaticProperty(prop, defaultValues.get(prop));
			}
		}

		//Process mappings
		for(String aname : attr.getAttributeNames()) {
			StaticProperty prop = getProperty(aname);

			//Protected property, don't set from attributes
			if(isProtected(prop)) {
//				Logger.log.trace("\tProperty is protected, skipping");
				continue;
			}

			Logger.log.trace ("Property " + aname);
			//No mapping for this attribute, store in attributeMap
			if(prop == null)
			{
//TODO needs more testing
/*				String value = null;
				switch (attr.getType(id))
				{
				case CyAttributes.TYPE_STRING:
					value = attr.getStringAttribute(id, aname);
					break;
				case CyAttributes.TYPE_BOOLEAN:
					value = "" + attr.getBooleanAttribute(id, aname);
					break;
				case CyAttributes.TYPE_FLOATING:
					value = "" + attr.getDoubleAttribute(id, aname);
					break;
				case CyAttributes.TYPE_INTEGER:
					value = "" + attr.getIntegerAttribute(id, aname);
					break;
				case CyAttributes.TYPE_UNDEFINED:
					try
					{
						value = "" + attr.getAttribute(id, aname);
					}
					catch (IllegalArgumentException e)
					{
						Logger.log.error ("Illegal argument exception " + aname + " " + value);
					}
					Logger.log.trace ("Undefined " + value);
					break;
				default:
					//TODO: handle other types such as List
				}
				Logger.log.trace("\tNo mapping found, adding as generic attribute " + aname + " " + value);
				if(value != null && !(value.length() == 0))
				{
					Logger.log.trace ("Setting value");
					elm.setDynamicProperty(aname, value);
				}
*/			}
			else
			{
				//Found a property, try to set it
				try {
					Object value = null;
				PropertyType pt = prop.getType();
				if (pt.equals(StaticPropertyType.BOOLEAN))
					value = attr.getBooleanAttribute(id, aname);
				else if (pt.equals(StaticPropertyType.INTEGER))
					value = attr.getIntegerAttribute(id, aname);
				else if (pt.equals(StaticPropertyType.DOUBLE))
					value = attr.getDoubleAttribute(id, aname);
				else if (pt.equals(StaticPropertyType.COLOR))
					value = Color.decode("" + attr.getIntegerAttribute(id, aname));
				else if (pt.equals(StaticPropertyType.DATASOURCE))
					value = attr.getAttribute(id, aname);
				else if (pt.equals(StaticPropertyType.DB_SYMBOL))
					value = attr.getAttribute(id, aname);
				else if (pt.equals(StaticPropertyType.STRING))
					value = attr.getAttribute(id, aname);
				else if (pt.equals(StaticPropertyType.DB_ID))
					value = attr.getAttribute(id, aname);
				else
					// Don't transfer the attribute, if it's not a supported type
				
				Logger.log.trace("Setting property " + prop + " to " + value);
				if (value != null)
				{
					elm.setStaticProperty(prop, value);
				}
				} catch(Exception e) {
					// Logger.log.error("Unable to parse value for " + prop, e);
				}
			}
		}
	}

	private StaticProperty getProperty(String attributeName) {
		return getMapping(attributeName);
	}

	private String getAttributeName(StaticProperty property) {
		return getMapping(property);
	}

	public void propertiesToAttributes(String id, PathwayElement elm,
			CyAttributes attr) {
		for(StaticProperty prop : elm.getStaticPropertyKeys()) {
			Object value = elm.getStaticProperty(prop);
			if(value != null) {
				String aname = getAttributeName(prop);
				PropertyType pt = prop.getType();
				if (pt.equals(StaticPropertyType.BOOLEAN))
					attr.setAttribute(id, aname, (Boolean) value);
				else if (pt.equals(StaticPropertyType.INTEGER))
					attr.setAttribute(id, aname, (Integer) value);
				else if (pt.equals(StaticPropertyType.DOUBLE))
					attr.setAttribute(id, aname, (Double) value);
				else if (pt.equals(StaticPropertyType.COLOR))
					attr.setAttribute(id, aname, ((Color) value).getRGB());
				else //STRING, DB_ID, DB_SYMBOL, default
					attr.setAttribute(id, aname, value.toString());
				
				//set hidden state; default is visible
				if(isHidden(prop)) {
					attr.setUserVisible(aname, false);
				}
			}
		}
//TODO needs more testing
/*
		// now deal with the attributes in attributeMap.
		for (String key : elm.getDynamicPropertyKeys())
		{
			attr.setAttribute(id, key, elm.getDynamicProperty(key));
		}
*/
	}
}
