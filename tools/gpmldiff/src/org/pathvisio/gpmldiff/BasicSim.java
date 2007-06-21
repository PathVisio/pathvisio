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

import java.util.*;

/**
   Basic Similarity function
*/
class BasicSim extends SimilarityFunction
{
	/**
	   returns a score between 0 and 100, 100 if both elements are completely similar
	*/
	public int getSimScore (PwyElt e1, PwyElt e2)
	{
		Map<String, String> c1 = e1.getContents();
		Map<String, String> c2 = e2.getContents();

		int n1 = c1.size();
		int n2 = c2.size();

		if (n1 + n2 == 0) return 0; // div by 0 prevention
		
		int score = 0;
		
		for (String key : c1.keySet())
		{
			if (c2.containsKey (key) && c2.get(key).equals (c1.get(key)))
			{
				score += 2;
			}
		}
		
		return (100 * score) / (n1 + n2);
	}
}