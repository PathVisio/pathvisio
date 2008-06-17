// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.Organism;
import org.pathvisio.model.OrientationType;
import org.pathvisio.model.OutlineType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.ShapeType;

public class TypedProperty implements Comparable<TypedProperty> {	
	Collection<PathwayElement> elements;
	Object value;
	PropertyType type;
	boolean different;

	public TypedProperty(PropertyType type) {
		this.type = type;
		elements = new HashSet<PathwayElement>();
	}
	
	public void addElement(PathwayElement e) {
		elements.add(e);
		refreshValue();		
	}
	
	public void removeElement(PathwayElement e) {
		elements.remove(e);
		refreshValue();
	}
	
	public void refreshValue() {
		boolean first = true;
		for(PathwayElement e : elements) {
			Object o = e.getProperty(type);
			if(!first && (o == null || !o.equals(value))) {
				setHasDifferentValues(true);
				return;
			}
			value = o;
			first = false;
		}
	}
	
	public int elementCount() { return elements.size(); }
	
	public int compareTo(TypedProperty o) 
	{
		return type.compareTo(o.getType());
	}
	
	public void setValue(Object value) {
		setValue(value, true);
	}
	
	public void setValue(Object value, boolean setElement) {
		this.value = value;
		if(value != null && setElement) {
			Engine.getCurrent().getActiveVPathway().getUndoManager().newAction (
					"Change " + type + " property");
			for(PathwayElement e : elements) {
				e.setProperty(type, value);
			}
		}
	}

	public Object getValue() {
		return value;
	}
	
	public PropertyType getType() {
		return type;
	}

	public boolean hasDifferentValues() { return different; }
	public void setHasDifferentValues(boolean diff) { different = diff; }

	public TableCellRenderer getCellRenderer() {
		if(hasDifferentValues()) return differentRenderer;
		switch(type.type()) {
		case COLOR:
			return colorRenderer;
		case LINETYPE:
			return lineTypeRenderer;
		case LINESTYLE:
			return lineStyleRenderer;
		case DATASOURCE:
		{
			Set<DataSource> dataSources = DataSource.getDataSources();
			if(dataSources.size() > datasourceRenderer.getItemCount()) {
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
		return null;
	}

	public TableCellEditor getCellEditor() {
		switch(type.type()) {
		case BOOLEAN:
			return checkboxEditor;
		case DATASOURCE:
		{
			Set<DataSource> dataSources = DataSource.getDataSources();
			if(dataSources.size() > datasourceEditor.getItemCount()) {
				Object[] labels = new Object[dataSources.size()];
				Object[] values = new Object[dataSources.size()];
				int i = 0;
				for(DataSource s : dataSources) {
					labels[i] = s.getFullName();
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
		case FONT:
			return fontEditor;
		case SHAPETYPE:
			return shapeTypeEditor;
		case COMMENTS:
			commentsEditor.setInput(this);
			return commentsEditor;
		case OUTLINETYPE:
			return outlineTypeEditor;
		case GENETYPE:
			return datanodeTypeEditor;
		default:
			return null;
		}
	}
	
	private PathwayElement getFirstElement() {
		PathwayElement first;
		for(first = (PathwayElement)elements.iterator().next();;) break;
		return first;
	}
	
	private static class DoubleEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;
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
	
	private static class AngleEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;
		
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
	
	private static class ComboEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;
		
		HashMap<Object, Object> label2value;
		HashMap<Object, Object> value2label;
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
		private static final long serialVersionUID = 1L;

		static final String BUTTON_LABEL = "View/edit comments";
		JButton button;
		PathwayElement currentElement;
		TypedProperty property;
		
		protected static final String EDIT = "edit";

		public CommentsEditor() {
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
					PathwayElementDialog d = PathwayElementDialog.getInstance(currentElement, false, null, this.button);
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
		private static final long serialVersionUID = 1L;

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
	private static ComboEditor fontEditor = new ComboEditor(GraphicsEnvironment
			.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(), false);
	private static ComboEditor shapeTypeEditor= new ComboEditor(ShapeType.getValues(), false);
	private static DefaultTableCellRenderer angleRenderer = new DefaultTableCellRenderer() {
		private static final long serialVersionUID = 1L;

		protected void setValue(Object value) {
			super.setValue( (Double)(value) * 180.0 / Math.PI );
		}
	};
	private static CommentsEditor commentsEditor = new CommentsEditor();
	private static ComboEditor datanodeTypeEditor = new ComboEditor(DataNodeType.getNames(), false);
	
	private static DefaultTableCellRenderer doubleRenderer = new DefaultTableCellRenderer() {
		private static final long serialVersionUID = 1L;

		protected void setValue(Object value) {
			double d = (Double)value;
			super.setValue(d);
		}
	};
	
	private static DefaultTableCellRenderer differentRenderer = new DefaultTableCellRenderer() {
		private static final long serialVersionUID = 1L;

		protected void setValue(Object value) {
			value = "Different values";
			super.setValue(value);
		}
	};

	private static class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setSelected((Boolean)value);
			return this;
		}
	}
	
	private static class ComboRenderer extends JComboBox implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		HashMap<Object, Object> value2label;
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
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			String fn = (String)value;
			Font f = getFont();
			setFont(new Font(fn, f.getStyle(), f.getSize()));
			setText(fn);
			return this;
		}
	}
	
	private static class ColorRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

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
}
