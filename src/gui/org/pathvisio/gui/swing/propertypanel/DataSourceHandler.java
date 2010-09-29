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
package org.pathvisio.gui.swing.propertypanel;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.StaticPropertyType;
import org.pathvisio.util.swing.PermissiveComboBox;

/**
 * This class knows how to handle a datasource, which is context sensitive and needs to be updated before use.
 */
public class DataSourceHandler extends DefaultCellEditor implements ContextSensitiveEditor, TableCellRenderer,
		TypeHandler {
	private PermissiveComboBox renderer;
	private JComboBox editor;

	public DataSourceHandler() {
		super(new JComboBox(new String[0]));
		editor = (JComboBox)getComponent();
		editor.setBorder(BorderFactory.createEmptyBorder());
		renderer = new PermissiveComboBox();
	}

	//-- ContextSensitiveEditor methods --//

	//TODO: make part of org.bridgedb.DataSource
	/**
	 * returns a filtered subset of available datasources.
	 * @param type Filter for specified type. If null, don't filter on primary-ness.
	 * @param o Filter for specified organism. If null, don't filter on organism.
	 * @return filtered set.
	 */
	public static Set<DataSource> getFilteredSetAlt (Boolean primary, String[] type, Object o)
	{
		final Set<DataSource> result = new HashSet<DataSource>();
		final Set<String> types = new HashSet<String>();
		if (type != null) types.addAll(Arrays.asList(type));
		for (DataSource ds : DataSource.getDataSources())
		{
			if (
					(primary == null || primary == ds.isPrimary()) &&
					(type == null || types.contains(ds.getType())) &&
					(o == null || ds.getOrganism() == null || o == ds.getOrganism())
				)
			{
				result.add (ds);
			}
		}
		return result;
	}

	public static final Map<String, String[]> DSTYPE_BY_DNTYPE = new HashMap<String, String[]>();
	static
	{
		DSTYPE_BY_DNTYPE.put (DataNodeType.UNKOWN.getName(), null);
		DSTYPE_BY_DNTYPE.put (DataNodeType.METABOLITE.getName(), new String[] {"metabolite"});
		DSTYPE_BY_DNTYPE.put (DataNodeType.COMPLEX.getName(), null);
		DSTYPE_BY_DNTYPE.put (DataNodeType.PATHWAY.getName(), new String[] {"pathway"});
		DSTYPE_BY_DNTYPE.put (DataNodeType.PROTEIN.getName(), new String[] {"gene", "protein"});
		DSTYPE_BY_DNTYPE.put (DataNodeType.GENEPRODUCT.getName(), new String[] {"gene", "protein"});
		DSTYPE_BY_DNTYPE.put (DataNodeType.RNA.getName(), new String[] {"gene", "protein"});
	}

	public void updateEditor(SwingEngine swingEngine, Collection<PathwayElement> elements,
			Pathway pathway, PropertyView propHandler)
	{
		boolean first = true;
		String dnType = null;
		for (PathwayElement element : elements)
		{
			if (first)
			{
				dnType = element.getDataNodeType();
				first = false;
			}
			else
			{
				if (dnType != element.getDataNodeType())  // mix of types
					dnType = null;
			}
		}
		
		SortedSet<DataSource> dataSources = new TreeSet<DataSource>(new Comparator<DataSource>() {
			public int compare(DataSource arg0, DataSource arg1) {
				return ("" + arg0.getFullName()).toLowerCase().compareTo(("" + arg1.getFullName()).toLowerCase());
			}
		});
		
		String[] dsType = null; // null is default: no filtering
		if (DSTYPE_BY_DNTYPE.containsKey(dnType)) dsType = DSTYPE_BY_DNTYPE.get(dnType);

		dataSources.addAll(getFilteredSetAlt(true, dsType, 
				Organism.fromLatinName(pathway.getMappInfo().getOrganism())));

		if (isDifferent(dataSources)) {
			renderer.removeAllItems();
			editor.removeAllItems();
			for (DataSource s : dataSources) {
				String name = value2label (s);
				renderer.addItem(name);
				editor.addItem(name);
			}
		}
	}
	
	private DataSource label2value(String label)
	{
		if (DataSource.getFullNames().contains(label))
			return DataSource.getByFullName(label);
		else
			return DataSource.getBySystemCode(label);
	}

	private String value2label(DataSource value)
	{
		if (value == null) return null;
		String result = value.getFullName();
		if (result == null) result = value.getSystemCode();
		return result;
	}
	
	private boolean isDifferent(SortedSet<DataSource> dataSources) {

		if (editor.getItemCount() != dataSources.size()) {
			return true;
		}
		Iterator<DataSource> it = dataSources.iterator();
		for (int x = 0; x < editor.getItemCount(); x++) {
			if (!editor.getItemAt(x).equals(it.next().getFullName())) {
				return true;
			}
		}
		return false;
	}



	//-- TypeHandler methods --//

	public PropertyType getType() {
		return StaticPropertyType.DATASOURCE;
	}

	public TableCellRenderer getLabelRenderer() {
		return null;
	}

	public TableCellRenderer getValueRenderer() {
		return this;
	}

	public TableCellEditor getValueEditor() {
		return this;
	}


	//-- TableCellRenderer methods --//

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (isSelected) {
			renderer.setForeground(table.getSelectionForeground());
			renderer.setBackground(table.getSelectionBackground());
		} else {
			renderer.setForeground(table.getForeground());
			renderer.setBackground(table.getBackground());
		}

		Object o = value2label((DataSource)value);
		renderer.setSelectedItem(o);
		return renderer;
	}


	//-- TableCellEditor methods --//

	public Object getCellEditorValue() {

		return label2value((String)editor.getSelectedItem());
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		editor.setSelectedItem(value2label((DataSource)value));
		return editor;
	}

}
