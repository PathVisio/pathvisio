package gmmlVision.sidepanels;

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
		TabItem ti = new TabItem(tabFolder, SWT.NULL);
		ti.setText(title);
		ti.setControl(content);
	}
	
}
