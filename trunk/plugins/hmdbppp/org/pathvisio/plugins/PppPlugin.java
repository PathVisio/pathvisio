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
package org.pathvisio.plugins;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import org.pathvisio.gui.swing.PathwayElementMenuListener.PathwayElementMenuHook;
import org.pathvisio.gui.swing.StandaloneEngine;
import org.pathvisio.model.Pathway;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathwayElement;

/**
 * Putative Parts plug-in - 
 * shows a sidebar where other plug-ins can add suggestions.
 * 
 * Also hooks into the right-click menu
 */
public class PppPlugin implements Plugin, PathwayElementMenuHook
{
	PppPane pane;
	HmdbPppAction hmdbPppAction = new HmdbPppAction(this);
	HmdbPppPlugin hmdbPpp = new HmdbPppPlugin();
	
	/**
	 * return the existing PppPane
	 */
	public PppPane getPane()
	{
		return pane;
	}
	
	/**
	 * Initialize plug-in, is called by plugin manager.
	 */
	public void init(StandaloneEngine standaloneEngine) 
	{
		// create new PppPane and add it to the side bar.	
		pane = new PppPane(standaloneEngine.getSwingEngine());
		JTabbedPane sidebarTabbedPane = standaloneEngine.getSwingEngine().getApplicationPanel().getSideBarTabbedPane();
		sidebarTabbedPane.add("PPP", pane);
		
		// register our pathwayElementMenu hook.
		standaloneEngine.getSwingEngine().getApplicationPanel().getPathwayElementMenuListener().addPathwayElementMenuHook(this);
	}
	
	/**
	 * Action to be added to right-click menu.
	 * This action is recycled, i.e. it's instantiated only once but
	 * added each time to the new popup Menu.
	 */
	private class HmdbPppAction extends AbstractAction
	{
		private final PppPlugin parent;
		
		GeneProduct elt;
		
		/**
		 * set the element that will be used as input for the suggestion.
		 * Call this before adding to the menu. 
		 */
		void setElement (GeneProduct anElt)
		{
			elt = anElt;
		}
		
		/** called when plug-in is initialized */
		HmdbPppAction(PppPlugin aParent)
		{
			parent = aParent;
			putValue(NAME, "Hmdb Suggestions");
		}
		
		/** called when user clicks on the menu item */
		public void actionPerformed(ActionEvent e) 
		{
			PppPane pane = parent.getPane();
			Pathway result = hmdbPpp.doSuggestion(elt.getPathwayElement());
			pane.addPart("Hoi", result);
		}
	}

	/**
	 * callback, is called when user clicked with RMB on a pathway element.
	 */
	public void pathwayElementMenuHook(VPathwayElement e, JPopupMenu menu) 
	{
		if (e instanceof GeneProduct)
		{
			hmdbPppAction.setElement ((GeneProduct)e);
			menu.add(hmdbPppAction);	
		}
	}
}

