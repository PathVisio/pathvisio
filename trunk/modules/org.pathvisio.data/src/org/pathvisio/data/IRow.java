package org.pathvisio.data;

import java.util.Collection;
import java.util.Map;

import org.bridgedb.Xref;

/**
 * Corresponds to a row in an expression matrix.
 * Usually, each row is identified by a microarray reporter or some other probe, identified by an Xref.
 * However, note that an Xref doesn't need to be unique. Some data platforms measure the same probe multiple times.
 * In those cases, rows may be distinguished by different group Ids.
 * <p>
 */
public interface IRow extends Comparable<IRow>
{
	/**
	 * Reporter Xref, e.g. Affy, Illumina id for microarray data, or Uniprot id for proteomics data.
	 */
	Xref getXref();
	
	/**
	 * Data value for the given Sample
	 */
	Object getSampleData(ISample iSample);
	
	/**
	 * get all data mapped to sample name.
	 * @deprecated this method will be removed in the future. Instead use getSamples() to get the keys of this map, 
	 * and getSampleData() to get the individual values.
	 */
	Map<String, Object> getByName();
	
	/**
	 * Get all samples
	 */
	Collection<? extends ISample> getSamples();
	
	/** Used to distinguish rows that have the same Xref */
	int getGroup();
}
