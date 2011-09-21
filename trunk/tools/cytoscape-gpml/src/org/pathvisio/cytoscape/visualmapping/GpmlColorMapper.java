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
package org.pathvisio.cytoscape.visualmapping;

import cytoscape.visual.mappings.PassThroughMapping;

import java.awt.Color;
import java.util.Map;

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
