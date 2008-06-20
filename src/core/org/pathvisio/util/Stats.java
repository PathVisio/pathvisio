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
package org.pathvisio.util;

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
	public static double zscore (int i_n, double i_r, int i_N, int i_R)
	{
		double n = (double)i_n;
		double r = (double)i_r;
		double N = (double)i_N;
		double R = (double)i_R;
		
		double f1 = r - (n * (R / N));
		double f2 = R / N;
		double f3 = 1.0 - (R / N);
		double f4 = 1.0 - ((n - 1) / (N - 1));
		
		return f1 / Math.sqrt (i_n * f2 * f3 * f4);
	}
}
