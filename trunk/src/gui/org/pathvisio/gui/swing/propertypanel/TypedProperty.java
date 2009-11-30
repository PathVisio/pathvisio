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
package org.pathvisio.gui.swing.propertypanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.GroupStyle;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.OrientationType;
import org.pathvisio.model.OutlineType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.ShapeType;
import org.pathvisio.view.VPathway;

/**
 * TypedProperty ties together functionality to view / edit a property
 * on one or more PathwayElements at the same time
 */
public class TypedProperty implements Comparable<TypedProperty> {
	Collection<PathwayElement> elements;
	Object value;
	Object type;
	boolean different;

	/**
	 * @param aType is either String for a dynamic property,
	 * or PropertyType for a static property;
	 * @param aVPathway is used to register undo actions when setting a value
	 * to this property. May be null, in which case no undo actions are registered.
	 */
	public TypedProperty(VPathway aVPathway, Object aType) {
		type = aType;
		if (!(type instanceof String || type instanceof PropertyType))
		{
			throw new IllegalArgumentException();
		}
		vPathway = aVPathway;
		elements = new HashSet<PathwayElement>();
	}

	/**
	 * Add a PathwayElement to the set of elements that are viewed / edited together
	 */
	public void addElement(PathwayElement e) {
		elements.add(e);
		refreshValue();
	}

	/**
	 * Remove a PathwayElement to the set of elements that are viewed / edited together
	 */
	public void removeElement(PathwayElement e) {
		elements.remove(e);
		refreshValue();
	}

	/**
	 * Refresh the viewer / editor value by checking all PathwayElements
	 * This notifies the TypedProperty that one of the PathwayElements has changed
	 * or that the PathwayElement list has been changed, and a new value should be cached.
	 */
	public void refreshValue() {
		boolean first = true;
		for(PathwayElement e : elements) {
			Object o = e.getPropertyEx(type);
			if(!first && (o == null || !o.equals(value))) {
				different = true;
				return;
			}
			value = o;
			first = false;
		}
	}

	/**
	 * Number of PathwayElement's being edited / viewed
	 */
	public int elementCount() { return elements.size(); }

	/**
	 * Get a description for the property being edited.
	 */
	public String getDesc()
	{
		if (type instanceof PropertyType)
		{
			return ((PropertyType)type).desc();
		}
		else
		{
			return type.toString();
		}
	}

	/**
	 * Set a value for the property being edited.
	 * This will update all PathwayElements that are being edited at once.
	 */
	public void setValue(Object value) {
		this.value = value;
		if(value != null) {
			if (vPathway != null)
			{
				vPathway.getUndoManager().newAction (
					"Change " + type + " property");
			}
			for(PathwayElement e : elements) {
				e.setPropertyEx(type, value);
			}
		}
	}

	/**
	 * The value of the property being viewed / edited.
	 * This value is cached, call refreshValue() to update the cache.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * The type of the property being edited. This is a String
	 * if the property is dynamic, or a PropertyType is the property
	 * is static. (See PathwayElement for an explanation of static / dynamic)
	 */
	public Object getType() {
		return type;
	}

	/**
	 * Returns true if the PathwayElement's being edited differ for this Property.
	 */
	public boolean hasDifferentValues() { return different; }


	private VPathway vPathway;

	/**
	 * Returns a TableCellRenderer suitable for rendering this property
	 */
	public TableCellRenderer getCellRenderer()
	{
		if(hasDifferentValues()) return differentRenderer;
		if (type instanceof PropertyType)
		{
			switch(((PropertyType)type).type())
			{
				case COLOR:
					return colorRenderer;
				case LINETYPE:
					return lineTypeRenderer;
				case LINESTYLE:
					return lineStyleRenderer;
				case DATASOURCE:
				{
					//TODO Make use of DataSourceModel for datasources
					Set<DataSource> dataSources = DataSource.getFilteredSet(true, null, null);
					if(dataSources.size() != datasourceRenderer.getItemCount()) {
						Object[] labels = new Object[dataSources.size()];
						Object[] values = new Object[dataSources.size()];
						int i = 0;
						for(DataSource s : dataSources) {
							labels[i] = s.getFullName();
							values[i] = s;
							i++;
						}
						datasourceRenderer.updateData(labels, values);
					}
					return datasourceRenderer;
				}
				case BOOLEAN:
					return checkboxRenderer;
				case ORIENTATION:
					return orientationRenderer;
				case ORGANISM:
					return organismRenderer;
				case ANGLE:
					return angleRenderer;
				case DOUBLE:
					return doubleRenderer;
				case FONT:
					return fontRenderer;
				case SHAPETYPE:
					return shapeTypeRenderer;
				case OUTLINETYPE:
					return outlineTypeRenderer;
				case GENETYPE:
					return datanodeTypeRenderer;
			}
		}
		return null;
	}

	/**
	 * Returns a TableCellEditor suitable for editing this property.
	 *
	 * @param swingEngine: the comments editor requires a connection to swingEngine, so you need to pass it here.
	 */
	public TableCellEditor getCellEditor(SwingEngine swingEngine) {
		if (type instanceof PropertyType)
		{
			switch(((PropertyType)type).type())
			{
				case BOOLEAN:
					return checkboxEditor;
				case DATASOURCE:
				{
					List<DataSource> dataSources = new ArrayList<DataSource>();
					dataSources.addAll (DataSource.getFilteredSet(true, null,
							Organism.fromLatinName(vPathway.getPathwayModel().getMappInfo().getOrganism())));
					if(dataSources.size() != datasourceEditor.getItemCount())
					{
						Collections.sort (dataSources, new Comparator<DataSource>() {

							public int compare(DataSource arg0, DataSource arg1)
							{
								return ("" + arg0.getFullName()).toLowerCase().compareTo(("" + arg1.getFullName()).toLowerCase());
							}});

						Object[] labels = new Object[dataSources.size()];
						Object[] values = new Object[dataSources.size()];
						int i = 0;
						for(DataSource s : dataSources) {
							labels[i] = s.getFullName() == null ? s.getSystemCode() : s.getFullName();
							values[i] = s;
							i++;
						}
						datasourceEditor.updateData(labels, values);
					}
					return datasourceEditor;
				}
				case COLOR:
					return colorEditor;
				case LINETYPE:
					return lineTypeEditor;
				case LINESTYLE:
					return lineStyleEditor;
				case ORIENTATION:
					return orientationEditor;
				case ORGANISM:
					return organismEditor;
				case ANGLE:
					return angleEditor;
				case DOUBLE:
					return doubleEditor;
				case INTEGER:
					return integerEditor;
				case FONT:
					return fontEditor;
				case SHAPETYPE:
					return shapeTypeEditor;
				case COMMENTS:
					CommentsEditor commentsEditor = new CommentsEditor(swingEngine);
					commentsEditor.setInput(this);
					return commentsEditor;
				case OUTLINETYPE:
					return outlineTypeEditor;
				case GENETYPE:
					return datanodeTypeEditor;
				case GROUPSTYLETYPE:
					return groupStyleEditor;
				default:
					return null;
			}
		}
		else return null;
	}

	/**
	 * Return the first of the set of PathwayElement's
	 */
	private PathwayElement getFirstElement()
	{
		return elements.iterator().next();
	}

	private static class DoubleEditor extends DefaultCellEditor {
		public DoubleEditor() {
			super(new JTextField());
		}
		public Object getCellEditorValue() {
			String value = ((JTextField)getComponent()).getText();
			Double d = new Double(0);
			try {
				d = Double.parseDouble(value);
			} catch(Exception e) {
				//ignore
			}
			return d;
		}
	}

	private static class IntegerEditor extends DefaultCellEditor {

		public IntegerEditor() {
			super(new JTextField());
		}
		public Object getCellEditorValue() {
			String value = ((JTextField)getComponent()).getText();
			Integer i = new Integer(0);
			try {
				i = Integer.parseInt(value);
			} catch(Exception e) {
				//ignore
			}
			return i;
		}
	}

	private static class AngleEditor extends DefaultCellEditor {

		public AngleEditor() {
			super(new JTextField());
		}
		public Object getCellEditorValue() {
			String value = ((JTextField)getComponent()).getText();
			Double d = new Double(0);
			try {
				d = Double.parseDouble(value) * Math.PI / 180;
			} catch(Exception e) {
				//ignore
			}
			return d;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			value =  (Double)(value) * 180.0 / Math.PI;
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}

	//TODO: merge with ComboRenderer
	private static class ComboEditor extends DefaultCellEditor {

		Map<Object, Object> label2value;
		Map<Object, Object> value2label;
		boolean useIndex;
		JComboBox combo;

		public ComboEditor(boolean editable, Object[] labels, boolean useIndex) {
			this(labels, useIndex);
			combo.setEditable(editable);
		}

		public ComboEditor(Object[] labels, boolean useIndex) {
			super(new JComboBox(labels));
			combo = (JComboBox)getComponent();
			this.useIndex = useIndex;
		}

		public ComboEditor(Object[] labels, Object[] values) {
			this(labels, false);
			if(values != null) {
				updateData(labels, values);
			}
		}

		public int getItemCount() {
			return label2value.size();
		}

		public void updateData(Object[] labels, Object[] values) {
			combo.setModel(new DefaultComboBoxModel(labels));
			if(values != null) {
				if(labels.length != values.length) {
					throw new IllegalArgumentException("Number of labels doesn't equal number of values");
				}
				if(label2value == null) label2value = new HashMap<Object, Object>();
				else label2value.clear();
				if(value2label == null) value2label = new HashMap<Object, Object>();
				else value2label.clear();
				for(int i = 0; i < labels.length; i++) {
					label2value.put(labels[i], values[i]);
					value2label.put(values[i], labels[i]);
				}
			}
		}

		public Object getCellEditorValue() {
			if(label2value == null) { //Use index
				JComboBox cb = (JComboBox)getComponent();
				return useIndex ? cb.getSelectedIndex() : cb.getSelectedItem();
			} else {
				Object label = super.getCellEditorValue();
				return label2value.get(label);
			}
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if(value2label != null) {
				value = value2label.get(value);
			}
			if(useIndex) {
				combo.setSelectedIndex((Integer)value);
			} else {
				combo.setSelectedItem(value);
			}
			return combo;
		}
	}

	private static class CommentsEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		static final String BUTTON_LABEL = "View/edit comments";
		JButton button;
		PathwayElement currentElement;
		TypedProperty property;

		protected static final String EDIT = "edit";

		private SwingEngine swingEngine;

		public CommentsEditor(SwingEngine swingEngine) {
			this.swingEngine = swingEngine;
			button = new JButton();
			button.setText(BUTTON_LABEL);
			button.setActionCommand("edit");
			button.addActionListener(this);
		}

		public void setInput(TypedProperty p) {
			property = p;
			button.setText("");
			if(!mayEdit()) fireEditingCanceled();
			button.setText(BUTTON_LABEL);
		}

		boolean mayEdit() { return property.elements.size() == 1; }

		public void actionPerformed(ActionEvent e) {
			if(!mayEdit()) {
				fireEditingCanceled();
				return;
			}
			if (EDIT.equals(e.getActionCommand()) && property != null) {
				currentElement = property.getFirstElement();
				if(currentElement != null) {
					PathwayElementDialog d = PathwayElementDialog.getInstance(swingEngine, currentElement, false, null, this.button);
					d.selectPathwayElementPanel(PathwayElementDialog.TAB_COMMENTS);
					d.setVisible(true);
					fireEditingCanceled(); //Value is directly saved in dialog
				}
			}
		}

		public Object getCellEditorValue() {
			return currentElement.getComments();
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value,
				boolean isSelected,
				int row,
				int column) {
			return button;
		}
	}

	private static class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		Color currentColor;
		JButton button;
		JDialog dialog;
		protected static final String EDIT = "edit";

		public ColorEditor() {
			button = new JButton();
			button.setActionCommand("edit");
			button.addActionListener(this);
			button.setBorderPainted(false);
		}

		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				button.setBackground(currentColor);

				Color newColor = JColorChooser.showDialog(button, "Choose a color", currentColor);
				if(newColor != null) currentColor = newColor;
				fireEditingStopped(); //Make the renderer reappear
			}
		}

		public Object getCellEditorValue() {
			return currentColor;
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value,
				boolean isSelected,
				int row,
				int column) {
			currentColor = (Color)value;
			return button;
		}
	}

	private static ColorRenderer colorRenderer = new ColorRenderer();
	private static ComboRenderer lineTypeRenderer = new ComboRenderer(LineType.getNames(), LineType.getValues());
	private static ComboRenderer lineStyleRenderer = new ComboRenderer(LineStyle.getNames());
	private static ComboRenderer datasourceRenderer = new ComboRenderer(new String[] {}, new String[] {});//data will be added on first use
	private static CheckBoxRenderer checkboxRenderer = new CheckBoxRenderer();
	private static ComboRenderer orientationRenderer = new ComboRenderer(OrientationType.getNames());
	private static ComboRenderer organismRenderer = new ComboRenderer(Organism.latinNames().toArray());
	private static FontRenderer fontRenderer = new FontRenderer();
	private static ComboRenderer shapeTypeRenderer = new ComboRenderer(ShapeType.getNames(), ShapeType.getValues());
	private static ComboRenderer outlineTypeRenderer = new ComboRenderer(OutlineType.getTags(), OutlineType.values());
	private static ComboRenderer datanodeTypeRenderer = new ComboRenderer(DataNodeType.getNames());
	private static ColorEditor colorEditor = new ColorEditor();
	private static ComboEditor lineTypeEditor = new ComboEditor(LineType.getNames(), LineType.getValues());
	private static ComboEditor lineStyleEditor = new ComboEditor(LineStyle.getNames(), true);
	private static ComboEditor outlineTypeEditor = new ComboEditor(OutlineType.getTags(), OutlineType.values());
	private static ComboEditor datasourceEditor = new ComboEditor(new String[] {}, new String[] {}); //data will be added on first use
	private static DefaultCellEditor checkboxEditor = new DefaultCellEditor(new JCheckBox());
	private static ComboEditor orientationEditor = new ComboEditor(OrientationType.getNames(), true);
	private static ComboEditor organismEditor = new ComboEditor(true, Organism.latinNames().toArray(), false);
	private static AngleEditor angleEditor = new AngleEditor();
	private static DoubleEditor doubleEditor = new DoubleEditor();
	private static IntegerEditor integerEditor = new IntegerEditor();
	private static ComboEditor fontEditor = new ComboEditor(GraphicsEnvironment
			.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(), false);
	private static ComboEditor shapeTypeEditor= new ComboEditor(ShapeType.getValues(), false);
	private static ComboEditor groupStyleEditor = new ComboEditor(GroupStyle.getNames(), false);

	private static DefaultTableCellRenderer angleRenderer = new DefaultTableCellRenderer() {

		protected void setValue(Object value) {
			super.setValue( (Double)(value) * 180.0 / Math.PI );
		}
	};

	private static ComboEditor datanodeTypeEditor = new ComboEditor(DataNodeType.getNames(), false);

	private static DefaultTableCellRenderer doubleRenderer = new DefaultTableCellRenderer() {

		protected void setValue(Object value) {
			if (value != null){ //hack needed to remove NPE following group refactoring
				double d = (Double)value;
				super.setValue(d);
			}
		}
	};

	private static DefaultTableCellRenderer differentRenderer = new DefaultTableCellRenderer() {

		protected void setValue(Object value) {
			value = "Different values";
			super.setValue(value);
		}
	};

	private static class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setSelected((Boolean)value);
			return this;
		}
	}

	//TODO: merge with ComboEditor
	private static class ComboRenderer extends JComboBox implements TableCellRenderer {

		Map<Object, Object> value2label;
		public ComboRenderer(Object[] values) {
			super(values);
		}

		public ComboRenderer(Object[] labels, Object[] values) {
			this(labels);
			if(labels.length != values.length) {
				throw new IllegalArgumentException("Number of labels doesn't equal number of values");
			}
			updateData(labels, values);
		}

		public void updateData(Object[] labels, Object[] values) {
			setModel(new DefaultComboBoxModel(labels));
			if(values != null) {
				if(labels.length != values.length) {
					throw new IllegalArgumentException("Number of labels doesn't equal number of values");
				}
				value2label = new HashMap<Object, Object>();
				for(int i = 0; i < labels.length; i++) {
					value2label.put(values[i], labels[i]);
				}
			}
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if(value2label != null) {
				value = value2label.get(value);
			}
			if(value instanceof Integer) {
				setSelectedIndex((Integer)value);
			} else {
				setSelectedItem(value);
			}
			return this;
		}
	}

	private static class FontRenderer extends JLabel implements TableCellRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			String fn = (String)value;
			Font f = getFont();
			setFont(new Font(fn, f.getStyle(), f.getSize()));
			setText(fn);
			return this;
		}
	}

	private static class ColorRenderer extends JLabel implements TableCellRenderer {

		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ColorRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(
				JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
			Color newColor = color != null ? (Color)color : Color.WHITE;
			setBackground(newColor);
			if (isBordered) {
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
								table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
								table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}

			setToolTipText("RGB value: " + newColor.getRed() + ", "
					+ newColor.getGreen() + ", "
					+ newColor.getBlue());
			return this;
		}
	}

	public int compareTo(TypedProperty arg0)
	{
		if (arg0 == null) throw new NullPointerException();

		if (type.getClass() != arg0.type.getClass())
		{
			return type instanceof PropertyType ? 1 : -1;
		}
		else
		{
			if (type instanceof PropertyType)
			{
				return ((PropertyType)type).getOrder() - ((PropertyType)arg0.type).getOrder();
			}
			else
			{
				return type.toString().compareTo(arg0.type.toString());
			}
		}
	}
}
