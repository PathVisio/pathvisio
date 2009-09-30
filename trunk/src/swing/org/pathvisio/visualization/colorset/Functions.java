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
package org.pathvisio.visualization.colorset;

import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.pathvisio.visualization.colorset.Criterion.Operation;

/**
 * All functions used in the PathVisio expression parser.
 * Designed to be as similar to Excel as possible.
 */
enum Functions implements Operation
{
	SUM() {
		public Object call(List<Object> params) 
		{
			double sum = 0;
			for (Object param : params)	sum += (Double)param;
			return sum;
		}
	},
	SUMSQ() {
		public Object call(List<Object> params) 
		{
			double sumSq = 0;
			for (Object param : params)	{ double p = (Double)param; sumSq += p * p; }
			return sumSq;
		}
	},
	VAR (){
		public Object call(List<Object> params) 
		{
			double[] doubles = toDoublesArray(params);
			return 	StatUtils.variance(doubles);
		}
	},
	STDEV () {
		public Object call(List<Object> params) 
		{
			double[] doubles = toDoublesArray(params);
			return Math.sqrt (StatUtils.variance(doubles));
		}
	},
	ARRAY () {
		public Object call(List<Object> params) 
		{
			return params;
		}
	},
	TTEST () {
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
 	LEFT() {
		public Object call(List<Object> params) 
		{
			String s = (String)params.get(0);
			double len = (Double)params.get(1);
			return s.substring(0, (int)len);
		}
	},
	MID() {
		public Object call(List<Object> params) 
		{
			String s = (String)params.get(0);
			double start = (Double)params.get(1);
			double len = (Double)params.get(2);
			
			return s.substring((int)start - 1, (int)(start + len - 1));
		}
	},
	FIND () {
		public Object call(List<Object> params) 
		{
			String s1 = (String)params.get(0);
			String s2 = (String)params.get(1);
			double start = 1;
			if (params.size() > 2) start = (Double)params.get(2);
			return new Double(s2.indexOf(s1, (int)start - 1) + 1);
		}
	},
	LEN() {
		public Object call(List<Object> params) 
		{
			String s1 = (String)params.get(0);
			return new Double(s1.length());
		}
	},
	RIGHT() {
		public Object call(List<Object> params) 
		{
			String s1 = (String)params.get(0);
			double len = 1;
			if (params.size() > 1) len = (Double)params.get(1);
			return s1.substring(s1.length() - (int)len);
		}
	},
	AVERAGE() {
		public Object call(List<Object> params) 
		{
			double sum = 0;
			for (Object param : params)	sum += (Double)param;
			return sum / params.size();
		}
	},
	MAX() {
		public Object call(List<Object> params) 
		{
			Double max = null;
			for (Object param : params)	
				if (max == null || (Double)param > max) max = (Double)param; 
			return max;
		}
	},
	MIN() {
		public Object call(List<Object> params) 
		{
			Double min = null;
			for (Object param : params)	
				if (min == null || (Double)param < min) min = (Double)param; 
			return min;
		}
	},
	LOG() {
		public Object call(List<Object> params) 
		{
			double number = (Double)params.get(0);
			double base = (Double)params.get(1);
			return (Math.log (number) / Math.log (base));
		}
	},
	POWER() {
		public Object call(List<Object> params) 
		{
			double number = (Double)params.get(0);
			double base = (Double)params.get(1);
			return Math.pow (number, base);
		}
	},
	EXP() {
		public Object call(List<Object> params) 
		{
			double number = (Double)params.get(0);
			return Math.exp (number);
		}
	},
	SIN() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.sin (arg);
		}
	},
	COS() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.cos (arg);
		}
	},
	SQRT() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.sqrt (arg);
		}
	},
	//TODO: 2nd param of ROUND: digits
	ROUND () {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.round (arg);
		}
	},
	//TODO: 2nd param of CEILING: significance
	CEILING() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.ceil (arg);
		}
	},
	//TODO: 2nd param of FLOOR: significance
	FLOOR() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.floor (arg);
		}
	},
	LOG10() {
		public Object call(List<Object> params) 
		{
			double number = (Double)params.get(0);
			return (Math.log10 (number));
		}
	},
	IF() {
		public Object call(List<Object> params) 
		{
			Boolean condition = (Boolean)params.get(0);
			if (condition) return params.get(1); else return params.get(2);
		}
	},
	ABS() {
		public Object call(List<Object> params) 
		{
			return Math.abs((Double)params.get(0));
		}
	},
	CONCATENATE() {
		public Object call(List<Object> params) 
		{
			StringBuilder builder = new StringBuilder();
			for (Object param : params) builder.append((String)param);
			return builder.toString();
		}
	},
	TRIM() {
		public Object call(List<Object> params) 
		{
			return ((String)params.get(0)).trim();
		}
	},
	ISNUMBER() {
		public Object call(List<Object> params) 
		{
			return (params.get(0) instanceof Double);
		}
	},
	
	;	
	
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