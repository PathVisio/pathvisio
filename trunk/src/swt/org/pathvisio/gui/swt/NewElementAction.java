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
import org.pathvisio.Engine;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.view.DefaultTemplates;
import org.pathvisio.view.Template;

/**
 * {@link Action} to add a new element to the gpml pathway
 */
public class NewElementAction extends Action
{
	public static final int MENULINE = 1;
	public static final int MENULINESHAPE = 2;
	
	MainWindow window;
		
	Template template;

	/**
	 * Constructor for parent menu's
	 * @param menu The menu type (one of the MENU* constants)
	 */
	public NewElementAction(int menu) {
		setMenuCreator(new NewItemMenuCreator(menu));
		String tooltip = "";
		URL imageURL = null;
		switch(menu) {
		case MENULINE:
			tooltip = "Draw new line or arrow";
			imageURL = Engine.getCurrent().getResourceURL("icons/newlinemenu.gif");
			break;
		case MENULINESHAPE:
			tooltip = "Draw new ligand or receptor";
			imageURL = Engine.getCurrent().getResourceURL("icons/newlineshapemenu.gif");
		}
		setChecked(false);
		setToolTipText(tooltip);
		setId("newItemAction");
		if(imageURL != null) setImageDescriptor(ImageDescriptor.createFromURL(imageURL));
	}
	
	/**
	 * Constructor for this class
	 * @param template The template to use for drawing the new object
	 */
	public NewElementAction(Template template) {
		this.template = template;
		setChecked(false);
		setToolTipText(template.getDescription());
		setId("newItemAction");
		URL imageURL = template.getIconLocation();
		if(imageURL != null) setImageDescriptor(ImageDescriptor.createFromURL(imageURL));
	}
				
	public void run () {
		if(isChecked())
		{
			SwtEngine.getCurrent().getWindow().deselectNewItemActions();
			setChecked(true);
			Engine.getCurrent().getActiveVPathway().setNewTemplate(template);
		}
		else
		{	
			Engine.getCurrent().getActiveVPathway().setNewTemplate(null);
		}
	}

	/**
	 * {@link IMenuCreator} that creates the drop down menus for 
	 * adding new line-type and -shape elements
	 */
	private class NewItemMenuCreator implements IMenuCreator
	{
		private Menu menu;
		int type;
		
		/**
		 * Constructor for this class
		 * @param e	type of menu to create; one of the MENU* constants
		 */
		public NewItemMenuCreator(int e) 
		{
			type = e;
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
			switch(type)
			{
			case MENULINE:
				actions.add(new NewElementAction(new DefaultTemplates.LineTemplate(
						LineStyle.SOLID, LineType.LINE, LineType.LINE)
				));
				actions.add(new NewElementAction(new DefaultTemplates.LineTemplate(
						LineStyle.SOLID, LineType.LINE, LineType.ARROW)
				));
				actions.add(new NewElementAction(new DefaultTemplates.LineTemplate(
						LineStyle.DASHED, LineType.LINE, LineType.LINE)
				));
				actions.add(new NewElementAction(new DefaultTemplates.LineTemplate(
						LineStyle.DASHED, LineType.LINE, LineType.ARROW)
				));
				break;
			case MENULINESHAPE:
				actions.add(new NewElementAction(new DefaultTemplates.LineTemplate(
						LineStyle.SOLID, LineType.LINE, LineType.LIGAND_ROUND)
				));
				actions.add(new NewElementAction(new DefaultTemplates.LineTemplate(
						LineStyle.SOLID, LineType.LINE, LineType.RECEPTOR_ROUND)
				));
				actions.add(new NewElementAction(new DefaultTemplates.LineTemplate(
						LineStyle.SOLID, LineType.LINE, LineType.LIGAND_SQUARE)
				));
				actions.add(new NewElementAction(new DefaultTemplates.LineTemplate(
						LineStyle.SOLID, LineType.LINE, LineType.RECEPTOR_SQUARE)
				));
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
