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
package org.pathvisio.gui.swt.awt;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.eclipse.swt.widgets.Composite;

public class VPathwaySwingComposite extends EmbeddedSwingComposite {
	JScrollPane scrollPane;
	
	public VPathwaySwingComposite(Composite parent, int style) {
		super(parent, style);
		populate();
	}
	
	protected JComponent createSwingComponent() {
		scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);
		
		//increase the scrollspeed when scrolling with wheel
		int incr = 30;
		scrollPane.getVerticalScrollBar().setUnitIncrement(incr);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(incr);
		
		return scrollPane;
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
}
