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
package org.pathvisio.visualization.plugins;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.Collection;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Element;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationManager.VisualizationEvent;

/**
 * The VisualizationPlugin class can be extended to create a visualization plugin for
 * the visualization of experimental data on GPML pathways
 * @author Thomas
 */
public abstract class VisualizationPlugin implements Comparable {
	public static String XML_ELEMENT = "plugin";
	public static String XML_ATTR_CLASS = "class";
	
	protected static final int SIDEPANEL = 2;
	protected static final int TOOLTIP = 4;
	protected static final int DRAWING = 8;
	
	private int DISPLAY_OPT; //Where on the display cann this plugin be used (SIDEPANEL | TOOLTIP | DRAWING)
	private boolean CONFIGURABLE; //Configurable (if true, override createConfigComposite)
	private boolean GENERIC; //For generic use, or expression dataset specific
	private boolean USE_PROVIDED_AREA; //Does this plugin use reserved region in GmmlGraphicsObject
	
	private boolean isActive;
		
	private Visualization visualization;
	
	/**
	 * Constructor for this class. Create an instance of this {@link VisualizationPlugin}
	 * @param v The {@link Visualization} the instance is part of
	 */
	public VisualizationPlugin(Visualization v) {
		visualization = v;
	}
	
	/**
	 * Get the {@link Visualization} this instance belongs to
	 * @return The {@link Visualization} this plugin belongs to
	 */
	protected final Visualization getVisualization() { return visualization; }
	
	/**
	 * Gets the name of this visualization plugin class
	 * @return the name of this visualization plugin class
	 */
	public abstract String getName();
	
	/**
	 * Gets the description of this visualization plugin class
	 * @return the description of this visualization plugin class
	 */
	public abstract String getDescription();
	
	//TODO: Update javadoc
	/**
	 * Create a visualization on the pathway drawing for the given {@link Graphics} object.
	 * This method will only be called when the plugin display options contains {@link VisualizationPlugin#DRAWING}.
	 * @param g	The {@link Graphics} object on which the visualization applies
	 * @param e	{@link PaintEvent} containing information about the paint
	 * @param gc Graphical context on which drawing operations can be performed
	 * @see <a href=http://www.eclipse.org/articles/Article-SWT-graphics/SWT_graphics.html>
	 * Introduction in SWT graphics</a>
	 */
	public abstract void visualizeOnDrawing(Graphics g, Graphics2D g2d);
	
	/**
	 * Create a visualization on the side panel for the given {@link Graphics} objects
	 * This method will only be called when the plugin display options contains {@link VisualizationPlugin#SIDEPANEL}.
	 * @param objects List of {@link Graphics} objects to create the visualization for
	 */
	public abstract void visualizeOnSidePanel(Collection<Graphics> objects);
	
//	/**
//	 * Create a visualization on the Tool Tip for the given {@link Graphics} object.
//	 * This method will only be called when the plugin display options contains {@link VisualizationPlugin#TOOLTIP}.
//	 * @param parent The parent of the {@link Composite} that will be displayed on the Tool Tip
//	 * @param g The {@link Graphics} object to create the visualization for
//	 * @return A {@link Composite} that will be displayed in the Tool Tip
//	 */
//	public abstract Composite visualizeOnToolTip(Composite parent, Graphics g);
	
	public abstract Component visualizeOnToolTip(Graphics g);
	
	/**
	 * Initialize a Composite for visualization on the side panel
	 * This method will only be called when the plugin display options contains {@link VisualizationPlugin#SIDEPANEL}.
	 * @param parent The parent of the new {@link Composite}
	 */
	public abstract void initSidePanel(Composite parent);
	
	/**
	 * Create a {@link Composite} that is displayed in the legend for every plug-in that is
	 * activated on the pathway drawing.
	 * This method may be overridden if the plug-in needs to show a legend item when it is
	 * active on the pathway drawing.
	 * @param parent The parent of the new {@link Composite} to return
	 * @return A {@link Composite} that displays the legend information for this plug-in
	 */
	public Composite createLegendComposite(Composite parent) {
		return null;
	}
	
	/**
	 * Create a {@link Composite} that can be used to configure this visualization plugin
	 * Override this method when the visualization plugin can be configured by the user
	 * @see  VisualizationPlugin#isConfigurable() VisualizationPlugin#openConfigDialog(Shell)
	 * @param parent The parent of the {@link Composite} to create
	 * @return A {@link Composite} that will be displayed when the user wants to configure the plugin
	 */
	protected Composite createConfigComposite(Composite parent) {
		return new Composite(parent, SWT.NULL); //Empty composite
	}
	
	/**
	 * Opens the configuration dialog (only when isConfigurable returnst true) that
	 * will display the {@link Composite} created in {@link VisualizationPlugin#createConfigComposite(Composite)}.
	 * @param shell The parent {@link Shell} of the dialog to open
	 */
	public final void openConfigDialog(Shell shell) {
		if(!CONFIGURABLE) return; //Not configurable, so don't open config dialog
		ApplicationWindow d = new ConfigurationDialog(shell);
		d.open();
	}
	
	
	/**
	 * Save the configuration of the current instance of this class to an XML element.<br>
	 * Override this method to save custom configuration settings:<br>
	 * <code>
	 * public Element toXML() {								<br>
	 * &nbsp;Element elm = super.toXML();					<br>
	 * &nbsp;//Add custom attributes and elements to elm, e.g.:	<br>
	 * &nbsp;elm.setAttribute("fontsize", fontsize)			<br>
	 * <br>
	 * &nbsp;return elm;									<br>
	 * }
	 * </code>
	 */
	public Element toXML() {
		Element elm = new Element(XML_ELEMENT);
		elm.setAttribute(XML_ATTR_CLASS, getClass().getCanonicalName());
		return elm;
	}
	
	/**
	 * Load the configuration of the current instance of this class from an XML element.<br>
	 * Override this method to load custom configuration settings:<br>
	 * <code>
	 * public Element loadXML(Element xml) {				<br>
	 * &nbsp;super.loadXML(xml)								<br>
	 * &nbsp;//Load custom attributes and elements to elm, e.g.:	<br>
	 * &nbsp;fontSize = xml.getAttributeValue("fontsize")			<br>
	 * }
	 * </code>
	 * @param xml The {@link Element} that contains the configuration for this plugin
	 */
	public void loadXML(Element xml) { }
	
	/**
	 * Returns whether the current instance of this visualization plugin is activated or not
	 * @return true if this instance is activated, false otherwise
	 */
	public final boolean isActive() { return isActive; }
	
	/**
	 * Set the activation state of this instance. If set to active, the visualization methods
	 * of this plugin will be called from the {@link Visualization} this instance belongs to.
	 * @param active true to activate this instance, false to de-activate
	 */
	public final void setActive(boolean active) { 
		if(isActive != active) {
			isActive = active;
			fireModifiedEvent();
		}	
	}
	
	/**
	 * Returns whether this plugin can be displayed in the side panel
	 * @return true when this plugin can be displayed in the side panel, false otherwise
	 */
	public final boolean canSidePanel() { return (DISPLAY_OPT & SIDEPANEL) != 0; }
	
	/**
	 * Returns whether this plugin can be displayed in the Tool Tip
	 * @return true when this plugin can be displayed in the Tool Tip, false otherwise
	 */
	public final boolean canToolTip() { return (DISPLAY_OPT & TOOLTIP) != 0; }
	
	/**
	 * Returns whether this plugin can be displayed in the pathway drawing
	 * @return true when this plugin can be displayed in the pathway drawing, false otherwise
	 */
	public final boolean canDrawing() { return (DISPLAY_OPT & DRAWING) != 0; }
	
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
	 * Specify whether the parent {@link Visualization} needs to provide an area on the 
	 * {@link Graphics} objects.
	 * When multiple visualization plugins apply visualizations on the same {@link Graphics}
	 * object, the available space will be divided over the plugins for which this method is called
	 * with true as argument.
	 * The provided area can be obtained by calling {@link Visualization#provideDrawArea(VisualizationPlugin, Graphics)}
	 * @param use	true if this plugin uses the provided area, false if not
	 * @see Visualization#provideDrawArea(VisualizationPlugin, Graphics)
	 */
	protected void setUseProvidedArea(boolean use) {
		USE_PROVIDED_AREA = use;
	}
	
	/**
	 * Specify whether this visualization plugin is configurable or not.
	 * When the plugin is set to be configurable, override {@link VisualizationPlugin#createConfigComposite(Composite)}.
	 * @param configurable
	 * @see VisualizationPlugin#createConfigComposite(Composite)
	 */
	protected void setIsConfigurable(boolean configurable) {
		CONFIGURABLE = configurable;
	}
	
	/**
	 * Specify whether this visualization plugin is generic or not.
	 * Generic plugins are independent of expression data. Non-generic plugin configurations
	 * depend on an expression dataset and will be loaded/saved together with this expression dataset.
	 * @param generic true if this plugin is generic, false otherwise
	 */
	protected void setIsGeneric(boolean generic) {
		GENERIC = generic;
	}
	
	/**
	 * Returns whether this visualiazation plugin is generic or not
	 * Generic plugins are independent of expression data. Non-generic plugin configurations
	 * depend on an expression dataset and will be loaded/saved together with this expression dataset.
	 * @see VisualizationPlugin#setIsGeneric(boolean)
	 * @return true if this plugin is generic, false otherwise
	 */
	public final boolean isGeneric() { return GENERIC; }
	
	/**
	 * Returns whether this visualization plugin is configurable or not
	 * @see VisualizationPlugin#setIsConfigurable(boolean)
	 * @return true if this plugin is configurable, false otherwise
	 */
	public final boolean isConfigurable() { return CONFIGURABLE; }
	
	/**
	 * Returns whether this visualization plugin uses the area provided by
	 * the {@link Visualization} it belongs to.
	 * @see VisualizationPlugin#setUseProvidedArea(boolean)
	 * @return true if this plugin uses the provided area, false otherwise
	 */
	public final boolean isUseProvidedArea() { 
		return USE_PROVIDED_AREA; 
	}
		
	/**
	 * Fire a {@link VisualizationEvent} with type {@link VisualizationEvent#PLUGIN_MODIFIED}
	 */
	protected final void fireModifiedEvent() {
		VisualizationManager.fireVisualizationEvent(
				new VisualizationEvent(this, VisualizationEvent.PLUGIN_MODIFIED));
	}
	
	/**
	 * The configuration dialog that displays the configuration settings for a 
	 * visualization plugin
	 * @author Thomas
	 */
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
