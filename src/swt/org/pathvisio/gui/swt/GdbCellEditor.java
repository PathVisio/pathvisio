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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.pathvisio.data.Gdb;
import org.pathvisio.gui.swt.PropertyPanel.AutoFillData;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.Xref;
import org.pathvisio.util.swt.SuggestCellEditor;
import org.pathvisio.util.swt.SuggestCombo;
import org.pathvisio.util.swt.SuggestCombo.SuggestionListener;
import org.pathvisio.util.swt.SuggestCombo.SuggestionProvider;

public class GdbCellEditor extends SuggestCellEditor implements SuggestionProvider, SuggestionListener {
	public static final int TYPE_IDENTIFIER = 0;
	public static final int TYPE_SYMBOL = 1;
	int type;
	
	
	HashMap<String, PropertyPanel.AutoFillData> suggested;
	
	Button button;
	
	GdbCellEditor(Composite parent, int type) {
		super();
		this.type = type;
		create(parent); //Set type before creating contol
		suggestCombo.addSuggetsionListener(this);
		suggested = new HashMap<String, PropertyPanel.AutoFillData>();
	}
		
	protected Control createControl(Composite parent) {
		if(type == TYPE_IDENTIFIER) {
			Composite comp = new Composite(parent, SWT.NULL);
			super.createControl(comp);
			button = new Button(comp, SWT.PUSH);
			button.setText("Set Label");
			button.setToolTipText("Set the Label property by looking up the gene symbol in the synonym database");
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Object value = doGetValue();
					if(value instanceof AutoFillData) {
						AutoFillData afd = (AutoFillData) value;
						afd.setDoGuessData(true);
						fireApplyEditorValue();
						afd.setDoGuessData(false);
					}
				}
			});
			comp.setLayout(new CellLayout());
			return comp;
		} else {
			return super.createControl(parent);
		}
	}
	
	protected boolean isFocusLost() {
		return super.isFocusLost() &&
				(button != null && !button.isFocusControl());
	}
	
    private class CellLayout extends Layout {
    	//Adapted from DialogCellEditor
        public void layout(Composite editor, boolean force) {
            Rectangle bounds = editor.getClientArea();
            Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			suggestCombo.setBounds(0, 0, bounds.width - size.x, bounds.height);
            button.setBounds(bounds.width - size.x, 0, size.x, bounds.height);
        }

        public Point computeSize(Composite editor, int wHint, int hHint,
                boolean force) {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
            Point contentsSize = suggestCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    force);
            Point buttonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT,
            		force);
            Point result = new Point(buttonSize.x, Math.max(contentsSize.y,
            		buttonSize.y));
            return result;
        }
    }

    public String getLabel(AutoFillData adf) {
    	String iddb = adf.getProperty(PropertyType.GENEID) + " (" +
    	adf.getProperty(PropertyType.DATASOURCE) + ")";
    	switch(type) {
    	case TYPE_IDENTIFIER:
    		return 	iddb;
    	case TYPE_SYMBOL:
    	default:
    		return adf.getProperty(PropertyType.TEXTLABEL) + ": " + iddb;
    	}
				
	}
	    
	public SuggestionProvider getSuggestionProvider() {
		return this;
	}

	public void suggestionSelected(String suggestion) {
	}
	
	protected Object doGetValue() {
		String text = suggestCombo.getText();
		AutoFillData suggestion = suggested.get(text);
		if(suggestion == null) {
			suggestion = new GdbAutoFillData(getMainPropertyType(), text);
		}
		return suggestion;
	}
	
	protected PropertyType getMainPropertyType() {
		switch(type) {
		case TYPE_IDENTIFIER:
			return PropertyType.GENEID;
		case TYPE_SYMBOL:
		default:
			return PropertyType.TEXTLABEL;
		}
	}
	
	public String[] getSuggestions(String text, SuggestCombo suggestCombo) 
	{
		int limit = getLimit();
		
		List<Map<PropertyType, String>> data = new ArrayList<Map<PropertyType, String>>();
		List<String> sugg = new ArrayList<String>();

		Gdb gdb = SwtEngine.getCurrent().getGdbManager().getCurrentGdb();
		switch(type) {
		case TYPE_IDENTIFIER:
			List<Xref> refs = gdb.getIdSuggestions (text, limit);
			for(Xref r : refs) {
				Map<PropertyType, String> item = new HashMap<PropertyType, String>();
				item.put (PropertyType.GENEID, r.getId());
				item.put (PropertyType.DATASOURCE, r.getDatabaseName());
				data.add (item);
			}
			break;
		case TYPE_SYMBOL:
		default:
			List<String> symbols = gdb.getSymbolSuggestions (text, limit);
			for(String s : symbols) {
				List<Xref> xrefs = gdb.getCrossRefsByAttribute("Symbol", s);	
				for(Xref r : xrefs) {
					Map<PropertyType, String> item = new HashMap<PropertyType, String>();
					item.put (PropertyType.GENEID, r.getId());
					item.put (PropertyType.DATASOURCE, r.getDatabaseName());
					data.add (item);
				}
			}
			break;
		}
		
		// copy the data returned from getSymbolSuggestions or getIdSuggestions
		// into an AutoFillData list expected by the cell editor.
		for (Map<PropertyType, String> item : data)
		{
			AutoFillData adf = null;
			switch(type) {
			case TYPE_IDENTIFIER:
				adf = new GdbAutoFillData(PropertyType.GENEID, item.get(PropertyType.GENEID));
				adf.setProperty(PropertyType.DATASOURCE, item.get(PropertyType.DATASOURCE));
				break;
			case TYPE_SYMBOL:
			default:
				adf = new GdbAutoFillData(PropertyType.TEXTLABEL, item.get(PropertyType.TEXTLABEL));
				adf.setProperty(PropertyType.DATASOURCE, item.get(PropertyType.DATASOURCE));
				adf.setProperty(PropertyType.GENEID, item.get(PropertyType.GENEID));
			}
			String label = getLabel(adf);
			suggested.put(label, adf);
			sugg.add(label);
		}
			
		return sugg.toArray(new String[sugg.size()]);
	}

	int getLimit() {
		switch(type) {
		case TYPE_IDENTIFIER:
			return 100;
		case TYPE_SYMBOL:
		default:
			return 100;
		}
	}
	
	class GdbAutoFillData extends AutoFillData {
		public GdbAutoFillData(PropertyType mainProperty, String mainValue) {
			super(mainProperty, mainValue);
		}
		
		protected void guessData(PathwayElement o) {
			//Fetch info from self
			String id = getProperty(PropertyType.GENEID);
			String sysName = getProperty(PropertyType.DATASOURCE);
			Xref ref = new Xref (id, DataSource.getBySystemCode(sysName));
			//If null, fetch from dataobject
			if(id == null) id = (String)o.getProperty(PropertyType.GENEID);
			if(sysName == null) sysName = (String)o.getProperty(PropertyType.DATASOURCE);
			
			String code = sysName == null ? null : DataSource.getByFullName(sysName).getSystemCode();
			
			//Guess symbol
			if(id != null && code != null) {
				String symbol = SwtEngine.getCurrent().getGdbManager().getCurrentGdb().getGeneSymbol(ref);
				if(symbol != null) {
					setProperty(PropertyType.TEXTLABEL, symbol);
				}
			}
		}
	}

}
