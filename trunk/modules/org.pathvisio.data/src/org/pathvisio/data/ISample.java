// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.data;

/**
 * A base interface for a Sample, 
 * which represents a column in a gene expression matrix or other high-throughput dataset.\
 * <p>
 * Note that the word Sample is used in a very broad sense, it really corresponds to a column in your expression matrix.
 */
public interface ISample extends Comparable<ISample>
{
	/** a human readable name for this sample, corresponding to a column header in your expression matrix */
	public String getName();
	
	/** a numeric id for this sample. Note that a set of samples does not have to be numbered consecutively */
	public Integer getId();
	
	/** Return one of the ISample values */
	public int getDataType();
	
	/** A string that divides samples into groups, e.g. "treated" or "untreated". 
	 * If multiple factors play a role, they should be separated by ':', for example
	 * "wildtype:treated" or "mutant:untreated"  */
	public String getFactor();
	
	public static final int NUMBER_EXPR  = 1701; /* a java.lang.Number representing an expression value or a mean of expression values */
	public static final int NUMBER_DEV   = 1702; /* a java.lang.Number representing a standard deviation or standard error, relative to an expression value */ 
	public static final int NUMBER_RATIO = 1703; /* a java.lang.Number representing a ratio, or a mean of ratio's, such as a fold-change value */
	public static final int NUMBER_LOG   = 1704; /* a java.lang.Number representing a log-transformed value, such as a log fold-change */
	public static final int NUMBER_PVAL  = 1704; /* a java.lang.Number representing a pvalue, such as the result of a ttest or anova. Use this both for raw pvalues and multiple-testing-corrected pvalues */
	public static final int NON_NUMBER   = 1705; /* any non-number type, such as a String type */ 
	public static final int FACTOR	     = 1706; /* either a String or number representing a category or a cluster */
}
