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

import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PropertyType;
import org.pathvisio.debug.Logger;
import java.util.*;

class BetterSim extends SimilarityFunction
{

	/**
	   returns a score between 0 and 100, 100 if both elements are completely similar
	*/
	public int getSimScore (PathwayElement oldE, PathwayElement newE)
	{
		int oldOt = oldE.getObjectType();
		int newOt = newE.getObjectType();
		List<PropertyType> oldProps = oldE.getAttributes(true);
		List<PropertyType> newProps = newE.getAttributes(true);

		int oldN = oldProps.size();
		int newN = newProps.size();

		int possibleScore = 0;
		int actualScore = 0;

		for (PropertyType newProp : newProps)
		{
			if (oldProps.contains(newProp))
			{
				Object oo = oldE.getProperty(newProp);
				Object no = newE.getProperty(newProp);
				
				int max = 0;
				int score = 0;
				
				switch (newProp) 
				{
					case GRAPHID:
					case GROUPID:
						max = 80;
						if (oo == null ? no == null : oo.equals (no))
						{
							score = max;
						}
						break;
					case CENTERX:
					case CENTERY:
					case ENDX:
					case ENDY:
					case STARTX:		
					case STARTY:
						max = 10;
						double delta = (Double)oo - (Double)no;
						if (delta < 0.5)
						{
							score = 10;
						}
						else if (delta >= 0.5 && delta < 512)
						{
							score = 9 - (int)(Math.log10 (delta) / Math.log10 (2));
						}
						else
						{
							score = 0;
						}							
						break;
					default:
						max = 10;
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