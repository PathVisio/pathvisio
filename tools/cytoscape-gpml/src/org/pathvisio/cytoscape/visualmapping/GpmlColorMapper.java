package org.pathvisio.cytoscape.visualmapping;


import java.awt.Color;
import java.util.Map;

import cytoscape.visual.mappings.PassThroughMapping;

public class GpmlColorMapper extends PassThroughMapping {
	public GpmlColorMapper(Color defaultColor) {
		super(defaultColor);
	}
	
	public GpmlColorMapper(Color defaultColor, byte mapType) {
		super(defaultColor, mapType);
	}

	public GpmlColorMapper(Color defaultColor, String attrName) {
		super(defaultColor, attrName);
	}
	
	public Object calculateRangeValue(Map attrBundle) {
		if (attrBundle == null || getControllingAttributeName() == null)
			return null;

		Object value = attrBundle.get(getControllingAttributeName());

		if (value != null) {
			String colStr = value.toString();
			Color c = Color.decode(colStr);
			return c;
		}
		return null;
	}
	
}
