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