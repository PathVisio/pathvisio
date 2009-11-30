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
package org.pathvisio.view;

import java.util.ArrayList;
import java.util.List;

import org.pathvisio.Engine;
import org.pathvisio.model.Pathway;

/** Manages a stack of undo actions */
public class UndoManager
{
	public static final String CANT_UNDO = "Can't undo";

	private List<UndoAction> undoList = new ArrayList<UndoAction>();

	private Pathway pathway;

	public void setPathway (Pathway pathway) {
		this.pathway = pathway;
	}

	private Engine engine;

	/**
	 * Check if this undo manager is active.
	 * If there is no instance of Engine available,
	 * the undo manager will not record any undo events.
	 * Provide an instance of engine to activate the undo
	 * manager.
	 * @see #activate(Engine)
	 */
	public boolean isActive() {
		return engine != null;
	}

	/**
	 * Set the engine for this undo manager.
	 * @param engine
	 */
	public void activate (Engine engine) {
		this.engine = engine;
	}

	/**
	 * Get the engine for this undo manager. The engine
	 * can be null, in which case the undo manager will be
	 * inactive.
	 * @return The Engine, or null if the undo manager is inactive.
	 */
	protected Engine getEngine() {
		return engine;
	}

	static final int MAX_UNDO_SIZE = 25;
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
		if(!isActive()) return; //Don' record event if inactive

		act.setUndoManager(this);
		undoList.add (act);
		if (undoList.size() > MAX_UNDO_SIZE)
		{
			undoList.remove(0);
		}
		fireUndoManagerEvent (new UndoManagerEvent (getTopMessage()));
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
		if(!isActive()) return; //Don' record event if inactive

		if(pathway != null) {
			UndoAction x = new UndoAction (desc, (Pathway)pathway.clone());
			x.setUndoManager(this);
			newAction (x);
		}
	}

	public String getTopMessage()
	{
		String result;
		if (undoList.size() == 0)
		{
			result = CANT_UNDO;
		}
		else
		{
			result = undoList.get(undoList.size() - 1).getMessage();
		}
		return result;
	}

	void undo()
	{
		if (undoList.size() > 0 && isActive())
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

	private boolean disposed = false;
	public void dispose()
	{
		assert (!disposed);
		undoList.clear();
		listeners.clear();
		disposed = true;
	}

	/**
	   debugging helper function
	 */
	@SuppressWarnings("unused")
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
		System.out.print (pathway.summary());
		System.out.println();
	}
}
