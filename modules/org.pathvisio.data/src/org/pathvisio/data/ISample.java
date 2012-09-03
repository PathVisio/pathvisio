package org.pathvisio.data;

/**
 * A base interface for a Sample, 
 * which represents a column in a gene expression matrix or other high-throughput dataset.
 */
public interface ISample extends Comparable<ISample>
{
	public String getName();
	public Integer getId();
	public int getDataType();
}
