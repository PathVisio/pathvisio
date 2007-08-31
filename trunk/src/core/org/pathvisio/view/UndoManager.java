// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.view;

import java.util.ArrayList;
import java.util.List;
import org.pathvisio.Engine;
import org.pathvisio.model.Pathway;

public class UndoManager 
{
	private List<UndoAction> undoList = new ArrayList<UndoAction>();

	/**
	   Insert a new action into the Undo Queue based on an UndoAction
	   object that already contains a copy of the original state of
	   the pathway. This way you can actually record the action after
	   the pathway has already modified, useful for collapsing
	   multiple drag events into one action.
	   
	   @param act: UndoAction containing pre-recorded pathway state
	   and description of the action.
	 */
	public void newAction (UndoAction act)
	{		
		undoList.add (act);
		fireUndoManagerEvent (new UndoManagerEvent (act.getMessage()));
	}

	/**
	   Insert a new action into the Undo Queue. This method
	   will make a copy of the current state of the pathway, so call
	   this method before the action actually takes place.
	   
	   @param desc: description of the change, for display in the edit
	   menu.
	 */
	public void newAction (String desc)
	{
		Pathway pwy = Engine.getCurrent().getActivePathway();
		if(pwy != null) {
			UndoAction x = new UndoAction (desc, (Pathway)pwy.clone());
			undoList.add (x);
			fireUndoManagerEvent (new UndoManagerEvent (x.getMessage()));
		}
	}

	public String getTopMessage()
	{
		String result;
		if (undoList.size() == 0)
		{
			result = "Can't undo";
		}
		else
		{
			result = undoList.get(undoList.size() - 1).getMessage();
		}
		return result;
	}
	
	void undo()
	{
		if (undoList.size() > 0)
		{
			UndoAction a = undoList.get(undoList.size()-1);
			a.undo();
			undoList.remove(a);
			fireUndoManagerEvent (new UndoManagerEvent (getTopMessage()));			
		}
	}

	private List <UndoManagerListener> listeners =
		new ArrayList <UndoManagerListener>();

	public void addListener (UndoManagerListener v) { listeners.add(v); }
	public void removeListener (UndoManagerListener v) { listeners.remove(v); }

	/**
	   This is called whenever a new item is added to the Undo Manager,
	   or when an undo action takes place.
	   mainly intended for the menu item to update itself.
	 */
	void fireUndoManagerEvent (UndoManagerEvent e)
	{		
		//printSummary();
		for (UndoManagerListener g : listeners)
		{
			g.undoManagerEvent (e);
		}
	}

	private void printSummary()
	{
		System.out.println ("===============================");
		System.out.println (undoList.size() + " remaining");
		for (int i = undoList.size() - 1; i >= 0; --i)
		{
			System.out.printf ("%3d: ", i);
			undoList.get(i).printSummary();
			System.out.println ();
		}
		System.out.println ("Current pathway");
		System.out.print (Engine.getCurrent().getActivePathway().summary());
		System.out.println();
	}
}
