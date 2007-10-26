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
package org.pathvisio.visualization.colorset;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

abstract class ConfigComposite extends Composite 
{
	final int colorLabelSize = 15;
	ColorSetObject input;
	Text nameText;
	
	public ConfigComposite(Composite parent, int style) {
		super(parent, style);
		createContents();
	}
	
	public void setInput(ColorSetObject input) {
		this.input = input;
		refresh();
	}
	
	public boolean save() {
		return true;
	}
	
	void refresh() {
		String nm = "";
		if(input != null) nm = input.getName();
		nameText.setText(nm);
	}
			
	void changeName(String name) {
		input.setName(name);
	}
	
	abstract void createContents();
	
	protected Composite createNameComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout(2, false));
		
		Label nameLabel = new Label(comp, SWT.CENTER);
		nameLabel.setText("Name:");
	
		nameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    nameText.addModifyListener(new ModifyListener() {
	    	public void modifyText(ModifyEvent e) {
	    		changeName(nameText.getText());
	    	}
	    });
	    return comp;
	}
}
