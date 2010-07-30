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
package org.pathvisio.gui.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.gui.swing.propertypanel.DataSourceHandler;

/**
 * Stick this into a ComboBox to let the user
 * select a {@link DataSource}.
 *
 * By default this is based on all registered {@link DataSource}'s,
 * but optionally the model can be filtered to show only primary
 * data sources, only metabolites or only a certain organism.
 *
 * NB: if you want to use a JComboBox with a filtered list
 * (to narrow down the number of choices for the user)
 * but you still want to allow
 * programmatic selection of any DataSource, you need to
 * either make the {@link JComboBox} editable, or use a
 * {@link PermissiveComboBox}. An ordinary non-editable JComboBox
 * does not allow setting an item that is not in it's listModel.
 */
public class DataSourceModel implements ComboBoxModel
{
	List<ListDataListener> listeners = new ArrayList<ListDataListener>();
	private List<DataSource> items = new ArrayList<DataSource>();

	DataSource selectedItem;

	public Object getSelectedItem()
	{
		return selectedItem;
	}

	/** same as getSelectedItem, but type safe. */
	public DataSource getSelectedDataSource()
	{
		return selectedItem;
	}

	private void fireEvent(ListDataEvent e)
	{
		// fire change event
		for (ListDataListener listener : listeners)
		{
			listener.contentsChanged(e);
		}
	}

	/**
	 * @param value may be null, but should be of type DataSource, otherwise you get a ClassCastException
	 */
	public void setSelectedItem(Object value)
	{
		selectedItem = (DataSource)value;
		fireEvent(new ListDataEvent (this, ListDataEvent.CONTENTS_CHANGED, 0, 0));
	}

	public void addListDataListener(ListDataListener arg0)
	{
		listeners.add (arg0);
	}

	public Object getElementAt(int arg0)
	{
		return items.get(arg0);
	}

	public int getSize()
	{
		return items.size();
	}

	public void removeListDataListener(ListDataListener arg0)
	{
		listeners.remove(arg0);
	}

	public DataSourceModel()
	{
		super();
		initItems();
	}

	/**
	 * refresh combobox in response to e.g. changes in the list
	 * of available data sources
	 */
	private void initItems()
	{
		items = new ArrayList<DataSource>();
		items.addAll (DataSourceHandler.getFilteredSetAlt(primary, type, organism));
		Collections.sort (items, new Comparator<DataSource>()
		{
			public int compare(DataSource arg0, DataSource arg1)
			{
				String f0 = arg0.getFullName();
				String f1 = arg1.getFullName();
				if (f0 != null)
				{
					return f1 == null ? -1 : f0.compareTo (f1);
				}
				else
				{
					return f1 == null ? 0 : 1;
				}
			}
		});

		ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
				0, items.size());

		fireEvent(e);
	}

	private Organism organism = null;
	private String[] type = null;
	private Boolean primary = null;

	public void setSpeciesFilter (Organism aOrganism)
	{
		organism = aOrganism;
		initItems();
	}

	public void setMetaboliteFilter (Boolean aMetabolite)
	{
		type = new String[] { "metabolite" };
		initItems();
	}

	public void setPrimaryFilter (Boolean aPrimary)
	{
		primary = aPrimary;
		initItems();
	}

	public void setTypeFilter (String [] aType)
	{
		type = aType;
		initItems();
	}
}
