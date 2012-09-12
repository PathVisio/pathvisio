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
package org.pathvisio.desktop.gex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.debug.ThreadSafe;
import org.pathvisio.core.debug.WorkerThreadOnly;
import org.pathvisio.data.DataException;
import org.pathvisio.data.DataInterface;
import org.pathvisio.data.IRow;
import org.pathvisio.data.ISample;

/**
 * This class represents cached expression data for a pathway.
 * The caching of expression data will occur when a pathway is opened and an expression dataset is loaded.
 * The cache will be refreshed when another dataset is selected, another gene database is selected or another
 * pathway is opened.
 * A CachedData object will contain a list of {@link ReporterData} object for every gene-product on the pathway for
 * which data is available in the expression dataset
 */
public class CachedData
{
	//Data objects for gene-products on the pathway
	private final ConcurrentHashMap<Xref, List<IRow>> data = new ConcurrentHashMap<Xref, List<IRow>>();
	private final Executor executor;

	private final DataInterface parent;

	/**
	 * Do not instantiate in the PathVisio environment!
	 * Use GexManager.getCachedData() instead.
	 * Or you'll end up with multiple caches.
	 */
	public CachedData (DataInterface parent)
	{
		this.parent = parent;
		executor = Executors.newSingleThreadExecutor();
	}

	/**
	 * Check whether the cached data contains data for the given gene-product
	 * @param pwId The IdCodePair that represents the gene-product
	 * @return true if data is available for the gene-product, false if not
	 */
	public boolean hasData(Xref pwId) {
		return data.containsKey(pwId);
	}

	/**
	 * Get the cached data the given gene-product. If there was nothing in cache
	 * it returns 0. Should be used only in combination with asyncGet.
	 * @param idc The Xref for which the data has to be returned
	 * @return a list of {@link ReporterData} object containing the cached data, or null when no data is available
	 */
	public List<? extends IRow> getData(Xref idc) {
		return data.get(idc);
	}

	public interface Callback
	{
		public void callback();
	}

	private volatile int tasks = 0;

	// called from two different threads
	@ThreadSafe
	private void updateTasks (int delta)
	{
		int oldtasks = tasks;
		tasks += delta;
		if (oldtasks == 0 || tasks == 0)
		{
			Logger.log.info ("CACHE: " + (tasks == 0 ? "STOPPED" : "STARTED"));
		}
	}

	// set of data sources that occur in SimpleGex.
	private Set<DataSource> destFilterCache = null;

	@WorkerThreadOnly
	public List<? extends IRow> syncGet(Xref ref) throws IDMapperException, DataException
	{
		if (destFilterCache == null)
		{
			destFilterCache = parent.getUsedDatasources();
		}

		List<IRow> result;
		if (!data.containsKey (ref))
		{
			// get results and sort them
			result = new ArrayList<IRow>();
			Collection <? extends IRow> collection = getDataForXref(ref, mapper, destFilterCache);
			if (collection != null) result.addAll(collection);
			Collections.sort(result);
			data.put (ref, result);
		}
		else
		{
			result = data.get(ref);
		}
		return result;
	}

	public void asyncGet(final Xref ref, final Callback callback)
	{
		updateTasks (+1);
		executor.execute (new Runnable()
		{
			public void run()
			{
				try {
					syncGet(ref);
				} catch (IDMapperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (DataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				updateTasks (-1);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					callback.callback();
				}});
			}
		});
	}

	private Collection<? extends IRow> getDataForXref(Xref srcRef, IDMapper gdb, Set<DataSource> destFilter) throws IDMapperException, DataException
	{
		// get all cross-refs for this id
		Set<Xref> destRefs = new HashSet<Xref>();
		if (gdb.isConnected() && srcRef.getId() != null && srcRef.getDataSource() != null)
		{
			for (Xref destRef : gdb.mapID(srcRef))
			{
				// add only the ones that are in the dest filter.
				if (destFilter.contains(destRef.getDataSource()))
				{
					destRefs.add(destRef);
				}

			}
		}
		// also the srcRef, in case we can't look up cross references
		if (destFilter.contains(srcRef.getDataSource()))
		{
			destRefs.add(srcRef);
		}

		if(destRefs.size() > 0)
		{
			return parent.getData(destRefs);
		}
		else
			return Collections.emptyList();
	}

	private IDMapper mapper = null;

	/**
	 * Set the mapper that is used for ID Mapping. Clears cache if necessary.
	 * TODO In the future I want to set this in the constructor, and
	 * make mapper final.
	 */
	public void setMapper(IDMapper mapper)
	{
		if (this.mapper != mapper)
		{
			this.mapper = mapper;
			clearCache();
		}
	}

	/**
	 * Starts loading expression data for all the given gene ids into memory
	 * @param srcRefs Xrefs to cache the expression data for
	 * 	(typically all genes and metabolites in a pathway)
	 */
	public void preSeed(Collection<Xref> srcRefs) throws DataException
	{
		// seed samples cache
		parent.getSamples();

		//TODO preseed cache with srcRefs in background.
		// somehow with lower priority than the ones for visualization
	}

	@WorkerThreadOnly
	/**
	 * Load expression data for given Xrefs into cache.
	 * Waits until the process is done.
	 */
	public void syncSeed(Collection<Xref> srcRefs) throws DataException, IDMapperException
	{
		// seed samples cache
		parent.getSamples();

		for (Xref ref : srcRefs)
		{
			syncGet(ref);
		}
	}

	public void clearCache()
	{
		data.clear();
	}

	public String getDbName()
	{
		return parent.getDbName();
	}

	/** @deprecated use getIterator() instead */
	public int getNrRow() throws DataException
	{
		return parent.getNrRow();
	}

	/** @deprecated use getIterator() instead */
	public IRow getRow(int i) throws DataException
	{
		return parent.getRow(i);
	}

	public Iterable<IRow> getIterator() throws DataException
	{
		return parent.getIterator();
	}
	
	public boolean isConnected()
	{
		return parent.isConnected();
	}

	List<? extends ISample> getOrderedSamples() throws DataException
	{
		return parent.getOrderedSamples();
	}

	public void dispose()
	{
		((ExecutorService)executor).shutdown();
	}
}
