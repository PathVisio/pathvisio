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

package org.pathvisio.gpmldiff;

import java.util.Map;

import org.pathvisio.model.PathwayElement;

/**
   Basic Similarity function
*/
class BasicSim extends SimilarityFunction
{
	/**
	   returns a score between 0 and 100, 100 if both elements are completely similar
	*/
	public int getSimScore (PathwayElement oldE, PathwayElement newE)
	{
		Map<String, String> oldC = PwyElt.getContents(oldE);
		Map<String, String> newC = PwyElt.getContents(newE);

		int oldN = oldC.size();
		int newN = newC.size();

		if (oldN + newN == 0) return 0; // div by 0 prevention

		int score = 0;

		for (String key : oldC.keySet())
		{
			if (newC.containsKey (key) && newC.get(key).equals (oldC.get(key)))
			{
				score += 2;
			}
		}

		return (100 * score) / (oldN + newN);
	}
}