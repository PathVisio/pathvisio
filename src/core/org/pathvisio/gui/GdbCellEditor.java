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
package org.pathvisio.gui;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import org.pathvisio.util.SuggestCellEditor;
import org.pathvisio.util.SuggestCombo;
import org.pathvisio.util.SuggestCombo.SuggestionListener;
import org.pathvisio.util.SuggestCombo.SuggestionProvider;
import org.pathvisio.data.DataSources;
import org.pathvisio.data.Gdb;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.gui.PropertyPanel.AutoFillData;

public class GdbCellEditor extends SuggestCellEditor implements SuggestionProvider, SuggestionListener {
	public static final int TYPE_IDENTIFIER = 0;
	public static final int TYPE_SYMBOL = 1;
	int type;
	
	public static final int NO_LIMIT = 0;
	public static final int NO_TIMEOUT = 0;
	public static int query_timeout = 5; //seconds
	
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
	
	protected void setFocusListeners() {
        suggestCombo.getControl().addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	if(!suggestCombo.isSuggestFocus() &&
            		(button != null && !button.isFocusControl())) { //Also check focus on button
            		GdbCellEditor.this.focusLost();
            	}
            }
        });
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
    	adf.getProperty(PropertyType.SYSTEMCODE) + ")";
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
		suggestCombo.setText(suggestion);
		fireApplyEditorValue();
	}
	
	protected Object doGetValue() {
		String text = suggestCombo.getText();
		AutoFillData suggestion = suggested.get(text);
		if(suggestion == null) {
			suggested.put(text, suggestion = new GdbAutoFillData(getMainPropertyType(), text));
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
	
	public String[] getSuggestions(String text, SuggestCombo suggestCombo) {
		int limit = getLimit();
		
		List<String> sugg = new ArrayList<String>();
		try {
			Statement s = Gdb.getCon().createStatement();
			
			s.setQueryTimeout(query_timeout);
			if(limit > NO_LIMIT) s.setMaxRows(limit);
			
			String query = "";
			switch(type) {
			case TYPE_IDENTIFIER:
				query =
						"SELECT id, code FROM gene WHERE " +
						"id LIKE '" + text + "%'";
				break;
			case TYPE_SYMBOL:
			default:
				query =
						"SELECT id, code, backpageText FROM gene WHERE " +
						"backpageText LIKE '%<TH>Gene Name:<TH>" + text + "%'";
			}
			
			ResultSet r = s.executeQuery(query);
	
			while(r.next()) {
				String sysCode = r.getString("code");
				String sysName = DataSources.sysCode2Name.get(sysCode);
				
				AutoFillData adf = null;
				switch(type) {
				case TYPE_IDENTIFIER:
					adf = new GdbAutoFillData(PropertyType.GENEID, r.getString("id"));
					adf.setProperty(PropertyType.SYSTEMCODE, sysName);
					break;
				case TYPE_SYMBOL:
				default:
					String symbol = Gdb.parseGeneSymbol(r.getString("backpageText"));
					adf = new GdbAutoFillData(PropertyType.TEXTLABEL, symbol);
					adf.setProperty(PropertyType.SYSTEMCODE, sysName);
					adf.setProperty(PropertyType.GENEID, r.getString("id"));
					
				}
				
				String label = getLabel(adf);
				suggested.put(label, adf);
				sugg.add(label);
			}
		} catch (SQLException e) {
			Engine.log.error("Unable to query suggestions", e);
		}
		if(limit > NO_LIMIT && sugg.size() == limit) sugg.add("...results limited to " + limit);
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
			String sysName = getProperty(PropertyType.SYSTEMCODE);
			
			//If null, fetch from dataobject
			if(id == null) id = (String)o.getProperty(PropertyType.GENEID);
			if(sysName == null) sysName = (String)o.getProperty(PropertyType.SYSTEMCODE);
			
			String code = sysName == null ? null : DataSources.sysName2Code.get(sysName);
			
			//Guess symbol
			if(id != null && code != null) {
				String symbol = Gdb.getGeneSymbol(id, code);
				if(symbol != null) {
					setProperty(PropertyType.TEXTLABEL, symbol);
				}
			}
		}
	}

}
