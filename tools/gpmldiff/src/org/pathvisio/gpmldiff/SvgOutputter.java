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
package org.pathvisio.gpmldiff;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.awt.font.TextLayout;
import java.io.*;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

class SvgOutputter extends DiffOutputter
{
	private OutputStream out;
	private PwyDoc oldPwy;
	private PwyDoc newPwy;
	
	private static final int PWY_OLD = 0;
	private static final int PWY_NEW = 1;
	VPathway vpwy[] = {new VPathway(null), new VPathway(null)};
	SVGGraphics2D svgGenerator;
	
	private static final int HEADER_HIGHT = 32;
	int deltax = 0;
	
	SvgOutputter(PwyDoc _oldPwy, PwyDoc _newPwy, File f) throws IOException
	{
		this(_oldPwy, _newPwy);
		out = new FileOutputStream(f);
	}
	
	SvgOutputter(PwyDoc _oldPwy, PwyDoc _newPwy)
	{
		out = System.out;
		oldPwy = _oldPwy;
		newPwy = _newPwy;

		vpwy[PWY_OLD].fromGmmlData (oldPwy.getPathway());
		vpwy[PWY_NEW].fromGmmlData (newPwy.getPathway());

		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument (svgNS, "svg", null);

		int[] width = new int[2];
		int[] height = new int[2];

		for (int i = 0; i < 2; ++i)
		{
//  			vpwy[i].setPctZoom (50);
			width[i] = vpwy[i].getVWidth();
			height[i] = vpwy[i].getVHeight();
		}

		
		int maxh = height[PWY_OLD] > height[PWY_NEW] ? height[PWY_OLD] : height[PWY_NEW];
		int maxw = width[PWY_OLD] > width[PWY_NEW] ? width[PWY_OLD] : width[PWY_NEW];

		deltax = width[0];
		svgGenerator = new SVGGraphics2D(document);
		svgGenerator.setSVGCanvasSize (new Dimension (width[0] + width[1], maxh + HEADER_HIGHT));

		// titles
		svgGenerator.setFont(new Font(null, Font.PLAIN, 16));
		TextLayout tl = new TextLayout(
			"OLD: " + oldPwy.getSourceFile().getName(),
			svgGenerator.getFont(), svgGenerator.getFontRenderContext());
		Rectangle2D tb = tl.getBounds();
		tl.draw (svgGenerator, (float)((width[PWY_OLD] - tb.getWidth()) / 2), (float)tb.getHeight());
		tl = new TextLayout(
			"NEW: " + newPwy.getSourceFile().getName(),
			svgGenerator.getFont(), svgGenerator.getFontRenderContext());
		tb = tl.getBounds();
		tl.draw (svgGenerator, (float)(width[PWY_OLD] + (width[PWY_OLD] - tb.getWidth()) / 2), (float)tb.getHeight());

	}

	public void flush() throws IOException
	{		
		// pwy's themselves		
 		svgGenerator.translate (0, HEADER_HIGHT);
		vpwy[PWY_OLD].draw (svgGenerator, null, false);
 		svgGenerator.translate (deltax, 0);
 		vpwy[PWY_NEW].draw (svgGenerator, null, false);
		
		boolean useCSS = true;
		Writer out = new OutputStreamWriter (System.out, "UTF-8");
		svgGenerator.stream (out, useCSS);

	}
	
	public void insert(PwyElt newElt)
	{
		VPathwayElement velt = findElt (newElt, vpwy[PWY_NEW]);
		assert (velt != null);
		velt.highlight (Color.GREEN);
	}

	public void delete(PwyElt oldElt)
	{
		VPathwayElement velt = findElt (oldElt, vpwy[PWY_OLD]);
		assert (velt != null);
		velt.highlight (Color.RED);
	}

	public void modify(PwyElt oldElt, PwyElt newElt, String path, String oldVal, String newVal)
	{
		VPathwayElement velt = findElt (oldElt, vpwy[PWY_OLD]);
		assert (velt != null);
		velt.highlight (Color.YELLOW);
		Rectangle r1 = velt.getVBounds();
		
		velt = findElt (newElt, vpwy[PWY_NEW]);
		assert (velt != null);
		velt.highlight (Color.YELLOW);
		Rectangle r2 = velt.getVBounds();

		svgGenerator.setColor (Color.YELLOW);
		svgGenerator.drawLine (
			(int)(r1.getX() + r1.getWidth() / 2),
			(int)(r1.getY() + r1.getHeight() / 2 + HEADER_HIGHT),
			(int)(r2.getX() + r2.getWidth() / 2) + deltax,
			(int)(r2.getY() + r2.getHeight() / 2) + HEADER_HIGHT);
	}

	/**
	   helper to find a VPathwayElt that corresponds to a certain PwyElt
	*/
	private VPathwayElement findElt (PwyElt elt, VPathway vpwy)
	{
		PathwayElement target = elt.getElement();
		for (VPathwayElement velt : vpwy.getDrawingObjects())
		{
			if (velt instanceof Graphics)
			{
				Graphics g = (Graphics)velt;
				if (g.getGmmlData() == target)
				{
					return velt;
				}
			}
		}
		return null;
	}

}
