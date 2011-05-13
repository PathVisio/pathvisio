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
package org.pathvisio.desktop.util;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import org.pathvisio.core.debug.Logger;

/**
 * Contains a utility function to insert a piece of text at the cursor in a TextField.
 */
public class TextFieldUtils
{
	/**
	 * Inserts text at cursor, ensures that there are spaces around it
	 *	TODO: don't insert spaces when not necessary.
	 */
	public static void insertAtCursorWithSpace (JTextField field, String toInsert)
	{
		try
		{
			field.getDocument().insertString(
					field.getCaretPosition(), " " + toInsert + " ", null);
		}
		catch (BadLocationException e) { Logger.log.error ("BadLocationException", e); }
	}

}
