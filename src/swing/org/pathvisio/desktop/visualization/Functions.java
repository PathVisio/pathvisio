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
package org.pathvisio.desktop.visualization;

import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.pathvisio.desktop.visualization.Criterion.Operation;

/**
 * All functions used in the PathVisio expression parser.
 * Designed to be as similar to Excel as possible.
 */
enum Functions implements Operation
{
	SUM(0, "SUM(numbers...): sum of all parameters. Example: SUM(1,2,3) -> 6") {
		public Object call(List<Object> params)
		{
			double sum = 0;
			for (Object param : params)	sum += (Double)param;
			return sum;
		}
	},
	SUMSQ(0, "SUMSQ(numbers...): sum of the squares of all parameters. Example: SUMSQ(1, 2, 3) -> 14") {
		public Object call(List<Object> params)
		{
			double sumSq = 0;
			for (Object param : params)	{ double p = (Double)param; sumSq += p * p; }
			return sumSq;
		}
	},
	VAR (0, "VAR(numbers...): variance of the parameters, defined as the squared deviation from the mean, divided by degrees of freedom.")
	{
		public Object call(List<Object> params)
		{
			double[] doubles = toDoublesArray(params);
			return 	StatUtils.variance(doubles);
		}
	},
	STDEV (0, "STDEV(numbers...): standard deviation of the parameters, defined as the square root of the variance")
	{
		public Object call(List<Object> params)
		{
			double[] doubles = toDoublesArray(params);
			return Math.sqrt (StatUtils.variance(doubles));
		}
	},
	ARRAY (0, "ARRAY(numbers...): Turns a series of objects into an array object, required for the TTEST function") {
		public Object call(List<Object> params)
		{
			return params;
		}
	},
	TTEST (4, "TTEST(array, array, tails, type): Student's T-Test. " +
			"Parameters 1 and 2 are each an ARRAY of measures. " +
			"The third parameter is either 1 for a one-tailed test or 2 for a two-tailed test. " +
			"The fourth parameter is 1 for a paired t-test, 2 for a homoscedastic (equal variance) " +
			" test or 3 for a t-test with non-equal variances. " +
			"Example: TTEST(ARRAY(1,2),ARRAY(3,4),2,1")
	{
		public Object call(List<Object> params)
		{
			double[] doubles1 = toDoublesArray((List<?>)params.get(0));
			double[] doubles2 = toDoublesArray((List<?>)params.get(1));
			boolean twoTailed = ((Double)params.get(2) == 2);
			double type = (Double)params.get(3);
			TTest ttest = new TTestImpl();
			double result = 0;
			try
			{
				switch ((int)type)
				{
				case 1: // paired
					result = ttest.pairedTTest(doubles1, doubles2);
					break;
				case 2: // homoscedastic
					result = ttest.homoscedasticTTest(doubles1, doubles2);
					break;
				case 3: // unequal population variance
					result = ttest.tTest(doubles1, doubles2);
					break;
				}
			}
			catch (MathException ex)
			{  // make this a runtime error
				throw new IllegalArgumentException (ex);
			}
			if (!twoTailed) result /= 2;
			return result;
		}
	},
 	LEFT(2, "LEFT(string, length): left part of a string. Example: LEFT(\"abc\",2) -> \"ab\"")
 	{
		public Object call(List<Object> params)
		{
			String s = (String)params.get(0);
			double len = (Double)params.get(1);
			return s.substring(0, (int)len);
		}
	},
	MID(3, "MID(string, start, length): middle part of a string. Example: MID(\"abc\", 2, 1) -> \"b\"") {
		public Object call(List<Object> params)
		{
			String s = (String)params.get(0);
			double start = (Double)params.get(1);
			double len = (Double)params.get(2);

			return s.substring((int)start - 1, (int)(start + len - 1));
		}
	},
	FIND (2, "FIND(query, string [, start]): looks for a substring in a string. " +
			"First parameter is the string to search for, " +
			"second is the string to search in. An optional third parameter determines the start " +
			"position. Example: FIND(\"ss\", \"mississippi\") -> 3") {
		public Object call(List<Object> params)
		{
			String s1 = (String)params.get(0);
			String s2 = (String)params.get(1);
			double start = 1;
			if (params.size() > 2) start = (Double)params.get(2);
			return new Double(s2.indexOf(s1, (int)start - 1) + 1);
		}
	},
	LEN(1, "LEN(string): Returns the length of a string") {
		public Object call(List<Object> params)
		{
			String s1 = (String)params.get(0);
			return new Double(s1.length());
		}
	},
	RIGHT(1, "RIGHT (string [, length]): the right part of a string. " +
			"The first parameter is the input string, " +
			"the optional second parameter determines the length to return. Example: " +
			"RIGHT(\"abcde\",2) -> \"de\"")
	{
		public Object call(List<Object> params)
		{
			String s1 = (String)params.get(0);
			double len = 1;
			if (params.size() > 1) len = (Double)params.get(1);
			return s1.substring(s1.length() - (int)len);
		}
	},
	AVERAGE(0, "AVERAGE(numbers...): Average of a list of values. Example: AVERAGE(3,4,5) -> 4") {
		public Object call(List<Object> params)
		{
			double sum = 0;
			for (Object param : params)	sum += (Double)param;
			return sum / params.size();
		}
	},
	MAX(0, "MAX(numbers...): Maximum of a list of values. Example: MAX(3,4,5) -> 5")
	{
		public Object call(List<Object> params)
		{
			Double max = null;
			for (Object param : params)
				if (max == null || (Double)param > max) max = (Double)param;
			return max;
		}
	},
	MIN(0, "MIN(numbers...): Minimum of a list of values. Example: MIN(3,4,5) -> 3") {
		public Object call(List<Object> params)
		{
			Double min = null;
			for (Object param : params)
				if (min == null || (Double)param < min) min = (Double)param;
			return min;
		}
	},
	LOG(2, "LOG(number, base): Calculate logarithm. Example: LOG(64,2) -> 6") {
		public Object call(List<Object> params)
		{
			double number = (Double)params.get(0);
			double base = (Double)params.get(1);
			return (Math.log (number) / Math.log (base));
		}
	},
	POWER(2, "POWER(number, base): Raises a number to a power. Example: POWER(2,3) -> 8") {
		public Object call(List<Object> params)
		{
			double number = (Double)params.get(0);
			double base = (Double)params.get(1);
			return Math.pow (number, base);
		}
	},
	EXP(1, "EXP(number): Calculates the exponent, i.e. a power of e") {
		public Object call(List<Object> params)
		{
			double number = (Double)params.get(0);
			return Math.exp (number);
		}
	},
	SIN(1, "SIN(number): sine of a number in radians")
	{
		public Object call(List<Object> params)
		{
			double arg = (Double)params.get(0);
			return Math.sin (arg);
		}
	},
	COS(1, "COS(number): cosine of a number in radians") {
		public Object call(List<Object> params)
		{
			double arg = (Double)params.get(0);
			return Math.cos (arg);
		}
	},
	SQRT(1, "SQRT(number): square root of a number") {
		public Object call(List<Object> params)
		{
			double arg = (Double)params.get(0);
			return Math.sqrt (arg);
		}
	},
	//TODO: 2nd param of ROUND: digits
	ROUND (1, "ROUND(number): Rounds a number to the nearest whole integer") {
		public Object call(List<Object> params)
		{
			double arg = (Double)params.get(0);
			return Math.round (arg);
		}
	},
	//TODO: 2nd param of CEILING: significance
	CEILING(1, "CEILING(number): round a number up") {
		public Object call(List<Object> params)
		{
			double arg = (Double)params.get(0);
			return Math.ceil (arg);
		}
	},
	//TODO: 2nd param of FLOOR: significance
	FLOOR(1, "FLOOR(number): round a number down") {
		public Object call(List<Object> params)
		{
			double arg = (Double)params.get(0);
			return Math.floor (arg);
		}
	},
	LOG10(1, "LOG10(number): Base-10 logarithm") {
		public Object call(List<Object> params)
		{
			double number = (Double)params.get(0);
			return (Math.log10 (number));
		}
	},
	IF(3, "IF(condition,true value,false value): Make a decision. " +
			"Returns true-value or false-value depending on the condition. " +
			"Example: IF(2 > 1, \"greater\", \"less\") -> \"greater\"") {
		public Object call(List<Object> params)
		{
			Boolean condition = (Boolean)params.get(0);
			if (condition) return params.get(1); else return params.get(2);
		}
	},
	ABS(1, "ABS(number): the absolute value") {
		public Object call(List<Object> params)
		{
			return Math.abs((Double)params.get(0));
		}
	},
	CONCATENATE(0, "CONCATENATE(strings...): joins strings together") {
		public Object call(List<Object> params)
		{
			StringBuilder builder = new StringBuilder();
			for (Object param : params) builder.append((String)param);
			return builder.toString();
		}
	},
	TRIM(1, "TRIM(string): removes whitespace at the start or end of a string") {
		public Object call(List<Object> params)
		{
			return ((String)params.get(0)).trim();
		}
	},
	ISNUMBER(1, "ISNUMBER(value): returns TRUE if the value is a number") {
		public Object call(List<Object> params)
		{
			return (params.get(0) instanceof Double);
		}
	},

	;

	private final int minArgs;
	private final String help;

	Functions(int minArgs, String help)
	{
		this.minArgs = minArgs;
		this.help = help;
	}

	public int getMinArgs()
	{
		return minArgs;
	}

	// helper
	private static double[] toDoublesArray (List<?> list)
	{
		double[] result = new double[list.size()];
		for (int i = 0; i < list.size(); ++i)
		{
			result[i] = (Double)list.get(i);
		}
		return result;
	}
}