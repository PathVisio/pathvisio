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

import org.pathvisio.visualization.colorset.Criterion.Func;

/**
 * All functions used in the PathVisio expression parser.
 * Designed to be as similar to Excel as possible.
 */
enum Functions
{
	SUM (new Func() {
		public Object call(List<Object> params) 
		{
			double sum = 0;
			for (Object param : params)	sum += (Double)param;
			return sum;
		}
	}),
	SUMSQ (new Func() {
		public Object call(List<Object> params) 
		{
			double sumSq = 0;
			for (Object param : params)	{ double p = (Double)param; sumSq += p * p; }
			return sumSq;
		}
	}),
/*
   //TODO: these functions are commented out because they 
    * need to be compared to Excel equivalents, and unit testing.
 
	VARIANCE (new Func() {
		public Object call(List<Object> params) 
		{
			double n = 0;
			double sum = 0;
			double sumSq = 0;
			for (Object param : params)	
			{
				double p = (Double)param;
				sum += p;
				n+= 1.0;
				sumSq += p * p;
			}
			return (sumSq - (sum * sum / n)) / (n - 1);
		}
	}),
	STDDEV (new Func() {
		public Object call(List<Object> params) 
		{
			double n = 0;
			double sum = 0;
			double sumSq = 0;
			for (Object param : params)	
			{
				double p = (Double)param;
				sum += p;
				n+= 1.0;
				sumSq += p * p;
			}
			return Math.sqrt (sumSq - (sum * sum / n)) / (n);
		}
	}),
	LEFT (new Func() {
		public Object call(List<Object> params) 
		{
			String s = (String)params.get(0);
			double len = (Double)params.get(1);
			return s.substring(0, (int)len);
		}
	}),
	MID (new Func() {
		public Object call(List<Object> params) 
		{
			String s = (String)params.get(0);
			double start = (Double)params.get(1);
			double len = (Double)params.get(2);
			return s.substring((int)start, (int)len);
		}
	}),
	FIND (new Func() {
		public Object call(List<Object> params) 
		{
			String s1 = (String)params.get(0);
			String s2 = (String)params.get(1);
			return new Double(s1.indexOf(s2));
		}
	}),
	LEN (new Func() {
		public Object call(List<Object> params) 
		{
			String s1 = (String)params.get(0);
			return new Double(s1.length());
		}
	}),
	RIGHT (new Func() {
		public Object call(List<Object> params) 
		{
			String s1 = (String)params.get(0);
			double len = (Double)params.get(1);
			return s1.substring(s1.length() - (int)len);
		}
	}),
	//TODO: TTEST
	*/
	AVERAGE(new Func() {
		public Object call(List<Object> params) 
		{
			double sum = 0;
			for (Object param : params)	sum += (Double)param;
			return sum / params.size();
		}
	}),
	MAX(new Func() {
		public Object call(List<Object> params) 
		{
			Double max = null;
			for (Object param : params)	
				if (max == null || (Double)param > max) max = (Double)param; 
			return max;
		}
	}),
	MIN(new Func() {
		public Object call(List<Object> params) 
		{
			Double min = null;
			for (Object param : params)	
				if (min == null || (Double)param < min) min = (Double)param; 
			return min;
		}
	}),
	LOG (new Func() {
		public Object call(List<Object> params) 
		{
			double number = (Double)params.get(0);
			double base = (Double)params.get(1);
			return (Math.log (number) / Math.log (base));
		}
	}),
	POWER (new Func() {
		public Object call(List<Object> params) 
		{
			double number = (Double)params.get(0);
			double base = (Double)params.get(1);
			return Math.pow (number, base);
		}
	}),
	EXP (new Func() {
		public Object call(List<Object> params) 
		{
			double number = (Double)params.get(0);
			return Math.exp (number);
		}
	}),
	SIN (new Func() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.sin (arg);
		}
	}),
	COS (new Func() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.cos (arg);
		}
	}),
	SQRT (new Func() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.sqrt (arg);
		}
	}),
	ROUND (new Func() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.round (arg);
		}
	}),
	CEILING (new Func() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.ceil (arg);
		}
	}),
	FLOOR (new Func() {
		public Object call(List<Object> params) 
		{
			double arg = (Double)params.get(0);
			return Math.floor (arg);
		}
	}),
	LOG10 (new Func() {
		public Object call(List<Object> params) 
		{
			double number = (Double)params.get(0);
			return (Math.log10 (number));
		}
	}),
	IF (new Func() {
		public Object call(List<Object> params) 
		{
			Boolean condition = (Boolean)params.get(0);
			if (condition) return params.get(1); else return params.get(2);
		}
	}),
	ABS (new Func() {
		public Object call(List<Object> params) 
		{
			return Math.abs((Double)params.get(0));
		}
	}),
	CONCATENATE (new Func() {
		public Object call(List<Object> params) 
		{
			StringBuilder builder = new StringBuilder();
			for (Object param : params) builder.append((String)param);
			return builder.toString();
		}
	}),
	TRIM (new Func() {
		public Object call(List<Object> params) 
		{
			return ((String)params.get(0)).trim();
		}
	}),
	ISNUMBER (new Func() {
		public Object call(List<Object> params) 
		{
			return (params.get(0) instanceof Double);
		}
	}),
	
	;
	
	Functions(Func def)
	{
		this.def = def;
	}
	
	final Func def;
}