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
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

class SvgOutputter extends DiffOutputter
{
	// graphics parameters
	private static final int BALOON_COLS = 3;
	private static final int BALOON_Y_SPACER = 10;
	private static final int CENTER_MARGIN = 400;
	private static final int BALOON_MARGIN = 5;
	private static final int BALOON_WIDTH = (CENTER_MARGIN / BALOON_COLS) - BALOON_MARGIN;
	private static final int HEADER_HEIGHT = 32;
	//private static final int BALOON_FONT_SIZE = 6;
	private static final int HEADER_FONT_SIZE = 16;
	private static final int PCT_ZOOM = 70;
	
	private OutputStream out; //TODO: use this for output
	private PwyDoc oldPwy;
	private PwyDoc newPwy;
	
	private static final int PWY_OLD = 0;
	private static final int PWY_NEW = 1;
	VPathway vpwy[] = {new VPathway(null), new VPathway(null)};
	SVGGraphics2D svgGenerator;
	
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
  			vpwy[i].setPctZoom (PCT_ZOOM);
			width[i] = vpwy[i].getVWidth();
			height[i] = vpwy[i].getVHeight();
		}

		
		int maxh = height[PWY_OLD] > height[PWY_NEW] ? height[PWY_OLD] : height[PWY_NEW];
		//int maxw = width[PWY_OLD] > width[PWY_NEW] ? width[PWY_OLD] : width[PWY_NEW];

		deltax = width[0];
		svgGenerator = new SVGGraphics2D(document);
		svgGenerator.setSVGCanvasSize (new Dimension (width[0] + width[1] + CENTER_MARGIN, maxh + HEADER_HEIGHT));

		// titles
		svgGenerator.setFont(new Font(null, Font.PLAIN, HEADER_FONT_SIZE));
		TextLayout tl = new TextLayout(
			"OLD: " + oldPwy.getSourceFile().getName(),
			svgGenerator.getFont(), svgGenerator.getFontRenderContext());
		Rectangle2D tb = tl.getBounds();
		tl.draw (svgGenerator, (float)((width[PWY_OLD] - tb.getWidth()) / 2), (float)tb.getHeight());
		tl = new TextLayout(
			"NEW: " + newPwy.getSourceFile().getName(),
			svgGenerator.getFont(), svgGenerator.getFontRenderContext());
		tb = tl.getBounds();
		tl.draw (
			svgGenerator,
			(float)(width[PWY_OLD] + CENTER_MARGIN + (width[PWY_OLD] - tb.getWidth()) / 2),
			(float)tb.getHeight()
			);

	}

	public void flush() throws IOException
	{
		Collections.sort (modifications);
		
		// mod hints
		svgGenerator.setFont(new Font(null, Font.PLAIN, HEADER_FONT_SIZE));
		int col = 0;
		int[] ypos = new int[BALOON_COLS];
		for (int i = 0; i < BALOON_COLS; i++) ypos[i] = HEADER_HEIGHT;
		for (ModData m : modifications)
		{
			int xpos = deltax + (BALOON_MARGIN + BALOON_WIDTH) * col;
			FontRenderContext frc = svgGenerator.getFontRenderContext();

			AttributedCharacterIterator styledText =
				new AttributedString (m.hint).getIterator();
			// let styledText be an AttributedCharacterIterator containing at least
			// one character
			
			LineBreakMeasurer measurer = new LineBreakMeasurer(styledText, frc);

			int top = ypos[col];
			while (measurer.getPosition() < m.hint.length()) {
				
				TextLayout layout = measurer.nextLayout(BALOON_WIDTH - 2 * BALOON_MARGIN);
				
				ypos[col] += (layout.getAscent());				
				layout.draw(svgGenerator, xpos + BALOON_MARGIN, ypos[col]);
				ypos[col] += layout.getDescent() + layout.getLeading();
			}
			int bot = ypos[col];
			m.midy = top + (bot - top) / 2;
			ypos[col] += BALOON_Y_SPACER;
			col = (++col) % BALOON_COLS;

			svgGenerator.draw (
				new RoundRectangle2D.Double (
					xpos, top,
					BALOON_WIDTH, bot - top,
					BALOON_MARGIN, BALOON_MARGIN));			
		}
		
		// mod lines
		svgGenerator.setColor (Color.YELLOW);
		
		for (ModData m : modifications)
		{
			svgGenerator.drawLine (
				m.x1, m.y1,	deltax,	m.midy);
			svgGenerator.drawLine (
				deltax + CENTER_MARGIN,	m.midy,	m.x2, m.y2);
		}
		
		// pwy's themselves		
 		svgGenerator.translate (0, HEADER_HEIGHT);
		vpwy[PWY_OLD].draw (svgGenerator, null, false);
		svgGenerator.setClip (null); // reset clipping
 		svgGenerator.translate (deltax + CENTER_MARGIN, 0);
 		vpwy[PWY_NEW].draw (svgGenerator, null, false);
		
		boolean useCSS = true;
		Writer out = new OutputStreamWriter (System.out, "UTF-8");
		svgGenerator.stream (out, useCSS);
	}
	
	public void insert(PathwayElement newElt)
	{
		VPathwayElement velt = findElt (newElt, vpwy[PWY_NEW]);
 		//assert (velt != null || newElt.getObjectType () == ObjectType.INFOBOX);
		if (velt == null)
		{
			Logger.log.warn (PwyElt.summary(newElt) + " doesn't have a corresponding view element");
		}
		if (velt != null) velt.highlight (Color.GREEN);
	}

	public void delete(PathwayElement oldElt)
	{
		VPathwayElement velt = findElt (oldElt, vpwy[PWY_OLD]);
 		//assert (velt != null || oldElt.getObjectType () == ObjectType.INFOBOX);
		if (velt == null)
		{
			Logger.log.warn (PwyElt.summary(oldElt) + " doesn't have a corresponding view element");
		}
		if (velt != null) velt.highlight (Color.RED);
	}


	/**
	   private data about a modification,
	   for displaying hints in the middle.
	 */
	private class ModData implements Comparable<ModData>
	{
		int midy;
		String hint;
		int x1;
		int y1;
		int x2;
		int y2;

		ModData (int x1, int y1, int x2, int y2, String hint)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.hint = hint;
		}

		/**
		   note: compareTo <=> equals for ModData
		*/
		public int compareTo (ModData other)
		{
			return y1 + y2 - other.y1 - other.y2;
		}
	}

	PathwayElement curOldElt = null;
	PathwayElement curNewElt = null;
	Set<String> curHint = null;
	
	public void modifyStart (PathwayElement oldElt, PathwayElement newElt)
	{
		curOldElt = oldElt;
		curNewElt = newElt;
		curHint = new HashSet<String>();
	}

	public void modifyEnd ()
	{
		VPathwayElement velt = findElt (curOldElt, vpwy[PWY_OLD]);
		assert (velt != null);
		velt.highlight (Color.YELLOW);
		Rectangle2D r1 = velt.getVBounds();
		
		velt = findElt (curNewElt, vpwy[PWY_NEW]);
		assert (velt != null);
		velt.highlight (Color.YELLOW);
		Rectangle2D r2 = velt.getVBounds();

		String completeHint = "";
		for (String hint : curHint)
		{
			completeHint += hint;
			completeHint += ", ";
		}
		completeHint += "changed";
		
		modifications.add (
			new ModData (
				(int)(r1.getX() + r1.getWidth() / 2),
				(int)(r1.getY() + r1.getHeight() / 2) + HEADER_HEIGHT,
				(int)(r2.getX() + r2.getWidth() / 2) + deltax + CENTER_MARGIN,
				(int)(r2.getY() + r2.getHeight() / 2) + HEADER_HEIGHT,
				completeHint)
			);
		
		curOldElt = null;
		curNewElt = null;
	}

	private List <ModData> modifications = new ArrayList<ModData>();
	
	public void modifyAttr(String attr, String oldVal, String newVal)
	{
		if (attr.equalsIgnoreCase("centerx") || 
			attr.equalsIgnoreCase("centery") ||
			attr.equalsIgnoreCase("endx") ||
			attr.equalsIgnoreCase("endy") ||
			attr.equalsIgnoreCase("startx") ||
			attr.equalsIgnoreCase("starty"))
		{
			curHint.add ("position");
		}
		else if (
			attr.equalsIgnoreCase("width") ||
			attr.equalsIgnoreCase("height")
			)
		{
			curHint.add ("size");
		}
		else
		{
			curHint.add (attr);
		}
	}

	/**
	   helper to find a VPathwayElt that corresponds to a certain PathwayElement
	*/
	private VPathwayElement findElt (PathwayElement target, VPathway vpwy)
	{
		for (VPathwayElement velt : vpwy.getDrawingObjects())
		{
			if (velt instanceof Graphics)
			{
				Graphics g = (Graphics)velt;
				if (g.getPathwayElement() == target)
				{
					return velt;
				}
			}
		}
		return null;
	}

}
