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
package org.pathvisio.gui.handler;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.core.model.PropertyType;

/**
 * This interface defines a handler that knows how to display/edit a {@link PropertyType}.
 *
 * @author Mark Woon
 */
public interface TypeHandler {

	/**
	 * The {@link PropertyType} this handler is responsible for.
	 */
	PropertyType getType();

	/**
	 * Gets the renderer used to display the label.
	 *
	 * @return the TableCellRenderer to use to display the label (if null, default will be used)
	 */
	TableCellRenderer getLabelRenderer();

	/**
	 * Gets the renderer used to display the value.
	 *
	 * @return the TableCellRenderer to use to display the value (if null, default will be used)
	 */
	TableCellRenderer getValueRenderer();

	/**
	 * Gets the editor used to edit the value.
	 *
	 * @return the TableCellRenderer to use to edit the value (if null, default will be used)
	 */
	TableCellEditor getValueEditor();
}
