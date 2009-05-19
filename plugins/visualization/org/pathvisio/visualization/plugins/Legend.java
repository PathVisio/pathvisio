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
package org.pathvisio.visualization.plugins;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationEvent;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationManager.VisualizationListener;
import org.pathvisio.visualization.VisualizationMethod;
import org.pathvisio.visualization.colorset.ColorGradient;
import org.pathvisio.visualization.colorset.ColorRule;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetManager;
import org.pathvisio.visualization.colorset.ColorSetObject;

/**
* This class shows a legend for the currently loaded visualization and color-sets.
*/
public class Legend extends JPanel implements VisualizationListener {
	
	final ColorSetManager colorSetManager;
	final VisualizationManager visualizationManager;
	
	public Legend(PvDesktop desktop)
	{
		visualizationManager = desktop.getVisualizationManager();
		visualizationManager.addListener(this);
		colorSetManager = visualizationManager.getColorSetManager();
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
		drawVisualization ((Graphics2D)g);
	}
	
	/**
	 * Rebuild the contents of the legend (refresh the names
	 * in colorSetCombo and refresh the content)
	 */
	public void rebuildContent() 
	{
		refreshContent();
	}

	
	private void drawVisualization(Graphics2D g)
	{
		Visualization v = visualizationManager.getActiveVisualization();
		if (v == null) return;
		
		int xpos = MARGIN_LEFT;
		int ypos = MARGIN_TOP;
		
		boolean advanced = colorSetManager.getColorSets().size() > 1;
		
		for (VisualizationMethod vm : v.getMethods())
		{
			if (vm instanceof ColorByExpression)
			{
				ypos = drawSamples (g, (ColorByExpression)vm, xpos, ypos, advanced);
			}
		}
		
		for (ColorSet cs : colorSetManager.getColorSets())
		{
			ypos = drawColorset(g, cs, xpos, ypos, advanced);
		}
	}
	
	private static final int TOTAL_SAMPLES_WIDTH = 100;
	private static final int SAMPLES_HEIGHT = 20;
	private static final int COLOR_BOX_SIZE = 20;
	private static final int MARGIN_LEFT = 5;
	private static final int MARGIN_TOP = 5;
	private static final int INNER_MARGIN = 5;
	
	private int drawSamples (Graphics2D g, ColorByExpression cbex, int left, int top, boolean advanced)
	{
		int lineHeight = g.getFontMetrics().getHeight();
		int sampleNum = cbex.getConfiguredSamples().size();
		int partWidth = TOTAL_SAMPLES_WIDTH / sampleNum;
		int baseline = top + SAMPLES_HEIGHT + INNER_MARGIN;
		
		for (int i = 0; i < sampleNum; ++i)
		{
			g.drawRect (left + i * partWidth, top, partWidth, SAMPLES_HEIGHT);
			int base = lineHeight - g.getFontMetrics().getDescent();
			
			int labelLeft = (partWidth / 2) + (i * partWidth);
			int labelTop = baseline + lineHeight * (sampleNum - i);
			
			ColorByExpression.ConfiguredSample s = cbex.getConfiguredSamples().get(i);
			String label = s.getSample().getName();
			if (advanced) label += " (Color set: " + s.getColorSetName() + ")";
			g.drawString (s.getSample().getName(), labelLeft, labelTop + base);
			g.drawLine (labelLeft, baseline, labelLeft, labelTop);
		}
		
		return baseline + (lineHeight * (sampleNum + 1)) + INNER_MARGIN;
	}
	
	private int drawColorset (Graphics2D g, ColorSet cs, int left, int top, boolean advanced)
	{
		int xco = left;
		int yco = top;
		int base = g.getFontMetrics().getHeight() - g.getFontMetrics().getDescent();
		if (advanced)
		{
			g.drawString("ColorSet: " + cs.getName(), left, top + base);
			yco += g.getFontMetrics().getHeight();
		}
		
		for (ColorSetObject cso : cs.getObjects())
		{
			yco = drawColorsetObject (g, cso, xco, yco);
		}
		
		return yco + INNER_MARGIN;
	}
	
	private int drawColorsetObject (Graphics2D g, ColorSetObject cso, int left, int top)
	{
		int height = g.getFontMetrics().getHeight();
		int base = height - g.getFontMetrics().getDescent();
		int xco = left;
		int yco = top;
		Rectangle bounds = new Rectangle (xco, yco, COLOR_BOX_SIZE, COLOR_BOX_SIZE);
		if (cso instanceof ColorGradient)
		{
			ColorGradient cg = (ColorGradient)cso;
			cg.paintPreview(g, bounds);
			g.drawString (cg.getName(), xco + COLOR_BOX_SIZE + INNER_MARGIN, yco + base);
		}
		else if (cso instanceof ColorRule)
		{
			ColorRule cr = (ColorRule)cso;
			g.drawString (cr.getExpression(), xco + COLOR_BOX_SIZE + INNER_MARGIN, yco + base);
			g.setBackground(cr.getColor());
			g.fill(bounds);
		}
		g.draw (bounds);
		return top + height;
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