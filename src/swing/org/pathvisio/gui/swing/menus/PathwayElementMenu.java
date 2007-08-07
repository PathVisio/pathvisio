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
package org.pathvisio.gui.swing.menus;

import java.util.Collection;
import java.util.Collections;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import org.pathvisio.gui.swing.actions.PropertiesAction;
import org.pathvisio.view.VPathwayElement;

public class PathwayElementMenu extends JPopupMenu {		
	VPathwayElement element;
	
	public PathwayElementMenu(VPathwayElement e) {
		this(e, new Action[] { new PropertiesAction(e) });
	}
	
	public PathwayElementMenu(VPathwayElement e, Action[] actions) {
		element = e;
		for(Action a : actions) {
			if(a != null) {
				add(a);
			} else {
				addSeparator();
			}
		}
	}
	
	public PathwayElementMenu(VPathwayElement e, Collection<Action> actions) {
		this(e, actions.toArray(new Action[actions.size()]));
	}
}
