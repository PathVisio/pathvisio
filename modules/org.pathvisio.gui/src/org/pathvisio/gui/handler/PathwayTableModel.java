/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.gui.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElementEvent;
import org.pathvisio.core.model.PathwayElementListener;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.SelectionBox.SelectionEvent;
import org.pathvisio.core.view.SelectionBox.SelectionListener;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.gui.SwingEngine;

/**
 * The model for the table in the Properties side panel.
 * Each row corresponds to a Property, the first column is the name of the
 * property and the second column its value.
 *
 * This will pass through the properties for zero, one or many selected
 * PathwayElements. If many are selected, the subset of shared Properties
 * is used as row set.
 */
public class PathwayTableModel extends AbstractTableModel implements SelectionListener,
									PathwayElementListener,
									ApplicationEventListener {

	private JTable table;
	final private Collection<PathwayElement> input;
	final private Map<Object, PropertyView> propertyValues;
	final private List<PropertyView> shownProperties;

	private SwingEngine swingEngine;

	public PathwayTableModel(SwingEngine swingEngine) {
		input = new HashSet<PathwayElement>();
		propertyValues = new HashMap<Object, PropertyView>();
		shownProperties = new ArrayList<PropertyView>();
		this.swingEngine = swingEngine;
		swingEngine.getEngine().addApplicationEventListener(this);
		VPathway vp = swingEngine.getEngine().getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	private void reset() {
		stopEditing();
		for(PathwayElement e : input) {
			//System.err.println("Removed " + e);
			e.removeListener(this);
		}
		propertyValues.clear();
		shownProperties.clear();
		input.clear();
		refresh(true);
	}

	private void removeInput(PathwayElement pwElm) {
		stopEditing();
		//System.err.println("Input removed");
		input.remove(pwElm);
		if (input.size() > 0)
		{
			updatePropertyCounts(pwElm, true);
			pwElm.removeListener(this);
			refresh(true);
		}
		else
		{
			reset();
		}
	}

	private void stopEditing() {
		if(table != null && table.getCellEditor() != null) {
			table.getCellEditor().stopCellEditing();
		}
	}

	private void addInput(PathwayElement pwElm) {
		stopEditing();
		//System.err.println("Input added");
		input.add(pwElm);
		updatePropertyCounts(pwElm, false);
		pwElm.addListener(this);
		refresh(true);
	}

	protected void refresh() { refresh(false); }

	public void refresh(boolean propertyCount) {
		if(propertyCount) {
			updateShownProperties();
		}
		refreshPropertyValues();
		fireTableDataChanged();
	}

	/**
	 * Add/remove properties to/from the table model.
	 *
	 * @param e the PathwayElement with the properties of interest
	 * @param remove true if the PathwayElement's visible properties should be removed from the table model,
	 *  false if it should be added to the table model
	 */
	public void updatePropertyCounts(PathwayElement e, boolean remove)
	{
		for(Object o : PropertyDisplayManager.getVisiblePropertyKeys(e))
		{
			PropertyView tp = propertyValues.get(o);
			if(tp == null) {
				propertyValues.put(o, tp = new PropertyView(swingEngine.getEngine().getActiveVPathway(), o));
			}
			if(remove) {
				tp.removeElement(e);
			} else {
				tp.addElement(e);
			}
		}
	}

	protected void updateShownProperties() {
		for(PropertyView tp : propertyValues.values()) {
			boolean shown = shownProperties.contains(tp);
			if(tp.elementCount() == input.size()) {
				//System.err.println("\tadding " + tp + " from shown");
				if(!shown) shownProperties.add(tp);
			} else {
				//System.err.println("\tremoveing " + tp + " from shown");
				shownProperties.remove(tp);
			}
			Collections.sort(shownProperties);
		}
	}

	protected void refreshPropertyValues() {
		for(PropertyView p : shownProperties) {
			p.refreshValue();
		}
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return shownProperties.size();
	}

	public PropertyView getPropertyAt(int row) {
		return shownProperties.get(row);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		PropertyView p = getPropertyAt(rowIndex);
		if(columnIndex == 0) return p.getName();
		else return p.getValue();
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex != 0) {
			PropertyView p = getPropertyAt(rowIndex);
			p.setValue(aValue);
		}
		swingEngine.getEngine().getActiveVPathway().redrawDirtyRect();
	}

	public String getColumnName(int column) {
		if(column == 0) return "Property";
		return "Value";
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1 &&
				swingEngine.getEngine().hasVPathway() &&
				swingEngine.getEngine().getActiveVPathway().isEditMode();
	}

	public void selectionEvent(SelectionEvent e) {
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			//System.err.println("OBJECT ADDED");
			if(e.affectedObject instanceof Graphics)
				addInput(((Graphics)e.affectedObject).getPathwayElement());
			break;
		case SelectionEvent.OBJECT_REMOVED:
			//System.err.println("OBJECT REMOVED");
			if(e.affectedObject instanceof Graphics)
				removeInput(((Graphics)e.affectedObject).getPathwayElement());
			break;
		case SelectionEvent.SELECTION_CLEARED:
			//System.err.println("CLEARED");
			 reset();
			break;
		}
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		PropertyView propHandler = getPropertyAt(row);
		if (propHandler != null) {
			if (column == 0) {
				return propHandler.getLabelRenderer();
			} else {
				return propHandler.getCellRenderer();
			}
		}
		return null;
	}

	public TableCellEditor getCellEditor(int row, int column) {
		if(column != 0) {
			PropertyView tp = getPropertyAt(row);
			if(tp != null) return tp.getCellEditor(swingEngine);
		}
		return null;
	}

	public void gmmlObjectModified(PathwayElementEvent e) {
		refresh();
	}

	public void applicationEvent(ApplicationEvent e)
	{
		switch(e.getType())
		{
		case VPATHWAY_CREATED:
			((VPathway)e.getSource()).addSelectionListener(this);
			break;
		case VPATHWAY_DISPOSED:
			((VPathway)e.getSource()).removeSelectionListener(this);
			reset(); // clear selected set
			break;
		}
	}
}
