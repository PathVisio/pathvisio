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
package org.pathvisio.data;

import java.awt.Component;

/**
 * Interface to add user-interface functionality to
 * DBConnection classes. When a database
 * class implements this interface, it can provide
 * dialogs for opening and creating a database of this type.
 */
public interface DBConnectorSwing
{

	/**
	 * This method will be called when the user
	 * needs to select a database. Open a dialog (e.g. FileDialog) in this
	 * method to let the user select the database and return the database name.
	 * @param shell The shell to create the dialog
	 * @return The database name that was selected by the user, or null if no database was selected
	 */
	public String openChooseDbDialog(Component parent);

	/**
	 * This method will be called when the user
	 * needs to select a database to create. Open a dialog (e.g. FileDialog) in this
	 * method to let the user select the new database name/file/directory and return the database name.
	 * @param shell The shell to create the dialog
	 * @return The database name to create, or null if no database was specified
	 */
	public String openNewDbDialog(Component parent, String defaultName);

}
