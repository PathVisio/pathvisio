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

import java.util.Set;

import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.StaticProperty;

/**
 * An implementation of the SimilarityFunction that compares elements,
 * that was written after BasicSim and should be more finely tuned
 */
public class BetterSim extends SimilarityFunction
{

	/**
	   returns a score between 0 and 100, 100 if both elements are completely similar
	*/
	public int getSimScore (PathwayElement oldE, PathwayElement newE)
	{
		ObjectType oldOt = oldE.getObjectType();
		ObjectType newOt = newE.getObjectType();
		Set<StaticProperty> oldProps = oldE.getStaticPropertyKeys();
		Set<StaticProperty> newProps = newE.getStaticPropertyKeys();

		int oldN = oldProps.size();
		//int newN = newProps.size();

		int possibleScore = 0;
		int actualScore = 0;


		{
			int max;
			if (oldOt == ObjectType.LEGEND || oldOt == ObjectType.INFOBOX)
			{
				max = 80;
			}
			else
			{
				max = 20;
			}
			int score = 0;
			if (oldOt == newOt)
			{
				score = max;
			}
			possibleScore += max;
			actualScore += score;
		}

		for (StaticProperty newProp : newProps)
		{
			if (oldProps.contains(newProp))
			{
				Object oo = oldE.getStaticProperty(newProp);
				Object no = newE.getStaticProperty(newProp);

				int max = 0;
				int score = 0;

				switch (newProp)
				{
				case BOARDWIDTH:
				case BOARDHEIGHT:
					max = 100 / oldN;
					score = max;
					// since these two are calculated dynamically, ignore them.
					break;
				case GRAPHID:
				case GROUPID:
						max = 600 / oldN;
						if (oo == null ? no == null : oo.equals (no))
						{
							score = max;
						}
						break;
				case STARTX:
				case ENDX:
					// disregard x coords, we do the calculations for y coords.
					break;
				case STARTY:
				{
					max = 300 / oldN;
					double dx = newE.getMStartX() - oldE.getMStartX();
					double dy = newE.getMStartY() - oldE.getMStartY();
					double dist = Math.sqrt (dx * dx + dy * dy);
					if (dist < 1)
					{
						score = max;
					}
					else if (dist < 10)
					{
						score = max * 3 / 4;
					}
					else if (dist < 100)
					{
						score = max / 2;
					}
					else if (dist < 1000)
					{
						score = max / 4;
					}
					else
					{
						score = 0;
					}
				}
					break;
				case ENDY:
				{
					max = 300 / oldN;
					double dx = newE.getMEndX() - oldE.getMEndX();
					double dy = newE.getMEndY() - oldE.getMEndY();
					double dist = Math.sqrt (dx * dx + dy * dy);
					if (dist < 1)
					{
						score = max;
					}
					else if (dist < 10)
					{
						score = max * 3 / 4;
					}
					else if (dist < 100)
					{
						score = max / 2;
					}
					else if (dist < 1000)
					{
						score = max / 4;
					}
					else
					{
						score = 0;
					}
				}
					break;
				case CENTERX:
				case CENTERY:
						max = 100 / oldN;
						double delta = (Double)oo - (Double)no;
						if (delta < 0.5)
						{
							score = max;
						}
						else if (delta >= 0.5 && delta < 512)
						{
							score = ((9 - (int)(Math.log10 (delta) / Math.log10 (2))) * max) / 10;
						}
						else
						{
							score = 0;
						}
						break;
					default:
						if (oo != null && no != null)
						{
							max = 100 / oldN;
						}
						else
						{
							max = 50 / oldN;
						}
						if (oo == null ? no == null : oo.equals (no))
						{
							score = max;
						}
						break;
				}

				possibleScore += max;
				actualScore += score;
			}
		}

		if (possibleScore == 0) return 0; // div by zero prevention
		return 100 * actualScore / possibleScore;
	}
}