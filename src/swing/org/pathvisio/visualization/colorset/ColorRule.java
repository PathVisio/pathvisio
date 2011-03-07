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
package org.pathvisio.visualization.colorset;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import org.jdom.Element;
import org.pathvisio.debug.Logger;
import org.pathvisio.gex.ReporterData;
import org.pathvisio.gex.Sample;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.visualization.colorset.Criterion.CriterionException;

/**
 * A rule, or boolean expression, that determines how an
 * Object value (usually a Double, but that is not required)
 * maps to a color
 */
public class ColorRule extends ColorSetObject
{
	// colorCriterion for backwards compatibility
	public static final String XML_ELEMENT_NAME = "ColorCriterion";

	private Criterion criterion;

	public static final Color INITIAL_COLOR = Color.WHITE;
	private Color color;

	public void setColor(Color color)
	{
		this.color = color;
		fireModifiedEvent();
	}

	public Color getColor() { return color == null ? INITIAL_COLOR : color; }

	/**
	 * Returns error message or null if there was no error.
	 */
	public String setExpression(String expression, List<String> symbols)
	{
		return criterion.setExpression(expression, symbols);
	}

	public String getExpression() { return criterion.getExpression(); }

	public ColorRule() {
		setName("rule");
		criterion = new Criterion();
	}

	public ColorRule(Element xml) {
		loadXML(xml);
	}

	@Override Color getColor(ReporterData data, Sample key) throws CriterionException
	{
		if (criterion.evaluate(data.getByName())) return color;
		return null;
	}

	public void paintPreview(Graphics2D g, Rectangle bounds) {
		Color c = getColor();
		if(c == null) c = Color.BLACK;
		g.setColor(c);
		g.fill(bounds);
	}

	public String getXmlElementName() {
		return XML_ELEMENT_NAME;
	}

	protected void loadXML(Element xml) {
		setName(xml.getAttributeValue(XML_ATTR_NAME));
		try {
			String expression = xml.getAttributeValue(XML_ATTR_EXPRESSION);
			criterion = new Criterion();
			criterion.setExpression(expression);
			Element ce = xml.getChild(XML_ELM_COLOR);
			if(ce != null) color = ColorConverter.parseColorElement(ce);
		} catch(Exception e) {
			Logger.log.error("Unable to load ColorCriterion", e);
		}
	}

	static final String XML_ELM_COLOR = "color";
	static final String XML_ATTR_EXPRESSION = "expression";
	public Element toXML() {
		Element elm = super.toXML();
		Element ce = ColorConverter.createColorElement(XML_ELM_COLOR, getColor());
		elm.addContent(ce);
		elm.setAttribute(XML_ATTR_EXPRESSION, criterion.getExpression());

		return elm;
	}
}
