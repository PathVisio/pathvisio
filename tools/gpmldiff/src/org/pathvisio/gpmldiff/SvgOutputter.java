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
	OutputStream out;
	PwyDoc oldPwy;
	PwyDoc newPwy;
	VPathway vpwy[] = {new VPathway(null), new VPathway(null)};
	
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

		vpwy[0].fromGmmlData (oldPwy.getPathway());
		vpwy[1].fromGmmlData (newPwy.getPathway());
	}

	public void flush() throws IOException
	{
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument (svgNS, "svg", null);

		int[] width = new int[2];
		int[] height = new int[2];
		for (int i = 0; i < 2; ++i)
		{
 			vpwy[i].setPctZoom (50);
			width[i] = vpwy[i].getVWidth();
			height[i] = vpwy[i].getVHeight();
		}

		int maxh = height[0] > height[1] ? height[0] : height[1];		
		int maxw = width[0] > width[1] ? width[0] : width[1];

		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		svgGenerator.setSVGCanvasSize (new Dimension (width[0] + width[1], maxh));

		vpwy[0].draw (svgGenerator, null, true);
 		svgGenerator.translate (width[0], 0);
 		vpwy[1].draw (svgGenerator, null, true);
		
		boolean useCSS = true;
		Writer out = new OutputStreamWriter (System.out, "UTF-8");
		svgGenerator.stream (out, useCSS);

	}
	
	public void insert(PwyElt newElt)
	{
		VPathwayElement velt = findElt (newElt, vpwy[0]);
		assert (velt != null);
		velt.highlight (Color.GREEN);
	}

	public void delete(PwyElt oldElt)
	{
		VPathwayElement velt = findElt (oldElt, vpwy[1]);
		assert (velt != null);
		velt.highlight (Color.RED);
	}

	public void modify(PwyElt oldElt, PwyElt newElt, String path, String oldVal, String newVal)
	{
		VPathwayElement velt = findElt (oldElt, vpwy[0]);
		assert (velt != null);
		velt.highlight (Color.YELLOW);
		velt = findElt (newElt, vpwy[1]);
		assert (velt != null);
		velt.highlight (Color.YELLOW);
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
