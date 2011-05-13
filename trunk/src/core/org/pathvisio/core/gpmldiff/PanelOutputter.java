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
package org.pathvisio.core.gpmldiff;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;

/**
 * Outputs a pathway diff to two VPathways, highlighting the elements
 * in the VPathway red, green or yellow.
 * This is the Outputter used by GpmlDiffWindow, and thus by the GpmlDiff applet.
 * TODO: this class is maybe better named DualVPathwayOutputter
 * as it's not directly linked to a panel.
 */
public class PanelOutputter extends DiffOutputter
{
	VPathway vpwy[] = new VPathway[2];

	Pathway pwy[] = new Pathway[2];

	private static final int PWY_OLD = 0;
	private static final int PWY_NEW = 1;

	public PanelOutputter (VPathway aOld, VPathway aNew)
	{
		vpwy[PWY_OLD] = aOld;
		vpwy[PWY_NEW] = aNew;
		for (int i = 0; i < 2; ++i)
		{
			pwy[i] = vpwy[i].getPathwayModel();
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
		else
		{
			velt.highlight (Color.GREEN);

			Map <String, String> hint = new HashMap<String, String>();
			hint.put ("element", "Element added");

			Rectangle2D r = velt.getVBounds();
			ModData mod = new ModData (
					0,
					0,
					(int)vpwy[PWY_NEW].mFromV(r.getX() + r.getWidth() / 2),
					(int)vpwy[PWY_NEW].mFromV(r.getY() + r.getHeight() / 2),
						hint, ModData.ModType.ADDED);
				modifications.add (mod);
				modsByElt.put (velt, mod);
		}
	}

	public void delete(PathwayElement oldElt)
	{
		VPathwayElement velt = findElt (oldElt, vpwy[PWY_OLD]);
 		//assert (velt != null || oldElt.getObjectType () == ObjectType.INFOBOX);
		if (velt == null)
		{
			Logger.log.warn (PwyElt.summary(oldElt) + " doesn't have a corresponding view element");
		}
		else
		{
			velt.highlight (Color.RED);

			Map <String, String> hint = new HashMap<String, String>();
			hint.put ("element", "Element removed");

			Rectangle2D r = velt.getVBounds();
			ModData mod = new ModData (
					(int)vpwy[PWY_NEW].mFromV(r.getX() + r.getWidth() / 2),
					(int)vpwy[PWY_NEW].mFromV(r.getY() + r.getHeight() / 2),
					0,
					0,
					hint, ModData.ModType.REMOVED);
			modifications.add (mod);
			modsByElt.put (velt, mod);
		}
	}

	/**
	   private data about a modification,
	   for displaying hints in the middle.
	 */
	static public class ModData implements Comparable<ModData>
	{
		/** type of modication: added, removed, changed */
		public static enum ModType
		{
			ADDED,
			REMOVED,
			CHANGED
		}

		private Map <String, String> hints;
		private int x1;
		private int y1;
		private int x2;
		private int y2;
		private ModType type;

		ModData (int x1, int y1, int x2, int y2, Map<String, String> hints, ModType type)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.hints = hints;
			this.type = type;
		}

		/**
		   note: compareTo <=> equals for ModData
		*/
		public int compareTo (ModData other)
		{
			return y1 + y2 - other.y1 - other.y2;
		}
		
		

		public Map<String, String> getHints() {
			return hints;
		}

		public int getX1() {
			return x1;
		}

		public int getY1() {
			return y1;
		}

		public int getX2() {
			return x2;
		}

		public int getY2() {
			return y2;
		}

		public ModType getType() {
			return type;
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

	PathwayElement curOldElt = null;
	PathwayElement curNewElt = null;
	Map<String, String> curHint = null;

	public void modifyStart (PathwayElement oldElt, PathwayElement newElt)
	{
		curOldElt = oldElt;
		curNewElt = newElt;
		curHint = new HashMap<String, String>();
	}

	public void flush() throws IOException
	{
	}

	public void modifyEnd ()
	{
		VPathwayElement veltOld = findElt (curOldElt, vpwy[PWY_OLD]);
		assert (veltOld != null);
		veltOld.highlight (Color.YELLOW);
		Rectangle2D r1 = veltOld.getVBounds();

		VPathwayElement veltNew = findElt (curNewElt, vpwy[PWY_NEW]);
		assert (veltNew != null);
		veltNew.highlight (Color.YELLOW);
		Rectangle2D r2 = veltNew.getVBounds();

		ModData mod = new ModData (
			(int)vpwy[PWY_OLD].mFromV(r1.getX() + r1.getWidth() / 2),
			(int)vpwy[PWY_OLD].mFromV(r1.getY() + r1.getHeight() / 2),
			(int)vpwy[PWY_NEW].mFromV(r2.getX() + r2.getWidth() / 2),
			(int)vpwy[PWY_NEW].mFromV(r2.getY() + r2.getHeight() / 2),
				curHint, ModData.ModType.CHANGED);

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
			attr.equalsIgnoreCase("centery"))
		{
			String temp =
				curOldElt.getMCenterX() + ", " +
				curOldElt.getMCenterY() + " -> " +
				curNewElt.getMCenterX() + ", " +
				curNewElt.getMCenterY();
			curHint.put ("Center", temp);
		}
		else if (
			attr.equalsIgnoreCase("endx") ||
			attr.equalsIgnoreCase("endy") ||
			attr.equalsIgnoreCase("startx") ||
			attr.equalsIgnoreCase("starty"))
		{
			String temp =
				curOldElt.getMStartX() + ", " +
				curOldElt.getMStartY() + " -> " +
				curNewElt.getMStartX() + ", " +
				curNewElt.getMStartY() + " " +
				curOldElt.getMEndX() + ", " +
				curOldElt.getMEndY() + " -> " +
				curNewElt.getMEndX() + ", " +
				curNewElt.getMEndY();
			curHint.put ("Position", temp);
		}
		else if (
			attr.equalsIgnoreCase("width") ||
			attr.equalsIgnoreCase("height")
			)
		{
			String temp =
				curOldElt.getMWidth() + ", " +
				curOldElt.getMHeight() + "-> " +
				curNewElt.getMWidth() + ", " +
				curNewElt.getMHeight();
			curHint.put ("Size", temp);
		}
		else
		{
			curHint.put (
				attr,
				" \"" + oldVal + "\" -> \"" + newVal + "\""
				);
		}
	}

}