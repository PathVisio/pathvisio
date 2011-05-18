// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.desktop.visualization;

import java.awt.Component;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.jdom.Element;
import org.pathvisio.core.view.Graphics;

/**
 * The VisualizationMethod class can be extended to create a visualization method for
 * the visualization of experimental data on GPML pathways
 * @author Thomas
 */
public abstract class VisualizationMethod implements Comparable <VisualizationMethod>
{
	public static final String XML_ELEMENT = "method";
	public static final String XML_ATTR_NAME = "name";

	private boolean isConfigurable; //Configurable (if true, override createConfigComposite)
	private boolean isUseProvidedArea; //Does this plugin use reserved region in o.p.view.Graphics

	private Visualization visualization;

	/** Internal method, should be called by Visualization.add-,removeMethod() only */
	void setVisualization(Visualization v) { visualization = v; }
	
	/**
	 * @return The {@link Visualization} this plugin belongs to. May be null!
	 */
	public final Visualization getVisualization() { return visualization; }

	/**
	 * Call this method whenever a setting is modified. It will
	 * notify the parent visualization of the modification.
	 */
	protected final void modified() {
		if (visualization != null) visualization.modified();
	}

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
	 * Create a visualization on the pathway drawing.
	 */
	public abstract void visualizeOnDrawing(Graphics g, Graphics2D g2d);

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
	 * Return a default drawing order for this visualization method.
	 * The user may override this default,
	 * but a sensible default can be provided.
	 *
	 * Opaque drawing methods should return a value of 0 or less
	 * Transparant drawing methods (e.g. labels) should return a value of 1 or higher.
	 */
	public abstract int defaultDrawingOrder();

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
		elm.setAttribute(XML_ATTR_NAME, getClass().toString());
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
	 * Specify whether the parent {@link Visualization} needs to provide an area on the
	 * graphics.
	 * When multiple visualization plugins apply visualizations on the same {@link Graphics}
	 * object, the available space will be divided over the plugins for which this method is called
	 * with true as argument.
	 * The provided area can be obtained by calling {@link Visualization#provideDrawArea(VisualizationMethod, Graphics)}
	 * @param use	true if this plugin uses the provided area, false if not
	 * @see Visualization#provideDrawArea(VisualizationMethod, Graphics)
	 */
	protected void setUseProvidedArea(boolean use) {
		isUseProvidedArea = use;
	}

	/**
	 * Specify whether this visualization plugin is configurable or not.
	 * @param configurable
	 */
	protected void setIsConfigurable(boolean configurable) {
		isConfigurable = configurable;
	}

	/**
	 * Returns whether this visualization plugin is configurable or not
	 * @see VisualizationMethod#setIsConfigurable(boolean)
	 * @return true if this plugin is configurable, false otherwise
	 */
	public final boolean isConfigurable() { return isConfigurable; }

	public JPanel getConfigurationPanel() { return new JPanel(); }

	/**
	 * Returns whether this visualization plugin uses the area provided by
	 * the {@link Visualization} it belongs to.
	 * @see VisualizationMethod#setUseProvidedArea(boolean)
	 * @return true if this plugin uses the provided area, false otherwise
	 */
	public final boolean isUseProvidedArea() {
		return isUseProvidedArea;
	}

	public int compareTo(VisualizationMethod o)
	{
		return getName().compareTo(o.getName());
	}
}
