// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.desktop.visualization.ColorRule;
import org.pathvisio.desktop.visualization.ColorSet;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.Visualization;
import org.pathvisio.desktop.visualization.VisualizationEvent;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.desktop.visualization.VisualizationManager.VisualizationListener;
import org.pathvisio.desktop.visualization.VisualizationMethod;
import org.pathvisio.visualization.plugins.ColorByExpression.ConfiguredSample;

/**
* This class shows a legend for the currently loaded visualization and color-sets.
*/
public class LegendPanel extends JPanel implements VisualizationListener {

	final VisualizationManager visualizationManager;

	public LegendPanel(VisualizationManager visualizationManager)
	{
		this.visualizationManager = visualizationManager;
		visualizationManager.addListener(this);
		setBorder (BorderFactory.createLineBorder(Color.BLACK));
		createContents();
		rebuildContent();
	}

	public Dimension getPreferredSize()
	{
		return new Dimension (200, 600);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		drawVisualization (visualizationManager.getActiveVisualization(), visualizationManager.getColorSetManager(), 
				(Graphics2D)g, new Rectangle2D.Double(0, 0, 100, 100), 1.0);
	}

	/**
	 * Rebuild the contents of the legend (refresh the names
	 * in colorSetCombo and refresh the content)
	 */
	public void rebuildContent()
	{
		refreshContent();
	}


	public static void drawVisualization(Visualization v, ColorSetManager colorSetManager, Graphics2D g, Rectangle2D area, double zoomFactor)
	{
		if (v == null) return;

		double xpos = (int) (zoomFactor * MARGIN_LEFT + area.getMinX());
		double ypos = (int) (zoomFactor * MARGIN_TOP + area.getMinY());

		boolean advanced = colorSetManager.getColorSets().size() > 1;

		Set<ColorSet> usedColorSets = new HashSet<ColorSet>();

		for (VisualizationMethod vm : v.getMethods())
		{
			if (vm instanceof ColorByExpression)
			{
				ypos = drawSamples (g, (ColorByExpression)vm, xpos, ypos, advanced, zoomFactor);
				for (ConfiguredSample cs : ((ColorByExpression)vm).getConfiguredSamples())
				{
					usedColorSets.add(cs.getColorSet());
				}
			}
		}

		for (ColorSet cs : usedColorSets)
		{
			ypos = drawColorset(g, cs, xpos, ypos, advanced, zoomFactor);
		}
	}

	private static final double TOTAL_SAMPLES_WIDTH = 100;
	private static final double SAMPLES_HEIGHT = 20;
	private static final double COLOR_BOX_SIZE = 20;
	private static final double COLOR_GRADIENT_WIDTH = 80;
	private static final double COLOR_GRADIENT_MARGIN = 50;
	private static final double MARGIN_LEFT = 5;
	private static final double MARGIN_TOP = 5;
	private static final double INNER_MARGIN = 5;

	private static double drawSamples (Graphics2D g, ColorByExpression cbex, double left, double top, boolean advanced, double zoomFactor)
	{
		double lineHeight = g.getFontMetrics().getHeight();
		int sampleNum = cbex.getConfiguredSamples().size();
		double partWidth = TOTAL_SAMPLES_WIDTH / sampleNum * zoomFactor;
		double baseline = top + (SAMPLES_HEIGHT + INNER_MARGIN * zoomFactor);

		for (int i = 0; i < sampleNum; ++i)
		{
			g.drawRect ((int)(left + i * partWidth), (int)top, (int)partWidth, (int) (SAMPLES_HEIGHT * zoomFactor));
			double base = lineHeight - g.getFontMetrics().getDescent();

			double labelLeft = left + (partWidth / 2) + (i * partWidth);
			double labelTop = baseline + lineHeight * (sampleNum - i);

			ColorByExpression.ConfiguredSample s = cbex.getConfiguredSamples().get(i);
			String label = s.getSample().getName();
			if (advanced) label += " (" + s.getColorSetName() + ")";
			g.drawString (label, (int)labelLeft, (int)(labelTop + base));
			g.drawLine ((int)labelLeft, (int)baseline, (int)labelLeft, (int)labelTop);
		}

		return baseline + (lineHeight * (sampleNum + 1)) + (INNER_MARGIN * zoomFactor);
	}

	private static double drawColorset (Graphics2D g, ColorSet cs, double left, double top, boolean advanced, double zoomFactor)
	{
		double xco = left;
		double yco = top;
		int base = g.getFontMetrics().getHeight() - g.getFontMetrics().getDescent();
		g.setColor (Color.BLACK);
		if (advanced)
		{
			g.drawString(cs.getName(), (int)left, (int)(top + base));
			yco += g.getFontMetrics().getHeight();
		}

		ColorGradient gradient = cs.getGradient();
		if (gradient != null) yco = drawGradient(g, gradient, left, top, zoomFactor);
		for (ColorRule cr : cs.getColorRules())
		{
			yco = drawColorRule (g, cr, xco, yco, zoomFactor);
		}

		return (int) (yco + (INNER_MARGIN * zoomFactor));
	}

	private static double drawColorRule (Graphics2D g, ColorRule cr, double left, double top, double zoomFactor)
	{
		int height = g.getFontMetrics().getHeight();
		int base = height - g.getFontMetrics().getDescent();
		double xco = left + (zoomFactor * COLOR_GRADIENT_MARGIN);
		double yco = top;
		Rectangle2D bounds2 = new Rectangle2D.Double (xco, yco, zoomFactor * COLOR_BOX_SIZE, zoomFactor * COLOR_BOX_SIZE);
		Rectangle2D bounds = new Rectangle2D.Double (xco + 1, yco + 1, COLOR_BOX_SIZE-2, COLOR_BOX_SIZE-2);
		g.drawString (cr.getExpression(), (int)(xco + (zoomFactor * (COLOR_BOX_SIZE + INNER_MARGIN))), (int)(yco + base));
		g.setColor(cr.getColor());
		g.fill(bounds);
		g.setColor (Color.WHITE);
		g.draw (bounds);
		g.setColor (Color.BLACK);
		g.draw (bounds2);
		return top + height + INNER_MARGIN;
	}

	private static double drawGradient (Graphics2D g, ColorGradient cg, double left, double top, double zoomFactor)
	{
		int height = g.getFontMetrics().getHeight();
		int base = height - g.getFontMetrics().getDescent();
		double xco = left + (zoomFactor * COLOR_GRADIENT_MARGIN);
		double yco = top;
		Rectangle bounds = new Rectangle ((int)xco, (int)yco, (int)(COLOR_GRADIENT_WIDTH * zoomFactor), (int)(COLOR_BOX_SIZE * zoomFactor));
		cg.paintPreview(g, bounds);
		g.setColor (Color.BLACK); // paintPreview will change pen Color
		yco += zoomFactor * COLOR_BOX_SIZE;

		int num = cg.getColorValuePairs().size();
		double w = (zoomFactor * COLOR_GRADIENT_WIDTH) / (num - 1);
		for (int i = 0; i < num; ++i)
		{
			ColorValuePair cvp = cg.getColorValuePairs().get (i);
			double labelLeft = xco + i * w;
			double labelTop = yco + (INNER_MARGIN * zoomFactor);
			String label = "" + cvp.getValue();
			int labelWidth = (int)g.getFontMetrics().getStringBounds(label, g).getWidth();
			g.drawString (label, (int)(labelLeft - labelWidth / 2), (int)(labelTop + base));
			g.drawLine ((int)labelLeft, (int)yco, (int)labelLeft, (int)labelTop);
		}

		return yco + height + (zoomFactor * (INNER_MARGIN + INNER_MARGIN));
	}

	/**
	 * Refresh the content of the legend
	 */
	void refreshContent()
	{
	}

	/**
	 * Create the contents of the legend
	 */
	void createContents()
	{
	}

	public void visualizationEvent(final VisualizationEvent e)
	{
		repaint();
	}

}