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
package org.pathvisio.core.model;

/**
 * This interface represents a type (the <i>Type</i> in Property).
 *
 * @author Mark Woon
 */
public interface PropertyType {

	/**
	 * The id for this type.
	 */
	String getId();

	/**
	 * Handle the translation of a Property from a (JDOM) GPML element to a PathwayElement.
	 * This is responsible for copying the value(s) of prop from gpmlElem to pwElem.
	 */
	//void translateFromGpml(Property prop, Element gpmlElem, PathwayElement pwElem) throws ConverterException;

	/**
	 * Handle the translation of a Property from a PathwayElement to a (JDOM) GPML element.
	 * This is responsible for copying the value(s) of a prop from pwElem to gpmlElem.
	 */
	//void translateToGpml(Property prop, PathwayElement pwElem, Element gpmlElem) throws ConverterException;
}
