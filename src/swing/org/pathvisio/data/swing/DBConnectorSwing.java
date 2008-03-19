package org.pathvisio.data.swing;

import org.eclipse.swt.widgets.Shell;

/**
 * Interface to add user-interface functionality to
 * DBConnection classes. When a database 
 * class implements this interface, it can provide
 * dialogs for opening and creating a database of this type.
 *
 * Swing equivalent of DBConnectorSwt.
 */
public interface DBConnectorSwing 
{
	/**
	 * This method will be called when the user
	 * needs to select a database. Open a dialog (e.g. FileDialog) in this
	 * method to let the user select the database and return the database name.
	 * @return The database name that was selected by the user, or null if no database was selected
	 */
	public String openChooseDbDialog();
	
	/**
	 * This method will be called when the user
	 * needs to select a database to create. Open a dialog (e.g. FileDialog) in this
	 * method to let the user select the new database name/file/directory and return the database name.
	 * @return The database name to create, or null if no database was specified
	 */
	public String openNewDbDialog(String defaultName);
}
