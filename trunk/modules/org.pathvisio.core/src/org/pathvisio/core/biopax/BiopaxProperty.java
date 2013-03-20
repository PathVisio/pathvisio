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
package org.pathvisio.core.biopax;

import org.jdom.Element;
import org.pathvisio.core.debug.Logger;

/**
 * Represents a property of a BiopaxElement, such as "Title" for a PublicationXref
 */
public class BiopaxProperty extends Element {

	public static final int UNBOUND = -1;

	protected int maxCardinality;

	private BiopaxProperty() {
		setNamespace(Namespaces.BIOPAX);
	}

	BiopaxProperty(String name, String value, String datatype, int maxCardinality) {
		this();
		setName(name);
		setText(value);
		setDatatype(datatype);
		this.maxCardinality = maxCardinality;
	}

	BiopaxProperty(String name, String value, String datatype) {
		this(name, value, datatype, UNBOUND);
	}

	BiopaxProperty(PropertyType type, String value) {
		this(type.name(), value, type.datatype, type.maxCardinality);
	}

	public BiopaxProperty(Element e) {
		this();
		setName(e.getName());
		PropertyType pt = PropertyType.byName(name);
		if(pt == null) {
			Logger.log.warn ("Unknown property: " + name);
			maxCardinality = UNBOUND;
			setDatatype("http://www.w3.org/2001/XMLSchema#string");
		}
		else
		{
			setDatatype(pt.datatype);
			maxCardinality = pt.maxCardinality;
		}
		setText(e.getText());
		
	}

	public void setDatatype(String datatype) {
		setAttribute("datatype", datatype, Namespaces.RDF);
	}

	public String getDatatype() {
		return getAttributeValue("datatype", Namespaces.RDF);
	}

	public int getMaxCardinality() {
		return maxCardinality;
	}
}
