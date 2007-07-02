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
package org.pathvisio.util.swt;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.pathvisio.util.swt.SuggestCombo.SuggestionProvider;

public abstract class SuggestCellEditor extends CellEditor {
	protected SuggestCombo suggestCombo;
	
	public SuggestCellEditor() {
		super();
	}
	
	public SuggestCellEditor(Composite parent) {
		super(parent);
	}
		
	protected Control createControl(Composite parent) {
		suggestCombo = new SuggestCombo(parent, getSuggestionProvider());

		setKeyListeners();
		setFocusListeners();

		return suggestCombo;
	}

	protected void setKeyListeners() {
		suggestCombo.getControl().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);
            }
		});
	}
	
	protected void setFocusListeners() {
        suggestCombo.getControl().addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	if(!suggestCombo.isSuggestFocus()) {
            		SuggestCellEditor.this.focusLost();
            	}
            }
        });
	}
	public abstract SuggestionProvider getSuggestionProvider();
		
	protected Object doGetValue() {
		return suggestCombo.getText();
	}
	
	protected void doSetValue(Object value) {
		suggestCombo.setText(value == null ? "" : value.toString());
	}

	protected void doSetFocus() {}


}
