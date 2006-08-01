package gmmlVision.sidepanels;

import java.util.HashMap;

import gmmlVision.GmmlVision;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
	private TabFolder tabFolder;
	HashMap<String, Control> controls;
	HashMap<String, TabItem> tabItems;
	
	/**
	 * Returns the {@link TabFolder} containing the different
	 * tabItems of this sidepanel
	 * @return
	 */
	public TabFolder getTabFolder() { return tabFolder; }
	
	/**
	 * Constructor for this class
	 * @param parent	The parent composite (needs to be an {@link SashForm} for the
	 * minimize button to work
	 * @param style	
	 * @param gmmlVision	
	 */
	public TabbedSidePanel(Composite parent, int style, GmmlVision gmmlVision) {
		super(parent, style, gmmlVision);
		controls = new HashMap<String, Control>();
		tabItems = new HashMap<String, TabItem>();
		
		tabFolder = new TabFolder(getContentComposite(), SWT.NULL);
	}
	
	/**
	 * Add a TabItem containing the given Control.
	 * @param content	{@link Control} that needs to be a child of the
	 * TabFolder returned by {@link #getTabFolder()}
	 * @param title		The title for the TabItem
	 */
	public void addTab(Control content, String title)
	{		
		createTabItem(content, title);
		controls.put(title, content);
	}
	
	/**
	 * Creates an {@link TabItem} with the given control and title
	 * @param content
	 * @param title
	 * @returns
	 */
	private TabItem createTabItem(Control content, String title)
	{
		return createTabItem(content, title, tabFolder.getItemCount());
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
	private TabItem createTabItem(Control content, String title, int index) {
		int nrTabs = tabFolder.getItemCount();
		
		if(index > nrTabs) index = nrTabs; //If index is invalid, choose first or last tab
		else if(index < 0) index = 0;
		
		TabItem ti = new TabItem(tabFolder, SWT.NULL, index);
		ti.setText(title);
		ti.setControl(content);
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
	public boolean showTab(String title) {
		return showTab(title, tabFolder.getItemCount());
	}
	
	/**
	 * Shows a tab if a control with the given title exists
	 * @param title	The title of the tabitem, also serves to find the {@link Control} for the tabItem
	 * @param position The index of the position to add the tab
	 * @return true if the tab is added, false if not ({@link Control} not found)
	 */
	public boolean showTab(String title, int position) {
		if(controls.containsKey(title)) {
			createTabItem(controls.get(title), title, position);
			return true;
		}
		return false;
	}
	
	public void selectTab(String title) {
		if(tabItems.containsKey(title)) 
			tabFolder.setSelection(new TabItem[] {tabItems.get(title)});
	}
	
	/**
	 * Checks whether a tabitem is visible
	 */
	public boolean isVisible(String title) {
		return tabItems.containsKey(title) && controls.containsKey(title);
	}
}
