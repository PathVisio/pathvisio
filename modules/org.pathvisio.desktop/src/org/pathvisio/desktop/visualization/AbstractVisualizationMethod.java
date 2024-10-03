/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.desktop.visualization;

import javax.swing.JPanel;

import org.jdom2.Element;

/**
 * The VisualizationMethod class can be extended to create a visualization method for
 * the visualization of experimental data on GPML pathways
 */
public abstract class AbstractVisualizationMethod implements VisualizationMethod
{
	private boolean isConfigurable; //Configurable (if true, override createConfigComposite)
	private boolean isUseProvidedArea; //Does this plugin use reserved region in o.p.view.Graphics

	private Visualization visualization;

	/** Internal method, should be called by Visualization.add-,removeMethod() only */
	public void setVisualization(Visualization v) { visualization = v; }
	
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
	 * The provided area can be obtained by calling {@link Visualization#provideDrawArea(AbstractVisualizationMethod, Graphics)}
	 * @param use	true if this plugin uses the provided area, false if not
	 * @see Visualization#provideDrawArea(AbstractVisualizationMethod, Graphics)
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
	 * @see AbstractVisualizationMethod#setIsConfigurable(boolean)
	 * @return true if this plugin is configurable, false otherwise
	 */
	public final boolean isConfigurable() { return isConfigurable; }

	public JPanel getConfigurationPanel() { return new JPanel(); }

	/**
	 * Returns whether this visualization plugin uses the area provided by
	 * the {@link Visualization} it belongs to.
	 * @see AbstractVisualizationMethod#setUseProvidedArea(boolean)
	 * @return true if this plugin uses the provided area, false otherwise
	 */
	public final boolean isUseProvidedArea() {
		return isUseProvidedArea;
	}

	public int compareTo(VisualizationMethod o)
	{
		return getName().compareTo(o.getName());
	}

	@Override 
	public void setActive(boolean value) {}
}
