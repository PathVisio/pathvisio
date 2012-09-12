// PathVisio,
package org.pathvisio.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;

public interface DataInterface
{
    public ISample getSample(int id) throws DataException;

    public ISample findSample(String name) throws DataException;
    
    public List<String> getSampleNames() throws DataException;

    public List<String> getSampleNames(int dataType) throws DataException;
    
    public List<? extends ISample> getOrderedSamples() throws DataException;

    /**
     * get all datasouces used in this gex.
     * This is used to filter out unneeded xrefs as early as possible, to speed up identifier mapping.
     */
    public Set<DataSource> getUsedDatasources() throws DataException;

    /**
    @deprecated use getIterator() instead
    */
    public IRow getRow(int rowId) throws DataException;

    /**
    @deprecated use getIterator() instead
    */
    public int getNrRow() throws DataException;

	public Map<Integer, ? extends ISample> getSamples() throws DataException;

	public Collection<? extends IRow> getData(Set<Xref> destRefs) throws DataException;

	public Iterable<IRow> getIterator() throws DataException;
	
	public boolean isConnected();

	public String getDbName();

	public void close() throws DataException;
}
