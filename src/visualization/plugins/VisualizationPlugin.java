package visualization.plugins;

import graphics.GmmlGraphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Element;

import visualization.Visualization;
import visualization.VisualizationManager;
import visualization.VisualizationManager.VisualizationEvent;

public abstract class VisualizationPlugin implements Comparable {
	public static String XML_ELEMENT = "plugin";
	public static String XML_ATTR_CLASS = "class";
	
	private String[] rep_names = new String[] {"Drawing object", "Side panel", "Tooltip"};
	protected static final int SIDEPANEL = 2;
	protected static final int TOOLTIP = 4;
	protected static final int DRAWING = 8;
	
	private int DISPLAY_OPT; //Which representations possible (SIDEPANEL | TOOLTIP | DRAWING)
	private boolean CONFIGURABLE; //Configurable (if true, override createConfigComposite)
	private boolean GENERIC; //For generic use, or expression dataset specific
	private boolean USE_RESERVED_REGION; //Does this plugin use reserved region in GmmlGraphicsObject
	
	private boolean isActive;
		
	private Visualization visualization;
	
	public VisualizationPlugin(Visualization v) {
		visualization = v;
	}
	
	protected Visualization getVisualization() { return visualization; }
	
	public abstract String getName();
	public abstract String getDescription();
	
	public abstract void draw(GmmlGraphics g, PaintEvent e, GC buffer);
	public abstract void updateSidePanel(Collection<GmmlGraphics> objects);
	public abstract Composite getToolTipComposite(Composite parent, GmmlGraphics g);
	
	public abstract void createSidePanelComposite(Composite parent);
			
	protected Composite createConfigComposite(Composite parent) {
		return new Composite(parent, SWT.NULL); //Empty composite
	}
	
	public final void openConfigDialog(Shell shell) {
		if(!CONFIGURABLE) return; //Not configurable, so don't open config dialog
		ApplicationWindow d = new ConfigurationDialog(shell);
		d.open();
	}
	
	public String[] getRepresentations() {
		List<String> reps = new ArrayList<String>();
		if(canDrawingObject()) 	reps.add(rep_names[0]);
		if(canSidePanel()) 		reps.add(rep_names[1]);
		if(canToolTip()) 	reps.add(rep_names[2]);
		return reps.toArray(new String[reps.size()]);
	}
	
	public Element toXML() {
		Element elm = new Element(XML_ELEMENT);
		elm.setAttribute(XML_ATTR_CLASS, getClass().getCanonicalName());
		return elm;
	}
	
	public void loadXML(Element xml) { }
		
	public final boolean isActive() { return isActive; }
	public final void setActive(boolean active) { 
		if(isActive != active) {
			isActive = active;
			fireModifiedEvent();
		}	
	}
	
	public final boolean canSidePanel() { return (DISPLAY_OPT & SIDEPANEL) != 0; }
	public final boolean canToolTip() { return (DISPLAY_OPT & TOOLTIP) != 0; }
	public final boolean canDrawingObject() { return (DISPLAY_OPT & DRAWING) != 0; }
	
	/**
	 * Specify where this plugin can be displayed.
	 * One of:<BR><UL>
	 * <LI><CODE>DRAWING</CODE>: this plugin implements visualization on drawing objects
	 * <LI><CODE>TOOLTIP</CODE>: this plugins implements visualization in the tooltip showed
	 * when hovering over GeneProducts
	 * <LI><CODE>SIDEPANEL</CODE>: this plugin implements visualization to be displayed in the side panel
	 * </UL><BR>
	 * When multiple visualization options are implemented, 
	 * use bitwise OR (e.g. <CODE>SIDEPANEL | DRAWING</CODE>)
	 * @param options
	 */
	protected void setDisplayOptions(int options) {
		DISPLAY_OPT = options;
	}
	
	/**
	 * Specify if this plugin uses the area provided by its parent {@link Visualization}.
	 * @param use	true if this plugin uses the provided area, false if not
	 */
	protected void setUseProvidedArea(boolean use) {
		USE_RESERVED_REGION = use;
	}
	
	protected void setIsConfigurable(boolean configurable) {
		CONFIGURABLE = configurable;
	}
	
	protected void setIsGeneric(boolean generic) {
		GENERIC = generic;
	}
	
	public final boolean isGeneric() { return GENERIC; }
	public final boolean isConfigurable() { return CONFIGURABLE; }
	public final boolean isUseReservedRegion() { 
		return USE_RESERVED_REGION; 
	}
				
	public final void fireModifiedEvent() {
		VisualizationManager.fireVisualizationEvent(
				new VisualizationEvent(this, VisualizationEvent.PLUGIN_MODIFIED));
	}
	
	private class ConfigurationDialog extends ApplicationWindow {
		public ConfigurationDialog(Shell shell) {
			super(shell);
			setBlockOnOpen(true);
		}
		
		public Control createContents(Composite parent) {
			Composite contents = new Composite(parent, SWT.NULL);
			contents.setLayout(new GridLayout());
			
			Composite config = createConfigComposite(contents);
			config.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Composite buttonComp = createButtonComposite(contents);
			buttonComp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			
			return contents;
		}
		
		public Composite createButtonComposite(Composite parent) {
			Composite comp = new Composite(parent, SWT.NULL);
			comp.setLayout(new GridLayout(2, false));
			
			Button ok = new Button(comp, SWT.PUSH);
			ok.setText(" Ok ");
			ok.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					close();
				}
			});
			
			return comp;
		}
	}
	
	public int compareTo(Object o) {
		if(o instanceof VisualizationPlugin)
			return getName().compareTo(((VisualizationPlugin)o).getName());
		return -1;
	}
}
