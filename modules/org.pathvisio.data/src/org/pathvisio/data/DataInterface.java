// PathVisio,
package org.pathvisio.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;

/**
 * Generic interface for accessing high-throughput data
 */
public interface DataInterface
{
	/**
	 * Get a sample by numeric id. Note that the ids do not have to be sequential, i.e. there may be gaps.
	 */
    public ISample getSample(int id) throws DataException;

    /**
     * Get a sample by string id.
     */
    public ISample findSample(String name) throws DataException;
    
    /**
     * Get all sample names
     */
    public List<String> getSampleNames() throws DataException;

    /**
     * Get the sample names, filtered by Samples that have the given datatype. 
     */
    public List<String> getSampleNames(int dataType) throws DataException;
    
    /**
     * Get all samples ordererd by Id. Note that the ids are not sequential, i.e. there may be gaps.
     */
    public List<? extends ISample> getOrderedSamples() throws DataException;

    /**
     * get all datasouces used in this gex.
     * This is used to filter out unneeded xrefs as early as possible, to speed up identifier mapping.
     */
    public Set<DataSource> getUsedDatasources() throws DataException;

    /**
     * Get a row with the given row number
     * @deprecated use getIterator() instead
    */
    public IRow getRow(int rowId) throws DataException;

    /**
     * Get the total number of rows. Note that some implementations may choose not to implement this
     * for efficiency reasons.
     * @deprecated use getIterator() instead
    */
    public int getNrRow() throws DataException;

    /**
     * Get a map of samples.
     * @deprecated instead use getOrderedSamples() to get all keys, and getSample(int) to look up individual samples.
     */
	public Map<Integer, ? extends ISample> getSamples() throws DataException;

	/**
	 * Get all data for given set of Xrefs. Note that there may be multiple rows
	 * returned per Xref.
	 */
	public Collection<? extends IRow> getData(Set<Xref> destRefs) throws DataException;

	/**
	 * Get an iterator, that allows you to loop through all rows in this dataset.
	 */
	public Iterable<IRow> getIterator() throws DataException;
	
	/**
	 * Return true if the backend is working properly
	 */
	public boolean isConnected();

	/**
	 * Get the file name of this data interface, or a descriptive string in case this is not a file-based implementation.
	 */
	public String getDbName();

	/**
	 * Cleanly shut down whatever backend may be behind this interface.
	 */
	public void close() throws DataException;
}
