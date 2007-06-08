// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * This class represents a side panel with contents placed in different
 * tabitems. Components to add needs to be children of the {@link TabFolder}
 * returned by {@link #getTabFolder()}
 */
public class TabbedSidePanel extends SidePanel {
	private CTabFolder tabFolder;
	HashMap<String, Control> controls;
	HashMap<String, CTabItem> tabItems;
	
	/**
	 * Returns the {@link TabFolder} containing the different
	 * tabItems of this sidepanel
	 */
	public CTabFolder getTabFolder() { return tabFolder; }
	
	public HashMap<String, CTabItem> getTabItemHash() { return tabItems; }
	
	/**
	 * Constructor for this class
	 * @param parent	The parent composite (needs to be an {@link SashForm} for the
	 * minimize button to work
	 * @param style		
	 */
	public TabbedSidePanel(Composite parent, int style) {
		super(parent, style);
		controls = new HashMap<String, Control>();
		tabItems = new HashMap<String, CTabItem>();
		
		tabFolder = new CTabFolder(getContentComposite(), SWT.BORDER);
		tabFolder.setSimple(false);
	}
	
	/**
	 * Add a TabItem containing the given Control.
	 * @param content	{@link Control} that needs to be a child of the
	 * TabFolder returned by {@link #getTabFolder()}
	 * @param title		The title for the TabItem
	 */
	public void addTab(Control content, String title)
	{		
		createTabItem(content, title, false);
		controls.put(title, content);
	}
	
	public void addTab(Control content, String title, boolean close)
	{		
		createTabItem(content, title, close);
		controls.put(title, content);
	}
	
	/**
	 * Creates an {@link TabItem} with the given control and title
	 * @param content
	 * @param title
	 * @param close
	 * @returns
	 */
	private CTabItem createTabItem(Control content, String title, boolean close)
	{
		return createTabItem(content, title, tabFolder.getItemCount(), close);
	}
	
	/**
	 * Creates an {@link TabItem} with the given control and title at the given index.
	 * If the index is lower than the number of tabitems, the new tabitem is placed before the first,
	 * otherwise it is placed after the last.
	 * @param content
	 * @param title
	 * @returns
	 * @param index
	 * @return
	 */
	private CTabItem createTabItem(Control content, String title, int index, boolean close) {
		int nrTabs = tabFolder.getItemCount();
		
		if(index > nrTabs) index = nrTabs; //If index is invalid, choose first or last tab
		else if(index < 0) index = 0;
		
		final CTabItem ti = new CTabItem(tabFolder, close ? SWT.CLOSE : SWT.NULL, index);
		ti.setText(title);
		ti.setControl(content);
		ti.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				tabItems.remove(ti.getText());
			}
		});
		tabItems.put(title, ti);
		return ti;
	}
	
	/**
	 * Hides a tab (without disposing the containing {@link Control})
	 * @param title The title of the tab
	 */
	public void hideTab(String title) {
		if(!tabItems.containsKey(title)) return;
		tabItems.get(title).dispose();
		tabItems.remove(title);
	}
	
	/**
	 * Shows a tab if a control with the given title exists
	 * @param title	The title of the tabitem, also serves to find the {@link Control} for the tabItem
	 * @return true if the tab is added, false if not ({@link Control} not found)
	 */
	public boolean unhideTab(String title) {
		return unhideTab(title, tabFolder.getItemCount());
	}
	
	/**
	 * Shows a tab if a control with the given title exists
	 * @param title	The title of the tabitem, also serves to find the {@link Control} for the tabItem
	 * @param position The index of the position to add the tab
	 * @return true if the tab is added, false if not ({@link Control} not found)
	 */
	public boolean unhideTab(String title, int position) {
		if(controls.containsKey(title)) {
			createTabItem(controls.get(title), title, position, false);
			return true;
		}
		return false;
	}
	
	public void selectTab(String title) {
		if(tabItems.containsKey(title)) 
			tabFolder.setSelection(tabItems.get(title));
	}
	
	/**
	 * Checks whether a tabitem is visible
	 */
	public boolean isVisible(String title) {
		return tabItems.containsKey(title) && controls.containsKey(title);
	}
	
	/**
	 * Check whether a tabitem with the given title exists
	 * @param title
	 */
	public boolean hasTab(String title) {
		return tabItems.containsKey(title);
	}
}
