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
package org.pathvisio.gui.swing.dialogs;

import java.awt.Component;
import java.awt.Frame;
import java.util.HashSet;
import java.util.Set;

import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.PathwayElement;

/**
 * This is a factory class for the PathwayElement Popup dialog, which pops up after double-clicking an element in the pathway.
 * A dialog is constructed depending on the type of the element that was clicked.
 * <p>
 * It is possible to add hooks to this handler, so that plugins can register new panels to be added to PathwayElement Popup dialogs.
 */
public class PopupDialogHandler
{
	final private SwingEngine swingEngine;
	
	public PopupDialogHandler(SwingEngine swingEngine)
	{
		this.swingEngine = swingEngine;
	}
	
	/**
	 * Implement this interface if you want to add a hook to the handler.
	 */
	public interface PopupDialogHook
	{
		/**
		 * This method is called just before the PathwayElementDialog is shown.
		 * @param e the element which will be edited
		 * @param dlg A partially constructed dialog, which may be modified by the hook. 
		 */
		void popupDialogHook (PathwayElement e, PathwayElementDialog dlg);
	}
	
	private Set<PopupDialogHook> hooks = new HashSet<PopupDialogHook>();
	
	/**
	 * register a new hook.
	 */
	public void addHook(PopupDialogHook hook)
	{
		hooks.add(hook);
	}
	
	public void removeHook(PopupDialogHook hook)
	{
		hooks.remove(hook);
	}
	
	/**
	 * Create a dialog for the given pathway element.
	 * @param e The pathway element
	 * @param readonly Whether the dialog should be read-only or not
	 * @return An instance of a subclass of PathwayElementDialog (depends on the
	 * type attribute of the given PathwayElement, e.g. type DATANODE returns a DataNodeDialog
	 */
	public PathwayElementDialog getInstance(PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		PathwayElementDialog result = null;
		
		switch(e.getObjectType()) {
		case LABEL:
		case SHAPE:
			result = new LabelDialog(swingEngine, e, readonly, frame, locationComp);
		case DATANODE:
			result = new DataNodeDialog(swingEngine, e, readonly, frame, locationComp);
		case INFOBOX:
			result = new PathwayElementDialog(swingEngine, e.getParent().getMappInfo(), readonly, frame, "Pathway properties", locationComp);
		default:
			result = new PathwayElementDialog(swingEngine, e, readonly, frame, "Element properties", locationComp);
		}	
		
		for (PopupDialogHook hook : hooks)
		{
			hook.popupDialogHook(e, result);
		}
		
		result.refresh();
		return result;
	}
}
