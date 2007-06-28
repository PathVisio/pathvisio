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

package org.pathvisio.gui.swt;

import java.net.URL;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.pathvisio.gui.swt.MainWindow;
import org.pathvisio.view.VPathway;

/**
 * {@link Action} to add a new element to the gpml pathway
 */
public class NewElementAction extends Action
{
	MainWindow window;
	int element;
		
	/**
	 * Constructor for this class
	 * @param e	type of element this action adds; a {@link VPathway} field constant
	 */
	public NewElementAction (int e)
	{
		// TODO: this should be moved to CommonActions, since it is both in v1 and v2
		element = e;
		
		String toolTipText;
		URL imageURL = null;
		toolTipText = null;
		switch(element) {
		case VPathway.NEWLINE: 
			toolTipText = "Draw new line";
			imageURL = Engine.getResourceURL("icons/newline.gif");
			setChecked(false);
			break;
		case VPathway.NEWLINEARROW:
			toolTipText = "Draw new arrow";
			imageURL = Engine.getResourceURL("icons/newarrow.gif");
			setChecked(false);
			break;
		case VPathway.NEWLINEDASHED:
			toolTipText = "Draw new dashed line";
			imageURL = Engine.getResourceURL("icons/newdashedline.gif");
			setChecked(false);
			break;
		case VPathway.NEWLINEDASHEDARROW:
			toolTipText = "Draw new dashed arrow";
			imageURL = Engine.getResourceURL("icons/newdashedarrow.gif");
			setChecked(false);
			break;
		case VPathway.NEWLABEL:
			toolTipText = "Draw new label";
			imageURL = Engine.getResourceURL("icons/newlabel.gif");
			setChecked(false);
			break;
		case VPathway.NEWARC:
			toolTipText = "Draw new arc";
			imageURL = Engine.getResourceURL("icons/newarc.gif");
			setChecked(false);
			break;
		case VPathway.NEWBRACE:
			toolTipText = "Draw new brace";
			imageURL = Engine.getResourceURL("icons/newbrace.gif");
			setChecked(false);
			break;
		case VPathway.NEWGENEPRODUCT:
			toolTipText = "Draw new geneproduct";
			imageURL = Engine.getResourceURL("icons/newgeneproduct.gif");
			setChecked(false);
			break;
		case VPathway.NEWRECTANGLE:
			imageURL = Engine.getResourceURL("icons/newrectangle.gif");
			setChecked(false);
			break;
		case VPathway.NEWOVAL:
			toolTipText = "Draw new oval";
			imageURL = Engine.getResourceURL("icons/newoval.gif");
			setChecked(false);
			break;
		case VPathway.NEWTBAR:
			toolTipText = "Draw new TBar";
			imageURL = Engine.getResourceURL("icons/newtbar.gif");
			setChecked(false);
			break;
		case VPathway.NEWRECEPTORROUND:
			toolTipText = "Draw new round receptor";
			imageURL = Engine.getResourceURL("icons/newreceptorround.gif");
			setChecked(false);
			break;
		case VPathway.NEWRECEPTORSQUARE:
			toolTipText = "Draw new square receptor";
			imageURL = Engine.getResourceURL("icons/newreceptorsquare.gif");
			setChecked(false);
			break;
		case VPathway.NEWLIGANDROUND:
			toolTipText = "Draw new round ligand";
			imageURL = Engine.getResourceURL("icons/newligandround.gif");
			setChecked(false);
			break;
		case VPathway.NEWLIGANDSQUARE:
			toolTipText = "Draw new square ligand";
			imageURL = Engine.getResourceURL("icons/newligandsquare.gif");
			setChecked(false);
			break;
		case VPathway.NEWLINEMENU:
			setMenuCreator(new NewItemMenuCreator(VPathway.NEWLINEMENU));
			imageURL = Engine.getResourceURL("icons/newlinemenu.gif");
			toolTipText = "Draw new line or arrow";
			break;
		case VPathway.NEWLINESHAPEMENU:
			setMenuCreator(new NewItemMenuCreator(VPathway.NEWLINESHAPEMENU));
			imageURL = Engine.getResourceURL("icons/newlineshapemenu.gif");
			toolTipText = "Draw new ligand or receptor";
			break;
		}
		setToolTipText(toolTipText);
		setId("newItemAction");
		if(imageURL != null) setImageDescriptor(ImageDescriptor.createFromURL(imageURL));
	}
				
	public void run () {
		if(isChecked())
		{
			Engine.getWindow().deselectNewItemActions();
			setChecked(true);
			Engine.getVPathway().setNewGraphics(element);
		}
		else
		{	
			Engine.getVPathway().setNewGraphics(VPathway.NEWNONE);
		}
	}

	/**
	 * {@link IMenuCreator} that creates the drop down menus for 
	 * adding new line-type and -shape elements
	 */
	private class NewItemMenuCreator implements IMenuCreator
	{
		private Menu menu;
		int element;
		
		/**
		 * Constructor for this class
		 * @param e	type of menu to create; one of {@link VPathway}.NEWLINEMENU
		 * , {@link VPathway}.NEWLINESHAPEMENU
		 */
		public NewItemMenuCreator(int e) 
		{
			element = e;
		}
		
		public Menu getMenu(Menu parent)
		{
			return null;
		}

		public Menu getMenu(Control parent)
		{
			if (menu != null)
				menu.dispose();
			
			menu = new Menu(parent);
			Vector<Action> actions = new Vector<Action>();
			switch(element)
			{
			case VPathway.NEWLINEMENU:
				actions.add(new NewElementAction(VPathway.NEWLINE));
				actions.add(new NewElementAction(VPathway.NEWLINEARROW));
				actions.add(new NewElementAction(VPathway.NEWLINEDASHED));
				actions.add(new NewElementAction(VPathway.NEWLINEDASHEDARROW));
				break;
			case VPathway.NEWLINESHAPEMENU:
				actions.add(new NewElementAction(VPathway.NEWLIGANDROUND));
				actions.add(new NewElementAction(VPathway.NEWRECEPTORROUND));
				actions.add(new NewElementAction(VPathway.NEWLIGANDSQUARE));
				actions.add(new NewElementAction(VPathway.NEWRECEPTORSQUARE));
			}
			
			for (Action act : actions)
			{			
				addActionToMenu(menu, act);
			}

			return menu;
		}
		
		protected void addActionToMenu(Menu parent, Action a)
		{
			ActionContributionItem item = new ActionContributionItem(a);
			item.fill(parent, -1);
		}
		
		public void dispose() 
		{
			if (menu != null)  {
				menu.dispose();
				menu = null;
			}
		}
	}

}
