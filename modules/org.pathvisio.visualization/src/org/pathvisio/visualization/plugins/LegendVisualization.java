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

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.core.Engine;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.Legend;
import org.pathvisio.desktop.visualization.AbstractVisualizationMethod;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.Visualization;

public class LegendVisualization extends AbstractVisualizationMethod
{
	private final Engine engine;
	
	public LegendVisualization (ColorSetManager csm, Engine engine)
	{
		this.csm = csm;
		this.engine = engine;
	}
	
	@Override
	public String getName()
	{
		return "Legend";
	}

	@Override
	public String getDescription()
	{
		return "Show a legend on the pathway";
	}

	private final ColorSetManager csm;

	@Override
	public void visualizeOnDrawing(Graphics g, Graphics2D g2d)
	{
		if (g instanceof Legend)
		{
			Legend l = (Legend)g;
			Rectangle2D area = l.getVBounds();
			double zoomFactor = l.getDrawing().getZoomFactor();
			Font f = l.getVFont();
			g2d.setFont(f);
			LegendPanel.drawVisualization(getVisualization(), csm, g2d, area, zoomFactor);
		}		
	}

	@Override
	public Component visualizeOnToolTip(Graphics g)
	{
		return null;
	}

	@Override
	public int defaultDrawingOrder()
	{
		return 0;
	}
	
	@Override
	public void setActive(boolean value)
	{
		Visualization v = getVisualization();
		if (v != null && value)
		{
			// check if pwy contains a legend, add it if not
			Pathway pwy = engine.getActivePathway();
			if (pwy != null)
			{
				boolean found = false;
				for (PathwayElement elt : pwy.getDataObjects())
				{
					if (elt.getObjectType() == ObjectType.LEGEND) { found = true; break; } 
				}
				if (!found)
				{ 
					PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.LEGEND);
					elt.setMWidth (200);
					elt.setMHeight (400);
					elt.setMLeft(0);
					elt.setMTop(0);
					pwy.add(elt);
				}
			}
		}
	}
}
