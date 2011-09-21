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
package org.pathvisio.core.view;

import org.pathvisio.core.model.Pathway;

/**
 * a single item on the undo stack.
 * Stores a complete copy of the Pathway as it was before the user modified it, so
 * it can be restored.
 *
 * TODO: remember selection state as well.
 */
public class UndoAction
{
	public UndoAction(String aMessage,
					  Pathway current)
	{
		message = aMessage;
		originalState = current;
	}

	private String message;
	private Pathway originalState;
	private UndoManager undoMgr;

	/**
	 * Set the undo manager that will be used to perform
	 * the undo. This will be set by {@link UndoManager#newAction}
	 * @param undoMgr
	 */
	protected void setUndoManager(UndoManager undoMgr) {
		this.undoMgr = undoMgr;
	}

	public String getMessage()
	{
		return message;
	}

	void printSummary()
	{
		System.out.printf ("'%20s'\n", message);
		System.out.print ("" + originalState.summary());
	}

	public void undo()
	{
		/*
		UndoManager um = Engine.getCurrent().getActiveVPathway().getUndoManager();
		Engine.getCurrent().createVPathway (originalState);
		Engine.getCurrent().getActiveVPathway().setUndoManager(um);
		*/
		if(undoMgr != null) {
			undoMgr.getEngine().replacePathway (originalState);
		}
	}
}
