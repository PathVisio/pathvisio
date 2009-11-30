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
package org.pathvisio.util;

/**
 * Utility functions for statistics calculations.
 */
public class Stats
{
	/**
	 * Calculate z-score based on the hypergeometric distribution,
	 * where
	 * 	n = total beads sampled
	 *  r = red beads sampled
	 *  N = total beads
	 *  R = total red beads
	 *
	 * resulting score == 0 if the sampled beads are in the same ratio as the total beads
	 */
	public static double zscore (int an, double ar, int aBigN, int aBigR)
	{
		double n = (double)an;
		double r = (double)ar;
		double bigN = (double)aBigN;
		double bigR = (double)aBigR;

		double f1 = r - (n * (bigR / bigN));
		double f2 = bigR / bigN;
		double f3 = 1.0 - (bigR / bigN);
		double f4 = 1.0 - ((n - 1) / (bigN - 1));

		return f1 / Math.sqrt (an * f2 * f3 * f4);
	}
}
