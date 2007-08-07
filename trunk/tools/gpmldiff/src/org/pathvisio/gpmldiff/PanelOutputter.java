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
import java.awt.Rectangle;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.debug.Logger;
import java.util.*;
import java.io.*;

class PanelOutputter extends DiffOutputter
{
	VPathway vpwy[] = new VPathway[2];

	Pathway pwy[] = new Pathway[2];

	private static final int PWY_OLD = 0;
	private static final int PWY_NEW = 1;
	
	PanelOutputter (VPathway _old, VPathway _new)
	{
		vpwy[PWY_OLD] = _old;
		vpwy[PWY_NEW] = _new;
		for (int i = 0; i < 2; ++i)
		{
			pwy[i] = vpwy[i].getGmmlData();
		}
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
	public class ModData implements Comparable<ModData>
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
				if (g.getGmmlData() == target)
				{
					return velt;
				}
			}
		}
		return null;
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

	public void flush() throws IOException
	{
	}
	
	public void modifyEnd ()
	{
		VPathwayElement veltOld = findElt (curOldElt, vpwy[PWY_OLD]);
		assert (veltOld != null);
		veltOld.highlight (Color.YELLOW);
		Rectangle r1 = veltOld.getVBounds();
		
		VPathwayElement veltNew = findElt (curNewElt, vpwy[PWY_NEW]);
		assert (veltNew != null);
		veltNew.highlight (Color.YELLOW);
		Rectangle r2 = veltNew.getVBounds();

		String completeHint = "";
		for (String hint : curHint)
		{
			completeHint += hint;
			completeHint += ", ";
		}
		completeHint += "changed";

		ModData mod = new ModData (
				(int)(r1.getX() + r1.getWidth() / 2),
				(int)(r1.getY() + r1.getHeight() / 2),
				(int)(r2.getX() + r2.getWidth() / 2),
				(int)(r2.getY() + r2.getHeight() / 2),
				completeHint);

		modifications.add (mod);
		modsByElt.put (veltOld, mod);
		modsByElt.put (veltNew, mod);
		
		curOldElt = null;
		curNewElt = null;
	}

	private List <ModData> modifications = new ArrayList<ModData>();

	// TODO: accessors
	public Map <VPathwayElement, ModData> modsByElt = new HashMap<VPathwayElement, ModData>();
	
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

}