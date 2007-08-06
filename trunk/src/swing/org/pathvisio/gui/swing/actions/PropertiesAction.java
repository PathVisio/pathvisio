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
package org.pathvisio.gui.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.pathvisio.gui.swing.dialogs.DataNodeDialog;
import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.VPathwayElement;

public class PropertiesAction extends AbstractAction {
	VPathwayElement element;
	
	public PropertiesAction(VPathwayElement e) {
		super("properties");
		putValue(AbstractAction.SHORT_DESCRIPTION, "View this element's properties");
		element = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(element instanceof Graphics) {
			PathwayElement p = ((Graphics)element).getGmmlData();
			PathwayElementDialog pd = null;
			switch(p.getObjectType()) {
			case ObjectType.DATANODE:
				pd = new DataNodeDialog(p, null, null);
			}
			if(pd != null) pd.setVisible(true);
		}
	}

}
