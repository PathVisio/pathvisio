package org.pathvisio.data;

/**
 * A base interface for a Sample, 
 * which represents a column in a gene expression matrix or other high-throughput dataset.\
 * <p>
 * Note that the word Sample is used in a very broad sense. 
 */
public interface ISample extends Comparable<ISample>
{
	public String getName();
	public Integer getId();
	
	/* Return one of the ISample values */
	public int getDataType();
	
	/* A string that divides samples into groups, e.g. "treated" or "untreated". 
	 * If multiple factors play a role, they should be separated by ':', for example
	 * "wildtype:treated" or "mutant:untreated"  */
	public String getFactor();
	
	public static final int NUMBER_EXPR  = 1701; /* a java.lang.Number representing an expression value or a mean of expression values */
	public static final int NUMBER_DEV   = 1702; /* a java.lang.Number representing a standard deviation or standard error, relative to an expression value */ 
	public static final int NUMBER_RATIO = 1703; /* a java.lang.Number representing a ratio, or a mean of ratio's, such as a fold-change value */
	public static final int NUMBER_LOG   = 1704; /* a java.lang.Number representing a log-transformed value, such as a log fold-change */
	public static final int NUMBER_PVAL  = 1704; /* a java.lang.Number representing a pvalue, such as the result of a ttest or anova. Use this both for raw pvalues and multiple-testing-corrected pvalues */
	public static final int NON_NUMBER   = 1705; /* any non-number type, such as a String type */ 
}
