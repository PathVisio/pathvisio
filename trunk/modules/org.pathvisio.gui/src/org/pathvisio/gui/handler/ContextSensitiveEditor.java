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
package org.pathvisio.gui.handler;

import java.util.Collection;

import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.gui.SwingEngine;

/**
 * This interface indicates that the {@link TypeHandler}'s editor is context sensitive and needs to have additional
 * information before its {@link TypeHandler#getValueEditor()} is called.
 * <p>
 * For example, the {@link DataSourceHandler} will filter the list of available data sources depending on the organism
 * the pathway is for.  This information is dynamic, and TypeHandlers that implement this method will have
 * {@link #updateEditor} called immediately before {@link TypeHandler#getValueEditor()} is called so that it can be
 * prepared for use appropriately.
 *
 * @author Mark Woon
 */
public interface ContextSensitiveEditor {

	/**
	 * Update the editor in preparation for use.
	 *
	 * @param pathway the current pathway
	 * @param propHandler the PropertyHandler containing the elements whose properties are being edited
	 */
	void updateEditor(SwingEngine swingEngine, Collection<PathwayElement> elements, Pathway pathway, PropertyView propHandler);
}
