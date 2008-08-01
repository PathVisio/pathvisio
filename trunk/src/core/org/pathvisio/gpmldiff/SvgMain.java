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
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

class SvgMain
{
	static final int OLD = 0;
	static final int NEW = 1;

	static public void main (String argv[]) throws IOException, ConverterException
	{
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument (svgNS, "svg", null);
		
		Pathway pwy[] = {new Pathway(), new Pathway()};
		
// 		pwy[OLD].readFromXml (new File ("testcases/Simple1.1.gpml"), false);
// 		pwy[NEW].readFromXml (new File ("testcases/Simple1.2.gpml"), false);
 		pwy[OLD].readFromXml (new File ("testcases/sandbox070524.gpml"), false);
 		pwy[NEW].readFromXml (new File ("testcases/sandbox070522_5.gpml"), false);
		
		VPathway vpwy[] = {new VPathway(null), new VPathway(null)};
		
		int[] width = new int[2];
		int[] height = new int[2];
		for (int i = 0; i < 2; ++i)
		{
			vpwy[i].fromModel(pwy[i]);
// 			vpwy[i].setPctZoom (50);
			width[i] = vpwy[i].getVWidth();
			height[i] = vpwy[i].getVHeight();
		}

		int maxh = height[OLD] > height[NEW] ? height[OLD] : height[NEW];		
		int maxw = width[OLD] > width[NEW] ? width[OLD] : width[NEW];

		int j = 0;
		for (VPathwayElement e : vpwy[OLD].getDrawingObjects())
		{
			if (e instanceof org.pathvisio.view.Graphics)
			{
				switch (j % 5)
				{
				case 0: e.highlight (Color.GREEN); break;
				case 1: e.highlight (Color.BLUE); break;
				case 2: e.highlight (Color.YELLOW); break;
				case 3: e.highlight (Color.RED); break;
				case 4: break; // no highlight
				default: assert (false);
				}
				j++;
			}
		}

		
 		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		svgGenerator.setSVGCanvasSize (new Dimension (maxw * 2, maxh));

		vpwy[OLD].draw (svgGenerator, null, true);
 		svgGenerator.translate (maxw, 0);
 		vpwy[NEW].draw (svgGenerator, null, true);
		
		boolean useCSS = true;
		Writer out = new OutputStreamWriter (System.out, "UTF-8");
		svgGenerator.stream (out, useCSS);
	}
}
